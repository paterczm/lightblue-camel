package com.redhat.lightblue.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;

import com.redhat.lightblue.client.LightblueClient;
import com.redhat.lightblue.client.request.LightblueRequest;

/**
 * Lightblue endpoint.
 */
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
        return new LightblueScheduledPollConsumer(this, processor);
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
