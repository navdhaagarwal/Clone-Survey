package com.nucleus.master.audit;

import org.javers.core.Javers;
import org.javers.core.JaversBuilder;

import com.nucleus.logging.BaseLoggers;

public class MasterChangeDisJointChildEntityJaversHolder {

	private JaversBuilder entityJaversInstance;
	
	private Javers javerInstance;

	public Javers getJaverInstance() {
		if(javerInstance == null){
			if(entityJaversInstance == null){
				BaseLoggers.exceptionLogger.error("Javers instance can not be created with javersBuilder");
			}else{
				javerInstance = entityJaversInstance.build();
			}
		}
		return javerInstance;
	}

	
	public MasterChangeDisJointChildEntityJaversHolder(JaversBuilder javerInstance) {
		super();
		this.entityJaversInstance = javerInstance;
	}

	public JaversBuilder getEntityJaversInstance() {
		return entityJaversInstance;
	}

	public void setEntityJaversInstance(JaversBuilder entityJaversInstance) {
		this.entityJaversInstance = entityJaversInstance;
	}

	
}
