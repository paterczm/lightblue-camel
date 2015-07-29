package com.redhat.lightblue.camel;

import org.apache.camel.builder.RouteBuilder;

import com.redhat.lightblue.camel.model.User;
import com.redhat.lightblue.camel.request.LightblueInsertRequestFactory;
import com.redhat.lightblue.camel.utils.JacksonXmlDataFormat;
import com.redhat.lightblue.client.response.LightblueErrorResponseException;

public class ProducerTestRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(LightblueErrorResponseException.class)
            .bean(new FailureHandler())
            .handled(true);

        from("direct:start")
            .unmarshal(new JacksonXmlDataFormat(User[].class))
            .bean(new LightblueInsertRequestFactory("user", "1.0.0"))
            .to("lightblue://inboundTest")
            .to("mock:result");
    }
}
