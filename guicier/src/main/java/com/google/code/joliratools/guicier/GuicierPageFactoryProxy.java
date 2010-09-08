/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;

import com.google.code.joliratools.GuicierPageFactory;
import com.google.inject.Injector;

/**
 * @author jfk
 * @date Sep 8, 2010 2:05:47 PM
 * @since 1.0
 */
abstract class GuicierPageFactoryProxy implements IPageFactory {
    private IPageFactory getFactory() {
        final Injector i = getInjector();

        return i.getInstance(GuicierPageFactory.class);
    }

    abstract Injector getInjector();

    /**
     * @see IPageFactory#newPage(Class)
     */
    @Override
    public <C extends Page> Page newPage(final Class<C> pageClass) {
        final IPageFactory factory = getFactory();

        return factory.newPage(pageClass);
    }

    /**
     * @see IPageFactory#newPage(Class, PageParameters)
     */
    @Override
    public <C extends Page> Page newPage(final Class<C> pageClass, final PageParameters parameters) {
        final IPageFactory factory = getFactory();

        return factory.newPage(pageClass, parameters);
    }
}
