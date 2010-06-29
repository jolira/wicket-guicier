/**
 *
 */
package com.google.code.joliratools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.convert.IConverter;
import org.junit.Test;

import com.google.code.joliratools.Guicier.StringConverter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author jfk
 */
public class GuicierTest {
    private static class ParameterMock implements Parameter {
        private final String value;
        private final boolean optional;
        private final Class<? extends IConverter> converterClass;

        public ParameterMock(final String value) {
            this(value, false, IConverter.class);
        }

        public ParameterMock(final String value, final boolean optional,
                final Class<? extends IConverter> converterClass) {
            this.converterClass = converterClass;
            this.optional = optional;
            this.value = value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Parameter.class;
        }

        @Override
        public Class<? extends IConverter> converter() {
            return converterClass;
        }

        @Override
        public boolean optional() {
            return optional;
        }

        @Override
        public String value() {
            return value;
        }
    }

    static enum State {
        ON, OFF
    }

    /**
     * A test object
     */
    public static class TestObject {
        @Inject
        TestObject(@Parameter("x") final int x, final Object unused,
                @Parameter(value = "y", optional = true) final long y, @Nullable @Parameter("z") final String z) {
            assertEquals(1, x);
            assertEquals(2, y);
            assertEquals("3", z);
        }
    }

    /**
     * A test object
     */
    public static class TestObject2 {
        @Inject
        TestObject2(@Parameter("x") final float x, @Parameter(value = "y", optional = true) final double y,
                @Parameter(value = "z", optional = true) final String z) {
            assertEquals(1, x, 0.0);
            assertEquals(0, y, 0.0);
            assertNull(z);
        }
    }

    /**
     * A test object
     */
    public static class TestObject3 {
        @Inject
        TestObject3(@Parameter("state") final State x) {
            assertEquals(State.OFF, x);
        }
    }

    /**
     * Test enum parameters
     */
    @Test
    public void testGetEnumParameters() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final PageParameters params = guicier.get(TestObject3.class, State.OFF);

        assertEquals(1, params.size());
        assertEquals("OFF", params.get("state"));
    }

    /**
     * Test optional parameters
     */
    @Test
    public void testGetOptionalParameters() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        @SuppressWarnings("boxing")
        final PageParameters params = guicier.get(TestObject2.class, 1.0f);

        assertEquals(1, params.size());
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class, PageParameters).
     */
    @Test
    public void testGetPageParameters() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();

        parameters.put("company", "jolira");

        final PageParameters value = guicier.get(parameters, param, PageParameters.class, cleansed);

        assertSame(cleansed, value);
        assertEquals(0, cleansed.size());
    }

    /**
     *
     */
    @Test
    public void testGetParameters() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        @SuppressWarnings("boxing")
        final PageParameters params = guicier.get(TestObject.class, 1, 2l, "3");

        assertEquals(3, params.size());
        assertEquals("1", params.get("x"));
        assertEquals("2", params.get("y"));
        assertEquals("3", params.get("z"));
    }

    /**
     *
     */
    @Test(expected = WicketRuntimeException.class)
    public void testGetTooManyParameters() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        @SuppressWarnings("boxing")
        final PageParameters params = guicier.get(TestObject.class, 1, 2l, "3", 4.0);

        assertEquals(3, params.size());
        assertEquals("1", params.get("x"));
        assertEquals("2", params.get("y"));
        assertEquals("3", params.get("z"));
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class).
     */
    @Test(expected = WicketRuntimeException.class)
    public void testNoConverter() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();

        parameters.put("company", "jolira");

        final Object value = guicier.get(parameters, param, Map.class, cleansed);

        assertSame(parameters, value);
        assertEquals(1, cleansed.size());
        assertEquals("jolira", cleansed.get("company"));
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class).
     */
    @Test
    public void testSpecificConverter() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("company", false, StringConverter.class);
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();

        parameters.put("company", "jolira");

        final Object value = guicier.get(parameters, param, String.class, cleansed);

        assertEquals("jolira", value);
        assertEquals(1, cleansed.size());
        assertEquals("jolira", cleansed.get("company"));
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class).
     */
    @Test
    public void testStringArray0GetValue() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();

        parameters.put("company", new String[0]);

        final Object value = guicier.get(parameters, param, String.class, cleansed);

        assertNull(value);
        assertEquals(1, cleansed.size());

        final String[] actual = (String[]) cleansed.get("company");

        assertEquals(0, actual.length);
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class).
     */
    @Test
    public void testStringArray1GetValue() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("value");
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();

        parameters.put("value", new String[] { "jolira" });

        final String value = guicier.get(parameters, param, String.class, cleansed);

        assertEquals("jolira", value);
        assertEquals(1, cleansed.size());

        final String[] actual = (String[]) cleansed.get("value");

        assertEquals(1, actual.length);
        assertEquals("jolira", actual[0]);
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class).
     */
    @Test
    public void testStringArray3GetValue() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();

        parameters.put("company", new String[] { "1", "2", "3" });

        final String[] value = guicier.get(parameters, param, String[].class, cleansed);

        assertEquals(3, value.length);
        assertEquals("1", value[0]);
        assertEquals("2", value[1]);
        assertEquals("3", value[2]);
        assertEquals(1, cleansed.size());

        final String[] actual = (String[]) cleansed.get("company");

        assertEquals(3, actual.length);
        assertEquals("1", actual[0]);
        assertEquals("2", actual[1]);
        assertEquals("3", actual[2]);
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class).
     */
    @Test
    public void testStringGetNullValue() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();
        final Object value = guicier.get(parameters, param, String.class, cleansed);

        assertNull(value);
        assertEquals(0, cleansed.size());
    }

    /**
     * Test method for Guicier#get(PageParameters, Parameter, Class).
     */
    @Test
    public void testStringGetValue() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();
        final PageParameters cleansed = new PageParameters();

        parameters.put("company", "jolira");

        final Object value = guicier.get(parameters, param, String.class, cleansed);

        assertEquals("jolira", value);
        assertEquals(1, cleansed.size());
        assertEquals("jolira", cleansed.get("company"));
    }
}
