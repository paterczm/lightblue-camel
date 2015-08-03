package com.redhat.lightblue.camel;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.redhat.lightblue.client.LightblueClient;

public class LightblueModule extends AbstractModule {

    private final LightblueClient client;
    private final LightblueRequestsHolder requests;

    public LightblueModule(LightblueClient client) {
        this(client, null);
    }

    public LightblueModule(LightblueClient client, LightblueRequestsHolder requestMap) {
        super();
        this.client = client;
        this.requests = requestMap;
    }

    @Override
    protected void configure() {
        bind(LightblueClient.class).toInstance(client);
        bind(LightblueRequestsHolder.class).toProvider(new Provider<LightblueRequestsHolder>() {

            @Override
            public LightblueRequestsHolder get() {
                return requests;
            }

        });

    }

}
