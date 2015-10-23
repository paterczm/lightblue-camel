package com.redhat.lightblue.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.spi.UriEndpoint;

import com.redhat.lightblue.client.LightblueClient;
import com.redhat.lightblue.client.request.LightblueRequest;

/**
 * Lightblue endpoint.
 */
@UriEndpoint(scheme = "lightblue", title = "Lightblue", syntax = "lightblue:name", consumerClass = LightblueScheduledPollConsumer.class, label = "Lightblue")
public class LightblueScheduledPollEndpoint extends ScheduledPollEndpoint {

    private LightblueClient lightblueClient;

    private LightblueRequest lightbluePollingRequest;

    public LightblueScheduledPollEndpoint() {
    }

    public LightblueScheduledPollEndpoint(String uri, LightblueComponent component) {
        super(uri, component);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new LightblueProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        LightblueScheduledPollConsumer consumer = new LightblueScheduledPollConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    // @Inject
    public void setLightblueClient(LightblueClient lightblueClient) {
        this.lightblueClient = lightblueClient;
    }

    // @Inject
    public void setLightbluePollingRequest(LightblueRequest lightblueRequest) {
        this.lightbluePollingRequest = lightblueRequest;
    }

    public LightblueRequest getLightbluePollingRequest() {
        return lightbluePollingRequest;
    }

    public LightblueClient getLightblueClient() {
        return lightblueClient;
    }

}
