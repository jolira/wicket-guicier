package com.jolira.guicier;

import junit.framework.TestCase;

import org.apache.wicket.util.tester.WicketTester;

import com.jolira.guicier.HomePage;
import com.jolira.guicier.WicketApplication;

/**
 * Simple test using the WicketTester
 */
public class TestHomePage extends TestCase {
    private WicketTester tester;

    @Override
    public void setUp() {
        tester = new WicketTester(new WicketApplication());
    }

    public void testRenderMyPage() {
        // start and render the test page
        tester.startPage(HomePage.class);

        // assert rendered page class
        tester.assertRenderedPage(HomePage.class);
    }
}
