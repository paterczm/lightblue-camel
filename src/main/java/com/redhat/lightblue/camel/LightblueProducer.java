package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

import com.redhat.lightblue.client.request.AbstractLightblueDataRequest;
import com.redhat.lightblue.client.request.LightblueRequest;
import com.redhat.lightblue.client.response.LightblueErrorResponseException;
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
            if (response.hasError()) {
                exchange.setException(new LightblueErrorResponseException("LightblueProducer:Error returned in response: "
                        + response.getText()));
            } else {
                exchange.getOut().setBody(response);
            }
        } catch (Exception e) {
            /*
             * wrap all run time exceptions into LightblueErrorResponseException so routes are shielded from client errors and have to
             * handle only one type
             */
            exchange.setException(new LightblueErrorResponseException(e));
        }
    }

}
