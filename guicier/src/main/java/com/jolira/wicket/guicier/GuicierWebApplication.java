/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.jolira.wicket.guicier;

import javax.inject.Inject;

import org.apache.wicket.Component;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.IRequestCycleProvider;
import org.apache.wicket.Session;
import org.apache.wicket.application.ComponentInstantiationListenerCollection;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;

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
     * Installs the {@link GuicierPageFactory} and a customized
     * {@link IComponentInstantiationListener}.
     * 
     * @see WebApplication#init()
     */
    @Override
    protected void init() {
        super.init();

        final ComponentInstantiationListenerCollection listeners = getComponentInstantiationListeners();

        listeners.add(new IComponentInstantiationListener() {
            @Override
            public void onInstantiation(final Component component) {
                final Injector i = GuicierWebApplication.this.getInjector();

                i.injectMembers(component);
            }
        });

        setRequestCycleProvider(new IRequestCycleProvider() {
            @Override
            public RequestCycle get(final RequestCycleContext context) {
                return new GuicierWebRequestCycle(context);
            }
        });
    }

    @Override
    protected IPageFactory newPageFactory() {
        return new GuicierPageFactoryProxy() {
            @Override
            Injector getInjector() {
                return GuicierWebApplication.this.getInjector();
            }
        };
    }

    /**
     * /** Creates a {@link GuicierWebSession}.
     * 
     * @see WebApplication#newSession(Request, Response)
     */
    @Override
    public Session newSession(final Request request, final Response response) {
        return new GuicierWebSession(request);
    }
}
