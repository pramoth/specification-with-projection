package org.springframework.data.repository.query;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.domain.Slice;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;


import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import org.springframework.beans.BeanUtils;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;
import th.co.geniustree.springdata.jpa.annotation.Load;
import th.co.geniustree.springdata.jpa.repository.support.CustomSpelAwareProxyProjectionFactory.CustomDefaultProjectionInformation;
import th.co.geniustree.springdata.jpa.repository.support.JpaSpecificationExecutorWithProjectionImpl;

public class MyResultProcessor {

    private final ProjectingConverter converter;
    private final ProjectionFactory factory;
    private final ReturnTypeWarpper type;

    public MyResultProcessor(ProjectionFactory factory, ReturnTypeWarpper type, EntityManager entityManager) {
        this.converter = new ProjectingConverter(type, factory, entityManager).withType(type);
        this.factory = factory;
        this.type = type;
    }

    public <T> T processResult(@Nullable Object source, Converter<Object, Object> preparingConverter) {

        if (source == null || type.isInstance(source) || !type.isProjecting()) {
            return (T) source;
        }

        Assert.notNull(preparingConverter, "Preparing converter must not be null!");

        ChainingConverter converter = ChainingConverter.of(type.getReturnedType(), preparingConverter).and(this.converter);

        if (source instanceof Slice) {
            return (T) ((Slice<?>) source).map(converter::convert);
        }

        if (source instanceof Collection) {

            Collection<?> collection = (Collection<?>) source;
            Collection<Object> target = createCollectionFor(collection);

            for (Object columns : collection) {
                target.add(type.isInstance(columns) ? columns : converter.convert(columns));
            }

            return (T) target;
        }
        return (T) converter.convert(source);
    }

    @RequiredArgsConstructor(staticName = "of")
    private static class ChainingConverter implements Converter<Object, Object> {

        private final @NonNull
        Class<?> targetType;
        private final @NonNull
        Converter<Object, Object> delegate;

        /**
         * Returns a new {@link ChainingConverter} that hands the elements
         * resulting from the current conversion to the given {@link Converter}.
         *
         * @param converter must not be {@literal null}.
         * @return
         */
        public ChainingConverter and(final Converter<Object, Object> converter) {

            Assert.notNull(converter, "Converter must not be null!");

            return new ChainingConverter(targetType, source -> {

                Object intermediate = ChainingConverter.this.convert(source);

                return intermediate == null || targetType.isInstance(intermediate) ? intermediate
                        : converter.convert(intermediate);
            });
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
         */
        @Nullable
        @Override
        public Object convert(Object source) {
            return delegate.convert(source);
        }
    }

    private static Collection<Object> createCollectionFor(Collection<?> source) {

        try {
            return CollectionFactory.createCollection(source.getClass(), source.size());
        } catch (RuntimeException o_O) {
            return CollectionFactory.createApproximateCollection(source, source.size());
        }
    }

    @RequiredArgsConstructor
    private static class ProjectingConverter implements Converter<Object, Object> {

        private final @NonNull
        ReturnTypeWarpper type;
        private final @NonNull
        ProjectionFactory factory;
        private final @NonNull
        ConversionService conversionService;
        private final @NonNull
        EntityManager entityManager;

        /**
         * Creates a new {@link ProjectingConverter} for the given
         * {@link ReturnedType} and {@link ProjectionFactory}.
         *
         * @param type must not be {@literal null}.
         * @param factory must not be {@literal null}.
         */
        ProjectingConverter(ReturnTypeWarpper type, ProjectionFactory factory, EntityManager entityManager) {
            this(type, factory, DefaultConversionService.getSharedInstance(), entityManager);
        }

        /**
         * Creates a new {@link ProjectingConverter} for the given
         * {@link ReturnedType}.
         *
         * @param type must not be {@literal null}.
         * @return
         */
        ProjectingConverter withType(ReturnTypeWarpper type) {

            Assert.notNull(type, "ReturnedType must not be null!");

            return new ProjectingConverter(type, factory, conversionService, entityManager);
        }

        /*
         * (non-Javadoc)
         * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
         */
        @Nullable
        @Override
        public Object convert(Object source) {

            Class<?> targetType = type.getReturnedType();

            if (targetType.isInterface()) {
                return factory.createProjection(targetType, getProjectionTarget(source));
            }

            PreferredConstructor<?, ?> constructor = PreferredConstructorDiscoverer.discover(targetType);

            if (constructor == null) {
                return Collections.emptyList();
            }

            Map<String, Object> src = (Map<String, Object>) source;
            Object[] properties = new Object[constructor.getConstructor().getParameterCount()];
            int idx = 0;
            Object id = null;
            Field fId = Arrays.stream(targetType.getDeclaredFields()).filter(f -> f.getAnnotation(Id.class) != null).findFirst().get();
            for (PreferredConstructor.Parameter<Object, ?> parameter : constructor.getParameters()) {
                properties[idx] = src.get(parameter.getName().replaceAll("_", "."));
                if (parameter.getName().equals(fId.getName())) {
                    id = properties[idx];
                }
                if (properties[idx] != null && properties[idx].getClass().equals(GregorianCalendar.class)) {
                    properties[idx] = ((GregorianCalendar) properties[idx]).getTime();
                }
                idx++;
            }
            try {
                Object ret = constructor.getConstructor().newInstance(properties);
                CustomDefaultProjectionInformation information = (CustomDefaultProjectionInformation) factory.getProjectionInformation(targetType);
                for (Field field : information.getLoaders()) {
                    ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
                    Class<?> projection = (Class<?>) stringListType.getActualTypeArguments()[0];
                    CriteriaBuilder builder = entityManager.getCriteriaBuilder();
                    CriteriaQuery<Tuple> query = builder.createQuery(Tuple.class);
                    Root root = query.from(type.getDomainType());
                    query.where(builder.equal(root.get(fId.getName()), id));
                    String[] vet = field.getAnnotation(Load.class).value().split("\\.");
                    From join = root;
                    for (int i = 0; i < vet.length; i++) {
                        join = join.join(vet[i], JoinType.INNER);
                    }

                    ReturnTypeWarpper returnedType = ReturnTypeWarpper.of(projection, type.getDomainType(), factory);
                    List props = factory.getProjectionInformation(projection).getInputProperties();

                    JpaSpecificationExecutorWithProjectionImpl.configQuery(builder, query, join, returnedType, props, returnedType.getReturnedType());

                    TypedQuery<Tuple> queryWithMetadata = this.entityManager.createQuery(query);
                    final MyResultProcessor resultProcessor = new MyResultProcessor(factory, returnedType, entityManager);
                    List l = queryWithMetadata.getResultList();
                    final List resultList = resultProcessor.processResult(l, new TupleConverter(returnedType, props));

                    BeanUtils.getPropertyDescriptor(targetType, field.getName()).getWriteMethod().invoke(ret, resultList);
                }
                return ret;
            } catch (Exception ex) {
                Logger.getLogger(MyResultProcessor.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }

        private Object getProjectionTarget(Object source) {

            if (source != null && source.getClass().isArray()) {
                source = Arrays.asList((Object[]) source);
            }

            if (source instanceof Collection) {
                return toMap((Collection<?>) source, type.getInputProperties());
            }

            return source;
        }

        private static Map<String, Object> toMap(Collection<?> values, List<String> names) {

            int i = 0;
            Map<String, Object> result = new HashMap<>(values.size());

            for (Object element : values) {
                result.put(names.get(i++), element);
            }

            return result;
        }
    }
}
