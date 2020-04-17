package com.nucleus.core.rules.condition;

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

import com.nucleus.rules.service.*;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.web.common.controller.CASValidationUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.util.HtmlUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 
 */
@Named("conditionUploadBusinessObj")
public class ConditionUploadBusinessObj extends BaseServiceImpl implements IConditionUploadBusinessObj {

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

    @Inject
    @Named("expressionValidation")
    ExpressionValidationService expressionValidationService;

    @Inject
    @Named(value = "expressionEvaluator")
    private ExpressionEvaluator expressionEvaluator;




    public Condition uploadCondition(Condition condition){
        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
        if(condition.getUploadOperationType()!=null){
            performMentionedOperation(condition,dataValidationRuleResults);
        }else {
            validateCondition(condition, dataValidationRuleResults);

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Condition Upload", "Error in Condition Upload").setMessages(validationMessages).build();

            } else {
                User user = getCurrentUser().getUserReference();
                condition.markActive();
                if (condition.getId() == null && user != null) {
                    makerCheckerService.masterEntityChangedByUser(condition, user);
                }
            }
        }
     return condition;
     }


    private void validateCondition(Condition condition, List<ValidationRuleResult> dataValidationRuleResults){

//Condition Code
        if(condition != null && condition.getCode() != null){
            if(checkForDuplicateCode(condition.getCode())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition code already exists", Message.MessageType.ERROR,condition.getCode())));
            }
            if (!CASValidationUtils.isAlphaNumericAndUnderScore(condition.getCode())) {
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric and underscore is allowed in Condition Code", Message.MessageType.ERROR,condition.getCode())));
            }
            if(checkForCodeLength(condition.getCode())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition Code Up to length eight is allowed", Message.MessageType.ERROR,condition.getCode())));
            }
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition code is mandatory.",
                    Message.MessageType.ERROR,"Condition code is mandatory.")));
            return;
        }

//Source Product
        if(condition.getSourceProduct()!=null){
            SourceProduct sourceProduct=genericParameterService.findByCode(condition.getSourceProduct(),SourceProduct.class);
            if(sourceProduct!=null){
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }
//Condition Name
        if(condition != null && condition.getName() != null){
            if(checkForDuplicateName(condition.getName())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition name already exists", Message.MessageType.ERROR,condition.getName())));
            }
            if (!CASValidationUtils.isAlphaNumericAndUnderScore(condition.getName())) {
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric and underscore is allowed in Condition Name", Message.MessageType.ERROR,condition.getName())));
            }
            if(checkForNameLength(condition.getName())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition Name Up to length twenty is allowed", Message.MessageType.ERROR,condition.getName())));
            }
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition name is mandatory.",
                    Message.MessageType.ERROR,"Condition name is mandatory.")));
            return;
        }
//Module Name
        if(condition.getModuleName()!=null){
            ModuleName moduleName=genericParameterService.findByCode(condition.getModuleName().getCode(),ModuleName.class);
            if(moduleName!=null){
                condition.setModuleName(moduleName);
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            condition.setModuleName(null);
        }
//Code Description
        if(condition != null && condition.getDescription() != null){
        } else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Description is mandatory.",
                    Message.MessageType.ERROR,"Description is mandatory.")));
            return;
        }
