package com.redhat.lightblue.camel;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

public class LightblueModule extends AbstractModule {

    private final LightblueRequestsHolder requests;

    public LightblueModule() {
        this(null);
    }

    public LightblueModule(LightblueRequestsHolder requestMap) {
        super();
        requests = requestMap;
    }

    @Override
    protected void configure() {
        bind(LightblueRequestsHolder.class).toProvider(new Provider<LightblueRequestsHolder>() {

            @Override
            public LightblueRequestsHolder get() {
                return requests;
            }

        });

    }

}
