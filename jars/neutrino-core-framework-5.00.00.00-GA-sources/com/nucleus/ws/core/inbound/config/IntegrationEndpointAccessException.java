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

/**
 * @author Nucleus Software Exports Limited
 */
public class IntegrationEndpointAccessException extends RuntimeException {

    private static final long serialVersionUID = 2954449294403117561L;

    public IntegrationEndpointAccessException() {
        super();
    }

    public IntegrationEndpointAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public IntegrationEndpointAccessException(String message) {
        super(message);
    }

    public IntegrationEndpointAccessException(Throwable cause) {
        super(cause);
    }

    public IntegrationEndpointAccessException(String template, Object... arguments) {
        super(String.format(template != null ? template : "", arguments));
    }

}
