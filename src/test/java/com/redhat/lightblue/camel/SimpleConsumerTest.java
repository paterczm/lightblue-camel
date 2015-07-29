package com.redhat.lightblue.camel;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for {@link ConsumerTestRoute}.
 * 
 * @author mpatercz
 *
 */
public class SimpleConsumerTest extends AbstractConsumerTest {

    public SimpleConsumerTest() throws Exception {
        super();
    }

    @Override
    public JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[] { loadJsonNode("./metadata/event.json"), loadJsonNode("./metadata/user.json") };
    }

    @Test
    public void testMessageFromLightblue() throws Exception {
        // load events
        loadData("event", "1.0.0", "./data/events.json");
        loadData("user", "1.0.0", "./data/users.json");

        userResultEndpoint
                .expectedBodiesReceived("<Users xmlns=\"\"><item><firstName>Taylor</firstName><lastName>Swift</lastName></item></Users>");
        eventResultEndpoint
                .expectedBodiesReceived("<Events xmlns=\"\"><item><name>Something happened</name><processed>false</processed><_id>2</_id></item><item><name>Something else happened</name><processed>false</processed><_id>3</_id></item></Events>");
        userResultEndpoint.assertIsSatisfied();
        eventResultEndpoint.assertIsSatisfied();

    }

}
