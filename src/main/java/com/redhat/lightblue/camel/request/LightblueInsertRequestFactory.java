package com.redhat.lightblue.camel.request;

import com.redhat.lightblue.client.request.data.DataInsertRequest;

/**
 * Converts Objects into a lightblue insert request.
 */
@Deprecated
public class LightblueInsertRequestFactory extends LightblueRequestFactory<DataInsertRequest> {

    public LightblueInsertRequestFactory() {
        super();
    }

    public LightblueInsertRequestFactory(String entityName) {
        super(entityName);
    }

    public LightblueInsertRequestFactory(String entityName, String entityVersion) {
        super(entityName, entityVersion);
    }

    @Override
    protected DataInsertRequest createRequest(String entityName, String entityVersion) {
        DataInsertRequest request = new DataInsertRequest(entityName, entityVersion);
        request.create(getBody(Object[].class));
        request.returns(getProjections());

        return request;
    }

}
