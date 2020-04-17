package com.nucleus.sso.client.session;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.client.session.SessionMappingStorage;

public interface NeutrinoSessionMappingStorage extends SessionMappingStorage{

	 void removeSessionByMappingId(String mappingId, HttpServletRequest request);
	
}
