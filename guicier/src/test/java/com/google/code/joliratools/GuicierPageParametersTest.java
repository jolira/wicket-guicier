/**
 * 
 */
package com.google.code.joliratools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.convert.IConverter;
import org.junit.Test;

import com.google.code.joliratools.GuicierPageParameters.StringConverter;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author jfk
 * 
 */
public class GuicierPageParametersTest {
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

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test
    public void testGetPageParameters() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();

        parameters.put("company", "jolira");

        final PageParameters value = params.getValue(parameters, param,
                PageParameters.class);

        assertEquals(parameters, value);
    }

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test(expected = WicketRuntimeException.class)
    public void testNoConverter() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();

        parameters.put("company", "jolira");

        final Object value = params.getValue(parameters, param, Map.class);

        assertSame(parameters, value);
    }

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test
    public void testSpecificConverter() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("company", false,
                StringConverter.class);
        final PageParameters parameters = new PageParameters();

        parameters.put("company", "jolira");

        final Object value = params.getValue(parameters, param, String.class);

        assertEquals("jolira", value);
    }

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test
    public void testStringArray0GetValue() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();

        parameters.put("company", new String[0]);

        final Object value = params.getValue(parameters, param, String.class);

        assertNull(value);
    }

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test
    public void testStringArray1GetValue() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("value");
        final PageParameters parameters = new PageParameters();

        parameters.put("value", new String[] { "jolira" });

        final String value = params.getValue(parameters, param, String.class);

        assertEquals("jolira", value);
    }

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test
    public void testStringArray3GetValue() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();

        parameters.put("company", new String[] { "1", "2", "3" });

        final String[] value = params.getValue(parameters, param,
                String[].class);

        assertEquals(3, value.length);
        assertEquals("1", value[0]);
        assertEquals("2", value[1]);
        assertEquals("3", value[2]);
    }

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test
    public void testStringGetNullValue() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();
        final Object value = params.getValue(parameters, param, String.class);

        assertNull(value);
    }

    /**
     * Test method for
     * {@link GuicierPageParameters#getValue(PageParameters, Parameter, Class)}.
     */
    @Test
    public void testStringGetValue() {
        final Injector injector = Guice.createInjector();
        final GuicierPageParameters params = injector
                .getInstance(GuicierPageParameters.class);
        final Parameter param = new ParameterMock("company");
        final PageParameters parameters = new PageParameters();

        parameters.put("company", "jolira");

        final Object value = params.getValue(parameters, param, String.class);

        assertEquals("jolira", value);
    }
}
