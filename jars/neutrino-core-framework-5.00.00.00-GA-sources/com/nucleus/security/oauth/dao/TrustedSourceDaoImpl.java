package com.nucleus.security.oauth.dao;

import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.Transactional;

import org.hibernate.Hibernate;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import com.nucleus.entity.BaseEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;

@Transactional
@Named("trustedSourceDao")
public class TrustedSourceDaoImpl extends BaseDaoImpl<BaseEntity> implements TrustedSourceDao {

	@Override
	public OauthClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {

		OauthClientDetails trustedSource = null;

		try {
			Query query = getEntityManager().createNamedQuery("getTrustedSource");

			query.setParameter("clientId", clientId);

			trustedSource = (OauthClientDetails) query.getSingleResult();
		} catch (NoResultException ex) {
			BaseLoggers.flowLogger.debug(ex.getMessage());
			throw new NoSuchClientException("No client with requested id: " + clientId);
		}
		return trustedSource;
	}

	@Override
	public OauthClientDetails loadUnproxiedClientByClientId(String clientId) throws ClientRegistrationException {

		OauthClientDetails trustedSource = loadClientByClientId(clientId);

		Hibernate.initialize(trustedSource.getMappedAPIs());
		Hibernate.initialize(trustedSource.getIpAddresses());
		Hibernate.initialize(trustedSource.getTrustedUsers());
		return trustedSource;
	}

}
