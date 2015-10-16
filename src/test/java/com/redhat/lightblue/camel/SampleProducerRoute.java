package com.redhat.lightblue.camel;

import org.apache.camel.builder.RouteBuilder;

import com.redhat.lightblue.camel.model.User;
import com.redhat.lightblue.camel.request.LightblueInsertRequestFactory;
import com.redhat.lightblue.camel.utils.JacksonXmlDataFormat;

public class SampleProducerRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
            .to("mock:exception")
            .handled(true);

        from("direct:start")
            .unmarshal(new JacksonXmlDataFormat(User[].class))
            .bean(new LightblueInsertRequestFactory("user", "1.0.0"))
            .to("lightblue://inboundTest")
            .transform(simple("${body.getText}"))
            .to("mock:result");
    }
}
