package com.google.code.joliratools.guicier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.google.inject.Provider;

/**
 * @author jfk
 * @date Sep 8, 2010 1:14:01 PM
 * @since 1.0
 */
public class GuicerProviderTest {
    /**
     * Test the get call
     */
    @Test
    public void testGet() {
        final GuicerProvider<Integer> provider = new GuicerProvider<Integer>(new Provider<Integer>() {
            @Override
            public Integer get() {
                return Integer.valueOf(Integer.MAX_VALUE);
            }
        });

        final Integer val1 = provider.get();
        final Integer val2 = provider.get();

        assertEquals(Integer.MAX_VALUE, val1.intValue());
        assertEquals(Integer.MAX_VALUE, val2.intValue());
    }
}
