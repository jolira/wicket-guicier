package com.jolira.wicket.guicier;

import com.google.inject.Provider;

final class GuicerProvider<T> implements Provider<T> {
    private T cached = null;
    private final Provider<T> creator;
    private boolean resolved = false;

    GuicerProvider(final Provider<T> creator) {
        this.creator = creator;
    }

    @Override
    public synchronized T get() {
        if (!resolved) {
            cached = creator.get();
            resolved = true;
        }

        return cached;
    }
}
