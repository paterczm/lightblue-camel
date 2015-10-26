package com.redhat.lightblue.camel.lock;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.Policy;
import org.apache.camel.spi.RouteContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.lightblue.client.Locking;
import com.redhat.lightblue.client.response.LightblueException;

/**
 * Creates a lock (aka. acquire) in lightblue for ONE of array elements (exchange body type is expected to be an array)
 * and then unlocks (aka. release) when finished. It tries to lock a random element - if that fails, it will try another
 * one until lock is successful or it runs out of elements. In latter case, {@link Processor} will be skipped over.
 *
 *
 * @author dcrissman, mpatercz
 */
public class LightblueLockPolicy<T> implements Policy {

    private static final Logger LOGGER = LoggerFactory.getLogger(LightblueLockPolicy.class);

    /** Header key that will contain the current lock name used. This can be useful for ping() operations if they are needed.  */
    public final static String HEADER_LOCK_RESOURCE_ID = "LOCK_RESOURCE_ID";

    private final Long ttl;
    private static final Random randomGenerator = new Random();
    private ResourceIdExtractor<T> resourceIdExtractor;
    private Locking locking;
    private String callerId = null;

    /**
     * Tells {@link LightblueLockPolicy} how to figure out resourceId to lock given element.
     *
     * @author mpatercz
     *
     * @param <T>
     */
    public static interface ResourceIdExtractor<T> {
        public String getResourceId(T resource);
    }

    public LightblueLockPolicy(ResourceIdExtractor<T> resourceIdExtractor, Locking locking) {
        this(resourceIdExtractor, locking, null);
    }

    public LightblueLockPolicy(ResourceIdExtractor<T> resourceIdExtractor, Locking locking, Long ttl) {
        this.ttl = ttl;
        this.locking = locking;
        this.resourceIdExtractor = resourceIdExtractor;
    }

    @Override
    public void beforeWrap(RouteContext routeContext, ProcessorDefinition<?> definition) {
        //Do Nothing!!
    }

    public Locking getLocking() {
        return locking;
    }

    /**
     * Use for testing only.
     *
     * @param callerId
     */
    void setCallerId(String callerId) {
        this.callerId = callerId;
        locking.setCallerId(callerId);
    }

    @Override
    public Processor wrap(final RouteContext routeContext, final Processor processor) {

        return new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                T[] elements;
                try {
                    elements = (T[])exchange.getIn().getBody();
                } catch (ClassCastException e1) {
                    throw new LightblueLockingException(e1);
                }

                if (elements == null)
                    throw new LightblueLockingException(new IllegalArgumentException("Expecting array"));

                if (elements.length == 0)
                    throw new LightblueLockingException(new IllegalArgumentException("Expecting non empty array"));

                if (callerId == null)
                    locking.setCallerId(URLEncoder.encode(routeContext.getRoute().getId()+"-"+Thread.currentThread().getId(), "UTF-8"));

                Pair<T, String> pair = tryToLock(elements);

                if (pair == null) {
                    LOGGER.debug("Could not aquire a lock. Skipping processing.");
                    return;
                }

                T lockedElement = pair.getLeft();
                String lockedResourceId = pair.getRight();

                exchange.getIn().setBody(lockedElement);

                try {
                    LOGGER.debug("Lock aquired, processing");
                    exchange.getIn().setHeader(HEADER_LOCK_RESOURCE_ID, lockedResourceId);
                    processor.process(exchange);
                    exchange.getIn().removeHeader(HEADER_LOCK_RESOURCE_ID);
                } finally {
                    try{
                        locking.release(lockedResourceId);
                        if (LOGGER.isDebugEnabled())
                            LOGGER.debug("Releasing lock on "+ java.net.URLDecoder.decode(lockedResourceId, "UTF-8"));
                    }
                    catch (Exception e) {
                        if (exchange.isFailed()) {
                            //Let the original exception bubble up, but log this one.
                            LOGGER.error("Unexpected error while the route is already in a failed state.", e);
                        } else {
                            throw new LightblueLockingException(e);
                        }
                    }
                }
            }
        };

    }

    /**
     * Tries to lock a random element recursively.
     *
     * @param elements
     * @return Locked element and resourceId used to lock or null if locking was not possible.
     */
    private Pair<T, String> tryToLock(T[] elements) {
        try {

            int index;

            if (elements.length > 1) {
                index = randomGenerator.nextInt(elements.length);
            } else {
                index = 0;
            }

            T element = elements[index];

            String resourceId = resourceIdExtractor.getResourceId(element);

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("About to aquire lock on "+ java.net.URLDecoder.decode(resourceId, "UTF-8"));

            if (locking.acquire(resourceId, ttl)) {
                return new ImmutablePair<T, String>(element, resourceId);
            } else if (elements.length > 1) {
                T[] newElements = ArrayUtils.remove(elements, index);
                return tryToLock(newElements);
            } else {
                LOGGER.warn("Was not able to lock any of the elements. Batch too small?");
                return null;
            }
        } catch (UnsupportedEncodingException | LightblueException e) {
            throw new LightblueLockingException(e);
        }
    }
}
