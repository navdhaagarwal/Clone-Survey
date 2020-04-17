package com.nucleus.sso.client.session;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jasig.cas.client.session.SessionMappingStorage;

/*
 * 
 * Custom Implementation of org.jasig.cas.client.session.SingleSignOutHttpSessionListener
 * 
 */

public class NeutrinoSingleSignOutHttpSessionListener implements HttpSessionListener {
	
	private NeutrinoSessionMappingStorage sessionMappingStorage;

    public void sessionCreated(final HttpSessionEvent event) {
        // nothing to do at the moment
    }

    public void sessionDestroyed(final HttpSessionEvent event) {
        if (sessionMappingStorage == null) {
            sessionMappingStorage = getSessionMappingStorage();
        }
        final HttpSession session = event.getSession();
        sessionMappingStorage.removeBySessionById(session.getId());
    }

    /**
     * Obtains a {@link SessionMappingStorage} object. Assumes this method will always return the same
     * instance of the object.  It assumes this because it generally lazily calls the method.
     * 
     * @return the SessionMappingStorage
     */
    protected static NeutrinoSessionMappingStorage getSessionMappingStorage() {
        return NeutrinoSingleSignOutFilter.getCustomSingleSignOutHandler().getSessionMappingStorage();
    }
}
