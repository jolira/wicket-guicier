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
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import com.google.inject.Injector;

/**
 * @author jfk
 */
@Singleton
public class GuicierPageFactory implements IPageFactory {
    private final static class PageCreatorProxy implements PageMaker {
        private final Injector injector;
        private final Class<? extends IRequestablePage> pageClass;
        private PageMaker delegate = null;

        public PageCreatorProxy(final Injector injector, final Class<? extends IRequestablePage> pageClass) {
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
    private final Map<Class<? extends IRequestablePage>, PageMaker> makerCache = new WeakHashMap<Class<? extends IRequestablePage>, PageMaker>();

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

    private synchronized <C extends IRequestablePage> PageMaker getCreator(final Class<C> pageClass) {
        final PageMaker creator = makerCache.get(pageClass);

        if (creator != null) {
            return creator;
        }

        final PageMaker _creator = new PageCreatorProxy(injector, pageClass);

        makerCache.put(pageClass, _creator);

        return _creator;
    }

    @Override
    public <C extends IRequestablePage> boolean isBookmarkable(final Class<C> arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <C extends IRequestablePage> IRequestablePage newPage(final Class<C> pageClass) {
        return newPage(pageClass, null);
    }

    @Override
    public <C extends IRequestablePage> IRequestablePage newPage(final Class<C> pageClass,
            final PageParameters parameters) {
        final PageMaker creator = getCreator(pageClass);

        return creator.create(parameters);
    }
}
