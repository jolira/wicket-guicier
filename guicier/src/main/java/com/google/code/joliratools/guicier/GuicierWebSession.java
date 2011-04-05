/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License 2.0 which is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
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
    private static final String GUICIER_CHACHED_PROVIDERS = "guicierChachedProviders";
    private static final long serialVersionUID = 7170912849536454751L;

    /**
     * Get the session for the calling thread.
     * 
     * @return Session for calling thread
     */
    public static GuicierWebSession get() {
        return (GuicierWebSession) Session.get();
    }

    private static Map<Key<?>, Provider<?>> getCachedProviders() {
        final WebRequestCycle requestCycle = (WebRequestCycle) RequestCycle.get();
        final WebRequest webRequest = requestCycle.getWebRequest();
        final HttpServletRequest httpServletRequest = webRequest.getHttpServletRequest();
        final HttpSession session = httpServletRequest.getSession();

        @SuppressWarnings("unchecked")
        Map<Key<?>, Provider<?>> cachedProviders = (Map<Key<?>, Provider<?>>) session
                .getAttribute(GUICIER_CHACHED_PROVIDERS);

        if (cachedProviders != null) {
            return cachedProviders;
        }

        cachedProviders = new HashMap<Key<?>, Provider<?>>();
        session.setAttribute(GUICIER_CHACHED_PROVIDERS, cachedProviders);

        return cachedProviders;
    }

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
        final Map<Key<?>, Provider<?>> cachedProviders = getCachedProviders();

        @SuppressWarnings("unchecked")
        Provider<T> provider = (Provider<T>) cachedProviders.get(key);

        if (provider == null) {
            provider = new GuicerProvider<T>(creator);

            cachedProviders.put(key, provider);
        }

        return provider;
    }
}
