package com.redhat.lightblue.camel.lock;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Predicate;

import com.redhat.lightblue.client.Locking;
import com.redhat.lightblue.client.response.LightblueException;

public class LightbluePingPredicate implements Predicate {

    private final Expression lockExpression;
    private final Expression resourceExpression;

    /**
     * @param lockExpression - {@link Expression} to obtain a Lightblue {@link Locking} instance.
     * @param resourceExpression - {@link Expression} for determining the resourceId for a given Exchange.
     */
    public LightbluePingPredicate(Expression lockExpression, Expression resourceExpression) {
        this.lockExpression = lockExpression;
        this.resourceExpression = resourceExpression;
    }

    @Override
    public boolean matches(Exchange exchange) {
        Locking lock = lockExpression.evaluate(exchange, Locking.class);
        String resourceID = resourceExpression.evaluate(exchange, String.class);

        //TODO NPEs

        try {
            return lock.ping(resourceID);
        } catch (LightblueException e) {
            //TODO handle
            throw new RuntimeException("Unable to ping lock for resource id: " + resourceID, e);
        }
    }

}
