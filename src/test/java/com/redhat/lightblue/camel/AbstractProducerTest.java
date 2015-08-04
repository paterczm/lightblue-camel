package com.redhat.lightblue.camel;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.guice.CamelModule;
import org.junit.After;
import org.junit.Before;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.redhat.lightblue.client.integration.test.AbstractLightblueClientCRUDController;

/**
 * Test for {@link SampleProducerRoute}.
 * 
 * @author mpatercz
 *
 */
public abstract class AbstractProducerTest extends AbstractLightblueClientCRUDController {

    private CamelContext context;
    protected ProducerTemplate template;
    protected MockEndpoint resultEndpoint;
    protected MockEndpoint exceptionEndpoint;

    public AbstractProducerTest() throws Exception {
        super();
    }

    @Before
    public void setupCamel() throws Exception {
        // init guice and register the client
        Injector injector = Guice.createInjector(
                new CamelModule(),
                new LightblueModule(getLightblueClient()),
                new ProducerRouteModule()
        );

        // init camel context
        context = injector.getInstance(CamelContext.class);

        // setup template
        template = context.createProducerTemplate();
        template.setDefaultEndpointUri("direct:start");

        resultEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);

        exceptionEndpoint = context.getEndpoint("mock:exception", MockEndpoint.class);

        // start camel
        context.start();
    }

    @After
    public void tearDownCamel() throws Exception {
        context.stop();
    }

    private static class ProducerRouteModule extends AbstractModule {

        @Override
        protected void configure() {}

        @Provides
        Set<RoutesBuilder> routes(Injector injector) {
            Set<RoutesBuilder> set = new HashSet<RoutesBuilder>();
            set.add(new SampleProducerRoute());
            return set;
        }

    }

}
