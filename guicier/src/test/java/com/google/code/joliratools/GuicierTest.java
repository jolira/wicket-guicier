/**
 *
 */
package com.google.code.joliratools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.StringValue;
import org.junit.Test;

import com.google.code.joliratools.Guicier.StringConverter;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author jfk
 */
public class GuicierTest {
    /**
     * A test object annotated with {@link javax.inject.Inject}
     */
    public static class JavaxTestObject {
        /**
         * @param unused
         */
        @javax.inject.Inject
        JavaxTestObject(@Parameter("x") final int x, final Object unused,
                @Parameter(value = "y", optional = true) final long y, @Nullable @Parameter("z") final String z) {
            assertEquals(1, x);
            assertEquals(2, y);
            assertEquals("3", z);
        }
    }

    /**
     * A test object
     */
    public static class NoInjectTestObject {
        /**
         * @param unused
         */
        NoInjectTestObject(@Parameter("x") final int x, final Object unused,
                @Parameter(value = "y", optional = true) final long y, @Nullable @Parameter("z") final String z) {
            assertEquals(1, x);
            assertEquals(2, y);
            assertEquals("3", z);
        }
    }

    private static class ParameterMock implements Parameter {
        private final String value;
        private final boolean optional;
        private final Class<? extends IConverter<?>> converterClass;

        public ParameterMock(final String value) {
            this(value, false, NoConverter.class);
        }

        public ParameterMock(final String value, final boolean optional,
                final Class<? extends IConverter<?>> converterClass) {
            this.converterClass = converterClass;
            this.optional = optional;
            this.value = value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Parameter.class;
        }

        @Override
        public Class<? extends IConverter<?>> converter() {
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

        @Override
        public String verifier() {
            return "[\\w]*";
        }
    }

    static enum State {
        ON, OFF
    }

    /**
     * A test object
     */
    public static class TestObject {
        /**
         * @param unused
         */
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

        assertEquals(1, params.getNamedKeys().size());
        assertEquals("OFF", params.getValues("state").get(0).toString());
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

        assertEquals(1, params.getNamedKeys().size());
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

        parameters.add("company", "jolira");

        final PageParameters value1 = guicier.get(parameters, param, PageParameters.class, cleansed, false);

        assertSame(cleansed, value1);
        assertEquals(0, cleansed.getNamedKeys().size());

        final PageParameters value2 = guicier.get(parameters, param, PageParameters.class, cleansed, true);

        assertSame(parameters, value2);
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

        assertEquals(3, params.getNamedKeys().size());
        assertEquals("1", params.getValues("x").get(0).toString());
        assertEquals("2", params.getValues("y").get(0).toString());
        assertEquals("3", params.getValues("z").get(0).toString());
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

        assertEquals(3, params.getNamedKeys().size());
        assertEquals("1", params.get("x"));
        assertEquals("2", params.get("y"));
        assertEquals("3", params.get("z"));
    }

    /**
     * Test enum parameters
     */
    @Test
    public void testJavaxGetParameters() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);
        @SuppressWarnings("boxing")
        final PageParameters params = guicier.get(JavaxTestObject.class, 1, 2l, "3");

        assertEquals(3, params.getNamedKeys().size());
        assertEquals("1", params.getValues("x").get(0).toString());
        assertEquals("2", params.getValues("y").get(0).toString());
        assertEquals("3", params.getValues("z").get(0).toString());
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

        parameters.add("company", "jolira");

        final Object value = guicier.get(parameters, param, Map.class, cleansed, false);

        assertSame(parameters, value);
        assertEquals(1, cleansed.getNamedKeys().size());
        assertEquals("jolira", cleansed.get("company"));
    }

    /**
     * Test what happens if there is no InjectStatement
     */
    @Test(expected = WicketRuntimeException.class)
    public void testNoInjectStatement() {
        final Injector injector = Guice.createInjector();
        final Guicier guicier = injector.getInstance(Guicier.class);

        guicier.get(NoInjectTestObject.class, Integer.valueOf(1), Long.valueOf(2l), "3");
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

        parameters.add("company", "jolira");

        final Object value = guicier.get(parameters, param, String.class, cleansed, false);

        assertEquals("jolira", value);
        assertEquals(1, cleansed.getNamedKeys().size());
        assertEquals("jolira", cleansed.getValues("company").get(0).toString());
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

        parameters.add("company", new String[0]);

        final Object value = guicier.get(parameters, param, String.class, cleansed, false);

        assertNull(value);
        assertEquals(0, cleansed.getNamedKeys().size());

        final List<StringValue> actual = cleansed.getValues("company");

        assertEquals(0, actual.size());
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

        parameters.add("value", new String[] { "jolira" });

        final String value = guicier.get(parameters, param, String.class, cleansed, false);

        assertEquals("jolira", value);
        assertEquals(1, cleansed.getNamedKeys().size());

        final List<StringValue> actual = cleansed.getValues("value");

        assertEquals(1, actual.size());
        assertEquals("jolira", actual.get(0).toString());
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

        parameters.add("company", new String[] { "1", "2", "3" });

        final String[] value = guicier.get(parameters, param, String[].class, cleansed, false);

        assertEquals(3, value.length);
        assertEquals("1", value[0]);
        assertEquals("2", value[1]);
        assertEquals("3", value[2]);
        assertEquals(1, cleansed.getNamedKeys().size());

        final List<StringValue> actual = cleansed.getValues("company");

        assertEquals(3, actual.size());
        assertEquals("1", actual.get(0).toString());
        assertEquals("2", actual.get(1).toString());
        assertEquals("3", actual.get(2).toString());
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
        final Object value = guicier.get(parameters, param, String.class, cleansed, false);

        assertNull(value);
        assertEquals(0, cleansed.getNamedKeys().size());
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

        parameters.add("company", "jolira");

        final Object value = guicier.get(parameters, param, String.class, cleansed, false);

        assertEquals("jolira", value);
        assertEquals(1, cleansed.getNamedKeys().size());
        assertEquals("jolira", cleansed.getValues("company").get(0).toString());
    }
}