//Condition Expression
        if(condition!=null && condition.getConditionExpression()!=null) {
            String conditionExp = null;
            Map<String, Object> resultMap = convertNameExpressionToIdExpression(condition.getConditionExpression());
            List<String> invalidParameters = (List<String>) resultMap.get("invalidParameters");
            if (!invalidParameters.isEmpty()) {

                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Parameter Name is Invalid", Message.MessageType.ERROR, "Please correct the Parameter Name"));
                dataValidationRuleResults.add(validationRuleResult);

            } else {
                conditionExp = (String) resultMap.get("conditionExp");
                List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
                validationErrorsList = expressionValidationService.validateConditionExpression(conditionExp);

                if (validationErrorsList.isEmpty()) {
                    condition.setConditionExpression(conditionExp);
                    boolean phpExists = expressionEvaluator.placeHolderParamInCondition(conditionExp);
                    condition.setCriteriaConditionFlag(phpExists);
                    User user = getCurrentUser().getUserReference();
                    approveConstantParams(conditionExp,user);

                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Condition Expression is Invalid", Message.MessageType.ERROR, "Please correct the Expression"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }

        }
    }


    private void performMentionedOperation(Condition condition,List<ValidationRuleResult> dataValidationRuleResults){
        if(condition.getUploadOperationType().equalsIgnoreCase("Delete")){
            Condition deletedrecordDetails = findRecord(condition.getCode());
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
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Either Already Deleted or Already marked for Deletion.", Message.MessageType.ERROR,"Check the Condition Code"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }
            else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR,"Check the Condition Code"));
                dataValidationRuleResults.add(validationRuleResult);
            }

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Condition Upload", "Error in Condition Upload").setMessages(validationMessages).build();
            }
        } else  if(condition.getUploadOperationType().equalsIgnoreCase("Edit")){

            Condition recordToUpdate = findRecord(condition.getCode());
            if(recordToUpdate!=null){
//Condition code
                if(condition.getCode() != null) {
                    if (!CASValidationUtils.isAlphaNumericAndUnderScore(condition.getCode())) {
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric and underscore is allowed in Condition Code", Message.MessageType.ERROR,condition.getCode())));
                    }else if(checkForCodeLength(condition.getCode())){
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition Code Up to length eight is allowed", Message.MessageType.ERROR,condition.getCode())));
                    }else {
                        recordToUpdate.setCode(condition.getCode());
                    }
                } else
                {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Condition Code cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
//Condition name
                if(condition.getName() !=null)
                {
                    if (!CASValidationUtils.isAlphaNumericAndUnderScore(condition.getName())) {
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric and underscore is allowed in Condition Name", Message.MessageType.ERROR,condition.getName())));
                    }else if(checkForNameLength(condition.getName())){
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Condition Name Up to length twenty is allowed", Message.MessageType.ERROR,condition.getName())));
                    }else {
                        recordToUpdate.setName(condition.getName());
                    }
                } else
                {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Condition Name cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
//Source Product
                if(condition.getSourceProduct()!=null){
                    SourceProduct sourceProduct=genericParameterService.findByCode(condition.getSourceProduct(),SourceProduct.class);
                    if(sourceProduct!=null){
                        recordToUpdate.setSourceProduct(condition.getSourceProduct());
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }else{
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
//Module Name
                if(condition.getModuleName()!=null){
                    ModuleName moduleName=genericParameterService.findByCode(condition.getModuleName().getCode(),ModuleName.class);
                    if(moduleName!=null){
                        recordToUpdate.setModuleName(moduleName);
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }else{
                    condition.setModuleName(null);
                }

//Description
                if(condition != null && condition.getDescription() != null){
                    recordToUpdate.setDescription(condition.getDescription());
                } else {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Description is mandatory.",
                            Message.MessageType.ERROR,"Description is mandatory.")));
                    return;
                }
//Condition Expression
                if(condition!=null && condition.getConditionExpression()!=null){
                    String conditionExp =null;
                    Map<String,Object> resultMap = convertNameExpressionToIdExpression(condition.getConditionExpression());
                    List<String> invalidParameters = (List<String>) resultMap.get("invalidParameters");
                    if (!invalidParameters.isEmpty()) {

                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Parameter Name is Invalid", Message.MessageType.ERROR, "Please correct the Parameter Name"));
                        dataValidationRuleResults.add(validationRuleResult);

                    } else {
                        conditionExp = (String) resultMap.get("conditionExp");
                        List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
                        validationErrorsList = expressionValidationService.validateConditionExpression(conditionExp);

                        if (validationErrorsList.isEmpty()) {
                            recordToUpdate.setConditionExpression(conditionExp);
                            boolean phpExists = expressionEvaluator.placeHolderParamInCondition(conditionExp);
                            recordToUpdate.setCriteriaConditionFlag(phpExists);
                            User user = getCurrentUser().getUserReference();
                            approveConstantParams(conditionExp,user);
                        } else {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Condition Expression is Invalid", Message.MessageType.ERROR, "Please correct the Expression"));
                            dataValidationRuleResults.add(validationRuleResult);                        }
                    }
                }


            }else {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Condition Does Not Exist.", Message.MessageType.ERROR, "Check the Condition code"));
                dataValidationRuleResults.add(validationRuleResult);
            }



            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult1 : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult1.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Condition Upload", "Error in Condition Upload").setMessages(validationMessages).build();
            }else{
                User user1 = getCurrentUser().getUserReference();
                if (recordToUpdate.getId() != null && user1 != null) {
                    entityDao.detach(recordToUpdate);
                    makerCheckerService.masterEntityChangedByUser(recordToUpdate,user1);
                }
            }
        }
    }

    private boolean checkForDuplicateCode(Object conditionCode){
        boolean flag =false;
        String code = "code";
        flag = baseMasterService.hasEntity(Condition.class, code, conditionCode);
        return flag;
    }

    private boolean checkForDuplicateName(Object conditionName){
        boolean flag =false;
        String name = "name";
        flag = baseMasterService.hasEntity(Condition.class, name, conditionName);
        return flag;
    }


    public Condition findRecord(String conditionCode){
        NamedQueryExecutor<Condition> executor = new NamedQueryExecutor<Condition>("ConditionMaster.findConditionByConditionCode")
                .addParameter("conditionCode", conditionCode)
                .addParameter("approvalStatus", Arrays.asList(1,2,3,5,10));
        List<Condition> conditions = entityDao.executeQuery(executor);
        if(CollectionUtils.isNotEmpty(conditions)){
            return conditions.get(0);
        }
        return null;
    }

    private Map<String, Object> convertNameExpressionToIdExpression(String conditionExp) {
        conditionExp = conditionExp.trim().replaceAll("\\s+", " ").trim();
        String[] tokens = conditionExp.split(" ");
        Map<String, Object> resultMap = new HashMap<String, Object>();

        List<String> tokenList = new ArrayList<String>();
        List<String> invalidParameters = new ArrayList<String>();
        for (String token : tokens) {
            if (token != null && token.length() > 0) {
                tokenList.add(token);
            }
        }

        tokens = tokenList.toArray(new String[tokenList.size()]);
        conditionExp = " " + conditionExp + " ";

        Long paramId = null;
        for (String token : tokens) {
            if (!RuleConstants.conditionOperators.contains(token)) {
                paramId = ruleService.getParameterIdByName(token);
                if (paramId != null) {
                    conditionExp = conditionExp.replace(" " + token + " ", " " + paramId.toString() + " ");
                } else {
                    invalidParameters.add(HtmlUtils.htmlEscape(token));
                }

            }

        }
        conditionExp = conditionExp.trim();

        resultMap.put("invalidParameters", invalidParameters);
        resultMap.put("conditionExp", conditionExp);
        return resultMap;

    }

    private void approveConstantParams(String conditionExp, User user) {
        for (String tokenID : conditionExp.split(" ")) {
            if (tokenID.matches("[0-9]+")) {
                long id = Long.parseLong(tokenID);
                ConstantParameter constantParam = baseMasterService.getMasterEntityById(ConstantParameter.class, id);
                if (constantParam != null && constantParam.getApprovalStatus() == ApprovalStatus.UNAPPROVED_ADDED) {

                    if (null != constantParam.getModuleName()
                            && constantParam.getModuleName().getId() == null) {
                        constantParam.setModuleName(null);
                    }
                    if (user != null) {
                        makerCheckerService.saveAndSendForApproval(constantParam, user);
                    }
                }
            }
        }
    }

    private boolean checkForCodeLength(String conditionCode){
        boolean flag =false;
        if(conditionCode.length()>8){
            flag=true;
        }
        return flag;
    }

    private boolean checkForNameLength(String conditionName){
        boolean flag =false;
        if(conditionName.length()>20){
            flag=true;
        }
        return flag;
    }

}
