package com.redhat.lightblue.camel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.redhat.lightblue.client.Projection;
import com.redhat.lightblue.client.Query;
import com.redhat.lightblue.client.request.data.DataFindRequest;

/**
 * Test for {@link SampleConsumerRoute}.
 *
 * @author mpatercz
 *
 */
public abstract class AbstractConsumerTest extends AbstractLightblueCamelTest {

    protected MockEndpoint eventResultEndpoint;
    protected MockEndpoint userResultEndpoint;
    protected MockEndpoint exceptionEndpoint;

    public AbstractConsumerTest() throws Exception {
        super();
    }

    @Override
    protected List<Module> getModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(new LightblueModule(createLightblueRequestsHolder()));
        modules.add(new ConsumerRouteModule());

        return modules;
    }

    @Override
    protected void doSuiteCamelSetup(final CamelContext context) {
        userResultEndpoint = context.getEndpoint("mock:userResult", MockEndpoint.class);
        eventResultEndpoint = context.getEndpoint("mock:eventResult", MockEndpoint.class);
        exceptionEndpoint = context.getEndpoint("mock:exception", MockEndpoint.class);
    }

    protected LightblueRequestsHolder createLightblueRequestsHolder() {
        // polling request
        LightblueRequestsHolder requestMap = new LightblueRequestsHolder();
        DataFindRequest eventFindRequest = new DataFindRequest("event", "1.0.0");
        eventFindRequest.where(Query.withValue("processed = false"));
        eventFindRequest.select(Projection.includeFieldRecursively("*"));
        requestMap.put("eventPoller", eventFindRequest);
        DataFindRequest userFindRequest = new DataFindRequest("user", "1.0.0");
        userFindRequest.where(Query.withValue("firstName = Taylor"));
        userFindRequest.select(Projection.includeFieldRecursively("*"));
        requestMap.put("userPoller", userFindRequest);

        return requestMap;
    }

    private static class ConsumerRouteModule extends AbstractModule {

        @Override
        protected void configure() {}

        @Provides
        Set<RoutesBuilder> routes(Injector injector) {
            Set<RoutesBuilder> set = new HashSet<RoutesBuilder>();
            set.add(new SampleConsumerRoute());
            return set;
        }

    }

}
