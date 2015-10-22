package com.redhat.lightblue.camel;



import org.apache.camel.Exchange;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.redhat.lightblue.camel.exception.LightblueCamelProducerException;
import com.redhat.lightblue.client.response.LightblueException;

/**
 * Test for {@link SampleProducerRoute}.
 * 
 * @author mpatercz
 *
 */
public class ProducerExceptionTest extends AbstractProducerTest {

    public ProducerExceptionTest() throws Exception {
        super();
    }

    @Override
    public JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[] {};
    }

    @Test
    public void testMessageToLightblue() throws Exception {
        //setup asserts
        resultEndpoint.expectedMessageCount(0);
        exceptionEndpoint.expectedMessageCount(1);

        //Run test
        String message = Resources.toString(Resources.getResource("./data/user-message.xml"), Charsets.UTF_8);
        template.sendBody(message);

        //Verify asserts
        resultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();

        Exception e = exceptionEndpoint.getExchanges().get(0).getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        Assert.assertTrue(e instanceof LightblueCamelProducerException);
        Assert.assertTrue(e.getCause() instanceof LightblueException);
        Assert.assertTrue(e.getCause().getMessage().contains("Lightblue exception occurred"));
    }

}
