package com.nucleus.businessmapping.service;

import com.nucleus.master.BaseMasterEntity;


public interface UserBPMappingService {
	public String getAssociatedBPCodeByUserId(Long userId);
	public Long getAssociatedBPIdByUserId(Long userId);
	String getSimpleNameForBaseMasterEntity(BaseMasterEntity bma);
	public String getBusinessPartnerNameByUserId(Long id);

}