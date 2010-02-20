package com.google.code.joliratools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;

final class PageConstructor {
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

        final Guicier gpp = injector
                .getInstance(Guicier.class);

        return new PageConstructor(gpp, paramCount == 0,
                injectAnnotationPresent, params, providers, _isParametersOnly,
                constructor, injector, parameterTypes);
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

    private final Guicier gpp;

    private PageConstructor(final Guicier gpp,
            final boolean isDefault, final boolean isInjected,
            final Parameter[] params, final Provider<?>[] providers,
            final boolean isParametersOnly,
            final Constructor<Page> constructor, final Injector injector,
            final Class<?>[] parameterTypes) {
        this.gpp = gpp;
        this.params = params;
        this.providers = providers;
        this.isParametersOnly = isParametersOnly;
        this.isInjected = isInjected;
        this.isDefault = isDefault;
        this.constructor = constructor;
        this.injector = injector;
        this.parameterTypes = parameterTypes;
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

                args[idx] = gpp
                        .get(parameters, param, parameterTypes[idx]);
            }
        }

        final Page page = newInstance(args);

        injector.injectMembers(page);

        return page;
    }
}
