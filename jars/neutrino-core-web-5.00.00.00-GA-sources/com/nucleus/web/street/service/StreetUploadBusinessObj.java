package com.nucleus.web.street.service;

import com.nucleus.activeInactiveReason.*;
import com.nucleus.address.*;
import com.nucleus.core.actInactReasService.*;
import com.nucleus.dao.query.*;
import com.nucleus.entity.*;
import com.nucleus.finnone.pro.base.*;
import com.nucleus.finnone.pro.base.exception.*;
import com.nucleus.finnone.pro.base.utility.*;
import com.nucleus.finnone.pro.base.validation.domainobject.*;
import com.nucleus.makerchecker.*;
import com.nucleus.master.*;
import com.nucleus.persistence.*;
import com.nucleus.service.*;
import com.nucleus.user.*;
import com.nucleus.web.street.vo.*;
import org.hibernate.*;
import org.springframework.beans.factory.annotation.*;

import javax.inject.*;
import java.util.*;
import java.util.stream.*;

@Named("streetUploadBusinessObj")
public class StreetUploadBusinessObj extends BaseServiceImpl implements IStreetUploadBusinessObj{


    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;


    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("activeInactiveReasonService")
    private ActiveInactiveReasonService activeInactiveReasonService;

    @Value(value = "#{'${allowed.alphacharset.range}'}")
    private String alphachar;

    @Override
    public StreetVO uploadStreet(StreetVO streetVO) {

        Hibernate.initialize(streetVO.getReasonActInactMap());
        if(streetVO.getStatus()==null)
        {
            Street street=new Street();
            List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();

            if(streetVO.getStreetCode()!= null  && !streetVO.getStreetCode().equalsIgnoreCase("")) {
                if (checkForDuplicateCode(streetVO.getStreetCode())) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Code already exists.", Message.MessageType.ERROR, streetVO.getStreetCode())));
                }
            }
            else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Code field can't be left blank.", Message.MessageType.ERROR,"It is a Mandatory Field."));
                dataValidationRuleResults.add(validationRuleResult);
            }


            if (streetVO.getStreetName() != null ) {
                if (streetVO.getStreetName().length() > 250) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Name should be within 250 characters.", Message.MessageType.ERROR, streetVO.getStreetName()));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }
            else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Name field can't be left blank.", Message.MessageType.ERROR,"It is a Mandatory Field."));
                dataValidationRuleResults.add(validationRuleResult);
            }

