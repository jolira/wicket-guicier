package com.jolira.guicier;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.eclipse.jetty.ajp.Ajp13SocketConnector;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolira.testing.TestUtils;

/**
 * Quick & dirty starter for the war
 */
public class Starter {
    static class DemoWebAppContext extends WebAppContext {
        public void _doStart() throws Exception {
            super.doStart();
        }

        public void _doStop() throws Exception {
            super.doStop();
        }
    }

    private static final String CONTEXT = "context";
    private static final String AJP13_PORT = "ajp13";
    private static final String HTTP_PORT = "http";
    private static final String STANDALONE = "standalone";

    private static final String TARGET_TEST_CLASSES = "/target/test-classes";
    private static final String TARGET_CLASSES = "/target/classes";

    final static Logger LOG = LoggerFactory.getLogger(Starter.class);

    private static void addURL(final Collection<URL> result, final String directory) {
        final File dir = new File(directory);

        if (!dir.isDirectory()) {
            return;
        }

        final URL url = toURL(dir);

        LOG.info("adding " + url + " to the classpath");
        result.add(url);
    }

    private static URL[] getExtendedURLs() {
        final String classpath = System.getProperty("java.class.path");
        final Collection<URL> result = new ArrayList<URL>();
        final StringTokenizer izer = new StringTokenizer(classpath, ":");

        while (izer.hasMoreTokens()) {
            final String token = izer.nextToken();

            if (token.endsWith(TARGET_TEST_CLASSES)) {
                final int tokenLen = token.length();
                final int postfixLen = TARGET_TEST_CLASSES.length();
                final String base = token.substring(0, tokenLen - postfixLen);

                addURL(result, base + "/src/test/resources");
            } else if (token.endsWith(TARGET_CLASSES)) {
                final int tokenLen = token.length();
                final int postfixLen = TARGET_CLASSES.length();
                final String base = token.substring(0, tokenLen - postfixLen);

                addURL(result, base + "/src/main/resources");
            }
        }

        final int size = result.size();

        return result.toArray(new URL[size]);
    }

    private static void logSettings() {
        final Map<String, String> env = System.getenv();
        final Properties properties = System.getProperties();

        LOG.info("Environment: " + env);
        LOG.info("Properties: " + properties);
    }

    /**
     * Start up the server.
     * 
     * @param args
     *            not used
     * @throws Exception
     *             something failed
     */
    public static void main(final String[] args) throws Exception {
        // create the command line parser
        final CommandLineParser parser = new PosixParser();

        // create the Options
        final Options options = new Options();

        options.addOption(
                "x",
                STANDALONE,
                false,
                "run the server in standalone mode; sets the ajp13 port to 8010, "
                        + "set the http-port to 8081, the https-port to 8083, and the context name to \"/\". This mode is intended "
                        + "for users who want to operate the server in standalone mode, without having to set-up httpd");
        options.addOption("h", HTTP_PORT, true, "the http port to listen on");
        options.addOption("a", AJP13_PORT, true, "the ajp13 port to listen on");
        options.addOption("c", CONTEXT, true, "the name of the context to use (such as \"/m\"");
        options.addOption("?", "help", false, "display usage information");

        final CommandLine line = parser.parse(options, args);

        if (line.hasOption("help")) {
            final HelpFormatter formatter = new HelpFormatter();

            formatter.printHelp("java " + Starter.class.getName(), options, true);
            return;
        }

        int httpPort = -1;
        int ajp13Port = 8010;
        String context = "/";

        if (line.hasOption(STANDALONE)) {
            httpPort = 8081;
            ajp13Port = -1;
        }

        if (line.hasOption(HTTP_PORT)) {
            final String value = line.getOptionValue(HTTP_PORT);

            httpPort = Integer.parseInt(value);
        }

        if (line.hasOption(AJP13_PORT)) {
            final String value = line.getOptionValue(AJP13_PORT);

            ajp13Port = Integer.parseInt(value);
        }

        if (line.hasOption(CONTEXT)) {
            context = line.getOptionValue(CONTEXT);
        }

        final Starter starter = start(httpPort, ajp13Port, context);

        starter.join();
    }

    /**
     * Start an instance.
     * 
     * @param httpPort
     * @param ajp13Port
     * @param context
     * @return the starter instance
     * @throws Exception
     * @throws InterruptedException
     */
    public static Starter start(final int httpPort, final int ajp13Port, final String context)
            throws Exception, InterruptedException {
        logSettings();

        final ClassLoader cl = updateClassPath();

        return new Starter(cl, httpPort, ajp13Port, context);
    }

    private static URL toURL(final File dir) {
        final URI uri = dir.toURI();

        try {
            return uri.toURL();
        } catch (final MalformedURLException e) {
            throw new Error(e);
        }
    }

    private static ClassLoader updateClassPath() {
        final Thread currentThread = Thread.currentThread();
        final ClassLoader currentThreadClassLoader = currentThread.getContextClassLoader();
        final URL[] urls = getExtendedURLs();

        if (urls.length == 0) {
            return currentThreadClassLoader;
        }

        final URLClassLoader urlClassLoader = new URLClassLoader(urls, currentThreadClassLoader);

        currentThread.setContextClassLoader(urlClassLoader);

        return urlClassLoader;
    }

    private final Server server;

    private Starter(final ClassLoader cl, final int httpPort, final int ajp13Port,
            final String ctx) throws Exception {
        final Connector[] _connectors = getConnectors(httpPort, ajp13Port);

        server = new Server();
        server.setConnectors(_connectors);

        final HandlerCollection hc = new HandlerCollection();
        final ContextHandlerCollection handlers = new ContextHandlerCollection();
        final DemoWebAppContext context = createDemoContext(cl, ctx);

        hc.setHandlers(new Handler[] { handlers });
        handlers.addHandler(context);

        server.setHandler(hc);

        server.start();
    }

    private DemoWebAppContext createDemoContext(final ClassLoader cl, final String ctx) {
        final DemoWebAppContext context = new DemoWebAppContext();
        final File baseDir = TestUtils.getBaseDir(Starter.class);
        final File webappDir = new File(baseDir, "src/main/webapp");
        final File webxml = new File(webappDir, "WEB-INF/web.xml");
        final String resourceBase = webappDir.getAbsolutePath();
        final String descriptor = webxml.getAbsolutePath();

        context.setClassLoader(cl);
        context.setContextPath(ctx);
        context.setDescriptor(descriptor);
        context.setResourceBase(resourceBase);
        context.setServer(server);
        return context;
    }

    private final Connector[] getConnectors(final int httpPort, final int ajp13Port) {
        final Collection<Connector> connectors = new ArrayList<Connector>(2);

        if (httpPort > 0) {
            final AbstractConnector httpConnector = new SelectChannelConnector();

            httpConnector.setPort(httpPort);
            connectors.add(httpConnector);
        }

        if (ajp13Port > 0) {
            final AbstractConnector ajp13Connector = new Ajp13SocketConnector();

            ajp13Connector.setPort(ajp13Port);
            connectors.add(ajp13Connector);
        }

        final int size = connectors.size();

        return connectors.toArray(new Connector[size]);
    }

    /**
     * Blocks until the server is stopped.
     * 
     * @throws InterruptedException
     */
    private void join() throws InterruptedException {
        server.join();
    }

    /**
     * Stop the server.
     * 
     * @throws Exception
     */
    public void stop() throws Exception {
        server.stop();
    }
}