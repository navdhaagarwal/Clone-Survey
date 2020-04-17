package com.nucleus.service;



public interface EntityUpdateService{
	
	public void copyAssociatedEntitiesFromSourceToTarget(String fromOwnerEntityUri,
			String toOwnerEntityUri);
	
}