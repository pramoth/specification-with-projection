package org.springframework.data.repository.query;

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

import java.util.*;

public class MyResultProcessor {
    private final ProjectingConverter converter;
    private final ProjectionFactory factory;
    private final ReturnedType type;

    public MyResultProcessor(ProjectionFactory factory, ReturnedType type) {
        this.converter = new ProjectingConverter(type,factory).withType(type);
        this.factory = factory;
        this.type = type;
    }

    public <T> T processResult(@Nullable Object source, Converter<Object, Object> preparingConverter) {

        if (source == null || type.isInstance(source) || !type.isProjecting()) {
            return (T) source;
        }

        Assert.notNull(preparingConverter, "Preparing converter must not be null!");

        ChainingConverter converter = ChainingConverter.of(type.getReturnedType(), preparingConverter).and(this.converter);

        if (source instanceof Slice ) {
            return (T) ((Slice<?>) source).map(converter::convert);
        }

        if (source instanceof Collection ) {

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
        private final @NonNull Converter<Object, Object> delegate;

        /**
         * Returns a new {@link ChainingConverter} that hands the elements resulting from the current conversion to the
         * given {@link Converter}.
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

        private final @NonNull ReturnedType type;
        private final @NonNull ProjectionFactory factory;
        private final @NonNull ConversionService conversionService;

        /**
         * Creates a new {@link ProjectingConverter} for the given {@link ReturnedType} and {@link ProjectionFactory}.
         *
         * @param type must not be {@literal null}.
         * @param factory must not be {@literal null}.
         */
        ProjectingConverter(ReturnedType type, ProjectionFactory factory) {
            this(type, factory, DefaultConversionService.getSharedInstance());
        }

        /**
         * Creates a new {@link ProjectingConverter} for the given {@link ReturnedType}.
         *
         * @param type must not be {@literal null}.
         * @return
         */
        ProjectingConverter withType(ReturnedType type) {

            Assert.notNull(type, "ReturnedType must not be null!");

            return new ProjectingConverter(type, factory, conversionService);
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

            return conversionService.convert(source, targetType);
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
