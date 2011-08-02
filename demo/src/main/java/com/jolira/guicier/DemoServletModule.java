/**
 * Copyright (c) 2011 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.jolira.guicier;

import org.apache.wicket.protocol.http.WebApplication;

import com.google.code.joliratools.guicier.GuicierServletModule;

/**
 * A demo servlet module.
 * 
 */
public class DemoServletModule extends GuicierServletModule {

    @Override
    protected Class<? extends WebApplication> getWebApplicationClass() {
        return WicketGucierDemoApplication.class;
    }
}
