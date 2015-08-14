package com.redhat.lightblue.camel.request;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.redhat.lightblue.client.expression.query.Query;
import com.redhat.lightblue.client.projection.FieldProjection;
import com.redhat.lightblue.client.projection.Projection;
import com.redhat.lightblue.client.request.LightblueRequest;

@Deprecated
public abstract class LightblueRequestFactory<R extends LightblueRequest> implements Processor {

    public static final String HEADER_ENTITY_NAME = "entityName";
    public static final String HEADER_ENTITY_VERSION = "entityVersion";
    public static final String HEADER_PROJECTIONS = "projections";
    public static final String HEADER_QUERY = "query";

    static final Projection[] DEFAULT_PROJECTIONS = new Projection[]{
        FieldProjection.includeFieldRecursively("*")
    };

    private Exchange exchange;
    private final String entityName;
    private final String entityVersion;

    public String getEntityName() {
        if (entityName == null) {
            return exchange.getIn().getHeader(HEADER_ENTITY_NAME, String.class);
        }
        return entityName;
    }

    public String getEntityVersion() {
        if (entityVersion == null) {
            return exchange.getIn().getHeader(HEADER_ENTITY_VERSION, String.class);
        }
        return entityVersion;
    }

    /**
     * Will return the set projection. If not set, then will return the default projection
     * that will return all fields recursively.
     * @return projections
     */
    public Projection[] getProjections() {
        if (exchange.getIn().getHeader(HEADER_PROJECTIONS) == null) {
            return DEFAULT_PROJECTIONS;
        }
        return exchange.getIn().getHeader(HEADER_PROJECTIONS, Projection[].class);
    }

    public Query getQuery() {
        return exchange.getIn().getHeader(HEADER_QUERY, Query.class);
    }

    public <T> T getBody(Class<T> type) {
        return exchange.getIn().getBody(type);
    }

    public LightblueRequestFactory() {
        this(null, null);
    }

    public LightblueRequestFactory(String entityName) {
        this(entityName, null);
    }

    public LightblueRequestFactory(String entityName, String entityVersion) {
        this.entityName = entityName;
        this.entityVersion = entityVersion;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        this.exchange = exchange;

        R request = createRequest(getEntityName(), getEntityVersion());
        exchange.getIn().setBody(request);
    }

    protected abstract R createRequest(String entityName, String entityVersion);

}
