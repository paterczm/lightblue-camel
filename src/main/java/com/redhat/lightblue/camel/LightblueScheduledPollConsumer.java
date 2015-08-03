package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;

import com.redhat.lightblue.client.response.LightblueException;
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
            exchange.getIn().setBody(response);
        } catch (LightblueException e) {
            exchange.getIn().setBody(e.getLightblueResponse());
            exchange.setException(e);
        } catch (Exception e) {
            exchange.setException(
                    new Exception("Unexpected exception", e));
        }

        //  In case route has its own exception handling send message to next processor in the route.
        getProcessor().process(exchange);

        return (response == null) ? 0 : response.parseMatchCount();
    }
}
