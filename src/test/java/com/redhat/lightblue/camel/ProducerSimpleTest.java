package com.redhat.lightblue.camel;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;

import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.redhat.lightblue.camel.model.User;
import com.redhat.lightblue.client.Projection;
import com.redhat.lightblue.client.Query;
import com.redhat.lightblue.client.request.data.DataFindRequest;

/**
 * Test for {@link SampleProducerRoute}.
 *
 * @author mpatercz
 *
 */
public class ProducerSimpleTest extends AbstractProducerTest {

    public ProducerSimpleTest() throws Exception {
        super();
    }

    @Override
    public JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[] {
                loadJsonNode("./metadata/user.json")
        };
    }

    @Before
    public void cleanupLightblueCollections() throws UnknownHostException {
        cleanupMongoCollections("user");
    }

    @Test
    public void testMessageToLightblue() throws Exception {
        //setup asserts
        resultEndpoint.expectedMessageCount(1);
        //cannot verify the string value of the body as elements move.

        exceptionEndpoint.expectedMessageCount(0);

        //Run tests
        String message = Resources.toString(Resources.getResource("./data/user-message.xml"), Charsets.UTF_8);
        template.sendBody(message);

        //Verify asserts
        DataFindRequest findRequest = new DataFindRequest("user", null);
        findRequest.where(Query.withValue("objectType = user"));
        findRequest.select(Projection.includeField("*"));
        User[] users = getLightblueClient().data(findRequest, User[].class);

        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.length);

        resultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();
    }

}
