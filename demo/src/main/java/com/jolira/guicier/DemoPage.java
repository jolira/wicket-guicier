package com.jolira.guicier;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

public class DemoPage extends WebPage {
    public DemoPage() {
        add(new BookmarkablePageLink<Void>("link", HomePage.class));
    }
}
