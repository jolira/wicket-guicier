package com.jolira.guicier;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

/**
 * For testing only
 */
public class HomePage extends WebPage {
    public HomePage() {
        final PageParameters params = new PageParameters();

        params.put("name", "jolira");

        add(new BookmarkablePageLink<Void>("link", DemoPage.class, params));
    }
}