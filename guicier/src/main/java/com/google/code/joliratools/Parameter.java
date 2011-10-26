/**
 *
 */
package com.google.code.joliratools;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.apache.wicket.PageParameters;
import org.apache.wicket.util.convert.IConverter;

/**
 * Identifies a particular entry in a {@link PageParameters} object, which is associated with a particular parameter.
 * 
 * @author jfk
 * @see PageParameters
 */
@Target({ PARAMETER, CONSTRUCTOR, FIELD })
@Retention(RUNTIME)
@Documented
public @interface Parameter {
    /**
     * @return specifies what converter should be used for this paramter.
     */
    Class<? extends IConverter> converter() default IConverter.class;

    /**
     * Specifies whether this parameter must be present in the {@link PageParameters} list.
     */
    boolean optional() default false;

    /**
     * Specifies whether this parameter should be checked for character that are used for SQL and
     * other types of injection.
     */
    String verifier() default "[\\w_\\-%\\*\\./: ]*";

    /**
     * Identifies the name of the parameter. This is the string to used used as a key in {@link PageParameters}.
     */
    String value();
}
