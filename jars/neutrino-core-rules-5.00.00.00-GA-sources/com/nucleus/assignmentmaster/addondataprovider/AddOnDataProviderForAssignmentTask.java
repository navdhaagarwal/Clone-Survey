package com.nucleus.assignmentmaster.addondataprovider;

import java.io.Serializable;

import com.nucleus.entity.Entity;
import com.nucleus.rules.model.assignmentMatrix.BaseAssignmentMaster;

public interface AddOnDataProviderForAssignmentTask {
	
	<T extends BaseAssignmentMaster> T reInitializeAssignmentMaster(Class<T> entityClass, Serializable id);
}
