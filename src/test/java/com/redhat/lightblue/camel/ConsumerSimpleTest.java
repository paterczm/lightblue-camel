package com.redhat.lightblue.camel;

import static com.redhat.lightblue.util.test.AbstractJsonNodeTest.loadJsonNode;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Test for {@link SampleConsumerRoute}.
 * 
 * @author mpatercz
 *
 */
public class ConsumerSimpleTest extends AbstractConsumerTest {

    public ConsumerSimpleTest() throws Exception {
        super();
    }

    @Override
    public JsonNode[] getMetadataJsonNodes() throws Exception {
        return new JsonNode[] { loadJsonNode("./metadata/event.json"), loadJsonNode("./metadata/user.json") };
    }

    @Test
    public void testMessageFromLightblue() throws Exception {
        //setup asserts
        userResultEndpoint.expectedMessageCount(1);
        userResultEndpoint.expectedBodiesReceived(
                "<Users xmlns=\"\"><item><firstName>Taylor</firstName><lastName>Swift</lastName><_id>1</_id></item></Users>");
        
        eventResultEndpoint.expectedMessageCount(1);
        eventResultEndpoint.expectedBodiesReceived(
                "<Events xmlns=\"\"><item><name>Something happened</name><processed>false</processed><_id>2</_id></item><item><name>Something else happened</name><processed>false</processed><_id>3</_id></item></Events>");

        //Run test
        // load events
        loadData("event", "1.0.0", "./data/events.json");
        loadData("user", "1.0.0", "./data/users.json");

        //Verify asserts
        userResultEndpoint.assertIsSatisfied();
        eventResultEndpoint.assertIsSatisfied();

    }

}
