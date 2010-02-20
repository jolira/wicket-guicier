package com.google.code.joliratools;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;

public interface PageMaker {
    Page create(PageParameters parameters);
}
