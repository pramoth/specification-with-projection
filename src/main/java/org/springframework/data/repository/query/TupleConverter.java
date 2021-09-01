package org.springframework.data.repository.query;

import java.beans.PropertyDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import th.co.geniustree.springdata.jpa.annotation.FieldProperty;

public class TupleConverter implements Converter<Object, Object> {

    private final ReturnTypeWarpper type;
    private final List<PropertyDescriptor> props;

    /**
     * Creates a new {@link TupleConverter} for the given {@link ReturnedType}.
     *
     * @param type must not be {@literal null}.
     */
    public TupleConverter(ReturnTypeWarpper type, List<PropertyDescriptor> props) {

        Assert.notNull(type, "Returned type must not be null!");

        this.type = type;
        this.props = props;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object source) {

        if (!(source instanceof Tuple)) {
            return source;
        }

        Tuple tuple = (Tuple) source;
        List<TupleElement<?>> elements = tuple.getElements();

        if (elements.size() == 1) {

            Object value = tuple.get(elements.get(0));

            if (type.isInstance(value) || value == null) {
                return value;
            }
        }

        return new TupleConverter.TupleBackedMap(tuple, type, props);
    }

    /**
     * A {@link Map} implementation which delegates all calls to a
     * {@link Tuple}. Depending on the provided {@link Tuple} implementation it
     * might return the same value for various keys of which only one will
     * appear in the key/entry set.
     *
     * @author Jens Schauder
     */
    private static class TupleBackedMap implements Map<String, Object> {

        private static final String UNMODIFIABLE_MESSAGE = "A TupleBackedMap cannot be modified.";

        private final Tuple tuple;
        private final ReturnTypeWarpper type;
        private final List<PropertyDescriptor> props;
        private final Map<String, PropertyDescriptor> mapped = new HashMap<>();
        private final boolean isInterface;

        TupleBackedMap(Tuple tuple, ReturnTypeWarpper type, List<PropertyDescriptor> props) {
            this.tuple = tuple;
            this.type = type;
            this.props = props;
            isInterface = type.getReturnedType().isInterface();
            props.stream().forEach(p -> {
                FieldProperty fp = p.getReadMethod().getAnnotation(FieldProperty.class);
                if (fp != null && p.getReadMethod().getAnnotation(Value.class) != null) {
                    mapped.put(fp.path(), p);
                } else {
                    mapped.put(p.getName(), p);
                }
            });
//            mapped = props.stream().collect(Collectors.toMap(PropertyDescriptor::getName, Function.identity()));
        }

        @Override
        public int size() {
            return tuple.getElements().size();
        }

        @Override
        public boolean isEmpty() {
            return tuple.getElements().isEmpty();
        }

        /**
         * If the key is not a {@code String} or not a key of the backing
         * {@link Tuple} this returns {@code false}. Otherwise this returns
         * {@code true} even when the value from the backing {@code Tuple} is
         * {@code null}.
         *
         * @param key the key for which to get the value from the map.
         * @return wether the key is an element of the backing tuple.
         */
        @Override
        public boolean containsKey(Object key) {
            return mapped.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return Arrays.asList(tuple.toArray()).contains(value);
        }

        /**
         * If the key is not a {@code String} or not a key of the backing
         * {@link Tuple} this returns {@code null}. Otherwise the value from the
         * backing {@code Tuple} is returned, which also might be {@code null}.
         *
         * @param key the key for which to get the value from the map.
         * @return the value of the backing {@link Tuple} for that key or
         * {@code null}.
         */
        @Override
        @Nullable
        public Object get(Object key) {
            if (!(key instanceof String)) {
                return null;
            }

            try {
                if (!containsKey(key)) {
                    if (type.getInputProperties().stream().anyMatch(p -> p.startsWith(key + ".") || p.endsWith("." + key) || p.contains("." + key + "."))) {
                        return this;
                    }
                }

                PropertyDescriptor prop = mapped.get(key);
                String sKey = (String) key;
                if (isInterface) {
                    FieldProperty fp = AnnotationUtils.findAnnotation(prop.getReadMethod(), FieldProperty.class);
                    if (fp != null) {
                        sKey = fp.path();
                    }
                }

                Object obj = tuple.get(sKey);
                if (obj != null) {
                    if (obj instanceof GregorianCalendar) {
                        if (prop == null) {
                            prop = props.stream().filter(f -> f.getName().equals(key) || (AnnotationUtils.findAnnotation(f.getReadMethod(), FieldProperty.class) != null ? (AnnotationUtils.findAnnotation(f.getReadMethod(), FieldProperty.class).path().equals(key)) : false)).findFirst().get();
                        }
                        if (prop != null && prop.getReadMethod() != null) {
                            if (prop.getReadMethod().getReturnType().equals(Date.class)) {
                                obj = ((GregorianCalendar) obj).getTime();
                            }
                        }

                    }
                }

                return obj;
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public Object put(String key, Object value) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public Object remove(Object key) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public void putAll(Map<? extends String, ?> m) {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(UNMODIFIABLE_MESSAGE);
        }

        @Override
        public Set<String> keySet() {
            return tuple.getElements().stream() //
                    .map(TupleElement::getAlias) //
                    .collect(Collectors.toSet());
        }

        @Override
        public Collection<Object> values() {
            return Arrays.asList(tuple.toArray());
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return tuple.getElements().stream() //
                    .map(e -> new HashMap.SimpleEntry<String, Object>(e.getAlias(), tuple.get(e))) //
                    .collect(Collectors.toSet());
        }
    }
}
