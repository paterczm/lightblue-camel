package com.redhat.lightblue.camel.lock;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
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
import com.redhat.lightblue.client.LightblueClient;
import com.redhat.lightblue.client.Locking;

public class LightblueLockPolicyTest extends AbstractLightblueCamelTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected ProducerTemplate successfulLockTemplate;
    protected ProducerTemplate exceptionAfterLockTemplate;
    protected ProducerTemplate unableToAquireLockTemplate;

    protected MockEndpoint mockEndpoint;

    public LightblueLockPolicyTest() throws Exception {
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
        successfulLockTemplate = context.createProducerTemplate();
        successfulLockTemplate.setDefaultEndpointUri("direct:successfulLockTest");

        exceptionAfterLockTemplate = context.createProducerTemplate();
        exceptionAfterLockTemplate.setDefaultEndpointUri("direct:exceptionAfterLockTest");

        unableToAquireLockTemplate = context.createProducerTemplate();
        unableToAquireLockTemplate.setDefaultEndpointUri("direct:unableToAquireLockTest");

        mockEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
    }

    @Test
    public void successfulLockTest() throws Exception {
        successfulLockTemplate.sendBody("fake body");

        assertFalse(new RouteHelpers(getLightblueClient()).getLockingWithCalerId().ping("successfulLockTest"));
    }

    @Test
    public void exceptionAfterLockTest() throws Exception {
        exception.expect(CamelExecutionException.class);
        exception.expectCause(IsInstanceOf.<Throwable> instanceOf(RuntimeException.class));

        exceptionAfterLockTemplate.sendBody("fake body");

        assertFalse(new RouteHelpers(getLightblueClient()).getLockingWithCalerId().ping("exceptionAfterLockTest"));
    }

    @Test
    public void unableToAquireLockTest() {
        exception.expect(CamelExecutionException.class);
        exception.expectCause(IsInstanceOf.<Throwable> instanceOf(LightblueLockingException.class));

        unableToAquireLockTemplate.sendBody("fake body");
    }

    private static class SuiteModule extends AbstractModule {

        @Override
        protected void configure() {}

        @Provides
        Set<RoutesBuilder> routes(Injector injector) {
            Set<RoutesBuilder> set = new HashSet<RoutesBuilder>();
            set.add(new SuccessfulLockRouteBuilder());
            set.add(new ExceptionAfterLockRouteBuilder());
            set.add(new UnableToAquireLockRouteBuilder());
            return set;
        }

    }

    private static class SuccessfulLockRouteBuilder extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            from("direct:successfulLockTest")
                .policy(new LightblueLockPolicy(
                        method(RouteHelpers.class, "getLockingWithCalerId"),
                        constant("successfulLockTest")))
                .to("mock:result");
        }

    }

    private static class ExceptionAfterLockRouteBuilder extends RouteBuilder{
        @Override
        public void configure() throws Exception {
            from("direct:exceptionAfterLockTest")
                .policy(new LightblueLockPolicy(
                        method(RouteHelpers.class, "getLockingWithCalerId"),
                        constant("exceptionAfterLockTest")))
                .throwException(new RuntimeException("Fake Exception"))
                .to("mock:result");
        }
    }

    private static class UnableToAquireLockRouteBuilder extends RouteBuilder{
        private static final String RESOURCE = "unableToAquireLockTest";

        @Override
        public void configure() throws Exception {
            from("direct:unableToAquireLockTest")
                .policy(new LightblueLockPolicy(method(RouteHelpers.class, "getLocking"), constant(RESOURCE)))
                //The second lock attempt will fail because the first has it with a different callerId.
                .policy(new LightblueLockPolicy(method(RouteHelpers.class, "getLocking"), constant(RESOURCE)))
                .to("mock:result");
        }
    }

    public static class RouteHelpers {
        private static final String DOMAIN = "camelTestDomain";
        private static final String CALLER_ID = "fakeCallerId";

        @Inject
        private LightblueClient client;

        public RouteHelpers() {}

        public RouteHelpers(LightblueClient client) {
            this.client = client;
        }

        public Locking getLocking() {
            return client.getLocking(DOMAIN);
        }

        public Locking getLockingWithCalerId() {
            Locking lock = client.getLocking(DOMAIN);
            lock.setCallerId(CALLER_ID);

            return lock;
        }

    }

}
