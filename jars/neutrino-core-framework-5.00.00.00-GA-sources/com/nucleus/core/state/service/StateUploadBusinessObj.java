package com.nucleus.core.state.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fop.svg.ACIUtils;
import org.hibernate.Hibernate;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.address.Country;
import com.nucleus.address.IntraCountryRegion;
import com.nucleus.address.State;
import com.nucleus.address.VehicleStateRegistraionMapping;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.actInactReasService.ActiveInactiveReasonService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.jsMessageResource.service.JsMessageResourceService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;

@Named("stateUploadBusinessObj")
public class StateUploadBusinessObj extends BaseServiceImpl implements IStateUploadBusinessObj {

    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService       baseMasterService;

    @Inject
    @Named("vehicleStateService")
    private VehicleStateService     vehicleStateService;

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService     makerCheckerService;
    
    @Inject
    @Named("activeInactiveReasonService")
    private ActiveInactiveReasonService activeInactiveReasonService;

    public static String            stateCodeRegx       = "^[A-Za-z0-9_]{0,8}$";
    
    public static String            stateRtoCodeRegx    = "^[A-Za-z]+$";
    
    public static String            range               = "range";
    public static String            startsEndsWith      = "startsEndsWith";

    public static String            pincodeRangeRegx    = "^[0-9]+-[0-9]+$";

    public static String            pinCodeStart        = "^[a-zA-Z0-9]*$";

    public static final String      FALSE               = "false";
    public static final String      ACTIVE              = "active";
    public static final String      INACTIVE            = "Inactive";
    public static final String      DELETE              = "Delete";
    public static final String      EDIT                = "Edit";
    public static final String      CREATE              = "Create";
    public static final String      ERRORMSG            = "Error in State Upload";
    
    
    public static final int         stateRtoLength      =3;

    private static final String     CONFIGURATION_QUERY = "Configuration.getPropertyValueFromPropertyKey";

    @Inject
    @Named("configurationService")
    public ConfigurationService     configurationService;

