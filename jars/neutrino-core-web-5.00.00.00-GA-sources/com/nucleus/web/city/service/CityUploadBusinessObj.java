package com.nucleus.web.city.service;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.address.*;
import com.nucleus.dao.query.JPAQueryExecutor;

import org.apache.commons.collections4.CollectionUtils;
import org.hibernate.Hibernate;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.cas.parentChildDeletionHandling.BaseMasterDependency;
import com.nucleus.core.actInactReasService.ActiveInactiveReasonService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
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
import com.nucleus.web.city.vo.CityVO;
import com.nucleus.web.common.controller.CASValidationUtils;
import com.nucleus.jsMessageResource.service.JsMessageResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;


@Named("cityUploadBusinessObj")
public class CityUploadBusinessObj extends BaseServiceImpl implements ICityUploadBusinessObj {

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

    public CityVO uploadCity(CityVO cityVO) {
//        System.out.println(CityVO);
        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<>();

        if (cityVO.getStatus() != null) {

            if (cityVO.getStatus().equalsIgnoreCase("Delete")) {
                if (cityVO.getCityCode() != null && !cityVO.getCityCode().trim().isEmpty()) {
                    City deletedrecordDetails = findRecord(cityVO);
                    if (deletedrecordDetails != null) {
                        if (deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.UNAPPROVED_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED_IN_PROGRESS) {
							if (!BaseMasterDependency.isDependencyPresent(deletedrecordDetails.getClass(),
									deletedrecordDetails.getId())) {
								entityDao.detach(deletedrecordDetails);
								User user1 = getCurrentUser().getUserReference();
								EntityId updatedById = user1.getEntityId();
								makerCheckerService.masterEntityMarkedForDeletion(deletedrecordDetails, updatedById);
							} else {
								ValidationRuleResult validationRuleResult = new ValidationRuleResult(
										CoreUtility.prepareMessage(
												"Record " + deletedrecordDetails.getCityCode()
														+ " is being used by a parent Master",
												Message.MessageType.ERROR, "Check usage section under activity"));
								dataValidationRuleResults.add(validationRuleResult);
							}
						} else {
							ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility
									.prepareMessage("Record Either Already Deleted or Already marked for Deletion.",
											Message.MessageType.ERROR, "Check the City Code"));
							dataValidationRuleResults.add(validationRuleResult);
                        }
                    } else {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR, "Check the City Code"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR, "Check the City Code"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (!dataValidationRuleResults.isEmpty()) {
                    List<Message> validationMessages = new ArrayList<Message>();
                    for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                        validationMessages.add(validationRuleResult.getI18message());
                    }
                    throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in City Upload", "Error in City Upload").setMessages(validationMessages).build();
                }
            }

            if (cityVO.getStatus().equalsIgnoreCase("Edit")) {
                City recordToUpdate = findRecord(cityVO);
                Boolean actInactFlag = false;
                Boolean actionAmbiguityFlag = false;
                Boolean checkForReasons = false;
                Boolean checkForDuplicateReasons = false;
                if (recordToUpdate != null) {
                    String masterName = recordToUpdate.getClass().getSimpleName();
                    String uniqueParameter = "cityCode";
                    String uniqueValue = cityVO.getCityCode();
                    if (cityVO.getReasonActInactMap() != null) {
                        actInactFlag = activeInactiveReasonService.checkForActiveInactiveForApprovedModified(cityVO.getReasonActInactMap(),masterName,uniqueParameter,uniqueValue);
                        actionAmbiguityFlag = activeInactiveReasonService.checkForActionofReasons(cityVO.getReasonActInactMap());
                        checkForReasons = activeInactiveReasonService.checkForGenericReasons(cityVO.getReasonActInactMap());
                        checkForDuplicateReasons = activeInactiveReasonService.checkForDuplicateReasons(cityVO.getReasonActInactMap());
                        if(cityVO.isActiveFlag() && cityVO.getReasonActInactMap() != null && cityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("INACTIVE")){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"}));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        if(!cityVO.isActiveFlag() && cityVO.getReasonActInactMap() != null && cityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("ACTIVE")){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be ACTIVE when ActiveFlag is False"}));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        if(cityVO.getReasonActInactMap() != null && !actionAmbiguityFlag){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason is not provided for defined action", Message.MessageType.ERROR, ",Please provide reason for action:"+cityVO.getReasonActInactMap().getTypeOfAction()));
                            dataValidationRuleResults.add(validationRuleResult);

                        }
                        if (cityVO.getReasonActInactMap() != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(cityVO.getReasonActInactMap().getMasterActiveInactiveReasons()) && !actInactFlag) {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("No Reason Required", Message.MessageType.ERROR, "Please do not give Reason For this action"));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        if(cityVO.getReasonActInactMap() != null && !checkForReasons){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason Code not correct", Message.MessageType.ERROR, "Provide correct reasons"));
                            dataValidationRuleResults.add(validationRuleResult);

                        }if(!checkForDuplicateReasons){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate Reasons not allowed", Message.MessageType.ERROR, "Provide correct reasons"));
                            dataValidationRuleResults.add(validationRuleResult);

                        }
                        if (actInactFlag && actionAmbiguityFlag && checkForDuplicateReasons && checkForReasons ) {
                            saveReasonForApprovedRecord(recordToUpdate, cityVO, dataValidationRuleResults);
                        }
                    }
                    if (cityVO.getCityRiskCategory() != null && cityVO.getCityRiskCategory().getCode()!=null && !cityVO.getCityRiskCategory().getCode().equalsIgnoreCase("")) {
                        CityRiskCategory ct = genericParameterService.findByCode(cityVO.getCityRiskCategory().getCode(), CityRiskCategory.class);
                        if (ct != null && ct.getId() != null) {
                            recordToUpdate.setCityRiskCategory(ct);
                        } else {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage(
                                    "Invalid Risk Category Code", Message.MessageType.ERROR, cityVO.getCityRiskCategory().getCode()));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                    }else{
                        recordToUpdate.setCityRiskCategory(null);
                    }
                    Hibernate.initialize(recordToUpdate.getState());
                    Hibernate.initialize(recordToUpdate.getCountry());
                    Hibernate.initialize(recordToUpdate.getDistrict());
//                    if (recordToUpdate.getReasonActInactMap() != null) {
//                        Hibernate.initialize(recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons());
//                    }
                    entityDao.detach(recordToUpdate);
                    validateCity(cityVO, dataValidationRuleResults);
                    if (!dataValidationRuleResults.isEmpty()) {
                        List<Message> validationMessages = new ArrayList<Message>();
                        for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                            validationMessages.add(validationRuleResult.getI18message());
                        }
                        throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in City Upload", "Error in City Upload").setMessages(validationMessages).build();
                    }
                    copyVOToMaster(recordToUpdate, cityVO);
                    User user1 = getCurrentUser().getUserReference();
                    if (recordToUpdate.getId() != null && user1 != null) {
                        entityDao.detach(recordToUpdate);
                        makerCheckerService.masterEntityChangedByUser(recordToUpdate, user1);
                    }
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record not found, Check Code ", Message.MessageType.ERROR, "Code is  a Mandatory Field"));
                    dataValidationRuleResults.add(validationRuleResult);

                }
                if (!dataValidationRuleResults.isEmpty()) {
                    List<Message> validationMessages = new ArrayList<Message>();
                    for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                        validationMessages.add(validationRuleResult.getI18message());
                    }
                    throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in City Upload", "Error in City Upload").setMessages(validationMessages).build();
                }
            }
        } else {
            City city = new City();
            validateCity(cityVO, dataValidationRuleResults);


            if (cityVO.getReasonActInactMap() != null) {
                boolean flag = false;
                if (cityVO.isActiveFlag() && cityVO.getReasonActInactMap() != null && cityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("INACTIVE")) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"}));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (!cityVO.isActiveFlag() && cityVO.getReasonActInactMap() != null && cityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("ACTIVE")) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be ACTIVE when ActiveFlag is False"}));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (cityVO.getReasonActInactMap().getTypeOfAction() != null && cityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active"))
                    city.setActiveFlag(true);
                if (cityVO.getReasonActInactMap() != null && cityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")) {
                    city.setActiveFlag(false);
                }
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(cityVO.getReasonActInactMap().getMasterActiveInactiveReasons())) {
                    List<MasterActiveInactiveReasons> result = cityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                            .filter(m -> ((m.getReasonInactive() != null && m.getReasonInactive().getCode() != null) || (m.getReasonActive() != null && m.getReasonActive().getCode() != null))).collect(Collectors.toList());
                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(result)) {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Active/Inactive Reason is not required for new record,It is required only for approved or approved modified record", Message.MessageType.ERROR, "Please do not provide active/inactive reasons"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    if (org.apache.commons.collections.CollectionUtils.isEmpty(result)) {
                        List<MasterActiveInactiveReasons> resultDesc = cityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                                .filter(m -> (m.getDescription() != null)).collect(Collectors.toList());
                        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(resultDesc)) {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Description for Active/Inactive Reason not Required", Message.MessageType.ERROR, "Please do not provide active/inactive reasons description"));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                    }
                }
            }
            if (cityVO.getCityRiskCategory() != null && cityVO.getCityRiskCategory().getCode()!=null && !cityVO.getCityRiskCategory().getCode().equalsIgnoreCase("")) {
                CityRiskCategory ct = genericParameterService.findByCode(cityVO.getCityRiskCategory().getCode(), CityRiskCategory.class);
                if (ct != null && ct.getId() != null) {
                    city.setCityRiskCategory(ct);
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage(
                            "Invalid Risk Category Code", Message.MessageType.ERROR, cityVO.getCityRiskCategory().getCode()));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }else{
                city.setCityRiskCategory(null);
            }
            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in City Upload", "Error in City Upload").setMessages(validationMessages).build();
            }

            copyVOToMaster(city, cityVO);
            User user1 = getCurrentUser().getUserReference();
            city.markActive();
            if (city.getId() == null && user1 != null) {
                makerCheckerService.masterEntityChangedByUser(city, user1);
            }
        }
        return cityVO;
    }

    private void validateCity(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
            /*cityMICRCode.code
            locationType.code
            cityCategorization.code
                    highRiskArea*/
        if (validateCountry(cityVO, dataValidationRuleResults))
            if (validateState(cityVO, dataValidationRuleResults))
                validateDistrict(cityVO, dataValidationRuleResults);
        
        validateCityMICRCode(cityVO, dataValidationRuleResults);
        validateCityHighRiskArea(cityVO, dataValidationRuleResults);
        validateCityName(cityVO, dataValidationRuleResults);
        validateCityCode(cityVO, dataValidationRuleResults);
        validateCityStdCode(cityVO, dataValidationRuleResults);

        validateCityLocationType(cityVO, dataValidationRuleResults);
        validateCityCategorization(cityVO, dataValidationRuleResults);

    }

    private void validateCityLocationType(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if(cityVO.getLocationType()!=null)
            if(genericParameterService.findByCode(cityVO.getLocationType().getCode(), LocationType.class,true)!=null){
            }else
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid Location Type", Message.MessageType.ERROR, cityVO.getLocationType().getCode())));
    }

    private void validateCityMICRCode(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
    }

    private void validateCityCode(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (cityVO.getCityCode() != null && !cityVO.getCityCode().trim().isEmpty()) {
            if (cityVO.getStatus() == null) {
                if (checkForDuplicateCode(cityVO.getCityCode())) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("City Code already exists", Message.MessageType.ERROR, cityVO.getCityCode())));
                }
            } else {
                if (!checkForDuplicateCode(cityVO.getCityCode())) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("City Code doesn't exist  ", Message.MessageType.ERROR, cityVO.getCityCode())));
                }
            }
            checkForValidCode(cityVO.getCityCode(), dataValidationRuleResults);
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("City Code cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
        }
    }

    private void checkForValidCode(String cityCode, List<ValidationRuleResult> dataValidationRuleResults) {
        if (!CASValidationUtils.isAlphaNumericAndUnderScore(cityCode))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric value and underscore allowed for Code", Message.MessageType.ERROR, cityCode)));

    }

    private boolean checkForDuplicateCode(String cityCode) {
        return baseMasterService.hasEntity(City.class, "cityCode", cityCode);
    }


    private void validateCityStdCode(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (cityVO.getStdCode() != null && !cityVO.getStdCode().trim().isEmpty()) {
            checkForValidStdCode(cityVO.getStdCode(), dataValidationRuleResults);
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("City STD Code cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
        }
    }

    private void checkForValidStdCode(String stdCode, List<ValidationRuleResult> dataValidationRuleResults) {
        if (!CASValidationUtils.isDigitOnly(stdCode))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only digits  allowed for STD Code", Message.MessageType.ERROR, stdCode)));

    }

    private void validateCityCategorization(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if(cityVO.getCityCategorization()!=null)
            if(genericParameterService.findByCode(cityVO.getCityCategorization().getCode(), CityType.class,true)!=null){
            }else
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid City Categorization", Message.MessageType.ERROR, cityVO.getCityCategorization().getCode())));

    }

    private Boolean validateCountry(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (cityVO.getCountry()!=null && cityVO.getCountry().getCountryISOCode() != null) {
            Country country = new Country();
            country.setCountryISOCode(cityVO.getCountry().getCountryISOCode());
            return (checkForValidCountry(country, dataValidationRuleResults));

        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Country cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
            return false;
        }

    }

    private Boolean checkForValidCountry(Country country, List<ValidationRuleResult> dataValidationRuleResults) {
        if (findCountryByCode(country.getCountryISOCode()) == null) {
//        if (!baseMasterService.hasEntity(State.class, "stateCode", state.getStateCode()))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Country doesn't exist", Message.MessageType.ERROR, country.getCountryISOCode())));
            return false;
        }
        return true;
    }

    private Country findCountryByCode(String countryISOCode) {
        NamedQueryExecutor<Country> executor = new NamedQueryExecutor<Country>("CityMaster.findCountryByCode")
                .addParameter("countryISOCode", countryISOCode)
                .addParameter("approvalStatus", Arrays.asList(ApprovalStatus.APPROVED, ApprovalStatus.APPROVED_MODIFIED));
        List<Country> country = entityDao.executeQuery(executor);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(country)) {
            return country.get(0);
        }
        return null;
    }

    private Boolean validateState(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (cityVO.getState()!=null && cityVO.getState().getStateCode() != null) {
            State state = new State();
            state.setStateCode(cityVO.getState().getStateCode());
            if(checkForValidState(state, dataValidationRuleResults)){
                return checkCountryAndState(cityVO.getCountry(),cityVO.getState(),dataValidationRuleResults);
            }else
                return false;
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("State cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
            return false;
        }

    }

    private Boolean checkCountryAndState(Country country, State state, List<ValidationRuleResult> dataValidationRuleResults) {
        State stateCheck=findStateByCode(state.getStateCode());
        if(stateCheck.getCountry().getCountryISOCode().equals(country.getCountryISOCode())){
            return true;
        }else
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid mapping between state and country", Message.MessageType.ERROR,"Check Mapping")));
        return false;
    }

    private Boolean checkForValidState(State state, List<ValidationRuleResult> dataValidationRuleResults) {
        if (findStateByCode(state.getStateCode()) == null){
//        if (!baseMasterService.hasEntity(State.class, "stateCode", state.getStateCode()))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("State doesn't exist", Message.MessageType.ERROR, state.getStateCode())));
            return false;
        }
        return true;
    }

    private State findStateByCode(String stateCode) {
        NamedQueryExecutor<State> executor = new NamedQueryExecutor<State>("CityMaster.findStateByCode")
                .addParameter("stateCode", stateCode)
                .addParameter("approvalStatus", Arrays.asList(ApprovalStatus.APPROVED, ApprovalStatus.APPROVED_MODIFIED));
        List<State> states = entityDao.executeQuery(executor);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(states)) {
            return states.get(0);
        }
        return null;
    }

    private void validateDistrict(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (cityVO.getDistrict()!=null && cityVO.getDistrict().getDistrictCode() != null) {
            District district = new District();
            district.setDistrictCode(cityVO.getDistrict().getDistrictCode());
            if(checkForValidDistrict(district, dataValidationRuleResults)){
                checkStateAndDistrict(cityVO.getState(),cityVO.getDistrict(),dataValidationRuleResults);
            }else
                return;
        } else {
//            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));

            return;
        }

    }

    private void checkStateAndDistrict(State state, District district, List<ValidationRuleResult> dataValidationRuleResults) {
        District districtCheck=findDistrictByCode(district.getDistrictCode());
        if(districtCheck.getState().getStateCode().equals(state.getStateCode())){
            return;
        }else
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid mapping between state and district", Message.MessageType.ERROR,"Check Mapping")));

    }

    private boolean checkForValidDistrict(District district, List<ValidationRuleResult> dataValidationRuleResults) {
        if (findDistrictByCode(district.getDistrictCode()) == null){
//        if (!baseMasterService.hasEntity(State.class, "stateCode", state.getStateCode()))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("District doesn't exist", Message.MessageType.ERROR, district.getDistrictCode())));
            return false;
        }
        return true;
    }

    private District findDistrictByCode(String districtCode) {
        NamedQueryExecutor<District> executor = new NamedQueryExecutor<District>("CityMaster.findDistrictByCode")
                .addParameter("districtCode", districtCode)
                .addParameter("approvalStatus", Arrays.asList(ApprovalStatus.APPROVED, ApprovalStatus.APPROVED_MODIFIED));
        List<District> districts = entityDao.executeQuery(executor);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(districts)) {
            return districts.get(0);
        }
        return null;
    }

    private void validateCityHighRiskArea(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {

    }

    private void validateCityName(CityVO cityVO, List<ValidationRuleResult> dataValidationRuleResults) {
        if (cityVO.getCityName() != null && !cityVO.getCityName().trim().isEmpty()) {
            if (cityVO.getStatus() == null) {
                checkForDuplicateName(cityVO, dataValidationRuleResults);
            } else {
                if (!checkForNameAndCode(cityVO)) {
                    checkForDuplicateName(cityVO, dataValidationRuleResults);
                }
            }
            checkForValidName(cityVO.getCityName(), dataValidationRuleResults);
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("City Name cannot be Left Blank", Message.MessageType.ERROR, "It is a Mandatory Field.")));
        }
    }

    private void checkForValidName(String cityName, List<ValidationRuleResult> dataValidationRuleResults) {
        String regexForCityName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.city.name","core.web.validation.config.customValidatorForCityName");
        String allowedSpecCharsForName=jsMessageResourceService.getPropertyForKey("allowed.specChars.city.name");
        if (!CASValidationUtils.isSpecialCharsAndRegex(cityName,  regexForCityName))
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage(messageSource.getMessage("label.city.name.config.error",null, Locale.getDefault()), Message.MessageType.ERROR, cityName)));

    }

    private boolean checkForNameAndCode(CityVO cityVO) {
        String dName = cityVO.getCityName(), dCode = cityVO.getCityCode();
        City cityRetrieved = findRecord(cityVO);
        if (cityRetrieved.getCityName().equals(dName))
            return true;
        return false;
    }

    private void checkForDuplicateName(CityVO city, List<ValidationRuleResult> dataValidationRuleResults) {
        City cityRetrieved = findRecord(city);
        Map<String, Object> propertyNameValueMap = new HashMap<>();
        propertyNameValueMap.put("cityName", city.getCityName());
        State st = null;
        if (city.getState() != null && city.getState().getStateCode() != null)
            st = findStateByCode(city.getState().getStateCode());
        if (st != null) {
            propertyNameValueMap.put("state", st);
            if (city.getStatus() == null || cityRetrieved == null) {
                List<Integer> statusList = new ArrayList<Integer>();
                statusList.add(ApprovalStatus.UNAPPROVED_ADDED);
                statusList.add(ApprovalStatus.APPROVED_MODIFIED);
                statusList.add(ApprovalStatus.UNAPPROVED_MODIFIED);
                statusList.add(ApprovalStatus.APPROVED_DELETED);
                statusList.add(ApprovalStatus.WORFLOW_IN_PROGRESS);
                statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
                statusList.add(ApprovalStatus.APPROVED);
                List<Map<String, Object>> result = baseMasterService.getAllApprovedAndActiveSelectedListEntities(City.class,
                        statusList, propertyNameValueMap, new String[] { "cityName", "cityCode" });
                if (CollectionUtils.isNotEmpty(result)) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage(
                            "City Name already exists within this State", Message.MessageType.ERROR, city.getCityName())));
                }
            } else {
                if (baseMasterService.hasEntity(City.class, propertyNameValueMap, cityRetrieved.getId()))
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage(
                            "City Name already exists within this state", Message.MessageType.ERROR, city.getCityName())));
            }
        }
    }

    private void copyVOToMaster(City city, CityVO cityVO) {
        city.setCityCode(cityVO.getCityCode());
        city.setCityName(cityVO.getCityName());
        city.setStdCode(cityVO.getStdCode());
        city.setCityMICRCode(cityVO.getCityMICRCode());
        city.setHighRiskArea(cityVO.isHighRiskArea());
        if(cityVO.getCityCategorization()!=null)
        city.setCityCategorization(genericParameterService.findByCode(cityVO.getCityCategorization().getCode(), CityType.class,true));
        if(cityVO.getLocationType()!=null)
        city.setLocationType(genericParameterService.findByCode(cityVO.getLocationType().getCode(), LocationType.class,true));

        city.setCountry(findCountryByCode(cityVO.getCountry().getCountryISOCode()));

        city.setState(findStateByCode(cityVO.getState().getStateCode()));
        if(cityVO.getDistrict()!=null)
        city.setDistrict(findDistrictByCode(cityVO.getDistrict().getDistrictCode()));
        if(null != cityVO.getReasonActInactMap()){
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsLists = cityVO.getReasonActInactMap().getMasterActiveInactiveReasons();
            if(null != cityVO.getReasonActInactMap().getMasterActiveInactiveReasons() && masterActiveInactiveReasonsLists.size() > 0){
                city.setReasonActInactMap(cityVO.getReasonActInactMap());
            }
        }

    }

    private City findRecord(CityVO cityVO) {
        HashMap cityMap = new HashMap<String, Object>();
        cityMap.put("cityCode", cityVO.getCityCode());
        if(cityVO.getCityCode()!=null)
        	return (findCityByCode(cityVO.getCityCode()));
        else
            return null;
    }

    private City findCityByCode(String cityCode) {
        NamedQueryExecutor<City> executor = new NamedQueryExecutor<City>("CityMaster.findCityByCode")
                .addParameter("cityCode", cityCode)
                .addParameter("approvalStatus", Arrays.asList(1, 2, 3, 5, 10));
        List<City> districts = entityDao.executeQuery(executor);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(districts)) {
            return districts.get(0);
        }
        return null;
    }


    public void saveReasonForApprovedRecord(City recordToUpdate, CityVO entityVO, List<ValidationRuleResult> dataValidationRuleResults) {
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