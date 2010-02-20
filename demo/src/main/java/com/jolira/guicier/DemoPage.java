package com.jolira.guicier;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;

import com.google.code.joliratools.Parameter;
import com.google.inject.Inject;

public class DemoPage extends WebPage {
    @Inject
    public DemoPage(@Parameter("offset") final int offset) {
        add(new Label("offset", "" + offset));
        add(new BookmarkablePageLink<Void>("link", HomePage.class));
    }
}
