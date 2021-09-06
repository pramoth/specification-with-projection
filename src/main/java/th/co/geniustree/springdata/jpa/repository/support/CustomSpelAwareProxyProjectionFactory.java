package th.co.geniustree.springdata.jpa.repository.support;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.projection.ProjectionInformation;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.type.MethodsMetadata;
import org.springframework.data.type.classreading.MethodsMetadataReader;
import org.springframework.data.type.classreading.MethodsMetadataReaderFactory;
import org.springframework.data.util.StreamUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import th.co.geniustree.springdata.jpa.annotation.FieldProperty;
import th.co.geniustree.springdata.jpa.annotation.Load;

public class CustomSpelAwareProxyProjectionFactory extends SpelAwareProxyProjectionFactory {

    @Override
    protected ProjectionInformation createProjectionInformation(Class<?> projectionType) {
        return new CustomDefaultProjectionInformation(projectionType);
    }

    public class CustomDefaultProjectionInformation implements ProjectionInformation {

        private final Class<?> projectionType;
        private final List<PropertyDescriptor> properties;
        private final List<Field> loaders;

        /**
         * Creates a new {@link DefaultProjectionInformation} for the given
         * type.
         *
         * @param type must not be {@literal null}.
         */
        CustomDefaultProjectionInformation(Class<?> type) {

            Assert.notNull(type, "Projection type must not be null!");

            this.projectionType = type;
            CustomPropertyDescriptorSource descriptor = new CustomPropertyDescriptorSource(type);
            this.properties = descriptor.getDescriptors();
            this.loaders = descriptor.getLoaders();
        }

        /*
	 * (non-Javadoc)
	 * @see org.springframework.data.projection.ProjectionInformation#getType()
         */
        @Override
        public Class<?> getType() {
            return projectionType;
        }

        /*
	 * (non-Javadoc)
	 * @see org.springframework.data.projection.ProjectionInformation#getInputProperties()
         */
        public List<PropertyDescriptor> getInputProperties() {

            return properties.stream()//
                    .filter(this::isInputProperty)//
                    .distinct()//
                    .collect(Collectors.toList());
        }

        public List<Field> getLoaders() {
            return loaders.stream().distinct().collect(Collectors.toList());
        }

        /*
	 * (non-Javadoc)
	 * @see org.springframework.data.projection.ProjectionInformation#isDynamic()
         */
        @Override
        public boolean isClosed() {
            for (PropertyDescriptor inputProperty : getInputProperties()) {
                if (inputProperty.getName().contains(".")) {
                    return true;
                }
            }
            return this.properties.equals(getInputProperties());
        }

        /**
         * Returns whether the given {@link PropertyDescriptor} describes an
         * input property for the projection, i.e. a property that needs to be
         * present on the source to be able to create reasonable projections for
         * the type the descriptor was looked up on.
         *
         * @param descriptor will never be {@literal null}.
         * @return
         */
        protected boolean isInputProperty(PropertyDescriptor descriptor) {
            Method readMethod = descriptor.getReadMethod();

            if (readMethod == null) {
                return false;
            }

            if (AnnotationUtils.findAnnotation(readMethod, FieldProperty.class) != null) {
                return true;
            }

            if (Collection.class.isAssignableFrom(descriptor.getPropertyType())) {
                return false;
            }

            return AnnotationUtils.findAnnotation(readMethod, Value.class) == null;
        }
    }

    private static boolean hasDefaultGetter(PropertyDescriptor descriptor) {

        Method method = descriptor.getReadMethod();

        return method != null && method.isDefault();
    }

    private class CustomPropertyDescriptorSource {

        private final Class<?> type;
        private final Optional<MethodsMetadata> metadata;

        /**
         * Creates a new {@link PropertyDescriptorSource} for the given type.
         *
         * @param type must not be {@literal null}.
         */
        CustomPropertyDescriptorSource(Class<?> type) {

            Assert.notNull(type, "Type must not be null!");

            this.type = type;
            this.metadata = getMetadata(type);
        }

        /**
         * Returns {@link PropertyDescriptor}s for all properties exposed by the
         * given type and all its super interfaces.
         *
         * @return
         */
        List<PropertyDescriptor> getDescriptors() {
            return collectDescriptors().distinct().collect(StreamUtils.toUnmodifiableList());
        }

        List<Field> getLoaders() {
            return collectDescriptorsLoaders().distinct().collect(StreamUtils.toUnmodifiableList());
        }

        /**
         * Recursively collects {@link PropertyDescriptor}s for all properties
         * exposed by the given type and all its super interfaces.
         *
         * @return
         */
        private Stream<PropertyDescriptor> collectDescriptors() {

            List<PropertyDescriptor> props = new ArrayList<>();
            searchProperties(props, type, "");

            Stream<PropertyDescriptor> allButDefaultGetters = props.stream() //
                    .filter(it -> !hasDefaultGetter(it));

            Stream<PropertyDescriptor> ownDescriptors = metadata.map(it -> filterAndOrder(allButDefaultGetters, it))
                    .orElse(allButDefaultGetters);

            return ownDescriptors;
        }

        private Stream<Field> collectDescriptorsLoaders() {

            List<Field> lista = new ArrayList<>();
            searchLoads(lista, type, "");

            return lista.stream();
        }

