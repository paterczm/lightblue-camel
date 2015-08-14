package com.redhat.lightblue.camel.request;

import com.redhat.lightblue.client.request.data.DataSaveRequest;

/**
 * Converts Objects into a lightblue save request.
 */
@Deprecated
public class LightblueSaveRequestFactory extends LightblueRequestFactory<DataSaveRequest> {

    public LightblueSaveRequestFactory() {
        super();
    }

    public LightblueSaveRequestFactory(String entityName) {
        super(entityName);
    }

    public LightblueSaveRequestFactory(String entityName, String entityVersion) {
        super(entityName, entityVersion);
    }

    @Override
    protected DataSaveRequest createRequest(String entityName, String entityVersion) {
        DataSaveRequest request = new DataSaveRequest(entityName, entityVersion);
        request.create(getBody(Object[].class));
        request.returns(getProjections());

        return request;
    }

}
