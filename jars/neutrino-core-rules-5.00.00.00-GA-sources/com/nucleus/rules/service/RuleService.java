package com.nucleus.rules.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.vo.RuleAutoCompleteSearchVO;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.Entity;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.*;
import com.nucleus.rules.taskAssignmentMaster.ObjectGraphClassMapping;
import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited RuleService interface, defines the
 *         contract to save, update and delete Rule/Condition/Parameter
 *         entities. It also provides the contract to evaluate the rule.
 * 
 */

public interface RuleService extends BaseService {

    /**
     * Save the rule entity.
     * 
     * @param rule
     */
    public void saveRule(Rule rule);

    /**
     * Update the rule entity.
     * 
     * @param rule
     */
    public void updateRule(Rule rule);

    /**
     * Delete the rule entity
     * 
     * @param rule
     */
    public void deleteRule(Rule rule);

    /**
     * Save the condition entity.
     * 
     * @param condition
     */
    public void saveCondition(Condition condition);

    /**
     * Update the condition entity.
     * 
     * @param condition
     */
    public void updateCondition(Condition condition);

    /**
     * Delete the condition entity.
     * 
     * @param condition
     */
    public void deleteCondition(Condition condition);

    /**
     * Save the Parameter entity.
     * 
     * @param parameter
     */
    public void saveParameter(Parameter parameter);

    /**
     * Update the Parameter entity.
     * 
     * @param parameter
     */
    public void updateParameter(Parameter parameter);

    /**
     * Delete the Parameter entity.
     * 
     * @param parameter
     */
    public void deleteParameter(Parameter parameter);

    /**
     * 
     * Get the Rule entity by its name.
     * @param name
     * @return
     */
    public Rule getRuleByName(String name);

    /**
     * Get the Condition entity by its name.
     * 
     * @param name
     * @return
     */
    public Condition getConditionByName(String name, String productId);

    /**
     * Get the Parameter entity by its name.
     * 
     * @param name
     * @return
     */
    public Parameter getParameterByName(String name, String productId);

    /**
     * Get the Rule entity by long identifier.
     * 
     * @param ruleId
     * @return
     */
    public Rule getRule(Long ruleId);

    /**
     * Get the Condition entity by long identifier.
     * 
     * @param conditionId
     * @return
     */
    public Condition getCondition(Long conditionId);

    /**
     * Get the Parameter entity by long identifier.
     * 
     * @param parameterId
     * @return
     */
    public Parameter getParameter(Long parameterId);

    /**
     * 
     * Get Parameter based on id
     * @param id
     * @return
     */
    public ParameterType getParameterTypeById(Long id);

    /**
     * Get the Approved Rule entity by its name.
     * 
     * @param name
     * @return
     */
    public Rule getApprovedRuleByName(String name);

    /**
     * Get the Approved Condition entity by its name.
     * 
     * @param name
     * @return
     */
    public Condition getApprovedConditionByName(String name);

    /**
     * Get the Approved Parameter entity by its name.
     * 
     * @param name
     * @return
     */
    public Parameter getApprovedParameterByName(String name);

    /**
     * Search a value in a column for specified entity
     * 
     * @param entityClass
     * @param field
     * @param value
     * @return
     */
    public <T extends Entity> List<T> searchOnFieldValue(Class<T> entityClass, String field, String value);

    /**
     * Find ObjectGraphTypes for Id
     * 
     * @param objectGraphTypesId
     * @return
     */
    public ObjectGraphTypes getObjectGraphTypes(Long objectGraphTypesId);

    /**
     * Find all ObjectGraphTypes
     * 
     * @return
     */
    public List<ObjectGraphTypes> retrieveObjectGraphTypes();

    /**
     * Find ObjectGraphTypes for ObjectGraph
     * 
     * @param objectGraph
     * @return
     */
    public List<ObjectGraphTypes> findObjectGraphTypesByObjectGraph(String objectGraph);

    /**
     * Get the Source Product By Id
     * 
     * @param sourceProductId
     * @return
     */
    public SourceProduct getSourceProductById(Long sourceProductId);

    /**
     * 
     * Load the entity class extending Generic Parameter
     * 
     * @param entityClass
     * @return
     */
    public <T extends GenericParameter> List<T> retrieveTypes(Class<T> entityClass);

