package com.redhat.lightblue.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.After;
import org.junit.Before;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.redhat.lightblue.client.expression.query.ValueQuery;
import com.redhat.lightblue.client.integration.test.AbstractLightblueClientCRUDController;
import com.redhat.lightblue.client.projection.FieldProjection;
import com.redhat.lightblue.client.request.data.DataFindRequest;

/**
 * Test for {@link ConsumerTestRoute}.
 * 
 * @author mpatercz
 *
 */
public abstract class AbstractConsumerTest extends AbstractLightblueClientCRUDController {

    public AbstractConsumerTest() throws Exception {
        super();
    }

    CamelContext context;
    MockEndpoint eventResultEndpoint, userResultEndpoint, exceptionEndpoint;

    @Before
    public void setupCamel() throws Exception {
        // polling request
        LightblueRequestsHolder requestMap = new LightblueRequestsHolder();
        DataFindRequest eventFindRequest = new DataFindRequest("event", "1.0.0");
        eventFindRequest.where(ValueQuery.withValue("processed = false"));
        eventFindRequest.select(FieldProjection.includeFieldRecursively("*"));
        requestMap.put("eventPoller", eventFindRequest);
        DataFindRequest userFindRequest = new DataFindRequest("user", "1.0.0");
        userFindRequest.where(ValueQuery.withValue("firstName = Taylor"));
        userFindRequest.select(FieldProjection.includeFieldRecursively("*"));
        requestMap.put("userPoller", userFindRequest);

        // init guice and register the client and polling request
        Injector injector = Guice.createInjector(new TestCamelModule(getLightblueClient(), requestMap));

        // init camel context
        context = injector.getInstance(CamelContext.class);

        userResultEndpoint = context.getEndpoint("mock:userResult", MockEndpoint.class);
        eventResultEndpoint = context.getEndpoint("mock:eventResult", MockEndpoint.class);
        exceptionEndpoint = context.getEndpoint("mock:exception", MockEndpoint.class);

        // start camel
        context.start();
    }

    @After
    public void tearDownCamel() throws Exception {
        context.stop();
    }

}
