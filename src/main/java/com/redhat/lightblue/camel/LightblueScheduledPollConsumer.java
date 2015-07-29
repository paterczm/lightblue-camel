package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;

import com.redhat.lightblue.client.response.LightblueErrorResponseException;
import com.redhat.lightblue.client.response.LightblueResponse;

/**
 * Lightblue polling consumer.
 */
public class LightblueScheduledPollConsumer extends ScheduledPollConsumer {

    private final LightblueScheduledPollEndpoint endpoint;

    public LightblueScheduledPollConsumer(LightblueScheduledPollEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected int poll() throws Exception {
        Exchange exchange = endpoint.createExchange();
        LightblueResponse response = null;

        try {
            response = endpoint.getLightblueClient().data(endpoint.getLightbluePollingRequest());
            if (response.hasError()) {
                exchange.setException(new LightblueErrorResponseException("LightblueScheduledPollConsumer: returned in response: "
                        + response.getText()));
            } else {
                exchange.getIn().setBody(response);
            }
        } catch (Exception e) {
            exchange.setException(new LightblueErrorResponseException("LightblueScheduledPollConsumer: returned in response: "
                    + e.getMessage()));
        }

        try {
            // send message to next processor in the route, even if there was an exception, incase route has its own exception handling
            getProcessor().process(exchange);
        } finally {
            // log exception if an exception occurred and was not handled
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        }
        return (response == null) ? 0 : response.parseMatchCount();
    }
}
