package com.redhat.lightblue.camel.response;

import org.apache.camel.Handler;

import com.redhat.lightblue.client.response.LightblueResponse;

/**
 * Converts @{link {@link LightblueResponse} to POJOs.
 *
 * @param <T> type to convert to
 *
 * @author dcrissman
 */
public class LightblueResponseTransformer<T> {

    private final Class<T> type;

    public LightblueResponseTransformer(Class<T> type) {
        this.type = type;
    }

    @Handler
    public T transform(LightblueResponse body) throws Exception {
        return body.parseProcessed(type);
    }

}
