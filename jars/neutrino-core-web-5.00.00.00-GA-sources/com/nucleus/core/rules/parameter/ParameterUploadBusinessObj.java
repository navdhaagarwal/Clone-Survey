package com.nucleus.core.rules.parameter;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.rules.rulesMaster.RuleVO;
import com.nucleus.core.rules.rulesMaster.SQLRuleParameterMappingVO;
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
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.parentChildDeletionFW.BaseMasterDependencyFW;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.*;
import com.nucleus.rules.service.*;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.web.common.controller.CASValidationUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.AggregateFunction;
import org.apache.poi.ss.formula.functions.T;
import org.docx4j.wml.P;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.util.HtmlUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Named("parameterUploadBusinessObj")
public class ParameterUploadBusinessObj extends BaseServiceImpl implements IParameterUploadBusinessObj{

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
    @Named("expressionValidation")
    ExpressionValidationService expressionValidationService;

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder   compiledExpressionBuilder;

    @Inject
    @Named("sQLRuleExecutor")
    SQLRuleExecutor sqlRuleExecutor;

    @Inject
    @Named("parameterService")
    private ParameterService parameterService;

    private static final String         DELETE                = "delete";

    private static final String         UPDATE                = "update";

    private static final String         INSERT                = "insert";

    private final String[]      PARAMETER_NAME_VALIDATOR = { "$", "-", "/", "//", "(", ")", "#", "!", "@", "%", "^", "&",
            "=", ",", ".", "?"                          };



    public ParameterVO uploadParameter(ParameterVO parameterVO){

        List<ValidationRuleResult> dataValidationRuleResults = new ArrayList<ValidationRuleResult>();
        if(parameterVO.getUploadOperationType()!=null){
            performMentionedOperation(parameterVO,dataValidationRuleResults);
        }else {
            Parameter parameter = validateAndConvertParameter(parameterVO, dataValidationRuleResults);

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Parameter Upload", "Error in Parameter Upload").setMessages(validationMessages).build();

            } else {
                User user = getCurrentUser().getUserReference();
                parameter.markActive();
                if (parameter.getId() == null && user != null) {
                    makerCheckerService.masterEntityChangedByUser(parameter, user);
                }
            }
        }

       return  parameterVO;
    }



    private Parameter validateAndConvertParameter(ParameterVO parameterVO, List<ValidationRuleResult> dataValidationRuleResults){
        Parameter parameter = new Parameter();
        if(parameterVO!=null) {
            if(parameterVO.getParamType()!=null && parameterVO.getParamType()== ParameterType.PARAMETER_TYPE_CONSTANT) {
                ConstantParameter cp = new ConstantParameter();
                convertParameterVoToConstantParameter(parameterVO,dataValidationRuleResults,cp);
                return cp;
            }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()== ParameterType.PARAMETER_TYPE_REFERENCE){
                ReferenceParameter rp =new ReferenceParameter();
                convertParameterVoToReferenceParameter(parameterVO,dataValidationRuleResults,rp);
                return rp;
           }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()==ParameterType.PARAMETER_TYPE_OBJECT_GRAPH){
                 ObjectGraphParameter og =new ObjectGraphParameter();
                 convertParameterVoToObjectGraphParameter(parameterVO,dataValidationRuleResults,og);
                 return og;
           }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()==ParameterType.PARAMETER_TYPE_COMPOUND){
                CompoundParameter cop =new CompoundParameter();
                convertParameterVoToCompoundParameter(parameterVO,dataValidationRuleResults,cop);
                return cop;
           }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()==ParameterType.PARAMETER_TYPE_PLACEHOLDER){
                PlaceHolderParameter pp =new PlaceHolderParameter();
                convertParameterVoToPlaceHolderParameter(parameterVO,dataValidationRuleResults,pp);
                return pp;
           }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()==ParameterType.PARAMETER_TYPE_SCRIPT){
                ScriptParameter sp =new ScriptParameter();
                convertParameterVoToScriptParameter(parameterVO,dataValidationRuleResults,sp);
                return sp;
           }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()==ParameterType.PARAMETER_TYPE_QUERY){
                QueryParameter qp = new QueryParameter();
                convertParameterVoToQueryParameter(parameterVO,dataValidationRuleResults,qp);
                return qp;

           }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()==ParameterType.PARAMETER_TYPE_DERIVED){
                DerivedParameter dp = new DerivedParameter();
                convertParameterVoToDerivedParameter(parameterVO,dataValidationRuleResults,dp);
                return dp;
           }else if(parameterVO.getParamType()!=null && parameterVO.getParamType()==ParameterType.PARAMETER_TYPE_SQL){
                SQLParameter sp = new SQLParameter();
                convertParameterVoToSQLParameter(parameterVO,dataValidationRuleResults,sp);
                return sp;
            } else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter Type is invalid.", Message.MessageType.ERROR, "Specify a valid Parameter Type")));
            }

        }
        return parameter;
    }

    private void convertParameterVoToConstantParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,ConstantParameter cp){

        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,cp);

//Data Type
        if(parameterVO.getDataType()!=null){
            cp.setDataType(parameterVO.getDataType());
            if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                if (!CASValidationUtils.isAlphaNumericAndUnderScore(parameterVO.getLiteral())){
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only Alphanumeric value is allowed", Message.MessageType.ERROR,parameterVO.getLiteral())));
                }else{
                    cp.setLiteral(parameterVO.getLiteral());
                }
            } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                if(StringUtils.isNumeric(parameterVO.getLiteral())){
                    cp.setLiteral(parameterVO.getLiteral());
                }else{
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid number", Message.MessageType.ERROR,parameterVO.getLiteral())));
                }
            } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                if(CASValidationUtils.isValidBoolean(parameterVO.getLiteral())){
                    cp.setLiteral(parameterVO.getLiteral());
                }else{
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter true or false", Message.MessageType.ERROR,parameterVO.getLiteral())));
                }

            } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                if(CASValidationUtils.isValidDate(parameterVO.getLiteral())){
                    cp.setLiteral(parameterVO.getLiteral());
                }else{
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid date", Message.MessageType.ERROR,parameterVO.getLiteral())));
                }
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
            }
        }else{
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
        }


    }

    private void convertParameterVoToObjectGraphParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,ObjectGraphParameter og){
        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,og);

        List<ObjectGraphTypes> objectGraphTypesList = null;
        if(og.getSourceProduct()!=null && og.getModuleName()!=null){
            objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(og.getSourceProduct(), og.getModuleName().getId());
        } else {
            objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(og.getSourceProduct());
        }


//Object Graph
        if(parameterVO.getObjectGraph()!=null){
           ObjectGraphTypes objectGraphObject = findObjectGraphObject(parameterVO.getObjectGraph());
           boolean found=false;
            if(objectGraphObject!=null) {
                if (objectGraphTypesList != null) {
                    for (ObjectGraphTypes ob : objectGraphTypesList) {
                        if (parameterVO.getObjectGraph().equals(ob.getDisplayName())) {
                            found = true;
                            break;
                        }
                    }
                    if (found == false) {
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Given Object Graph not valid", Message.MessageType.ERROR, "Please mention object graph according to source system and module name selected")));
                    }
                }
                String objectGraph = objectGraphObject.getObjectGraph();
                ParameterDataType dataType = objectGraphObject.getDataType();
                if (objectGraph != null && dataType != null) {
                    og.setObjectGraph(objectGraph);
                    og.setDataType(Integer.parseInt(dataType.getCode()));
                }
            } else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Not a valid Object Graph", Message.MessageType.ERROR, "Please mention a valid object graph name")));
            }

        }else{
           dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Object Graph cannot be Left Blank", Message.MessageType.ERROR, "Object Graph is mandatory")));
        }

        if (og.getObjectGraph().contains(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)) {
            og.setCollectionBased(true);
        }



    }


    private void convertParameterVoToReferenceParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,ReferenceParameter rp){
        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,rp);

