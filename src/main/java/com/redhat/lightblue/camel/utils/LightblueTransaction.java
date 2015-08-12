package com.redhat.lightblue.camel.utils;

import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.model.ExpressionNodeHelper;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.spi.TransactedPolicy;

import com.redhat.lightblue.client.Locking;

public class LightblueTransaction implements TransactedPolicy {

    private final Locking lock;
    private final Expression expression;

    public LightblueTransaction(Locking lock, Expression expression) {
        this.lock = lock;
        this.expression = expression;
    }

    @Override
    public void beforeWrap(RouteContext routeContext, ProcessorDefinition<?> definition) {
        //Do Nothing!!
    }

    @Override
    public Processor wrap(final RouteContext routeContext, final Processor processor) {
        ExpressionDefinition red = ExpressionNodeHelper.toExpressionDefinition(expression);
        final Expression routeExpression = red.createExpression(routeContext);

        return new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String resourceId = routeExpression.evaluate(exchange, String.class);
                lock.acquire(resourceId);
                processor.process(exchange);
                lock.release(resourceId);
            }
        };

    }

}
