package com.nucleus.master.audit.service.util;

import java.util.HashMap;
import java.util.Map;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.audit.MasterChangeDisJointChildEntityJaversHolder;

public class MasterChangeJaversHolder {

	private JaversBuilder rootEntityJaversBuilderInstance;
	
	private Javers rootEntityJavrseInstance;
	
	private Map<String,MasterChangeDisJointChildEntityJaversHolder> disJointHolders;

	public Javers getRootEntityJavrseInstance() {
		if(rootEntityJavrseInstance == null){
			if(rootEntityJaversBuilderInstance == null){
				BaseLoggers.exceptionLogger.error("Javers instance can not be created with javersBuilder");
			}else{
				rootEntityJavrseInstance = rootEntityJaversBuilderInstance.build();
			}
		}
		return rootEntityJavrseInstance;
	}

	public Map<String, MasterChangeDisJointChildEntityJaversHolder> getDisJointHolders() {
		return disJointHolders;
	}

	public MasterChangeJaversHolder(JaversBuilder rootEntityJavrseInstance) {
		super();
		this.rootEntityJaversBuilderInstance = rootEntityJavrseInstance;
	}
	
	public MasterChangeJaversHolder withDisJointHolders(String key, MasterChangeDisJointChildEntityJaversHolder disJointEntityHolder){
		if(this.disJointHolders == null){
			this.disJointHolders = new HashMap<>();
		}
		this.disJointHolders.put(key, disJointEntityHolder);
		return this;
	}

	public JaversBuilder getRootEntityJaversBuilderInstance() {
		return rootEntityJaversBuilderInstance;
	}

	public void setRootEntityJaversBuilderInstance(JaversBuilder rootEntityJaversBuilderInstance) {
		this.rootEntityJaversBuilderInstance = rootEntityJaversBuilderInstance;
	}
	
	
}