//Data Type
        rp.setDataType(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE);
//Entity Type and Reference URI
        if(parameterVO.getEntityType().getDisplayEntityName()!=null){

            Map<String, Object> variableMap = new HashMap<String, Object>();
            variableMap.put("displayEntityName", parameterVO.getEntityType().getDisplayEntityName());
            variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
            EntityType entityType= baseMasterService.findMasterByCode(EntityType.class, variableMap);

            if(entityType!=null && entityType.getFields()!=null){
                Entity entity = null;
                String[]fields=entityType.getFields().split(",");
                Map<String,Object> map=new HashMap<>();
                for (String field:fields){
                    if(StringUtils.isNotEmpty(parameterVO.getReference())){
                        map.put(field,parameterVO.getReference());
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Reference Value is mandatory.", Message.MessageType.ERROR, "Please fill a reference value for corresponding Entity Type")));
                    }
                }
                try {
                    Class className = Class.forName(entityType.getClassName());
                    entity = baseMasterService.findMasterByCode(className, map, false);
                } catch (ClassNotFoundException e) {
                    BaseLoggers.flowLogger.error("Exception occurred in finding entity class {}", e);
                }
                if(entity != null){
                    String uri=entity.getUri();
                    rp.setReferenceEntityId(EntityId.fromUri(uri));
                }
            }
        }else{
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Entity Type is mandatory.", Message.MessageType.ERROR, "Please fill an Entity Type")));
        }


    }


    private void convertParameterVoToCompoundParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,CompoundParameter cop){
        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,cop);

//Data Type
        if(parameterVO.getDataType()!=null){
            if(parameterVO.getDataType()==ParameterDataType.PARAMETER_DATA_TYPE_STRING || parameterVO.getDataType()==ParameterDataType.PARAMETER_DATA_TYPE_NUMBER){
                cop.setDataType(parameterVO.getDataType());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is invalid", Message.MessageType.ERROR, "Only Alphanumeric and Number allowed")));
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Data Type cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            dataValidationRuleResults.add(validationRuleResult);
        }
//Parameter Expression
        if(parameterVO.getParameterExpression()!=null){
            String parameterExp=null;
            Map<String, Object> resultMap = convertNameExpressionToIdExpression(parameterVO.getParameterExpression());

            List<String> invalidParameters = (List<String>) resultMap.get("invalidParameters");
            if (!invalidParameters.isEmpty()) {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Parameter Name is Invalid", Message.MessageType.ERROR, "Please correct the Parameter Name"));
                dataValidationRuleResults.add(validationRuleResult);

            } else {
                parameterExp = (String) resultMap.get("parameterExp");
                List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
                validationErrorsList = expressionValidationService.validateCompoundParameterExpression(parameterExp,parameterVO.getDataType());

                if (validationErrorsList.isEmpty()) {
                    cop.setParameterExpression(parameterExp);
                    User user = getCurrentUser().getUserReference();
                    approveConstantParams(parameterExp, user);
                } else {
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Parameter Expression is Invalid", Message.MessageType.ERROR, "Please correct the Expression"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            }
        }

    }


    private void convertParameterVoToPlaceHolderParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,PlaceHolderParameter pp){
        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,pp);

//Context Name
        if(parameterVO.getContextName()!=null){
            if (!CASValidationUtils.isAlphaNumericAndUnderScore(parameterVO.getContextName())) {
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric and underscore is allowed in Context Name", Message.MessageType.ERROR,parameterVO.getContextName())));
            }else{
                pp.setContextName(parameterVO.getContextName());
            }
            if(parameterVO.getObjectGraph()!=null){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Either select context name or object graph", Message.MessageType.ERROR,"Specify one field")));
            }

        }
//Object Graph
        else if(parameterVO.getObjectGraph()!=null){
            if(parameterVO.getContextName()!=null){
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Either select context name or object graph", Message.MessageType.ERROR,"Specify one field")));
            }
            List<ObjectGraphTypes> objectGraphTypesList = null;
            if(pp.getSourceProduct()!=null && pp.getModuleName()!=null){
                objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(pp.getSourceProduct(), pp.getModuleName().getId());
            } else {
                objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(pp.getSourceProduct());
            }
                ObjectGraphTypes objectGraphObject = findObjectGraphObject(parameterVO.getObjectGraph());
                boolean found=false;
                if(objectGraphObject!=null) {
                    if (objectGraphTypesList != null) {
                        for (ObjectGraphTypes ob : objectGraphTypesList) {
                            if (parameterVO.getObjectGraph().equals(ob.getDisplayName())) {
                                found = true;
                                break;
                            }
                        }
                        if (found == false) {
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Given Object Graph not valid", Message.MessageType.ERROR, "Please mention object graph according to source system and module name selected")));
                        }
                    }
                    String objectGraph = objectGraphObject.getObjectGraph();
                    ParameterDataType dataType = objectGraphObject.getDataType();
                    if (objectGraph != null && dataType != null) {
                        pp.setObjectGraph(objectGraph);
                        pp.setDataType(Integer.parseInt(dataType.getCode()));
                    }
                } else{
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Not a valid Object Graph", Message.MessageType.ERROR, "Please mention a valid object graph name")));
                }
        }else{
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Either fill context name or object graph", Message.MessageType.ERROR,"Specify atleast one field")));
        }
    }

    private void convertParameterVoToScriptParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,ScriptParameter sp){
        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,sp);

//Data Type
        if(parameterVO.getDataType()!=null){
            if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                sp.setDataType(parameterVO.getDataType());
            } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                sp.setDataType(parameterVO.getDataType());
            } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                sp.setDataType(parameterVO.getDataType());
            } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                sp.setDataType(parameterVO.getDataType());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
            }
        }else{
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
        }
//Script Code Type
        sp.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT);
//Script Code
        if (parameterVO.getScriptCode()!=null) {
            sp.setScriptCodeValue(parameterVO.getScriptCode());
            ruleService.encryptScriptCode(sp);
        }

    }


    private void convertParameterVoToQueryParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,QueryParameter qp){
        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,qp);

//Data Type
        if(parameterVO.getDataType()!=null){
            if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER
                    || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE
                    || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                qp.setDataType(parameterVO.getDataType());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
            }
        }else{
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
        }
