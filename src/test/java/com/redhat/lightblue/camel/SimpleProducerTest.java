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
 * Test for {@link ProducerTestRoute}.
 * 
 * @author mpatercz
 *
 */
public class SimpleProducerTest extends AbstractProducerTest {

    public SimpleProducerTest() throws Exception {
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
        String message = Resources.toString(Resources.getResource("./data/user-message.xml"), Charsets.UTF_8);
        template.sendBody(message);

        DataFindRequest findRequest = new DataFindRequest("user", null);
        findRequest.where(ValueQuery.withValue("objectType = user"));
        findRequest.select(FieldProjection.includeField("*"));
        User[] users = getLightblueClient().data(findRequest, User[].class);

        Assert.assertNotNull(users);
        Assert.assertEquals(2, users.length);

        resultEndpoint.expectedMinimumMessageCount(1);
        resultEndpoint.assertIsSatisfied();

        exceptionEndpoint.expectedMessageCount(0);
        exceptionEndpoint.assertIsSatisfied();
    }

}
