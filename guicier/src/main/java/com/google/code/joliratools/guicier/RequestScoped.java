/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

/**
 * This is a customized scope for Guice. Any type annotated using this scope will be available throughout one request.
 * 
 * @author jfk
 * @date Sep 7, 2010 2:15:51 PM
 * @since 1.0
 */
@Target({ TYPE })
@Retention(RUNTIME)
@ScopeAnnotation
@Documented
public @interface RequestScoped {
    // nothing
}
