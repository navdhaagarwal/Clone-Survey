package com.nucleus.rules.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.address.District;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.formsConfiguration.validationcomponent.Tuple_2;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.entity.SystemEntity;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvel2.MVEL;
import org.springframework.context.MessageSource;

import com.nucleus.core.common.EntityUtil;
import com.nucleus.core.event.EventDefinition;
import com.nucleus.core.event.EventTask;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.core.vo.RuleAutoCompleteSearchVO;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.dao.query.RuleQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.BaseMasterDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.rules.eventdefinition.service.EventDefinitionService;
import com.nucleus.rules.model.eventDefinition.RuleValidationTask;
import com.nucleus.rules.taskAssignmentMaster.ObjectGraphClassMapping;

import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * @author Nucleus Software Exports Limited Implementation class for rule
 *         service
 */

@Named(value = "ruleService")
public class RuleServiceImpl extends BaseRuleServiceImpl implements RuleService {

    public static final  int DEFAULT_PAGE_SIZE = 3;
    public static final String APPROVAL_STATUS = "approvalStatus";
    public static final String SOURCE_ID= "source";
    public static final String MODULE_ID="moduleName";
    
    @Inject
    @Named("baseMasterDao")
    private BaseMasterDao             baseMasterDao;

    @Inject
    @Named("messageSource")
    protected MessageSource           messageSource;

    @Inject
    @Named("compiledExpressionBuilder")
    private CompiledExpressionBuilder compiledExpressionBuilder;

    @Inject
    @Named(value = "ruleInvocationService")
    private RuleInvocationService     ruleInvocationService;

    @Inject
    @Named(value = "eventDefinitionService")
    private EventDefinitionService    eventDefinitionService;
	
	@Inject
	@Named("parameterService")
	private ParameterService             parameterService;

    @Inject
    @Named("sQLRuleExecutor")
    private SQLRuleExecutor sqlExecutor;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Lazy
    @Inject
    @Named("ruleExceptionLoggingServiceImpl")
    private RuleExceptionLoggingService ruleExceptionLoggingService;


    @Override
    public void saveRule(Rule rule) {
        NeutrinoValidator.notNull(rule, "Rule cannot be null");
        entityDao.persist(rule);
    }

    @Override
    public void updateRule(Rule rule) {
        NeutrinoValidator.notNull(rule, "Rule cannot be null");
        entityDao.update(rule);
    }

    @Override
    public void deleteRule(Rule rule) {
        // TODO: delete rule
    }


    @Override
    public void saveCondition(Condition condition) {
        NeutrinoValidator.notNull(condition, "Condition cannot be null");
        entityDao.persist(condition);
    }

    @Override
    public void updateCondition(Condition condition) {
        NeutrinoValidator.notNull(condition, "Condition cannot be null");
        entityDao.update(condition);
    }

    @Override
    public void deleteCondition(Condition condition) {
    }

    @Override
    public void saveParameter(Parameter parameter) {
        NeutrinoValidator.notNull(parameter, "Parameter cannot be null");
        entityDao.persist(parameter);
    }

    @Override
    public void updateParameter(Parameter parameter) {
        NeutrinoValidator.notNull(parameter, "Parameter cannot be null");
        entityDao.update(parameter);
    }

    @Override
    public void deleteParameter(Parameter parameter) {
    }

    @Override
    public Rule getRule(Long ruleId) {
        return entityDao.find(Rule.class, ruleId);
    }

    @Override
    public Condition getCondition(Long conditionId) {
        return entityDao.find(Condition.class, conditionId);
    }

    @Override
    public Parameter getParameter(Long parameterId) {
        return entityDao.find(Parameter.class, parameterId);
    }

    @Override
    public ParameterType getParameterTypeById(Long id) {
        return entityDao.find(ParameterType.class, id);
    }

