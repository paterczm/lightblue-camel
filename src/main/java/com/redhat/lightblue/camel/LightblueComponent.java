package com.redhat.lightblue.camel;

import java.util.Map;

import javax.annotation.Nullable;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

import com.google.inject.Inject;
import com.redhat.lightblue.client.LightblueClient;

/**
 * Represents the component that manages {@link LightblueScheduledPollEndpoint}.
 */
public class LightblueComponent extends DefaultComponent {

    private LightblueClient lightblueClient;
    private LightblueRequestsHolder lightblueRequests;

    public LightblueComponent() {
        super();
    }

    public LightblueComponent(CamelContext context) {
        super(context);
    }

    @Override
    /*
     * @param uri the full URI of the endpoint
     * 
     * @param remaining the remaining part of the URI without the query parameters or component prefix, used to identify the lightblue
     * request. since there is only one consumer right now, its okay to use remaining clause for identifying the request. if needed, we can
     * use a uri param
     * 
     * @param parameters the optional parameters passed in
     * 
     * @return a newly created endpoint or null if the endpoint cannot be created based on the inputs
     * 
     * @throws Exception is thrown if error creating the endpoint
     * 
     * 
     * @see org.apache.camel.impl.DefaultComponent#createEndpoint(java.lang.String, java.lang.String, java.util.Map)
     */
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        LightblueScheduledPollEndpoint endpoint = new LightblueScheduledPollEndpoint(uri, this);

        endpoint.setLightblueClient(lightblueClient);
        if (lightblueRequests != null) {
            endpoint.setLightbluePollingRequest(lightblueRequests.get(remaining));
        }
        setProperties(endpoint, parameters);
        return endpoint;
    }

    @Inject
    public void setLightblueClient(LightblueClient lightblueClient) {
        this.lightblueClient = lightblueClient;
    }

    @Inject
    public void setLightbluePollingRequests(@Nullable LightblueRequestsHolder lightblueRequests) {
        this.lightblueRequests = lightblueRequests;
    }

}
