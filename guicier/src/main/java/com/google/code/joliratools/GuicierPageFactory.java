/**
 *
 */
package com.google.code.joliratools;

import java.util.Map;
import java.util.WeakHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;

import com.google.inject.Injector;

/**
 * @author jfk
 */
@Singleton
public class GuicierPageFactory implements IPageFactory {
    private final static class PageCreatorProxy implements PageMaker {
        private final Injector injector;
        private final Class<? extends Page> pageClass;
        private PageMaker delegate = null;

        <C extends Page> PageCreatorProxy(final Injector injector, final Class<C> pageClass) {
            this.injector = injector;
            this.pageClass = pageClass;
        }

        @Override
        public Page create(final PageParameters parameters) {
            final PageMaker _delegate = getDelegate();

            return _delegate.create(parameters);
        }

        private synchronized PageMaker getDelegate() {
            if (delegate != null) {
                return delegate;
            }

            return delegate = new PageCreatorImpl(injector, pageClass);
        }

    }

    private final Injector injector;
    private final Map<Class<? extends Page>, PageMaker> makerCache = new WeakHashMap<Class<? extends Page>, PageMaker>();

    /**
     * Create a new factory.
     * 
     * @param injector
     *            the injector to be used to create new pages.
     */
    @Inject
    public GuicierPageFactory(final Injector injector) {
        if (injector == null) {
            throw new IllegalArgumentException();
        }

        this.injector = injector;
    }

    private synchronized <C extends Page> PageMaker getCreator(final Class<C> pageClass) {
        final PageMaker creator = makerCache.get(pageClass);

        if (creator != null) {
            return creator;
        }

        final PageMaker _creator = new PageCreatorProxy(injector, pageClass);

        makerCache.put(pageClass, _creator);

        return _creator;
    }

    /**
     * @see IPageFactory#newPage(Class)
     */
    @Override
    public <C extends Page> Page newPage(final Class<C> pageClass) {
        return newPage(pageClass, null);
    }

    /**
     * @see IPageFactory#newPage(Class, PageParameters)
     */
    @Override
    public <C extends Page> Page newPage(final Class<C> pageClass, final PageParameters parameters) {
        final PageMaker creator = getCreator(pageClass);

        return creator.create(parameters);
    }
}
