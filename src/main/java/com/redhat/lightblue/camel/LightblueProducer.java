package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

import com.redhat.lightblue.client.request.AbstractLightblueDataRequest;
import com.redhat.lightblue.client.request.LightblueRequest;
import com.redhat.lightblue.client.response.LightblueException;
import com.redhat.lightblue.client.response.LightblueResponse;

/**
 * The Lightblue producer.
 */
public class LightblueProducer extends DefaultProducer {

    private final LightblueScheduledPollEndpoint endpoint;

    public LightblueProducer(LightblueScheduledPollEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        LightblueRequest req = (AbstractLightblueDataRequest) exchange.getIn().getBody();

        try {
            LightblueResponse response = endpoint.getLightblueClient().data(req);
            exchange.getIn().setBody(response);
        } catch (LightblueException e) {
            exchange.getIn().setBody(e.getLightblueResponse());
            exchange.setException(e);
        } catch (Exception e) {
            exchange.setException(
                    new Exception("Unexpected exception", e));
        }
    }

}
