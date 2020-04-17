package com.nucleus.core.ipaddress.vo;



import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;

import java.io.Serializable;


public class IpAddressVO implements Serializable {


    private String ipAddress;
    private String accessType;
    private String status;

    private ReasonsActiveInactiveMapping reasonActInactMap;

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }
    public String getIpAddress(){
        return ipAddress;
    }

    public void setIpAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public String getAccessType() {
        return accessType;
    }

    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public ReasonsActiveInactiveMapping getReasonActInactMap() {
        return reasonActInactMap;
    }

    public void setReasonActInactMap(ReasonsActiveInactiveMapping reasonActInactMap) {
        this.reasonActInactMap = reasonActInactMap;
    }
}
