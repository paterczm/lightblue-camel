package com.redhat.lightblue.camel.lock;

import javax.inject.Inject;

import com.redhat.lightblue.client.LightblueClient;
import com.redhat.lightblue.client.Locking;

public class LockRouteHelpers {

    public static final String DOMAIN = "camelTestDomain";
    public static final String CALLER_ID = "fakeCallerId";

    @Inject
    private LightblueClient client;

    public LockRouteHelpers() {}

    public LockRouteHelpers(LightblueClient client) {
        this.client = client;
    }

    public Locking getLocking() {
        return client.getLocking(DOMAIN);
    }

    public Locking getLockingWithCalerId() {
        Locking lock = client.getLocking(DOMAIN);
        lock.setCallerId(CALLER_ID);

        return lock;
    }

}
