package com.redhat.lightblue.camel;

import java.util.ArrayList;
import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.guice.CamelModule;
import org.junit.After;
import org.junit.Before;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.redhat.lightblue.client.LightblueClient;
import com.redhat.lightblue.client.integration.test.AbstractLightblueClientCRUDController;

/**
 * Contains everything needed to run a camel test using guice and lightblue. The implementation
 * will need to include a guice {@link Module} that provides any {@link org.apache.camel.builder.RouteBuilder}s
 * needed.
 *
 * @author dcrissman
 */
public abstract class AbstractLightblueCamelTest extends AbstractLightblueClientCRUDController {

    private CamelContext context;

    public AbstractLightblueCamelTest() throws Exception {
        super();
    }

    public AbstractLightblueCamelTest(int httpServerPort) throws Exception {
        super(httpServerPort);
    }

    @Before
    public void setupCamel() throws Exception {
        List<Module> modules = new ArrayList<>();
        modules.add(new CamelModule());
        modules.add(new LightblueClientModule());
        modules.addAll(getModules());

        // init guice and register the client
        Injector injector = Guice.createInjector(modules);

        // init camel context
        context = injector.getInstance(CamelContext.class);

        doSuiteCamelSetup(context);

        // start camel
        context.start();
    }

    @After
    public void tearDownCamel() throws Exception {
        if (context != null) {
            context.stop();
        }
    }

    /**
     * @return all the guice {@link Module}s needed for this test suite.
     */
    protected abstract List<Module> getModules();

    /**
     * Any specific setup for this test suite that requires the {@link CamelContext}.
     * For example, any MockEndpoints you might need.
     * @param context - {@link CamelContext}
     */
    protected abstract void doSuiteCamelSetup(final CamelContext context);

    private class LightblueClientModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(LightblueClient.class).toInstance(getLightblueClient());
        }

    }

}