// Query and Query Parameter Attributes
        if(parameterVO.getQuery()!=null){
           if(queryContainsText(parameterVO.getQuery(), INSERT, UPDATE, DELETE)){
               dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Query is invalid.", Message.MessageType.ERROR, "Enquiries about insertion, update, and deletion are not allowed.")));
           }else{
               qp.setQuery(parameterVO.getQuery());

               String query = parameterVO.getQuery();

               if(query.indexOf(":")!=-1){
                   List<QueryParameterAttribute> queryParameterAttributeList = new ArrayList<>();
                   for(QueryDerivedParameterVO q:parameterVO.getQueryDerivedParameterVO()){
                       while (query.indexOf(":") != -1) {
                           query = query.substring(query.indexOf(":") + 1);
                           String[] abc = query.split(" ");

                           if(abc[0].equalsIgnoreCase(q.getQueryParameterName())){
                                QueryParameterAttribute queryParameterAttribute=new QueryParameterAttribute();
                                queryParameterAttribute.setQueryParameterName(q.getQueryParameterName());

                               List<ObjectGraphTypes> objectGraphTypesList = null;
                               if(qp.getSourceProduct()!=null && qp.getModuleName()!=null){
                                   objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(qp.getSourceProduct(), qp.getModuleName().getId());
                               } else {
                                   objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(qp.getSourceProduct());
                               }
                               ObjectGraphTypes objectGraphObject = findObjectGraphObject(q.getQueryObjectGraph());
                               boolean found=false;
                               if(objectGraphObject!=null) {
                                   if (objectGraphTypesList != null) {
                                       for (ObjectGraphTypes ob : objectGraphTypesList) {
                                           if (q.getQueryObjectGraph().equals(ob.getDisplayName())) {
                                               found = true;
                                               break;
                                           }
                                       }
                                       if (found == false) {
                                           dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Given Object Graph not valid", Message.MessageType.ERROR, "Please mention object graph according to source system and module name selected")));
                                       }
                                   }
                                   String objectGraph = objectGraphObject.getObjectGraph();
                                   if (objectGraph != null) {
                                       queryParameterAttribute.setObjectGraph(objectGraph);
                                   }
                               } else{
                                   dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Not a valid Object Graph", Message.MessageType.ERROR, "Please mention a valid object graph name")));
                               }
                            queryParameterAttributeList.add(queryParameterAttribute);
                           }
                       }
                   }
                   qp.setQueryParameterAttributes(queryParameterAttributeList);
               }
           }
        }
    }



    private void convertParameterVoToDerivedParameter(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults,DerivedParameter dp){
       setCommonInfoForParameters(parameterVO,dataValidationRuleResults,dp);
//Data Type
        if(parameterVO.getDataType()!=null){
            List<ObjectGraphParameter> objectGraphParameterListCollection =ruleService.getCollectionTypGraphByDataType(Integer.valueOf(
                    ParameterDataType.PARAMETER_DATA_TYPE_COLLECTION).toString());
            List<ObjectGraphParameter> objectGraphParameterList1 =ruleService.getCollectionTypGraphByDataType(parameterVO.getDataType().toString());
            Map<String, String> aggregateFunctionMap=RuleConstants.MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION;
            String objectGraph=null;
            boolean found=false;
            boolean aggregateFound=false;
            if(parameterVO.getTargetObjectGraph()!=null){
                Map<String, Object> variableMap = new HashMap<String, Object>();
                variableMap.put("name", parameterVO.getTargetObjectGraph());
                Parameter parameter= baseMasterService.findMasterByCode(Parameter.class, variableMap);
                if(parameter!=null && parameter instanceof ObjectGraphParameter){
                    objectGraph =((ObjectGraphParameter)parameter).getObjectGraph();
                }
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Return Value is mandatory", Message.MessageType.ERROR,"Please fill target object graph parameter")));
            }
            if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING ||
                  parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE ||
                    parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE
                    ||parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                dp.setDataType(parameterVO.getDataType());
                for(ObjectGraphParameter o:objectGraphParameterList1){
                   if(o.getObjectGraph().equalsIgnoreCase(objectGraph)){
                       found=true;
                       dp.setTargetObjectGraph(o);
                       break;
                   }
                }
                if(found==false) {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Return Value is not according to Data Type mentioned", Message.MessageType.ERROR, "Please fill valid Target object graph")));
                }
                if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                    if(parameterVO.getAggregateFunction()!=null){
                        for(Map.Entry<String,String> entry:aggregateFunctionMap.entrySet()){
                            if(entry.getValue().equalsIgnoreCase(parameterVO.getAggregateFunction())){
                                aggregateFound=true;
                                dp.setAggregateFunction(entry.getKey());
                                break;
                            }
                        }
                        if(aggregateFound==false){
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Aggregate Function value is not valid", Message.MessageType.ERROR,"It can only be Sum, Average, Max or Min")));
                        }
                    }
                }

            }else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                dp.setDataType(parameterVO.getDataType());
                if(parameterVO.getEntityField()==null){
                    parameterVO.setEntityField(true);
                }
                List<ObjectGraphParameter> finalList=new ArrayList<>();
                if(parameterVO.getEntityField()!=null && parameterVO.getEntityField().equals(false)){
                   finalList = objectGraphParameterListCollection;
                }else if(parameterVO.getEntityField()!=null && parameterVO.getEntityField().equals(true)){
                    finalList = objectGraphParameterList1;
                }
                for(ObjectGraphParameter o:finalList){
                    if(o.getObjectGraph().equalsIgnoreCase(objectGraph)){
                        found=true;
                        dp.setTargetObjectGraph(o);
                        break;
                    }
                }
                if(found==false){
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Return Value is not according to Data Type mentioned", Message.MessageType.ERROR,"Please fill valid Target object graph")));
                }
                if(parameterVO.getEntityField()!=null){
                    if(parameterVO.getEntityField().equals(true)){
                        dp.setEntityField(parameterVO.getEntityField());
                        dp.setValidateOnAll(false);
                    }else if(parameterVO.getEntityField().equals(false)){
                        dp.setEntityField(parameterVO.getEntityField());
                        if(parameterVO.getValidateOnAll()!=null){
                            dp.setValidateOnAll(parameterVO.getValidateOnAll());
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Validate On all value is required in case of Entity Field false", Message.MessageType.ERROR,"Please fill true or false")));
                        }
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid value of Entity Field", Message.MessageType.ERROR,"Please fill true or false")));
                    }
                }else{
                    dp.setEntityField(true);
                }
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
            }

        }else{
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
        }

        if (parameterVO.getDataType() != ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
            dp.setEntityField(null);
            dp.setValidateOnAll(null);
        }

        if (dp.getTargetObjectGraph().getId() != null) {
            Parameter param = ruleService.getParameter(dp.getTargetObjectGraph().getId());
            if (param instanceof ObjectGraphParameter) {
                List<DerivedParamFilterCriteria> filterCriterias = new ArrayList<DerivedParamFilterCriteria>();
                DerivedParamFilterCriteria derivedParamFilterCriteria = null;
                String objectGraph = ((ObjectGraphParameter) param).getObjectGraph();
                if (objectGraph.contains(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)) {
                    int index = objectGraph.indexOf(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE);
                    while (index >= 0) {
                        if (index > 0) {
                            derivedParamFilterCriteria = new DerivedParamFilterCriteria();
                            derivedParamFilterCriteria.setCollectionName(objectGraph.substring(0, index
                                    + RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE.length()));
                            derivedParamFilterCriteria.setOrderSequence(filterCriterias.size() + 1);
                            filterCriterias.add(derivedParamFilterCriteria);

                        }
                        index = objectGraph.indexOf(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE, index
                                + RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE.length());
                    }
                    dp.setFilterCriterias(filterCriterias);
                }
            }
        }
        List<DerivedParamFilterCriteria> derivedParamFilterCriteriaList=new ArrayList<>();

        for(QueryDerivedParameterVO queryDerivedParameterVO:parameterVO.getQueryDerivedParameterVO()){

            Map<String, Object> resultMap = convertMvelScriptNameExpressionToIdExpression(queryDerivedParameterVO.getWhereExpression());

            List<String> invalidParameters = (List<String>) resultMap.get("invalidParameters");
            String parameteIdExp = null;
            if (!invalidParameters.isEmpty()) {
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid Parameter Expression", Message.MessageType.ERROR, "Please correct the where expression.")));
            } else {
                parameteIdExp = (String) resultMap.get("parameterExp");
            }
            for(DerivedParamFilterCriteria filterCriteria:dp.getFilterCriterias()){
                if(filterCriteria.getOrderSequence()==queryDerivedParameterVO.getOrderSequence()){
                    filterCriteria.setWhereExpression(parameteIdExp);
                    filterCriteria.setWhereExpressionInName(queryDerivedParameterVO.getWhereExpression());
                }
                derivedParamFilterCriteriaList.add(filterCriteria);
            }
            dp.setFilterCriterias(derivedParamFilterCriteriaList);
        }

        dp.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT);
        dp.setScriptCode(compiledExpressionBuilder.buildExpressionOfDerivedParameter(dp));


    }

    private void convertParameterVoToSQLParameter(ParameterVO parameterVO, List<ValidationRuleResult> dataValidationRuleResults, SQLParameter parameter){

        setCommonInfoForParameters(parameterVO,dataValidationRuleResults,parameter);
        //Data Type
        if(parameterVO.getDataType()!=null){
            if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER
                    || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE
                    || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                parameter.setDataType(parameterVO.getDataType());
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
            }
        }else{
            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
        }

