/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import static com.google.inject.Stage.PRODUCTION;

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
import com.google.code.joliratools.plugins.PluginManager;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;

/**
 * @author jfk
 * @date Sep 7, 2010 2:49:30 PM
 * @since 1.0
 */
public abstract class GuicierWebApplication extends WebApplication {
    private Injector injector = null;

    /**
     * Creates a new injector. Should be overridden for unit test environments.
     * 
     * @param stage
     *            the stage to be created
     * @param modules
     *            an optional set of module to be loaded
     * @return the injector.
     */
    protected Injector create(final Stage stage, final Module... modules) {
        final PluginManager mgr = new PluginManager(stage, modules);

        return mgr.getInjector();
    }

    /**
     * Checks whether an injector already exists and create a new one if necessary using the
     * {@link #create(Stage, Module...)} call. The {@code state} parameter is determined by calling
     * {@link GuicierWebApplication#getConfigurationType()}.
     * <p>
     * Once the injector is created, the {@link GuicierPageFactory} is installed as the page factory.
     * 
     * @return the injector used by the application
     */
    protected synchronized Injector getInjector() {
        if (injector != null) {
            return injector;
        }

        final String configType = getConfigurationType();
        final Stage stage = DEVELOPMENT.equals(configType) ? Stage.DEVELOPMENT : PRODUCTION;
        final Injector i = create(stage, new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bindScope(RequestScoped.class, new Scope() {
                    @Override
                    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
                        return requestScoped(key, unscoped);
                    }
                });
                binder.bindScope(SessionScoped.class, new Scope() {
                    @Override
                    public <T> Provider<T> scope(final Key<T> key, final Provider<T> unscoped) {
                        return sessionScoped(key, unscoped);
                    }
                });
            }
        });

        return injector = i;
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

    /**
     * Provides access to the request scoped provider. Accesses the request cycle and tries to retrieve values from the
     * {@link GuicierWebRequestCycle}. Ha to be overridden if we are not using this type of cycle.
     * 
     * @param <T>
     *            the type of the requested object.
     * @param key
     *            the key (as defined by Guice)
     * @param unscoped
     *            the provider for the unscoped value
     * @return the provider
     */
    protected <T> Provider<T> requestScoped(final Key<T> key, final Provider<T> unscoped) {
        final GuicierWebRequestCycle cycle = GuicierWebRequestCycle.get();

        return cycle.scope(key, unscoped);
    }

    /**
     * Resets the injector by nullifying it. A new injector will be created next time {@link #getInjector()} is called.
     */
    protected final void resetInjector() {
        injector = null;
    }

    /**
     * Provides access to the session scoped provider. Accesses the request cycle and tries to retrieve values from the
     * {@link GuicierWebRequestCycle}. Ha to be overridden if we are not using this type of cycle.
     * 
     * @param <T>
     *            the type of the requested object.
     * @param key
     *            the key (as defined by Guice)
     * @param unscoped
     *            the provider for the unscoped value
     * @return the provider
     */
    protected <T> Provider<T> sessionScoped(final Key<T> key, final Provider<T> unscoped) {
        final GuicierWebSession cycle = GuicierWebSession.get();

        return cycle.scope(key, unscoped);
    }
}
