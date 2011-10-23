/**
 *
 */
package com.google.code.joliratools;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.BigDecimalConverter;
import org.apache.wicket.util.convert.converters.BooleanConverter;
import org.apache.wicket.util.convert.converters.ByteConverter;
import org.apache.wicket.util.convert.converters.CharacterConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.apache.wicket.util.convert.converters.DoubleConverter;
import org.apache.wicket.util.convert.converters.FloatConverter;
import org.apache.wicket.util.convert.converters.IntegerConverter;
import org.apache.wicket.util.convert.converters.LongConverter;
import org.apache.wicket.util.convert.converters.ShortConverter;
import org.apache.wicket.util.convert.converters.SqlDateConverter;
import org.apache.wicket.util.convert.converters.SqlTimeConverter;
import org.apache.wicket.util.convert.converters.SqlTimestampConverter;

import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * @author jfk
 */
@Singleton
public class Guicier {
    private class EnumConverter<T extends Enum<T>> implements IConverter {
        private static final long serialVersionUID = -5797300013416456562L;
        private final Class<T> type;

        EnumConverter(final Class<T> type) {
            this.type = type;
        }

        @Override
        public Object convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return null;
            }

            if (!isNumeric(value)) {
                return Enum.valueOf(type, value);
            }

            final int ordinal = Integer.parseInt(value);

            final Enum<?>[] constants = type.getEnumConstants();

            for (final Enum<?> constant : constants) {
                final int _ordinal = constant.ordinal();

                if (_ordinal == ordinal) {
                    return constant;
                }
            }

