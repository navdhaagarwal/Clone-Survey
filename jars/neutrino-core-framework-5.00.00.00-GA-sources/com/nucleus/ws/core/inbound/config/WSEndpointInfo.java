/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.ws.core.inbound.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Nucleus Software Exports Limited
 */
public final class WSEndpointInfo implements Serializable {

    private static final long serialVersionUID = 2456897968032611943L;

    private String            serviceId;
    private Set<String>       authoritiesAllowed;
    private boolean           secured          = true;

    public WSEndpointInfo(String serviceId, Set<String> authoritiesAllowed, boolean secured) {
        super();
        this.serviceId = serviceId;
        if (authoritiesAllowed == null) {
            authoritiesAllowed = new HashSet<String>();
        }
        this.authoritiesAllowed = Collections.unmodifiableSet(authoritiesAllowed);
        this.secured = secured;
    }

    public boolean isSecured() {
        return secured;
    }

    public Set<String> getAuthoritiesAllowed() {
        return new HashSet<String>(authoritiesAllowed);
    }

    public String getServiceId() {
        return serviceId;
    }

}