    /**
     * 
     * Load the entity class extending Base Entity
     * 
     * @param entityClass
     * @return
     */
    public <T extends BaseEntity> List<T> loadEntity(Class<T> entityClass);

    /**
     * Returns approved Numeric Parameters
     * 
     * @return
     */
    public List<Parameter> getApprovedNumericParameter();

    /**
     * 
     * Returns the list of approved conditions based on product selected
     * 
     * @param name
     * @param productId
     * @return
     */
    public List<Condition> getApprovedConditionsbySourceProduct(String name, String productId);

    /**
     * 
     * Returns the list of approved Parameters based on product selected
     * 
     * @param name
     * @param productId
     * @return
     */
    public List<Parameter> getApprovedParametersbySourceProduct(String name, String productId);

    /**
     * 
     * Returns the list of approved object graph parameters based on product
     * selected
     * 
     * @param productId
     * @return
     */
    public List<ObjectGraphTypes> getApprovedObjectGraphBySourceProduct(String productId);

    /**
     * 
     * Method to fetch Approved Parameters based on Below Conditions
     * @param name
     * @param productId
     * @param dataType
     * @return
     */

    public List<Parameter> getApprovedParametersbyDataType(String name, String productId, int dataType);

    /**
     * 
     * populate fields from classname
     * @param className
     * @return
     */
    public String populateFields(String className);

    /**
     * Get All Approved and uncompiled rules
     */
    public List<Rule> getAllApprovedRules();

    /**
     * 
     * Get list of approved rules based on product id, name
     * whose criteria flag is 0
     * @param name
     * @param productId
     * @return
     */
    public List<Rule> getApprovedRulesBySourceProduct(String name, String productId);

    /**
     * 
     * Get list of approved rules based on product id, name
     * whose criteria flag is false or true
     * @param name
     * @param productId
     * @return
     */
    public List<Rule> getApprovedCriteriaRulesBySourceProduct(String name, String productId);

    /**
     * 
     * Returns Description of the ObjectGraphType 
     * @param objectGraph
     * @return
     */
    public List<ObjectGraphTypes> getApprovedObjectGraphbyObjectGraphDescription(String objectGraph);

    /**
     * 
     * Returns Id of Null System Parameter
     * @return
     */

    public NullParameter getNullParameter();

    /**
     * 
     * Method to save Rule Group
     * @param ruleGroup
     */
    public void saveRuleGroup(RuleGroup ruleGroup);

    /**
     * 
     * Check for duplicates while saving records
     * @param entityClass
     * @param propertyValueMap
     * @param id - To prevent checking of duplicate columns against entity with this id.
     * @return
     */
    <T extends BaseMasterEntity> Boolean hasEntity(Class<T> entityClass, Map<String, Object> propertyValueMap, Long id);

    /**
     * 
     * Entity List based on the searchedValue on columns (fields)
     * @param entityClass
     * @param fields
     * @return
     */
    public <T extends Entity> List searchEntityData(Class<T> entityClass, String[] fields);

    /**
     * 
     *  Returns the entity by Id.
     * @param geoEntityclass
     * @param id
     * @return
     */

    public <T extends BaseEntity> T getEntityById(Class<T> geoEntityclass, Long id);

    /**
     * 
     * Get Error Message for Rule
     * @param rule
     * @param locale
     * @return
     */
    public String getRuleErrorMessage(Rule rule, Locale locale);

    public String getRuleErrorMessage(Rule rule,Locale locale,Map<Object,Object> contextMap);

    /**
     * 
     * Get Success Message for Rule
     * @param rule
     * @param locale
     * @return
     */
    public String getRuleSuccessMessage(Rule rule, Locale locale);
    public String getRuleSuccessMessage(Rule rule, Locale locale,Map<Object,Object> contextMap);

    /**
     * Method to fetch Paginated Approved Parameters based on Below Conditions 
     * @param name
     * @param productId
     * @param dataType
     * @param startIndex
     * @param pageSize
     * @return
     */
    public List<Parameter> getApprovedParametersbyDataTypePaginated(String name, String productId, List<Integer> dataTypes,
            int startIndex, int pageSize);

