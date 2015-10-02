package com.redhat.lightblue.camel.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Before;
import org.junit.Test;

import com.redhat.lightblue.client.request.DataBulkRequest;
import com.redhat.lightblue.client.request.data.DataInsertRequest;

public class TestDataBulkRequestAggregationStrategy {

    private CamelContext context;

    @Before
    public void before() {
        context = new DefaultCamelContext();
    }

    @Test
    public void testSingleRequest() {
        DefaultExchange newExchange = new DefaultExchange(context);
        newExchange.getIn().setBody(new DataInsertRequest("fake", "0.0.0"));

        Exchange aggregatedExchange = new DataBulkRequestAggregationStrategy().aggregate(null, newExchange);
        assertNotNull(aggregatedExchange);

        DataBulkRequest bulkRequest = aggregatedExchange.getIn().getBody(DataBulkRequest.class);
        assertNotNull(bulkRequest);
        assertEquals(1, bulkRequest.getRequests().size());
    }

    @Test
    public void testMultipleRequests() {
        DataBulkRequestAggregationStrategy aggregator = new DataBulkRequestAggregationStrategy();

        DefaultExchange newExchange1 = new DefaultExchange(context);
        newExchange1.getIn().setBody(new DataInsertRequest("fake", "0.0.1"));

        Exchange aggregatedExchange1 = aggregator.aggregate(null, newExchange1);
        assertNotNull(aggregatedExchange1);

        DefaultExchange newExchange2 = new DefaultExchange(context);
        newExchange2.getIn().setBody(new DataInsertRequest("fake2", "0.0.2"));
        Exchange aggregatedExchange2 = aggregator.aggregate(aggregatedExchange1, newExchange2);
        assertNotNull(aggregatedExchange2);
        
        DataBulkRequest bulkRequest = aggregatedExchange2.getIn().getBody(DataBulkRequest.class);
        assertNotNull(bulkRequest);
        assertEquals(2, bulkRequest.getRequests().size());
    }

}
