package com.nucleus.businessmapping.service;

import javax.inject.Named;

import com.nucleus.master.BaseMasterEntity;

@Named("userBPMappingService")
public class UserBPMappingServiceImpl implements UserBPMappingService{

	@Override
	public String getAssociatedBPCodeByUserId(Long userId) {
		return null;
	}

	@Override
	public String getSimpleNameForBaseMasterEntity(BaseMasterEntity bma) {
		return bma.getClass().getSimpleName();
	}

	@Override
	public Long getAssociatedBPIdByUserId(Long userId) {
		return null;
	}

	@Override
	public String getBusinessPartnerNameByUserId(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
