package com.google.code.joliratools;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;

import com.google.inject.Injector;

final class PageCreatorImpl implements PageMaker {
    private final PageConstructor defaultConstructor;
    private final PageConstructor paramsOnlyConstructor;
    private final PageConstructor[] annotatedConstructors;
    private final Class<? extends Page> pageClass;

    <C extends Page> PageCreatorImpl(final Injector injector, final Class<C> cls) {
        pageClass = cls;
        @SuppressWarnings("unchecked")
        final Constructor<Page>[] constructors = (Constructor<Page>[]) cls
                .getDeclaredConstructors();
        final Collection<PageConstructor> _annotatedConstructors = new ArrayList<PageConstructor>();
        PageConstructor _defaultConstructor = null;
        PageConstructor _paramsOnlyConstructor = null;

        for (final Constructor<Page> _constructor : constructors) {
            final PageConstructor constructor = PageConstructor.get(injector,
                    _constructor);

            if (constructor == null) {
                continue;
            }

            if (constructor.isDefault()) {
                _defaultConstructor = constructor;
            } else if (constructor.isPageParametersOnly()) {
                _paramsOnlyConstructor = constructor;
            } else if (constructor.isInjectAnnotated()) {
                _annotatedConstructors.add(constructor);
            }
        }

        final int size = _annotatedConstructors.size();

        defaultConstructor = _defaultConstructor;
        paramsOnlyConstructor = _paramsOnlyConstructor;
        annotatedConstructors = _annotatedConstructors
                .toArray(new PageConstructor[size]);
    }

    @Override
    public Page create(final PageParameters parameters) {
        int matchedCount = 0;
        PageConstructor matchedConstructor = null;

        for (final PageConstructor constructor : annotatedConstructors) {
            final int count = constructor.getMatchCount(parameters);

            if (count > matchedCount) {
                matchedCount = count;
                matchedConstructor = constructor;
            }
        }

        if (matchedConstructor != null) {
            return matchedConstructor.newInstance(parameters);
        }

        if (paramsOnlyConstructor != null) {
            final PageParameters params = parameters == null ? new PageParameters()
                    : parameters;

            return paramsOnlyConstructor.newInstance(params);
        }

        if (defaultConstructor != null) {
            return defaultConstructor.newInstance(parameters);
        }

        final StringBuilder buf = new StringBuilder();

        buf.append("no suitable constructor for page ");
        buf.append(pageClass);

        if (parameters != null) {
            buf.append(" using { ");
            buf.append(parameters);
            buf.append(" }");
        }

        throw new WicketRuntimeException(buf.toString());
    }
}