        private void searchProperties(List<PropertyDescriptor> properties, Class<?> t, String base) {
            List<String> fields = null;
            if (!t.isInterface()) {
                fields = Arrays.stream(t.getDeclaredFields()).map(m -> m.getName()).collect(Collectors.toList());
            }
            PropertyDescriptor[] props = BeanUtils.getPropertyDescriptors(t);
            for (PropertyDescriptor prop : props) {
                if (!prop.getName().equals("class") && (fields == null || (fields.contains(prop.getName())))) {
                    if (isDto(prop.getPropertyType())) {
                        searchProperties(properties, prop.getPropertyType(), (base != "" ? base + "." + prop.getName() : prop.getName()));
                    } else {
                        try {
                            PropertyDescriptor newP = new PropertyDescriptor(prop.getName(), prop.getReadMethod(), prop.getWriteMethod());
                            if (base != "" && !prop.getName().startsWith(base)) {
                                newP.setName(base != "" ? base + "." + newP.getName() : newP.getName());
                            }
                            properties.add(newP);
                        } catch (IntrospectionException ex) {
                            Logger.getLogger(CustomPropertyDescriptorSource.class.getName()).log(Level.SEVERE, "Not find property", ex);
                        }
                    }
                }
            }
        }

        private void searchLoads(List<Field> lista, Class<?> t, String base) {
            Field[] props = t.getDeclaredFields();
            for (Field prop : props) {
                if (isDto(prop.getType()) && !prop.getName().startsWith("this$")) {
                    searchLoads(lista, prop.getType(), (base != "" ? base + "." + prop.getName() : prop.getName()));
                } else {
                    Load load = prop.getAnnotation(Load.class);
                    if (load != null) {
                        lista.add(prop);
                    }
                }
            }
        }

        private boolean isDto(Class<?> type) {

            return !Object.class.equals(type)
                    && !type.isEnum()
                    && !ClassUtils.isPrimitiveOrWrapper(type)
                    && !Number.class.isAssignableFrom(type)
                    && !Void.class.equals(type)
                    && !void.class.equals(type)
                    && !type.getPackage().getName().startsWith("java.");
        }

        /**
         * Returns a {@link Stream} of {@link PropertyDescriptor} ordered
         * following the given {@link MethodsMetadata} only returning methods
         * seen by the given {@link MethodsMetadata}.
         *
         * @param source must not be {@literal null}.
         * @param metadata must not be {@literal null}.
         * @return
         */
        private Stream<PropertyDescriptor> filterAndOrder(Stream<PropertyDescriptor> source,
                MethodsMetadata metadata) {

            Map<String, Integer> orderedMethods = getMethodOrder(metadata);

            if (orderedMethods.isEmpty()) {
                return source;
            }

            Stream<PropertyDescriptor> ret = source.filter(descriptor -> descriptor.getReadMethod() != null)
                    .filter(descriptor -> orderedMethods.containsKey(descriptor.getName() + "." + descriptor.getReadMethod().getName()))
                    .sorted(Comparator.comparingInt(left -> orderedMethods.get(left.getName() + "." + left.getReadMethod().getName())));

            return ret;
        }

        /**
         * Returns a {@link Stream} of interfaces using the given
         * {@link MethodsMetadata} as primary source for ordering.
         *
         * @param metadata must not be {@literal null}.
         * @return
         */
        private Stream<Class<?>> fromMetadata(MethodsMetadata metadata) {
            return Arrays.stream(metadata.getInterfaceNames()).map(it -> findType(it, type.getInterfaces()));
        }

        /**
         * Returns a {@link Stream} of interfaces using the given type as
         * primary source for ordering.
         *
         * @return
         */
        private Stream<Class<?>> fromType() {
            return Arrays.stream(type.getInterfaces());
        }

        /**
         * Attempts to obtain {@link MethodsMetadata} from {@link Class}.
         * Returns {@link Optional} containing {@link MethodsMetadata} if
         * metadata was read successfully, {@link Optional#empty()} otherwise.
         *
         * @param type must not be {@literal null}.
         * @return the optional {@link MethodsMetadata}.
         */
        private Optional<MethodsMetadata> getMetadata(Class<?> type) {

            try {

                MethodsMetadataReaderFactory factory = new MethodsMetadataReaderFactory(type.getClassLoader());
                MethodsMetadataReader metadataReader = factory.getMetadataReader(ClassUtils.getQualifiedName(type));

                return Optional.of(metadataReader.getMethodsMetadata());

            } catch (IOException e) {

                return Optional.empty();
            }
        }

        /**
         * Find the type with the given name in the given array of
         * {@link Class}.
         *
         * @param name must not be {@literal null} or empty.
         * @param types must not be {@literal null}.
         * @return
         */
        private Class<?> findType(String name, Class<?>[] types) {

            return Arrays.stream(types) //
                    .filter(it -> name.equals(it.getName())) //
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                    String.format("Did not find type %s in %s!", name, Arrays.toString(types))));
        }

        /**
         * Returns a {@link Map} containing method name to its positional index
         * according to {@link MethodsMetadata}.
         *
         * @param metadata
         * @return
         */
        private Map<String, Integer> getMethodOrder(MethodsMetadata metadata) {
            List<String> methods = new ArrayList<>();

            try {
                Class t = Class.forName(metadata.getClassName());
                List<PropertyDescriptor> props = new ArrayList<>();
                searchProperties(props, t, "");
                for (PropertyDescriptor prop : props) {
                    methods.add(prop.getName() + "." + prop.getReadMethod().getName());
                }
            } catch (Exception ex) {
                Logger.getLogger(CustomPropertyDescriptorSource.class.getName()).log(Level.SEVERE, null, ex);
            }

            return IntStream.range(0, methods.size()) //
                    .boxed() //
                    .collect(Collectors.toMap(methods::get, i -> i));
        }

    }
}
