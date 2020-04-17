package com.nucleus.parentChildDeletionFW;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.rules.model.*;
import com.nucleus.rules.model.assignmentMatrix.TaskAssignmentMaster;
import com.nucleus.rules.taskAssignmentMaster.ObjectGraphClassMapping;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.nucleus.address.City;
import com.nucleus.cas.parentChildDeletionHandling.BaseMasterDependency;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.dynamicform.entities.DynamicFormFilter;
import com.nucleus.core.dynamicform.entities.FieldFilterMapping;
import com.nucleus.core.dynamicform.entities.PlaceholderFilterMapping;
import com.nucleus.core.dynamicform.entities.ServiceFieldFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderFilterMapping;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMapping;
import com.nucleus.core.formsConfiguration.DynamicFormScreenMappingDetail;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.document.core.entity.DocumentChecklist;
import com.nucleus.document.core.entity.DocumentDefinition;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.rules.model.assignmentMatrix.AssignmentMaster;
import com.nucleus.rules.model.eventDefinition.LetterGenerationTask;
import com.nucleus.rules.model.eventDefinition.NotificationTask;
import com.nucleus.rules.model.eventDefinition.RuleInvocationMappingTask;
import com.nucleus.rules.model.eventDefinition.RuleValidationTask;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserCityMapping;
import com.nucleus.user.UserVillageMapping;

@Component
@DependsOn(value = "baseMasterDependency")
public class BaseMasterDependencyFW extends BaseServiceImpl {

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @PostConstruct
    public void init() {
        Boolean isChildDeletionCheckEnabled = BaseMasterDependency.isConfigPresent();
        try {
            if (isChildDeletionCheckEnabled) {
                prepareExclusions();
                BaseMasterDependency.removeExclusionsFromReverseDependencyMap();
                BaseMasterDependency.addMapEntry(DocumentDefinition.class, DocumentChecklist.class, "Select e from DocumentChecklist e inner join e.documents fd where fd.document.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(Parameter.class, Condition.class,"SELECT e FROM Condition e WHERE e.conditionExpression LIKE CONCAT('%', :id, '%') "+ BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(Parameter.class, RuleMatrixMaster.class,"Select e from RuleMatrixMaster e inner join e.assignmentSet e1 inner join e1.assignmentMatrixRowData e2 where e2.rowMapValues like CONCAT('%\"', :id, '\"%') or e2.assignmentMatrixAction.assignActionValues like CONCAT('%\"', :id, '\"%') "+ BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(Parameter.class, AssignmentMaster.class,"Select e from AssignmentMaster e inner join e.assignmentSet e1 inner join e1.assignmentMatrixRowData e2 where e2.rowMapValues like CONCAT('%\"', :id, '\"%') or e2.assignmentMatrixAction.assignActionValues like CONCAT('%\"', :id, '\"%') "+ BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(City.class, UserCityMapping.class, "Select e.user from UserCityVillageMapping e inner join e.userCityMappings e1 where e1.city.id =:id AND e.user.masterLifeCycleData.approvalStatus not in (1,5,10)");
                BaseMasterDependency.addMapEntry(VillageMaster.class, UserVillageMapping.class,"Select e.user from UserCityVillageMapping e inner join e.userVillageMappings e1 where e1.villageMaster.id =:id AND e.user.masterLifeCycleData.approvalStatus not in (1,5,10)");
                BaseMasterDependency.addMapEntry(Rule.class, NotificationTask.class, "Select e from EventDefinition e inner join e.eventTaskList et inner join NotificationTask nt  on et.id = nt.id inner join nt.ruleGroup.rules fd where fd.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(Rule.class, LetterGenerationTask.class, "Select e from EventDefinition e inner join e.eventTaskList et inner join LetterGenerationTask nt  on et.id = nt.id inner join nt.ruleGroup.rules fd where fd.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(Rule.class, RuleValidationTask.class, "Select e from EventDefinition e inner join e.eventTaskList et inner join RuleValidationTask nt  on et.id = nt.id inner join nt.ruleGroup.rules fd where fd.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(RuleInvocationMapping.class, RuleInvocationMappingTask.class, "Select e from EventDefinition e inner join e.eventTaskList et inner join RuleInvocationMappingTask nt  on et.id = nt.id where nt.ruleInvocationMapping.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(Rule.class, RuleInvocationMapping.class, "Select e from RuleInvocationMapping e inner join e.ruleMapping fd where fd.rule.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(RuleAction.class, RuleInvocationMapping.class, "Select e from RuleInvocationMapping e inner join e.ruleMapping fd where fd.thenAction.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(RuleAction.class, RuleInvocationMapping.class, "Select e from RuleInvocationMapping e inner join e.ruleMapping fd where fd.elseAction.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(RuleSet.class, RuleInvocationMapping.class, "Select e from RuleInvocationMapping e inner join e.rulesetMapping fd where fd.ruleSet.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(Rule.class, RuleGroup.class, "Select e from RuleInvocationMapping e inner join e.ruleGroup.rules fd where fd.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(FormConfigurationMapping.class, DynamicFormScreenMapping.class, "Select e from DynamicFormScreenMapping e inner join e.dynamicFormScreenDtlList fd where fd.formConfigurationMapping.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(DynamicFormFilter.class, ServicePlaceholderFilterMapping.class, "Select e from ServicePlaceholderFilterMapping e inner join e.placeholderFilterMappings fd where fd.dynamicFormFilter.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(DynamicFormFilter.class, ServiceFieldFilterMapping.class, "Select e from ServiceFieldFilterMapping e inner join e.fieldFilterMappings fd where fd.dynamicFormFilter.id =:id " + BaseMasterDependency.APPROVAL_STATUS);
                BaseMasterDependency.addMapEntry(ObjectGraphClassMapping.class, TaskAssignmentMaster.class,"Select e from ObjectGraphClassMapping ogcm, TaskAssignmentMaster e inner join e.assignmentSet e1 inner join e1.assignmentFieldMetaDataList e2 where e2.ognl = ogcm.objectGraphType.objectGraph  AND ogcm.id = :id "+ BaseMasterDependency.APPROVAL_STATUS);
            }

        }catch (Exception e){
            BaseLoggers.exceptionLogger.error("Exception Occured : ",e);
        }
    }

    private void prepareExclusions(){
        BaseMasterDependency.addToExclusions(Rule.class,RuleActionMapping.class);
        BaseMasterDependency.addToExclusions(RuleAction.class,RuleActionMapping.class);
        BaseMasterDependency.addToExclusions(RuleAction.class,RuleSetActionMapping.class);
        BaseMasterDependency.addToExclusions(FormConfigurationMapping.class,DynamicFormScreenMappingDetail.class);
        BaseMasterDependency.addToExclusions(DynamicFormFilter.class,PlaceholderFilterMapping.class);
        BaseMasterDependency.addToExclusions(DynamicFormFilter.class,FieldFilterMapping.class);
        BaseMasterDependency.addToExclusions(RuleSet.class,RuleSetActionMapping.class);

    }

    public static Boolean isDependencyPresent(Class childClass, Long id)
    {
       return BaseMasterDependency.isDependencyPresent(childClass,id);
    }

}
