package com.google.code.joliratools.guicier;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;

/**
 * Homepage
 */
public class HomePage extends WebPage {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor that is invoked when page is invoked without a session.
     * 
     * @param parameters
     *            Page parameters
     */
    public HomePage(final PageParameters parameters) {
        final Link<String> homeLink = new BookmarkablePageLink<String>("home",
                HomePage.class);

        add(homeLink);
    }
}
