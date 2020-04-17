package com.nucleus.core.rules.rulesMaster;


import com.nucleus.core.genericparameter.service.GenericParameterService;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.Entity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.parentChildDeletionFW.BaseMasterDependencyFW;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.rules.model.*;
import com.nucleus.rules.service.*;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.web.common.controller.CASValidationUtils;
import net.sf.ehcache.hibernate.HibernateUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.web.util.HtmlUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named("ruleUploadBusinessObj")
public class RuleUploadBusinessObj extends BaseServiceImpl implements IRuleUploadBusinessObj {

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
    @Named("ruleService")
    private RuleService ruleService;

    @Inject
    @Named(value = "expressionEvaluator")
    private ExpressionEvaluator expressionEvaluator;

    @Inject
    @Named("expressionValidation")
    ExpressionValidationService expressionValidationService;

    @Inject
    @Named("sQLRuleExecutor")
    SQLRuleExecutor sqlRuleExecutor;


    public RuleVO uploadRule(RuleVO ruleVO){

        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
        if(ruleVO.getUploadOperationType()!=null){
            performMentionedOperation(ruleVO,dataValidationRuleResults);
        }else {
            Rule rule = validateAndConvertRule(ruleVO, dataValidationRuleResults);

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Rule Upload", "Error in Rule Upload").setMessages(validationMessages).build();

            } else {
                User user = getCurrentUser().getUserReference();
                rule.markActive();
                if (rule.getId() == null && user != null) {
                    makerCheckerService.masterEntityChangedByUser(rule, user);
                }
            }
        }

