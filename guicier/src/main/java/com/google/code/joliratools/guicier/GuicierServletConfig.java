/**
 * Copyright (c) 2011 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import com.google.code.joliratools.plugins.PluginManager;
import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 * Creates the injector, which can be reset. Also binds the different scopes.
 * 
 * @author jfk
 * @date Aug 1, 2011 9:43:24 PM
 * @since 1.0
 * 
 */
public abstract class GuicierServletConfig extends GuiceServletContextListener {
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
     * @return the configuration type to be used for the injector.
     */
    protected abstract Stage getConfigurationType();

    @Override
    protected Injector getInjector() {
        final Stage stage = getConfigurationType();

        return create(stage, new Module() {
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
    }

    /**
     * Provides access to the request scoped provider. Accesses the request
     * cycle and tries to retrieve values from the
     * {@link GuicierWebRequestCycle}. Ha to be overridden if we are not using
     * this type of cycle.
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
        return new Provider<T>() {
            @Override
            public T get() {
                final GuicierWebRequestCycle cycle = GuicierWebRequestCycle.get();

                if (cycle == null) {
                    return unscoped.get();
                }

                final Provider<T> provider = cycle.scope(key, unscoped);

                return provider.get();
            }

        };
    }

    /**
     * Provides access to the session scoped provider. Accesses the request
     * cycle and tries to retrieve values from the
     * {@link GuicierWebRequestCycle}. Ha to be overridden if we are not using
     * this type of cycle.
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
        return new Provider<T>() {

            @Override
            public T get() {
                final GuicierWebSession cycle = GuicierWebSession.get();

                if (cycle == null) {
                    return unscoped.get();
                }

                final Provider<T> provider = cycle.scope(key, unscoped);

                return provider.get();
            }
        };
    }
}
