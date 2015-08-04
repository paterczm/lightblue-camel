package com.redhat.lightblue.camel;

import org.apache.camel.builder.RouteBuilder;

import com.redhat.lightblue.camel.model.Event;
import com.redhat.lightblue.camel.model.User;
import com.redhat.lightblue.camel.utils.JacksonXmlDataFormat;
import com.redhat.lightblue.camel.utils.LightblueResponseTransformer;
import com.redhat.lightblue.client.response.LightblueException;

public class SampleConsumerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {

        onException(LightblueException.class)
            .transform(simple("${body.getText}"))
            .to("mock:exception")
            .handled(true);

        from("lightblue://eventPoller?delay=100000")
            .bean(new LightblueResponseTransformer<Event[]>(Event[].class))
            .marshal(new JacksonXmlDataFormat())
            .to("mock:eventResult");

        from("lightblue://userPoller?initialDelay=5&delay=10&timeUnit=SECONDS")
            .bean(new LightblueResponseTransformer<User[]>(User[].class))
            .marshal(new JacksonXmlDataFormat())
            .to("mock:userResult");
    }
}
