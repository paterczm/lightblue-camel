package com.redhat.lightblue.camel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.component.mock.MockEndpoint;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;

/**
 * Test for {@link SampleProducerRoute}.
 *
 * @author mpatercz
 *
 */
public abstract class AbstractProducerTest extends AbstractLightblueCamelTest {

    protected ProducerTemplate template;
    protected MockEndpoint resultEndpoint;
    protected MockEndpoint exceptionEndpoint;

    public AbstractProducerTest() throws Exception {
        super();
    }

    @Override
    protected List<Module> getModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(new LightblueModule());
        modules.add(new ProducerRouteModule());

        return modules;
    }

    @Override
    protected void doSuiteCamelSetup(final CamelContext context) {
        template = context.createProducerTemplate();
        template.setDefaultEndpointUri("direct:start");

        resultEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
        exceptionEndpoint = context.getEndpoint("mock:exception",
                MockEndpoint.class);
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
