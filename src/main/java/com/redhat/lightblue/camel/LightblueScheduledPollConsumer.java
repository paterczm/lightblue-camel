package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;

import com.redhat.lightblue.camel.exception.LightblueCamelConsumerException;
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
            if (response.parseMatchCount() <= 0) {
                return 0;
            }
            exchange.getIn().setBody(response);
        } catch (Exception e) {
            // routing didn't start yet, so we can't expect camel to handle this error
            // set exception on exchange and pass it to camel
            exchange.setException(new LightblueCamelConsumerException(e));
        }

        //  In case route has its own exception handling send message to next processor in the route.
        getProcessor().process(exchange);

        return (response == null) ? 0 : response.parseMatchCount();
    }
}
