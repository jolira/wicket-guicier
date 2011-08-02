package com.jolira.guicier;

import javax.inject.Inject;

import com.google.code.joliratools.guicier.GuicierWebApplication;
import com.google.code.joliratools.guicier.InjectorResetter;
import com.google.inject.Injector;

/**
 * Application object for your web application. If you want to run this application without deploying, run the Start
 * class.
 */
public class WicketGucierDemoApplication extends GuicierWebApplication {
    @Inject
    WicketGucierDemoApplication(final Injector injector, InjectorResetter resetter) {
        super(injector, resetter);
    }

    @Override
    public Class<HomePage> getHomePage() {
        return HomePage.class;
    }
}