//SQL Rule
        if (parameterVO.getSqlQuery()!=null) {
            String result = sqlRuleExecutor.validateSQLQuery(parameterVO.getSqlQuery());
            if(result.isEmpty()){
                parameter.setSqlQueryPlain(parameterVO.getSqlQuery());
                parameterService.encryptSQLParam(parameter);
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Please correct the query.")));
            }

            String sql = parameter.getSqlQueryPlain();

            List<SQLParameterMapping> sqlParameterMapping=new ArrayList<>();

            if(sql != null && !sql.isEmpty()){
                String[] whereClauses =StringUtils.substringsBetween(sql ,RuleConstants.LEFT_CURLY_BRACES,RuleConstants.RIGHT_CURLY_BRACES);
                if(whereClauses == null || whereClauses.length == 0){
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "SQL Query Without user input Where Clause Not Allowed")));
                }else{
                    Set<String> uniqueWhere = new HashSet<>();
                    for (int i = 0; i < whereClauses.length; i++) {
                        for(QueryDerivedParameterVO queryDerivedParameterVO:parameterVO.getQueryDerivedParameterVO()){
                            String whereClauseKey = whereClauses[i];
                            if(queryDerivedParameterVO.getSeq()==i){
                                if(!uniqueWhere.add(whereClauseKey)){
                                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate placeholder", Message.MessageType.ERROR,whereClauseKey)));
                                }
                                SQLParameterMapping paramMapping = new SQLParameterMapping();
                                paramMapping.setPlaceHolderName(RuleConstants.LEFT_CURLY_BRACES+whereClauseKey+RuleConstants.RIGHT_CURLY_BRACES);
                                paramMapping.setSeq(i);
                                if(queryDerivedParameterVO.getParameter()!=null){
                                    Map<String, Object> variableMap = new HashMap<String, Object>();
                                    variableMap.put("code", queryDerivedParameterVO.getParameter().getCode());
                                    variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                                    Parameter param=baseMasterService.findMasterByCode(Parameter.class,variableMap);
                                    if(param!=null){
                                        paramMapping.setParameter(param);
                                    }else{
                                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid Parameter Code", Message.MessageType.ERROR, "Please correct the Parameter Code")));
                                    }
                                }
                                sqlParameterMapping.add(paramMapping);
                            }
                        }
                    }
                    parameter.setParamMapping(sqlParameterMapping);
                }
            }else{
                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Blank SQL Query.")));
            }
        }
    }
    private void setCommonInfoForParameters(ParameterVO parameterVO, List<ValidationRuleResult> validationRuleResults,Parameter parameter){
        parameter.setParamType(parameterVO.getParamType());
//Parameter Code
        if (parameterVO.getCode() != null) {
            if(checkForDuplicateCode(parameterVO.getCode())){
                validationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter code already exists", Message.MessageType.ERROR,parameterVO.getCode())));
            }else {
                if(validateParameterCode(parameterVO.getCode())){
                    parameter.setCode(parameterVO.getCode());
                }else{
                    validationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter code is invalid.", Message.MessageType.ERROR, "No space is allowed.")));
                }
            }
        } else {
            validationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter code is mandatory.", Message.MessageType.ERROR, "Parameter code is mandatory.")));
        }
//Parameter Name
        if (parameterVO.getName() != null) {
            if(validateParameterName(parameterVO.getName())){
                parameter.setName(parameterVO.getName());
            }else{
                validationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter name is invalid.", Message.MessageType.ERROR, "No special character and space is allowed.")));
            }
        } else {
            validationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter name is mandatory.", Message.MessageType.ERROR, "Parameter name is mandatory.")));
        }
//Parameter Description
        if (parameterVO.getDescription() != null) {
            parameter.setDescription(parameterVO.getDescription());
        } else {
            validationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter description is mandatory.", Message.MessageType.ERROR, "Parameter description is mandatory.")));
        }
//Module name
        if (parameterVO.getModuleName() != null) {
            ModuleName moduleName = genericParameterService.findByCode(parameterVO.getModuleName().getCode(), ModuleName.class);
            if (moduleName != null) {
                parameter.setModuleName(moduleName);
            } else {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Module Name is Invalid", Message.MessageType.ERROR, "Please mention a valid Module Name"));
                validationRuleResults.add(validationRuleResult);
            }
        } else {
            parameter.setModuleName(null);
        }
//Source Product
        if(parameterVO.getSourceProduct()!=null){
            SourceProduct sourceProduct=genericParameterService.findByCode(parameterVO.getSourceProduct(),SourceProduct.class);
            if(sourceProduct!=null){
                parameter.setSourceProduct(parameterVO.getSourceProduct());
            }else{
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                validationRuleResults.add(validationRuleResult);
            }
        }else{
            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
            validationRuleResults.add(validationRuleResult);
        }

    }
    private List<Integer> getStatusList(){
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(PersistenceStatus.ACTIVE);
        return statusList;
    }

    public ObjectGraphTypes findObjectGraphObject(String displayName){
        NamedQueryExecutor<ObjectGraphTypes> executor = new NamedQueryExecutor<ObjectGraphTypes>("ObjectGraphTypesMaster.findObjectGraphByDisplayName")
                .addParameter("displayName", displayName)
                .addParameter("approvalStatus", Arrays.asList(1,2,3,5,10));
        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(executor);
        if(CollectionUtils.isNotEmpty(objectGraphTypesList)){
            return objectGraphTypesList.get(0);
        }
        return null;
    }

    private Map<String, Object> convertNameExpressionToIdExpression(String parameterExp) {
        parameterExp = parameterExp.trim().replaceAll("\\s+", " ").trim();
        String[] tokens = parameterExp.split(" ");
        Map<String, Object> resultMap = new HashMap<String, Object>();

        List<String> tokenList = new ArrayList<String>();
        List<String> invalidParameters = new ArrayList<String>();
        for (String token : tokens) {
            if (token != null && token.length() > 0) {
                tokenList.add(token);
            }
        }

        tokens = tokenList.toArray(new String[tokenList.size()]);
        parameterExp = " " + parameterExp + " ";
        Long paramId = null;
        for (String token : tokens) {
            if (!RuleConstants.compoundOperators.contains(token)) {
                paramId = ruleService.getParameterIdByName(token);
                if (paramId != null) {
                    parameterExp = parameterExp.replace(" " + token + " ", " " + paramId.toString() + " ");
                } else {
                    invalidParameters.add(HtmlUtils.htmlEscape(token));
                }

            }

        }
        parameterExp = parameterExp.trim();

        resultMap.put("invalidParameters", invalidParameters);
        resultMap.put("parameterExp", parameterExp);
        return resultMap;

    }

    private void approveConstantParams(String parameterExp, User user) {
        for (String tokenID : parameterExp.split(" ")) {
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


    private void performMentionedOperation(ParameterVO parameterVO,List<ValidationRuleResult> dataValidationRuleResults) {
        if (parameterVO.getUploadOperationType().equalsIgnoreCase("Delete")) {
            Parameter deletedrecordDetails = findRecord(parameterVO.getCode());
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
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Either Already Deleted or Already marked for Deletion.", Message.MessageType.ERROR, "Check the Parameter Code"));
                    dataValidationRuleResults.add(validationRuleResult);
                }
            } else {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Record Does Not Exists.", Message.MessageType.ERROR, "Check the Parameter Code"));
                dataValidationRuleResults.add(validationRuleResult);
            }

            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Parameter Upload", "Error in Parameter Upload").setMessages(validationMessages).build();
            }
        }
