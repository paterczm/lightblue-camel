package com.redhat.lightblue.camel;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for {@link SampleConsumerRoute}.
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
        return new JsonNode[]{/* Nothing! */};
    }

    @Test
    public void testMessageFromLightblue() throws Exception {
        //Setup asserts
        eventResultEndpoint.expectedMessageCount(0);

        userResultEndpoint.expectedMessageCount(0);

        exceptionEndpoint.expectedMessageCount(2);
        exceptionEndpoint.expectedBodiesReceivedInAnyOrder(
                "{\"status\":\"ERROR\",\"modifiedCount\":0,\"matchCount\":0,\"errors\":[{\"objectType\":\"error\",\"context\":\"rest/FindCommand/user/find(user:1.0.0)/getEntityMetadata(user:1.0.0)\",\"errorCode\":\"mongo-metadata:UnknownVersion\",\"msg\":\"user:1.0.0\"}]}",
                "{\"status\":\"ERROR\",\"modifiedCount\":0,\"matchCount\":0,\"errors\":[{\"objectType\":\"error\",\"context\":\"rest/FindCommand/event/find(event:1.0.0)/getEntityMetadata(event:1.0.0)\",\"errorCode\":\"mongo-metadata:UnknownVersion\",\"msg\":\"event:1.0.0\"}]}"
        );

        //Run tests
        //No need to do anything here, the poller will automatically run the queries defined in AbstractConsumerTest

        //Verify asserts
        eventResultEndpoint.assertIsSatisfied();
        userResultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();
    }

}
