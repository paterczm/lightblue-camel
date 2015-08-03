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
import com.redhat.lightblue.client.expression.query.ValueQuery;
import com.redhat.lightblue.client.projection.FieldProjection;
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
        resultEndpoint.expectedBodiesReceived(
                "{\"status\":\"COMPLETE\",\"modifiedCount\":2,\"matchCount\":0,\"processed\":[{\"lastName\":\"Smith\",\"_id\":\"1\",\"firstName\":\"John\",\"objectType\":\"user\"},{\"lastName\":\"Smith\",\"_id\":\"2\",\"firstName\":\"Jane\",\"objectType\":\"user\"}]}");

        exceptionEndpoint.expectedMessageCount(0);

        //Run tests
        String message = Resources.toString(Resources.getResource("./data/user-message.xml"), Charsets.UTF_8);
        template.sendBody(message);

        //Verify asserts
        DataFindRequest findRequest = new DataFindRequest("user", null);
        findRequest.where(ValueQuery.withValue("objectType = user"));
        findRequest.select(FieldProjection.includeField("*"));
        User[] users = getLightblueClient().data(findRequest, User[].class);

        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.length);

        resultEndpoint.assertIsSatisfied();
        exceptionEndpoint.assertIsSatisfied();
    }

}