    /**
     * Returns the Paginated  list of approved Parameters based on product selected
     * @param name
     * @param productId
     * @param startIndex
     * @param pageSize
     * @return
     */
    public List<Parameter> getApprovedParametersbySourceProductPaginated(String name, String productId, int startIndex,
            int pageSize);

    /**
     * Returns the count of the list of approved Parameters based on product selected
     * @param name
     * @param productId
     * @param dataType
     * @return
     */
    public long getCountOfApprovedParametersbyDataType(String name, String productId, List<Integer> dataTypes);

    /**
     * Returns the count of the list of approved Parameters based on product and datatype selected
     * @param name
     * @param productId
     * @return
     */
    public long getCountOfApprovedParametersbySourceProduct(String name, String productId);

    /**
     * Returns the list of approved conditions based on product selected for Pagination
     * @param name
     * @param productId
     * @param startIndex
     * @param pageSize
     * @return
     */
    public List<Condition> getConditionsSourceProductPage(String name, String productId, int startIndex, int pageSize);

    /**
     * Returns the count of  list of approved conditions based on product selected
     * @param name
     * @param productId
     * @return
     */
    public long getCountOfConditions(String name, String productId);

    /**
     * 
     * Returns all the rules whether
     * criteria flag is false or true
     * @param name
     * @return
     */
    public Rule getAllRulesByName(String name);

    /**
     * 
     * Returns all the Non criteria rules
     * @return
     */
    public List<Rule> getNonCriteriaRules();

    /**
     * 
     * Retrieve list of all Rule Tags
     * @return
     */
    public List<RuleTagType> retrieveRuleTags();

    /**
     * 
     * Save new Rule Tag Type
     * @param ruleTagType
     * @return
     */
    public RuleTagType saveRuleTagType(RuleTagType ruleTagType);

    /**
     * 
     * Get Rule by Rule Name and Rule Tag Name
     * @param name
     * @param tagName
     * @return
     */
    public Rule getRuleByTagName(String name, List<String> tagNameList);

    /**
     * 
     * Get Rule Tag by Tag Name
     * @param tagName
     * @return
     */
    public RuleTagType getRuleTagByTagName(String tagName);

    /**
     * 
     * Fetch Parameter Data Types based on Parameters
     * @param codes
     * @return
     */
    public List<ParameterDataType> loadParamtypeBasedOnParameter(List<String> codes);

    /**
     * This method give all the EventMappings
     * where a Particular rule is used 
     * @param id
     * @return
     */
    public Map<String, List> getRulesUsages(Long id);

    /**
     * 
     * Encrypt Script Parameter Code
     * @param scriptParameter
     * @return
     */

    public ScriptParameter encryptScriptCode(ScriptParameter scriptParameter);

    /**
     * 
     * Decrypt Script Parameter Code
     * @param scriptParameter
     * @return
     */

    public ScriptParameter decryptScriptCode(ScriptParameter scriptParameter);

    /**
     * 
     * Encrypt Script Rule Code
     * @param scriptRule
     * @return
     */

    public ScriptRule encryptScriptCode(ScriptRule scriptRule);

    /**
     * 
     * Decrypt Script Rule Code
     * @param scriptRule
     * @return
     */
    public ScriptRule decryptScriptCode(ScriptRule scriptRule);

    /**
     * 
     * Returns the list of approved rules based on product selected for Pagination
     * @param name
     * @param productId
     * @param startIndex
     * @param pageSize
     * @return
     */

    public List<Rule> getRulesbySourceProduct(String name, String productId, int startIndex, int pageSize);

    /**
     * 
     * Returns the count of the list of approved Rules based on product selected
     * @param name
     * @param productId
     * @return
     */

    public long getCountOfApprovedRulesbySourceProduct(String name, String productId);

    /**
     * 
     * Get list of approved rules based on product id, name
     * whose criteria flag is false or true (i.e. all rules)
     * @param name
     * @param productId
     * @param startIndex
     * @param pageSize
     * @return
     */

    public List<Rule> getCriteriaRules(String name, String productId, int startIndex, int pageSize);