        return ruleVO;
    }


    private Rule validateAndConvertRule(RuleVO ruleVO, List<ValidationRuleResult> dataValidationRuleResults){
        Rule rule = new Rule();
        if(ruleVO!=null) {
            if(ruleVO.getRuleType()!=null && ruleVO.getRuleType()== RuleType.RULE_TYPE_EXPRESSION_BASED ) {
                Rule newRule = new Rule();
                convertRuleVoToRule(ruleVO,dataValidationRuleResults,newRule);
                return newRule;
            }else if(ruleVO.getRuleType()!=null && ruleVO.getRuleType()== RuleType.RULE_TYPE_SCRIPT_BASED){
                ScriptRule scriptRule =new ScriptRule();
                convertRuleVoToScriptRule(ruleVO,dataValidationRuleResults,scriptRule);
                return scriptRule;
            }else if(ruleVO.getRuleType()!=null && ruleVO.getRuleType()== RuleType.RULE_TYPE_SQL_BASED){
                SQLRule sqlRule =new SQLRule();
                convertRuleVoToSQLRule(ruleVO,dataValidationRuleResults,sqlRule);
                return sqlRule;
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule Type is invalid.", Message.MessageType.ERROR, "Specify a valid Rule Type")));
            }

        }
        return rule;
    }


    private void convertRuleVoToRule(RuleVO ruleVO,List<ValidationRuleResult> dataValidationRuleResults,Rule newRule){

        newRule.setRuleType(ruleVO.getRuleType());
//Rule Code
        if(ruleVO.getCode()!=null){
            if(checkForDuplicateCode(ruleVO.getCode())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule code already exists", Message.MessageType.ERROR,ruleVO.getCode())));
            }
            if(validRuleName(ruleVO.getCode())){
                newRule.setCode(ruleVO.getCode().trim().replaceAll("\\s+", " ").trim());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Code is invalid")));
            }
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule code is mandatory.", Message.MessageType.ERROR, "Rule code is mandatory.")));
        }
//Rule Name
        if(ruleVO.getName()!=null){
            if(validRuleName(ruleVO.getName())){
                newRule.setName(ruleVO.getName().trim().replaceAll("\\s+", " ").trim());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Name is invalid")));
            }
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule name is mandatory.", Message.MessageType.ERROR, "Rule name is mandatory.")));
        }
//Rule Description
        if(ruleVO.getDescription()!=null){
            newRule.setDescription(ruleVO.getDescription());
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule description is mandatory.", Message.MessageType.ERROR, "Rule description is mandatory.")));
        }
//Module name
        if (ruleVO.getModuleName() != null) {
            ModuleName moduleName = genericParameterService.findByCode(ruleVO.getModuleName().getCode(), ModuleName.class);
            if (moduleName != null) {
                newRule.setModuleName(moduleName);
            } else {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                dataValidationRuleResults.add(validationRuleResult);
            }
        } else {
            newRule.setModuleName(null);
        }
//Source Product
        if(ruleVO.getSourceProduct()!=null){
            SourceProduct sourceProduct=genericParameterService.findByCode(ruleVO.getSourceProduct(),SourceProduct.class);
            if(sourceProduct!=null){
                newRule.setSourceProduct(ruleVO.getSourceProduct());
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }

//Rule Expression
        if(ruleVO.getRuleExpression()!=null){
            String ruleExp=null;
            Map<String, Object> resultMap = convertNameExpressionToIdExpression(ruleVO.getRuleExpression());

            List<String> invalidConditions = (List<String>) resultMap.get("invalidConditions");
            if (!invalidConditions.isEmpty()) {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Condition Name is Invalid", Message.MessageType.ERROR, "Please correct the Condition Name"));
                dataValidationRuleResults.add(validationRuleResult);

            } else {
                ruleExp = (String) resultMap.get("ruleExp");
                List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
                validationErrorsList = expressionValidationService.validateRule(ruleExp);

                if (validationErrorsList.isEmpty()) {
                    newRule.setRuleExpression(ruleExp);
//Criteria Rule Flag
                    boolean phpExists = expressionEvaluator.placeHolderParamInRule(ruleExp);
                    newRule.setCriteriaRuleFlag(phpExists);
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Rule Expression is Invalid", Message.MessageType.ERROR, "Please correct the Expression"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }
        }
//Success Message
        if(ruleVO.getSuccessMessage()!=null){
            newRule.setSuccessMessage(ruleVO.getSuccessMessage());
        }else{
            newRule.setSuccessMessage(null);
        }
//Success Message Key
        if(ruleVO.getSuccessMessageKey()!=null){
            newRule.setSuccessMessageKey(ruleVO.getSuccessMessageKey());
        }else{
            newRule.setSuccessMessageKey(null);
        }
//Error Message
        if(ruleVO.getErrorMessage()!=null){
            newRule.setErrorMessage(ruleVO.getErrorMessage());
        }else {
            newRule.setErrorMessage(null);
        }
//Error Message Key
        if(ruleVO.getErrorMessageKey()!=null){
            newRule.setErrorMessageKey(ruleVO.getErrorMessageKey());
        }else{
            newRule.setErrorMessageKey(null);
        }
//Rule Tag Names
        if(StringUtils.isNotEmpty(ruleVO.getRuleTagNames()))
        {
            List<String> ruleTagNames = new ArrayList<>();

            String ruleTagValues = ruleVO.getRuleTagNames();
            String[] rValuesarr = ruleTagValues.split(" ",-1);
            for(int i=0; i<rValuesarr.length; i++){

                Map<String, Object> variableMap = new HashMap<String, Object>();
                variableMap.put("tagName", rValuesarr[i]);
                variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                RuleTagType ruleTagType= baseMasterService.findMasterByCode(RuleTagType.class, variableMap);

                if(ruleTagType==null){
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Rule Tag Name entered is Invalid", Message.MessageType.ERROR,rValuesarr[i]));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                ruleTagNames.add(ruleTagType.getTagName());
            }
            newRule.setRuleTagNames(ruleTagNames);
        }

    }

    private void convertRuleVoToScriptRule(RuleVO ruleVO,List<ValidationRuleResult> dataValidationRuleResults,ScriptRule scriptRule){

        scriptRule.setRuleType(ruleVO.getRuleType());
//Rule Code
        if(ruleVO.getCode()!=null){
            if(checkForDuplicateCode(ruleVO.getCode())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule code already exists", Message.MessageType.ERROR,ruleVO.getCode())));
            }
            if(validRuleName(ruleVO.getCode())){
                scriptRule.setCode(ruleVO.getCode().trim().replaceAll("\\s+", " ").trim());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Code is invalid")));
            }
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule code is mandatory.", Message.MessageType.ERROR, "Rule code is mandatory.")));
        }
//Rule Name
        if(ruleVO.getName()!=null){
            if(validRuleName(ruleVO.getName())){
                scriptRule.setName(ruleVO.getName().trim().replaceAll("\\s+", " ").trim());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Name is invalid")));
            }
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule name is mandatory.", Message.MessageType.ERROR, "Rule name is mandatory.")));
        }
//Rule Description
        if(ruleVO.getDescription()!=null){
            scriptRule.setDescription(ruleVO.getDescription());
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule description is mandatory.", Message.MessageType.ERROR, "Rule description is mandatory.")));
        }
//Module name
        if (ruleVO.getModuleName() != null) {
            ModuleName moduleName = genericParameterService.findByCode(ruleVO.getModuleName().getCode(), ModuleName.class);
            if (moduleName != null) {
                scriptRule.setModuleName(moduleName);
            } else {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                dataValidationRuleResults.add(validationRuleResult);
            }
        } else {
            scriptRule.setModuleName(null);
        }
//Source Product
        if(ruleVO.getSourceProduct()!=null){
            SourceProduct sourceProduct=genericParameterService.findByCode(ruleVO.getSourceProduct(),SourceProduct.class);
            if(sourceProduct!=null){
                scriptRule.setSourceProduct(ruleVO.getSourceProduct());
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }
//Script Code Type
        scriptRule.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT);
//Script Code
        if (ruleVO.getScriptCode()!=null) {
            scriptRule.setScriptCodeValue(ruleVO.getScriptCode());
            ruleService.encryptScriptCode(scriptRule);
        }
//Success Message
        if(ruleVO.getSuccessMessage()!=null){
            scriptRule.setSuccessMessage(ruleVO.getSuccessMessage());
        }else{
            scriptRule.setSuccessMessage(null);
        }
//Success Message Key
        if(ruleVO.getSuccessMessageKey()!=null){
            scriptRule.setSuccessMessageKey(ruleVO.getSuccessMessageKey());
        }else{
            scriptRule.setSuccessMessageKey(null);
        }
//Error Message
        if(ruleVO.getErrorMessage()!=null){
            scriptRule.setErrorMessage(ruleVO.getErrorMessage());
        }else {
            scriptRule.setErrorMessage(null);
        }
//Error Message Key
        if(ruleVO.getErrorMessageKey()!=null){
            scriptRule.setErrorMessageKey(ruleVO.getErrorMessageKey());
        }else{
            scriptRule.setErrorMessageKey(null);
        }

//Rule Tag Names
        if(StringUtils.isNotEmpty(ruleVO.getRuleTagNames()))
        {
            List<String> ruleTagNames = new ArrayList<>();

            String ruleTagValues = ruleVO.getRuleTagNames();
            String[] rValuesarr = ruleTagValues.split(" ",-1);
            for(int i=0; i<rValuesarr.length; i++){

                Map<String, Object> variableMap = new HashMap<String, Object>();
                variableMap.put("tagName", rValuesarr[i]);
                variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                RuleTagType ruleTagType= baseMasterService.findMasterByCode(RuleTagType.class, variableMap);

                if(ruleTagType==null){
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Rule Tag Name entered is Invalid", Message.MessageType.ERROR,rValuesarr[i]));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                ruleTagNames.add(ruleTagType.getTagName());
            }
            scriptRule.setRuleTagNames(ruleTagNames);
        }
    }

    private void convertRuleVoToSQLRule(RuleVO ruleVO,List<ValidationRuleResult> dataValidationRuleResults,SQLRule sqlRule){

        sqlRule.setRuleType(ruleVO.getRuleType());
//Rule Code
        if(ruleVO.getCode()!=null){
            if(checkForDuplicateCode(ruleVO.getCode())){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule code already exists", Message.MessageType.ERROR,ruleVO.getCode())));
            }
            if(validRuleName(ruleVO.getCode())){
                sqlRule.setCode(ruleVO.getCode().trim().replaceAll("\\s+", " ").trim());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Code is invalid")));
            }
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule code is mandatory.", Message.MessageType.ERROR, "Rule code is mandatory.")));
        }
//Rule Name
        if(ruleVO.getName()!=null){
            if(validRuleName(ruleVO.getName())){
                sqlRule.setName(ruleVO.getName().trim().replaceAll("\\s+", " ").trim());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Name is invalid")));
            }
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule name is mandatory.", Message.MessageType.ERROR, "Rule name is mandatory.")));
        }
//Rule Description
        if(ruleVO.getDescription()!=null){
            sqlRule.setDescription(ruleVO.getDescription());
        }else {
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule description is mandatory.", Message.MessageType.ERROR, "Rule description is mandatory.")));
        }
//Module name
        if (ruleVO.getModuleName() != null) {
            ModuleName moduleName = genericParameterService.findByCode(ruleVO.getModuleName().getCode(), ModuleName.class);
            if (moduleName != null) {
                sqlRule.setModuleName(moduleName);
            } else {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                dataValidationRuleResults.add(validationRuleResult);
            }
        } else {
            sqlRule.setModuleName(null);
        }
//Source Product
        if(ruleVO.getSourceProduct()!=null){
            SourceProduct sourceProduct=genericParameterService.findByCode(ruleVO.getSourceProduct(),SourceProduct.class);
            if(sourceProduct!=null){
                sqlRule.setSourceProduct(ruleVO.getSourceProduct());
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                dataValidationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }
//SQL Rule
        if (ruleVO.getSqlQuery()!=null) {
            String result = sqlRuleExecutor.validateSQLQuery(ruleVO.getSqlQuery());
            if(result.isEmpty()){
                sqlRule.setSqlQueryPlain(ruleVO.getSqlQuery());
                ruleService.encryptSQLRule(sqlRule);
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Please correct the query.")));
            }

            String sql = sqlRule.getSqlQueryPlain();

            List<SQLRuleParameterMapping> sqlRuleParameterMapping=new ArrayList<>();

            if(sql != null && !sql.isEmpty()){
                String[] whereClauses =StringUtils.substringsBetween(sql ,RuleConstants.LEFT_CURLY_BRACES,RuleConstants.RIGHT_CURLY_BRACES);
                if(whereClauses == null || whereClauses.length == 0){
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "SQL Query Without user input Where Clause Not Allowed")));
                }else{
                    Set<String> uniqueWhere = new HashSet<>();
                    for (int i = 0; i < whereClauses.length; i++) {
                        for(SQLRuleParameterMappingVO sqlRuleParameterMappingVO:ruleVO.getParamMapping()){
                            String whereClauseKey = whereClauses[i];
                          if(sqlRuleParameterMappingVO.getSeq()==i){
                              if(!uniqueWhere.add(whereClauseKey)){
                                  dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate placeholder", Message.MessageType.ERROR,whereClauseKey)));
                              }
                              SQLRuleParameterMapping paramMapping = new SQLRuleParameterMapping();
                              paramMapping.setPlaceHolderName(RuleConstants.LEFT_CURLY_BRACES+whereClauseKey+RuleConstants.RIGHT_CURLY_BRACES);
                              paramMapping.setSeq(i);
                              if(ruleVO.getParamMapping().get(i).getParameter()!=null){
                                  Map<String, Object> variableMap = new HashMap<String, Object>();
                                  variableMap.put("code", ruleVO.getParamMapping().get(i).getParameter().getCode());
                                  variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                                  Parameter parameter=baseMasterService.findMasterByCode(Parameter.class,variableMap);
                                  if(parameter!=null){
                                      paramMapping.setParameter(parameter);
                                  }else{
                                      dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid Parameter Code", Message.MessageType.ERROR, "Please correct the Parameter Code")));
                                  }
                              }
                              sqlRuleParameterMapping.add(paramMapping);
                          }
                        }
                    }
                    sqlRule.setParamMapping(sqlRuleParameterMapping);
                }
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Blank SQL Query.")));
            }
        }
//Success Message
        if(ruleVO.getSuccessMessage()!=null){
            sqlRule.setSuccessMessage(ruleVO.getSuccessMessage());
        }else{
            sqlRule.setSuccessMessage(null);
        }
//Success Message Key
        if(ruleVO.getSuccessMessageKey()!=null){
            sqlRule.setSuccessMessageKey(ruleVO.getSuccessMessageKey());
        }else{
            sqlRule.setSuccessMessageKey(null);
        }
//Error Message
        if(ruleVO.getErrorMessage()!=null){
            sqlRule.setErrorMessage(ruleVO.getErrorMessage());
        }else {
            sqlRule.setErrorMessage(null);
        }
//Error Message Key
        if(ruleVO.getErrorMessageKey()!=null){
            sqlRule.setErrorMessageKey(ruleVO.getErrorMessageKey());
        }else{
            sqlRule.setErrorMessageKey(null);
        }

//Rule Tag Names
        if(StringUtils.isNotEmpty(ruleVO.getRuleTagNames()))
        {
            List<String> ruleTagNames = new ArrayList<>();

            String ruleTagValues = ruleVO.getRuleTagNames();
            String[] rValuesarr = ruleTagValues.split(" ",-1);
            for(int i=0; i<rValuesarr.length; i++){

                Map<String, Object> variableMap = new HashMap<String, Object>();
                variableMap.put("tagName", rValuesarr[i]);
                variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                RuleTagType ruleTagType= baseMasterService.findMasterByCode(RuleTagType.class, variableMap);

                if(ruleTagType==null){
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Rule Tag Name entered is Invalid", Message.MessageType.ERROR,rValuesarr[i]));
                    dataValidationRuleResults.add(validationRuleResult);
                }
                ruleTagNames.add(ruleTagType.getTagName());
            }
            sqlRule.setRuleTagNames(ruleTagNames);
        }
    }




    private void performMentionedOperation(RuleVO ruleVO,List<ValidationRuleResult> dataValidationRuleResults) {
        if (ruleVO.getUploadOperationType().equalsIgnoreCase("Delete")) {
            Rule deletedrecordDetails = findRecord(ruleVO.getCode());
            if (deletedrecordDetails != null) {
                if (deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.UNAPPROVED_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.DELETED_APPROVED_IN_HISTORY && deletedrecordDetails.getApprovalStatus() != ApprovalStatus.APPROVED_DELETED_IN_PROGRESS) {
                    if(!BaseMasterDependencyFW.isDependencyPresent(deletedrecordDetails.getClass(),deletedrecordDetails.getId())) {
                        entityDao.detach(deletedrecordDetails);
                        User user1 = getCurrentUser().getUserReference();
                        EntityId updatedById = user1.getEntityId();
                        makerCheckerService.masterEntityMarkedForDeletion(deletedrecordDetails, updatedById);
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record " + deletedrecordDetails.getCode() +
                                " is being used by a parent Master", Message.MessageType.ERROR,"Check usage section under activity"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Either Already Deleted or Already marked for Deletion.", Message.MessageType.ERROR, "Check the Rule Code"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            } else {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR, "Check the Rule Code"));
                dataValidationRuleResults.add(validationRuleResult);
            }

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Rule Upload", "Error in Rule Upload").setMessages(validationMessages).build();
            }
        }
//Edit
        else if (ruleVO.getUploadOperationType().equalsIgnoreCase("Edit")) {

            Rule recordToUpdate = findRecord(ruleVO.getCode());
            if(recordToUpdate.getRuleTagNames()!=null){
                HibernateUtils.initializeAndUnproxy(recordToUpdate.getRuleTagNames());
                if(recordToUpdate.getRuleTagNames().size()==0){
                    recordToUpdate.setRuleTagNames(null);
                }
            }

            if (null != recordToUpdate.getRuntimeRuleMapping()) {
                HibernateUtils.initializeAndUnproxy(recordToUpdate.getRuntimeRuleMapping().getParameters());
                if(recordToUpdate.getRuntimeRuleMapping().getParameters().size()==0){
                    recordToUpdate.getRuntimeRuleMapping().setParameters(null);
                }
                HibernateUtils.initializeAndUnproxy(recordToUpdate.getRuntimeRuleMapping().getObjectGraphs());
                if(recordToUpdate.getRuntimeRuleMapping().getObjectGraphs().size()==0){
                    recordToUpdate.getRuntimeRuleMapping().setObjectGraphs(null);
                }
            }
            if (recordToUpdate != null) {
//Rule Code
                if (ruleVO.getCode() != null) {
                    if(validRuleName(ruleVO.getCode())){
                        recordToUpdate.setCode(ruleVO.getCode().trim().replaceAll("\\s+", " ").trim());
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Code is invalid")));
                    }
                } else {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule code is mandatory.", Message.MessageType.ERROR, "Rule code is mandatory.")));
                }
//Rule Name
                if (ruleVO.getName() != null) {
                    if(validRuleName(ruleVO.getName())){
                        recordToUpdate.setName(ruleVO.getName().trim().replaceAll("\\s+", " ").trim());
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please Enter Only alphanumeric,underscore,hyphen or space", Message.MessageType.ERROR, "Rule Name is invalid")));
                    }
                } else {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule name is mandatory.", Message.MessageType.ERROR, "Rule name is mandatory.")));
                }
//Rule Description
                if (ruleVO.getDescription() != null) {
                    recordToUpdate.setDescription(ruleVO.getDescription());
                } else {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Rule description is mandatory.", Message.MessageType.ERROR, "Rule description is mandatory.")));
                }
//Module name
                if (ruleVO.getModuleName() != null) {
                    ModuleName moduleName = genericParameterService.findByCode(ruleVO.getModuleName().getCode(), ModuleName.class);
                    if (moduleName != null) {
                        recordToUpdate.setModuleName(moduleName);
                    } else {
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                } else {
                    recordToUpdate.setModuleName(null);
                }
//Source Product
                if(ruleVO.getSourceProduct()!=null){
                    SourceProduct sourceProduct=genericParameterService.findByCode(ruleVO.getSourceProduct(),SourceProduct.class);
                    if(sourceProduct!=null){
                        recordToUpdate.setSourceProduct(ruleVO.getSourceProduct());
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }else{
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }
//Success Message
                if(ruleVO.getSuccessMessage()!=null){
                    Map<String,String> errorMap = ruleService.checkParanthesisForErrorMessage(ruleVO.getSuccessMessage());
                    if(MapUtils.isNotEmpty(errorMap)){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage(errorMap.get(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND),Message.MessageType.ERROR,"Please provide correct success message"));
                    }
                    else {
                        recordToUpdate.setSuccessMessage(ruleVO.getSuccessMessage());
                    }
                }else{
                    recordToUpdate.setSuccessMessage(null);
                }
//Success Message Key
                if(ruleVO.getSuccessMessageKey()!=null){
                    recordToUpdate.setSuccessMessageKey(ruleVO.getSuccessMessageKey());
                }else{
                    recordToUpdate.setSuccessMessageKey(null);
                }
//Error Message
                if(ruleVO.getErrorMessage()!=null){
                    Map<String,String> errorMap = ruleService.checkParanthesisForErrorMessage(ruleVO.getErrorMessage());
                    if(MapUtils.isNotEmpty(errorMap)){
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage(errorMap.get(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND),Message.MessageType.ERROR,"Please provide correct error message"));
                    }
                    else {
                        recordToUpdate.setErrorMessage(ruleVO.getErrorMessage());
                    }
                }else {
                    recordToUpdate.setErrorMessage(null);
                }
//Error Message Key
                if(ruleVO.getErrorMessageKey()!=null){
                    recordToUpdate.setErrorMessageKey(ruleVO.getErrorMessageKey());
                }else{
                    recordToUpdate.setErrorMessageKey(null);
                }

//Rule Tag Names
                if(StringUtils.isNotEmpty(ruleVO.getRuleTagNames()))
                {
                    List<String> ruleTagNames = new ArrayList<>();

                    String ruleTagValues = ruleVO.getRuleTagNames();
                    String[] rValuesarr = ruleTagValues.split(" ",-1);
                    for(int i=0; i<rValuesarr.length; i++){

                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        variableMap.put("tagName", rValuesarr[i]);
                        variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                        RuleTagType ruleTagType= baseMasterService.findMasterByCode(RuleTagType.class, variableMap);

                        if(ruleTagType==null){
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Rule Tag Name entered is Invalid", Message.MessageType.ERROR,rValuesarr[i]));
                            dataValidationRuleResults.add(validationRuleResult);
                        }
                        ruleTagNames.add(ruleTagType.getTagName());
                    }
                    if(recordToUpdate.getRuleTagNames()!=null){
                        recordToUpdate.getRuleTagNames().clear();
                        recordToUpdate.getRuleTagNames().addAll(ruleTagNames);
                    }else{
                        recordToUpdate.setRuleTagNames(ruleTagNames);
                    }
                }

                if(recordToUpdate instanceof ScriptRule){

                    ScriptRule scriptRule =(ScriptRule) recordToUpdate;
//Script Code Type
                    scriptRule.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT);
//Script Code
                    if (ruleVO.getScriptCode()!=null) {
                        scriptRule.setScriptCodeValue(ruleVO.getScriptCode());
                        ruleService.encryptScriptCode(scriptRule);
                    }

                }else if(recordToUpdate instanceof SQLRule){

                    SQLRule sqlRule = (SQLRule) recordToUpdate;
//SQL Rule
                    if (ruleVO.getSqlQuery()!=null) {
                        String result = sqlRuleExecutor.validateSQLQuery(ruleVO.getSqlQuery());
                        if(result.isEmpty()){
                            sqlRule.setSqlQueryPlain(ruleVO.getSqlQuery());
                            ruleService.encryptSQLRule(sqlRule);
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Please correct the query.")));
                        }

                        String sql = sqlRule.getSqlQueryPlain();

                        List<SQLRuleParameterMapping> sqlRuleParameterMapping=new ArrayList<>();

                        if(sql != null && !sql.isEmpty()){
                            String[] whereClauses =StringUtils.substringsBetween(sql ,RuleConstants.LEFT_CURLY_BRACES,RuleConstants.RIGHT_CURLY_BRACES);
                            if(whereClauses == null || whereClauses.length == 0){
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "SQL Query Without user input Where Clause Not Allowed")));
                            }else{
                                Set<String> uniqueWhere = new HashSet<>();
                                for (int i = 0; i < whereClauses.length; i++) {
                                    for(SQLRuleParameterMappingVO sqlRuleParameterMappingVO:ruleVO.getParamMapping()){
                                        String whereClauseKey = whereClauses[i];
                                        if(sqlRuleParameterMappingVO.getSeq()==i){
                                            if(!uniqueWhere.add(whereClauseKey)){
                                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate placeholder", Message.MessageType.ERROR,whereClauseKey)));
                                            }
                                            SQLRuleParameterMapping paramMapping = new SQLRuleParameterMapping();
                                            paramMapping.setPlaceHolderName(RuleConstants.LEFT_CURLY_BRACES+whereClauseKey+RuleConstants.RIGHT_CURLY_BRACES);
                                            paramMapping.setSeq(i);
                                            if(ruleVO.getParamMapping().get(i).getParameter()!=null){
                                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                                variableMap.put("code", ruleVO.getParamMapping().get(i).getParameter().getCode());
                                                variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                                                Parameter parameter=baseMasterService.findMasterByCode(Parameter.class,variableMap);
                                                if(parameter!=null){
                                                    paramMapping.setParameter(parameter);
                                                }else{
                                                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid Parameter Code", Message.MessageType.ERROR, "Please correct the Parameter Code")));
                                                }
                                            }
                                            sqlRuleParameterMapping.add(paramMapping);
                                        }
                                    }
                                }
                                sqlRule.setParamMapping(sqlRuleParameterMapping);
                            }
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Blank SQL Query.")));
                        }
                    }


                }else{
                    Rule newRule = (Rule) recordToUpdate;
//Rule Expression
                    if(ruleVO.getRuleExpression()!=null){
                        String ruleExp=null;
                        Map<String, Object> resultMap = convertNameExpressionToIdExpression(ruleVO.getRuleExpression());

                        List<String> invalidConditions = (List<String>) resultMap.get("invalidConditions");
                        if (!invalidConditions.isEmpty()) {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Condition Name is Invalid", Message.MessageType.ERROR, "Please correct the Condition Name"));
                            dataValidationRuleResults.add(validationRuleResult);

                        } else {
                            ruleExp = (String) resultMap.get("ruleExp");
                            List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
                            validationErrorsList = expressionValidationService.validateRule(ruleExp);

                            if (validationErrorsList.isEmpty()) {
                                newRule.setRuleExpression(ruleExp);
//Criteria Rule Flag
                                boolean phpExists = expressionEvaluator.placeHolderParamInRule(ruleExp);
                                newRule.setCriteriaRuleFlag(phpExists);
                            } else {
                                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Rule Expression is Invalid", Message.MessageType.ERROR, "Please correct the Expression"));
                                dataValidationRuleResults.add(validationRuleResult);
                            }
                        }
                    }
                }
            } else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Either Empty or Invalid - ", Message.MessageType.ERROR,"Rule Code"));
                dataValidationRuleResults.add(validationRuleResult);
            }

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult1 : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult1.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Rule Upload", "Error in Rule Upload").setMessages(validationMessages).build();
            } else {
                User user1 = getCurrentUser().getUserReference();
                if (recordToUpdate.getId() != null && user1 != null) {
                    entityDao.detach(recordToUpdate);
                    makerCheckerService.masterEntityChangedByUser(recordToUpdate, user1);
                }
            }
        }
    }

    public Rule findRecord(String ruleCode){
        NamedQueryExecutor<Rule> executor = new NamedQueryExecutor<Rule>("RuleMaster.findRuleByRuleCode")
                .addParameter("ruleCode", ruleCode)
                .addParameter("approvalStatus", Arrays.asList(1,2,3,5,10));
        List<Rule> rules = entityDao.executeQuery(executor);
        if(CollectionUtils.isNotEmpty(rules)){
            return rules.get(0);
        }
        return null;
    }


    private Map<String, Object> convertNameExpressionToIdExpression(String ruleExp) {
        ruleExp = ruleExp.trim().replaceAll("\\s+", " ").trim();
        String[] tokens = ruleExp.split(" ");
        Map<String, Object> resultMap = new HashMap<String, Object>();

        List<String> tokenList = new ArrayList<String>();
        List<String> invalidConditions = new ArrayList<String>();
        for (String token : tokens) {
            if (token != null && token.length() > 0) {
                tokenList.add(token);
            }
        }

        tokens = tokenList.toArray(new String[tokenList.size()]);
        ruleExp = " " + ruleExp + " ";
        Long paramId = null;
        for (String token : tokens) {
            if (!RuleConstants.ruleOperators.contains(token)) {
                paramId = ruleService.getConditionIdByName(token);
                if (paramId != null) {
                    ruleExp = ruleExp.replace(" " + token + " ", " " + paramId.toString() + " ");
                } else {
                    invalidConditions.add(HtmlUtils.htmlEscape(token));
                }

            }

        }
        ruleExp = ruleExp.trim();

        resultMap.put("invalidConditions", invalidConditions);
        resultMap.put("ruleExp", ruleExp);
        return resultMap;

    }

    private boolean validRuleName(String ruleName){
        String regex = "^[A-Za-z0-9_\\- ]*$";
        return ruleName.matches(regex);
    }

    private List<Integer> getStatusList(){
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(PersistenceStatus.ACTIVE);
        return statusList;
    }

    private boolean checkForDuplicateCode(Object ruleCode){
        boolean flag =false;
        String code = "code";
        flag = baseMasterService.hasEntity(Rule.class, code, ruleCode);
        return flag;
    }


}
