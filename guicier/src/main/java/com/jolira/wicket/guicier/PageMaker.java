package com.jolira.wicket.guicier;

import org.apache.wicket.Page;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * An interface for creating pages.
 * 
 * @author jfk
 * @date Jun 28, 2010 8:31:16 PM
 * @since 1.0
 */
public interface PageMaker {
    /**
     * Creates a new page using the specified parameter.
     * 
     * @param parameters
     *            the parameter to be passed to the new page
     * @return the new page
     */
    Page create(PageParameters parameters);
}
