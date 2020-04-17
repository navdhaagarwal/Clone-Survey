package com.nucleus.core.ipaddress.service;


import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.actInactReasService.ActiveInactiveReasonService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.ipaddress.vo.IpAddressVO;
import com.nucleus.core.misc.util.IpAddressUtils;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;


import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.AccessType;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.ipaddress.IpAddress;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nucleus.user.AccessType.BOTH;

@Named("ipAddressUploadBusinessObj")
public class IpAddressUploadBusinessObj extends BaseServiceImpl implements IIpAddressUploadBusinessObj {

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
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("activeInactiveReasonService")
    private ActiveInactiveReasonService activeInactiveReasonService;




    public IpAddressVO uploadIpAddress(IpAddressVO ipAddressVO){


        if(ipAddressVO.getStatus()!=null){

            if(ipAddressVO.getStatus().equalsIgnoreCase("Delete")){

                List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
                IpAddress deletedrecordDetails = findRecord(ipAddressVO.getIpAddress());
                if(deletedrecordDetails != null)
                {
                    if(deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.UNAPPROVED_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED_IN_PROGRESS)
                    {
                        entityDao.detach(deletedrecordDetails);
                        User user1 =getCurrentUser().getUserReference();
                        EntityId updatedById = user1.getEntityId();
                        makerCheckerService.masterEntityMarkedForDeletion(deletedrecordDetails,updatedById);
                    }
                    else
                    {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Either Already Deleted or Already marked for Deletion.", Message.MessageType.ERROR,"Check the IpAddress Code"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }
                else
                {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR,"Check the IpAddress Code"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                if (!dataValidationRuleResults.isEmpty()) {
                    List<Message> validationMessages = new ArrayList<Message>();
                    for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                        validationMessages.add(validationRuleResult.getI18message());
                    }
                    throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in IpAddress Upload", "Error in IpAddress Upload").setMessages(validationMessages).build();
                }
            }

            if(ipAddressVO.getStatus().equalsIgnoreCase("Edit")){

                List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
                IpAddress recordToUpdate=findRecord(ipAddressVO.getIpAddress());
                Boolean actInactFlag = false;
                Boolean actionAmbiguityFlag = false;
                Boolean checkForReasons = false;
                Boolean checkForDuplicateReasons = false;
                if(recordToUpdate!=null){
                    //IP Address
                    String masterName = recordToUpdate.getClass().getSimpleName();
                    String uniqueParameter = "ipAddress";
                    String uniqueValue = ipAddressVO.getIpAddress();
                    actInactFlag = activeInactiveReasonService.checkForActiveInactiveForApprovedModified(ipAddressVO.getReasonActInactMap(), masterName, uniqueParameter, uniqueValue);
                    actionAmbiguityFlag = activeInactiveReasonService.checkForActionofReasons(ipAddressVO.getReasonActInactMap());
                    checkForReasons = activeInactiveReasonService.checkForGenericReasons(ipAddressVO.getReasonActInactMap());
                    checkForDuplicateReasons = activeInactiveReasonService.checkForDuplicateReasons(ipAddressVO.getReasonActInactMap());

                    if(ipAddressVO.getReasonActInactMap() != null && !actionAmbiguityFlag){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason is not provided for defined action", Message.MessageType.ERROR, ",Please provide reason for action:"+ipAddressVO.getReasonActInactMap().getTypeOfAction()));
                        dataValidationRuleResults.add(validationRuleResult);

                    }
                    if (ipAddressVO.getReasonActInactMap() != null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(ipAddressVO.getReasonActInactMap().getMasterActiveInactiveReasons()) && !actInactFlag) {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("No Reason Required", Message.MessageType.ERROR, "Please do not give Reason For this action"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    if(ipAddressVO.getReasonActInactMap() != null && !checkForReasons){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Reason Code not correct", Message.MessageType.ERROR, "Provide correct reasons"));
                        dataValidationRuleResults.add(validationRuleResult);

                    }if(!checkForDuplicateReasons){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate Reasons not allowed", Message.MessageType.ERROR, "Provide correct reasons"));
                        dataValidationRuleResults.add(validationRuleResult);

                    }
                    if (actInactFlag && actionAmbiguityFlag && checkForDuplicateReasons && checkForReasons ) {
                        saveReasonForApprovedRecord(recordToUpdate, ipAddressVO, dataValidationRuleResults);
                    }

                    if(ipAddressVO.getIpAddress()!=null){
                        recordToUpdate.setIpAddress(ipAddressVO.getIpAddress());
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("IpAddress Code cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                    //Access Type
                    if(ipAddressVO.getAccessType()!=null){
                        AccessType accessType=genericParameterService.findByCode(ipAddressVO.getAccessType(),AccessType.class);
                        if(accessType!=null && !accessType.getCode().equals(BOTH)){
                            recordToUpdate.setAccessType(accessType);
                        }else{
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Access Type is Invalid", Message.MessageType.ERROR, "It can only be Internet or Intranet"));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Access Type cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }


                if (!dataValidationRuleResults.isEmpty()) {
                    List<Message> validationMessages = new ArrayList<Message>();
                    for (ValidationRuleResult validationRuleResult1 : dataValidationRuleResults) {
                        validationMessages.add(validationRuleResult1.getI18message());
                    }
                    throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in IpAddress Upload", "Error in IpAddress Upload").setMessages(validationMessages).build();
                }else{
                    User user1 = getCurrentUser().getUserReference();
                    if (recordToUpdate.getId() != null && user1 != null) {
                        entityDao.detach(recordToUpdate);
                        makerCheckerService.masterEntityChangedByUser(recordToUpdate,user1);
                    }
                }
            }
        }

        else{
            List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
            IpAddress IpAddress = new IpAddress();
            //IP Address
            if(ipAddressVO.getIpAddress()!= null) {
                if (checkForDuplicateCode(ipAddressVO.getIpAddress())) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("IpAddress Code already exists", Message.MessageType.ERROR, ipAddressVO.getIpAddress())));
                }
                if (!validIP(ipAddressVO.getIpAddress())) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid IP Address", Message.MessageType.ERROR, ipAddressVO.getIpAddress())));
                }
                IpAddress.setIpAddress(ipAddressVO.getIpAddress());
            }
            else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("IpAddress Code cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                dataValidationRuleResults.add(validationRuleResult);
            }

            //Access Type
            if(ipAddressVO.getAccessType()!=null){
                String accessType=ipAddressVO.getAccessType();
                AccessType accessType1=genericParameterService.findByCode(accessType,AccessType.class);
                if(accessType1!=null && !accessType1.getCode().equals(BOTH)){
                    IpAddress.setAccessType(accessType1);
                }else{
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Access Type is Invalid", Message.MessageType.ERROR, "It can only be Internet or Intranet"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Access Type cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                dataValidationRuleResults.add(validationRuleResult);
            }

            chekForMaliciousString(ipAddressVO.getIpAddress(), dataValidationRuleResults);
            if (ipAddressVO.getReasonActInactMap() != null) {
                    boolean flag = false;
                    if (ipAddressVO.getReasonActInactMap().getTypeOfAction() != null && ipAddressVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("active"))
                        IpAddress.setActiveFlag(true);
                    if (ipAddressVO.getReasonActInactMap() != null && ipAddressVO.getReasonActInactMap().getTypeOfAction().equalsIgnoreCase("inactive")) {
                        IpAddress.setActiveFlag(false);
                    }
                    if(org.apache.commons.collections.CollectionUtils.isNotEmpty(ipAddressVO.getReasonActInactMap().getMasterActiveInactiveReasons())){
                        List<MasterActiveInactiveReasons> result = ipAddressVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
                                .filter(m -> ((m.getReasonInactive() != null && m.getReasonInactive().getCode() != null) || (m.getReasonActive() != null && m.getReasonActive().getCode() != null))).collect(Collectors.toList());
                        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(result)) {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Active/Inactive Reason is not required for new record,It is required only for approved or approved modified record", Message.MessageType.ERROR, "Please do not provide active/inactive reasons"));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        if (org.apache.commons.collections.CollectionUtils.isEmpty(result)) {
                            List<MasterActiveInactiveReasons> resultDesc = ipAddressVO.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
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
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in IpAddress Upload", "Error in IpAddress Upload").setMessages(validationMessages).build();
            }else{
                User user1= getCurrentUser().getUserReference();
                IpAddress.markActive();
                if(IpAddress.getId() == null && user1 != null){
                    makerCheckerService.masterEntityChangedByUser(IpAddress,user1);
                }
            }
        }
        return ipAddressVO;
    }

    public IpAddress findRecord(String ipAddress){
        NamedQueryExecutor<IpAddress> executor = new NamedQueryExecutor<IpAddress>("IpAddressMaster.findIpByIpAddress")
                .addParameter("ipAddress", ipAddress)
                .addParameter("approvalStatus", Arrays.asList(1,2,3,5,10));
        List<IpAddress> ipAddresses = entityDao.executeQuery(executor);
        if(CollectionUtils.isNotEmpty(ipAddresses)){
            return ipAddresses.get(0);
        }
        return null;
    }


    private boolean checkForDuplicateCode(Object ipAddress){
        boolean flag;
        String code = "ipAddress";
        flag = baseMasterService.hasEntity(IpAddress.class, code, ipAddress);
        return flag;
    }


    public  void chekForMaliciousString(String ipAddress,List<ValidationRuleResult> dataValidationRuleResults) {

        if(ipAddress != null) {
            if(ipAddress.matches("^[\\+|\\-|\\=|\\@](.*)")){
                ipAddress = new StringBuilder(ipAddress).insert(0,"'").toString();
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("IpAddress can not be start with + or - or @ or =", Message.MessageType.ERROR, ipAddress)));
            }
        }


    }

    private boolean validIP(String ipAddress){
        String regex = "^(([0-9]|[*]|[1-9][0-9]|[*]|1[0-9]{2}|[*]|2[0-4][0-9]|[*]|25[0-5])\\.){3}([0-9]|[*]|[1-9][0-9]|[*]|1[0-9]{2}|[*]|2[0-4][0-9]|[*]|25[0-5])$";
        return ipAddress.matches(regex);
    }

    public void saveReasonForApprovedRecord(IpAddress recordToUpdate, IpAddressVO entityVO, List<ValidationRuleResult> dataValidationRuleResults) {
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
        else if(entityVO.getReasonActInactMap() == null && recordToUpdate.getReasonActInactMap() != null){
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
