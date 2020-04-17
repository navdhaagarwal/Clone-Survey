package com.nucleus.user;

import com.nucleus.master.audit.annotation.EmbedInAuditAsValue;

import javax.persistence.Embeddable;

@Embeddable
public class IPAddressRange {

    @EmbedInAuditAsValue
    private String ipaddress;

    @EmbedInAuditAsValue
    private String fromIpAddress;

    @EmbedInAuditAsValue
    private String toIpAddress;

    @EmbedInAuditAsValue
    private Boolean securedIp;

    public String getFromIpAddress() {
        return fromIpAddress;
    }

    public void setFromIpAddress(String fromIpAddress) {
        this.fromIpAddress = fromIpAddress;
    }

    public String getToIpAddress() {
        return toIpAddress;
    }

    public void setToIpAddress(String toIpAddress) {
        this.toIpAddress = toIpAddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress = ipaddress;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public Boolean getSecuredIp() {
        return securedIp;
    }

    public void setSecuredIp(Boolean securedIp) {
        this.securedIp = securedIp;
    }
}