             if (streetVO.getAbbreviation() != null && streetVO.getAbbreviation().length()>250) {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Abbreviation should be within 250 characters.", Message.MessageType.ERROR,streetVO.getAbbreviation()));
                dataValidationRuleResults.add(validationRuleResult);
            }
            if(streetVO.getAbbreviation() == null)
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Abbreviation field can't be left blank.", Message.MessageType.ERROR,"It is a Mandatory Field."));
                dataValidationRuleResults.add(validationRuleResult);
            }

            if (streetVO.getCity() != null && streetVO.getCity().getCityCode()!=null) {
                //TODO check for city code
                City city = findCityByCode(streetVO.getCity().getCityCode());
                if(city == null) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Invalid city code", Message.MessageType.ERROR, streetVO.getCity().getCityCode()));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }
            if (streetVO.getCity() == null)
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("City field can't be left blank.", Message.MessageType.ERROR,"It is a Mandatory Field."));
                dataValidationRuleResults.add(validationRuleResult);
            }

            if (streetVO.getReasonActInactMap() != null) {
                boolean flag = false;
                if (streetVO.isActiveFlag() && streetVO.getReasonActInactMap() != null && streetVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("INACTIVE")) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"}));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (!streetVO.isActiveFlag() && streetVO.getReasonActInactMap() != null && streetVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("ACTIVE")) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be ACTIVE when ActiveFlag is False"}));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (streetVO.getReasonActInactMap().getTypeOfAction() != null && streetVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active"))
                    street.setActiveFlag(true);
                if (streetVO.getReasonActInactMap() != null && streetVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")) {
                    street.setActiveFlag(false);
                }
                if (org.apache.commons.collections.CollectionUtils.isNotEmpty(streetVO.getReasonActInactMap().getMasterActiveInactiveReasons())) {
                    List<MasterActiveInactiveReasons> result = streetVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                            .filter(m -> ((m.getReasonInactive() != null && m.getReasonInactive().getCode() != null) || (m.getReasonActive() != null && m.getReasonActive().getCode() != null))).collect(Collectors.toList());
                    if (org.apache.commons.collections.CollectionUtils.isNotEmpty(result)) {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Active/Inactive Reason is not required for new record,It is required only for approved or approved modified record", Message.MessageType.ERROR, "Please do not provide active/inactive reasons"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    if (org.apache.commons.collections.CollectionUtils.isEmpty(result)) {
                        List<MasterActiveInactiveReasons> resultDesc = streetVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
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
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Street Upload", "Error in Street Upload").setMessages(validationMessages).build();
            }

            else {
                User user2 =  getCurrentUser().getUserReference();
                street.markActive();
                convertStreetVOtoStreetEntity(street,streetVO);
                if (street.getId() == null && user2 != null) {
                    makerCheckerService.masterEntityChangedByUser(street, user2);
                }
            }
            return streetVO;

        }
        //Edit Master
        else if(streetVO.getStatus().equalsIgnoreCase("Edit"))
        {
            List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
            Street recordtoupdate=null;
            if(streetVO.getStreetCode()!=null)
                recordtoupdate = findRecordStreet(streetVO.getStreetCode());
            Boolean actInactFlag = false;
            Boolean actionAmbiguityFlag = false;
            Boolean checkForReasons = false;
            Boolean checkForDuplicateReasons = false;

            if(recordtoupdate != null){
                String masterName = recordtoupdate.getClass().getSimpleName();
                String uniqueParameter = "streetCode";
                String uniqueValue = streetVO.getStreetCode();
                if (streetVO.getReasonActInactMap() != null) {
                    actInactFlag = activeInactiveReasonService.checkForActiveInactiveForApprovedModified(streetVO.getReasonActInactMap(),masterName,uniqueParameter,uniqueValue);
                    actionAmbiguityFlag = activeInactiveReasonService.checkForActionofReasons(streetVO.getReasonActInactMap());
                    checkForReasons = activeInactiveReasonService.checkForGenericReasons(streetVO.getReasonActInactMap());
                    checkForDuplicateReasons = activeInactiveReasonService.checkForDuplicateReasons(streetVO.getReasonActInactMap());
                    if(streetVO.isActiveFlag() && streetVO.getReasonActInactMap() != null && streetVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("INACTIVE")){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be INACTIVE when ActiveFlag is True"}));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    if(!streetVO.isActiveFlag() && streetVO.getReasonActInactMap() != null && streetVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("ACTIVE")){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Ambiguity in ActiveFlag and Action", Message.MessageType.ERROR, new String[]{"Action cannot be ACTIVE when ActiveFlag is False"}));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    if(streetVO.getReasonActInactMap() != null && !actionAmbiguityFlag){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason is not provided for defined action", Message.MessageType.ERROR, ",Please provide reason for action:"+streetVO.getReasonActInactMap().getTypeOfAction()));
                        dataValidationRuleResults.add(validationRuleResult);

                    }
                    if (streetVO.getReasonActInactMap() != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(streetVO.getReasonActInactMap().getMasterActiveInactiveReasons()) && !actInactFlag) {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("No Reason Required", Message.MessageType.ERROR, "Please do not give Reason For this action"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    if(streetVO.getReasonActInactMap() != null && !checkForReasons){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason Code not correct", Message.MessageType.ERROR, "Provide correct reasons"));
                        dataValidationRuleResults.add(validationRuleResult);

                    }if(!checkForDuplicateReasons){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate Reasons not allowed", Message.MessageType.ERROR, "Provide correct reasons"));
                        dataValidationRuleResults.add(validationRuleResult);

                    }
                    if (actInactFlag && actionAmbiguityFlag && checkForDuplicateReasons && checkForReasons ) {
                        saveReasonForApprovedRecord(recordtoupdate, streetVO, dataValidationRuleResults);
                    }
                }


                entityDao.detach(recordtoupdate);
                if(streetVO.getStreetCode() != null && !streetVO.getStreetCode().equalsIgnoreCase("")) {

                    recordtoupdate.setStreetCode(streetVO.getStreetCode());
                }
                else
                {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Street code cannot be Left Blank",Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (streetVO.getStreetName() != null && streetVO.getStreetName().length()>250) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Street name should be within 250 characters.", Message.MessageType.ERROR,"Only 250 characters are allowed."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                else {
                    recordtoupdate.setStreetName(streetVO.getStreetName());
                }
                if (streetVO.getAbbreviation() != null && streetVO.getAbbreviation().length()>250) {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Abbreviation should be within 250 characters.", Message.MessageType.ERROR,"Only 250 characters are allowed."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                else {
                    recordtoupdate.setAbbreviation(streetVO.getAbbreviation());
                }
                City city = findCityByCode(streetVO.getCity().getCityCode());
                if (streetVO.getCity().getCityCode() != null && city == null ) {

                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Abbreviation should be within 250 characters.", Message.MessageType.ERROR, "Only 250 characters are allowed."));
                    dataValidationRuleResults.add(validationRuleResult);

                }

                if(dataValidationRuleResults.isEmpty()) {

                    recordtoupdate.setCity(city);
                }



            }
            else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Either Empty or Invalid - ",Message.MessageType.ERROR,"Street Code"));
                dataValidationRuleResults.add(validationRuleResult);
            }
            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Street Upload", "Error in Street Upload").setMessages(validationMessages).build();
            }else{
                User user2 = getCurrentUser().getUserReference();
                if (recordtoupdate.getId() != null && user2 != null) {
                    makerCheckerService.masterEntityChangedByUser(recordtoupdate,user2);
                }
            }
        }


        else if(streetVO.getStatus().equalsIgnoreCase("Delete")){
            List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
            Street deletedrecordDetails = findRecordStreet(streetVO.getStreetCode());
            if(deletedrecordDetails != null)
            {
                if(deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.UNAPPROVED_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED_IN_PROGRESS)
                {

                }
                else
                {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Either Already Deleted or Already marked for Deletion.",Message.MessageType.ERROR,"Check Street Code."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }
            else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.",Message.MessageType.ERROR,"Check Street Code."));
                dataValidationRuleResults.add(validationRuleResult);
            }
            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Street Upload", "Error in Street").setMessages(validationMessages).build();
            }
        }
        else
        {
            List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Status can only be empty (for creation), Edit or Delete..",Message.MessageType.ERROR,"Check Status."));
            dataValidationRuleResults.add(validationRuleResult);
            List<Message> validationMessages = new ArrayList<Message>();
            validationMessages.add(validationRuleResult.getI18message());
            throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Street Upload", "Error in Street").setMessages(validationMessages).build();

        }
        return null;
    }

    public void saveReasonForApprovedRecord(Street recordToUpdate, StreetVO entityVO, List<ValidationRuleResult> dataValidationRuleResults) {
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
        if (entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
            entityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonInactive() != null).forEach(m -> InactiveReasonList.add(m.getReasonInactive()));
            entityVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream().filter(m -> Objects.nonNull(m)).filter(m -> m.getReasonActive() != null).forEach(m -> activeReasonList.add(m.getReasonActive()));
        }
        if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(activeReasonList) || org.apache.commons.collections4.CollectionUtils.isNotEmpty(InactiveReasonList))
            mstActInactReasList = activeInactiveReasonService.getMasterReasonList(mstActInactReasList, entityVO.getReasonActInactMap().getMasterActiveInactiveReasons(), dataValidationRuleResults);
        else if (recordToUpdate.getReasonActInactMap() != null && recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons() != null
                && (entityVO.getReasonActInactMap() != null && (entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active")
                && recordToUpdate.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active")) || ((entityVO.getReasonActInactMap() != null && entityVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")
                && (recordToUpdate.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")))))) {
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(mstActInactReasList))
                mstActInactReasList.add(new MasterActiveInactiveReasons());
        } else if (entityVO.getReasonActInactMap() == null && recordToUpdate.getReasonActInactMap() != null) {
            mstActInactReasList = recordToUpdate.getReasonActInactMap().getMasterActiveInactiveReasons();
            entityVO.setReasonActInactMap(recordToUpdate.getReasonActInactMap());
            if (org.apache.commons.collections4.CollectionUtils.isEmpty(mstActInactReasList))
                mstActInactReasList.add(new MasterActiveInactiveReasons());
        } else {
            mstActInactReasList.add(new MasterActiveInactiveReasons());
            if (entityVO.getReasonActInactMap() == null)
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

    private Street findRecordStreet(String streetCode) {
        NamedQueryExecutor<Street> executor = new NamedQueryExecutor<Street>("StreetMaster.findStreetByCode")
                .addParameter("streetCode", streetCode)
                .addParameter("approvalStatus", Arrays.asList(1, 2, 3, 5, 10));
        List<Street> streets = entityDao.executeQuery(executor);
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(streets)) {
            return streets.get(0);
        }
        return null;
    }

    private boolean checkForDuplicateCode(String streetCode) {
        return baseMasterService.hasEntity(Street.class, "streetCode", streetCode);
    }

    private void convertStreetVOtoStreetEntity(Street street, StreetVO streetVO) {

        street.setStreetCode(streetVO.getStreetCode());
        street.setStreetName(streetVO.getStreetName());
        street.setAbbreviation(streetVO.getAbbreviation());
        street.setCity(findCityByCode(streetVO.getCity().getCityCode()));



        if(null != streetVO.getReasonActInactMap()){
            List<MasterActiveInactiveReasons> masterActiveInactiveReasonsLists = streetVO.getReasonActInactMap().getMasterActiveInactiveReasons();
            if(null != streetVO.getReasonActInactMap().getMasterActiveInactiveReasons() && masterActiveInactiveReasonsLists.size() > 0){
                street.setReasonActInactMap(streetVO.getReasonActInactMap());
            }
        }

    }
}
