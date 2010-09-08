/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebSession;

import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * @author jfk
 * @date Sep 8, 2010 12:57:21 PM
 * @since 1.0
 */
public class GuicierWebSession extends WebSession {
    private static final long serialVersionUID = 7170912849536454751L;

    /**
     * Get the session for the calling thread.
     * 
     * @return Session for calling thread
     */
    public static GuicierWebSession get() {
        return (GuicierWebSession) Session.get();
    }

    private final Map<Key<?>, Provider<?>> cachedProviders = new HashMap<Key<?>, Provider<?>>();

    /**
     * Create a new session. Implementation just calls the super-class.
     * 
     * @param request
     *            the request object triggering the session create.
     */
    public GuicierWebSession(final Request request) {
        super(request);
    }

    /**
     * Create a session scoped provider.
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
