package com.nucleus.master.audit;

import java.util.HashMap;
import java.util.Map;

import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;

public class MasterChangeEntityHolder {

	private BaseMasterEntity rootEntity;
	
	private Map<String, MasterChangeDisjointChildEntityHolder> disJointChildEntity;
	
	private AuditableClassMetadataFactory metadataFactory;

	public BaseMasterEntity getRootEntity() {
		return rootEntity;
	}

	public void setRootEntity(BaseMasterEntity rootEntity) {
		this.rootEntity = rootEntity;
	}

	public Map<String, MasterChangeDisjointChildEntityHolder> getDisJointChildEntity() {
		return disJointChildEntity;
	}

	public void setDisJointChildEntity(Map<String, MasterChangeDisjointChildEntityHolder> disJointChildEntity) {
		this.disJointChildEntity = disJointChildEntity;
	}

	public MasterChangeEntityHolder(BaseMasterEntity rootEntity) {
		super();
		this.rootEntity = rootEntity;
	}
	
	public MasterChangeDisjointChildEntityHolder getDisJointChildByName(String key){
		if(disJointChildEntity !=null){
			return disJointChildEntity.get(key);
		}
		return null;
	}
	
	
	public void addDisJointChildEntity(String key, MasterChangeDisjointChildEntityHolder disJointChildEntity) {
		if(this.disJointChildEntity == null){
			this.disJointChildEntity = new HashMap<>();
		}
		this.disJointChildEntity.put(key, disJointChildEntity);
	}

	public AuditableClassMetadataFactory getMetadataFactory() {
		return metadataFactory;
	}

	public void setMetadataFactory(AuditableClassMetadataFactory metadataFactory) {
		this.metadataFactory = metadataFactory;
	}
	
	
}
