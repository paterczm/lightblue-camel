package com.redhat.lightblue.camel.request;

import com.redhat.lightblue.client.expression.update.Update;
import com.redhat.lightblue.client.request.data.DataUpdateRequest;

/**
 * Converts {@link Update} instances into a lightblue update request.
 */
@Deprecated
public class LightblueUpdateRequestFactory extends LightblueRequestFactory<DataUpdateRequest> {

    public LightblueUpdateRequestFactory() {
        super();
    }

    public LightblueUpdateRequestFactory(String entityName) {
        super(entityName);
    }

    public LightblueUpdateRequestFactory(String entityName, String entityVersion) {
        super(entityName, entityVersion);
    }

    @Override
    protected DataUpdateRequest createRequest(String entityName, String entityVersion) {
        DataUpdateRequest request = new DataUpdateRequest(entityName, entityVersion);
        request.updates(getBody(Update[].class));
        request.where(getQuery());
        request.returns(getProjections());

        return request;
    }

}
