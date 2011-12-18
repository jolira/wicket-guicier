/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.jolira.wicket.guicier;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * Just like its superclass, but can store scoped values that are
 * {@link RequestScoped}.
 * 
 * @author jfk
 * @date Sep 8, 2010 11:03:32 AM
 * @since 1.0
 */
public class GuicierWebRequestCycle extends RequestCycle {
    public static GuicierWebRequestCycle get() {
        return (GuicierWebRequestCycle) RequestCycle.get();
    }

    private final Map<Key<?>, Provider<?>> cachedProviders = new HashMap<Key<?>, Provider<?>>();

    /**
     * Create a new one.
     * 
     * @param context
     *            the context
     */
    public GuicierWebRequestCycle(final RequestCycleContext context) {
        super(context);
    }

    /**
     * Create a request scoped provider.
     * 
     * @param key
     *            the Guice key
     * @param creator
     *            the unscoped creator
     * @param <T>
     *            the type of object create by the provider
     * @return the provider
     * @see Scope#scope(Key, Provider)
     */
    protected synchronized <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
        @SuppressWarnings("unchecked")
        Provider<T> provider = (Provider<T>) cachedProviders.get(key);

        if (provider == null) {
            provider = new GuicerProvider<T>(creator);

            cachedProviders.put(key, provider);
        }

        return provider;
    }
}
