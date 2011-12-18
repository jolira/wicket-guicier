package com.jolira.guicier;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * For testing only
 */
public class HomePage extends WebPage {
    private static final long serialVersionUID = -7592981219887496438L;

    HomePage() {
        final PageParameters params = new PageParameters();

        params.add("offset", "15");

        add(new BookmarkablePageLink<Void>("link", DemoPage.class, params));
    }
}
