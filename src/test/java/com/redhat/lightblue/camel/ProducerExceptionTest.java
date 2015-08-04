package com.redhat.lightblue.camel;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

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
        exceptionEndpoint.expectedBodiesReceived(
                "{\"status\":\"ERROR\",\"modifiedCount\":0,\"matchCount\":0,\"errors\":[{\"objectType\":\"error\",\"context\":\"rest/InsertCommand/user/insert(user:1.0.0)/getEntityMetadata(user:1.0.0)\",\"errorCode\":\"mongo-metadata:UnknownVersion\",\"msg\":\"user:1.0.0\"}]}");

        //Run test
        String message = Resources.toString(Resources.getResource("./data/user-message.xml"), Charsets.UTF_8);
        template.sendBody(message);

        //Verify asserts
        resultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();

    }

}
