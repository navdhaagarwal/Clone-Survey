package com.nucleus.assignmentmaster.addondataprovider;

import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMatrixRowData;
import com.nucleus.rules.model.assignmentMatrix.AssignmentSet;
import com.nucleus.rules.model.assignmentMatrix.BaseAssignmentMaster;
import com.nucleus.service.BaseServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class AddOnDataProviderForAssignmentTaskImpl extends BaseServiceImpl implements AddOnDataProviderForAssignmentTask {

    @Override
    public <T extends BaseAssignmentMaster> T reInitializeAssignmentMaster(Class<T> aClass, Serializable serializable) {
        T assignmentMaster = entityDao.find(aClass,serializable);
        Hibernate.initialize(assignmentMaster.getAssignmentSet());
        if(CollectionUtils.isNotEmpty(assignmentMaster.getAssignmentSet())){
            for(AssignmentSet assignmentSet : assignmentMaster.getAssignmentSet()){
                Hibernate.initialize(assignmentSet.getAssignmentMatrixRowData());
                if(CollectionUtils.isNotEmpty(assignmentSet.getAssignmentMatrixRowData())){
                    for(AssignmentMatrixRowData assignmentMatrixRowData : assignmentSet.getAssignmentMatrixRowData()){
                        Rule rule = assignmentMatrixRowData.getRule();
                        if(rule.getRuntimeRuleMapping()!=null){
                            if(CollectionUtils.isNotEmpty(rule.getRuntimeRuleMapping().getParameters())){
                                Hibernate.initialize(rule.getRuntimeRuleMapping().getParameters());
                            }
                            if(CollectionUtils.isNotEmpty(rule.getRuntimeRuleMapping().getObjectGraphs())){
                                Hibernate.initialize(rule.getRuntimeRuleMapping().getObjectGraphs());
                            }
                        }
                        if(assignmentMatrixRowData.getAssignmentMatrixAction()!=null){
                            Hibernate.initialize(assignmentMatrixRowData.getAssignmentMatrixAction().getParameters());
                        }
                    }
                }
            }
        }
        return assignmentMaster;
    }
}
