package com.nucleus.master.audit.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.master.audit.service.diffmessage.MasterChangeMessageGenerationUtility;
import org.apache.commons.collections.MapUtils;
import org.javers.core.Javers;
import org.javers.core.diff.Diff;
import org.springframework.stereotype.Component;

import com.nucleus.master.audit.MasterChangeDisjointChildEntityHolder;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.service.util.MasterChangeJaversHolder;
import com.nucleus.master.audit.MasterChangeDiffHolder;
import com.nucleus.master.audit.MasterChangeDisJointEntiyDiffHolder;

@Component("masterChangeJaversExecutor")
public class MasterChangeJaversExecutor {

	@Inject
	@Named("masterChangeJaversRegister")
	private MasterChangeJaversRegister register;

	public MasterChangeDiffHolder getDiff(MasterChangeEntityHolder oldEntity, MasterChangeEntityHolder newEntity,
			Class entity) throws Exception {
		// new record;
		if (oldEntity.getRootEntity() == null && newEntity.getRootEntity() != null) {
			return new MasterChangeDiffHolder("New Record");
		}
		// edited record
		if (oldEntity.getRootEntity() != null
				&& newEntity.getRootEntity() != null) {
			MasterChangeJaversHolder javerHolder = register.getJaversInstance(entity);
			if (javerHolder == null) {
				throw new Exception("No Javers Definition Defined for Class :" + entity);
			}
			Javers rootEntityJavers = javerHolder.getRootEntityJavrseInstance();
			if (rootEntityJavers != null) {
				Diff rootDiff = rootEntityJavers.compare(oldEntity.getRootEntity(), newEntity.getRootEntity());
				final MasterChangeDiffHolder diffHolder = new MasterChangeDiffHolder(rootDiff);
				if (MapUtils.isNotEmpty(javerHolder.getDisJointHolders())) {
					javerHolder.getDisJointHolders().forEach((key, disJaverInstance) -> {
						MasterChangeDisjointChildEntityHolder oldDisJoint = oldEntity.getDisJointChildByName(key);
						MasterChangeDisjointChildEntityHolder newDisJoint = newEntity.getDisJointChildByName(key);
						if (oldDisJoint != null && oldDisJoint.getDisJointEntity() != null && newDisJoint != null
								&& newDisJoint.getDisJointEntity() != null) {
							diffHolder.addDisJointEntityDiff(key,new MasterChangeDisJointEntiyDiffHolder(disJaverInstance.getJaverInstance()
									.compare(oldDisJoint.getDisJointEntity(), newDisJoint.getDisJointEntity())));
						}else if(oldDisJoint != null && oldDisJoint.getDisJointEntity() != null && (newDisJoint == null
								|| newDisJoint.getDisJointEntity() == null)){
							diffHolder.addDeltaInString(MasterChangeMessageGenerationUtility.REMOVED+key);
						}else if(newDisJoint != null && newDisJoint.getDisJointEntity() != null && (oldDisJoint == null
								|| oldDisJoint.getDisJointEntity() == null)){
							diffHolder.addDeltaInString(MasterChangeMessageGenerationUtility.ADDED+key);
						}
					});
				}
				return diffHolder;
			}
		}
		return null;
	}
}