//Edit
        else if (parameterVO.getUploadOperationType().equalsIgnoreCase("Edit")) {

            Parameter recordToUpdate = findRecord(parameterVO.getCode());
            if (recordToUpdate != null) {
//Parameter Code
                if (parameterVO.getCode() != null) {
                    if(validateParameterCode(parameterVO.getCode())){
                        recordToUpdate.setCode(parameterVO.getCode());
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter code is invalid.", Message.MessageType.ERROR, "No space is allowed.")));
                    }
                } else {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter code is mandatory.", Message.MessageType.ERROR, "Parameter code is mandatory.")));
                }
//Parameter Name
                if (parameterVO.getName() != null) {
                    if(validateParameterName(parameterVO.getName())){
                        recordToUpdate.setName(parameterVO.getName());
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter name is invalid.", Message.MessageType.ERROR, "No special character and space is allowed.")));
                    }

                } else {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter name is mandatory.", Message.MessageType.ERROR, "Parameter name is mandatory.")));
                }
//Parameter Description
                if (parameterVO.getDescription() != null) {
                    recordToUpdate.setDescription(parameterVO.getDescription());
                } else {
                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Parameter description is mandatory.", Message.MessageType.ERROR, "Parameter description is mandatory.")));
                }
//Module name
                if (parameterVO.getModuleName() != null) {
                    ModuleName moduleName = genericParameterService.findByCode(parameterVO.getModuleName().getCode(), ModuleName.class);
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
                if(parameterVO.getSourceProduct()!=null){
                    SourceProduct sourceProduct=genericParameterService.findByCode(parameterVO.getSourceProduct(),SourceProduct.class);
                    if(sourceProduct!=null){
                        recordToUpdate.setSourceProduct(parameterVO.getSourceProduct());
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product is Invalid", Message.MessageType.ERROR, "Please mention a valid Source Product "));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
                }else{
                    ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Source Product cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                    dataValidationRuleResults.add(validationRuleResult);
                }

                if(recordToUpdate instanceof ConstantParameter){

                    ConstantParameter cp = (ConstantParameter) recordToUpdate;
                    cp.setParamType(parameterVO.getParamType());
                    //Data Type
                    if(parameterVO.getDataType()!=null){
                        recordToUpdate.setDataType(parameterVO.getDataType());
                        if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                            if (!CASValidationUtils.isAlphaNumericAndUnderScore(parameterVO.getLiteral())){
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only Alphanumeric value is allowed", Message.MessageType.ERROR,parameterVO.getLiteral())));
                            }else{
                                cp.setLiteral(parameterVO.getLiteral());
                            }
                        } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                            if(StringUtils.isNumeric(parameterVO.getLiteral())){
                                cp.setLiteral(parameterVO.getLiteral());
                            }else{
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid number", Message.MessageType.ERROR,parameterVO.getLiteral())));
                            }
                        } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                            if(CASValidationUtils.isValidBoolean(parameterVO.getLiteral())){
                                cp.setLiteral(parameterVO.getLiteral());
                            }else{
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter true or false", Message.MessageType.ERROR,parameterVO.getLiteral())));
                            }

                        } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                            if (CASValidationUtils.isValidDate(parameterVO.getLiteral())) {
                                cp.setLiteral(parameterVO.getLiteral());
                            }else{
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid date", Message.MessageType.ERROR,parameterVO.getLiteral())));
                            }
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
                        }
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
                    }


                }else if(recordToUpdate instanceof ObjectGraphParameter){

                    ObjectGraphParameter og = (ObjectGraphParameter) recordToUpdate;
                    og.setParamType(parameterVO.getParamType());
                    List<ObjectGraphTypes> objectGraphTypesList = null;
                    if(og.getSourceProduct()!=null && og.getModuleName()!=null){
                        objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(og.getSourceProduct(), og.getModuleName().getId());
                    } else {
                        objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(og.getSourceProduct());
                    }

 //Object Graph
                    if(parameterVO.getObjectGraph()!=null){
                        ObjectGraphTypes objectGraphObject = findObjectGraphObject(parameterVO.getObjectGraph());
                        boolean found=false;
                        if(objectGraphObject!=null) {
                            if (objectGraphTypesList != null) {
                                for (ObjectGraphTypes ob : objectGraphTypesList) {
                                    if (parameterVO.getObjectGraph().equals(ob.getDisplayName())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (found == false) {
                                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Given Object Graph not valid", Message.MessageType.ERROR, "Please mention object graph according to source system and module name selected")));
                                }
                            }
                            String objectGraph = objectGraphObject.getObjectGraph();
                            ParameterDataType dataType = objectGraphObject.getDataType();
                            if (objectGraph != null && dataType != null) {
                                og.setObjectGraph(objectGraph);
                                og.setDataType(Integer.parseInt(dataType.getCode()));
                            }
                        } else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Not a valid Object Graph", Message.MessageType.ERROR, "Please mention a valid object graph name")));
                        }

                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Object Graph cannot be Left Blank", Message.MessageType.ERROR, "Object Graph is mandatory")));
                    }

                    if (og.getObjectGraph().contains(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)) {
                        og.setCollectionBased(true);
                    }


                }else if(recordToUpdate instanceof ReferenceParameter){

                    ReferenceParameter rp = (ReferenceParameter) recordToUpdate;
                    rp.setParamType(parameterVO.getParamType());
//Data Type
                    rp.setDataType(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE);
//Entity Type and Reference URI
                    if(parameterVO.getEntityType().getDisplayEntityName()!=null){

                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        variableMap.put("displayEntityName", parameterVO.getEntityType().getDisplayEntityName());
                        variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                        EntityType entityType= baseMasterService.findMasterByCode(EntityType.class, variableMap);

                        if(entityType!=null && entityType.getFields()!=null){
                            Entity entity = null;
                            String[]fields=entityType.getFields().split(",");
                            Map<String,Object> map=new HashMap<>();
                            for (String field:fields){
                                if(StringUtils.isNotEmpty(parameterVO.getReference())){
                                    map.put(field,parameterVO.getReference());
                                }else{
                                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Reference Value is mandatory.", Message.MessageType.ERROR, "Please fill a reference value for corresponding Entity Type")));
                                }
                            }
                            try {
                                Class className = Class.forName(entityType.getClassName());
                                entity = baseMasterService.findMasterByCode(className, map, false);
                            } catch (ClassNotFoundException e) {
                                BaseLoggers.flowLogger.error("Exception occurred in finding entity class {}", e);
                            }
                            if(entity != null){
                                String uri=entity.getUri();
                                rp.setReferenceEntityId(EntityId.fromUri(uri));
                            }
                        }
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Entity Type is mandatory.", Message.MessageType.ERROR, "Please fill an Entity Type")));
                    }



                }else if(recordToUpdate instanceof CompoundParameter){
                    CompoundParameter cop = (CompoundParameter) recordToUpdate;
                    cop.setParamType(parameterVO.getParamType());
//Data Type
                    if(parameterVO.getDataType()!=null){
                        if(parameterVO.getDataType()==ParameterDataType.PARAMETER_DATA_TYPE_STRING || parameterVO.getDataType()==ParameterDataType.PARAMETER_DATA_TYPE_NUMBER){
                            cop.setDataType(parameterVO.getDataType());
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is invalid", Message.MessageType.ERROR, "Only Alphanumeric and Number allowed")));
                        }
                    }else{
                        ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Data Type cannot be Left Blank", Message.MessageType.ERROR,"It is a Mandatory Field."));
                        dataValidationRuleResults.add(validationRuleResult);
                    }
//Parameter Expression
                    if(parameterVO.getParameterExpression()!=null){
                        String parameterExp=null;
                        Map<String, Object> resultMap = convertNameExpressionToIdExpression(parameterVO.getParameterExpression());

                        List<String> invalidParameters = (List<String>) resultMap.get("invalidParameters");
                        if (!invalidParameters.isEmpty()) {
                            ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Parameter Name is Invalid", Message.MessageType.ERROR, "Please correct the Parameter Name"));
                            dataValidationRuleResults.add(validationRuleResult);

                        } else {
                            parameterExp = (String) resultMap.get("parameterExp");
                            List<ValidationError> validationErrorsList = new ArrayList<ValidationError>();
                            validationErrorsList = expressionValidationService.validateCompoundParameterExpression(parameterExp,parameterVO.getDataType());

                            if (validationErrorsList.isEmpty()) {
                                cop.setParameterExpression(parameterExp);
                                User user = getCurrentUser().getUserReference();
                                approveConstantParams(parameterExp, user);
                            } else {
                                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Parameter Expression is Invalid", Message.MessageType.ERROR, "Please correct the Expression"));
                                dataValidationRuleResults.add(validationRuleResult);
                            }
                        }
                    }




                }else if(recordToUpdate instanceof PlaceHolderParameter){

                    PlaceHolderParameter pp = (PlaceHolderParameter) recordToUpdate;
                    pp.setParamType(parameterVO.getParamType());

                    //Context Name
                    if(parameterVO.getContextName()!=null){
                        if (!CASValidationUtils.isAlphaNumericAndUnderScore(parameterVO.getContextName())) {
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Only alphanumeric and underscore is allowed in Context Name", Message.MessageType.ERROR,parameterVO.getContextName())));
                        }else{
                            pp.setContextName(parameterVO.getContextName());
                        }
                        if(parameterVO.getObjectGraph()!=null){
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Either select context name or object graph", Message.MessageType.ERROR,"Specify one field")));
                        }

                    }
//Object Graph
                    else if(parameterVO.getObjectGraph()!=null){
                        if(parameterVO.getContextName()!=null){
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Either select context name or object graph", Message.MessageType.ERROR,"Specify one field")));
                        }
                        List<ObjectGraphTypes> objectGraphTypesList = null;
                        if(pp.getSourceProduct()!=null && pp.getModuleName()!=null){
                            objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(pp.getSourceProduct(), pp.getModuleName().getId());
                        } else {
                            objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(pp.getSourceProduct());
                        }
                        ObjectGraphTypes objectGraphObject = findObjectGraphObject(parameterVO.getObjectGraph());
                        boolean found=false;
                        if(objectGraphObject!=null) {
                            if (objectGraphTypesList != null) {
                                for (ObjectGraphTypes ob : objectGraphTypesList) {
                                    if (parameterVO.getObjectGraph().equals(ob.getDisplayName())) {
                                        found = true;
                                        break;
                                    }
                                }
                                if (found == false) {
                                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Given Object Graph not valid", Message.MessageType.ERROR, "Please mention object graph according to source system and module name selected")));
                                }
                            }
                            String objectGraph = objectGraphObject.getObjectGraph();
                            ParameterDataType dataType = objectGraphObject.getDataType();
                            if (objectGraph != null && dataType != null) {
                                pp.setObjectGraph(objectGraph);
                                pp.setDataType(Integer.parseInt(dataType.getCode()));
                            }
                        } else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Not a valid Object Graph", Message.MessageType.ERROR, "Please mention a valid object graph name")));
                        }
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Either fill context name or object graph", Message.MessageType.ERROR,"Specify atleast one field")));
                    }




                }else if(recordToUpdate instanceof DerivedParameter){
                    DerivedParameter dp = (DerivedParameter) recordToUpdate;
                    dp.setParamType(ParameterType.PARAMETER_TYPE_DERIVED);

                    //Data Type
                    if(parameterVO.getDataType()!=null){
                        List<ObjectGraphParameter> objectGraphParameterListCollection =ruleService.getCollectionTypGraphByDataType(Integer.valueOf(
                                ParameterDataType.PARAMETER_DATA_TYPE_COLLECTION).toString());
                        List<ObjectGraphParameter> objectGraphParameterList1 =ruleService.getCollectionTypGraphByDataType(parameterVO.getDataType().toString());
                        Map<String, String> aggregateFunctionMap=RuleConstants.MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION;
                        String objectGraph=null;
                        boolean found=false;
                        boolean aggregateFound=false;
                        if(parameterVO.getTargetObjectGraph()!=null){
                            Map<String, Object> variableMap = new HashMap<String, Object>();
                            variableMap.put("name", parameterVO.getTargetObjectGraph());
                            Parameter parameter= baseMasterService.findMasterByCode(Parameter.class, variableMap);
                            if(parameter!=null && parameter instanceof ObjectGraphParameter){
                                objectGraph =((ObjectGraphParameter)parameter).getObjectGraph();
                            }
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Return Value is mandatory", Message.MessageType.ERROR,"Please fill target object graph parameter")));
                        }
                        if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING ||
                                parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE ||
                                parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE
                                ||parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                            dp.setDataType(parameterVO.getDataType());
                            for(ObjectGraphParameter o:objectGraphParameterList1){
                                if(o.getObjectGraph().equalsIgnoreCase(objectGraph)){
                                    found=true;
                                    dp.setTargetObjectGraph(o);
                                    break;
                                }
                            }
                            if(found==false) {
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Return Value is not according to Data Type mentioned", Message.MessageType.ERROR, "Please fill valid Target object graph")));
                            }
                            if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                                if(parameterVO.getAggregateFunction()!=null){
                                    for(Map.Entry<String,String> entry:aggregateFunctionMap.entrySet()){
                                        if(entry.getValue().equalsIgnoreCase(parameterVO.getAggregateFunction())){
                                            aggregateFound=true;
                                            dp.setAggregateFunction(entry.getKey());
                                            break;
                                        }
                                    }
                                    if(aggregateFound==false){
                                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Aggregate Function value is not valid", Message.MessageType.ERROR,"It can only be Sum, Average, Max or Min")));
                                    }
                                }
                            }

                        }else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                            dp.setDataType(parameterVO.getDataType());
                            if(parameterVO.getEntityField()==null){
                                parameterVO.setEntityField(true);
                            }
                            List<ObjectGraphParameter> finalList=new ArrayList<>();
                            if(parameterVO.getEntityField()!=null && parameterVO.getEntityField().equals(false)){
                                finalList = objectGraphParameterListCollection;
                            }else if(parameterVO.getEntityField()!=null && parameterVO.getEntityField().equals(true)){
                                finalList = objectGraphParameterList1;
                            }
                            for(ObjectGraphParameter o:finalList){
                                if(o.getObjectGraph().equalsIgnoreCase(objectGraph)){
                                    found=true;
                                    dp.setTargetObjectGraph(o);
                                    break;
                                }
                            }
                            if(found==false){
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Return Value is not according to Data Type mentioned", Message.MessageType.ERROR,"Please fill valid Target object graph")));
                            }
                            if(parameterVO.getEntityField()!=null){
                                if(parameterVO.getEntityField().equals(true)){
                                    dp.setEntityField(parameterVO.getEntityField());
                                    dp.setValidateOnAll(false);
                                }else if(parameterVO.getEntityField().equals(false)){
                                    dp.setEntityField(parameterVO.getEntityField());
                                    if(parameterVO.getValidateOnAll()!=null){
                                        dp.setValidateOnAll(parameterVO.getValidateOnAll());
                                    }else{
                                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Validate On all value is required in case of Entity Field false", Message.MessageType.ERROR,"Please fill true or false")));
                                    }
                                }else{
                                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid value of Entity Field", Message.MessageType.ERROR,"Please fill true or false")));
                                }
                            }else{
                                dp.setEntityField(true);
                            }
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
                        }

                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
                    }

                    if (parameterVO.getDataType() != ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                        dp.setEntityField(null);
                        dp.setValidateOnAll(null);
                    }
                    List<DerivedParamFilterCriteria> filterCriterias=null;
                    if (dp.getTargetObjectGraph().getId() != null) {
                        Parameter param = ruleService.getParameter(dp.getTargetObjectGraph().getId());
                        if (param instanceof ObjectGraphParameter) {
                            filterCriterias = new ArrayList<DerivedParamFilterCriteria>();
                            DerivedParamFilterCriteria derivedParamFilterCriteria = null;
                            String objectGraph = ((ObjectGraphParameter) param).getObjectGraph();
                            if (objectGraph.contains(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)) {
                                int index = objectGraph.indexOf(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE);
                                while (index >= 0) {
                                    if (index > 0) {
                                        derivedParamFilterCriteria = new DerivedParamFilterCriteria();
                                        derivedParamFilterCriteria.setCollectionName(objectGraph.substring(0, index
                                                + RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE.length()));
                                        derivedParamFilterCriteria.setOrderSequence(filterCriterias.size() + 1);
                                        filterCriterias.add(derivedParamFilterCriteria);

                                    }
                                    index = objectGraph.indexOf(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE, index
                                            + RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE.length());
                                }
                            }
                        }
                    }
                    List<DerivedParamFilterCriteria> derivedParamFilterCriteriaList=new ArrayList<>();

                    for(QueryDerivedParameterVO queryDerivedParameterVO:parameterVO.getQueryDerivedParameterVO()){

                        Map<String, Object> resultMap = convertMvelScriptNameExpressionToIdExpression(queryDerivedParameterVO.getWhereExpression());

                        List<String> invalidParameters = (List<String>) resultMap.get("invalidParameters");
                        String parameteIdExp = null;
                        if (!invalidParameters.isEmpty()) {
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid Parameter Expression", Message.MessageType.ERROR, "Please correct the where expression.")));
                        } else {
                            parameteIdExp = (String) resultMap.get("parameterExp");
                        }
                        if(CollectionUtils.isNotEmpty(filterCriterias)){
                            for(DerivedParamFilterCriteria filterCriteria:filterCriterias){
                                if(filterCriteria.getOrderSequence()==queryDerivedParameterVO.getOrderSequence()){
                                    filterCriteria.setWhereExpression(parameteIdExp);
                                    filterCriteria.setWhereExpressionInName(queryDerivedParameterVO.getWhereExpression());
                                }
                                derivedParamFilterCriteriaList.add(filterCriteria);
                            }
                            if(CollectionUtils.isNotEmpty(dp.getFilterCriterias())){
                                dp.getFilterCriterias().clear();
                                dp.getFilterCriterias().addAll(derivedParamFilterCriteriaList);
                            }
                        }

                    }

                    dp.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT);
                    dp.setScriptCode(compiledExpressionBuilder.buildExpressionOfDerivedParameter(dp));


                }else if (recordToUpdate instanceof ScriptParameter){

                    ScriptParameter sp = (ScriptParameter) recordToUpdate;
                    sp.setParamType(parameterVO.getParamType());
//Data Type
                    if(parameterVO.getDataType()!=null){
                        if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING) {
                            sp.setDataType(parameterVO.getDataType());
                        } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER) {
                            sp.setDataType(parameterVO.getDataType());
                        } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN) {
                            sp.setDataType(parameterVO.getDataType());
                        } else if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                            sp.setDataType(parameterVO.getDataType());
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
                        }
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
                    }
