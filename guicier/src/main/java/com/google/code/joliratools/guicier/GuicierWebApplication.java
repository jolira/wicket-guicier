/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.settings.ISessionSettings;

import com.google.code.joliratools.GuicierPageFactory;
import com.google.inject.Injector;

/**
 * Set up the components responsible for page creation.
 * 
 * @author jfk
 * @date Sep 7, 2010 2:49:30 PM
 * @since 1.0
 */
public abstract class GuicierWebApplication extends WebApplication {
    private final Injector injector;

    /**
     * Create a new application.
     * 
     * @param injector
     *            the managed injector to be used.
     * @param resetter
     *            the resetter
     */
    @Inject
    protected GuicierWebApplication(final Injector injector) {
        this.injector = injector;
    }

    /**
     * @return the injector used by the application
     */
    protected synchronized Injector getInjector() {
        return injector;
    }

    /**
     * Installs the {@link GuicierPageFactory} and a customized {@link IComponentInstantiationListener}.
     * 
     * @see WebApplication#init()
     */
    @Override
    protected void init() {
        super.init();

        final ISessionSettings settings = getSessionSettings();

        settings.setPageFactory(new GuicierPageFactoryProxy() {
            @Override
            Injector getInjector() {
                return GuicierWebApplication.this.getInjector();
            }
        });
        addComponentInstantiationListener(new IComponentInstantiationListener() {
            @Override
            public void onInstantiation(final Component component) {
                final Injector i = GuicierWebApplication.this.getInjector();

                i.injectMembers(component);
            }
        });
    }

    /**
     * Create a {@link GuicierWebRequestCycle}.
     * 
     * @see WebApplication#newRequestCycle(Request, Response)
     */
    @Override
    public RequestCycle newRequestCycle(final Request request, final Response response) {
        return new GuicierWebRequestCycle(this, (WebRequest) request, response);
    }

    /**
     * Creates a {@link GuicierWebSession}.
     * 
     * @see WebApplication#newSession(Request, Response)
     */
    @Override
    public Session newSession(final Request request, final Response response) {
        return new GuicierWebSession(request);
    }
}
