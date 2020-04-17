package com.nucleus.infinispan.console.service;

import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nucleus.infinispan.console.entity.ClusterCommunicationToken;
import com.nucleus.persistence.EntityDao;

/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("clusterConsoleHelper")
public class ClusterConsoleHelper {

	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String createToken() {
		ClusterCommunicationToken clusterCommunicationToken = new ClusterCommunicationToken();
		String commToken = UUID.randomUUID().toString();
		clusterCommunicationToken.setCommunicationTokenCode(commToken);
		entityDao.persist(clusterCommunicationToken);
		return commToken;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String updateToken(Long id) {
		ClusterCommunicationToken communicationToken = entityDao.find(ClusterCommunicationToken.class, id);
		String commToken = UUID.randomUUID().toString();
		communicationToken.setCommunicationTokenCode(commToken);
		communicationToken.setIsValidToken(true);
		entityDao.persist(communicationToken);
		return commToken;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public String markInvalidOnRead(Long id) {

		ClusterCommunicationToken communicationToken = entityDao.find(ClusterCommunicationToken.class, id);
		communicationToken.setIsValidToken(false);
		entityDao.persist(communicationToken);
		return communicationToken.getCommunicationTokenCode();
	}

}