//Script Code Type
                    sp.setScriptCodeType(RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT);
//Script Code
                    if (parameterVO.getScriptCode()!=null) {
                        sp.setScriptCodeValue(parameterVO.getScriptCode());
                        ruleService.encryptScriptCode(sp);
                    }
                    if(parameterVO.getEntityField()!=null || parameterVO.getValidateOnAll()!=null || parameterVO.getAggregateFunction()!=null ||
                            parameterVO.getCompiledExpression()!=null || parameterVO.getContextName()!=null ||
                            parameterVO.getEntityType()!=null || parameterVO.getQuery()!=null || parameterVO.getReference()!=null ||
                            parameterVO.getLiteral()!=null){

                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid fields filled for Parameter Type Computed", Message.MessageType.ERROR,"Please specify only computed related fields.")));

                    }

                }else if(recordToUpdate instanceof QueryParameter){

                    QueryParameter qp = (QueryParameter) recordToUpdate;
                    qp.setParamType(ParameterType.PARAMETER_TYPE_QUERY);
//Data Type
                    if(parameterVO.getDataType()!=null){
                        if (parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_STRING || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_NUMBER
                                || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE
                                || parameterVO.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                            qp.setDataType(parameterVO.getDataType());
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Please enter a valid data type code", Message.MessageType.ERROR,parameterVO.getDataType().toString())));
                        }
                    }else{
                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Data Type is mandatory.", Message.MessageType.ERROR, "Data Type is mandatory.")));
                    }
// Query and Query Parameter Attributes
                    if(parameterVO.getQuery()!=null){
                        if(queryContainsText(parameterVO.getQuery(), INSERT, UPDATE, DELETE)){
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Query is invalid.", Message.MessageType.ERROR, "Enquiries about insertion, update, and deletion are not allowed.")));
                        }else{
                            qp.setQuery(parameterVO.getQuery());

                            String query = parameterVO.getQuery();

                            if(query.indexOf(":")!=-1){
                                List<QueryParameterAttribute> queryParameterAttributeList = new ArrayList<>();
                                for(QueryDerivedParameterVO q:parameterVO.getQueryDerivedParameterVO()){
                                    while (query.indexOf(":") != -1) {
                                        query = query.substring(query.indexOf(":") + 1);
                                        String[] abc = query.split(" ");

                                        if(abc[0].equalsIgnoreCase(q.getQueryParameterName())){
                                            QueryParameterAttribute queryParameterAttribute=new QueryParameterAttribute();
                                            queryParameterAttribute.setQueryParameterName(q.getQueryParameterName());

                                            List<ObjectGraphTypes> objectGraphTypesList = null;
                                            if(qp.getSourceProduct()!=null && qp.getModuleName()!=null){
                                                objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(qp.getSourceProduct(), qp.getModuleName().getId());
                                            } else {
                                                objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(qp.getSourceProduct());
                                            }
                                            ObjectGraphTypes objectGraphObject = findObjectGraphObject(q.getQueryObjectGraph());
                                            boolean found=false;
                                            if(objectGraphObject!=null) {
                                                if (objectGraphTypesList != null) {
                                                    for (ObjectGraphTypes ob : objectGraphTypesList) {
                                                        if (q.getQueryObjectGraph().equals(ob.getDisplayName())) {
                                                            found = true;
                                                            break;
                                                        }
                                                    }
                                                    if (found == false) {
                                                        dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Given Object Graph not valid", Message.MessageType.ERROR, "Please mention object graph according to source system and module name selected")));
                                                    }
                                                }
                                                String objectGraph = objectGraphObject.getObjectGraph();
                                                if (objectGraph != null) {
                                                    queryParameterAttribute.setObjectGraph(objectGraph);
                                                }
                                            } else{
                                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Not a valid Object Graph", Message.MessageType.ERROR, "Please mention a valid object graph name")));
                                            }
                                            queryParameterAttributeList.add(queryParameterAttribute);
                                        }
                                    }
                                }
                                if(CollectionUtils.isNotEmpty(qp.getQueryParameterAttributes())){
                                    qp.getQueryParameterAttributes().clear();
                                    qp.getQueryParameterAttributes().addAll(queryParameterAttributeList);
                                }

                            }
                        }
                    }
                }else if(recordToUpdate instanceof SQLParameter){
                    SQLParameter sqlParameter = (SQLParameter) recordToUpdate;
//SQL Rule
                    if (parameterVO.getSqlQuery()!=null) {
                        String result = sqlRuleExecutor.validateSQLQuery(parameterVO.getSqlQuery());
                        if(result.isEmpty()){
                            sqlParameter.setSqlQueryPlain(parameterVO.getSqlQuery());
                            parameterService.encryptSQLParam(sqlParameter);
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Please correct the query.")));
                        }

                        String sql = sqlParameter.getSqlQueryPlain();

                        List<SQLParameterMapping> sqlParameterMapping=new ArrayList<>();

                        if(sql != null && !sql.isEmpty()){
                            String[] whereClauses =StringUtils.substringsBetween(sql ,RuleConstants.LEFT_CURLY_BRACES,RuleConstants.RIGHT_CURLY_BRACES);
                            if(whereClauses == null || whereClauses.length == 0){
                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "SQL Query Without user input Where Clause Not Allowed")));
                            }else{
                                Set<String> uniqueWhere = new HashSet<>();
                                for (int i = 0; i < whereClauses.length; i++) {
                                    for(QueryDerivedParameterVO queryDerivedParameterVO:parameterVO.getQueryDerivedParameterVO()){
                                        String whereClauseKey = whereClauses[i];
                                        if(queryDerivedParameterVO.getSeq()==i){
                                            if(!uniqueWhere.add(whereClauseKey)){
                                                dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Duplicate placeholder", Message.MessageType.ERROR,whereClauseKey)));
                                            }
                                            SQLParameterMapping paramMapping = new SQLParameterMapping();
                                            paramMapping.setPlaceHolderName(RuleConstants.LEFT_CURLY_BRACES+whereClauseKey+RuleConstants.RIGHT_CURLY_BRACES);
                                            paramMapping.setSeq(i);
                                            if(queryDerivedParameterVO.getParameter()!=null){
                                                Map<String, Object> variableMap = new HashMap<String, Object>();
                                                variableMap.put("code", queryDerivedParameterVO.getParameter().getCode());
                                                variableMap.put("entityLifeCycleData.persistenceStatus", getStatusList());
                                                Parameter parameter=baseMasterService.findMasterByCode(Parameter.class,variableMap);
                                                if(parameter!=null){
                                                    paramMapping.setParameter(parameter);
                                                }else{
                                                    dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid Parameter Code", Message.MessageType.ERROR, "Please correct the Parameter Code")));
                                                }
                                            }
                                            sqlParameterMapping.add(paramMapping);
                                        }
                                    }
                                }
                                sqlParameter.setParamMapping(sqlParameterMapping);
                            }
                        }else{
                            dataValidationRuleResults.add(new ValidationRuleResult(CoreUtility.prepareMessage("Invalid SQL Query", Message.MessageType.ERROR, "Blank SQL Query.")));
                        }
                    }
                }


            }else
            {
                ValidationRuleResult validationRuleResult = new ValidationRuleResult(CoreUtility.prepareMessage("Either Empty or Invalid - ", Message.MessageType.ERROR,"Parameter Code"));
                dataValidationRuleResults.add(validationRuleResult);
            }
            if (!dataValidationRuleResults.isEmpty()) {
                List<Message> validationMessages = new ArrayList<Message>();
                for (ValidationRuleResult validationRuleResult1 : dataValidationRuleResults) {
                    validationMessages.add(validationRuleResult1.getI18message());
                }
                throw ExceptionBuilder.getInstance(ServiceInputException.class, "Error in Parameter Upload", "Error in Parameter Upload").setMessages(validationMessages).build();
            } else {
                User user1 = getCurrentUser().getUserReference();
                if (recordToUpdate.getId() != null && user1 != null) {
                    entityDao.detach(recordToUpdate);
                    makerCheckerService.masterEntityChangedByUser(recordToUpdate, user1);
                }
            }
        }
    }


    public Parameter findRecord(String parameterCode){
        NamedQueryExecutor<Parameter> executor = new NamedQueryExecutor<Parameter>("ParameterMaster.findParameterByParameterCode")
                .addParameter("parameterCode", parameterCode)
                .addParameter("approvalStatus", Arrays.asList(1,2,3,5,10));
        List<Parameter> parameters = entityDao.executeQuery(executor);
        if(CollectionUtils.isNotEmpty(parameters)){
            return parameters.get(0);
        }
        return null;
    }

    private boolean checkForDuplicateCode(Object parameterCode){
        boolean flag =false;
        String code = "code";
        flag = baseMasterService.hasEntity(Parameter.class, code, parameterCode);
        return flag;
    }

    private boolean queryContainsText(String query, String... allowedLiterals) {
        boolean hasParameterQuery = false;
        if (query != null) {
            String[] queryTokens = StringUtils.split(StringUtils.lowerCase(query));
            List<String> tokens = Arrays.asList(queryTokens);
            for (String allowedLiteral : allowedLiterals) {
                String matchingText = allowedLiteral.toLowerCase().trim();
                hasParameterQuery = tokens.contains(matchingText);
                if (hasParameterQuery) {
                    break;
                }
            }
        }
        return hasParameterQuery;
    }

    protected Map<String, Object> convertMvelScriptNameExpressionToIdExpression(String parameterExp) {
        parameterExp = parameterExp.trim().replaceAll("\\s+", " ").trim();
        String[] tokens = parameterExp.split(" ");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<String> invalidParameters = new ArrayList<String>();

        parameterExp = " " + parameterExp + " ";
        Long paramId = null;
        for (String token : tokens) {
            if (!RuleConstants.conditionOperatorsForMVELScript.contains(token)
                    && !Arrays.asList(ExpressionValidationConstants.SUPPORTED_CONDITION_JOIN_OPERATORS_MVEL_SCRIPT)
                    .contains(token) && !RuleConstants.LEFT_PAREN.equals(token)
                    && !RuleConstants.RIGHT_PAREN.equals(token)) {
                paramId = ruleService.getParameterIdByName(token);
                if (paramId != null) {
                    parameterExp = parameterExp.replace(" " + token + " ", " " + paramId.toString() + " ");
                } else {
                    invalidParameters.add(HtmlUtils.htmlEscape(token));
                }
            }
        }
        parameterExp = parameterExp.trim();

        resultMap.put("invalidParameters", invalidParameters);
        resultMap.put("parameterExp", parameterExp);
        return resultMap;

    }


        private boolean validateParameterName(String name){
            if(StringUtils.containsWhitespace(name)){
                return false;
            }
            for (String input : PARAMETER_NAME_VALIDATOR) {
                if (name.contains(input)) {
                    return false;
                }
            }
            return true;
        }

    private boolean validateParameterCode(String code){
        if(StringUtils.containsWhitespace(code)){
            return false;
        }
        return true;
    }

}
