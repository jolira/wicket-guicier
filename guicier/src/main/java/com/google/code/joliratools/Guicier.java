/**
 * 
 */
package com.google.code.joliratools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/**
 * @author jfk
 * 
 */
@Singleton
public class Guicier {
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

    private static boolean isAssignableFrom(final Class<?> left,
            final Class<? extends Object> right) {
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
    public Guicier(final Injector injector) {
        this.injector = injector;
    }

    private int findMatchingParameter(final int idx, final Object arg,
            final Class<? extends Object> argClass, final Class<?>[] types,
            final Annotation[][] annos, final PageParameters params,
            final Locale locale) {
        if (idx >= types.length) {
            return -1;
        }

        final Parameter param = getParameter(annos[idx]);

        if (param == null) {
            return findMatchingParameter(idx + 1, arg, argClass, types, annos,
                    params, locale);
        }

        if (isAssignableFrom(types[idx], argClass)) {
            put(params, param, arg, types[idx], locale);

            return idx + 1;
        }

        if (!param.optional()) {
            return -1;
        }

        return findMatchingParameter(idx + 1, arg, argClass, types, annos,
                params, locale);
    }

    public PageParameters get(final Class<?> pageClass, final Object... args) {
        final Constructor<?>[] constructors = pageClass.getConstructors();
        final Locale locale = Locale.getDefault();

        for (final Constructor<?> constructor : constructors) {
            final PageParameters result = get(constructor, args, locale);

            if (result != null) {
                return result;
            }
        }

        throw new WicketRuntimeException("no constructor found for "
                + pageClass + " and parameters " + Arrays.toString(args));
    }

    private PageParameters get(final Constructor<?> constructor,
            final Object[] args, final Locale locale) {
        final Class<?>[] types = constructor.getParameterTypes();
        final PageParameters params = new PageParameters();
        final Annotation[][] annos = constructor.getParameterAnnotations();
        int paramIdx = 0;

        for (final Object arg : args) {
            final Class<? extends Object> argClass = arg.getClass();

            paramIdx = findMatchingParameter(paramIdx, arg, argClass, types,
                    annos, params, locale);

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

    public <T> T get(final PageParameters parameters, final Parameter param,
            final Class<T> type) {
        if (PageParameters.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            final T params = (T) parameters;

            return params;
        }

        final String key = param.value();
        final Object value = parameters.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof String) {
            return getValue(param, type, value);
        }

        final String[] array = (String[]) value;

        if (!type.isArray()) {
            if (array.length < 1) {
                return null;
            }

            return getValue(param, type, array[0]);
        }

        final Class<?> componentType = type.getComponentType();
        final Object[] _array = (Object[]) Array.newInstance(componentType,
                array.length);

        for (int idx = 0; idx < array.length; idx++) {
            _array[idx] = getValue(param, componentType, array[idx]);
        }

        @SuppressWarnings("unchecked")
        final T array_ = (T) _array;

        return array_;
    }

    private IConverter getConverter(final Parameter param, final Class<?> type) {
        final Class<? extends IConverter> converterClass = getConverterClass(
                param, type);

        if (converterClass == null) {
            throw new WicketRuntimeException("please specify a converter "
                    + param + " of type " + type);
        }

        return injector.getInstance(converterClass);
    }

    private Class<? extends IConverter> getConverterClass(
            final Parameter param, final Class<?> type) {
        final Class<? extends IConverter> converterClass = param.converter();

        if (IConverter.class.equals(converterClass)) {
            return getDefaultConverterClass(type);
        }

        return converterClass;
    }

    private Class<? extends IConverter> getDefaultConverterClass(
            final Class<?> type) {
        if (String.class.equals(type)) {
            return StringConverter.class;
        }

        if (int.class.equals(type) || Integer.class.equals(type)) {
            return IntegerConverter.class;
        }

        if (long.class.equals(type) || Long.class.equals(type)) {
            return LongConverter.class;
        }

        if (short.class.equals(type) || Short.class.equals(type)) {
            return ShortConverter.class;
        }

        if (float.class.equals(type) || Float.class.equals(type)) {
            return FloatConverter.class;
        }

        if (double.class.equals(type) || Double.class.equals(type)) {
            return DoubleConverter.class;
        }

        if (boolean.class.equals(type) || Boolean.class.equals(type)) {
            return BooleanConverter.class;
        }

        if (byte.class.equals(type) || Byte.class.equals(type)) {
            return ByteConverter.class;
        }

        if (char.class.equals(type) || Character.class.equals(type)) {
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

    private Parameter getParameter(final Annotation[] annotations) {
        for (final Annotation anno : annotations) {
            if (anno instanceof Parameter) {
                return (Parameter) anno;
            }
        }
        return null;
    }

    private <T> T getValue(final Parameter param, final Class<T> type,
            final Object value) {
        final IConverter converter = getConverter(param, type);
        @SuppressWarnings("unchecked")
        final T converted = (T) converter.convertToObject((String) value, null);

        return converted;
    }

    private void put(final PageParameters params, final Parameter param,
            final Object arg, final Class<?> type, final Locale locale) {
        final String name = param.value();
        final IConverter converter = getConverter(param, type);
        final String converted = converter.convertToString(arg, locale);

        params.put(name, converted);
    }
}
