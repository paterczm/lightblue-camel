package com.redhat.lightblue.camel;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Test for {@link ProducerTestRoute}.
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
        String message = Resources.toString(Resources.getResource("./data/user-message.xml"), Charsets.UTF_8);
        template.sendBody(message);

        resultEndpoint.expectedMinimumMessageCount(0);
        resultEndpoint.assertIsSatisfied();
        exceptionEndpoint.expectedMinimumMessageCount(1);
        exceptionEndpoint.assertIsSatisfied();

    }

}
