/**
 * Copyright (c) 2011 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import static org.apache.wicket.protocol.http.WicketFilter.FILTER_MAPPING_PARAM;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;

import com.google.inject.Binder;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

/**
 * Defined the components of the Wicket application.
 * 
 * @author jfk
 * @date Aug 2, 2011 7:47:23 AM
 * @since 1.0
 * 
 */
public abstract class GuicierServletModule extends ServletModule {
    private static final TypeLiteral<Class<? extends WebApplication>> WEBAPP_CLASS_LITERAL = new TypeLiteral<Class<? extends WebApplication>>() {
        // nothing
    };

    @Override
    protected void configureServlets() {
        final String wicketFilterPath = getWicketFilterPath();
        final Class<GuicierWicketFilter> wicketFilterClass = getWicketFilterClass();
        final Binder binder = binder();
        final Map<String, String> properties = new HashMap<String, String>();
        final Class<? extends WebApplication> appCls = getWebApplicationClass();

        bind(WEBAPP_CLASS_LITERAL).toInstance(appCls);
        filter(wicketFilterPath).through(wicketFilterClass);
        bind(IWebApplicationFactory.class).to(GuicierWebApplicationFactory.class);
        properties.put(FILTER_MAPPING_PARAM, wicketFilterPath);
        Names.bindProperties(binder, properties);
    }

    /**
     * @return the application class to be used
     */
    protected abstract Class<? extends WebApplication> getWebApplicationClass();

    /**
     * @return
     */
    protected Class<GuicierWicketFilter> getWicketFilterClass() {
        return GuicierWicketFilter.class;
    }

    /**
     * @return the filter path
     */
    protected String getWicketFilterPath() {
        return "/*";
    }
}
