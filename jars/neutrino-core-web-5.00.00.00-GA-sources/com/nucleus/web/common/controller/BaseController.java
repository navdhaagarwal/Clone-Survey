package com.nucleus.web.common.controller;

import java.lang.reflect.Field;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.persistence.HibernateUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import org.springframework.context.NoSuchMessageException;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.event.EventDefinition;
import com.nucleus.core.event.EventExecutionResult;
import com.nucleus.core.event.EventTask;
import com.nucleus.core.event.service.EventExecutionService;
import com.nucleus.core.event.service.EventExecutionVO;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.BaseEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.eventdefinition.service.EventDefinitionService;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleGroup;
import com.nucleus.rules.model.eventDefinition.RuleInvocationMappingTask;
import com.nucleus.rules.model.eventDefinition.RuleValidationTask;
import com.nucleus.rules.service.RuleInvocationResult;
import com.nucleus.rules.service.RulesAuditLogService;

@Transactional(propagation = Propagation.REQUIRED)
public class BaseController extends NonTransactionalBaseController {

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("eventExecutionService")
    private EventExecutionService eventExecutionService;

    @Inject
    @Named("eventDefinitionService")
    private EventDefinitionService eventDefinitionService;

    @Inject
    @Named("rulesAuditLogService")
    private RulesAuditLogService rulesAuditLogService;

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    public static final String MASTER = "MASTER_";
    public static final String IS_GETTING_FAILED = " is getting failed";
    public static final String RULE = "Rule ";
    public static final String ERROR_IN_RETRIVING = "Error in Retriving :";
    public static final String RULE_ERROR_MESSAGES = "ruleErrorMessages";

