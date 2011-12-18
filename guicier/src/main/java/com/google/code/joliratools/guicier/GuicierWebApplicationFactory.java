/**
 * Copyright (c) 2011 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import javax.inject.Inject;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;

import com.google.inject.Injector;

/**
 * @author jfk
 * @date Aug 2, 2011 7:57:34 AM
 * @since 1.0
 * 
 */
public class GuicierWebApplicationFactory implements IWebApplicationFactory {
    private final Class<? extends WebApplication> appCls;
    private final Injector injector;

    /**
     * Create the the application factory.
     * 
     * @param injector
     *            the injector to be used to create the class
     * @param appCls
     *            the application class to create
     */
    @Inject
    protected GuicierWebApplicationFactory(final Injector injector, final Class<? extends WebApplication> appCls) {
        this.injector = injector;
        this.appCls = appCls;

    }

    @Override
    public WebApplication createApplication(final WicketFilter filter) {
        return injector.getInstance(appCls);
    }

    @Override
    public void destroy(final WicketFilter filter) {
        // nothing
    }
}
