package com.redhat.lightblue.camel;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.guice.CamelModule;
import org.junit.After;
import org.junit.Before;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
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

    private CamelContext context;
    protected MockEndpoint eventResultEndpoint;
    protected MockEndpoint userResultEndpoint;
    protected MockEndpoint exceptionEndpoint;

    public AbstractConsumerTest() throws Exception {
        super();
    }

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
        Injector injector = Guice.createInjector(
                new CamelModule(),
                new LightblueModule(getLightblueClient(), requestMap),
                new ConsumerRouteModule()
        );

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

    private static class ConsumerRouteModule extends AbstractModule {

        @Override
        protected void configure() {}

        @Provides
        Set<RoutesBuilder> routes(Injector injector) {
            Set<RoutesBuilder> set = new HashSet<RoutesBuilder>();
            set.add(new ConsumerTestRoute());
            return set;
        }

    }

}