    public boolean executeMasterEvent(BaseMasterEntity entity, String contextObjectKey, ModelMap map) {
        Map<Object, Object> contextMap = new HashedMap();
        contextMap.put(contextObjectKey, entity);
        String eventCodeName = null;
        Class cls = entity.getClass();
        Class superClass = cls.getSuperclass();
        /*if (superClass != null) {
            Inheritance inheritance = AnnotationUtils.findAnnotation(superClass, Inheritance.class);
            if (inheritance != null && inheritance.strategy() != null && inheritance.strategy().equals(InheritanceType.SINGLE_TABLE)
                    && superClass.getSimpleName().equals("GenericParameter")) {
                eventCodeName = superClass.getSimpleName();
            }
        }
        if (eventCodeName == null) {
            eventCodeName = cls.getSimpleName();
        }
        EventCode eventCode = genericParameterService.findByCode((MASTER + eventCodeName).toUpperCase(), EventCode.class);*/
        EventCode eventCode = null;
        eventCodeName = cls.getSimpleName();
        if(!eventCodeName.isEmpty()){
            eventCode = genericParameterService.findByCode((MASTER + eventCodeName).toUpperCase(), EventCode.class);
            if(eventCode == null){
                if (superClass != null) {
                    Inheritance inheritance = AnnotationUtils.findAnnotation(superClass, Inheritance.class);
                    if (inheritance != null && inheritance.strategy() != null && inheritance.strategy().equals(InheritanceType.SINGLE_TABLE)) {
                        eventCodeName = superClass.getSimpleName();
                    }
                }
                eventCode = genericParameterService.findByCode((MASTER + eventCodeName).toUpperCase(), EventCode.class);
            }
        }

        if (eventCode != null) {
            EventDefinition eventDefinition = eventDefinitionService.getEventDefinitionByCode(eventCode.getCode());
            List<String> ruleErrorList = new ArrayList<>();
            Boolean validationTaskResult = true;
            if (eventDefinition != null && CollectionUtils.isNotEmpty(eventDefinition.getEventTaskList())) {
                try {
                    replaceEmptyToNull(entity);
                } catch (IllegalAccessException e) {
                    BaseLoggers.exceptionLogger.error(ERROR_IN_RETRIVING, e);
                }
                EventExecutionVO eventExecutionVO = new EventExecutionVO();
                eventExecutionVO.setAuditingEnabled(true);
                eventExecutionVO.setPurgingRequired(true);
                EventExecutionResult eventExecutionResult = eventExecutionService.fireEventExecution(eventCode.getCode(), contextMap, eventExecutionVO);
                if (eventExecutionResult != null) {
                    for (EventTask eventTask : eventDefinition.getEventTaskList()) {
                        if (eventTask instanceof RuleInvocationMappingTask) {
                            RuleInvocationResult ruleInvocationResult = (RuleInvocationResult) eventExecutionResult.getResultMap().get(eventTask.getCode());
                            if (ruleInvocationResult != null) {
                                Map<Object, Object> allRulesResult = ruleInvocationResult.getRuleExecutionMap();
                                ruleErrorList = rulesAuditLogService.getRulesErrorMessages(allRulesResult);
                                /*if(CollectionUtils.isNotEmpty(errorMessages)){
                                	ruleErrorList = new ArrayList<>(errorMessages);
                                }*/
                            }
                        } else if (eventTask instanceof RuleValidationTask) {
                            validationTaskResult = eventExecutionResult.getValidationTask();
                            if (validationTaskResult != null && !validationTaskResult.booleanValue()) {
                                RuleGroup ruleGroup = ((RuleValidationTask) eventTask).getRuleGroup();
                                List<Rule> rules = ruleGroup.getRules();
                                if (CollectionUtils.isNotEmpty(rules) && MapUtils.isNotEmpty(contextMap)) {
                                    for (Rule rule : rules) {
                                        Boolean result = (Boolean) contextMap.get(rule.getName() + "$" + rule.getId());
                                        if (result != null && !result.booleanValue()) {
                                            if (StringUtils.isNotBlank(rule.getErrorMessageKey())) {
                                                getMessageFromKey(Locale.getDefault(), ruleErrorList, rule);
                                            } else if (StringUtils.isNotBlank(rule.getErrorMessage())) {
                                                ruleErrorList.add(rule.getErrorMessage());
                                            } else {
                                                ruleErrorList.add(RULE + rule.getCode() + IS_GETTING_FAILED);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (CollectionUtils.isEmpty(ruleErrorList) && validationTaskResult != null && validationTaskResult.booleanValue()) {
                return true;
            } else {
                map.put(RULE_ERROR_MESSAGES, ruleErrorList);
                return false;
            }
        } else {
            return true;
        }
    }

    private void getMessageFromKey(Locale locale, List<String> ruleErrorList, Rule rule) {
        try {
            String message = messageSource.getMessage(rule.getErrorMessageKey(), null, locale);
            if (StringUtils.isNotBlank(message)) {
                ruleErrorList.add(message);
            } else {
                ruleErrorList.add(rule.getErrorMessage());
            }
        } catch (NoSuchMessageException e) {
            BaseLoggers.exceptionLogger.error("No message found ", e);
        }
    }

    public void replaceEmptyToNull(Object object) throws IllegalAccessException {
        Class aClass = object.getClass();
        List<Field> fields = getFieldsInHierarchy(aClass);
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.getType() != null) {
                if (field.getType().equals(String.class)) {
                    String value = (String) field.get(object);
                    if (StringUtils.isBlank(value)) {
                        field.set(object, null);
                    }
                } else if (field.get(object) instanceof BaseEntity) {
                    replaceEmptyToNull(field.get(object));
                }/*else if(field.get(object)instanceof Collection){
                    Collection collection = (Collection) field.get(object);
                    for (Object chileObject : collection) {
                        replaceEmptyToNull(chileObject);
                    }
                }*/
            }
        }
    }

    private List<Field> getFieldsInHierarchy(Class clazz) {
        List<Field> fieldList = new ArrayList();
        try {
            for (Class acls = clazz; acls != null; acls = acls.getSuperclass()) {
                if (acls == Object.class) {
                    continue;
                }
                Field[] fieldArray = acls.getDeclaredFields();
                fieldList.addAll(Arrays.asList(fieldArray));
            }
        } catch (Exception e) {
            fieldList.clear();
            Field[] fieldArray = clazz.getDeclaredFields();
            fieldList.addAll(Arrays.asList(fieldArray));
        }
        return fieldList;
    }

    public void flushCurrentTransaction() {
        entityDao.flush();

    }


    public void saveActInactReasonForMaster(ReasonsActiveInactiveMapping reasonsActiveInactiveMapping, BaseMasterEntity entity) {
        String action = null;
        if (entity.isActiveFlag()) {
            action = "active";
        } else {
            action = "Inactive";
        }
        reasonsActiveInactiveMapping.setTypeOfAction(action);
        List<MasterActiveInactiveReasons> masterActiveInactiveReasonslist = reasonsActiveInactiveMapping.getMasterActiveInactiveReasons();
        if (masterActiveInactiveReasonslist != null && masterActiveInactiveReasonslist.size() > 0){
            for (MasterActiveInactiveReasons mstActiveInactiveReason : masterActiveInactiveReasonslist) {
                if (mstActiveInactiveReason.getReasonActive() != null && mstActiveInactiveReason.getReasonActive().getId() != null && entity.isActiveFlag()) {
                    HibernateUtils.initializeAndUnproxy(mstActiveInactiveReason.getReasonActive());
                    ReasonActive savedReasonActive = genericParameterService.findById(mstActiveInactiveReason.getReasonActive().getId(), ReasonActive.class);
                    mstActiveInactiveReason.setReasonActive(savedReasonActive);
                    mstActiveInactiveReason.setDescription(mstActiveInactiveReason.getActiveDescription());

                } else {

                    mstActiveInactiveReason.setReasonActive(null);
                }
                if (mstActiveInactiveReason.getReasonInactive() != null && mstActiveInactiveReason.getReasonInactive().getId() != null && !entity.isActiveFlag()) {
                    HibernateUtils.initializeAndUnproxy(mstActiveInactiveReason.getReasonInactive());
                    ReasonInActive savedReasonInActive = genericParameterService.findById(mstActiveInactiveReason.getReasonInactive().getId(), ReasonInActive.class);
                    mstActiveInactiveReason.setReasonInactive(savedReasonInActive);
                    mstActiveInactiveReason.setDescription(mstActiveInactiveReason.getInacitveDescription());
                } else {

                    mstActiveInactiveReason.setReasonInactive(null);
                }
                //entityDao.saveOrUpdate(mstActiveInactiveReason);
            }
    }
        //entityDao.saveOrUpdate(reasonsActiveInactiveMapping);
    }

    public ReasonsActiveInactiveMapping getActInactReasMapForCreate(ModelMap map) {
        ReasonsActiveInactiveMapping reasonsActiveInactiveMappingForCreate = new ReasonsActiveInactiveMapping();
        MasterActiveInactiveReasons masterActiveInactiveReasonsForCreate = new MasterActiveInactiveReasons();
        List<MasterActiveInactiveReasons> masterActiveInactiveReasonsListForCreate = new ArrayList<MasterActiveInactiveReasons>();
        masterActiveInactiveReasonsListForCreate.add(masterActiveInactiveReasonsForCreate);
        reasonsActiveInactiveMappingForCreate.setMasterActiveInactiveReasons(masterActiveInactiveReasonsListForCreate);
        map.put("activeFlag", true);
        map.put("activeFlagApproved", true);
        map.put("editActive", false);
        map.put("viewable", false);
        map.put("create", true);
        map.put("inactiveReasonFlag", false);
        map.put("flagForFirstTimeEdit",false);
        return reasonsActiveInactiveMappingForCreate;
    }

    public void getActInactReasMapForEdit(ModelMap map, BaseMasterEntity entity) {
        if (ApprovalStatus.APPROVED_MODIFIED == entity.getApprovalStatus() || ApprovalStatus.APPROVED == entity.getApprovalStatus() || ApprovalStatus.UNAPPROVED_MODIFIED == entity.getApprovalStatus() ) {
            map.put("create", true);
            map.put("inactiveReasonFlag", true);

        } else {
            map.put("create", false);
            map.put("inactiveReasonFlag", false);
        }
        map.put("editActive", true);
        map.put("activeFlag", entity.isActiveFlag());
        map.put("inactiveReasonFlag", false);
    }

    public void getActInactReasMapForEditApproved(ModelMap map, BaseMasterEntity entity, String masterId, String uniqueParameter, String uniqueValue) {
        getActInactReasMapForEditApproved(map, entity, masterId, uniqueParameter, uniqueValue, false);
    }

    public void getActInactReasMapForEditApproved(ModelMap map, BaseMasterEntity entity, String masterId, String uniqueParameter, String uniqueValue, Boolean numValue) {
        boolean flagForFirstTimeEdit = false;
        if (ApprovalStatus.UNAPPROVED_MODIFIED == entity.getApprovalStatus()) {
            map.put("create", true);
            map.put("inactiveReasonFlag", true);

        } else {
            map.put("create", false);
            map.put("inactiveReasonFlag", false);
        }
        map.put("editActive", true);
        map.put("activeFlag", entity.isActiveFlag());
        if (ApprovalStatus.CLONED == entity.getApprovalStatus()) {
            map.put("activeFlag", true);
            map.put("activeFlagApproved", true);
            map.put("editActive", false);
            map.put("viewable", false);
            map.put("create", true);
            map.put("inactiveReasonFlag", false);
            map.put("flagForFirstTimeEdit",false);
        }else {
            List<Integer> approvalStatusList = new ArrayList<Integer>();
            approvalStatusList.add(ApprovalStatus.APPROVED);
            approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
            StringBuilder sb = new StringBuilder();
            sb.append("from " + masterId + " c where c." + uniqueParameter + " = :uniqueValue and c.masterLifeCycleData.approvalStatus IN :approvalStatus");
            JPAQueryExecutor<BaseMasterEntity> jpaQueryExecutor = new JPAQueryExecutor<BaseMasterEntity>(sb.toString());

            if (numValue) {
                try {
                    jpaQueryExecutor.addParameter("uniqueValue", Long.parseLong(uniqueValue));
                } catch (NumberFormatException e) {
                    BaseLoggers.exceptionLogger.error("Unique Value for searching record is null", e);
                    throw new SystemException(e);
                }
            } else {
                jpaQueryExecutor.addParameter("uniqueValue", uniqueValue);
            }
            jpaQueryExecutor.addParameter("approvalStatus", approvalStatusList);
            BaseMasterEntity entity1 = entityDao.executeQueryForSingleValue(jpaQueryExecutor);
            if (entity1 != null) {
                Field field = null;
                try {
                    field = entity1.getClass().getDeclaredField("reasonActInactMap");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                field.setAccessible(true);
                try {
                    ReasonsActiveInactiveMapping value = (ReasonsActiveInactiveMapping) field.get(entity1);
                    if (value == null) {
                        map.put("flagForFirstTimeEdit", true);
                    } else {
                        map.put("flagForFirstTimeEdit", false);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                map.put("activeFlagApproved", entity1.isActiveFlag());
                map.put("create", true);
                map.put("inactiveReasonFlag", true);
            } else {
                map.put("flagForFirstTimeEdit", false);
                map.put("activeFlagApproved", entity.isActiveFlag());
            }
        }
    }

    public ReasonsActiveInactiveMapping getActInactReasonsForEditGeneric(ModelMap map,ReasonsActiveInactiveMapping reason){
        if(reason != null) {
            if(reason.getTypeOfAction() != null && reason.getTypeOfAction().equalsIgnoreCase("active")) {
                for (MasterActiveInactiveReasons mst : reason.getMasterActiveInactiveReasons()) {
                    mst.setActiveDescription(mst.getDescription());
                }
            }
            if(reason.getTypeOfAction() != null && reason.getTypeOfAction().equalsIgnoreCase("inactive")) {
                for (MasterActiveInactiveReasons mst : reason.getMasterActiveInactiveReasons()) {
                    mst.setInacitveDescription(mst.getDescription());
                }
            }
            map.put("reasonsActiveInactiveMapping", reason);
        }
        else{
           /* ReasonsActiveInactiveMapping reasonsActiveInactiveMappingEdit = new ReasonsActiveInactiveMapping();
            MasterActiveInactiveReasons masterActiveInactiveReasonsEdit = new MasterActiveInactiveReasons();
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsEditList = new ArrayList<MasterActiveInactiveReasons>();
            masterActiveInactiveReasonsEditList.add(masterActiveInactiveReasonsEdit);
            reasonsActiveInactiveMappingEdit.setMasterActiveInactiveReasons(masterActiveInactiveReasonsEditList);
            reason = reasonsActiveInactiveMappingEdit;*/
            map.put("reasonsActiveInactiveMapping",reason);
        }
        if(reason != null&& reason.getMasterActiveInactiveReasons() != null){
            for(MasterActiveInactiveReasons mstReason:reason.getMasterActiveInactiveReasons()){
                if(mstReason.getReasonActive() != null){
                    HibernateUtils.initializeAndUnproxy(mstReason.getReasonActive());
                }
                if(mstReason.getReasonInactive() != null){
                    HibernateUtils.initializeAndUnproxy(mstReason.getReasonInactive());
                }
            }
        }
        return reason;
    }
    public void removeDeletedReasons(String[] reasonIdArr, List<MasterActiveInactiveReasons> mstList){
        List<String> reasonIdList = Arrays.asList(reasonIdArr);
        for (String reasonIdStr : reasonIdList) {
            String idOfdeltReas = reasonIdStr.substring(0,reasonIdStr.lastIndexOf("+"));
            Long reasonId = Long.parseLong(idOfdeltReas);
            String description =  reasonIdStr.substring(reasonIdStr.lastIndexOf("+") +1);
            Iterator iterator = mstList.iterator();
            while (iterator.hasNext()) {
                MasterActiveInactiveReasons mstReason = (MasterActiveInactiveReasons) iterator.next();
                if (mstReason.getReasonActive() != null && mstReason.getReasonActive().getId() != null && mstReason.getReasonActive().getId().compareTo(reasonId) == 0 && ((mstReason.getActiveDescription() != null && mstReason.getActiveDescription().equals(description))|| mstReason.getActiveDescription() == null)) {
                    iterator.remove();
                    break;
                }
                if (mstReason.getReasonInactive() != null && mstReason.getReasonInactive().getId() != null && mstReason.getReasonInactive().getId().compareTo(reasonId) == 0 && ((mstReason.getInacitveDescription() != null && mstReason.getInacitveDescription().equals(description)) || mstReason.getInacitveDescription() == null )) {
                    iterator.remove();
                    break;
                }
            }

        }

    }
}