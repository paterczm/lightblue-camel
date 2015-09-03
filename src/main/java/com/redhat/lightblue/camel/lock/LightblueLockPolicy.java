package com.redhat.lightblue.camel.lock;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.model.ExpressionNodeHelper;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.language.ConstantExpression;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.spi.Policy;
import org.apache.camel.spi.RouteContext;

import com.redhat.lightblue.client.Locking;

/**
 * Creates a lock (aka. acquire) in lightblue for each Exchange and then unlocks (aka. release) when finished.
 * If a lock cannot be acquired, then {@link Processor} will be skipped over.
 *
 * @author dcrissman
 */
public class LightblueLockPolicy implements Policy {

    /** Header key that will contain the current lock name used. This can be useful for ping() operations if they are needed.  */
    public final static String HEADER_LOCK_RESOURCE_ID = "LOCK_RESOURCE_ID";

    private final Expression lockExpression;
    private final Expression resourceExpression;
    private final Long ttl;

    /**
     * Uses the default ttl.
     * @param lockExpression - {@link Expression} to obtain a Lightblue {@link Locking} instance.
     * @param resourceExpression - {@link Expression} for determining the resourceId for a given Exchange.
     */
    public LightblueLockPolicy(Expression lockExpression, Expression resourceExpression) {
        this(lockExpression, resourceExpression, null);
    }

    /**
     *
     * @param lockExpression - {@link Expression} to obtain a Lightblue {@link Locking} instance.
     * @param resourceExpression - {@link Expression} for determining the resourceId for a given Exchange.
     * @param ttl - time to live for the lock
     */
    public LightblueLockPolicy(Expression lockExpression, Expression resourceExpression, Long ttl) {
        this.lockExpression = lockExpression;
        this.resourceExpression = resourceExpression;
        this.ttl = ttl;
    }

    @Override
    public void beforeWrap(RouteContext routeContext, ProcessorDefinition<?> definition) {
        //Do Nothing!!
    }

    @Override
    public Processor wrap(final RouteContext routeContext, final Processor processor) {
        final Expression routeLockExpression = createRouteExpression(routeContext, lockExpression);
        final Expression routeResourceExpression = createRouteExpression(routeContext, resourceExpression);

        return new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                final Locking lock = routeLockExpression.evaluate(exchange, Locking.class);
                final String resourceId = routeResourceExpression.evaluate(exchange, String.class);

                //TODO NPEs

                routeContext.getRoute().setHeader(HEADER_LOCK_RESOURCE_ID, new ConstantExpression(resourceId));

                if (lock.acquire(resourceId, ttl)) {
                    try {
                        processor.process(exchange);
                    } finally {
                        lock.release(resourceId);
                    }
                }
                else{
                    throw new LightblueLockingException("Unable to acquire a lock for: " + resourceId);
                }

                routeContext.getRoute().removeHeader(HEADER_LOCK_RESOURCE_ID);
            }
        };

    }

    private Expression createRouteExpression(final RouteContext routeContext, final Expression expression){
        ExpressionDefinition red = ExpressionNodeHelper.toExpressionDefinition(expression);
        return red.createExpression(routeContext);
    }

}
