package org.springframework.data.repository.query;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.core.annotation.AnnotationUtils;

import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.ProjectionInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import th.co.geniustree.springdata.jpa.annotation.FieldProperty;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class ReturnTypeWarpper {

    private static final Map<ReturnTypeWarpper.CacheKey, ReturnTypeWarpper> CACHE = new ConcurrentReferenceHashMap<>(32);
    
    private final @NonNull ProjectionInformation information;
    private final @NonNull Class<?> returnedType;

    private final @NonNull
    Class<?> domainType;

    public static ReturnTypeWarpper of(Class<?> returnedType, Class<?> domainType, ProjectionFactory factory) {
        Assert.notNull(returnedType, "Returned type must not be null!");
        Assert.notNull(domainType, "Domain type must not be null!");
        Assert.notNull(factory, "ProjectionFactory must not be null!");

        return CACHE.computeIfAbsent(ReturnTypeWarpper.CacheKey.of(returnedType, domainType, factory.hashCode()), key -> {

            return returnedType.isInterface()
                    ? new ReturnTypeWarpper.ReturnedInterface(factory.getProjectionInformation(returnedType), returnedType, domainType)
                    : new ReturnTypeWarpper.ReturnedClass(factory.getProjectionInformation(returnedType),returnedType, domainType);
        });
//    return ReturnedType.of(returnedType, domainType, factory);
    }

    /**
     * Returns the entity type.
     *
     * @return
     */
    public final Class<?> getDomainType() {
        return domainType;
    }

    /**
     * Returns whether the given source object is an instance of the returned
     * type.
     *
     * @param source can be {@literal null}.
     * @return
     */
    public final boolean isInstance(@Nullable Object source) {
        return getReturnedType().isInstance(source);
    }

    /**
     * Returns whether the type is projecting, i.e. not of the domain type.
     *
     * @return
     */
    public abstract boolean isProjecting();

    /**
     * Returns the type of the individual objects to return.
     *
     * @return
     */
    public abstract Class<?> getReturnedType();

    /**
     * Returns whether the returned type will require custom construction.
     *
     * @return
     */
    public abstract boolean needsCustomConstruction();

    /**
     * Returns the type that the query execution is supposed to pass to the
     * underlying infrastructure. {@literal null} is returned to indicate a
     * generic type (a map or tuple-like type) shall be used.
     *
     * @return
     */
    @Nullable
    public abstract Class<?> getTypeToRead();

    /**
     * Returns the properties required to be used to populate the result.
     *
     * @return
     */
    public abstract List<String> getInputProperties();
    
    public List<PropertyDescriptor> getInputPropertiesDescritors(){
        return information.getInputProperties();
    }
    
    public Map<String, PropertyDescriptor> getMappedProperties(){
        Map<String, PropertyDescriptor> mapped = new HashMap<>();
        boolean isInterface = returnedType.isInterface();
        information.getInputProperties().stream().forEach(p -> {
            FieldProperty fp = p.getReadMethod().getAnnotation(FieldProperty.class);
            if (!isInterface) {
                try {
                    fp = AnnotationUtils.findAnnotation(returnedType.getDeclaredField(p.getName()), FieldProperty.class);
                } catch (Exception ex) {
                }
            }
            if (fp != null && (!isInterface || p.getReadMethod().getAnnotation(org.springframework.beans.factory.annotation.Value.class) != null)) {
                mapped.put(fp.path(), p);
            } else {
                mapped.put(p.getName(), p);
            }
        });
        return mapped;
    }

    /**
     * A {@link ReturnedType} that's backed by an interface.
     *
     * @author Oliver Gierke
     * @since 1.12
     */
    private static final class ReturnedInterface extends ReturnTypeWarpper {

        private final ProjectionInformation information;
        private final Class<?> domainType;

        /**
         * Creates a new {@link ReturnedInterface} from the given
         * {@link ProjectionInformation} and domain type.
         *
         * @param information must not be {@literal null}.
         * @param domainType must not be {@literal null}.
         */
        public ReturnedInterface(ProjectionInformation information, Class<?> returnedType, Class<?> domainType) {
            super(information,returnedType,domainType);

            Assert.notNull(information, "Projection information must not be null!");

            this.information = information;
            this.domainType = domainType;
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ReturnedType#getReturnedType()
         */
        @Override
        public Class<?> getReturnedType() {
            return information.getType();
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ReturnedType#needsCustomConstruction()
         */
        public boolean needsCustomConstruction() {
            return isProjecting() && information.isClosed();
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ReturnedType#isProjecting()
         */
        @Override
        public boolean isProjecting() {
            return !information.getType().isAssignableFrom(domainType);
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ReturnedType#getTypeToRead()
         */
        @Nullable
        @Override
        public Class<?> getTypeToRead() {
            return isProjecting() && information.isClosed() ? null : domainType;
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ReturnedType#getInputProperties()
         */
        @Override
        public List<String> getInputProperties() {

            List<String> properties = new ArrayList<>();

            for (PropertyDescriptor descriptor : information.getInputProperties()) {
                if (!properties.contains(descriptor.getName())) {
                    properties.add(descriptor.getName());
                }
            }

            return properties;
        }
    }

    /**
     * A {@link ReturnedType} that's backed by an actual class.
     *
     * @author Oliver Gierke
     * @since 1.12
     */
    private static final class ReturnedClass extends ReturnTypeWarpper {

        private static final Set<Class<?>> VOID_TYPES = new HashSet<>(Arrays.asList(Void.class, void.class));

        private final Class<?> type;
        private final List<String> inputProperties;

        /**
         * Creates a new {@link ReturnedClass} instance for the given returned
         * type and domain type.
         *
         * @param returnedType must not be {@literal null}.
         * @param domainType must not be {@literal null}.
         */
        public ReturnedClass(ProjectionInformation information, Class<?> returnedType, Class<?> domainType) {
            super(information,returnedType,domainType);

            Assert.notNull(returnedType, "Returned type must not be null!");
            Assert.notNull(domainType, "Domain type must not be null!");
            Assert.isTrue(!returnedType.isInterface(), "Returned type must not be an interface!");

            this.type = returnedType;
            inputProperties = new ArrayList<>();

            for (PropertyDescriptor descriptor : information.getInputProperties()) {
                FieldProperty fp = null;
                try {
                    fp = AnnotationUtils.findAnnotation(returnedType.getDeclaredField(descriptor.getName()), FieldProperty.class);
                } catch (Exception ex) {
                    Logger.getLogger(ReturnTypeWarpper.class.getName()).log(Level.WARNING, "Not found Field");
                }
                String name = fp == null ? descriptor.getName() : fp.path();
                if (!inputProperties.contains(name)) {
                    inputProperties.add(name);
                }
            }
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ResultFactory.ReturnedTypeInformation#getReturnedType()
         */
        @Override
        public Class<?> getReturnedType() {
            return type;
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ResultFactory.ReturnedType#getTypeToRead()
         */
        public Class<?> getTypeToRead() {
            return type;
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ResultFactory.ReturnedType#isProjecting()
         */
        @Override
        public boolean isProjecting() {
            return isDto();
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ResultFactory.ReturnedType#needsCustomConstruction()
         */
        public boolean needsCustomConstruction() {
            return isDto() && !inputProperties.isEmpty();
        }

        /*
		 * (non-Javadoc)
		 * @see org.springframework.data.repository.query.ResultFactory.ReturnedTypeInformation#getInputProperties()
         */
        @Override
        public List<String> getInputProperties() {
            return inputProperties;
        }

        private boolean isDto() {
            return !Object.class.equals(type)
                    && //
                    !type.isEnum()
                    && //
                    !isDomainSubtype()
                    && //
                    !isPrimitiveOrWrapper()
                    && //
                    !Number.class.isAssignableFrom(type)
                    && //
                    !VOID_TYPES.contains(type)
                    && //
                    !type.getPackage().getName().startsWith("java.");
        }

        private boolean isDomainSubtype() {
            return getDomainType().equals(type) && getDomainType().isAssignableFrom(type);
        }

        private boolean isPrimitiveOrWrapper() {
            return ClassUtils.isPrimitiveOrWrapper(type);
        }
    }

    @Value(staticConstructor = "of")
    private static class CacheKey {

        Class<?> returnedType, domainType;
        int projectionFactoryHashCode;
    }
}
