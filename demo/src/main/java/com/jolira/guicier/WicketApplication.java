package com.jolira.guicier;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.settings.ISessionSettings;

import com.google.code.joliratools.GuicierPageFactory;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

/**
 * Application object for your web application. If you want to run this
 * application without deploying, run the Start class.
 * 
 * @see com.jolira.guicier.Start#main(String[])
 */
public class WicketApplication extends WebApplication {
    /**
     * @see org.apache.wicket.Application#getHomePage()
     */
    @Override
    public Class<HomePage> getHomePage() {
        return HomePage.class;
    }

    /**
     * Install the Guicer version of the page factory..
     * 
     * @see WebApplication#init()
     */
    @Override
    protected void init() {
        final Provider<? extends Page> provider = new Provider<Page>() {
            @Override
            public Page get() {
                // TODO Auto-generated method stub
                return null;
            }
        };
        final ISessionSettings sessionSettings = getSessionSettings();
        final Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(Page.class).toProvider(provider);

            }
        });
        final GuicierPageFactory pageFactory = new GuicierPageFactory(injector);

        sessionSettings.setPageFactory(pageFactory);

        super.init();
    }

}