    /**
     * 
     * Returns the count of the list of approved rules based on product id, name
     * whose criteria flag is false or true  (i.e. all rules)
     * @param name
     * @param productId
     * @return
     */

    public long getCountOfCriteriaRules(String name, String productId);

    /**
     * 
     * Returns Approved Parameter's name, Description based on conditions
     * @param name
     * @param productId
     * @param dataTypes
     * @param startIndex
     * @param pageSize
     * @return
     */

    public Map<String, String> getParameterNameDesc(String name, String productId, List<Integer> dataTypes, int startIndex,
            int pageSize);

    /**
     * 
     * return parameter Id from the parameter Name
     * @param name
     * @return
     */
    public Long getParameterIdByName(String name);

    /**
     * Returns Approved Condition's name, Description based on conditions
     * @param name
     * @param productId
     * @param dataTypes
     * @param startIndex
     * @param pageSize
     * @return
     */

    public Map<String, String> getConditionNameDesc(String name, String productId, int startIndex, int pageSize);

    /**
     * 
     * return Condition Id from Condition Name
     * @param name
     * @return
     */
    public Long getConditionIdByName(String name);

    /**
     * 
     * returns Approved Object Graph Type based on SourceProduct And Module
     * @param productId
     * @param moduleName
     * @return
     */

    public List<ObjectGraphTypes> getOgnlBySourceProductAndModuleName(String productId, String moduleName);

    /**
     * 
     * return the ModuleName Object from its name
     * @param moduleName
     * @return
     */
    public ModuleName getModuleNameFromName(String moduleName);

    /**
     * 
     * Get list of approved rules and active rules
     * @return
     */

    public List<Rule> getApprovedAndActiveRule();

    /**
     * 
     * executes named query
     * @param queryName
     * @return
     */

    public List executeQuery(String queryName);

    /**
     * 
     * populate fields from classname
     * @param className
     * @return
     */

    public EntityType getEntityTypeData(String className);

    /**
    * 
    * get All Mvel based Script Parameters
    * @return
    */
    public List<ScriptParameter> getAllMvelBasedScriptParameters();

    /**
     * 
     * get ObjectGraphGraph with Display Name
     * @param displayName
     * @return
     */
    public ObjectGraphTypes getObjectGraphTypesWithDisplayName(String displayName);

    /**
      * return 
      * @return all the object graph of collections on the basic of datatype
      */
    public List<ObjectGraphParameter> getCollectionTypGraphByDataType(String dataType);

    /**
     * 
     * @return return all the attribute of a collection on the basic of parent ognl type matching
     */
    public List<ObjectGraphParameter> getAttributesOfCollectionTypOgnl(String parentOgnl);

    /**
     * getApprovedParameterByDataAndParamTypePaginated
     * @param name
     * @param productId
     * @param dataTypes
     * @param paramTypes
     * @param startIndex
     * @param pageSize
     * @return
     */
    public Map<String, String> getParameterByDataParamType(String name, String productId, List<Integer> dataTypes,
            List<Integer> paramTypes, int startIndex, int pageSize);

    /**
     * Returns Approved Rule's name, Description for paginated
     * @param name
     * @param productId
     * @param dataTypes
     * @param startIndex
     * @param pageSize
     * @return
     */

    public Map<String, String> getRuleNameAndDesc(String name, String productId, int startIndex, int pageSize);

    /**
     * 
     * return Rule Id from Rule Name
     * @param name
     * @return
     */
    public Long getRuleIdByName(String name);

    /**
     * getApprovedCollectionTypeParameterbyDataTypePaginated
     * @param name
     * @param productId
     * @param dataTypes
     * @param startIndex
     * @param pageSize
     * @return return collection  type object graph parameter
     */
    public Map<String, String> getCollectionTypeParameter(String name, String ObjectGraph, String productId,
            List<Integer> dataTypes, int startIndex, int pageSize);

    /**
     * getCountOfParameterByDataAndParamTypePaginated
     * @param productId
     * @param dataTypes
     * @param paramTypes
     * @return return count of parameter on the basic of product id, data type and param type
     */
    public Long getCountOfParamete(String name, String productId, List<Integer> dataTypes, List<Integer> paramTypes);

