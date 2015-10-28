package com.redhat.lightblue.camel.lock;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.Policy;
import org.apache.camel.spi.RouteContext;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
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

    /** Property key that will contain the current lock name used. This can be useful for ping() operations if they are needed.  */
    public final static String PROPERTY_LOCK_RESOURCE_ID = "LOCK_RESOURCE_ID";

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
                    locking.setCallerId(URLEncoder.encode(routeContext.getRoute().getId()+"-"+UUID.randomUUID(), "UTF-8"));

                Pair<T, String> pair = tryToLock(Lists.newArrayList(elements));

                if (pair == null) {
                    LOGGER.debug("Could not aquire a lock. Skipping processing.");
                    return;
                }

                T lockedElement = pair.getLeft();
                String lockedResourceId = pair.getRight();

                exchange.getIn().setBody(lockedElement);

                try {
                    LOGGER.debug("Lock aquired, processing");
                    exchange.setProperty(PROPERTY_LOCK_RESOURCE_ID, lockedResourceId);
                    processor.process(exchange);
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
     * Tries to lock a random element from list provided.
     *
     * @param elements
     * @return Locked element and resourceId used to lock or null if locking was not possible.
     */
    private Pair<T, String> tryToLock(List<T> elements) {
        try {

            Set<String> cantLockResourceIds = new HashSet<String>();

            while(true) {

                int index;

                if (elements.size() > 1) {
                    index = randomGenerator.nextInt(elements.size());
                } else {
                    index = 0;
                }

                T element = elements.get(index);

                String resourceId = resourceIdExtractor.getResourceId(element);

                if (LOGGER.isDebugEnabled())
                    LOGGER.debug("About to aquire lock on "+ java.net.URLDecoder.decode(resourceId, "UTF-8"));

                if (cantLockResourceIds.contains(resourceId)) {
                    LOGGER.debug("Skipping resourceId={} because it was not available for locking previously", resourceId);
                    if (elements.size() > 1) {
                        elements.remove(index);
                        continue;
                    } else {
                        LOGGER.warn("Was not able to lock any of the elements. Batch too small?");
                        return null;
                    }
                }

                if (locking.acquire(resourceId, ttl)) {
                    return new ImmutablePair<T, String>(element, resourceId);
                } else if (elements.size() > 1) {
                    cantLockResourceIds.add(resourceId);
                    elements.remove(index);
                } else {
                    LOGGER.warn("Was not able to lock any of the elements. Batch too small?");
                    return null;
                }
            }
        } catch (UnsupportedEncodingException | LightblueException e) {
            throw new LightblueLockingException(e);
        }
    }
}
