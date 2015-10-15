package com.redhat.lightblue.camel;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for {@link SampleConsumerRoute}.
 *
 * @author mpatercz
 *
 */
public class ConsumerNoLightblueResponseTest extends AbstractConsumerTest {

    public ConsumerNoLightblueResponseTest() throws Exception {
        super();
    }

    @Override
    public JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[]{/* Nothing! */};
    }

    @Test
    public void testMessageFromLightblue() throws Exception {
        stopHttpServer();

        //Setup asserts
        eventResultEndpoint.expectedMessageCount(0);

        userResultEndpoint.expectedMessageCount(0);

        // SampleConsumerRoute is consuming from 2 endpoints
        exceptionEndpoint.expectedMessageCount(2);
        exceptionEndpoint.expectedBodiesReceivedInAnyOrder("java.net.ConnectException: Connection refused",
                "java.net.ConnectException: Connection refused");

        //Verify asserts
        eventResultEndpoint.assertIsSatisfied();
        userResultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();
    }

}