    /**
     * getCountOfCollectionTypeParameterByDataAndParamType
     * @param currentTargetOgnl
     * @param productId
     * @param dataTypes
     * @return return the count of collection type Objectgraph parameter on the basic of datatype, product id and object graph like valuy
     */
    public Long getCountOfCollectionTypParam(String name, String currentTargetOgnl, String productId, List<Integer> dataTypes);

    /**     
     * Loads list  Object Graph Types from array of ids
     * @param ids
     * @return
     */
    public List<ObjectGraphTypes> getObjectGraphTypesByIds(long[] ids);

    /**
     * 
     * get approved numeric parameters
     * @return
     */
    public List<Map<String, ?>> getApprovedNumericParameterForBinder();

    /**
     * loadParamtypeBasedOnParameterForBinder
     * Get loadParamtypeBasedOnParameterForBinder
     * @param codes
     * @return
     */
    public List<Map<String, ?>> loadParamtypeBasedOnParam(List<String> codes);

    /**
     * 
     * get ognl based on module and source product
     * @param productId
     * @param moduleName
     * @return
     */
    public List<ObjectGraphTypes> getOgnlBySourceProductAndModule(String productId, Long moduleName);

    /**
     * 
     * get parameters based on below arguments
     * @param name
     * @param productId
     * @param dataTypes
     * @param startIndex
     * @param pageSize
     * @param moduleId
     * @return
     */
    public List<Parameter> getApprovedParametersbyDataTypePaginated(String name, String productId, List<Integer> dataTypes,
            int startIndex, int pageSize, Long moduleId);

    /**
     * 
     * get count of parameters based on below arguments
     * @param name
     * @param productId
     * @param dataTypes
     * @param moduleId
     * @return
     */
    public long getCountOfApprovedParametersbyDataType(String name, String productId, List<Integer> dataTypes, Long moduleId);

    /**
     * 
     * Load module based on moduleId
     * @param moduleId
     * @return
     */
    public ModuleName getModuleById(Long moduleId);

    /**
     * 
     * Load collection type Ognl's based on module
     * @param dataType
     * @param moduleId
     * @return
     */
    public List<ObjectGraphParameter> getCollectionTypGraphByDataType(String dataType, Long moduleId);

    /**
     * 
     * Load conditions based on below arguments
     * @param name
     * @param productId
     * @param startIndex
     * @param pageSize
     * @param moduleId
     * @return
     */
    public List<Condition> getConditionsSourceProductPage(String name, String productId, int startIndex, int pageSize,
            Long moduleId);

    /**
     * 
     * Load condition size based on below arguments
     * @param name
     * @param productId
     * @param moduleId
     * @return
     */
    public long getCountOfConditions(String name, String productId, Long moduleId);


    /**
     * 
     * Load all parameters based on specific module only
     * @param name
     * @param productId
     * @param startIndex
     * @param pageSize
     * @param moduleId
     * @return
     */
    public List<Parameter> getParametersBySpecificModule(String name, String productId, Long moduleId);

    /**
     * 
     * Load all condition based on specific module only
     * @param name
     * @param productId
     * @param moduleId
     * @return
     */
    public List<Condition> getConditionBySpecificModule(String name, String productId, Long moduleId);

    /**
     * 
     * Load all rules based on specific module only
     * @param name
     * @param productId
     * @param moduleId
     * @return
     */
    public List<Rule> getRuleBySpecificModule(String name, String productId, Long moduleId);

    /**
     * 
     * method to search for specific parameter id to 
     * find its usage in compouted parameters.
     * @param paramId
     * @param productId
     * @return
     */

    public List<CompoundParameter> getComputedParamsUsingThisParam(Long paramId, String productId);

    /**
     * 
     * method to search for specific parameter id to 
     * find its usage in conditions.
     * @param paramId
     * @param productId
     * @return
     */

    public List<Condition> getConditionsUsingThisParam(Long paramId, String productId);

    /**
     * 
     * method to search for specific condition id to 
     * find its usage in rule parameters.
     * @param paramId
     * @param productId
     * @return
     */
    public List<Rule> getRulesUsingThisCondition(Long condId, String productId);

    /**
     * 
     * This will return map of Key - Rule Id
     *                         Value - Rule Name
     * This will return active and approved Rules
     * @return
     */
    public Map<Long, String> getApprovedRuleIdAndName();

