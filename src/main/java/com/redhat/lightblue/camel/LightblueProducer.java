package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;

import com.redhat.lightblue.client.request.AbstractDataBulkRequest;
import com.redhat.lightblue.client.request.AbstractLightblueDataRequest;
import com.redhat.lightblue.client.request.AbstractLightblueMetadataRequest;
import com.redhat.lightblue.client.request.LightblueRequest;
import com.redhat.lightblue.client.response.LightblueException;

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
        LightblueRequest req = exchange.getIn().getBody(LightblueRequest.class);
        if (req == null) {
            throw new IllegalArgumentException("Unable to find an instance of LightblueRequest on the exchange in.");
        }

        try {
            Object response = sendRequest(req);
            exchange.getIn().setBody(response);
        } catch (LightblueException e) {
            exchange.getIn().setBody(e.getLightblueResponse());
            exchange.setException(e);
        } catch (Exception e) {
            exchange.setException(
                    new Exception("Unexpected exception", e));
        }
    }

    /*
     * TODO I would prefer this method return a LightblueResponse instead of Object,
     * but bulkData has a different parent hierarchy.
     */
    private Object sendRequest(LightblueRequest request) throws LightblueException {
        if (request instanceof AbstractLightblueDataRequest) {
            return endpoint.getLightblueClient().data(request);
        }
        else if (request instanceof AbstractDataBulkRequest){
            @SuppressWarnings({"rawtypes", "unchecked"})
            AbstractDataBulkRequest<AbstractLightblueDataRequest> bulkRequest = (AbstractDataBulkRequest) request;
            return endpoint.getLightblueClient().bulkData(bulkRequest);
        }
        else if (request instanceof AbstractLightblueMetadataRequest){
            return endpoint.getLightblueClient().metadata(request);
        }
        else{
            throw new IllegalArgumentException("Unknown LightblueRequest type: " + request.getClass().getName());
        }
    }

}
