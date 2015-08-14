package com.redhat.lightblue.camel.utils;

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
 *
 *
 * @author dcrissman
 */
public class LightblueLockPolicy implements Policy {

    public final static String LOCK_RESOURCE_ID = "LOCK_RESOURCE_ID";

    private final Locking lock;
    private final Expression expression;

    /**
     * @param lock - Lightblue {@link Locking}
     * @param expression - {@link Expression} for determining the resourceId for a given Exchange.
     */
    public LightblueLockPolicy(Locking lock, Expression expression) {
        this.lock = lock;
        this.expression = expression;
    }

    @Override
    public void beforeWrap(RouteContext routeContext, ProcessorDefinition<?> definition) {
        //Do Nothing!!
    }

    @Override
    public Processor wrap(final RouteContext routeContext, final Processor processor) {
        final ExpressionDefinition red = ExpressionNodeHelper.toExpressionDefinition(expression);
        final Expression routeExpression = red.createExpression(routeContext);

        return new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                final String resourceId = routeExpression.evaluate(exchange, String.class);
                routeContext.getRoute().setHeader(LOCK_RESOURCE_ID, new ConstantExpression(resourceId));

                if (lock.acquire(resourceId)) {
                    processor.process(exchange);
                    lock.release(resourceId);
                }
            }
        };

    }

}
