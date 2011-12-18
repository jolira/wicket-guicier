/**
 * Copyright (c) 2011 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.jolira.wicket.guicier;

import java.util.Enumeration;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WicketFilter;

/**
 * Create a wicket filter. The filter deals with the fact that wicket has to
 * know the primary filter mapping and set the factory.
 * 
 * @author jfk
 * @date Aug 2, 2011 7:14:13 AM
 * @since 1.0
 * 
 */
@Singleton
public class GuicierWicketFilter extends WicketFilter {
    private final IWebApplicationFactory factory;
    private final String filterMapping;

    /**
     * Create a new instance
     * 
     * @param factory
     *            the factory used to create the web application
     * @param filterMapping
     *            the filter mapping string
     */
    @Inject
    protected GuicierWicketFilter(final IWebApplicationFactory factory,
            @Named(FILTER_MAPPING_PARAM) final String filterMapping) {
        this.factory = factory;
        this.filterMapping = filterMapping;
    }

    @Override
    protected IWebApplicationFactory getApplicationFactory() {
        return factory;
    }

    @Override
    public void init(final boolean isServlet, final FilterConfig filterConfig) throws ServletException {
        final String _filterMapping = filterMapping;

        super.init(isServlet, new FilterConfig() {

            @Override
            public String getFilterName() {
                return filterConfig.getFilterName();
            }

            @Override
            public String getInitParameter(final String name) {
                if (FILTER_MAPPING_PARAM.equals(name)) {
                    return _filterMapping;
                }

                return filterConfig.getInitParameter(name);
            }

            @SuppressWarnings("rawtypes")
            @Override
            public Enumeration getInitParameterNames() {
                return filterConfig.getInitParameterNames();
            }

            @Override
            public ServletContext getServletContext() {
                return filterConfig.getServletContext();
            }
        });
    }
}
