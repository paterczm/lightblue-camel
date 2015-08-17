package com.redhat.lightblue.camel.lock;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;

import com.redhat.lightblue.client.Locking;
import com.redhat.lightblue.client.response.LightblueException;

public class LightbluePingPredicate implements Predicate {

    private final Locking lock;
    private final Expression expression;

    public LightbluePingPredicate(Locking lock, Expression expression) {
        this.lock = lock;
        this.expression = expression;
    }

    @Override
    public boolean matches(Exchange exchange) {
        String resourceID = expression.evaluate(exchange, String.class);

        try {
            return lock.ping(resourceID);
        } catch (LightblueException e) {
            //TODO handle
            throw new RuntimeException("Unable to ping lock for resource id: " + resourceID, e);
        }
    }

}
