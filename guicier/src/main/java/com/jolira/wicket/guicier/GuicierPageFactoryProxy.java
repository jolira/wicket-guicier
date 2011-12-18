/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.jolira.wicket.guicier;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.inject.Inject;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Injector;

/**
 * @author jfk
 * @date Sep 8, 2010 2:05:47 PM
 * @since 1.0
 */
abstract class GuicierPageFactoryProxy implements IPageFactory {
    private final ConcurrentMap<String, Boolean> pageToBookmarkableCache = new ConcurrentHashMap<String, Boolean>();

    private <C> boolean doIsBookmarkable(final Class<C> pageClass) {
        final Constructor<?>[] constructors = pageClass.getConstructors();

        for (final Constructor<?> constructor : constructors) {
            final Inject i1 = constructor.getAnnotation(Inject.class);

            if (i1 != null) {
                return true;
            }

            final com.google.inject.Inject i2 = constructor.getAnnotation(com.google.inject.Inject.class);

            if (i2 != null) {
                return true;
            }

            final Class<?>[] params = constructor.getParameterTypes();

            switch (params.length) {
            case 0:
                return true;
            case 1:
                if (PageParameters.class.isAssignableFrom(params[0])) {
                    return true;
                }
            }
        }

        return false;
    }

    abstract Injector getInjector();

    /**
     * @see IPageFactory#isBookmarkable(Class)
     */
    @Override
    public <C extends IRequestablePage> boolean isBookmarkable(final Class<C> pageClass) {
        final String className = pageClass.getName();
        final Boolean bookmarkable = pageToBookmarkableCache.get(className);

        if (bookmarkable != null) {
            return bookmarkable.booleanValue();
        }

        final boolean _bookmarkable = doIsBookmarkable(pageClass);
        final Boolean bookmarkable_ = Boolean.valueOf(_bookmarkable);
        pageToBookmarkableCache.put(className, bookmarkable_);

        return _bookmarkable;
    }

    /**
     * @see IPageFactory#newPage(Class)
     */
    @Override
    public <C extends IRequestablePage> IRequestablePage newPage(final Class<C> pageClass) {
        return newPage(pageClass, null);
    }

    /**
     * @see IPageFactory#newPage(Class, PageParameters)
     */
    @Override
    public <C extends IRequestablePage> IRequestablePage newPage(final Class<C> pageClass,
            final PageParameters parameters) {
        final Injector i = getInjector();
        final IPageFactory factory = i.getInstance(GuicierPageFactory.class);

        return factory.newPage(pageClass, parameters);
    }
}
