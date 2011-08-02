/**
 * Copyright (c) 2011 jolira.
 * All rights reserved. This program and the accompanying 
 * materials are made available under the terms of the 
 * GNU Public License 2.0 which is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.jolira.guicier;

import com.google.code.joliratools.guicier.WicketGucierServletConfig;
import com.google.inject.Stage;

/**
 * Create a development injector
 *
 */
public class DemoWicketGucierServletConfig extends WicketGucierServletConfig {
    @Override
    protected Stage getConfigurationType() {
        return Stage.DEVELOPMENT;
    }
}
