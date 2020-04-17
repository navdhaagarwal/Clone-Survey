package com.nucleus.web.district.service;


import com.nucleus.jsMessageResource.service.JsMessageResourceService;
import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.address.District;
import com.nucleus.address.State;
import com.nucleus.cas.parentChildDeletionHandling.BaseMasterDependency;
import com.nucleus.core.actInactReasService.ActiveInactiveReasonService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.web.common.controller.CASValidationUtils;
import com.nucleus.web.district.vo.DistrictVO;
import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;


@Named("districtUploadBusinessObj")
public class DistrictUploadBusinessObj extends BaseServiceImpl implements IDistrictUploadBusinessObj {

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("activeInactiveReasonService")
    private ActiveInactiveReasonService activeInactiveReasonService;


    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    @Autowired
    private MessageSource messageSource;

    public DistrictVO uploadDistrict(DistrictVO districtVO) {
//        System.out.println(districtVO);
        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<>();

        if (districtVO.getStatus() != null) {

            if (districtVO.getStatus().equalsIgnoreCase("Delete")) {
                if (districtVO.getDistrictCode() != null && !districtVO.getDistrictCode().trim().isEmpty()) {
                    District deletedrecordDetails = findRecord(districtVO);
                    if (deletedrecordDetails != null) {
                        if (deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.UNAPPROVED_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED_IN_PROGRESS) {
                            if(!BaseMasterDependency.isDependencyPresent(deletedrecordDetails.getClass(),deletedrecordDetails.getId())) {
                                entityDao.detach(deletedrecordDetails);
                                User user1 = getCurrentUser().getUserReference();
                                EntityId updatedById = user1.getEntityId();
                                makerCheckerService.masterEntityMarkedForDeletion(deletedrecordDetails, updatedById);
                            }else{
                                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record " + deletedrecordDetails.getDistrictCode() +
                                        " is being used by a parent Master",Message.MessageType.ERROR,"Check usage section under activity"));
                                dataValidationRuleResults.add(validationRuleResult);
                            }
                        } else {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Either Already Deleted or Already marked for Deletion.", Message.MessageType.ERROR, "Check the District Code"));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                    } else {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR, "Check the District Code"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR, "Check the District Code"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (!dataValidationRuleResults.isEmpty()) {
                    List<Message> validationMessages = new ArrayList<Message>();
                    for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                        validationMessages.add(validationRuleResult.getI18message());
                    }
                    throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in District Upload", "Error in District Upload").setMessages(validationMessages).build();
                }
            }

            if (districtVO.getStatus().equalsIgnoreCase("Edit")) {
                District recordToUpdate = findRecord(districtVO);
                Boolean actInactFlag = false;
                Boolean actionAmbiguityFlag = false;
                Boolean checkForReasons = false;
                Boolean checkForDuplicateReasons = false;
                if (recordToUpdate != null) {
                    String masterName = recordToUpdate.getClass().getSimpleName();
                    String uniqueParameter = "districtCode";
                    String uniqueValue = districtVO.getDistrictCode();
                    if(districtVO.getReasonActInactMap()!=null){
                        actInactFlag = activeInactiveReasonService.checkForActiveInactiveForApprovedModified(districtVO.getReasonActInactMap(),masterName,uniqueParameter,uniqueValue);
                        actionAmbiguityFlag = activeInactiveReasonService.checkForActionofReasons(districtVO.getReasonActInactMap());
                        checkForReasons = activeInactiveReasonService.checkForGenericReasons(districtVO.getReasonActInactMap());
                        checkForDuplicateReasons = activeInactiveReasonService.checkForDuplicateReasons(districtVO.getReasonActInactMap());
                        if(districtVO.isActiveFlag() && districtVO.getReasonActInactMap() != null && districtVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("INACTIVE")){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"}));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        if(!districtVO.isActiveFlag() && districtVO.getReasonActInactMap() != null && districtVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("ACTIVE")){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be ACTIVE when ActiveFlag is False"}));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        if(districtVO.getReasonActInactMap() != null && !actionAmbiguityFlag){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason is not provided for defined action", Message.MessageType.ERROR, ",Please provide reason for action:"+districtVO.getReasonActInactMap().getTypeOfAction()));
                            dataValidationRuleResults.add(validationRuleResult);

                        }
                        if (districtVO.getReasonActInactMap() != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(districtVO.getReasonActInactMap().getMasterActiveInactiveReasons()) && !actInactFlag) {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("No Reason Required", Message.MessageType.ERROR, "Please do not give Reason For this action"));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        if(districtVO.getReasonActInactMap() != null && !checkForReasons){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason Code not correct", Message.MessageType.ERROR, "Provide correct reasons"));
                            dataValidationRuleResults.add(validationRuleResult);

                        }if(!checkForDuplicateReasons){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate Reasons not allowed", Message.MessageType.ERROR, "Provide correct reasons"));
                            dataValidationRuleResults.add(validationRuleResult);

                        }
                        if (actInactFlag && actionAmbiguityFlag && checkForDuplicateReasons && checkForReasons ) {
                            saveReasonForApprovedRecord(recordToUpdate, districtVO, dataValidationRuleResults);
                        }
                    }
                    Hibernate.initialize(recordToUpdate.getState());
                    if(districtVO.getReasonActInactMap()!=null){
                        Hibernate.initialize(districtVO.getReasonActInactMap().getMasterActiveInactiveReasons());
                    }
                    entityDao.detach(recordToUpdate);
                    validateDistrict(districtVO, dataValidationRuleResults);
                    if (!dataValidationRuleResults.isEmpty()) {
                        List<Message> validationMessages = new ArrayList<Message>();
                        for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                            validationMessages.add(validationRuleResult.getI18message());
                        }
                        throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in District Upload", "Error in District Upload").setMessages(validationMessages).build();
                    }
                    copyVOToMaster(recordToUpdate, districtVO);
                    User user1 = getCurrentUser().getUserReference();
                    if (recordToUpdate.getId() != null && user1 != null) {
                        entityDao.detach(recordToUpdate);
                        makerCheckerService.masterEntityChangedByUser(recordToUpdate, user1);
                    }
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record not found, Check Code ", Message.MessageType.ERROR, "Code is a mandatory Field"));
                    dataValidationRuleResults.add(validationRuleResult);

                }
                if (!dataValidationRuleResults.isEmpty()) {
                    List<Message> validationMessages = new ArrayList<Message>();
                    for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                        validationMessages.add(validationRuleResult.getI18message());
                    }
                    throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in District Upload", "Error in District Upload").setMessages(validationMessages).build();
                }

            }
        } else {
            District district = new District();
            validateDistrict(districtVO, dataValidationRuleResults);


            if (districtVO.getReasonActInactMap() != null) {
                boolean flag = false;
                if (districtVO.isActiveFlag() && districtVO.getReasonActInactMap() != null && districtVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("INACTIVE")) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"}));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (!districtVO.isActiveFlag() && districtVO.getReasonActInactMap() != null && districtVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("ACTIVE")) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be ACTIVE when ActiveFlag is False"}));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (districtVO.getReasonActInactMap().getTypeOfAction() != null && districtVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active"))
                    district.setActiveFlag(true);
                if (districtVO.getReasonActInactMap() != null && districtVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")) {
                    district.setActiveFlag(false);
                }
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(districtVO.getReasonActInactMap().getMasterActiveInactiveReasons())) {
                    List<MasterActiveInactiveReasons> result = districtVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                            .filter(m -> ((m.getReasonInactive() != null && m.getReasonInactive().getCode() != null) || (m.getReasonActive() != null && m.getReasonActive().getCode() != null))).collect(Collectors.toList());
                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(result)) {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Active/Inactive Reason is not required for new record,It is required only for approved or approved modified record", Message.MessageType.ERROR, "Please do not provide active/inactive reasons"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    if (org.apache.commons.collections.CollectionUtils.isEmpty(result)) {
                        List<MasterActiveInactiveReasons> resultDesc = districtVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                                .filter(m -> (m.getDescription() != null)).collect(Collectors.toList());
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(resultDesc)) {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Description for Active/Inactive Reason not Required", Message.MessageType.ERROR, "Please do not provide active/inactive reasons description"));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                    }
                }
            }
            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in District Upload", "Error in District Upload").setMessages(validationMessages).build();
            }
            copyVOToMaster(district, districtVO);
            User user1 = getCurrentUser().getUserReference();
            district.markActive();
            if (district.getId() == null && user1 != null) {
                makerCheckerService.masterEntityChangedByUser(district, user1);
            }

        }

        return districtVO;
    }

    private void copyVOToMaster(District recordToUpdate, DistrictVO districtVO) {
//        if(districtVO.getStatus()==null)
        recordToUpdate.setDistrictCode(districtVO.getDistrictCode());
        recordToUpdate.setDistrictName(districtVO.getDistrictName());
        recordToUpdate.setDistrictAbbreviation(districtVO.getDistrictAbbreviation());
        recordToUpdate.setState(findStateRecord(districtVO.getState().getStateCode()));
        if(null != districtVO.getReasonActInactMap()){
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsLists = districtVO.getReasonActInactMap().getMasterActiveInactiveReasons();
            if(null != districtVO.getReasonActInactMap().getMasterActiveInactiveReasons() && masterActiveInactiveReasonsLists.size() > 0){
                recordToUpdate.setReasonActInactMap(districtVO.getReasonActInactMap());
            }
        }
    }



    private void validateDistrict(DistrictVO districtVO, List dataValidationRuleResults) {
        validateDistrictName(districtVO, dataValidationRuleResults);
        validateDistrictCode(districtVO, dataValidationRuleResults);
        validateDistrictAbbreviation(districtVO, dataValidationRuleResults);
        validateDistrictState(districtVO, dataValidationRuleResults);
    }

    private void validateDistrictState(DistrictVO districtVO, List dataValidationRuleResults) {
        if (districtVO.getState()!=null && districtVO.getState().getStateCode() != null) {
            State state = new State();
            state.setStateCode(districtVO.getState().getStateCode());
            checkForValidState(state, dataValidationRuleResults);
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("State cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
        }
    }

    private void validateDistrictAbbreviation(DistrictVO districtVO, List dataValidationRuleResults) {
        if (districtVO.getDistrictAbbreviation() != null && !districtVO.getDistrictAbbreviation().trim().isEmpty()) {
            checkForValidAbbreviation(districtVO.getDistrictAbbreviation(), dataValidationRuleResults);
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District Abbreviation cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
        }
    }

    private void validateDistrictCode(DistrictVO districtVO, List dataValidationRuleResults) {
        if (districtVO.getDistrictCode() != null && !districtVO.getDistrictCode().trim().isEmpty()) {
            if (districtVO.getStatus() == null) {
                if (checkForDuplicateCode(districtVO.getDistrictCode())) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District Code already exists", Message.MessageType.ERROR, districtVO.getDistrictCode())));
                }
            } else {
                if (!checkForDuplicateCode(districtVO.getDistrictCode())) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District Code doesn't exist  ", Message.MessageType.ERROR, districtVO.getDistrictCode())));
                }
            }
            checkForValidCode(districtVO.getDistrictCode(), dataValidationRuleResults);
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District Code cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
        }
    }

    private void validateDistrictName(DistrictVO districtVO, List dataValidationRuleResults) {
        if (districtVO.getDistrictName() != null && !districtVO.getDistrictName().trim().isEmpty()) {
            if (districtVO.getStatus() == null) {
                checkForDuplicateName(districtVO.getDistrictName(), dataValidationRuleResults);
            } else {
                if (!checkForNameAndCode(districtVO)) {
                    checkForDuplicateName(districtVO.getDistrictName(), dataValidationRuleResults);
                }
            }
            checkForValidName(districtVO.getDistrictName(), dataValidationRuleResults);
        }
        else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District Name cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
        }
    }

    private boolean checkForNameAndCode(DistrictVO districtVO) {
        String dName = districtVO.getDistrictName(), dCode = districtVO.getDistrictCode();
        District districtRetrieved = findRecord(districtVO);
        if (districtRetrieved.getDistrictName().equals(dName))
            return true;
        return false;
    }

    private District findRecord(DistrictVO districtVO) {
        HashMap districtMap = new HashMap<String, Object>();
        districtMap.put("districtCode", districtVO.getDistrictCode());
        if(districtVO.getDistrictCode()!=null)
        	return (findDistrictByCode(districtVO.getDistrictCode()));
        else
            return null;
    }

    private District findDistrictByCode(String districtCode) {
        NamedQueryExecutor<District> executor = new NamedQueryExecutor<District>("DistrictMaster.findDistrictByCode")
                .addParameter("districtCode", districtCode)
                .addParameter("approvalStatus", Arrays.asList(1, 2, 3, 5, 10));
        List<District> districts = entityDao.executeQuery(executor);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(districts)) {
            return districts.get(0);
        }
        return null;
    }

    private State findStateRecord(String stateCode) {
        HashMap stateMap = new HashMap<String, Object>();
        stateMap.put("stateCode", stateCode);
        return (findStateByCode(stateCode));
    }

    private State findStateByCode(String stateCode) {
        NamedQueryExecutor<State> executor = new NamedQueryExecutor<State>("DistrictMaster.findStateByCode")
                .addParameter("stateCode", stateCode)
                .addParameter("approvalStatus", Arrays.asList(ApprovalStatus.APPROVED, ApprovalStatus.APPROVED_MODIFIED));
        List<State> states = entityDao.executeQuery(executor);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(states)) {
            return states.get(0);
        }
        return null;
    }


    private boolean checkForDuplicateCode(String districtCode) {
        return baseMasterService.hasEntity(District.class, "districtCode", districtCode);

    }

    private void checkForDuplicateName(String districtName, List dataValidationRuleResults) {
        if (baseMasterService.hasEntity(District.class, "districtName", districtName))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District Name already exists", Message.MessageType.ERROR, districtName)));
    }

    private void checkForValidState(State state, List dataValidationRuleResults) {
        if (findStateByCode(state.getStateCode()) == null)
//        if (!baseMasterService.hasEntity(State.class, "stateCode", state.getStateCode()))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("State doesn't exist", Message.MessageType.ERROR, state.getStateCode())));


    }

    private void checkForValidCode(String districtCode, List dataValidationRuleResults) {
        if (!CASValidationUtils.isAlphaNumericAndUnderScore(districtCode))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric value and underscore allowed for Code", Message.MessageType.ERROR, districtCode)));

    }

    private void checkForValidAbbreviation(String districtAbbreviation, List dataValidationRuleResults) {
        if (!CASValidationUtils.isAlphaNumeric(districtAbbreviation))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric value allowed for Abbreviation", Message.MessageType.ERROR, districtAbbreviation)));

    }


    private void checkForValidName(String districtName, List dataValidationRuleResults) {
        String regexForDistrictName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.district.name","core.web.validation.config.customValidatorForDistrictName");
        String allowedSpecCharsForName=jsMessageResourceService.getPropertyForKey("allowed.specChars.district.name");
        if (!CASValidationUtils.isSpecialCharsAndRegex(districtName,regexForDistrictName))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage(messageSource.getMessage("label.district.name.config.error",null, Locale.getDefault()), Message.MessageType.ERROR, districtName)));
        if (districtName.length()>8)
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Length of name has to be less than 8" , Message.MessageType.ERROR, districtName)));
    }


    public void saveReasonForApprovedRecord(District recordToUpdate, DistrictVO  entityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        List<MasterActiveInactiveReasons> mstActInactReasList = new ArrayList<>();
        if (recordToUpdate.getReasonActInactMap() != null && recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("select r.masterActiveInactiveReasons from ReasonsActiveInactiveMapping r where r.id = :Value");
            JPAQueryExecutor<MasterActiveInactiveReasons> jpaQueryExecutor = new JPAQueryExecutor<MasterActiveInactiveReasons>(sb.toString());
            jpaQueryExecutor.addParameter("Value", recordToUpdate.getReasonActInactMap().getId());
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsList = entityDao.executeQuery(jpaQueryExecutor);
            mstActInactReasList = masterActiveInactiveReasonsList;
            mstActInactReasList.clear();
        }
        List<ReasonActive> activeReasonList = new ArrayList<>();
        List<ReasonInActive> InactiveReasonList = new ArrayList<>();
        if (entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getMasterActiveInactiveReasons() != null){
            entityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> InactiveReasonList.add(m.getReasonInactive()));
            entityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> activeReasonList.add(m.getReasonActive()));
        }
        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(activeReasonList) || org.apache.commons.collections4.CollectionUtils.isNotEmpty(InactiveReasonList))
            mstActInactReasList = activeInactiveReasonService.getMasterReasonList(mstActInactReasList, entityVO.getReasonActInactMap().getMasterActiveInactiveReasons(), dataValidationRuleResults);
        else if(recordToUpdate.getReasonActInactMap() != null && recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons() != null
                && (entityVO.getReasonActInactMap() != null && (entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active")
                && recordToUpdate.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active")) || ((entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")
                && (recordToUpdate.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")))))) {
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            if(org.apache.commons.collections4.CollectionUtils.isEmpty(mstActInactReasList))
                mstActInactReasList.add(new MasterActiveInactiveReasons());
        }
        else if(entityVO.getReasonActInactMap() == null && recordToUpdate.getReasonActInactMap()!=null){
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            entityVO.setReasonActInactMap(recordToUpdate.getReasonActInactMap());
            if(org.apache.commons.collections4.CollectionUtils.isEmpty(mstActInactReasList))
                mstActInactReasList.add(new MasterActiveInactiveReasons());
        }
        else{
            mstActInactReasList.add(new MasterActiveInactiveReasons());
            if(entityVO.getReasonActInactMap() == null)
                entityVO.setReasonActInactMap(new ReasonsActiveInactiveMapping());
        }
        entityVO.getReasonActInactMap().setMasterActiveInactiveReasons(mstActInactReasList);
        recordToUpdate.setReasonActInactMap(entityVO.getReasonActInactMap());
        if (entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getTypeOfAction() != null && entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active")) {
            recordToUpdate.setActiveFlag(true);
        }
        if (entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getTypeOfAction() != null && entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("Inactive")) {
            recordToUpdate.setActiveFlag(false);
        }

    }


}