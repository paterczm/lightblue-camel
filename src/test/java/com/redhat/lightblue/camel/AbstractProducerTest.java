package com.redhat.lightblue.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.redhat.lightblue.client.integration.test.AbstractLightblueClientCRUDController;

/**
 * Test for {@link ProducerTestRoute}.
 * 
 * @author mpatercz
 *
 */
public abstract class AbstractProducerTest extends AbstractLightblueClientCRUDController {

    public AbstractProducerTest() throws Exception {
        super();
    }

    CamelContext context;
    ProducerTemplate template;
    MockEndpoint resultEndpoint, exceptionEndpoint;

    @Before
    public void setupCamel() throws Exception {
        // init guice and register the client
        Injector injector = Guice.createInjector(new TestCamelModule(getLightblueClient()));

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

}