    @Override
    public State uploadState(State stateObj) {

        List<Message> validationMessages = new ArrayList<Message>();

        if (DELETE.equalsIgnoreCase(stateObj.getUploadOperationType())) {
            
            deleteStateMasterData(stateObj, validationMessages);
            
        } else if (EDIT.equalsIgnoreCase(stateObj.getUploadOperationType())) {
            
            editStateMaster(stateObj, validationMessages);

        } else if (CREATE.equalsIgnoreCase(stateObj.getUploadOperationType())) {
            
            createNewState(stateObj, validationMessages);
        }else {
            validationMessages.add(CoreUtility.prepareMessage("Invalid status code "+stateObj.getUploadOperationType(),
                    Message.MessageType.ERROR,"Acceptable status codes are  "+Arrays.asList(CREATE,EDIT,DELETE)));
            throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG)
            .setMessages(validationMessages).build();
        }
        return stateObj;
    }

    private void deleteStateMasterData(State stateObj, List<Message> validationMessages) {
        Map<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put("stateCode", stateObj.getStateCode());
        List<Integer> statusList = new ArrayList<Integer>();
        getApprovalStatusForDelete(statusList);
        variablesMap.put("masterLifeCycleData.approvalStatus", statusList);
        State orginalEntity = baseMasterService.findMasterByCode(State.class, variablesMap);
        
        if (orginalEntity == null) {
            statusList.add(ApprovalStatus.APPROVED_DELETED);
            statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
            variablesMap.put("masterLifeCycleData.approvalStatus", statusList);
            orginalEntity = baseMasterService.findMasterByCode(State.class, variablesMap);
            if(orginalEntity != null) {
                validationMessages.add(CoreUtility.prepareMessage("Record Already in progress for deletion", Message.MessageType.ERROR,
                        stateObj.getStateCode()));
                throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG)
                .setMessages(validationMessages).build();
            }else {
                validationMessages.add(CoreUtility.prepareMessage("Record does not exists", Message.MessageType.ERROR,
                        stateObj.getStateCode()));
                throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG)
                .setMessages(validationMessages).build();
            }
        }else {
            entityDao.detach(orginalEntity);
            User user = getCurrentUser().getUserReference();
            makerCheckerService.masterEntityMarkedForDeletion(orginalEntity,user.getEntityId());

        }
    }

    private void editStateMaster(State stateObj, List<Message> validationMessages) {
        State orginalEntity = validateAndAddMessageForEdit(stateObj, validationMessages);
        entityDao.detach(orginalEntity);
        if (CollectionUtils.isNotEmpty(validationMessages)) {
            throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG)
                    .setMessages(validationMessages).build();
        } else {
            List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<>();
            populateStateData(stateObj, orginalEntity, dataValidationRuleResults);
            if (!dataValidationRuleResults.isEmpty()) {
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder
                        .getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG)
                        .setMessages(validationMessages).build();
            } else {
                User user = getCurrentUser().getUserReference();
                if (user != null) {
                    makerCheckerService.masterEntityChangedByUser(orginalEntity, user);
                }
            }

        }

    }

    private State validateAndAddMessageForEdit(State stateObj, List<Message> validationMessages) {
        Map<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put("stateCode", stateObj.getStateCode());
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);        
        variablesMap.put("masterLifeCycleData.approvalStatus", statusList);        
        State orginalEntity = baseMasterService.findMasterByCode(State.class, variablesMap);
        if (orginalEntity == null) {
            variablesMap.clear();
            variablesMap.put("stateCode", stateObj.getStateCode());
            statusList.add(ApprovalStatus.APPROVED_DELETED);
            statusList.add(ApprovalStatus.APPROVED_MODIFIED);
            variablesMap.put("masterLifeCycleData.approvalStatus", statusList);
            orginalEntity = baseMasterService.findMasterByCode(State.class, variablesMap);
            if(orginalEntity != null) {
                validationMessages.add(CoreUtility.prepareMessage("Edit not allowed", Message.MessageType.ERROR,
                        stateObj.getStateCode()));
                throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG).setMessages(validationMessages).build();
            }else {
                validationMessages.add(CoreUtility.prepareMessage("Record does not exists", Message.MessageType.ERROR,
                        stateObj.getStateCode()));
                throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG).setMessages(validationMessages).build();
            }

        }else {
            initializeData(orginalEntity);
            validateAndAddMessage(stateObj, validationMessages,EDIT, orginalEntity);
        }
        return orginalEntity;
    }

    private void initializeData(State orginalEntity) {
        Hibernate.initialize(orginalEntity.getVehicleStateRegistraionMappings());
        Hibernate.initialize(orginalEntity.getCountry());
        Hibernate.initialize(orginalEntity.getRegion());
        Hibernate.initialize(orginalEntity.getVehicleStateRegistraionMappings());
        initializeReasonActivityMapping(orginalEntity);
    }

    private void initializeReasonActivityMapping(State orginalEntity) {
        Hibernate.initialize(orginalEntity.getReasonActInactMap());
        if(orginalEntity.getReasonActInactMap() != null) {
             Hibernate.initialize(orginalEntity.getReasonActInactMap().getMasterActiveInactiveReasons());
             List<MasterActiveInactiveReasons> masterActiveInactiveReasons = orginalEntity.getReasonActInactMap().getMasterActiveInactiveReasons();
             if(masterActiveInactiveReasons != null) {
                 for(MasterActiveInactiveReasons maActiveInactiveReasons : masterActiveInactiveReasons) {
                     Hibernate.initialize(maActiveInactiveReasons.getReasonActive());
                     Hibernate.initialize(maActiveInactiveReasons.getReasonInactive());
                 }
             }
             
        }
    }

    private void createNewState(State stateObj, List<Message> validationMessages) {
        State newState = new State();
        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<>();
        validateAndAddMessage(stateObj, validationMessages, "Create",null);
        if (CollectionUtils.isNotEmpty(validationMessages)) {
            throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG)
                    .setMessages(validationMessages).build();
        } else {
            populateStateData(stateObj, newState,dataValidationRuleResults);
            if(!dataValidationRuleResults.isEmpty()){
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, ERRORMSG, ERRORMSG)
                        .setMessages(validationMessages).build();
            }else {
                User user = getCurrentUser().getUserReference();
                if (user != null) {
                    newState.setReasonActInactMap(null);
                    makerCheckerService.masterEntityChangedByUser(newState, user);
                }
            }
        }
    }

    private void populateStateData(State stateObj, State newState,List<ValidationRuleResult> dataValidationRuleResults) {
        newState.setStateCode(stateObj.getStateCode());
        newState.setStateName(stateObj.getStateName());
        newState.setUnionTerritory(stateObj.getUnionTerritory());
        Country country = getApprovedEntity(Country.class, "countryISOCode", stateObj.getCountry().getCountryISOCode());
        newState.setCountry(country);
        newState.setRegion(getApprovedEntity(IntraCountryRegion.class, "intraRegionCode",stateObj.getRegion().getIntraRegionCode()));
               
        populatePinCodeRelatedData(stateObj, newState);
        
        if (CollectionUtils.isNotEmpty(stateObj.getVehicleStateRegistraionMappings())) {
            List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings = stateObj.getVehicleStateRegistraionMappings();
            List<VehicleStateRegistraionMapping> newVehicleStateRegistraionMappings = filterVehicleStateRegistrationMapping(
                    vehicleStateRegistraionMappings,newState);
            if(CollectionUtils.isNotEmpty(newState.getVehicleStateRegistraionMappings())) {
                newState.getVehicleStateRegistraionMappings().addAll(newVehicleStateRegistraionMappings);
            }else {
                newState.setVehicleStateRegistraionMappings(newVehicleStateRegistraionMappings);
            }
        }
        if(newState != null && newState.getId() != null && Arrays.asList(ApprovalStatus.APPROVED,ApprovalStatus.UNAPPROVED_MODIFIED).contains(newState.getApprovalStatus())) {
            setReasonActivityMappingData(newState, stateObj, dataValidationRuleResults);
        }
        newState.setActiveFlag(stateObj.isActiveFlag());
    }

    private void getApprovalStatusForApprovedEntity(List<Integer> statusList) {
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
    }

    private List<VehicleStateRegistraionMapping> filterVehicleStateRegistrationMapping(List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings,State orginalSate) {
        List<VehicleStateRegistraionMapping> newVehicleStateRegistraionMappings = new ArrayList<VehicleStateRegistraionMapping>();
        if(CollectionUtils.isNotEmpty(vehicleStateRegistraionMappings)) {
            for(VehicleStateRegistraionMapping vehicleStateRegistraionMapping : vehicleStateRegistraionMappings) {
                if((EDIT.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType()) || CREATE.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType())) 
                        && vehicleStateRegistraionMapping.getStateRTOCode() != null) {
                    vehicleStateRegistraionMapping.setStateRTOCode(vehicleStateRegistraionMapping.getStateRTOCode().toUpperCase());
                    newVehicleStateRegistraionMappings.add(vehicleStateRegistraionMapping);
                }else if(DELETE.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType()) && vehicleStateRegistraionMapping.getStateRTOCode() != null) {
                    populateStateRTOCodes(orginalSate, vehicleStateRegistraionMapping.getStateRTOCode());
                }
            }
        }
        return newVehicleStateRegistraionMappings;
    }
    private void populateStateRTOCodes(State orginalSate,String rtoCode) {
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings = orginalSate.getVehicleStateRegistraionMappings();
        if (CollectionUtils.isNotEmpty(vehicleStateRegistraionMappings)) {
            Iterator<VehicleStateRegistraionMapping> iterator = vehicleStateRegistraionMappings.iterator();
            while (iterator.hasNext()) {
                VehicleStateRegistraionMapping vehRegMap = iterator.next();
                if (rtoCode.equalsIgnoreCase(vehRegMap.getStateRTOCode())) {
                    iterator.remove();
                }
            }
        } 
    }
    private void populatePinCodeRelatedData(State stateObj, State newState) {
        if (!FALSE.equals(isPincodeValidation())) {
            boolean pinCodeType = stateObj.getPincodeType() != null ? stateObj.getPincodeType() : Boolean.FALSE;
            newState.setPincodeType(pinCodeType);
            String validationType = StringUtils.isNotBlank(stateObj.getValidationType()) ? stateObj.getValidationType()
                    : startsEndsWith;
            newState.setValidationType(validationType);
            newState.setPincodeStart(stateObj.getPincodeStart());
            newState.setPincodeEnd(stateObj.getPincodeEnd());
            newState.setPincodeRange(stateObj.getPincodeRange());   
            newState.setMinimumLength(stateObj.getMinimumLength());
            newState.setMaximumLength(stateObj.getMaximumLength());
        }
    }

    private void validateAndAddMessage(State stateObj, List<Message> validationMessages, String operation,State orginalState) {

        Map<String, Object> variablesMap = new HashMap<String, Object>();
        if (orginalState == null) {
            List<Integer> statusList = new ArrayList<Integer>();
            getApprovalStatusForAnyEntity(statusList);
            State state = null;
            if(stateObj.getStateCode() != null) {
                variablesMap.put("stateCode", stateObj.getStateCode());
                variablesMap.put("masterLifeCycleData.approvalStatus", statusList);
                state = baseMasterService.findMasterByCode(State.class, variablesMap);
                if (state != null) {
                    validationMessages.add(CoreUtility.prepareMessage("State Code already exist", Message.MessageType.ERROR,
                            stateObj.getStateCode()));
                }
            }
            if(stateObj.getStateName() != null) {
                variablesMap.clear();
                variablesMap.put("stateName", stateObj.getStateName());
                variablesMap.put("masterLifeCycleData.approvalStatus", statusList);
                state = baseMasterService.findMasterByCode(State.class, variablesMap);
                if (state != null) {
                    validationMessages.add(CoreUtility.prepareMessage("State Name already exists", Message.MessageType.ERROR,
                            stateObj.getStateName()));
                }
            }
            
        }else if(stateObj.getStateName() != null && orginalState != null) {
            variablesMap.clear();
            variablesMap.put("stateName", stateObj.getStateName());
            List<String> colNameList = baseMasterService.getDuplicateColumnNames(State.class, variablesMap, orginalState.getId());
            if(CollectionUtils.isNotEmpty(colNameList)) {
                validationMessages.add(CoreUtility.prepareMessage("State Name already exists",
                        Message.MessageType.ERROR, stateObj.getStateName()));
            }
        }

        String regexForStateCode = jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.state.code",
                "core.web.validation.config.customValidatorForStateCode");
        if (!validateRegex(stateObj.getStateName(), regexForStateCode)) {
            validationMessages.add(
                    CoreUtility.prepareMessage("Only alphabets and special characters .-& up to length thirty allowed", Message.MessageType.ERROR, stateObj.getStateName()));
        }
        if (!validateRegex(stateObj.getStateCode(), stateCodeRegx)) {
            validationMessages
                    .add(CoreUtility.prepareMessage("Only alphanumeric and underscore up to length eight is allowed",
                            Message.MessageType.ERROR, stateObj.getStateCode()));
        }

        validateCountryAndAddMessage(stateObj, validationMessages);
    
        validateIntraCountryRegionAndAddMessage(stateObj, validationMessages);
        if(!FALSE.equalsIgnoreCase(isPincodeValidation())) {
            validatePinCodeRelatedData(stateObj, validationMessages);
        }else{
            if(checkIfPincodeRelatedDataPresent(stateObj)) {
                validationMessages.add(CoreUtility.prepareMessage("Pincode related data not applicable",
                        Message.MessageType.ERROR, "Pincode related data not applicable"));
            }
        }

        validateStateRTOCodesAndAddMessage(stateObj, validationMessages,operation,orginalState);
        
        validateReasonActivityMapping(stateObj, validationMessages, operation, orginalState);

    }

    private boolean checkIfPincodeRelatedDataPresent(State stateObj) {
        return (checkPinCodeRange(stateObj)|| checkPinCodeStartEnd(stateObj)|| checkMaxMinPincodeLength(stateObj));
    }
    
    private boolean checkMaxMinPincodeLength(State stateObj) {
        return stateObj.getMaximumLength() != null || stateObj.getMinimumLength() != null;
    }
    private boolean checkPinCodeStartEnd(State stateObj) {
        return StringUtils.isNotBlank(stateObj.getPincodeStart()) || StringUtils.isNotBlank(stateObj.getPincodeEnd());
    }
    private boolean checkPinCodeRange(State stateObj) {
        return  StringUtils.isNotBlank(stateObj.getValidationType()) || StringUtils.isNotBlank(stateObj.getPincodeRange()) ;
    }
    
    private void getApprovalStatusForAnyEntity(List<Integer> statusList) {
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);        
    }
    
    private void getApprovalStatusForDelete(List<Integer> statusList) {
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
        statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);        
    }
    
    private void validateReasonActivityMapping(State stateObj, List<Message> validationMessages, String operation,
            State orginalState) {
        
        Boolean actInactFlag = false;
        Boolean actionAmbiguityFlag = false;
        Boolean checkForReasons = false;
        
        if("Create".equalsIgnoreCase(operation) && stateObj.getReasonActInactMap() != null) {
            
            validationMessages.add(CoreUtility.prepareMessage("ReasonsActiveInactiveMapping is not allowed in create",Message.MessageType.ERROR, " NA"));
        }
        if (EDIT.equalsIgnoreCase(operation) && stateObj.getReasonActInactMap() != null) {
            String masterName = stateObj.getClass().getSimpleName();
            String uniqueParameter = "stateCode";
            String uniqueValue = stateObj.getStateCode();
            if(CollectionUtils.isNotEmpty(stateObj.getReasonActInactMap().getMasterActiveInactiveReasons())) {
                validateStatusCodeForAciveInacReasonAndAddMessage(validationMessages, stateObj.getReasonActInactMap().getMasterActiveInactiveReasons());
            }
            
            actInactFlag = activeInactiveReasonService.checkForActiveInactiveForApprovedModified(stateObj.getReasonActInactMap(),masterName,uniqueParameter,uniqueValue);
            actionAmbiguityFlag = activeInactiveReasonService.checkForActionofReasons(stateObj.getReasonActInactMap());
            checkForReasons = activeInactiveReasonService.checkForGenericReasons(stateObj.getReasonActInactMap());
            if(stateObj.isActiveFlag() && stateObj.getReasonActInactMap() != null && INACTIVE.equalsIgnoreCase(stateObj.getReasonActInactMap().getTypeOfAction())){
                validationMessages.add(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action",
                        Message.MessageType.ERROR, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"}));
            }
            if(!stateObj.isActiveFlag() && stateObj.getReasonActInactMap() != null && ACTIVE.equalsIgnoreCase(stateObj.getReasonActInactMap().getTypeOfAction())){
                validationMessages.add(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", 
                        Message.MessageType.ERROR, new String[]{"Action cannot be ACTIVE when ActiveFlag is False"}));
            }
            if(stateObj.getReasonActInactMap() != null && !actionAmbiguityFlag){
                validationMessages.add(CoreUtility.prepareMessage("Reason is not provided for defined action", 
                        Message.MessageType.ERROR, ",Please provide reason for action:"+stateObj.getReasonActInactMap().getTypeOfAction()));

            }
            boolean flag = true;
            if (stateObj.getReasonActInactMap() != null && CollectionUtils.isNotEmpty(stateObj.getReasonActInactMap().getMasterActiveInactiveReasons()) && !actInactFlag) {
                validationMessages.add(CoreUtility.prepareMessage("No Reason Required", Message.MessageType.ERROR, "Please do not give Reason For this action"));
                flag = false;
            }
            if(stateObj.getReasonActInactMap() != null && !checkForReasons){
                validationMessages.add(CoreUtility.prepareMessage("Reason Code not correct", Message.MessageType.ERROR, "Provide correct reasons"));

            }
            if (flag && orginalState != null && orginalState.getId() != null 
                    && !Arrays.asList(ApprovalStatus.APPROVED,ApprovalStatus.UNAPPROVED_MODIFIED).contains(orginalState.getApprovalStatus())
                    && stateObj.getReasonActInactMap() != null) {
                validationMessages.add(CoreUtility.prepareMessage("No Reason Required", Message.MessageType.ERROR, "Please do not give Reason For this action"));
            }
            List<String> tobeDeletedActiveInactiveList = new ArrayList<>();
            List<String> tobeDeletedActiveInactiveListCopy = new ArrayList<>();

            List<String> tobeEditedInActiveInactiveList = new ArrayList<>();
            List<String> activeInactiveListInOrg = new ArrayList<>();
            List<String> activeInactiveListInOrgCopy = new ArrayList<>();


            List<MasterActiveInactiveReasons> masterActiveInactiveReasons = stateObj.getReasonActInactMap().getMasterActiveInactiveReasons();
            if(masterActiveInactiveReasons != null) {
                getTobeEditAndDeleteList(masterActiveInactiveReasons, tobeDeletedActiveInactiveList, tobeEditedInActiveInactiveList);
            }
            Set<String> sets = new HashSet<>(tobeEditedInActiveInactiveList);
            if(sets.size() < tobeEditedInActiveInactiveList.size()){
                validationMessages.add(CoreUtility.prepareMessage("Duplicate Reasons not allowed", Message.MessageType.ERROR, "Provide correct reasons"));
            }
            if(orginalState != null && orginalState.getReasonActInactMap() != null) {
                List<MasterActiveInactiveReasons> masterActiveInactiveReasonsInOrg = orginalState.getReasonActInactMap().getMasterActiveInactiveReasons();
                getTobeEditAndDeleteList(masterActiveInactiveReasonsInOrg, null, activeInactiveListInOrg);
            }
            activeInactiveListInOrgCopy.addAll(activeInactiveListInOrg);
            tobeDeletedActiveInactiveListCopy.addAll(tobeDeletedActiveInactiveList);
            if((activeInactiveListInOrg.size() == tobeDeletedActiveInactiveList.size()) && tobeEditedInActiveInactiveList.size() == 0 && tobeDeletedActiveInactiveList.size() >0) {
                validationMessages.add(CoreUtility.prepareMessage("At least one reasonsActiveInactiveMapping is required", Message.MessageType.ERROR, "can not delete all reasons"+tobeDeletedActiveInactiveList.toString()));
            }
            
            activeInactiveListInOrg.retainAll(tobeDeletedActiveInactiveList);
            if((CollectionUtils.isEmpty(activeInactiveListInOrg) && CollectionUtils.isNotEmpty(tobeDeletedActiveInactiveList)) ||(activeInactiveListInOrg.size() < tobeDeletedActiveInactiveList.size())) {
                tobeDeletedActiveInactiveList.removeAll(activeInactiveListInOrg);
                validationMessages.add(CoreUtility.prepareMessage("Reason does not exists in this record for delete", 
                        Message.MessageType.ERROR, tobeDeletedActiveInactiveList.toString()));
            }
            
            tobeEditedInActiveInactiveList.removeAll(tobeDeletedActiveInactiveListCopy);
            activeInactiveListInOrgCopy.retainAll(tobeEditedInActiveInactiveList);
            if(CollectionUtils.isNotEmpty(activeInactiveListInOrgCopy)) {
                validationMessages.add(CoreUtility.prepareMessage("Duplicate Reasons not allowed", Message.MessageType.ERROR,activeInactiveListInOrgCopy.toString()));
            }

        }   
        
    }

    private void validatePinCodeRelatedData(State stateObj, List<Message> validationMessages) {

        boolean pinCodeType = stateObj.getPincodeType() != null ? stateObj.getPincodeType() : Boolean.FALSE;
        String validationType = StringUtils.isBlank(stateObj.getValidationType()) ? startsEndsWith : stateObj.getValidationType();
        stateObj.setValidationType(validationType);
        if (pinCodeType) {
            if (range.equals(stateObj.getValidationType())) {
                validatePincodeRangeAndAddMessage(stateObj, validationMessages);
                if (StringUtils.isNotBlank(stateObj.getPincodeStart()) || StringUtils.isNotBlank(stateObj.getPincodeEnd())) {
                    validationMessages.add(CoreUtility.prepareMessage("PincodeStart pincodeEnd not allowed with validation type ",
                                    Message.MessageType.ERROR, stateObj.getValidationType()));
                }
            } else {
                validatePincodeStartEnd(stateObj, validationMessages);
            }
        
        } else {   
            
            if (range.equals(stateObj.getValidationType())) {
                validationMessages.add(CoreUtility.prepareMessage("Invalid validationType", Message.MessageType.ERROR,
                        stateObj.getValidationType()));
            } else {
                validatePincodeStartEnd(stateObj, validationMessages);
            }
        }

        if (stateObj.getMinimumLength() != null && stateObj.getMinimumLength() < 0) {
            validationMessages.add(CoreUtility.prepareMessage("Invalid MinimumLength value", Message.MessageType.ERROR,
                    stateObj.getMinimumLength().toString()));
        }
        if (stateObj.getMaximumLength() != null && stateObj.getMaximumLength() < 0) {
            validationMessages.add(CoreUtility.prepareMessage("Invalid MinimumLength value", Message.MessageType.ERROR,
                    stateObj.getMaximumLength().toString()));
        }
    }

    private void validatePincodeStartEnd(State stateObj, List<Message> validationMessages) {
        if (StringUtils.isNotBlank(stateObj.getPincodeRange())) {
            validationMessages.add(CoreUtility.prepareMessage("PincodeRange  not allowed with validation type ", Message.MessageType.ERROR,
                    stateObj.getValidationType()));
        }
        validatePincodeStartWithAndEndWithAndAddMessage(stateObj.getPincodeStart(), validationMessages,
                "Incorrect Pincode Start");
        validatePincodeStartWithAndEndWithAndAddMessage(stateObj.getPincodeEnd(), validationMessages,
                "Incorrect Pincode End");
    }

    private void validateStateRTOCodesAndAddMessage(State stateObj, List<Message> validationMessages,String operation,State orginalState) {
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings = stateObj.getVehicleStateRegistraionMappings();
        if (CollectionUtils.isNotEmpty(vehicleStateRegistraionMappings)) {
            
            validateStatusCodeAndAddMessage(validationMessages, vehicleStateRegistraionMappings);
            
            List<String> rtoCodes = getStateRTOCodes(stateObj);

            Set<String> sets = new HashSet<>(rtoCodes);
            if (sets.size() < rtoCodes.size()) {
                validationMessages.add(CoreUtility.prepareMessage("Duplicate state  rto code", Message.MessageType.ERROR,
                        rtoCodes.toString()));
            }
            List<String> rtoCodesFromOrginal = getStateRTOCodes(orginalState);
            List<String> rtoCodesFromOrginalCopy = new ArrayList<String>();
            rtoCodesFromOrginalCopy.addAll(rtoCodesFromOrginal);
            List<String> markDeletedrtoCodes  = getDeletedStateRTOCodes(stateObj);
            
            rtoCodes.removeAll(markDeletedrtoCodes); // delete a code and add same code
            rtoCodesFromOrginal.retainAll(rtoCodes);
            if(CollectionUtils.isNotEmpty(rtoCodesFromOrginal)) {
                validationMessages.add(CoreUtility.prepareMessage("Duplicate state  rto code", Message.MessageType.ERROR,
                        rtoCodesFromOrginal.toString()));
            }
            //if sto code does not exists for delete
            
            rtoCodesFromOrginalCopy.retainAll(markDeletedrtoCodes);
            if((CollectionUtils.isEmpty(rtoCodesFromOrginalCopy) && CollectionUtils.isNotEmpty(markDeletedrtoCodes)) || (rtoCodesFromOrginalCopy.size() < markDeletedrtoCodes.size())) {
                markDeletedrtoCodes.removeAll(rtoCodesFromOrginalCopy);
                validationMessages.add(CoreUtility.prepareMessage("state  rto code does not exists", Message.MessageType.ERROR,
                        markDeletedrtoCodes.toString()));
            }
            
            for(String rtoCode : rtoCodes) {
                if(rtoCode.length() > stateRtoLength) {
                    validationMessages.add(CoreUtility.prepareMessage("state rto code length can not be greater than "+stateRtoLength, Message.MessageType.ERROR,rtoCode));
                }
                if(!validateRegex(rtoCode, stateRtoCodeRegx)) {
                    validationMessages.add(CoreUtility.prepareMessage("Only letters allowed in state rto code", Message.MessageType.ERROR,rtoCode));
                }
            }
            Long stateId = null;
            if(EDIT.equalsIgnoreCase(operation)) {
                stateId = orginalState.getId();
            }
            
            Map<String, List<String>> duplicateStateRTOCode = vehicleStateService
                    .checkForDuplicateStateRTOCode(stateId, rtoCodes);
            String errorMessage = prepareDataforDuplicateStateRTOCode(duplicateStateRTOCode);
            if (errorMessage != null) {
                validationMessages.add(CoreUtility.prepareMessage(errorMessage, Message.MessageType.ERROR, errorMessage));
            }
        }
    }

    private void validateStatusCodeAndAddMessage(List<Message> validationMessages,List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings) {
        for(VehicleStateRegistraionMapping vehicleStateRegistraionMapping : vehicleStateRegistraionMappings) {
            if(!(CREATE.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType()) || EDIT.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType())
                    || DELETE.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType()) )) {
                validationMessages.add(CoreUtility.prepareMessage("Invalid status code "+vehicleStateRegistraionMapping.getUploadOperationType(),
                        Message.MessageType.ERROR,"Acceptable status codes are  "+Arrays.asList(CREATE,EDIT,DELETE)));
            }
        }
    }
    private void validateStatusCodeForAciveInacReasonAndAddMessage(List<Message> validationMessages,List<MasterActiveInactiveReasons> masterActiveInactiveReasons) {
        for(MasterActiveInactiveReasons masterActiveInactiveReason : masterActiveInactiveReasons) {
            if(!(CREATE.equalsIgnoreCase(masterActiveInactiveReason.getUploadOperationType()) || EDIT.equalsIgnoreCase(masterActiveInactiveReason.getUploadOperationType()) 
                    || DELETE.equalsIgnoreCase(masterActiveInactiveReason.getUploadOperationType()) )) {
                validationMessages.add(CoreUtility.prepareMessage("Invalid status code "+masterActiveInactiveReason.getUploadOperationType(),
                        Message.MessageType.ERROR,"Acceptable status codes are  "+Arrays.asList(CREATE,EDIT,DELETE)));
            }
        }
    }
    private List<String> getStateRTOCodes(State state) {
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings = state !=null?state.getVehicleStateRegistraionMappings():null;
        List<String> stateRTOCodes = new ArrayList<String>();
        if(CollectionUtils.isNotEmpty(vehicleStateRegistraionMappings)) {
            for(VehicleStateRegistraionMapping vehicleStateRegistraionMapping : vehicleStateRegistraionMappings) {
                if(vehicleStateRegistraionMapping.getStateRTOCode() != null && !DELETE.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType())) {
                    stateRTOCodes.add(vehicleStateRegistraionMapping.getStateRTOCode().toUpperCase().trim());
                }
            }
        }
        return stateRTOCodes;
    }
    private List<String> getDeletedStateRTOCodes(State state) {
        List<VehicleStateRegistraionMapping> vehicleStateRegistraionMappings = state !=null?state.getVehicleStateRegistraionMappings():null;
        List<String> stateRTOCodes = new ArrayList<String>();
        if(CollectionUtils.isNotEmpty(vehicleStateRegistraionMappings)) {
            for(VehicleStateRegistraionMapping vehicleStateRegistraionMapping : vehicleStateRegistraionMappings) {
                if(vehicleStateRegistraionMapping.getStateRTOCode() != null && DELETE.equalsIgnoreCase(vehicleStateRegistraionMapping.getUploadOperationType())) {
                    stateRTOCodes.add(vehicleStateRegistraionMapping.getStateRTOCode().toUpperCase().trim());
                }
            }
        }
        return stateRTOCodes;
    }
    private void validatePincodeStartWithAndEndWithAndAddMessage(String pinCodeRange, List<Message> validationMessages,
            String errorMessage) {
        if (StringUtils.isNotBlank(pinCodeRange)) {
            String[] records = pinCodeRange.split(",");
            boolean isValid = true;
            for (String record : records) {
                if (!validateRegex(record, pinCodeStart)) {
                    isValid = false;
                    break;
                }
            }
            if (!isValid) {
                validationMessages.add(CoreUtility.prepareMessage(errorMessage, Message.MessageType.ERROR, pinCodeRange));
            }
        }
    }

    private void validatePincodeRangeAndAddMessage(State stateObj, List<Message> validationMessages) {
        if (StringUtils.isNotBlank(stateObj.getPincodeRange())) {
            String[] ranges = stateObj.getPincodeRange().split(",");
            boolean flag = true;
            for (String rangeValue : ranges) {
                if (validateRegex(rangeValue, pincodeRangeRegx)) {
                    String[] split = rangeValue.split("-");
                    if (new Long(split[0]).compareTo(new Long(split[1])) >= 0) {
                        flag = false;
                        break;
                    }
                } else {
                    flag = false;
                    break;
                }
            }
            if (!flag) {
                validationMessages.add(CoreUtility.prepareMessage("Incorrect Pincode Range", Message.MessageType.ERROR,
                        stateObj.getPincodeRange()));
            }
        }
    }

    private void validateCountryAndAddMessage(State stateObj, List<Message> validationMessages) {
        if (stateObj.getCountry() != null && stateObj.getCountry().getCountryISOCode() != null) {
            Country country = getApprovedEntity(Country.class,"countryISOCode",stateObj.getCountry().getCountryISOCode());
            if (country == null) {
                validationMessages.add(CoreUtility.prepareMessage("Country code does not exists", Message.MessageType.ERROR,
                        stateObj.getStateCode()));
            }
        } else {
            validationMessages.add(CoreUtility.prepareMessage("Invalid country ISOCode", Message.MessageType.ERROR, "Invalid country ISOCode"));
        }
    }

    private void validateIntraCountryRegionAndAddMessage(State stateObj, List<Message> validationMessages) {
        if (stateObj.getRegion() != null && StringUtils.isNotBlank(stateObj.getRegion().getIntraRegionCode())) {
            IntraCountryRegion intraCountryRegion = getApprovedEntity(IntraCountryRegion.class,"intraRegionCode",stateObj.getRegion().getIntraRegionCode());
            if (intraCountryRegion == null) {
                validationMessages.add(CoreUtility.prepareMessage("Region code does not exists", Message.MessageType.ERROR,
                        stateObj.getRegion().getIntraRegionCode()));
            }
            if (intraCountryRegion != null && intraCountryRegion.getCountry() != null) {
                Hibernate.initialize(intraCountryRegion.getCountry());
                if (stateObj.getCountry() != null && !stateObj.getCountry().getCountryISOCode()
                        .equals(intraCountryRegion.getCountry().getCountryISOCode())) {
                    validationMessages.add(CoreUtility.prepareMessage("Region code does not mapped with given country",
                            Message.MessageType.ERROR, stateObj.getRegion().getIntraRegionCode()));
                }
            }

        } else {
            validationMessages.add(CoreUtility.prepareMessage("Invalid region code", Message.MessageType.ERROR, "Invalid region code"));
        }
    }

    private boolean validateRegex(String fieldValue, String regex) {
        if (fieldValue != null) {
            return fieldValue.matches(regex);
        }
        return false;
    }

    private String prepareDataforDuplicateStateRTOCode(Map<String, List<String>> duplicateStateRTOCode) {
        if (MapUtils.isNotEmpty(duplicateStateRTOCode)) {
            StringBuilder prepareRtoString = new StringBuilder();
            prepareRtoString.append("Sate RTO codes ");
            StringBuilder prepareStateString = new StringBuilder();
            int count = 0;
            for (Entry<String, List<String>> entry : duplicateStateRTOCode.entrySet()) {
                if (count > 0) {
                    prepareRtoString.append(" , ");
                    prepareStateString.append(" , ");
                }
                String listInString = entry.getValue() != null ? String.join(",", entry.getValue()) : null;
                prepareRtoString.append(listInString);
                prepareStateString.append(entry.getKey());
                count++;
            }
            if (count > 1) {
                prepareRtoString.append(" Already exists in state respectively ").append(prepareStateString.toString());

            } else {
                prepareRtoString.append(" Already exists in state ").append(prepareStateString.toString());
            }
            return prepareRtoString.toString();
        }
        return null;
    }

    private String isPincodeValidation() {
        String pincodeValidation = FALSE;
        String moduleCode = "";
        if (ProductInformationLoader.productInfoExists()) {
            moduleCode = ProductInformationLoader.getProductCode();
            pincodeValidation = configurationService.getPropertyValueByPropertyKey(
                    "custom.pincode.validation." + moduleCode.toLowerCase(), CONFIGURATION_QUERY);
            if (pincodeValidation == null) {
                pincodeValidation = FALSE;
            }
            pincodeValidation = pincodeValidation.toLowerCase();
        }
        return pincodeValidation;
    }
    public void setReasonActivityMappingData(State recordToUpdate, State stateUploadedObj, List<ValidationRuleResult> dataValidationRuleResults) {
        
        List<MasterActiveInactiveReasons> mstActInactReasList = new ArrayList<>();
        List<ReasonActive> activeReasonList = new ArrayList<>();
        List<ReasonInActive> InactiveReasonList = new ArrayList<>();
        
        if (stateUploadedObj.getReasonActInactMap() != null && stateUploadedObj.getReasonActInactMap().getMasterActiveInactiveReasons() != null){
            stateUploadedObj.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> InactiveReasonList.add(m.getReasonInactive()));
            stateUploadedObj.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> activeReasonList.add(m.getReasonActive()));
        }

        filterReasonActivityMapping(recordToUpdate,stateUploadedObj); 
        if(stateUploadedObj.getReasonActInactMap() != null){
            removeDeletedRecord(stateUploadedObj.getReasonActInactMap().getMasterActiveInactiveReasons());
        }
        if(CollectionUtils.isNotEmpty(activeReasonList) || CollectionUtils.isNotEmpty(InactiveReasonList)) {
           
            mstActInactReasList = activeInactiveReasonService.getMasterReasonList(mstActInactReasList, stateUploadedObj.getReasonActInactMap().getMasterActiveInactiveReasons(), dataValidationRuleResults);

        } else if(recordToUpdate.getReasonActInactMap() != null && recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons() != null
                && stateUploadedObj.getReasonActInactMap() != null && isActiveInactiveMappingValid(recordToUpdate, stateUploadedObj)) {
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            if(CollectionUtils.isEmpty(mstActInactReasList)) {
                mstActInactReasList.add(new MasterActiveInactiveReasons());
            }
        } else if(stateUploadedObj.getReasonActInactMap() == null && recordToUpdate.getReasonActInactMap()!=null){
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            stateUploadedObj.setReasonActInactMap(recordToUpdate.getReasonActInactMap());
            if(CollectionUtils.isEmpty(mstActInactReasList))
                mstActInactReasList.add(new MasterActiveInactiveReasons());
        } else{
            mstActInactReasList.add(new MasterActiveInactiveReasons());
            if(stateUploadedObj.getReasonActInactMap() == null) {
                stateUploadedObj.setReasonActInactMap(new ReasonsActiveInactiveMapping());
            }
        }

        stateUploadedObj.getReasonActInactMap().setMasterActiveInactiveReasons(mstActInactReasList);
        
        if(recordToUpdate.getReasonActInactMap() != null && recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons() !=null 
                && (recordToUpdate.isActiveFlag() == stateUploadedObj.isActiveFlag())) {
            stateUploadedObj.getReasonActInactMap().getMasterActiveInactiveReasons().addAll(recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons());
        }
        
        recordToUpdate.setReasonActInactMap(stateUploadedObj.getReasonActInactMap());
        
        if (stateUploadedObj.getReasonActInactMap() != null && ACTIVE.equalsIgnoreCase(stateUploadedObj.getReasonActInactMap().getTypeOfAction())) {
            recordToUpdate.getReasonActInactMap().setTypeOfAction(ACTIVE);
        }
        if (stateUploadedObj.getReasonActInactMap() != null && INACTIVE.equalsIgnoreCase(stateUploadedObj.getReasonActInactMap().getTypeOfAction())) {
            recordToUpdate.getReasonActInactMap().setTypeOfAction(INACTIVE);
        }
        if(recordToUpdate.getReasonActInactMap().getTypeOfAction() == null) {
            if(stateUploadedObj.isActiveFlag()) {
                recordToUpdate.getReasonActInactMap().setTypeOfAction(ACTIVE);
            }else {
                recordToUpdate.getReasonActInactMap().setTypeOfAction(INACTIVE);
            }
        }

    }

    private void removeDeletedRecord(List<MasterActiveInactiveReasons> mstActInactReasList) {
        if (mstActInactReasList != null) {
            Iterator<MasterActiveInactiveReasons> iterator = mstActInactReasList.iterator();
            while (iterator.hasNext()) {
                MasterActiveInactiveReasons activeInactiveReg = iterator.next();
                if (DELETE.equalsIgnoreCase(activeInactiveReg.getUploadOperationType())) {
                    iterator.remove();
                }
            }
        }
    }

    private void filterReasonActivityMapping(State recordToUpdate,State stateUploadedObj) {
        List<String> tobeDeletedActiveInactiveList = new ArrayList<>();
        List<String> tobeEditedInActiveInactiveList = new ArrayList<>();
        if (recordToUpdate != null && recordToUpdate.getReasonActInactMap() != null && stateUploadedObj.getReasonActInactMap() != null
                && CollectionUtils.isNotEmpty(recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons())) {
            List<MasterActiveInactiveReasons> masterActiveInactiveReasons = stateUploadedObj.getReasonActInactMap().getMasterActiveInactiveReasons();
            if(CollectionUtils.isNotEmpty(masterActiveInactiveReasons)) {
                getTobeEditAndDeleteList(masterActiveInactiveReasons, tobeDeletedActiveInactiveList,
                        tobeEditedInActiveInactiveList);
                removeRecord(tobeDeletedActiveInactiveList, recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons());
            }
        }
    }

    private void removeRecord(List<String> tobeDeletedActiveInactiveList,
            List<MasterActiveInactiveReasons> masterActiveInactiveReasons) {
        if (CollectionUtils.isNotEmpty(masterActiveInactiveReasons)) {
            Iterator<MasterActiveInactiveReasons> iterator = masterActiveInactiveReasons.iterator();
            while (iterator.hasNext()) {
                MasterActiveInactiveReasons activeInactiveReg = iterator.next();
                if (activeInactiveReg.getReasonActive() != null
                        && tobeDeletedActiveInactiveList.contains(activeInactiveReg.getReasonActive().getCode())) {
                    iterator.remove();
                } else if (activeInactiveReg.getReasonInactive() != null
                        && tobeDeletedActiveInactiveList.contains(activeInactiveReg.getReasonInactive().getCode())) {
                    iterator.remove();
                }
               if(activeInactiveReg.getReasonActive() == null && activeInactiveReg.getReasonInactive() == null) {
                   iterator.remove();
               }
            }
        }
    }

    private void getTobeEditAndDeleteList(List<MasterActiveInactiveReasons> masterActiveInactiveReasons,
            List<String> tobeDeletedActiveInactiveList, List<String> tobeEditedInActiveInactiveList) {
        for(MasterActiveInactiveReasons activeInactiveReasons : masterActiveInactiveReasons) {
            if(DELETE.equalsIgnoreCase(activeInactiveReasons.getUploadOperationType())) {
                if(activeInactiveReasons.getReasonActive() !=null && StringUtils.isNotBlank(activeInactiveReasons.getReasonActive().getCode())) {
                    tobeDeletedActiveInactiveList.add(activeInactiveReasons.getReasonActive().getCode());
                }else if(activeInactiveReasons.getReasonInactive() !=null && StringUtils.isNotBlank(activeInactiveReasons.getReasonInactive().getCode())) {
                    tobeDeletedActiveInactiveList.add(activeInactiveReasons.getReasonInactive().getCode());
                }
            }else {
                if(activeInactiveReasons.getReasonActive() !=null && StringUtils.isNotBlank(activeInactiveReasons.getReasonActive().getCode())) {
                    tobeEditedInActiveInactiveList.add(activeInactiveReasons.getReasonActive().getCode());
                }else if(activeInactiveReasons.getReasonInactive() !=null && StringUtils.isNotBlank(activeInactiveReasons.getReasonInactive().getCode())) {
                    tobeEditedInActiveInactiveList.add(activeInactiveReasons.getReasonInactive().getCode());
                }
            }
        }
    }

    private boolean isActiveInactiveMappingValid(State recordToUpdate, State stateUploadedObj) {
        return (ACTIVE.equalsIgnoreCase(stateUploadedObj.getReasonActInactMap().getTypeOfAction())
        && ACTIVE.equalsIgnoreCase(recordToUpdate.getReasonActInactMap().getTypeOfAction())) || 
                ( INACTIVE.equalsIgnoreCase(stateUploadedObj.getReasonActInactMap().getTypeOfAction())
        && (INACTIVE.equalsIgnoreCase(recordToUpdate.getReasonActInactMap().getTypeOfAction())));
    }
    private <T extends BaseMasterEntity> T getApprovedEntity(Class<T> entityClass, String key,String value) {
        Map<String, Object> variablesMap = new HashMap<String, Object>();
        variablesMap.put(key, value);
        List<Integer> statusList = new ArrayList<Integer>();
        getApprovalStatusForApprovedEntity(statusList);
        return baseMasterService.findMasterByCode(entityClass,variablesMap);
    }
}
