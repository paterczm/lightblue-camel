package com.redhat.lightblue.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public class FailureHandler implements Processor {

    public void process(Exchange exchange) throws Exception {
        // just a demo of how to fetch root cause
        Throwable cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        exchange.getContext().createProducerTemplate().send("mock:exception", exchange);
    }

}
