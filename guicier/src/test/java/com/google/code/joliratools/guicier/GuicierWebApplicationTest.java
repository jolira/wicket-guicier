/**
 * Copyright (c) 2010 jolira. All rights reserved. This program and the accompanying materials are made available under
 * the terms of the GNU Public License 2.0 which is available at http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 */

package com.google.code.joliratools.guicier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

/**
 * @author jfk
 * @date Sep 8, 2010 1:25:30 PM
 * @since 1.0
 */
public class GuicierWebApplicationTest {
    /**
     * Dummy interface
     */
    public static interface MyInterface {
        // nothing
    }

    /**
     * Dummy implementation
     */
    @RequestScoped
    public static class MyRequestScopedImplementation implements MyInterface {
        // nothing
    }

    /**
     * Dummy implementation
     */
    @SessionScoped
    public static class MySessionScopedImplementation implements MyInterface {
        // nothing
    }

    /**
     * Test class
     */
    public static class TestPage extends HomePage {
        @Inject
        TestPage(final String provider, final MyInterface iface, final PageParameters parameters) {
            super(parameters);

            assertEquals("jolira", provider);
            assertNotNull(iface);
        }
    }

    /**
     * Test method for
     * {@link GuicierWebApplication#newRequestCycle(org.apache.wicket.Request, org.apache.wicket.Response)}.
     */
    @Test
    public void testApplication() {
        final GuicierWebApplication app = new GuicierWebApplication() {
            @Override
            protected Injector create(final Stage stage, final Module... modules) {
                final Collection<Module> _modules = new ArrayList<Module>();

                _modules.addAll(Arrays.asList(modules));
                _modules.add(new Module() {
                    @Override
                    public void configure(final Binder binder) {
                        binder.bind(String.class).toInstance("jolira");
                        binder.bind(MyInterface.class).to(MyRequestScopedImplementation.class);
                    }
                });

                final int size = _modules.size();

                return super.create(stage, _modules.toArray(new Module[size]));
            }

            @Override
            public Class<? extends Page> getHomePage() {
                return HomePage.class;
            }
        };
        final WicketTester tester = new WicketTester(app);

        tester.startPage(TestPage.class);
        app.resetInjector();
        tester.startPage(TestPage.class);
    }

    /**
     * Test method for
     * {@link GuicierWebApplication#newRequestCycle(org.apache.wicket.Request, org.apache.wicket.Response)}.
     */
    @Test
    public void testNonDevApplication() {
        final WicketTester tester = new WicketTester(new GuicierWebApplication() {
            @Override
            protected Injector create(final Stage stage, final Module... modules) {
                final Collection<Module> _modules = new ArrayList<Module>();

                _modules.addAll(Arrays.asList(modules));
                _modules.add(new Module() {
                    @Override
                    public void configure(final Binder binder) {
                        binder.bind(String.class).toInstance("jolira");
                        binder.bind(MyInterface.class).to(MyRequestScopedImplementation.class);
                    }
                });

                final int size = _modules.size();

                return super.create(stage, _modules.toArray(new Module[size]));
            }

            @Override
            public String getConfigurationType() {
                return Application.DEPLOYMENT;
            }

            @Override
            public Class<? extends Page> getHomePage() {
                return HomePage.class;
            }
        });

        tester.startPage(TestPage.class);
    }

    /**
     * Test method for
     * {@link GuicierWebApplication#newRequestCycle(org.apache.wicket.Request, org.apache.wicket.Response)}.
     */
    @Test
    public void testSessionScoped() {
        final GuicierWebApplication app = new GuicierWebApplication() {
            @Override
            protected Injector create(final Stage stage, final Module... modules) {
                final Collection<Module> _modules = new ArrayList<Module>();

                _modules.addAll(Arrays.asList(modules));
                _modules.add(new Module() {
                    @Override
                    public void configure(final Binder binder) {
                        binder.bind(String.class).toInstance("jolira");
                        binder.bind(MyInterface.class).to(MySessionScopedImplementation.class);
                    }
                });

                final int size = _modules.size();

                return super.create(stage, _modules.toArray(new Module[size]));
            }

            @Override
            public Class<? extends Page> getHomePage() {
                return HomePage.class;
            }
        };
        final WicketTester tester = new WicketTester(app);

        tester.startPage(TestPage.class);
        app.resetInjector();
        tester.startPage(TestPage.class);
    }
}