    @Override
    public Rule getRuleByName(String name) {
        NamedQueryExecutor<Rule> ruleExecutor = new NamedQueryExecutor<Rule>("Rules.RuleByName").addParameter("name", name)
                .addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleExecutor);
        if (ruleList.size() > 0)
            return ruleList.get(0);
        else
            return null;
    }

    @Override
    public Condition getConditionByName(String name, String productId) {
        NamedQueryExecutor<Condition> conditionExecutor = new NamedQueryExecutor<Condition>("Rules.ConditionByName")
                .addParameter("name", name).addParameter("productId", productId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Condition> conditionList = entityDao.executeQuery(conditionExecutor);
        if (conditionList.size() > 0)
            return conditionList.get(0);
        else
            return null;
    }

    @Override
    public Parameter getParameterByName(String name, String productId) {
        NamedQueryExecutor<Parameter> parameterExecutor = new NamedQueryExecutor<Parameter>("Rules.ParameterByName")
                .addParameter("name", name).addParameter("productId", productId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Parameter> parameterList = entityDao.executeQuery(parameterExecutor);
        if (parameterList.size() > 0)
            return parameterList.get(0);
        else
            return null;
    }

    @Override
    public Rule getApprovedRuleByName(String name) {
        NamedQueryExecutor<Rule> ruleCriteria = new NamedQueryExecutor<Rule>("Rules.ApprovedRuleByName").addParameter(
                "name", name + "%").addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleCriteria);
        if (ruleList.size() > 0)
            return ruleList.get(0);
        else
            return null;
    }

    @Override
    public List<Rule> getAllApprovedRules() {
        NamedQueryExecutor<Rule> ruleCriteria = new NamedQueryExecutor<Rule>("Rules.AllApprovedRule").addParameter(
                "approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(ruleCriteria);
    }

    @Override
    public Condition getApprovedConditionByName(String name) {
        NamedQueryExecutor<Condition> conditionCriteria = new NamedQueryExecutor<Condition>("Rules.ApprovedConditionByName")
                .addParameter("name", name + "%").addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Condition> conditionList = entityDao.executeQuery(conditionCriteria);
        if (conditionList.size() > 0)
            return conditionList.get(0);
        else
            return null;
    }

    @Override
    public Parameter getApprovedParameterByName(String name) {
		return parameterService.getApprovedParameterByName(name);
    }

    @Override
    public List<Parameter> getApprovedNumericParameter() {
        NamedQueryExecutor<Parameter> parameterCriteria = new NamedQueryExecutor<Parameter>("Rules.ApprovedNumericParameters")
        		.addParameter("dataType", Arrays.asList(2,3))
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Parameter> parameterList = entityDao.executeQuery(parameterCriteria);

        return parameterList;
    }

    @Override
    public List<Map<String, ?>> getApprovedNumericParameterForBinder() {
        NamedQueryExecutor<Map<String, ?>> parameterCriteria = new NamedQueryExecutor<Map<String, ?>>(
                "Rules.ApprovedNumericParametersForBinder")
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
        		.addParameter("dataType", Arrays.asList(2,3));
        List<Map<String, ?>> parameterList = entityDao.executeQuery(parameterCriteria);

        return parameterList;
    }

    @Override
    public <T extends Entity> List<T> searchOnFieldValue(Class<T> entityClass, String field, String value) {
        return entityDao.searchEntityOnFieldAndValue(entityClass, field, value);

    }

    @Override
    public ObjectGraphTypes getObjectGraphTypes(Long objectGraphTypesId) {
        return entityDao.find(ObjectGraphTypes.class, objectGraphTypesId);
    }

    @Override
    public List<ObjectGraphTypes> retrieveObjectGraphTypes() {
        return entityDao.findAll(ObjectGraphTypes.class);
    }

    @Override
    public SourceProduct getSourceProductById(Long sourceProductId) {
        return entityDao.find(SourceProduct.class, sourceProductId);
    }

    @Override
    public <T extends GenericParameter> List<T> retrieveTypes(Class<T> entityClass) {
        if (entityClass == null) {
            throw new InvalidDataException("Entity class cannot be null");
        }
        return entityDao.findAll(entityClass);
    }

    @Override
    public List<ObjectGraphTypes> findObjectGraphTypesByObjectGraph(String objectGraph) {
        if (StringUtils.isBlank(objectGraph)) {
            throw new InvalidDataException("objectGraph cannot be null");
        }

        NamedQueryExecutor<ObjectGraphTypes> objectGraphTypesCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rules.ObjectGraphTypeByObjectGraph").addParameter("objectGraph", objectGraph)
                .addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(objectGraphTypesCriteria);
    }

    @Override
    public List<Condition> getApprovedConditionsbySourceProduct(String name, String productId) {
        NamedQueryExecutor<Condition> conditionCriteria = new NamedQueryExecutor<Condition>(
                "Rules.ApprovedConditions.BasedOnProductSelected").addParameter("name", "%" + name + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Condition> conditionList = entityDao.executeQuery(conditionCriteria);
        if (conditionList.size() > 0)
            return conditionList;
        else
            return null;
    }

    @Override
    public List<Condition> getConditionsSourceProductPage(String name, String productId, int startIndex, int pageSize) {
        NamedQueryExecutor<Condition> conditionCriteria = new NamedQueryExecutor<Condition>(
                "Rules.ApprovedConditions.BasedOnProductSelected").addParameter("name", "%" + name.toLowerCase() + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Condition> conditionList = entityDao.executeQuery(conditionCriteria, startIndex, pageSize);
        if (conditionList.size() > 0)
            return conditionList;
        else
            return null;
    }

    @Override
    public long getCountOfConditions(String name, String productId) {
        NamedQueryExecutor<Long> conditionCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedConditions.BasedOnProductSelected.Count").addParameter("name", "%" + name.toLowerCase() + "%")
                .addParameter("productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        long size = entityDao.executeQueryForSingleValue(conditionCriteria);
        return size;
    }

    @Override
    public List<Parameter> getApprovedParametersbySourceProduct(String name, String productId) {
        NamedQueryExecutor<Parameter> parameterCriteria = new NamedQueryExecutor<Parameter>(
                "Rules.ApprovedParameters.BasedOnProductSelected").addParameter("name", "%" + name + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Parameter> parameterList = entityDao.executeQuery(parameterCriteria);
        if (parameterList.size() > 0)
            return parameterList;
        else
            return null;
    }

    @Override
    public List<Parameter> getApprovedParametersbySourceProductPaginated(String name, String productId, int startIndex,
            int pageSize) {
        NamedQueryExecutor<Parameter> parameterCriteria = new NamedQueryExecutor<Parameter>(
                "Rules.ApprovedParameters.BasedOnProductSelected").addParameter("name", "%" + name + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Parameter> parameterList = entityDao.executeQuery(parameterCriteria, startIndex, pageSize);
        if (parameterList.size() > 0)
            return parameterList;
        else
            return null;
    }

    @Override
    public long getCountOfApprovedParametersbySourceProduct(String name, String productId) {
        NamedQueryExecutor<Long> parameterCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedParameters.BasedOnProductSelected.Count").addParameter("name", "%" + name + "%")
                .addParameter("productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        long size = entityDao.executeQueryForSingleValue(parameterCriteria);
        return size;
    }

    @Override
    public List<Parameter> getApprovedParametersbyDataTypePaginated(String name, String productId, List<Integer> dataTypes,
            int startIndex, int pageSize) {

        NamedQueryExecutor<Parameter> parameterCriteria = new NamedQueryExecutor<Parameter>(
                "Rules.ApprovedParameters.BasedOnDataTypeProductSelected").addParameter("name", "%" + name.toLowerCase() + "%")
                .addParameter("productId", productId).addParameter("dataType", dataTypes).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Parameter> parameters = entityDao.executeQuery(parameterCriteria, startIndex, pageSize);

        if (null != parameters && parameters.size() > 0) {
            return parameters;
        }

        return null;
    }

    @Override
    public long getCountOfApprovedParametersbyDataType(String name, String productId, List<Integer> dataTypes) {

        NamedQueryExecutor<Long> parameterCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedParameters.BasedOnDataTypeProductSelected.Count").addParameter("name", "%" + name.toLowerCase() + "%")
                .addParameter("productId", productId).addParameter("dataType", dataTypes).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        return entityDao.executeQueryForSingleValue(parameterCriteria);
    }

    @Override
    public List<Parameter> getApprovedParametersbyDataType(String name, String productId, int dataType) {
        NamedQueryExecutor<Parameter> parameterCriteria = new NamedQueryExecutor<Parameter>(
                "Rules.ApprovedParameters.BasedOnDataTypeProductSelected").addParameter("name", "%" + name + "%")
                .addParameter("productId", productId).addParameter("dataType", dataType)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Parameter> parameterList = entityDao.executeQuery(parameterCriteria);
        if (parameterList.size() > 0)
            return parameterList;
        else
            return null;
    }

    @Override
    public List<ObjectGraphTypes> getApprovedObjectGraphBySourceProduct(String productId) {
        NamedQueryExecutor<ObjectGraphTypes> parameterCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rules.ApprovedObjectGraph.BasedOnProductSelected").addParameter("productId", productId)
        		.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(parameterCriteria);
        if (objectGraphTypesList.size() > 0)
            return objectGraphTypesList;
        else
            return null;
    }

    @Override
    public List<ObjectGraphTypes> getOgnlBySourceProductAndModuleName(String productId, String moduleName) {
        NamedQueryExecutor<ObjectGraphTypes> parameterCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rule.getApprovedObjectGraphBySourceProductAndModuleName").addParameter("productId", productId)
                .addParameter("moduleName", moduleName).addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(parameterCriteria);
        if (objectGraphTypesList.size() > 0)
            return objectGraphTypesList;
        else
            return null;
    }

    @Override
    public String populateFields(String className) {
        NamedQueryExecutor<String> parameterExecutor = new NamedQueryExecutor<String>("Rules.EntityType").addParameter(
                "className", className).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<String> fields = entityDao.executeQuery(parameterExecutor);
        if (null != fields) {
            return fields.get(0);
        } else {
            return "";
        }
    }

    @Override
    public <T extends BaseEntity> List<T> loadEntity(Class<T> entityClass) {
        if (entityClass == null) {
            throw new InvalidDataException("Entity class cannot be null");
        }
        return entityDao.findAll(entityClass);
    }

    @Override
    public List<Rule> getApprovedRulesBySourceProduct(String name, String productId) {
        NamedQueryExecutor<Rule> conditionCriteria = new NamedQueryExecutor<Rule>(
                "Rules.ApprovedRules.BasedOnProductSelected").addParameter("name", "%" + name + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(conditionCriteria);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }
    
    @Override
    public List<Rule> getApprovedRulesBySourceProduct(String sourceProduct) {
        NamedQueryExecutor<Rule> conditionCriteria = new NamedQueryExecutor<Rule>(
                "Rules.ApprovedRules.BasedOnSourceProduct").addParameter(
                "sourceProduct", sourceProduct).addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(conditionCriteria);
        return hasElements(ruleList)?ruleList:null;
    }

    @Override
    public List<Rule> getApprovedCriteriaRulesBySourceProduct(String name, String productId) {
        NamedQueryExecutor<Rule> conditionCriteria = new NamedQueryExecutor<Rule>(
                "Rules.ApprovedCriteriaRules.BasedOnProductSelected").addParameter("name", "%" + name + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(conditionCriteria);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }

    @Override
    public List<ObjectGraphTypes> getApprovedObjectGraphbyObjectGraphDescription(String objectGraph) {
        NamedQueryExecutor<ObjectGraphTypes> parameterCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rules.ApprovedObjectGraph.BasedOnObjectGraphSelected").addParameter("objectGraph", objectGraph)
        		.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(parameterCriteria);
        if (objectGraphTypesList.size() > 0)
            return objectGraphTypesList;
        else
            return null;
    }

    @Override
    public NullParameter getNullParameter() {

        List<NullParameter> nullParameter = entityDao.findAll(NullParameter.class);
        if (nullParameter.size() > 0)
            return nullParameter.get(0);
        else
            return null;
    }

    @Override
    public void saveRuleGroup(RuleGroup ruleGroup) {
        NeutrinoValidator.notNull(ruleGroup, "ruleGroup cannot be null");
        entityDao.persist(ruleGroup);
    }

    @Override
    public <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, Map<String, Object> propertyValueMap, Long id) {
        return baseMasterDao.hasEntity(entityClass, propertyValueMap, id);
    }

    @SuppressWarnings("rawtypes")
	@Override
    public <T extends Entity> List searchEntityData(Class<T> entityClass, String[] fields) {
        List<Map<String, ?>> result = null;

        MapQueryExecutor executor = new MapQueryExecutor(entityClass);

        if (fields != null) {
            for (String colName : fields) {
                executor.addQueryColumns(colName);
            }
            executor.addQueryColumns("id");
            boolean isBaseMasterEntity = BaseMasterEntity.class.isAssignableFrom(entityClass);
            if (isBaseMasterEntity) {
            	executor.addAndClause("masterLifeCycleData.approvalStatus in :approvalStatus");
                executor.addBoundParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
            }
            result = entityDao.executeQuery(executor);
        }
        return result;

    }

    @Override
    public <T extends BaseEntity> T getEntityById(Class<T> geoEntityclass, Long id) {
        return entityDao.find(geoEntityclass, id);

    }

    @Override
    public String getRuleErrorMessage(Rule rule, Locale locale) {
        String message = rule.getCode()+" : ";
        if (null != rule.getErrorMessageKey() && !rule.getErrorMessageKey().equals("")) {
            String errorMessageValue = messageSource.getMessage(rule.getErrorMessageKey(), null, rule.getErrorMessage(),
                    locale);
            return message+errorMessageValue;
        } else {
            return message+rule.getErrorMessage();
        }
    }

    @Override
    public String getRuleErrorMessage(Rule rule,Locale locale,Map<Object,Object> contextMap){
        String message = rule.getCode()+" : ";
        if (null != rule.getErrorMessageKey() && !rule.getErrorMessageKey().equals("")) {
            String errorMessageValue = messageSource.getMessage(rule.getErrorMessageKey(), null, rule.getErrorMessage(),
                    locale);
            return message+errorMessageValue;
        } else {
            return message+addParamsInErrorMessage(rule.getErrorMessage(),contextMap);
        }
    }

    private String addParamsInErrorMessage(String errorMessage, Map<Object,Object> contextMap){
        if(errorMessage!=null && !errorMessage.isEmpty() && (errorMessage.contains(RuleConstants.LEFT_CURLY_BRACES) || errorMessage.contains(RuleConstants.RIGHT_CURLY_BRACES))){
            List<String> paramNames =generateParamNamesList(errorMessage);
            if(CollectionUtils.isNotEmpty(paramNames)) {
                Map<String, String> replacementMap = new HashMap<>();
                paramNames.forEach(pn -> {
                    if(pn!=null && !pn.isEmpty()){
                        Parameter parameter = getApprovedParameterByName(pn);
                        String key = RuleConstants.LEFT_CURLY_BRACES+pn+RuleConstants.RIGHT_CURLY_BRACES;
                        String value=RuleConstants.NOT_APPLICABLE;
                        if(parameter!=null){
                            try {
                                value = evaluateParameter(parameter, contextMap);
                            }catch(Exception e){
                                BaseLoggers.exceptionLogger.error("Exception occured while evaluating params for error Message:",e);
                            }
                        }
                        replacementMap.put(key,value);
                    }
                });
             errorMessage = replaceParamsByvalue(errorMessage,replacementMap);
            }

        }
        return errorMessage;

    }

    private String replaceParamsByvalue(String errorMessage, Map<String, String> replacementMap){
        for (Map.Entry<String, String> entry : replacementMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            errorMessage = errorMessage.replace(key, value);
        }
        return errorMessage;
    }

    @Override
    public String getRuleSuccessMessage(Rule rule, Locale locale) {
        String message = rule.getCode()+" : ";
        if (null != rule.getSuccessMessageKey() && !rule.getSuccessMessageKey().equals("")) {
            String successMessageValue = messageSource.getMessage(rule.getSuccessMessageKey(), null,
                    rule.getSuccessMessage(), locale);
            return message+successMessageValue;
        } else {
            return message+rule.getSuccessMessage();
        }
    }

    @Override
    public String getRuleSuccessMessage(Rule rule , Locale locale, Map<Object, Object> contextMap){
        String message = rule.getCode()+" : ";
        if (null != rule.getSuccessMessageKey() && !rule.getSuccessMessageKey().equals("")) {
            String successMessageValue = messageSource.getMessage(rule.getSuccessMessageKey(), null,
                    rule.getSuccessMessage(), locale);
            return message+successMessageValue;
        }else{
            return message+addParamsInErrorMessage(rule.getErrorMessage(),contextMap);
        }
    }

    @Override
    public Rule getAllRulesByName(String name) {
        NamedQueryExecutor<Rule> ruleExecutor = new NamedQueryExecutor<Rule>("Rules.AllRulesByName").addParameter("name",
                name).addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleExecutor);
        if (null != ruleList && ruleList.size() > 0)
            return ruleList.get(0);
        else
            return null;
    }

    @Override
    public List<Rule> getNonCriteriaRules() {
        NamedQueryExecutor<Rule> ruleExecutor = new NamedQueryExecutor<Rule>("Rules.NonCriteriaRules")
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        
        List<Rule> ruleList = entityDao.executeQuery(ruleExecutor);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }

    @Override
    public List<RuleTagType> retrieveRuleTags() {
        return entityDao.findAll(RuleTagType.class);
    }

    @Override
    public RuleTagType saveRuleTagType(RuleTagType ruleTagType) {
        entityDao.persist(ruleTagType);
        return ruleTagType;
    }

    @Override
    public Rule getRuleByTagName(String name, List<String> tagNameList) {
        NamedQueryExecutor<Rule> ruleExecutor = new NamedQueryExecutor<Rule>("Rules.RuleByNameAndTagName").addParameter(
                "name", name).addParameter("tagNameList", tagNameList)
                .addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleExecutor);
        if (ruleList.size() > 0)
            return ruleList.get(0);
        else
            return null;
    }

    @Override
    public RuleTagType getRuleTagByTagName(String tagName) {
        NamedQueryExecutor<RuleTagType> ruleExecutor = new NamedQueryExecutor<RuleTagType>("Rules.RuleTagByTagName")
                .addParameter("tagName", tagName);
        List<RuleTagType> ruleTagTypeList = entityDao.executeQuery(ruleExecutor);
        if (ruleTagTypeList.size() > 0)
            return ruleTagTypeList.get(0);
        else
            return null;
    }

    @Override
    public List<ParameterDataType> loadParamtypeBasedOnParameter(List<String> codes) {
        NamedQueryExecutor<ParameterDataType> paramTypeExecutor = new NamedQueryExecutor<ParameterDataType>(
                "Rules.loadParamDataTypes").addParameter("code", codes);

        List<ParameterDataType> paramDataTypeList = new ArrayList<ParameterDataType>();

        paramDataTypeList = entityDao.executeQuery(paramTypeExecutor);
        return paramDataTypeList;
    }

    @Override
    public List<Map<String, ?>> loadParamtypeBasedOnParam(List<String> codes) {
        NamedQueryExecutor<Map<String, ?>> paramTypeExecutor = new NamedQueryExecutor<Map<String, ?>>(
                "Rules.loadParamDataTypesForBinder").addParameter("code", codes);

        List<Map<String, ?>> paramDataTypeList = null;

        paramDataTypeList = entityDao.executeQuery(paramTypeExecutor);
        return paramDataTypeList;
    }

    /**
     * This method give all the EventMappings where a Particular rule is used
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Map getRulesUsages(Long id) {
        Map map = new HashMap();
        NamedQueryExecutor<Map<String, ?>> executor = new NamedQueryExecutor<Map<String, ?>>("RuleUsage.getEventMapping");
        executor.addParameter("rule", id);
        List result = entityDao.executeQuery(executor);
        map.put("ruleInvocationMappingPoint", result);
        return map;
    }

    @Override
    public ScriptParameter encryptScriptCode(ScriptParameter scriptParameter) {
        if (null != scriptParameter) {
            scriptParameter.setScriptCode(encryptString(scriptParameter.getScriptCodeValue()));
        }
        return scriptParameter;
    }

    @Override
    public ScriptParameter decryptScriptCode(ScriptParameter scriptParameter) {
        if (null != scriptParameter) {
            scriptParameter.setScriptCodeValue(decryptString(scriptParameter.getScriptCode()));
        }
        return scriptParameter;
    }

    @Override
    public ScriptRule encryptScriptCode(ScriptRule scriptRule) {
        if (null != scriptRule) {
            scriptRule.setScriptCode(encryptString(scriptRule.getScriptCodeValue()));
        }
        return scriptRule;
    }

    @Override
    public ScriptRule decryptScriptCode(ScriptRule scriptRule) {
        if (null != scriptRule) {
            scriptRule.setScriptCodeValue(decryptString(scriptRule.getScriptCode()));
        }
        return scriptRule;
    }

    @Override
    public List<Rule> getRulesbySourceProduct(String name, String productId, int startIndex, int pageSize) {
        NamedQueryExecutor<Rule> ruleCriteria = new NamedQueryExecutor<Rule>("Rules.ApprovedRules.BasedOnProductSelected")
                .addParameter("name", "%" + name.toLowerCase() + "%").addParameter("productId", productId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleCriteria, startIndex, pageSize);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }

    @Override
    public long getCountOfApprovedRulesbySourceProduct(String name, String productId) {
        NamedQueryExecutor<Long> ruleCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedRules.BasedOnProductSelected.Count").addParameter("name", "%" + name.toLowerCase() + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        long size = entityDao.executeQueryForSingleValue(ruleCriteria);
        return size;
    }

    @Override
    public List<Rule> getCriteriaRules(String name, String productId, int startIndex, int pageSize) {
        NamedQueryExecutor<Rule> ruleCriteria = new NamedQueryExecutor<Rule>(
                "Rules.ApprovedCriteriaRules.BasedOnProductSelected").addParameter("name", "%" + name + "%").addParameter(
                "productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleCriteria, startIndex, pageSize);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }

    @Override
    public long getCountOfCriteriaRules(String name, String productId) {
        NamedQueryExecutor<Long> ruleCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedCriteriaRules.BasedOnProductSelected.Count").addParameter("name", "%" + name + "%")
                .addParameter("productId", productId).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        long size = entityDao.executeQueryForSingleValue(ruleCriteria);
        return size;
    }

    @Override
    public Map<String, String> getParameterNameDesc(String name, String productId, List<Integer> dataTypes, int startIndex,
            int pageSize) {

        MapQueryExecutor executor = new MapQueryExecutor(Parameter.class, "r").addColumn("r.name", "name").addColumn(
                "r.description", "description");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" name like :name AND sourceProduct = :sourceProduct "
        		+ "AND  dataType  in  :dataType AND masterLifeCycleData.approvalStatus in :statusList "
        		+ "AND (entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) "
                + "AND (collectionBased IS NULL OR collectionBased = :collectionBased)");

        executor.addAndClause(whereClause.toString())
        		.addBoundParameter("name","%" + name + "%")
        		.addBoundParameter("sourceProduct", productId)
        		.addBoundParameter("dataType", dataTypes)
        		.addBoundParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
        		.addBoundParameter("collectionBased", false);
        List<Map<String, ?>> result = entityDao.executeQuery(executor, startIndex, pageSize);

        /**
         * Map contains Parameter name as key Parameter description as value
         * this Map is converted into JSON and displayed in Advance View
         */
        Map<String, String> finalResult = new LinkedHashMap<String, String>();
        for (Map<String, ?> map : result) {
            finalResult.put(map.get("name").toString(), map.get("description").toString());
        }

        return finalResult;
    }

    @Override
    public Map<String, String> getCollectionTypeParameter(String name, String objectGraph, String productId,
            List<Integer> dataTypes, int startIndex, int pageSize) {

        MapQueryExecutor executor = new MapQueryExecutor(ObjectGraphParameter.class, "r").addColumn("r.name", "name")
                .addColumn("r.description", "description").addColumn("r.objectGraph", "objectGraph");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" objectGraph like :objectGraph AND name like :name "
        		+ "AND sourceProduct = :sourceProduct AND  dataType  in :dataType "
        		+ "AND  masterLifeCycleData.approvalStatus in :statusList "
        		+ "AND (entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false)");

        executor.addAndClause(whereClause.toString())
        		.addBoundParameter("objectGraph","%" + objectGraph + ".%")
        		.addBoundParameter("name", "%" + name + "%")
				.addBoundParameter("sourceProduct", productId)
				.addBoundParameter("dataType", dataTypes)
				.addBoundParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, ?>> result = entityDao.executeQuery(executor, startIndex, pageSize);

        /**
         * Map contains Parameter name as key Parameter description as value
         * this Map is converted into JSON and displayed in Advance View
         */
        int countOfColectionInSource = 0;
        boolean isCollectionGraph = false;
        if (objectGraph.contains(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)) {
            countOfColectionInSource = StringUtils
                    .countMatches(objectGraph, RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE);
            isCollectionGraph = true;
        }
        Map<String, String> finalResult = new LinkedHashMap<String, String>();
        for (Map<String, ?> map : result) {
            if (isCollectionGraph) {
                int countOfCollectionInCurrentObjectGraph = StringUtils.countMatches((String) map.get("objectGraph"),
                        RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE);
                if (countOfColectionInSource == countOfCollectionInCurrentObjectGraph) {
                    finalResult.put(map.get("name").toString(), map.get("description").toString());
                }
            } else {
                finalResult.put(map.get("name").toString(), map.get("description").toString());
            }
        }
        return finalResult;
    }

    @Override
    public Map<String, String> getConditionNameDesc(String name, String productId, int startIndex, int pageSize) {
        MapQueryExecutor executor = new MapQueryExecutor(Condition.class, "r").addColumn("r.name", "name").addColumn(
                "r.description", "description");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" name like :name AND sourceProduct = :sourceProduct "
        		+ "AND masterLifeCycleData.approvalStatus in :statusList "
        		+ "AND (entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false)");

        executor.addAndClause(whereClause.toString())
        		.addBoundParameter("name","%" + name + "%")
				.addBoundParameter("sourceProduct", productId)
				.addBoundParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, ?>> result = entityDao.executeQuery(executor, startIndex, pageSize);

        /**
         * Map contains Condition name as key Condition description as value
         * this Map is converted into JSON and displayed in Advance View
         */

        Map<String, String> finalResult = new LinkedHashMap<String, String>();

        for (Map<String, ?> map : result) {

            finalResult.put(map.get("name").toString(), map.get("description").toString());
        }
        return finalResult;
    }

    @Override
    public Long getParameterIdByName(String name) {
        NamedQueryExecutor<Long> ruleExecutor = new NamedQueryExecutor<Long>("Rules.id.ParameterByName").addParameter(
                "name", name);
        List<Long> paramList = entityDao.executeQuery(ruleExecutor);
        if (paramList.size() > 0) {
            if (paramList.size() > 1) {
                for (Long paramId : paramList) {
                    Parameter param = getParameter(paramId);
                    if (param.getApprovalStatus() == ApprovalStatus.APPROVED) {
                        return paramId;
                    }
                }
            }

            return paramList.get(0);
        }

        else
            return null;
    }

    @Override
    public Long getConditionIdByName(String name) {
        NamedQueryExecutor<Long> ruleExecutor = new NamedQueryExecutor<Long>("Rules.id.ConditionByName").addParameter(
                "name", name).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        List<Long> ruleList = entityDao.executeQuery(ruleExecutor);
        if (ruleList.size() > 0)
            return ruleList.get(0);
        else
            return null;
    }

    @Override
    public ModuleName getModuleNameFromName(String moduleName) {

        NamedQueryExecutor<ModuleName> ruleExecutor = new NamedQueryExecutor<ModuleName>("Rule.getModuleNameFromName")
                .addParameter("name", moduleName);
        List<ModuleName> noduleNameList = entityDao.executeQuery(ruleExecutor);
        if (noduleNameList != null && noduleNameList.size() > 0)
            return noduleNameList.get(0);
        else
            return null;
    }

    @Override
    public List<Rule> getApprovedAndActiveRule() {
        NamedQueryExecutor<Rule> ruleExecutor = new NamedQueryExecutor<Rule>("Approved.Active.Rules")
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleExecutor);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }

    @SuppressWarnings("rawtypes")
	@Override
    public List executeQuery(String queryName) {
        NamedQueryExecutor<Object> executor = new NamedQueryExecutor<Object>(queryName);
        return entityDao.executeQuery(executor);
    }

    @Override
    public EntityType getEntityTypeData(String className) {
        NamedQueryExecutor<EntityType> parameterExecutor = new NamedQueryExecutor<EntityType>("Rules.entityTypeData")
                .addParameter("className", className).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<EntityType> fields = entityDao.executeQuery(parameterExecutor);
        if (null != fields) {
            return fields.get(0);
        } else {
            return null;
        }
    }

    @Override
    public List<ScriptParameter> getAllMvelBasedScriptParameters() {

        NamedQueryExecutor<ScriptParameter> ruleExecutor = new NamedQueryExecutor<ScriptParameter>(
                "Rules.AllSciptParameterMvelBased").addParameter("scriptCodeType", 2).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<ScriptParameter> paramList = entityDao.executeQuery(ruleExecutor);
        if (paramList.size() > 0)
            return paramList;
        else
            return null;

    }

    @Override
    public ObjectGraphTypes getObjectGraphTypesWithDisplayName(String displayName) {

        NamedQueryExecutor<ObjectGraphTypes> queryExecutor = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rules.getObjectGraphTypesWithDisplayName").addParameter("displayName", displayName)
        		.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(queryExecutor);
        if (objectGraphTypesList.size() > 0)
            return objectGraphTypesList.get(0);
        else
            return null;
    }

    @Override
    public List<ObjectGraphParameter> getCollectionTypGraphByDataType(String dataType) {

        NamedQueryExecutor<ObjectGraphParameter> ruleExecutor = new NamedQueryExecutor<ObjectGraphParameter>(
                "Rules.ObjectGraphParamOfCollectionTypeByDataType").addParameter("objectGraph", "%[]%").addParameter(
                "dataType", Integer.parseInt(dataType)).addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<ObjectGraphParameter> ogList = entityDao.executeQuery(ruleExecutor);

        if (ogList.size() > 0)
            return ogList;
        else
            return null;
    }

    @Override
    public List<ObjectGraphParameter> getAttributesOfCollectionTypOgnl(String parentOgnl) {
        NamedQueryExecutor<ObjectGraphParameter> ruleExecutor = new NamedQueryExecutor<ObjectGraphParameter>(
                "Rules.ObjectGraphParamOfCollectionType.allAttribute").addParameter("objectGraph", parentOgnl)
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<ObjectGraphParameter> ogList = entityDao.executeQuery(ruleExecutor);
        if (ogList.size() > 0)
            return ogList;
        else
            return null;
    }

    @Override
    public Map<String, String> getParameterByDataParamType(String name, String productId, List<Integer> dataTypes,
            List<Integer> paramTypes, int startIndex, int pageSize) {
        MapQueryExecutor executor = new MapQueryExecutor(Parameter.class, "r").addColumn("r.name", "name").addColumn(
                "r.description", "description");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("sourceProduct = :sourceProduct AND name like :name AND dataType  in :dataType "
        		+ "AND paramType in :paramType AND masterLifeCycleData.approvalStatus in :statusList "
                + "AND (entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false)");

        executor.addAndClause(whereClause.toString())
        		.addBoundParameter("sourceProduct", productId)
		        .addBoundParameter("name","%" + name + "%")
				.addBoundParameter("dataType", dataTypes)
				.addBoundParameter("paramType", paramTypes)
				.addBoundParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, ?>> result = entityDao.executeQuery(executor, startIndex, pageSize);

        /**
         * Map contains Parameter name as key Parameter description as value
         * this Map is converted into JSON and displayed in Advance View
         */
        Map<String, String> finalResult = new LinkedHashMap<String, String>();
        for (Map<String, ?> map : result) {

            finalResult.put(map.get("name").toString(), map.get("description").toString());
        }
        return finalResult;
    }

    @Override
    public Long getCountOfParamete(String name, String productId, List<Integer> dataTypes, List<Integer> paramTypes) {
        NamedQueryExecutor<Long> ruleCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedParameter.BasedOnProductDataParamType.Count").addParameter("productId", productId)
                .addParameter("dataType", dataTypes).addParameter("paramType", paramTypes)
                .addParameter("name", "%" + name + "%")
                .addParameter("approvalStatus", ApprovalStatus.APPROVED);
        return entityDao.executeQueryForSingleValue(ruleCriteria);
    }

    @Override
    public Map<String, String> getRuleNameAndDesc(String name, String productId, int startIndex, int pageSize) {

        MapQueryExecutor executor = new MapQueryExecutor(Rule.class, "r").addColumn("r.name", "name").addColumn(
                "r.description", "description");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" lower(name) like lower(:name) AND " + "sourceProduct = :sourceProduct "
        		+ "AND masterLifeCycleData.approvalStatus in :statusList and activeFlag = true");

        executor.addAndClause(whereClause.toString())
		        .addBoundParameter("name","%" + name + "%")
		        .addBoundParameter("sourceProduct", productId)
				.addBoundParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, ?>> result = entityDao.executeQuery(executor, startIndex, pageSize);

        /**
         * Map contains Rule name as key Rule description as value
         * this Map is converted into JSON and displayed in Advance View
         */

        Map<String, String> finalResult = new LinkedHashMap<String, String>();

        for (Map<String, ?> map : result) {

            finalResult.put(map.get("name").toString(), map.get("description").toString());
        }
        return finalResult;
    }

    @Override
    public Long getRuleIdByName(String name) {
        NamedQueryExecutor<Long> ruleExecutor = new NamedQueryExecutor<Long>("Rules.id.RulesByName").addParameter("name",
                name).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        List<Long> ruleList = entityDao.executeQuery(ruleExecutor);
        if (ruleList.size() > 0)
            return ruleList.get(0);
        else
            return null;
    }

    @Override
    public Long getCountOfCollectionTypParam(String name, String currentTargetOgnl, String productId, List<Integer> dataTypes) {
        NamedQueryExecutor<Long> ruleCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedCollectionParameter.BasedOnProductDataParamType.Count").addParameter("productId", productId)
                .addParameter("dataType", dataTypes).addParameter("objectGraph", currentTargetOgnl)
                .addParameter("name", name)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED);
        return entityDao.executeQueryForSingleValue(ruleCriteria);
    }

    @Override
    public List<ObjectGraphTypes> getObjectGraphTypesByIds(long[] ids) {

        List<ObjectGraphTypes> objectGraphTypeList = new ArrayList<ObjectGraphTypes>();

        for (long id : ids) {

            ObjectGraphTypes objectGraphType = entityDao.find(ObjectGraphTypes.class, id);
            if (objectGraphType == null) {
                return null;
            }
            objectGraphTypeList.add(objectGraphType);
        }
        return objectGraphTypeList;
    }

    @Override
    public List<ObjectGraphTypes> getOgnlBySourceProductAndModule(String productId, Long moduleName) {

        NamedQueryExecutor<ObjectGraphTypes> parameterCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rule.getApprovedObjectGraphBySourceProductAndModule").addParameter("productId", productId).addParameter(
                "moduleName", moduleName).addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        		

        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(parameterCriteria);

        if (objectGraphTypesList.size() > 0)
            return objectGraphTypesList;
        else
            return null;
    }

    @Override
    public List<Parameter> getApprovedParametersbyDataTypePaginated(String name, String productId, List<Integer> dataTypes,
            int startIndex, int pageSize, Long moduleId) {

        NamedQueryExecutor<Parameter> parameterCriteria = new NamedQueryExecutor<Parameter>(
                "Rules.ApprovedParameters.BasedOnDataTypeModuleSelected").addParameter("name", "%" + name.toLowerCase() + "%")
                .addParameter("productId", productId).addParameter("dataType", dataTypes)
                .addParameter("moduleName", moduleId).addParameter("approvalStatus", ApprovalStatus.APPROVED);

        List<Parameter> parameters = entityDao.executeQuery(parameterCriteria, startIndex, pageSize);

        if (null != parameters && parameters.size() > 0) {
            return parameters;
        }

        return null;
    }

    @Override
    public long getCountOfApprovedParametersbyDataType(String name, String productId, List<Integer> dataTypes, Long moduleId) {

        NamedQueryExecutor<Long> parameterCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedParameters.BasedOnDataTypeProductModuleSelected.Count")
                .addParameter("name", "%" + name.toLowerCase() + "%").addParameter("productId", productId)
                .addParameter("dataType", dataTypes).addParameter("moduleName", moduleId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED);
        return entityDao.executeQueryForSingleValue(parameterCriteria);

    }

    @Override
    public ModuleName getModuleById(Long moduleId) {

        if (null != moduleId) {

            return entityDao.find(ModuleName.class, moduleId);
        }

        return null;
    }

    @Override
    public List<ObjectGraphParameter> getCollectionTypGraphByDataType(String dataType, Long moduleId) {

        NamedQueryExecutor<ObjectGraphParameter> ruleExecutor = new NamedQueryExecutor<ObjectGraphParameter>(
                "Rules.ObjectGraphParamOfCollectionTypeByDataTypeModule").addParameter("objectGraph", "%[]%")
                .addParameter("dataType", Integer.parseInt(dataType)).addParameter("moduleName", moduleId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED);
        List<ObjectGraphParameter> ogList = entityDao.executeQuery(ruleExecutor);

        if (ogList.size() > 0)
            return ogList;
        else
            return null;
    }

    @Override
    public List<Condition> getConditionsSourceProductPage(String name, String productId, int startIndex, int pageSize,
            Long moduleId) {
        NamedQueryExecutor<Condition> conditionCriteria = new NamedQueryExecutor<Condition>(
                "Rules.ApprovedConditions.BasedOnProductSelectedModule").addParameter("name", "%" + name.toLowerCase() + "%")
                .addParameter("productId", productId).addParameter("moduleName", moduleId).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        List<Condition> conditionList = entityDao.executeQuery(conditionCriteria, startIndex, pageSize);
        if (conditionList.size() > 0)
            return conditionList;
        else
            return null;
    }

    @Override
    public long getCountOfConditions(String name, String productId, Long moduleId) {
        NamedQueryExecutor<Long> conditionCriteria = new NamedQueryExecutor<Long>(
                "Rules.ApprovedConditions.BasedOnProductSelectedModule.Count").addParameter("name", "%" + name.toLowerCase() + "%")
                .addParameter("productId", productId).addParameter("moduleName", moduleId).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        long size = entityDao.executeQueryForSingleValue(conditionCriteria);
        return size;
    }

    @Override
    public List<Parameter> getParametersBySpecificModule(String name, String productId, Long moduleId) {
        NamedQueryExecutor<Parameter> parameterCriteria = new NamedQueryExecutor<Parameter>(
                "Rules.ApprovedParameters.BasedOnProductSpecificModule").addParameter("name", "%" + name + "%")
                .addParameter("productId", productId).addParameter("moduleName", moduleId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Parameter> parameterList = entityDao.executeQuery(parameterCriteria);
        if (parameterList.size() > 0)
            return parameterList;
        else
            return null;
    }

    @Override
    public List<Condition> getConditionBySpecificModule(String name, String productId, Long moduleId) {
        NamedQueryExecutor<Condition> conditionCriteria = new NamedQueryExecutor<Condition>(
                "Rules.ApprovedConditions.BasedOnProductSpecificModule").addParameter("name", "%" + name + "%")
                .addParameter("productId", productId).addParameter("moduleName", moduleId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Condition> conditionList = entityDao.executeQuery(conditionCriteria);
        if (conditionList.size() > 0)
            return conditionList;
        else
            return null;
    }

    @Override
    public List<Rule> getRuleBySpecificModule(String name, String productId, Long moduleId) {
        NamedQueryExecutor<Rule> ruleCriteria = new NamedQueryExecutor<Rule>(
                "Rules.ApprovedRules.BasedOnProductSpecificModule").addParameter("name", "%" + name + "%")
                .addParameter("productId", productId).addParameter("moduleName", moduleId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Rule> ruleList = entityDao.executeQuery(ruleCriteria);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }

    @Override
    public List<CompoundParameter> getComputedParamsUsingThisParam(Long paramId, String productId) {
        NamedQueryExecutor<CompoundParameter> parameterCriteria = new NamedQueryExecutor<CompoundParameter>(
                "Rules.ParametersUsage.InCompoundParameter").addParameter("productId", productId).addParameter("paramId",
                "%" + paramId + "%").addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);

        List<CompoundParameter> paramList = entityDao.executeQuery(parameterCriteria);
        if (paramList.size() > 0)
            return paramList;
        else
            return null;
    }

    @Override
    public List<Condition> getConditionsUsingThisParam(Long paramId, String productId) {
        NamedQueryExecutor<Condition> conditionCriteria = new NamedQueryExecutor<Condition>(
                "Rules.ParametersUsage.InCondition").addParameter("productId", productId).addParameter("paramId",
                "%" + paramId + "%").addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);

        List<Condition> condList = entityDao.executeQuery(conditionCriteria);
        if (condList.size() > 0)
            return condList;
        else
            return null;
    }

    @Override
    public List<Rule> getRulesUsingThisCondition(Long condId, String productId) {
        NamedQueryExecutor<Rule> ruleCriteria = new NamedQueryExecutor<Rule>("Rules.ConditionsUsage.InRule").addParameter(
                "productId", productId).addParameter("condId", "%" + condId + "%")
                .addParameter("approvalStatus",ApprovalStatus.APPROVED_RECORD_STATUS_LIST);

        List<Rule> ruleList = entityDao.executeQuery(ruleCriteria);
        if (ruleList.size() > 0)
            return ruleList;
        else
            return null;
    }

    @Override
    public Map<Long, String> getApprovedRuleIdAndName() {
        NamedQueryExecutor<Map<String, Object>> queryExecutor = new NamedQueryExecutor<Map<String, Object>>(
                "Approved.Active.Rules.IdAndName").addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, Object>> rulesList = entityDao.executeQuery(queryExecutor);
        Map<Long, String> rulesMap = new LinkedHashMap<Long, String>();
        if (!CollectionUtils.isEmpty(rulesList)) {
            Iterator<Map<String, Object>> ruleIterator = rulesList.iterator();
            while (ruleIterator.hasNext()) {
                Map<String, Object> rule = ruleIterator.next();

                if (rule.get("name") != null) {
                    Long ruleId = (Long) rule.get("id");
                    rulesMap.put(ruleId, (String.valueOf(rule.get("name"))));
                }
            }
        }

        if (rulesMap.size() > 0)
            return rulesMap;
        else
            return null;
    }

    @Override
    public List<Map<String, Object>> getApprovedParametersbyDataTypes(String productId, List<Integer> dataTypes,
            Long moduleId) {

        NamedQueryExecutor<Map<String, Object>> parameterCriteria = new NamedQueryExecutor<Map<String, Object>>(
                "Rules.ApprovedParameters.BasedOnDataTypeModuleSelectedWithoutName").addParameter("productId", productId)
                .addParameter("dataType", dataTypes).addParameter("moduleName", moduleId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, Object>> parameters = entityDao.executeQuery(parameterCriteria);

        if (null != parameters && parameters.size() > 0) {
            return parameters;
        }

        return null;
    }

    @Override
    public List<Map<String, Object>> getApprovedParametersbyDataTypes(String productId, List<Integer> dataTypes) {

        NamedQueryExecutor<Map<String, Object>> parameterCriteria = new NamedQueryExecutor<Map<String, Object>>(
                "Rules.ApprovedParameters.BasedOnDataTypeProductSelectedWithoutName").addParameter("productId", productId)
                .addParameter("dataType", dataTypes)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<Map<String, Object>> parameters = entityDao.executeQuery(parameterCriteria);

        if (null != parameters && parameters.size() > 0) {
            return parameters;
        }

        return null;
    }

    @Override
    public List<Map<String, Object>> getAllApprovedRuleMap() {
        NamedQueryExecutor<Map<String, Object>> ruleCriteria = new NamedQueryExecutor<Map<String, Object>>(
                "Rules.AllApprovedRuleMap").addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(ruleCriteria);
    }

    @Override
    public List<Map<String, Object>> getAllApprovedObjectGraphParameter() {
        NamedQueryExecutor<Map<String, Object>> ruleCriteria = new NamedQueryExecutor<Map<String, Object>>(
                "Parameters.AllApprovedObjectGraphParameter").addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(ruleCriteria);
    }

    @Override
    public String getObjectGraphTypesDescWithDisplayName(String displayName) {

        NamedQueryExecutor<String> queryExecutor = new NamedQueryExecutor<String>(
                "Rules.getObjectGraphTypesDescWithDisplayName").addParameter("displayName", displayName)
        		.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        List<String> objectGraphTypesDescList = entityDao.executeQuery(queryExecutor);
        if (objectGraphTypesDescList != null && objectGraphTypesDescList.size() > 0)
            return objectGraphTypesDescList.get(0);
        else
            return null;
    }

    @Override
    public String getParameterDescWithDisplayName(String name) {

        NamedQueryExecutor<String> queryExecutor = new NamedQueryExecutor<String>("Rules.getParameterDescWithDisplayName")
                .addParameter("name", name).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        List<String> parameterDescList = entityDao.executeQuery(queryExecutor);
        if (parameterDescList != null && parameterDescList.size() > 0)
            return parameterDescList.get(0);
        else
            return null;
    }

    @Override
    public String findNamethroughObjectGraph(String objectGraph) {
		if (StringUtils.isNotBlank(objectGraph)) {
			return parameterService.getNameByObjectGraph(objectGraph);
		}
        return null;
    }

    @Override
	public String getApprovedParameterOgnlByName(String parameterName) {
		ObjectGraphParameter param = parameterService.getParameterByNameAndType(parameterName,
				ObjectGraphParameter.class);
		if (param != null) {
			return param.getObjectGraph();
		}
		return null;
	}

    @Override
    public List<Map<String, Object>> getAllRequiredParameterForScoreCard(List<Integer> paramList) {
        NamedQueryExecutor<Map<String, Object>> ruleCriteria = new NamedQueryExecutor<Map<String, Object>>(
                "Parameters.getAllRequiredParameterForScoreCard").addParameter("paramList", paramList)
        		.addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(ruleCriteria);
    }

    @Override
    public List<String> getValidationRuleErrorMessages(String eventCode, Map<Object, Object> contextMap) {
        List<String> ruleMessages = new ArrayList<String>();
        if (eventCode == null) {
            return ruleMessages;
        }
        EventDefinition eventDefinition = eventDefinitionService.getEventDefinitionByCode(eventCode);
        if (eventDefinition == null) {
            return ruleMessages;
        }

        if (CollectionUtils.isNotEmpty(eventDefinition.getEventTaskList())) {
            for (EventTask eventTask : eventDefinition.getEventTaskList()) {
                if (eventTask instanceof RuleValidationTask) {
                    RuleGroup ruleGroup = ((RuleValidationTask) eventTask).getRuleGroup();

                    if (ruleGroup != null && CollectionUtils.isNotEmpty(ruleGroup.getRules())) {
                        for (Rule rule : ruleGroup.getRules()) {
                            if (rule != null) {
                                Boolean result = (Boolean) contextMap.get(RulesConverterUtility.replaceSpace(rule.getName())
                                        + RuleConstants.PARAMETER_NAME_ID + rule.getId());
                                if (result != null && result == false) {
                                    String ruleMessage = getRuleErrorMessage(rule, Locale.getDefault(),contextMap);
                                    ruleMessages.add(ruleMessage);
                                }
                            }
                        }
                    }

                    break;
                }
            }
        }

        return ruleMessages;
    }
    
    public List<ObjectGraphTypes> getObjectGraphByRuleMatrixType(Long ruleMatrixTypeId) {
        NamedQueryExecutor<ObjectGraphTypes> parameterCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rules.ObjectGraph.BasedOnRuleMatrixType").addParameter("ruleMatrixTypeId", ruleMatrixTypeId)
                .addParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);;
        List<ObjectGraphTypes> objectGraphTypesList = entityDao.executeQuery(parameterCriteria);
        if (objectGraphTypesList.size() > 0)
            return objectGraphTypesList;
        else
            return null;
    }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public List searchRuleDataForAutoComplete(RuleAutoCompleteSearchVO autoCompleteSearchVO) {
        NeutrinoValidator.notNull(autoCompleteSearchVO.getClassName(), "Class name cannot be null");
        NeutrinoValidator.notNull(autoCompleteSearchVO.getSearchColumnList(), "Columns List cannot be null");
        NeutrinoValidator.notNull(autoCompleteSearchVO.getItemVal(), "Item value cannot be null");
        Class entityClass = null;
        List<Map<String, ?>> finalResult = new ArrayList<Map<String, ?>>();
        String[] classList = autoCompleteSearchVO.getClassName().split(",");
        
        
        
        int counter = 0;
        long totalRecords = 0;
        for (String tempClass : classList) 
        {
          try {
            entityClass = Class.forName(tempClass);
          } catch (ClassNotFoundException e) {
            BaseLoggers.exceptionLogger.error("Class with name " + tempClass + " Not Found: " + e.getMessage());
            throw new SystemException(e);
          }
          StringBuilder sb = new StringBuilder();
          boolean isFirstClause = true;
          
          MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(autoCompleteSearchVO.getSearchColumnList()).addQueryColumns(autoCompleteSearchVO.getItemVal());

          StringBuilder whereClause = new StringBuilder();
          updateExecutorAccordingToEntityClass(executor,whereClause,entityClass);
          for (String searchCol : autoCompleteSearchVO.getSearchColumnList()) {
            if (isFirstClause) {
              sb.append(" (lower(" + searchCol + ") like ");
              isFirstClause = false;
            } else {
              sb.append(" or " + "lower(" + searchCol + ") like ");
            }
            sb.append( "lower(:value) ");
          }
          sb.append(")");
          executor = executor.addAndClause(sb.toString()); 
          executor.addBoundParameter("value", "%"+autoCompleteSearchVO.getValue()+"%");
          
          String sortableField = "";
          sortableField = EntityUtil.getSortableField(entityClass);
          if (!sortableField.isEmpty()) {
              executor.addOrderByClause("ORDER BY lower(" + sortableField + ") ASC");
          }
          
          List<Map<String, ?>> result = entityDao.executeQuery(executor, autoCompleteSearchVO.getPage() * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);

          for (Map<String, ?> temp : result) {
              finalResult.add(counter, temp);
              counter++;
          }
          totalRecords = totalRecords + entityDao.executeTotalRowsQuery(executor);
        }
        
        Map<String, Long> sizeMap = new HashMap<String, Long>();
        sizeMap.put("size", totalRecords);
        finalResult.add(counter, sizeMap);
        if (finalResult != null) {
            BaseLoggers.flowLogger.debug("size of finalResult :" + finalResult.size());
        }
        return finalResult;
    }

    @SuppressWarnings("rawtypes")
	private void updateExecutorAccordingToEntityClass(
            MapQueryExecutor executor,
            StringBuilder whereClause,Class entityClass) {
         if("Rule".equals(entityClass.getSimpleName())) 
         {
        	 whereClause.append(" (criteriaRuleFlag = :criteriaRuleFlag) ");

             whereClause.append(" AND (masterLifeCycleData.approvalStatus = :approvalStatus) ");

             whereClause.append(" AND (entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) ");
             whereClause.append(" AND (scriptCodeType != 2 or scriptCodeType IS NULL) ");
             executor.addAndClause(whereClause.toString())
             		 .addBoundParameter("criteriaRuleFlag", false)
             		 .addBoundParameter("approvalStatus", ApprovalStatus.APPROVED);
         }
         else if("RuleAction".equals(entityClass.getSimpleName()) || "RuleSet".equals(entityClass.getSimpleName())) 
         {
             whereClause.append(" (activeFlag = true) ");
             whereClause.append(" AND (entityLifeCycleData.snapshotRecord is null or entityLifeCycleData.snapshotRecord = false) ");
             executor.addAndClause(whereClause.toString());

             executor.addAndClause("masterLifeCycleData.approvalStatus in :statusList")
     		 .addBoundParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
         }
         else if("EntityType".equals(entityClass.getSimpleName()))  
         {
             whereClause.append(" (entityLifeCycleData.snapshotRecord is null or entityLifeCycleData.snapshotRecord = false) ");
             executor.addAndClause(whereClause.toString());
         }
        
    }

    @Override
    public List<ObjectGraphTypes> getOgnlForModuleNameForTaskAssigmentMaster(String productId, String moduleName) {
        NamedQueryExecutor<ObjectGraphTypes> parameterCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
                "Rule.getApprovedObjectGraphforSpecificModuleName").addParameter("productId", productId).addParameter("moduleName", moduleName)
        		.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(parameterCriteria);
    }

    @Override
    public List<Map<String, Object>> getOgnlForModuleNameForTaskAssigmentMasterMapping(String productId, String moduleName) {
        NamedQueryExecutor<Map<String, Object>> parameterCriteria = new NamedQueryExecutor<Map<String, Object>>(
                "Rule.getApprovedObjectGraphMappingforSpecificModuleName").addParameter("productId", productId).addParameter("moduleName", moduleName)
                .addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(parameterCriteria);
    }

    @Override
    public List<ObjectGraphTypes> getObjectGraphsbyDataTypes(List<String> dataTypes) {

		NamedQueryExecutor<ObjectGraphTypes> objectGraphTypesCriteria = new NamedQueryExecutor<ObjectGraphTypes>(
				"Rules.ObjectGraphsbyDataTypes").addParameter("dataType",
				dataTypes).addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
		List<ObjectGraphTypes> objectGraphTypesList = entityDao
				.executeQuery(objectGraphTypesCriteria);
		return objectGraphTypesList;

    }
    
    
    @Override
    public SQLRule encryptSQLRule(SQLRule sqlRule) {
        if (null != sqlRule) {
            sqlRule.setSqlQuery(encryptString(sqlRule.getSqlQueryPlain()));
        }
        return sqlRule;
    }

    @Override
    public SQLRule decryptSQLRule(SQLRule scriptRule) {
        if (null != scriptRule) {
            scriptRule.setSqlQueryPlain(decryptString(scriptRule.getSqlQuery()));
        }
        return scriptRule;
    }
    
    @SuppressWarnings("rawtypes")
	@Override
    public String evaluateParameter(Parameter parameter,Map<Object, Object> objectmap) {
		String paramValue = null;
		String ognlTobeEvaluated;
		Object parameterResult;
		try {
            if (parameter instanceof ConstantParameter) {
                paramValue = (((ConstantParameter) parameter).getLiteral());

            } else if (parameter instanceof NullParameter) {
                paramValue = ("null");

            } else if (parameter instanceof SystemParameter) {
                paramValue = (String.valueOf(((SystemParameter) parameter)
                        .getSystemParameterValue()));

            } else if (parameter instanceof QueryParameter) {
                RuleQueryExecutor queryCriteria = new RuleQueryExecutor(((QueryParameter) parameter).getQuery());

                List<QueryParameterAttribute> queryParameterAttributes = ((QueryParameter) parameter)
                        .getQueryParameterAttributes();
                if (queryParameterAttributes != null) {
                    for (QueryParameterAttribute queryAttribute : queryParameterAttributes) {
                        queryCriteria.addQueryParameter(queryAttribute.getQueryParameterName(),
                                MVEL.eval(queryAttribute.getObjectGraph(), objectmap));
                    }
                }

                List list = entityDao.executeQuery(queryCriteria);
                if (list != null && !list.isEmpty() && null != list.get(0)) {
                    paramValue = (list.get(0).toString());
                }

            } else if (parameter instanceof CompoundParameter) {
                String compoundExpression = buildParameterExpression(
                        ((CompoundParameter) parameter).getParameterExpression(), objectmap);
                parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(compoundExpression, objectmap);
                if (null != parameterResult) {
                    paramValue = (parameterResult.toString());
                }

            } else if (parameter instanceof ReferenceParameter) {

                paramValue = (((ReferenceParameter) parameter).getReferenceEntityId()
                        .getUri());


            } else if (parameter instanceof ObjectGraphParameter) {

                if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {

                    ognlTobeEvaluated = "( ?"
                            + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                            .getObjectGraph() + RuleConstants.RULE_TIME_IN_MILLIS) + " )";

                    parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(ognlTobeEvaluated, objectmap);

                } else {
                    ognlTobeEvaluated = "( ?"
                            + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                            .getObjectGraph()) + " )";

                    parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(ognlTobeEvaluated, objectmap);
                }

                if (parameterResult != null) {

                    if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                        paramValue = (((BaseEntity) parameterResult).getUri());

                    } else {
                        paramValue = parameterResult.toString();
                    }

                }

            } else if (parameter instanceof ScriptParameter) {
                parameterResult = objectmap.get(RulesConverterUtility.replaceSpace(parameter.getName())
                        + RuleConstants.PARAMETER_NAME_ID + parameter.getId());
                if (null != parameterResult) {
                    paramValue = parameterResult.toString();
                }
            } else if (parameter instanceof SQLParameter) {
                Map<String, Object> resultMap = sqlExecutor.getParameterValue((SQLParameter) parameter, objectmap);
                if (resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND) != null) {
                    paramValue = resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND).toString();

                }
            }
        }catch(Exception e){
                BaseLoggers.exceptionLogger.error("Exception occured while evaluating parameter :" ,e);
                RuleExceptionLoggingVO ruleExceptionLoggingVO = new RuleExceptionLoggingVO();
                ruleExceptionLoggingVO.setContextMap(objectmap);
                ruleExceptionLoggingVO.setE(e);
                ruleExceptionLoggingVO.setParameter(parameter);
                ruleExceptionLoggingVO.setExceptionOwner(RuleConstants.PARAMETER_EXCEPTION);
                ruleExceptionLoggingService.saveRuleErrorLogs(ruleExceptionLoggingVO);
                throw new RuleException("Error occured while evaluating parameter : "+parameter.getCode()+" : ",e);
            }
		
		return paramValue;
	}
    
    
    /**
     * 
     * Method to build the parameter expression and evaluate 
     * @param parameterExpression
     * @param map
     * @return
     */
    public String buildParameterExpression(String parameterExpression, Map<Object, Object> map) {
        StringBuilder expression = new StringBuilder();
        String paramVal = "";

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
        // operator and parameter id.
        String[] tokens = parameterExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            int i = 0;

            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {
                    expression.append(token).append(" ");
                } else {
                    Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    if (parameter != null) {

                        paramVal = evaluateParameter(parameter, map);
                        if (!(parameter instanceof ConstantParameter || parameter instanceof ReferenceParameter
                                || parameter instanceof NullParameter || parameter instanceof SystemParameter || parameter instanceof CompoundParameter)) {
                            paramVal = addNullCheckParams(tokens, i, paramVal);
                        }
                        BaseLoggers.flowLogger
                                .info("RulesAuditLogServiceImpl CLass :: Method buildParameterExpression:: paramVal = "
                                        + paramVal);
                        expression.append(paramVal).append(" ");
                    }
                }

                i++;
            }
        }
        if (expression.length() > 0) {
            return expression.toString();
        }
        return null;

    }

    @Override
    public Map<String,String> checkParanthesisForErrorMessage(String message) {
        Map<String,String> resultMap = new HashMap<>();

        StringBuilder validationError = new StringBuilder();
        if (message != null && !message.isEmpty()) {
            Deque<Tuple_2> bracesStack = new ArrayDeque<>();
            boolean singleBalancedBracesFound = false;
            for (int i = 0; i < message.length(); i++) {
                String ch = Character.toString(message.charAt(i));
                if (ch.equals(RuleConstants.LEFT_CURLY_BRACES)) {
                    if (!bracesStack.isEmpty()
                            && bracesStack.pop().get_2().equals(RuleConstants.LEFT_CURLY_BRACES)) {
                        appendValidationMessages(validationError, "Invalid " + RuleConstants.LEFT_CURLY_BRACES + "Found at " + i);
                    } else {
                        bracesStack.push(new Tuple_2(String.valueOf(i), ch));
                    }
                } else if (ch.equals(RuleConstants.RIGHT_CURLY_BRACES)) {
                    if (bracesStack.isEmpty()) {
                        appendValidationMessages(validationError, "Invalid " + RuleConstants.RIGHT_CURLY_BRACES + "Found at " + i);
                    } else {
                        Tuple_2 currentTuple = bracesStack.pop();
                        if (currentTuple.get_2().equals(RuleConstants.RIGHT_CURLY_BRACES)) {
                            appendValidationMessages(validationError, "Invalid " + RuleConstants.RIGHT_CURLY_BRACES + "Found at " + i);
                        } else if (currentTuple.get_2().equals(RuleConstants.LEFT_CURLY_BRACES)) {
                            singleBalancedBracesFound = true;
                        }
                    }
                }
            }
            while(!bracesStack.isEmpty()){
                Tuple_2 remaingTuple = bracesStack.pop();
                appendValidationMessages(validationError,"Invalid "+remaingTuple.get_2()+" at position :"+remaingTuple.get_1());
            }
            if(validationError.length()==0) {
                String[] whereClauses = StringUtils.substringsBetween(message, RuleConstants.LEFT_CURLY_BRACES, RuleConstants.RIGHT_CURLY_BRACES);
                Set<String> uniqueWhere = new HashSet<>();
                for (int i = 0; i < whereClauses.length; i++) {
                    String whereClauseKey = whereClauses[i];
                    if (!uniqueWhere.add(whereClauseKey)) {
                        appendValidationMessages(validationError, "Duplicate placeholder : " + whereClauseKey);
                    }
                }
            }
            if(validationError.length()==0)
                verifyParametersByCode(validationError,message);
        }


        if(validationError.length()!=0){
            resultMap.put(RuleConstants.SQL_PARAM_RESULT_NOT_FOUND,validationError.toString());
        }
        return resultMap;
    }



    private void appendValidationMessages(StringBuilder validationError,String message) {
        if(StringUtils.isNotEmpty(message)){
            if(validationError.length() != 0){
                validationError.append("\n");
            }
            validationError.append(message);
        }
    }

    private void verifyParametersByCode(StringBuilder validationError, String errorMessage){
        StringJoiner paramNameMessage = new StringJoiner(",");
        List<String> paramNames =generateParamNamesList(errorMessage);
        if(paramNames.size()>5){
            appendValidationMessages(validationError, "More than 5 parameters can not be used in rule messages");
        }else {
            paramNames.forEach(pn -> {
                if (pn != null && !pn.isEmpty()) {
                    Parameter param = getApprovedParameterByName(pn);
                    if (param == null) {
                        paramNameMessage.add(pn);
                    }
                }
            });
        }
        if(paramNameMessage.length()!=0){
            appendValidationMessages(validationError, "No valid parameter found for : "+paramNameMessage.toString());
        }
    }

    private List<String> generateParamNamesList(String errorMessage){
        StringTokenizer st = new StringTokenizer(errorMessage,"{}",true);
        List<String> paramNames = new ArrayList<>();
        boolean isDelim = false;
        StringBuilder paramName = new StringBuilder();
        while(st.hasMoreTokens()){
            String token = st.nextToken();
            if(token!=null && !token.isEmpty()){
                if(token.equalsIgnoreCase("{")){
                    isDelim=true;
                }else if(token.equalsIgnoreCase("}")){
                    isDelim=false;
                    paramNames.add(paramName.toString());
                    paramName= new StringBuilder();
                }
                if(!token.equalsIgnoreCase("{") && isDelim){
                    paramName.append(token);
                }
            }
        }
        return paramNames;
    }
    @Override
    public List<ObjectGraphTypes> getObjectGraphTypesByIdList(long[] ids) {

        List<ObjectGraphTypes> objectGraphTypeList = new ArrayList<ObjectGraphTypes>();

        for (long id : ids) {

            ObjectGraphTypes objectGraphType = entityDao.find(ObjectGraphTypes.class, id);
            if (objectGraphType != null) {
                objectGraphTypeList.add(objectGraphType);
            }
        }
        if(objectGraphTypeList.isEmpty()){
            return null;
        }
        return objectGraphTypeList;
    }
    @Override
    public List<Rule> getRulesForModuleNameForTaskAssigmentMaster(String productId, String moduleName) {
        NamedQueryExecutor<Rule> parameterCriteria = new NamedQueryExecutor<Rule>(
                "Rule.getApprovedRulesforSpecificModuleName").addParameter("productId", productId).addParameter("moduleName", moduleName)
        		.addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(parameterCriteria);        
    }

    public List<Object> getAllRuleExpressions(){

        NamedQueryExecutor<Object> parameterCriteria = new NamedQueryExecutor<>(
                "Rule.allRuleExpressions").addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(parameterCriteria);

    }

    public List<SQLRule> getAllSQLRules(){
        NamedQueryExecutor<SQLRule> parameterCriteria = new NamedQueryExecutor<SQLRule>(
                "Rule.allSQLRules").addParameter("statusList", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        return entityDao.executeQuery(parameterCriteria);
    }

    
    public List<ObjectGraphTypes>findObjectGraphOnSourceModule(String source, Long moduleName,String input){
		
		 NeutrinoValidator.notNull(source);
    	 NeutrinoValidator.notNull(moduleName);
         NamedQueryExecutor<ObjectGraphTypes> objectGraphIdLis = new NamedQueryExecutor<ObjectGraphTypes>("Rules.ObjectGraphMapping.objectGraph")
                 .addParameter(SOURCE_ID, source).addParameter(MODULE_ID,moduleName).addParameter(APPROVAL_STATUS, ApprovalStatus.APPROVED_RECORD_STATUS_LIST)
                 .addParameter("input", "%"+input+"%");
         List<ObjectGraphTypes> objectGraphIdList = entityDao.executeQuery(objectGraphIdLis);
         return objectGraphIdList;
    }
    
   public List<ObjectGraphClassMapping>getCheckSourceProdObject(String source,Long mod, Long objectGraph,Long id){
	   NamedQueryExecutor<ObjectGraphClassMapping> sourceModObject = new NamedQueryExecutor<ObjectGraphClassMapping>("Rules.ObjectGraphMapping.SouProObj")
               .addParameter(SOURCE_ID, source).addParameter("mod",mod).addParameter("objectGraph",objectGraph)
               .addParameter(APPROVAL_STATUS, ApprovalStatus.HISTORY_RECORD_STATUS_LIST).addParameter("id",id);
	   List<ObjectGraphClassMapping> sourceModObjectList = entityDao.executeQuery(sourceModObject);
	   return sourceModObjectList;
	   
   }

    public List<ObjectGraphClassMapping>getCheckSourceProdObject(String source,Long mod, Long objectGraph){
        NamedQueryExecutor<ObjectGraphClassMapping> sourceModObject = new NamedQueryExecutor<ObjectGraphClassMapping>("Rules.ObjectGraphMapping.SouProdObj")
                .addParameter(SOURCE_ID, source).addParameter("mod",mod).addParameter("objectGraph",objectGraph)
                .addParameter(APPROVAL_STATUS, ApprovalStatus.HISTORY_RECORD_STATUS_LIST);
        List<ObjectGraphClassMapping> sourceModObjectList = entityDao.executeQuery(sourceModObject);
        return sourceModObjectList;

    }
}
    
