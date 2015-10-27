package com.redhat.lightblue.camel.lock;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.redhat.lightblue.camel.AbstractLightblueCamelTest;
import com.redhat.lightblue.camel.lock.LightblueLockPolicy.ResourceIdExtractor;
import com.redhat.lightblue.client.Locking;

public class LightblueLockPolicyTest extends AbstractLightblueCamelTest {

    public static final String DOMAIN = "camelTestDomain";
    public static final String CALLER_ID = "fakeCallerId";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected ProducerTemplate testTemplate;
    protected ProducerTemplate exceptionAfterLockTemplate;
    protected ProducerTemplate duplicatesTemplate;

    protected MockEndpoint mockEndpoint;

    Locking testLocking;

    ResourceIdExtractor<String> stringIdExtractor = new ResourceIdExtractor<String>() {

        @Override
        public String getResourceId(String resource) {
            return resource;
        }
    };

    LightblueLockPolicy<String> lightblueLockPolicy =
            new LightblueLockPolicy<String>(stringIdExtractor, getLightblueClient().getLocking(DOMAIN));


    public LightblueLockPolicyTest() throws Exception {
        super();

        lightblueLockPolicy.setCallerId(CALLER_ID);

        testLocking = getLightblueClient().getLocking(DOMAIN);
        testLocking.setCallerId("TEST");
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

        testTemplate = context.createProducerTemplate();
        testTemplate.setDefaultEndpointUri("direct:lockTest");

        exceptionAfterLockTemplate = context.createProducerTemplate();
        exceptionAfterLockTemplate.setDefaultEndpointUri("direct:exceptionAfterLockTest");

        duplicatesTemplate = context.createProducerTemplate();
        duplicatesTemplate.setDefaultEndpointUri("direct:duplicatesTest");


        mockEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
    }

    @Test
    public void successfulLockTest() throws Exception {
        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.expectedBodiesReceived("fakebody");

        testTemplate.sendBody(new String[] {"fakebody"});

        assertFalse(lightblueLockPolicy.getLocking().ping("fakebody"));

        mockEndpoint.assertIsSatisfied();
    }

    @Test
    public void successfulLockObjectBatchTest() throws Exception {
        Assert.assertTrue(testLocking.acquire("a"));
        Assert.assertTrue(testLocking.acquire("b"));

        mockEndpoint.expectedMessageCount(1);
        mockEndpoint.expectedBodiesReceived("c");

        testTemplate.sendBody(new String[]{"a", "b", "c"});

        assertFalse(lightblueLockPolicy.getLocking().ping("c"));

        mockEndpoint.assertIsSatisfied();
    }

    @Test
    public void exceptionAfterLockTest() throws Exception {
        exception.expect(CamelExecutionException.class);
        exception.expectCause(IsInstanceOf.<Throwable> instanceOf(RuntimeException.class));

        exceptionAfterLockTemplate.sendBody(new String[] {"fakebody"});

        assertFalse(lightblueLockPolicy.getLocking().ping("fakebody"));
    }

    @Test
    public void unableToAquireLockTest() throws Exception {
        Assert.assertTrue(testLocking.acquire("fakebody"));

        mockEndpoint.expectedMessageCount(0);

        testTemplate.sendBody(new String[] {"fakebody"});

        mockEndpoint.assertIsSatisfied();
    }

    @Test
    public void duplicatesTest() throws Exception {
        mockEndpoint.expectedMessageCount(0);

        // false means lock already taken
        Mockito.when(duplicatesLocking.acquire(Mockito.anyString(), Mockito.anyLong())).thenReturn(false);

        duplicatesTemplate.sendBody(new String[] {"a", "a", "a", "a", "a"});

        mockEndpoint.assertIsSatisfied();

        // confirm there was only one attempt to lock, because all elements are the same
        Mockito.verify(duplicatesLocking, Mockito.times(1)).acquire(Mockito.anyString(), Mockito.anyLong());
    }

    class SuiteModule extends AbstractModule {

        @Override
        protected void configure() {}

        @Provides
        Set<RoutesBuilder> routes(Injector injector) {
            Set<RoutesBuilder> set = new HashSet<RoutesBuilder>();
            set.add(new TestRouteBuilder());
            set.add(new ExceptionAfterLockRouteBuilder());
            set.add(new DuplicatesRouteBuilder());
            return set;
        }

    }


    private class TestRouteBuilder extends RouteBuilder {

        @Override
        public void configure() throws Exception {
            from("direct:lockTest")
                .policy(lightblueLockPolicy)
                .to("mock:result");
        }

    }

    private class ExceptionAfterLockRouteBuilder extends RouteBuilder{
        @Override
        public void configure() throws Exception {
            from("direct:exceptionAfterLockTest")
                .policy(lightblueLockPolicy)
                .throwException(new RuntimeException("Fake Exception"))
                .to("mock:result");
        }
    }

    Locking duplicatesLocking = Mockito.mock(Locking.class);

    private class DuplicatesRouteBuilder extends RouteBuilder{
        @Override
        public void configure() throws Exception {
            from("direct:duplicatesTest")
                .policy(new LightblueLockPolicy<String>(stringIdExtractor, duplicatesLocking))
                .to("mock:result");
        }
    }
}
