package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.lightblue.camel.exception.LightblueCamelConsumerException;
import com.redhat.lightblue.client.response.LightblueException;

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
//        exceptionEndpoint.expectedBodiesReceivedInAnyOrder("java.net.ConnectException: Connection refused",
//                "java.net.ConnectException: Connection refused");

        //Verify asserts
        eventResultEndpoint.assertIsSatisfied();
        userResultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();

        Exception e1 = exceptionEndpoint.getExchanges().get(0).getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Assert.assertTrue(e1 instanceof LightblueCamelConsumerException);
        Assert.assertTrue(e1.getCause() instanceof LightblueException);
        Assert.assertEquals("java.net.ConnectException: Connection refused",
                e1.getCause().getCause().toString());

        Exception e2 = exceptionEndpoint.getExchanges().get(1).getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Assert.assertTrue(e2 instanceof LightblueCamelConsumerException);
        Assert.assertTrue(e2.getCause() instanceof LightblueException);
        Assert.assertEquals("java.net.ConnectException: Connection refused",
                e2.getCause().getCause().toString());

    }

}