            throw new IllegalArgumentException("No enum const " + type + "." + value);
        }

        @Override
        public String convertToString(final Object value, final Locale locale) {
            final Enum<?> _enum = (Enum<?>) value;

            return _enum.name();
        }

        private boolean isNumeric(final String value) {
            final int length = value.length();

            for (int idx = 0; idx < length; idx++) {
                final char c = value.charAt(idx);

                if (c < '0' || c > '9') {
                    return false;
                }
            }

            return true;
        }

    }

    /**
     * Convert booleans.
     */
    public static class PrimitiveBooleanConverter extends BooleanConverter {
        private static final long serialVersionUID = -3890658188435687997L;

        @Override
        public Object convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Boolean.FALSE;
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert bytes.
     */
    public static class PrimitiveByteConverter extends ByteConverter {
        private static final long serialVersionUID = -2108916636178629276L;

        @Override
        public Object convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Byte.valueOf((byte) 0);
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert chars.
     */
    public static class PrimitiveCharConverter extends CharacterConverter {
        private static final long serialVersionUID = 2848347198715860269L;

        @Override
        public Object convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Character.valueOf('\0');
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert doubles.
     */
    public static class PrimitiveDoubleConverter extends DoubleConverter {
        private static final long serialVersionUID = -1963485530208715854L;

        @Override
        public Double convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Double.valueOf(0);
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert floats.
     */
    public static class PrimitiveFloatConverter extends FloatConverter {
        private static final long serialVersionUID = -4336506003120398278L;

        @Override
        public Float convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Float.valueOf(0);
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert ints.
     */
    public static class PrimitiveIntConverter extends IntegerConverter {
        private static final long serialVersionUID = 2912209049849424923L;

        @Override
        public Integer convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Integer.valueOf(0);
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert longs.
     */
    public static class PrimitiveLongConverter extends LongConverter {
        private static final long serialVersionUID = 8726058689616316514L;

        @Override
        public Long convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Long.valueOf(0);
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert shorts.
     */
    public static class PrimitiveShortConverter extends ShortConverter {
        private static final long serialVersionUID = -3491234864470269404L;

        @Override
        public Short convertToObject(final String value, final Locale locale) {
            if (value.isEmpty()) {
                return Short.valueOf((short) 0);
            }

            return super.convertToObject(value, locale);
        }
    }

    /**
     * Convert strings.
     */
    public static class StringConverter implements IConverter {
        private static final long serialVersionUID = 5221463436925128138L;

        @Override
        public Object convertToObject(final String value, final Locale locale) {
            return value;
        }

        @Override
        public String convertToString(final Object value, final Locale locale) {
            return value.toString();
        }
    }

    private static boolean isAssignableFrom(final Class<?> left, final Class<? extends Object> right) {
        if (left.isAssignableFrom(right)) {
            return true;
        }

        if (int.class.equals(left) && Integer.class.equals(right)) {
            return true;
        }

        if (long.class.equals(left) && Long.class.equals(right)) {
            return true;
        }

        if (double.class.equals(left) && Double.class.equals(right)) {
            return true;
        }

        if (float.class.equals(left) && Float.class.equals(right)) {
            return true;
        }

        if (byte.class.equals(left) && Byte.class.equals(right)) {
            return true;
        }

        return char.class.equals(left) && Character.class.equals(right);
    }

    private final Injector injector;

    @Inject
    Guicier(final Injector injector) {
        this.injector = injector;
    }

    private int findMatchingParameter(final int idx, final Object arg, final Class<? extends Object> argClass,
            final Class<?>[] types, final Annotation[][] annos, final PageParameters params, final Locale locale) {
        if (idx >= types.length) {
            return -1;
        }

        final Parameter param = getParameter(annos[idx]);

        if (param == null) {
            return findMatchingParameter(idx + 1, arg, argClass, types, annos, params, locale);
        }

        if (isAssignableFrom(types[idx], argClass)) {
            put(params, param, arg, types[idx], locale);

            return idx + 1;
        }

        if (!param.optional()) {
            return -1;
        }

        return findMatchingParameter(idx + 1, arg, argClass, types, annos, params, locale);
    }

    /**
     * Create parameters
     * 
     * @param pageClass
     *            the page class for which the parameters should be created
     * @param args
     *            the argument to be added to the {@link PageParameters}
     * @return the parameters
     */
    public PageParameters get(final Class<?> pageClass, final Object... args) {
        final Constructor<?>[] constructors = pageClass.getDeclaredConstructors();
        final Locale locale = Locale.getDefault();

        for (final Constructor<?> constructor : constructors) {
            if (!isAnnotationPresent(constructor)) {
                continue;
            }

            final PageParameters result = get(constructor, args, locale);

            if (result != null) {
                return result;
            }
        }

        throw new WicketRuntimeException("no constructor found for " + pageClass + " and parameters "
                + Arrays.toString(args));
    }

    private PageParameters get(final Constructor<?> constructor, final Object[] args, final Locale locale) {
        final Class<?>[] types = constructor.getParameterTypes();
        final PageParameters params = new PageParameters();
        final Annotation[][] annos = constructor.getParameterAnnotations();
        int paramIdx = 0;

        for (final Object arg : args) {
            final Class<? extends Object> argClass = arg.getClass();

            paramIdx = findMatchingParameter(paramIdx, arg, argClass, types, annos, params, locale);

            if (paramIdx < 0) {
                return null;
            }
        }

        for (; paramIdx < annos.length; paramIdx++) {
            final Parameter param = getParameter(annos[paramIdx]);

            if (param == null) {
                continue;
            }

            if (!param.optional()) {
                return null;
            }
        }

        return params;
    }

    <T> T get(final PageParameters parameters, final Parameter param, final Class<T> type,
            final PageParameters cleansed, final boolean isFirst) {
        if (PageParameters.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            final T params = isFirst ? (T) parameters : (T) cleansed;

            return params;
        }

        final String key = param.value();
        final Object value = parameters != null ? parameters.get(key) : null;

        if (value == null) {
            return getNullValue(type);
        }

        if (value instanceof String) {
            verifyString(param, String.class.cast(value));
        }

        cleansed.put(key, value);

        final Class<? extends Object> valClass = value.getClass();

        if (!valClass.isArray()) {
            return getValue(param, type, value, valClass);
        }

        final Object[] array = (Object[]) value;
        final Class<?> valComponentType = valClass.getComponentType();
        final Class<?> componentType = type.getComponentType();

        if (!type.isArray()) {
            if (array.length < 1) {
                @SuppressWarnings("unchecked")
                final Class<T> casted = (Class<T>) componentType;

                return getNullValue(casted);
            }
            return getValue(param, type, array[0], valComponentType);
        }

        final Object[] _array = (Object[]) Array.newInstance(componentType, array.length);

        for (int idx = 0; idx < array.length; idx++) {
            _array[idx] = getValue(param, componentType, array[idx], valComponentType);
        }

        @SuppressWarnings("unchecked")
        final T array_ = (T) _array;

        return array_;
    }

    private void verifyString(final Parameter param, final String value) {
        final String verifier = param.verifier();

        if (verifier == null || verifier.isEmpty()) {
            return;
        }

        if (!value.matches(verifier)) {
            throw new IllegalArgumentException("'" + value + "' does not match verifier '" + verifier + "'.");
        }
    }

    private IConverter getConverter(final Parameter param, final Class<?> type) {
        final Class<? extends IConverter> converterClass = getConverterClass(param, type);

        if (converterClass != null) {
            return injector.getInstance(converterClass);
        }

        if (!type.isEnum()) {
            throw new WicketRuntimeException("please specify a converter " + param + " of type " + type);
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        final EnumConverter<?> converter = new EnumConverter(type);

        return converter;
    }

    private Class<? extends IConverter> getConverterClass(final Parameter param, final Class<?> type) {
        final Class<? extends IConverter> converterClass = param.converter();

        if (IConverter.class.equals(converterClass)) {
            return getDefaultConverterClass(type);
        }

        return converterClass;
    }

    private Class<? extends IConverter> getDefaultConverterClass(final Class<?> type) {
        if (String.class.equals(type)) {
            return StringConverter.class;
        }

        if (int.class.equals(type)) {
            return PrimitiveIntConverter.class;
        }

        if (long.class.equals(type)) {
            return PrimitiveLongConverter.class;
        }

        if (short.class.equals(type)) {
            return PrimitiveShortConverter.class;
        }

        if (float.class.equals(type)) {
            return PrimitiveFloatConverter.class;
        }

        if (double.class.equals(type)) {
            return PrimitiveDoubleConverter.class;
        }

        if (boolean.class.equals(type)) {
            return PrimitiveBooleanConverter.class;
        }

        if (byte.class.equals(type)) {
            return PrimitiveByteConverter.class;
        }

        if (char.class.equals(type)) {
            return PrimitiveCharConverter.class;
        }

        if (Character.class.equals(type)) {
            return CharacterConverter.class;
        }

        if (Integer.class.equals(type)) {
            return IntegerConverter.class;
        }

        if (Long.class.equals(type)) {
            return LongConverter.class;
        }

        if (Short.class.equals(type)) {
            return ShortConverter.class;
        }

        if (Float.class.equals(type)) {
            return FloatConverter.class;
        }

        if (Double.class.equals(type)) {
            return DoubleConverter.class;
        }

        if (Boolean.class.equals(type)) {
            return BooleanConverter.class;
        }

        if (Byte.class.equals(type)) {
            return ByteConverter.class;
        }

        if (Character.class.equals(type)) {
            return CharacterConverter.class;
        }

        if (Date.class.equals(type)) {
            return DateConverter.class;
        }

        if (java.sql.Date.class.equals(type)) {
            return SqlDateConverter.class;
        }

        if (java.sql.Time.class.equals(type)) {
            return SqlTimeConverter.class;
        }

        if (java.sql.Timestamp.class.equals(type)) {
            return SqlTimestampConverter.class;
        }

        if (BigDecimal.class.equals(type)) {
            return BigDecimalConverter.class;
        }

        return null;
    }

    /**
     * Returns the value that represents null.
     * 
     * @param <T>
     *            the type of parameter
     * @param type
     *            the value type
     * @return the value representing null for the given type
     */
    @SuppressWarnings("unchecked")
    public <T> T getNullValue(final Class<T> type) {
        if (int.class.equals(type)) {
            return (T) Integer.valueOf(0);
        }

        if (double.class.equals(type)) {
            return (T) Double.valueOf(0);
        }

        if (float.class.equals(type)) {
            return (T) Float.valueOf(0);
        }

        if (short.class.equals(type)) {
            return (T) Short.valueOf((short) 0);
        }

        if (long.class.equals(type)) {
            return (T) Long.valueOf(0);
        }

        if (byte.class.equals(type)) {
            return (T) Byte.valueOf((byte) 0);
        }

        if (char.class.equals(type)) {
            return (T) Character.valueOf('\0');
        }

        if (boolean.class.equals(type)) {
            return (T) Boolean.FALSE;
        }

        return null;
    }

    private Parameter getParameter(final Annotation[] annotations) {
        for (final Annotation anno : annotations) {
            if (anno instanceof Parameter) {
                return (Parameter) anno;
            }
        }
        return null;
    }

    private <T> T getValue(final Parameter param, final Class<T> type, final Object value, final Class<?> valClass) {
        if (type.isAssignableFrom(valClass)) {
            @SuppressWarnings("unchecked")
            final T casted = (T) value;

            return casted;
        }

        final IConverter converter = getConverter(param, type);
        final String strValue = value.toString();
        @SuppressWarnings("unchecked")
        final T converted = (T) converter.convertToObject(strValue, null);

        return converted;
    }

    private boolean isAnnotationPresent(final AnnotatedElement e) {
        return e.isAnnotationPresent(Inject.class) || e.isAnnotationPresent(com.google.inject.Inject.class);
    }

    private void put(final PageParameters params, final Parameter param, final Object arg, final Class<?> type,
            final Locale locale) {
        final String name = param.value();
        final IConverter converter = getConverter(param, type);
        final String converted = converter.convertToString(arg, locale);

        params.put(name, converted);
    }
}
