package com.nucleus.security.masking.dao;

import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.security.masking.entities.MaskingPolicy;

@Named("maskingPolicyDAO")
public class MaskingPolicyDaoImpl extends BaseDaoImpl<BaseMasterEntity> implements MaskingPolicyDao{

	@Override
	public MaskingPolicy getMaskingPolicyByCode(String code) {

		MaskingPolicy maskingPolicy = null;
		
		try {
			Query query= getEntityManager().createNamedQuery("getMaskingPolcyByCode");
			
			query.setHint("org.hibernate.fetchSize", 1);		
			query.setParameter("code", code);
			
			maskingPolicy = (MaskingPolicy)query.getSingleResult();
			maskingPolicy.getMaskingDefinitions();
			maskingPolicy.getUserRoles();
		} catch (Exception e) {
			BaseLoggers.flowLogger.error(e.getMessage(),e);
		}
		
		return maskingPolicy;
	}

}
