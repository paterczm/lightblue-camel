package com.redhat.lightblue.camel;

import java.util.HashSet;
import java.util.Set;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.guice.CamelModule;

import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.redhat.lightblue.client.LightblueClient;

public class TestCamelModule extends CamelModule {

    private final LightblueClient client;
    private final LightblueRequestsHolder requests;

    public TestCamelModule(LightblueClient client) {
        this(client, null);
    }

    public TestCamelModule(LightblueClient client, LightblueRequestsHolder requestMap) {
        super();
        this.client = client;
        this.requests = requestMap;
    }

    @Override
    protected void configure() {
        super.configure();

        bind(LightblueClient.class).toInstance(client);
        bind(LightblueRequestsHolder.class).toProvider(new Provider<LightblueRequestsHolder>() {

            @Override
            public LightblueRequestsHolder get() {
                return requests;
            }

        });

    }
    @Provides
    Set<RoutesBuilder> routes(Injector injector) {
        Set<RoutesBuilder> set = new HashSet<RoutesBuilder>();
        set.add(new ProducerTestRoute());
        set.add(new ConsumerTestRoute());
        return set;
    }

}
