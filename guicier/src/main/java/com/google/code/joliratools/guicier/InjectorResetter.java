/**
 * Copyright (c) 2011 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

/**
 * Allows consumers of the Injector request a reset.
 * 
 * @author jfk
 * @date Aug 2, 2011 11:32:37 AM
 * @since 1.0
 * 
 */
public interface InjectorResetter {
    /**
     * Reset the injector.
     */
    public void reset();
}