    /**
     * Method to fetch  Approved Parameters based on Below Conditions 
     * @param productId
     * @param dataTypes
     * @param moduleId
     * @return
     */
    public List<Map<String, Object>> getApprovedParametersbyDataTypes(String productId, List<Integer> dataTypes,
            Long moduleId);

    /**
     * Method to fetch  Approved Parameters based on Below Conditions 
     * @param productId
     * @param dataTypes
     * @return
     */
    public List<Map<String, Object>> getApprovedParametersbyDataTypes(String productId, List<Integer> dataTypes);

    /**
     *Get Map Of All Approved rule with id 
     *name description
     */
    public List<Map<String, Object>> getAllApprovedRuleMap();

    /**
     *Get Map Of All Approved Onject Graph Parameter with id
     *name description
     */
    public List<Map<String, Object>> getAllApprovedObjectGraphParameter();

    /**
     *Method to get Description of OGNL by OGNL name
     */
    public String getObjectGraphTypesDescWithDisplayName(String displayName);

    /**
     *Method to get Description of Parameter by Parameter name
     */
    public String getParameterDescWithDisplayName(String displayName);

    public String findNamethroughObjectGraph(String objectGraph);

    /**
     * Get the Approved Parameter Ognl by its Parameter name.
     * 
     * @param name
     * @return
     */
    public String getApprovedParameterOgnlByName(String parameterName);

    /**
     * Gets the all approved parameter for score card.
     *
     * @return the all approved parameter for score card
     */
    public List<Map<String, Object>> getAllRequiredParameterForScoreCard(List<Integer> paramList);

    /**
     * This method will return rule error messages for rules in validation task of event definition whose result is false
     * 
     * @param eventCode
     * @param contextMap
     * @return
     */

    public List<String> getValidationRuleErrorMessages(String eventCode, Map<Object, Object> contextMap);

    /**
     * 
     * Returns the list of object graphs based on rule matrix type selected
     * 
     * @param ruleMatrixTypeId
     * @return
     */
    public List<ObjectGraphTypes> getObjectGraphByRuleMatrixType(Long ruleMatrixTypeId);
    
    
    /**
     * Searches for the Rules/RuleAction/RuleSet/EntityType for autcomplete
     *
     * @return the list of search result
     */
    public List searchRuleDataForAutoComplete(RuleAutoCompleteSearchVO autoCompleteSearchVO);
    
    public List<Rule> getApprovedRulesBySourceProduct(String sourceProduct);

    public List<ObjectGraphTypes> getObjectGraphsbyDataTypes(List<String> dataTypes);

	List<ObjectGraphTypes> getOgnlForModuleNameForTaskAssigmentMaster(String productId, String moduleName);

    List<Map<String, Object>> getOgnlForModuleNameForTaskAssigmentMasterMapping(String productId, String moduleName);
    /**
     * 
     * Encrypt Script Rule Code
     * @param scriptRule
     * @return
     */

    public SQLRule encryptSQLRule(SQLRule scriptRule);

    /**
     * 
     * Decrypt Script Rule Code
     * @param scriptRule
     * @return
     */
    public SQLRule decryptSQLRule(SQLRule scriptRule);
    
    
    public String evaluateParameter(Parameter parameter, Map<Object, Object> map) ;
    
    public String buildParameterExpression(String parameterExpression, Map<Object, Object> map);

    public Map<String,String> checkParanthesisForErrorMessage(String message);
    List<Rule> getRulesForModuleNameForTaskAssigmentMaster(String productId, String moduleName);
	public List<ObjectGraphTypes> getObjectGraphTypesByIdList(long[] ids);

    List<Object> getAllRuleExpressions();

    List<SQLRule> getAllSQLRules();

    public List<ObjectGraphTypes> findObjectGraphOnSourceModule(String source, Long moduleName, String input);

	public List<ObjectGraphClassMapping> getCheckSourceProdObject(String source, Long mod,
			Long objectGraph, Long id);
    public List<ObjectGraphClassMapping> getCheckSourceProdObject(String source, Long mod,
                                                                  Long objectGraph);

}
