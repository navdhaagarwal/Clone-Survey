package com.nucleus.entityhistory;

import java.util.List;

import com.nucleus.entity.BaseEntity;

public interface EntityHistoryService {

	/**
	 * To Retrieve Entity Record by its className and Id
	 */
	public BaseEntity getBaseEntityByEntityId(
			Class<? extends BaseEntity> entityClass, Long entityId);
	
	/*
	 * To Retrieve List Of Entities those who have common UUID. 
	 */
	public  List<BaseEntity> getBaseEntityByEntityUUID(
			Class<? extends BaseEntity> entityClass, String entityUUId);
}
