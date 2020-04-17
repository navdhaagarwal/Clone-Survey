package com.nucleus.security.oauth.dao;

import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.nucleus.entity.BaseEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.security.oauth.domainobject.OauthTokenDetails;

@Named("tokenDAO")
public class OauthTokenDaoImpl extends BaseDaoImpl<BaseEntity> implements OauthTokenDao {

	public OauthTokenDetails getTokenDetails(String clientId, String userName) {
		OauthTokenDetails tokenDetails = null;
		Query query = null;
		try {
			if(userName != null) {
				query = getEntityManager().createNamedQuery("getTokenDetailsByUsername");
				query.setParameter("userName", userName);
			}else {
				query = getEntityManager().createNamedQuery("getTokenDetails");	
			}
			query.setHint("org.hibernate.fetchSize", 1);
			query.setParameter("clientId", clientId);
			
			tokenDetails = (OauthTokenDetails) query.getSingleResult();
		} catch (NoResultException ex) {
			BaseLoggers.exceptionLogger.error(ex.getMessage());
		}
		return tokenDetails;
	}

	public void saveOrupdateTokenDetails(OauthTokenDetails tokenDetailsFromDB, OauthTokenDetails tokenDetails) {
		if (tokenDetailsFromDB == null) {
			//new token is created.
			if (tokenDetails == null) {
				BaseLoggers.flowLogger.error("TokenDetails and TokenDetailsFromDB are both null. EntityDaoImpl will throw an exception.");
				return ;
			}
			saveOrUpdate(tokenDetails);
		} else {
			//Update new expiry time.
			tokenDetailsFromDB.setExpiryTime(tokenDetails.getExpiryTime());
			tokenDetailsFromDB.setToken(tokenDetails.getToken());
			tokenDetailsFromDB.setScope(tokenDetails.getScope());
			tokenDetailsFromDB.setRefreshToken(tokenDetails.getRefreshToken());
			saveOrUpdate(tokenDetailsFromDB);
		}
	}

}
