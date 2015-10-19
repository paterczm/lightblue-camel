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

        //Run tests
        //No need to do anything here, the poller will automatically run the queries defined in AbstractConsumerTest

        //Verify asserts
        eventResultEndpoint.assertIsSatisfied();
        userResultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();

        Exception e1 = exceptionEndpoint.getExchanges().get(0).getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Assert.assertTrue(e1 instanceof LightblueCamelConsumerException);
        Assert.assertTrue(e1.getCause() instanceof LightblueException);
        Assert.assertTrue(e1.getCause().getMessage().contains("Lightblue exception occurred"));

        Exception e2 = exceptionEndpoint.getExchanges().get(1).getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Assert.assertTrue(e2 instanceof LightblueCamelConsumerException);
        Assert.assertTrue(e2.getCause() instanceof LightblueException);
        Assert.assertTrue(e2.getCause().getMessage().contains("Lightblue exception occurred"));
    }

}
