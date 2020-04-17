package com.nucleus.core.rules.objectGraph;

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
import com.nucleus.rules.model.*;
import com.nucleus.rules.service.RuleService;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import org.apache.commons.collections.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




/**
 * 
 */
@Named("objectGraphUploadBusinessObj")
public class ObjectGraphUploadBusinessObj extends BaseServiceImpl implements IObjectGraphUploadBusinessObj{

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @Inject
    @Named("ruleService")
    private RuleService ruleService;

    public ObjectGraphTypes uploadObjectGraph(ObjectGraphTypes objectGraphTypes){

        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
        if(objectGraphTypes.getUploadOperationType()!=null){
            performMentionedOperation(objectGraphTypes,dataValidationRuleResults);
        }else {
            validateObjectGraphTypes(objectGraphTypes, dataValidationRuleResults);

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Object Graph Upload", "Error in Object Graph Upload").setMessages(validationMessages).build();

            } else {
                User user = getCurrentUser().getUserReference();
                objectGraphTypes.markActive();
                if (objectGraphTypes.getId() == null && user != null) {
                    makerCheckerService.masterEntityChangedByUser(objectGraphTypes, user);
                }

            }
        }
         return objectGraphTypes;
    }



  private void performMentionedOperation(ObjectGraphTypes objectGraphTypes,List<ValidationRuleResult> dataValidationRuleResults){
        if(objectGraphTypes.getUploadOperationType().equalsIgnoreCase("Delete")){
            ObjectGraphTypes deletedrecordDetails = findRecord(objectGraphTypes.getDisplayName());
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
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Either Already Deleted or Already marked for Deletion.", Message.MessageType.ERROR,"Check the Object Graph Display Name"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }
            else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR,"Check the Object Graph Display Name"));
                dataValidationRuleResults.add(validationRuleResult);
            }

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Object Graph Upload", "Error in Object Graph Upload").setMessages(validationMessages).build();
            }
        } else  if(objectGraphTypes.getUploadOperationType().equalsIgnoreCase("Edit")){

            ObjectGraphTypes recordToUpdate = findRecord(objectGraphTypes.getDisplayName());
               if(recordToUpdate!=null){
//Source Product
                if(objectGraphTypes.getSourceProduct()!=null){
                    SourceProduct sourceProduct=genericParameterService.findByCode(objectGraphTypes.getSourceProduct(),SourceProduct.class);
                    if(sourceProduct!=null){
                        recordToUpdate.setSourceProduct(objectGraphTypes.getSourceProduct());
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }else{
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
//Module Name
                if(objectGraphTypes.getModuleName()!=null){
                    ModuleName moduleName=genericParameterService.findByCode(objectGraphTypes.getModuleName().getCode(),ModuleName.class);
                    if(moduleName!=null){
                        recordToUpdate.setModuleName(moduleName);
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }else{
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
//Display Name
               if(objectGraphTypes.getDisplayName()!=null){
                   recordToUpdate.setDisplayName(objectGraphTypes.getDisplayName());
               }else{
                   ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Display Name cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                   dataValidationRuleResults.add(validationRuleResult);
               }
//Description
               if(objectGraphTypes != null && objectGraphTypes.getDescription() != null){
                   recordToUpdate.setDescription(objectGraphTypes.getDescription());
               } else {
                   dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Description is mandatory.",
                           Message.MessageType.ERROR,"Description is mandatory.")));
                   return;
               }
//Object Graph
               if(objectGraphTypes != null && objectGraphTypes.getObjectGraph() != null){
                   recordToUpdate.setObjectGraph(objectGraphTypes.getObjectGraph());
               } else {
                   dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Object Graph is mandatory.",
                           Message.MessageType.ERROR,"Object Graph is mandatory.")));
                   return;
               }
//Data Type
               if(objectGraphTypes.getDataType()!=null){
                   ParameterDataType dataType=genericParameterService.findByCode(objectGraphTypes.getDataType().getCode(),ParameterDataType.class);
                   if(dataType!=null){
                       recordToUpdate.setDataType(dataType);
                   }else{
                       ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is Invalid", Message.MessageType.ERROR, "Please mention a valid Data Type"));
                       dataValidationRuleResults.add(validationRuleResult);
                   }
               }else{
                   ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Data Type cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                   dataValidationRuleResults.add(validationRuleResult);
               }

            }else {
                   ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Object Graph Does Not Exist.", Message.MessageType.ERROR, "Check the Object Graph display name"));
                   dataValidationRuleResults.add(validationRuleResult);
            }



            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult1 : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult1.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Object Graph Upload", "Error in Object Graph Upload").setMessages(validationMessages).build();
            }else{
                User user1 = getCurrentUser().getUserReference();
                if (recordToUpdate.getId() != null && user1 != null) {
                    entityDao.detach(recordToUpdate);
                    makerCheckerService.masterEntityChangedByUser(recordToUpdate,user1);
                }
            }
        }
    }


    private void validateObjectGraphTypes(ObjectGraphTypes objectGraphTypes,List<ValidationRuleResult> dataValidationRuleResults){

//Source Product
        if(objectGraphTypes.getSourceProduct()!=null){
            SourceProduct sourceProduct=genericParameterService.findByCode(objectGraphTypes.getSourceProduct(),SourceProduct.class);
            if(sourceProduct!=null){
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }
//Module Name
        if(objectGraphTypes.getModuleName()!=null){
            ModuleName moduleName=genericParameterService.findByCode(objectGraphTypes.getModuleName().getCode(),ModuleName.class);
            if(moduleName!=null){
                objectGraphTypes.setModuleName(moduleName);
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }

//Display Name
        if(objectGraphTypes != null && objectGraphTypes.getDisplayName() != null){
            if(checkForDuplicateDisplayName(objectGraphTypes.getDisplayName())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Object Graph name already exists", Message.MessageType.ERROR,objectGraphTypes.getDisplayName())));
            }
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Object Graph name is mandatory.",
                    Message.MessageType.ERROR,"Object Graph name is mandatory.")));
            return;
        }
//Description
        if(objectGraphTypes != null && objectGraphTypes.getDescription() != null){
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Description is mandatory.",
                    Message.MessageType.ERROR,"Description is mandatory.")));
            return;
        }
//Object Graph
        if(objectGraphTypes != null && objectGraphTypes.getObjectGraph() != null){
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Object Graph is mandatory.",
                    Message.MessageType.ERROR,"Object Graph is mandatory.")));
            return;
        }
//Data Type
        if(objectGraphTypes.getDataType()!=null){
            ParameterDataType dataType=genericParameterService.findByCode(objectGraphTypes.getDataType().getCode(),ParameterDataType.class);
            if(dataType!=null){
                objectGraphTypes.setDataType(dataType);
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is Invalid", Message.MessageType.ERROR, "Please mention a valid Data Type"));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Data Type cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }

    }

    private boolean checkForDuplicateDisplayName(Object displayName){
        boolean flag;
        String code = "displayName";
        flag = baseMasterService.hasEntity(ObjectGraphTypes.class, code, displayName);
        return flag;
    }


    public ObjectGraphTypes findRecord(String displayName){
        NamedQueryExecutor<ObjectGraphTypes> executor = new NamedQueryExecutor<ObjectGraphTypes>("ObjectGraphTypesMaster.findObjectGraphByDisplayName")
                .addParameter("displayName", displayName)
                .addParameter("approvalStatus", Arrays.asList(1,2,3,5,10));
        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(executor);
        if(CollectionUtils.isNotEmpty(objectGraphTypesList)){
            return objectGraphTypesList.get(0);
        }
        return null;
    }




}
