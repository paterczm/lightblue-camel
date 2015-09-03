package com.redhat.lightblue.camel.lock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Handler;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.redhat.lightblue.camel.AbstractLightblueCamelTest;

public class LightbluePingPredicateTest extends AbstractLightblueCamelTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected ProducerTemplate successfulPingTemplate;
    protected ProducerTemplate differentCallerPingTemplate;
    protected ProducerTemplate exceptionDuringPingTemplate;

    protected MockEndpoint mockEndpoint;

    public LightbluePingPredicateTest() throws Exception {
        super();
    }

    @Override
    protected JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[]{};
    }

    @Override
    protected List<Module> getModules() {
        List<Module> modules = new ArrayList<>();
        modules.add(new SuiteModule());

        return modules;
    }

    @Override
    protected void doSuiteCamelSetup(CamelContext context) {
        successfulPingTemplate = context.createProducerTemplate();
        successfulPingTemplate.setDefaultEndpointUri("direct:successfulPingTest");

        differentCallerPingTemplate = context.createProducerTemplate();
        differentCallerPingTemplate.setDefaultEndpointUri("direct:differentCallerPingTest");

        exceptionDuringPingTemplate = context.createProducerTemplate();
        exceptionDuringPingTemplate.setDefaultEndpointUri("direct:exceptionDuringPingTest");

        mockEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
    }

    @Test
    public void successfulPingTest() throws Exception {
        mockEndpoint.expectedBodiesReceived("fake body");

        successfulPingTemplate.sendBody("fake body");

        mockEndpoint.assertIsSatisfied();
    }

    @Test
    public void exceptionAfterLockTest() throws Exception {
        mockEndpoint.expectedBodiesReceived("fake body");

        differentCallerPingTemplate.sendBody("fake body");

        mockEndpoint.assertIsSatisfied();
    }

    @Test
    public void exceptionDuringPingTest() {
        exception.expect(CamelExecutionException.class);
        exception.expectCause(IsInstanceOf.<Throwable> instanceOf(LightblueLockingException.class));

        exceptionDuringPingTemplate.sendBody("fake body");
    }

    private class SuiteModule extends AbstractModule {

        @Override
        protected void configure() {}

        @Provides
        Set<RoutesBuilder> routes(Injector injector) {
            Set<RoutesBuilder> set = new HashSet<RoutesBuilder>();
            set.add(new SuccessfulPingRouteBuilder());
            set.add(new LockHeldByDifferentCallerPingRouteBuilder());
            set.add(new ExceptionDuringPingRouteBuilder());

            return set;
        }

    }

    private static class SuccessfulPingRouteBuilder extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            from("direct:successfulPingTest")
                .policy(new LightblueLockPolicy(
                        method(LockRouteHelpers.class, "getLockingWithCalerId"),
                        constant("successfulLockTest")))
                .choice()
                    .when(new LightbluePingPredicate(
                            method(LockRouteHelpers.class, "getLockingWithCalerId"),
                            header(LightblueLockPolicy.HEADER_LOCK_RESOURCE_ID)))
                        .to("mock:result")
                    .otherwise().throwException(new RuntimeException("LightbluePingPredicate evaluated to false and should not have"))
                .end();
        }

    }

    private static class LockHeldByDifferentCallerPingRouteBuilder extends RouteBuilder {

        /**
         * getLocking will have a different callerId each time it is called
         */
        @Override
        public void configure() throws Exception {
            from("direct:differentCallerPingTest")
                .policy(new LightblueLockPolicy(
                        method(LockRouteHelpers.class, "getLocking"),
                        constant("differentCallerPingTest")))
                .choice()
                    .when(new LightbluePingPredicate(
                            method(LockRouteHelpers.class, "getLocking"),
                            header(LightblueLockPolicy.HEADER_LOCK_RESOURCE_ID)))
                        .throwException(new RuntimeException("LightbluePingPredicate evaluated to true and should not have"))
                    .otherwise().to("mock:result")
                .end();
        }

    }

    private static class ExceptionDuringPingRouteBuilder extends RouteBuilder {

        /**
         * getLocking will have a different callerId each time it is called
         */
        @Override
        public void configure() throws Exception {
            from("direct:exceptionDuringPingTest")
                .policy(new LightblueLockPolicy(
                        method(LockRouteHelpers.class, "getLockingWithCalerId"),
                        constant("exceptionDuringPingTest")))
                .bean(StopLightblueServer.class)
                .filter(new LightbluePingPredicate(
                        method(LockRouteHelpers.class, "getLockingWithCalerId"),
                        header(LightblueLockPolicy.HEADER_LOCK_RESOURCE_ID)))
                    .throwException(new RuntimeException("LightbluePingPredicate evaluated to true and should thrown a LightblueLockingException"))
                .end();
        }

        public static class StopLightblueServer {

            @Handler
            public void stop() {
                stopHttpServer();
            }

        }

    }

}
