package com.google.code.joliratools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converters.IntegerConverter;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class GuicierPageFactoryTest {
    private static class StringConverter implements IConverter {
        private static final long serialVersionUID = 5221463436925128138L;

        @Override
        public Object convertToObject(final String value, final Locale locale) {
            return "converted:" + value;
        }

        @Override
        public String convertToString(final Object value, final Locale locale) {
            return value.toString();
        }
    }

    public static class TestPage0 extends WebPage {
        TestPage0(@SuppressWarnings("unused") final IConverter converter) {
            fail();
        }
    }

    public static class TestPage1 extends WebPage {
        // nothing
    }

    public static class TestPage2 extends WebPage {
        TestPage2(final PageParameters params) {
            assertNotNull(params);
        }
    }

    public static class TestPage3 extends WebPage {
        TestPage3(final PageParameters params) {
            assertEquals(1, params.size());
            assertEquals("jolira", params.get("company"));
        }
    }

    public static class TestPage4 extends WebPage {
        @Inject
        TestPage4(final IConverter converter, final PageParameters params) {
            assertEquals(1, params.size());
            assertEquals("jolira", params.get("company"));
            assertEquals(IntegerConverter.class, converter.getClass());
        }
    }

    public static class TestPage5 extends WebPage {
        @Inject
        TestPage5(final IConverter converter,
                @Parameter("company") final String company) {
            assertEquals("jolira", company);
            assertEquals(IntegerConverter.class, converter.getClass());
        }
    }

    public static class TestPage6 extends WebPage {
        @Inject
        TestPage6(@Parameter("offset") final int offset) {
            assertEquals(15, offset);
        }
    }

    public static class TestPage7 extends WebPage {
        @Inject
        TestPage7(
                @SuppressWarnings("unused") @Parameter(value = "value1", optional = true) final int value1,
                @SuppressWarnings("unused") @Parameter(value = "value2", optional = false) final int value2) {
            fail();
        }

        @Inject
        TestPage7(
                @SuppressWarnings("unused") @Parameter("offset") final int offset,
                @Parameter("offset") final long offset2,
                @Parameter("offset") final Long offset3,
                @Parameter("offset") final Integer offset4,
                @Parameter("offset") final short offset5,
                @Parameter("offset") final Short offset6,
                @Parameter("offset") final float offset7,
                @Parameter("offset") final Float offset8,
                @Parameter("offset") final Double offset9,
                @Parameter("offset") final double offset10,
                @Parameter("offset") final BigDecimal offset11,
                @Parameter("success") final boolean success1,
                @Parameter("success") final Boolean success2,
                @Parameter(value = "offset", converter = StringConverter.class) final String offset12,
                @Parameter("offset") final byte offset13,
                @Parameter("offset") final Byte offset14) {
            assertEquals(15, offset2);
            assertEquals(Long.valueOf(15), offset3);
            assertEquals(Integer.valueOf(15), offset4);
            assertEquals(Short.valueOf((short) 15), offset6);
            assertEquals(15, offset5);
            assertEquals(Float.valueOf(15), offset8);
            assertEquals(Float.valueOf(15), Float.valueOf(offset7));
            assertEquals(Double.valueOf(15), offset9);
            assertEquals(Double.valueOf(15), Double.valueOf(offset10));
            assertEquals(BigDecimal.valueOf(15), offset11);
            assertEquals(Boolean.TRUE, success2);
            assertEquals(Boolean.TRUE, Boolean.valueOf(success1));
            assertEquals("converted:15", offset12);
            assertEquals(Byte.valueOf((byte) 15), offset14);
            assertEquals(Byte.valueOf((byte) 15), Byte.valueOf(offset13));
        }
    }

    public static class TestPage8 extends WebPage {
        @Inject
        TestPage8(
                @Named("jfk") @SuppressWarnings("unused") final IConverter converter,
                @Parameter("company") final String company) {
            assertEquals("jolira", company);
        }
    }

    public static class TestPage9 extends WebPage {
        @Inject
        TestPage9(
                @Named("jfk") @SuppressWarnings("unused") final IConverter converter,
                @SuppressWarnings("unused") @Parameter("company") final Map<String, String> company) {
            fail();
        }
    }

    @SuppressWarnings("unused")
    private WicketTester tester;

    @Before
    public void setUp() {
        tester = new WicketTester();
    }

    @After
    public void teardown() {
        tester = null;
    }

    @Test
    public void testInjectedAndOneIntParameter() {
        final Injector injector = Guice.createInjector();
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final PageParameters params = new PageParameters();

        params.put("offset", "15");

        final Page page = factory.newPage(TestPage6.class, params);

        assertNotNull(page);
    }

    @Test
    public void testInjectedAndOneIntParameterMultiAnno() {
        final Injector injector = Guice.createInjector();
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final PageParameters params = new PageParameters();

        params.put("offset", "15");
        params.put("success", "true");

        final Page page = factory.newPage(TestPage7.class, params);

        assertNotNull(page);
    }

    @Test(expected = WicketRuntimeException.class)
    public void testInjectedAndOneInvalidTypeParameter() {
        final Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(IConverter.class).annotatedWith(Names.named("jfk"))
                        .to(IntegerConverter.class);
            }
        });
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final PageParameters params = new PageParameters();

        params.put("company", "jolira");

        factory.newPage(TestPage9.class, params);
    }

    @Test
    public void testInjectedAndOneParameter() {
        final Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(IConverter.class).to(IntegerConverter.class);
            }
        });
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final PageParameters params = new PageParameters();

        params.put("company", "jolira");

        final Page page = factory.newPage(TestPage5.class, params);

        assertNotNull(page);
    }

    public void testInjectedAndOneParameterMultiAno() {
        final Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(IConverter.class).to(IntegerConverter.class);
            }
        });
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final PageParameters params = new PageParameters();

        params.put("company", "jolira");

        final Page page = factory.newPage(TestPage8.class, params);

        assertNotNull(page);
    }

    @Test
    public void testInjectedAndPageParameters() {
        final Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(final Binder binder) {
                binder.bind(IConverter.class).to(IntegerConverter.class);
            }
        });
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final PageParameters params = new PageParameters();

        params.put("company", "jolira");

        final Page page = factory.newPage(TestPage4.class, params);

        assertNotNull(page);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidArgument() {
        new GuicierPageFactory(null);
    }

    @Test(expected = WicketRuntimeException.class)
    public void testNoSuitableConstructor() {
        final Injector injector = Guice.createInjector();
        final GuicierPageFactory factory = new GuicierPageFactory(injector);

        factory.newPage(TestPage0.class);
    }

    @Test
    public void testPageNoParameters() {
        final Injector injector = Guice.createInjector();
        final GuicierPageFactory factory = new GuicierPageFactory(injector);

        assertNotNull(factory.newPage(TestPage1.class));
        assertNotNull(factory.newPage(TestPage1.class));
    }

    @Test
    public void testPageParameters() {
        final Injector injector = Guice.createInjector();
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final PageParameters params = new PageParameters();

        params.put("company", "jolira");

        final Page page = factory.newPage(TestPage3.class, params);

        assertNotNull(page);
    }

    @Test
    public void testPageParametersWithNull() {
        final Injector injector = Guice.createInjector();
        final GuicierPageFactory factory = new GuicierPageFactory(injector);
        final Page page = factory.newPage(TestPage2.class);

        assertNotNull(page);
    }
}
