package com.redhat.lightblue.camel.request;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import com.redhat.lightblue.client.request.AbstractLightblueDataRequest;
import com.redhat.lightblue.client.request.DataBulkRequest;

/**
 * Aggregates a collection of {@link AbstractLightblueDataRequest}s into a single {@link DataBulkRequest}.
 *
 * @author dcrissman
 */
public class DataBulkRequestAggregationStrategy implements AggregationStrategy {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            oldExchange = new DefaultExchange(newExchange);
            oldExchange.getIn().setBody(new DataBulkRequest());
        }

        DataBulkRequest bulkRequest = oldExchange.getIn().getBody(DataBulkRequest.class);
        AbstractLightblueDataRequest request = newExchange.getIn().getBody(
                AbstractLightblueDataRequest.class);

        bulkRequest.add(request);

        return oldExchange;
    }

}
