/**
 * Copyright (c) 2011 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.code.joliratools.plugins.PluginManager;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.MembersInjector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scope;
import com.google.inject.Stage;
import com.google.inject.TypeLiteral;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.spi.TypeConverterBinding;

/**
 * Creates the injector, which can be reset. Also binds the different scopes.
 * 
 * @author jfk
 * @date Aug 1, 2011 9:43:24 PM
 * @since 1.0
 * 
 */
public abstract class GuicierServletConfig extends GuiceServletContextListener {
    Injector delegate = null;
    private final Injector injector = new Injector() {
        @Override
        public Injector createChildInjector(final Iterable<? extends Module> modules) {
            final Injector i = getDelegate();

            return i.createChildInjector(modules);
        }

        @Override
        public Injector createChildInjector(final Module... modules) {
            final Injector i = getDelegate();

            return i.createChildInjector(modules);
        }

        @Override
        public <T> List<Binding<T>> findBindingsByType(final TypeLiteral<T> type) {
            final Injector i = getDelegate();

            return i.findBindingsByType(type);
        }

        @Override
        public Map<Key<?>, Binding<?>> getAllBindings() {
            final Injector i = getDelegate();

            return i.getAllBindings();
        }

        @Override
        public <T> Binding<T> getBinding(final Class<T> type) {
            final Injector i = getDelegate();

            return i.getBinding(type);
        }

        @Override
        public <T> Binding<T> getBinding(final Key<T> key) {
            final Injector i = getDelegate();

            return i.getBinding(key);
        }

        @Override
        public Map<Key<?>, Binding<?>> getBindings() {
            final Injector i = getDelegate();

            return i.getBindings();
        }

        @Override
        public <T> Binding<T> getExistingBinding(final Key<T> key) {
            final Injector i = getDelegate();

            return i.getExistingBinding(key);
        }

        @Override
        public <T> T getInstance(final Class<T> type) {
            final Injector i = getDelegate();

            return i.getInstance(type);
        }

        @Override
        public <T> T getInstance(final Key<T> key) {
            final Injector i = getDelegate();

            return i.getInstance(key);
        }

        @Override
        public <T> MembersInjector<T> getMembersInjector(final Class<T> type) {
            final Injector i = getDelegate();

            return i.getMembersInjector(type);
        }

        @Override
        public <T> MembersInjector<T> getMembersInjector(final TypeLiteral<T> typeLiteral) {
            final Injector i = getDelegate();

            return i.getMembersInjector(typeLiteral);
        }

        @Override
        public Injector getParent() {
            final Injector i = getDelegate();

            return i.getParent();
        }

        @Override
        public <T> Provider<T> getProvider(final Class<T> type) {
            final Injector i = getDelegate();

            return i.getProvider(type);
        }

        @Override
        public <T> Provider<T> getProvider(final Key<T> key) {
            final Injector i = getDelegate();

            return i.getProvider(key);
        }

        @Override
        public Map<Class<? extends Annotation>, Scope> getScopeBindings() {
            final Injector i = getDelegate();

            return i.getScopeBindings();
        }

        @Override
        public Set<TypeConverterBinding> getTypeConverterBindings() {
            final Injector i = getDelegate();

            return i.getTypeConverterBindings();
        }

        @Override
        public void injectMembers(final Object instance) {
            final Injector i = getDelegate();

            i.injectMembers(instance);
        }
    };

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

    synchronized Injector getDelegate() {
        final Injector _delegate = delegate;

        if (_delegate != null) {
            return _delegate;
        }

        final Stage stage = getConfigurationType();
        final Injector i = create(stage, new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(InjectorResetter.class).toInstance(new InjectorResetter() {
                    @Override
                    public void reset() {
                        delegate = null;
                    }
                });
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

        delegate = i;

        return i;
    }

    @Override
    protected Injector getInjector() {
        return injector;
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
        return new Provider<T>() {
            @Override
            public T get() {
                final GuicierWebRequestCycle cycle = GuicierWebRequestCycle.get();
                final Provider<T> provider = cycle.scope(key, unscoped);

                return provider.get();
            }

        };
    }

    /**
     * Resets the injector by nullifying it. A new injector will be created next time {@link #getInjector()} is called.
     */
    protected void resetInjector() {
        delegate = null;
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
        return new Provider<T>() {

            @Override
            public T get() {
                final GuicierWebSession cycle = GuicierWebSession.get();
                final Provider<T> provider = cycle.scope(key, unscoped);

                return provider.get();
            }
        };
    }
}
