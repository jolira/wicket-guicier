package com.google.code.joliratools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Locale;

import org.apache.wicket.Page;
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
import com.google.inject.Key;
import com.google.inject.Provider;

final class PageConstructor {
    private static class StringConverter implements IConverter {
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

    static PageConstructor get(final Injector injector,
            final Constructor<Page> constructor) {
        final Annotation[][] paramAnnotations = constructor
                .getParameterAnnotations();
        int paramCount = 0;
        boolean _isParametersOnly = false;

        final Class<?>[] parameterTypes = constructor.getParameterTypes();
        final Parameter[] params = new Parameter[parameterTypes.length];
        final Provider<?>[] providers = new Provider<?>[parameterTypes.length];
        final boolean injectAnnotationPresent = constructor
                .isAnnotationPresent(Inject.class);

        for (int idx = 0; idx < parameterTypes.length; idx++) {
            final Annotation[] annos = paramAnnotations[idx];
            final Class<?> type = parameterTypes[idx];
            final Parameter parameter = getParameterAnnotation(annos);
            final boolean isPageParameters = PageParameters.class
                    .isAssignableFrom(type);

            if (parameter != null || isPageParameters) {
                paramCount++;
                params[idx] = parameter;
                _isParametersOnly = paramCount == 1 && isPageParameters;
            } else {
                if (!injectAnnotationPresent) {
                    return null;
                }

                final Annotation anno = getNonParamAnnotation(annos);
                final Key<?> key = anno != null ? Key.get(type, anno) : Key
                        .get(type);

                providers[idx] = injector.getProvider(key);
            }
        }

        return new PageConstructor(paramCount == 0, injectAnnotationPresent,
                params, providers, _isParametersOnly, constructor, injector,
                parameterTypes);
    }

    private static Annotation getNonParamAnnotation(final Annotation[] annos) {
        for (final Annotation anno : annos) {
            if (!(anno instanceof Parameter)) {
                return anno;
            }
        }

        return null;
    }

    private static Parameter getParameterAnnotation(final Annotation[] annos) {
        for (final Annotation anno : annos) {
            if (anno instanceof Parameter) {
                return (Parameter) anno;
            }
        }

        return null;
    }

    private final boolean isDefault;

    private final boolean isInjected;

    private final Parameter[] params;

    private final Provider<?>[] providers;

    private final boolean isParametersOnly;

    private final Constructor<Page> constructor;

    private final Injector injector;

    private final Class<?>[] parameterTypes;

    private PageConstructor(final boolean isDefault, final boolean isInjected,
            final Parameter[] params, final Provider<?>[] providers,
            final boolean isParametersOnly,
            final Constructor<Page> constructor, final Injector injector,
            final Class<?>[] parameterTypes) {
        this.params = params;
        this.providers = providers;
        this.isParametersOnly = isParametersOnly;
        this.isInjected = isInjected;
        this.isDefault = isDefault;
        this.constructor = constructor;
        this.injector = injector;
        this.parameterTypes = parameterTypes;
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

    int getMatchCount(final PageParameters parameters) {
        int count = 0;

        for (final Parameter param : params) {
            if (param == null) {
                continue;
            }

            final String key = param.value();

            if (!parameters.containsKey(key)) {
                if (!param.optional()) {
                    return -1;
                }

                continue;
            }

            count++;
        }

        return count;
    }

    private Object getValue(final PageParameters parameters,
            final Parameter param, final Class<?> type) {
        if (PageParameters.class.isAssignableFrom(type)) {
            return parameters;
        }

        final String key = param.value();
        final String value = (String) parameters.get(key);
        final IConverter converter = getConverter(param, type);

        return converter.convertToObject(value, null);
    }

    boolean isDefault() {
        return isDefault;
    }

    boolean isInjectAnnotated() {
        return isInjected;
    }

    public boolean isPageParametersOnly() {
        return isParametersOnly;
    }

    private Page newInstance(final Object[] args) throws Error {
        constructor.setAccessible(true);

        try {
            return constructor.newInstance(args);
        } catch (final InstantiationException e) {
            throw new WicketRuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new WicketRuntimeException(e);
        } catch (final InvocationTargetException e) {
            throw new WicketRuntimeException(e);
        }
    }

    public Page newInstance(final PageParameters parameters) {
        final Object[] args = new Object[params.length];

        for (int idx = 0; idx < params.length; idx++) {
            final Provider<?> provider = providers[idx];

            if (provider != null) {
                args[idx] = provider.get();
            } else {
                final Parameter param = params[idx];

                args[idx] = getValue(parameters, param, parameterTypes[idx]);
            }
        }

        final Page page = newInstance(args);

        injector.injectMembers(page);

        return page;
    }
}
