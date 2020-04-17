package com.nucleus.cfi.push.pojo;

import java.io.Serializable;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class PushNotification  implements Serializable {
    private static final long serialVersionUID = 32342342L;

    private String            deviceId;
    private String            body;
    private String            uniqueRequestId;
    public String getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getUniqueRequestId() {
        return uniqueRequestId;
    }
    public void setUniqueRequestId(String uniqueRequestId) {
        this.uniqueRequestId = uniqueRequestId;
    }  
}
