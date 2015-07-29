package com.redhat.lightblue.camel;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for {@link ConsumerTestRoute}.
 * 
 * @author mpatercz
 *
 */
public class ConsumerExceptionTest extends AbstractConsumerTest {

    public ConsumerExceptionTest() throws Exception {
        super();
    }

    @Override
    public JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[] {};
    }

    @Test
    public void testMessageFromLightblue() throws Exception {
        // load events
        userResultEndpoint.expectedMinimumMessageCount(0);
        eventResultEndpoint.expectedMinimumMessageCount(0);
        userResultEndpoint.assertIsSatisfied();
        eventResultEndpoint.assertIsSatisfied();
        exceptionEndpoint.expectedMinimumMessageCount(2);
        exceptionEndpoint.assertIsSatisfied();
    }

}
