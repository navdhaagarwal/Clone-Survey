
package com.nucleus.rules.service;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.initialization.*;
import com.nucleus.rules.model.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.mvel2.MVEL;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.PlatformTransactionManager;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.dao.query.RuleQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.UnapprovedEntityData;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.rules.exception.RuleException;
import com.nucleus.rules.utils.DataContext;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Implementation class for Rules Audit Logging
 */
@Named(value = "rulesAuditLogService")
public class RulesAuditLogServiceImpl extends BaseRuleServiceImpl implements RulesAuditLogService, ApplicationContextAware {

    @Inject
    @Named("baseMasterService")
    private BaseMasterService          baseMasterService;

    /**
     * 
     * code is commented as the same will be refactored later for threading
     */

    @Inject
    @Named("ruleService")
    RuleService                        ruleService;

    @Inject
    @Named("expressionBuilder")
    ExpressionBuilder                  expressionBuilder;
    
    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    private NeutrinoEventPublisher neutrinoEventPublisher;

    private PlatformTransactionManager transactionManager;

    ApplicationContext                 applicationContext;
    
    @Inject
    @Named("postCommitWorkerForRulesAuditLogData")
    private PostCommitWorkerForRulesAuditLogData postCommitWorkerForRulesAuditLogData;
    
    @Inject
    @Named("sQLRuleExecutor")
    private SQLRuleExecutor sqlRuleExecutor;
    
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService; 

    public RulesAuditLogServiceImpl() {
    	// Do nothing because 
    }
    @Override
    public void ruleInvocationMappingAudit(String uuid, Map<Object, Map<Object, Object>> ruleInvocationMappingResults,
            Map<Object, Object> objectMap, String invocationPoint,boolean auditingEnabled,boolean purgingRequired) {

            String productName = ProductInformationLoader.getProductName();
            ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),productName + ".enableRuleAuditing.flag");
            if(configVo!=null)
                auditingEnabled = configVo.getPropertyValue().equalsIgnoreCase("true")? true : auditingEnabled;

	    	if(!auditingEnabled){
	    		return;
	    	}
	    	
    		Map<Object, Object> ruleResults = ruleInvocationMappingResults.get(RuleConstants.RULE_KEY);
            Map<Object, Object> ruleSetResults = ruleInvocationMappingResults.get(RuleConstants.RULESET_KEY);
            Map<Object, Object> ruleGroupResults = ruleInvocationMappingResults.get(RuleConstants.RULEGROUP_KEY);
            UserInfo user = getCurrentUser();

            final List<RulesAuditLog> ruleAuditLogList = new ArrayList<>();

            // Setting for rule results
            prepareRuleAuditLogListForRuleResults(ruleResults, uuid, invocationPoint, user, objectMap, ruleAuditLogList,purgingRequired);

            // Setting for rule Set results
            prepareRuleAuditLogListForRuleSetResults(ruleSetResults, uuid, invocationPoint, user, objectMap, ruleAuditLogList,purgingRequired);

            // Setting for rule Group results
            prepareRuleAuditLogListForRuleGroupResults(ruleGroupResults, uuid, invocationPoint, user, objectMap, ruleAuditLogList,purgingRequired);

            // persist the RuleAuditLog list in different thread -- Start

            saveRuleAuditLogDataAsynchronously(ruleAuditLogList);

            // persist the RuleAuditLog list in different thread -- End
    	
    }

    /**
     * @deprecated (Since GA2.5, To Support configurable auditing)
     */
    @Deprecated
    @Override
    @MonitoredWithSpring(name = "RALSI_RULE_INVOCATION_MAP_AUDIT")
    public void ruleInvocationMappingAudit(String uuid, Map<Object, Map<Object, Object>> ruleInvocationMappingResults,
            Map<Object, Object> objectMap, String invocationPoint) {    	
    	ruleInvocationMappingAudit(uuid, ruleInvocationMappingResults, objectMap, invocationPoint, true, false);
    }
    
    
    
    
    /**
     * @param ruleGroupResults
     * @param uuid
     * @param invocationPoint
     * @param user
     * @param objectMap
     * @param ruleAuditLogList
     * @param purgingRequired
     */
    private void prepareRuleAuditLogListForRuleGroupResults(Map<Object, Object> ruleGroupResults, String uuid,
			String invocationPoint, UserInfo user, Map<Object, Object> objectMap,
			List<RulesAuditLog> ruleAuditLogList,boolean purgingRequired) {
    	
    	if (null != ruleGroupResults && ruleGroupResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : ruleGroupResults.entrySet()) {

                char[] result = (char[]) entry.getValue();

                RuleGroup ruleGroup = (RuleGroup) entry.getKey();
                List<Rule> ruleList = ruleGroup.getRules();

                for (int i = 0 ; i < result.length ; i++) {
                    RulesAuditLog rulesAuditLog = new RulesAuditLog();

                    rulesAuditLog.setRuleInvocationUUID(uuid);
                    rulesAuditLog.setRuleInvocationPoint(invocationPoint);
                    rulesAuditLog.setPurgingRequired(purgingRequired);
                    setAssociatedUserForAuditing(rulesAuditLog, user);
                    rulesAuditLog.setRuleId(ruleList.get(i).getId());
                    rulesAuditLog.setRuleResult(String.valueOf(getRuleResult(result[i])));

                   

                    Rule rule = ruleList.get(i);
                    rule = HibernateUtils.initializeAndUnproxy(rule);
                    if(rule instanceof SQLRule){
                    	List<Parameter> parameters = new ArrayList<>();
    					sqlRuleExecutor.generateParametesList((SQLRule) rule, parameters);
    					rulesAuditLog.setRulesParametersValues(setRuleAuditParameters(objectMap, parameters));
    				}
    				else if (!(rule instanceof ScriptRule)) {
    					 List<Parameter> parameters = new ArrayList<>();
                        getParametersList(rule.getRuleExpression(), parameters);
                        rulesAuditLog.setRulesParametersValues(setRuleAuditParameters(objectMap, parameters));
                    }
                    udpateRuleOwner(objectMap, rulesAuditLog);
                    setSuccessErrorMessagesForAudit(rulesAuditLog,rule,objectMap);
                    rulesAuditLog.setElapsedTime((Long) objectMap.get(rule.getName() + "_" + rule.getId() + "_"
                            + RuleConstants.RULE_ELAPSED_TIME));
                    ruleAuditLogList.add(rulesAuditLog);
                }
            }
        }
    }
        private void udpateRuleOwner(Map<Object, Object> objectMap, RulesAuditLog rulesAuditLog) {
		ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(
		        SystemEntity.getSystemEntityId(), "config.system.ruleaudit.owneruri");
		if (configVo != null) {
			String ownersName = configVo.getPropertyValue();
			if (StringUtils.isNoneEmpty(ownersName)) {
				List<String> owners = new ArrayList<>();
				if(!ownersName.contains(",")){
					owners.add(ownersName);
				}else{
					owners = Arrays.asList(ownersName.split(","));
				}
				for (String o : owners) {
					Object contextObjectOwner = objectMap.get(o);
					if (contextObjectOwner != null) {
						rulesAuditLog.setInvokerURI(contextObjectOwner.toString());
						if(contextObjectOwner.toString().indexOf(":") > -1){
							rulesAuditLog.setInvokerId(contextObjectOwner.toString().substring(contextObjectOwner.toString().lastIndexOf(":")+1));
						}
						break;
					}
				}
			}
		}
	}
    
    /**
     * @param rulesAuditLog
     * @param user
     */
    private void setAssociatedUserForAuditing(RulesAuditLog rulesAuditLog,UserInfo user){
    	if (user == null) {
            rulesAuditLog.setAssociatedUser(baseMasterService.getMasterEntityById(User.class,
                    Long.parseLong(RuleConstants.SYSTEM_USER.split(":")[1])));

        } else {
            rulesAuditLog.setAssociatedUser((user.getUserReference()));
        }
    }
    
    
    /**
     * @param ruleSetResults
     * @param uuid
     * @param invocationPoint
     * @param user
     * @param objectMap
     * @param ruleAuditLogList
     * @param purgingRequired
     */
    private void prepareRuleAuditLogListForRuleSetResults(Map<Object, Object> ruleSetResults, String uuid,
			String invocationPoint, UserInfo user, Map<Object, Object> objectMap,
			List<RulesAuditLog> ruleAuditLogList,boolean purgingRequired) {
    	if (null != ruleSetResults && ruleSetResults.size() > 0) {
            for (Map.Entry<Object, Object> entry : ruleSetResults.entrySet()) {

                char[] result = (char[]) entry.getValue();

                RuleSet ruleSet = (RuleSet) entry.getKey();
                List<Rule> ruleList = ruleSet.getRules();

                for (int i = 0 ; i < result.length ; i++) {
                    RulesAuditLog rulesAuditLog = new RulesAuditLog();

                    rulesAuditLog.setRuleInvocationUUID(uuid);
                    rulesAuditLog.setRuleInvocationPoint(invocationPoint);
                    rulesAuditLog.setPurgingRequired(purgingRequired);
                    setAssociatedUserForAuditing(rulesAuditLog, user);

                    rulesAuditLog.setRuleId(ruleList.get(i).getId());
                    rulesAuditLog.setRuleResult(String.valueOf(getRuleResult(result[i])));

                    

                    Rule rule = ruleList.get(i);
                    rule = HibernateUtils.initializeAndUnproxy(rule);
                    if(rule instanceof SQLRule){
                    	List<Parameter> parameters = new ArrayList<>();
    					sqlRuleExecutor.generateParametesList((SQLRule) rule, parameters);
    					rulesAuditLog.setRulesParametersValues(setRuleAuditParameters(objectMap, parameters));
    				}
    				else if (!(rule instanceof ScriptRule)) {
    					List<Parameter> parameters = new ArrayList<>();
                        getParametersList(rule.getRuleExpression(), parameters);
                        rulesAuditLog.setRulesParametersValues(setRuleAuditParameters(objectMap, parameters));
                    }
                    udpateRuleOwner(objectMap, rulesAuditLog);
                    setSuccessErrorMessagesForAudit(rulesAuditLog,rule,objectMap);
                    rulesAuditLog.setElapsedTime((Long) objectMap.get(rule.getName() + "_" + rule.getId() + "_"
                            + RuleConstants.RULE_ELAPSED_TIME));

                    ruleAuditLogList.add(rulesAuditLog);
                }
            }
        }
    }
    
	
	/**
	 * @param ruleResults
	 * @param uuid
	 * @param invocationPoint
	 * @param user
	 * @param objectMap
	 * @param ruleAuditLogList
	 * @param purgingRequired
	 */
	private void prepareRuleAuditLogListForRuleResults(Map<Object, Object> ruleResults, String uuid,
			String invocationPoint, UserInfo user, Map<Object, Object> objectMap,
			List<RulesAuditLog> ruleAuditLogList,boolean purgingRequired) {
		if (null != ruleResults && ruleResults.size() > 0) {

			for (Map.Entry<Object, Object> entry : ruleResults.entrySet()) {
				RulesAuditLog rulesAuditLog = new RulesAuditLog();
				rulesAuditLog.setRuleInvocationUUID(uuid);
				rulesAuditLog.setRuleInvocationPoint(invocationPoint);
				rulesAuditLog.setPurgingRequired(purgingRequired);
				if (user == null) {
					rulesAuditLog.setAssociatedUser(baseMasterService.getMasterEntityById(User.class,
							Long.parseLong(RuleConstants.SYSTEM_USER.split(":")[1])));

				} else {
					rulesAuditLog.setAssociatedUser((user.getUserReference()));
				}

                Rule rule = (Rule) entry.getKey();
                rule = HibernateUtils.initializeAndUnproxy(rule);
                rulesAuditLog.setRuleId(rule.getId());
                rulesAuditLog.setElapsedTime((Long) objectMap
                        .get(rule.getName() + "_" + rule.getId() + "_" + RuleConstants.RULE_ELAPSED_TIME));
                rulesAuditLog.setRuleResult(String.valueOf(entry.getValue()));
                if(rule instanceof SQLRule){
                    List<Parameter> parameters = new ArrayList<>();
                    sqlRuleExecutor.generateParametesList((SQLRule) rule, parameters);
                    rulesAuditLog.setRulesParametersValues(setRuleAuditParameters(objectMap, parameters));
                }
                else if (!(rule instanceof ScriptRule)) {
                    List<Parameter> parameters = new ArrayList<>();
                    getParametersList(rule.getRuleExpression(), parameters);
                    rulesAuditLog.setRulesParametersValues(setRuleAuditParameters(objectMap, parameters));
                }
                udpateRuleOwner(objectMap, rulesAuditLog);
                setSuccessErrorMessagesForAudit(rulesAuditLog,rule,objectMap);
                ruleAuditLogList.add(rulesAuditLog);
            }
        }
    }
    /**
     
     */

    private void setSuccessErrorMessagesForAudit(RulesAuditLog rulesAuditLog,Rule rule, Map<Object,Object> contextMap){
        String errorMessage = ruleService.getRuleErrorMessage(rule,Locale.getDefault(),contextMap);
        String successMessage = ruleService.getRuleSuccessMessage(rule,Locale.getDefault(),contextMap);
        if(errorMessage!=null && !errorMessage.isEmpty()){
            rulesAuditLog.setErrorMessage(errorMessage);
        }
        if(successMessage!=null && !successMessage.isEmpty()){
            rulesAuditLog.setSuccessMessage(errorMessage);
        }
    }
	
	@Override
	public void saveRuleAuditLogData(final List<RulesAuditLog> ruleAuditLogList){
		if(ruleAuditLogList !=null && !ruleAuditLogList.isEmpty()){
			saveRuleAuditLog(ruleAuditLogList);
		}
	}
	
	private void saveRuleAuditLog(List<RulesAuditLog> ruleAuditLogList){
		for (RulesAuditLog auditLog : ruleAuditLogList) {
			if(auditLog != null){
				entityDao.persist(auditLog);
				saveRuleAuditLogParamValData(auditLog.getRulesParametersValues());
			}			
		}	
	}
	
	private void saveRuleAuditLogParamValData(List<RulesAuditLogParametersValues> rulesParametersValues){
		if(rulesParametersValues != null && !rulesParametersValues.isEmpty()){
			for (RulesAuditLogParametersValues rulesParametersValue : rulesParametersValues) {
				if(rulesParametersValue != null){
					entityDao.persist(rulesParametersValue);
				}				
			}
		}		
	}

    private void saveRuleAuditLogDataAsynchronously(final List<RulesAuditLog> ruleAuditLogList) {

        RuleAUditLogEventClassWorker ruleAUditLogEventClassWorker = new RuleAUditLogEventClassWorker("Rule Audit " +
                "Worker");
        ruleAUditLogEventClassWorker.setName("Rule Audit Task");
        ruleAUditLogEventClassWorker.setRuleAuditLogList(ruleAuditLogList);
        neutrinoEventPublisher.publish(ruleAUditLogEventClassWorker);
    //	TransactionPostCommitWorker.handlePostCommit(postCommitWorkerForRulesAuditLogData, ruleAuditLogList, true);
    }

    /**
     * 
     * Set parameters in the list
     * @param Objectmap
     * @param parameters
     * @return
     */

    public List<RulesAuditLogParametersValues> setRuleAuditParameters(Map<Object, Object> objectmap,
            List<Parameter> parameters) {

        List<RulesAuditLogParametersValues> ruleAuditParamList = new ArrayList<>();
        String ognlTobeEvaluated = "";
        if(objectmap instanceof DataContext){
            DataContext dataContext = (DataContext)objectmap;
            dataContext.setExecutionStarted(true);
        }
        for (int j = 0 ; j < parameters.size() ; j++) {

            Parameter parameter = parameters.get(j);
            Object parameterResult = null;

            RulesAuditLogParametersValues rulesAuditLogParametersValues = new RulesAuditLogParametersValues();
            rulesAuditLogParametersValues.setParameterId(parameter);

            if (parameter instanceof ConstantParameter) {
                rulesAuditLogParametersValues.setParameterValue(((ConstantParameter) parameter).getLiteral());

            } else if (parameter instanceof NullParameter) {
                rulesAuditLogParametersValues.setParameterValue("null");

            } else if (parameter instanceof SystemParameter) {
                rulesAuditLogParametersValues.setParameterValue(String.valueOf(((SystemParameter) parameter)
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
                        rulesAuditLogParametersValues.setParameterValue(list.get(0).toString());
                }

            } else if (parameter instanceof CompoundParameter) {
                String compoundExpression = buildParameterExpression(
                        ((CompoundParameter) parameter).getParameterExpression(), objectmap);
                parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(compoundExpression, objectmap);
                if (null != parameterResult) {
                    rulesAuditLogParametersValues.setParameterValue(parameterResult.toString());
                } else {
                    rulesAuditLogParametersValues.setParameterValue(null);
                }

            } else if (parameter instanceof ReferenceParameter) {
            	
                rulesAuditLogParametersValues.setParameterValue(((ReferenceParameter) parameter).getReferenceEntityId()
                        .getUri());
                

            } else if (parameter instanceof ObjectGraphParameter) {

                if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {

                    ognlTobeEvaluated = "( ?"
                            + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                    .getObjectGraph() + RuleConstants.RULE_TIME_IN_MILLIS) + " )";
                } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {

                    ognlTobeEvaluated = "( ?"
                            + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                    .getObjectGraph() + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE) + " )";
                } else {
                    ognlTobeEvaluated = "( ?"
                            + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                    .getObjectGraph()) + " )";
                }
                
                parameterResult = RuleExpressionMvelEvaluator.evaluateExpression(ognlTobeEvaluated, objectmap);

                if (parameterResult != null) {

                    if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                        rulesAuditLogParametersValues.setParameterValue(((BaseEntity) parameterResult).getUri());

                    } else {
                        rulesAuditLogParametersValues.setParameterValue(parameterResult.toString());
                    }

                } else {
                    rulesAuditLogParametersValues.setParameterValue(null);
                }

            } else if (parameter instanceof ScriptParameter) {
                parameterResult = objectmap.get(RulesConverterUtility.replaceSpace(parameter.getName())
                        + RuleConstants.PARAMETER_NAME_ID + parameter.getId());
                if (null != parameterResult) {
                    rulesAuditLogParametersValues.setParameterValue(parameterResult.toString());
                } else {
                    rulesAuditLogParametersValues.setParameterValue(null);
                }
            }else if (parameter instanceof SQLParameter){
                Map<String,Object> resultMap =  sqlRuleExecutor.getParameterValue((SQLParameter)parameter,objectmap);
                if(resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND)!=null){
                    parameterResult = resultMap.get(RuleConstants.SQL_PARAM_RESULT_FOUND);
                    rulesAuditLogParametersValues.setParameterValue(parameterResult.toString());
                }
            }
            ruleAuditParamList.add(rulesAuditLogParametersValues);
        }

        return ruleAuditParamList;
    }

    /**
     * 
     * get result
     * @param result
     * @return
     */

    private boolean getRuleResult(char result) {
        return result == RuleConstants.RULE_RESULT_PASS;
    }

    /**
     * 
     * Method to break rule expression into conditions
     * @param ruleExpression
     * @param parametersList
     */

    public void getParametersList(String ruleExpression, List<Parameter> parametersList) {

        // splitting expression with space - assuming that expression can have only brackets, and or operator and condition
        // id.
        if(null!=ruleExpression) {
            String[] tokens = ruleExpression.split(" ");
            if (tokens != null && tokens.length > 0) {
                for (String token : tokens) {
                    token = token.trim();
                    // if token is bracket and operator
                    if (!(token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN) || commaDelimitesString(
                            ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1)) {

                        Condition condition = entityDao.find(Condition.class, Long.parseLong(token));
                        if (condition != null) {
                            buildConditionExpr(condition.getConditionExpression(), parametersList);
                        }
                    }
                }
            }

        }
    }

    /**
     * 
     * Method to get parameters from condition expression
     * @param conditionExpression
     * @param parametersList
     */

    private void buildConditionExpr(String conditionExpression, List<Parameter> parametersList) {

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
        // operator and parameter id.
        String[] tokens = conditionExpression.split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (!(token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1 || commaDelimitesString(
                            ExpressionValidationConstants.REL_OPS).indexOf(token) != -1)) {

                    Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    parametersList.add(parameter);

                }
            }
        }

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

    /**
     * 
     * evaluate parameter 
     * @param parameter
     * @param map
     * @return
     */

    private String evaluateParameter(Parameter parameter, Map<Object, Object> map) {
        if (parameter == null) {
            throw new RuleException("Parameter Cannot be null/empty");
        }
        if(map instanceof DataContext){
            DataContext dataContext = (DataContext)map;
            dataContext.setExecutionStarted(true);
        }
        Object parameterValue = null;
        String parameterKey = RulesConverterUtility.replaceSpace(parameter.getName()) + RuleConstants.PARAMETER_NAME_ID
                + parameter.getId();

        if (parameter instanceof ObjectGraphParameter) {
            if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
                parameterKey = "?"
                        + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter).getObjectGraph())
                        + RuleConstants.RULE_TIME_IN_MILLIS;
            } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                parameterKey = "?"
                        + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter).getObjectGraph())
                        + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE;
            } else {
                parameterKey = "?"
                        + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter).getObjectGraph());
            }

        } else if (parameter instanceof ConstantParameter) {
            parameterValue = ((ConstantParameter) parameter).getLiteralValue();
            map.put(parameterKey, parameterValue);

        } else if (parameter instanceof ReferenceParameter) {
            parameterValue = entityDao.get(((ReferenceParameter) parameter).getReferenceEntityId());
            map.put(parameterKey, parameterValue);

        } else if (parameter instanceof QueryParameter) {
            RuleQueryExecutor queryCriteria = new RuleQueryExecutor(((QueryParameter) parameter).getQuery());

            List<QueryParameterAttribute> queryParameterAttributes = ((QueryParameter) parameter)
                    .getQueryParameterAttributes();
            if (queryParameterAttributes != null) {
                for (QueryParameterAttribute queryAttribute : queryParameterAttributes) {
                    queryCriteria.addQueryParameter(queryAttribute.getQueryParameterName(),
                            MVEL.eval(queryAttribute.getObjectGraph(), map));
                }
            }

            List list = entityDao.executeQuery(queryCriteria);
            if (list != null && list.size() > 0) {
                parameterValue = list.get(0);
            }
            map.put(parameterKey, parameterValue);

        } else if (parameter instanceof SystemParameter) {

            if (((SystemParameter) parameter).getSystemParameterType() == SystemParameterType.SYSTEM_PARAMETER_TYPE_CURRENT_USER) {

                parameterValue = entityDao.get(EntityId.fromUri(map.get("user.referenceURI").toString()));

                map.put(parameterKey, parameterValue);

            } else {
                parameterValue = ((SystemParameter) parameter).getSystemParameterValue();
                map.put(parameterKey, parameterValue);
            }

        }

        else if (parameter instanceof CompoundParameter) {
            parameterValue = buildParameterExpression(((CompoundParameter) parameter).getParameterExpression(), map);
            return parameterValue.toString();

        } else if (parameter instanceof NullParameter) {
            map.put(parameterKey, null);
        }

        return parameterKey;
    }

    @Override
    public String ruleExpressionWithParameterValue(String uuid, Long ruleId) {
        // getting actual values during rule execution
        NamedQueryExecutor<RulesAuditLogParametersValues> rulesAuditLog = new NamedQueryExecutor<RulesAuditLogParametersValues>(
                "CreditPolicyExecutor.getRealParamValue").addParameter("ruleInvocationUUID", uuid).addParameter("ruleId",
                ruleId);
        List<RulesAuditLogParametersValues> rulesAuditLogValues = entityDao.executeQuery(rulesAuditLog);
        if (null != rulesAuditLogValues) {
            Rule rule = ruleService.getRule(ruleId);
            if (rule instanceof ScriptRule) {
                return RuleConstants.SCRIPT_RULE_SENTENCE;

            }else if (rule instanceof SQLRule) {
                return RuleConstants.SQL_RULE_SENTENCE;

            } else {
                return buildRuleExpression(rule, rulesAuditLogValues);
            }
        }
        return "Some Error in Rule Execution";
    }

    /**
     * 
     * Build rule level expression
     * @param rule
     * @param rulesAuditLogValues
     * @return
     */

    private String buildRuleExpression(Rule rule, List<RulesAuditLogParametersValues> rulesAuditLogValues) {
        StringBuilder expression = new StringBuilder();

        // splitting expression with space - assuming that expression can have only brackets, and or operator and condition
        // id.
        String[] tokens = rule.getRuleExpression().split(" ");
        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1) {
                    expression.append(token).append(" ");
                } else {
                    Condition condition = entityDao.find(Condition.class, Long.parseLong(token));
                    if (condition != null) {
                        expression.append(
                                buildConditionExpressionToCompile(condition.getConditionExpression(), rulesAuditLogValues))
                                .append(" ");
                    }
                }
            }
        }
        if (expression.length() > 0) {
            return expression.toString();
        }
        return null;

    }

    private String buildConditionExpressionToCompile(String conditionExpression,
            List<RulesAuditLogParametersValues> rulesAuditLogValues) {
        StringBuilder expression = new StringBuilder();

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
        // operator and parameter id.
        String[] tokens = conditionExpression.split(" ");

        if (tokens != null && tokens.length > 0) {
            for (String token : tokens) {
                token = token.trim();
                // if token is bracket and operator
                if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
                        || commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1
                        || commaDelimitesString(ExpressionValidationConstants.REL_OPS).indexOf(token) != -1) {
                    expression.append(token).append(" ");
                } else {

                    Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
                    if (parameter != null) {

                        for (RulesAuditLogParametersValues rulesAuditLogParametersValues : rulesAuditLogValues) {
                            if (rulesAuditLogParametersValues.getParameterId().getId().equals(parameter.getId())) {
                                String parameterValue = null;
								if (rulesAuditLogParametersValues
										.getParameterValue() != null) {
									parameterValue = getParameterValueFromParameter(
											parameter,
											rulesAuditLogParametersValues
													.getParameterValue(),
											rulesAuditLogParametersValues);

								} else {
                                    parameterValue = "";
                                }
                                expression.append(RulesConverterUtility.replaceSpace(parameter.getName())
                                        + RuleConstants.LEFT_CURLY_BRACES + "<span class='royalBlueBlock'>" + parameterValue
                                        + "</span>" + RuleConstants.RIGHT_CURLY_BRACES);
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (expression.length() > 0) {
            return expression.toString();
        }
        return null;

    }

    @Override
    public Map<Long, String> getRuleStatus(String uuid) {
        MapQueryExecutor executor = new MapQueryExecutor(RulesAuditLog.class, "r").addColumn("r.ruleId", "ruleId")
                .addColumn("r.ruleResult", "ruleResult");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("rule_invocationuuid = :uuid");
        executor.addOrClause(whereClause.toString());
        executor.addBoundParameter("uuid",uuid);
        List<Map<String, ?>> result = entityDao.executeQuery(executor);
        Map<Long, String> finalResult = new HashMap<>();
        for (Map<String, ?> map : result) {
            Long ruleId = Long.parseLong(map.get("ruleId").toString());
            String ruleResult = map.get("ruleResult").toString();
            finalResult.put(ruleId, ruleResult);
        }
        return finalResult;
    }

    @Override
    public List<String> getRuleErrorMessages(String uuid) {
        List<String> ruleMessages = new ArrayList<>();

        MapQueryExecutor executor = new MapQueryExecutor(RulesAuditLog.class, "r").addColumn("r.ruleId", "ruleId").addColumn("r.errorMessage","errorMessage");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("r.ruleInvocationUUID like :uuid and r.ruleResult like 'false'");
        executor.addBoundParameter("uuid", uuid);
        executor.addOrClause(whereClause.toString());
        List<Map<String, ?>> resultMap = entityDao.executeQuery(executor);

        if (null != resultMap && !resultMap.isEmpty()) {
            for (Map<String, ?> map : resultMap) {
                Long ruleId = Long.parseLong(map.get("ruleId").toString());
                Rule rule = ruleService.getRule(ruleId);
                String ruleCode = rule.getCode()+" : ";
                String ruleMessage = map.get("errorMessage").toString();
                ruleMessages.add(ruleMessage);
            }
        }
        return ruleMessages;
    }

    @Override
    public List<RulesAuditLogParametersValues> getRulesAuditLogParameters(String uuid, Long ruleId) {
        NamedQueryExecutor<Object> rulesAuditLog = new NamedQueryExecutor<Object>("RulesAudit.Rules.getParameterValues")
                .addParameter("ruleInvocationUUID", uuid).addParameter("ruleId", ruleId);
        List<Object> resultList = entityDao.executeQuery(rulesAuditLog);

        List<RulesAuditLogParametersValues> rulesAuditLogValues = new ArrayList<>();

        if (CollectionUtils.isNotEmpty(resultList)) {
            for (Object obj : resultList) {
                Object[] countArr = (Object[]) obj;
                if (countArr.length > 0) {
                    rulesAuditLogValues.add(entityDao.find(RulesAuditLogParametersValues.class, (Long) countArr[0]));
                }
            }
        }
        return rulesAuditLogValues;
    }

    @Override
    public List<Map<String, ?>> getRulesAuditLogMap(String uuid) {
        MapQueryExecutor executor = new MapQueryExecutor(RulesAuditLog.class, "r").addColumn("r.ruleId", "ruleId")
                .addColumn("r.ruleResult", "ruleResult").addColumn("r.ruleInvocationPoint", "ruleInvocationPoint")
                .addColumn("r.entityLifeCycleData.creationTimeStamp", "entityLifeCycleDataCreationTimeStamp")
                .addColumn("r.associatedUser.username", "username").addColumn("r.elapsedTime", "elapsedTime")
                .addColumn("r.errorMessage","errorMessage");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("rule_invocationuuid = :uuid");
        executor.addOrClause(whereClause.toString());
        executor.addBoundParameter("uuid",uuid);
        return entityDao.executeQuery(executor);

    }

    @Override
    public List<String> getDistinctRuleInvocationPointsFromRuleAudits() {

        NamedQueryExecutor<String> ruleInvocationPoints = new NamedQueryExecutor<>(
                "RulesAudit.Rules.getRuleInvocationPoints");
        List<String> rulesPointList = entityDao.executeQuery(ruleInvocationPoints);
        // adding points which are not present in database as points of event mapping
        rulesPointList.add(RuleInvocationPoint.RULE_INVOCATION_CREDIT_POLICY_EXECUTION);
        rulesPointList.add(RuleInvocationPoint.RULE_INVOCATION_ELIGIBILITY_POLICY_EXECUTION);
        rulesPointList.add(RuleInvocationPoint.RULE_INVOCATION_ELIGIBILITY_SET_EXECUTION);
        return rulesPointList;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        transactionManager = (PlatformTransactionManager) applicationContext.getBean("transactionManager");
    }

    @Override
    public List<String> getRulesErrorMessages(Map<Object, Object> allRulesResult) {

        List<String> ruleMessages = new ArrayList<>();
        if (null != allRulesResult && allRulesResult.size() > 0) {
            for (Map.Entry<Object, Object> entry : allRulesResult.entrySet()) {
                if (entry.getValue() == "false") {
                    Rule rule = (Rule) entry.getKey();
                    String ruleMessage = ruleService.getRuleErrorMessage(rule, Locale.getDefault());
                    if(ruleMessage!=null){
                    	ruleMessages.add(ruleMessage);
                    }
                }
            }
        }
        return ruleMessages;
    }

    @Override
    public List<String> getRulesErrorMessages(Map<Object, Object> allRulesResult, Map<Object,Object> contextMap) {

        List<String> ruleMessages = new ArrayList<>();
        if (null != allRulesResult && allRulesResult.size() > 0) {
            for (Map.Entry<Object, Object> entry : allRulesResult.entrySet()) {
                if (entry.getValue() == "false") {
                    Rule rule = (Rule) entry.getKey();
                    String ruleMessage = ruleService.getRuleErrorMessage(rule, Locale.getDefault(),contextMap);
                    if(ruleMessage!=null){
                        ruleMessages.add(ruleMessage);
                    }
                }
            }
        }
        return ruleMessages;
    }

    @Override
    public String getInvocationPointFromUUID(String uuid) {

        NamedQueryExecutor<String> ruleInvocationPoints = new NamedQueryExecutor<String>(
                "RulesAudit.Rules.getRuleInvocationPointsNameFromUUID").addParameter("invocationUUID", uuid);
        List<String> invocationPointList = entityDao.executeQuery(ruleInvocationPoints);
        if (invocationPointList != null && !invocationPointList.isEmpty()) {
            return invocationPointList.get(0);
        }
        return null;
    }

    @Override
    public Long getOriginalRule(Long ruleId, DateTime creationTimeStamp) {

        NamedQueryExecutor<Rule> ruleExecutor = new NamedQueryExecutor<Rule>("Rules.AuditLog.getRuleFromRuleAudit")
                .addParameter("ruleId", ruleId).addParameter("approvalStatus", ApprovalStatus.APPROVED);
        List<Rule> ruleList = entityDao.executeQuery(ruleExecutor);

        if (ruleList != null && !ruleList.isEmpty()) {
            Rule rule = ruleList.get(0);

            NamedQueryExecutor<String> uriExecutor = new NamedQueryExecutor<String>("Rules.AuditLog.getOriginalRule")
                    .addParameter("timeStamp", creationTimeStamp).addParameter("uuid",
                            rule.getEntityLifeCycleData().getUuid());
            Set<String> uriSet = new HashSet<>(entityDao.executeQuery(uriExecutor));

            if (!uriSet.isEmpty()) {
                String uri = uriSet.iterator().next();

                if (null != uri && uri.indexOf(":") != -1) {
                    return Long.parseLong((uri.split(":")[1]));
                }
            }
        }

        return null;
    }

    @Override
    public List<UnapprovedEntityData> getRuleChangeState(String uuid) {

        NamedQueryExecutor<UnapprovedEntityData> uedExecutor = new NamedQueryExecutor<UnapprovedEntityData>(
                "Rules.AuditLog.getRuleChangeState").addParameter("uuid", uuid);
        return entityDao.executeQuery(uedExecutor);

    }
    
	private String getParameterValueFromParameter(Parameter parameter,
			String uri,
			RulesAuditLogParametersValues rulesAuditLogParametersValues) {

		String parameterValue = rulesAuditLogParametersValues
				.getParameterValue();
		
		if (((parameter instanceof ReferenceParameter)|| ((parameter instanceof ObjectGraphParameter) &&  ((ObjectGraphParameter)parameter).getDataType()==ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE))  && notNull(uri)) {
			parameterValue = getParameterValue(uri,parameterValue);		
		}
		return parameterValue;
	}
	
	private String getParameterValue(String uri,String parameterValue) {
	
			 String className=getClassName(uri);
			 NamedQueryExecutor<EntityType> entityTypeExecutor = new NamedQueryExecutor<EntityType>(
		                "EntityType.fetchEntityTypeFromClassName").addParameter("className", className);
			EntityType entityType=entityDao.executeQueryForSingleValue(entityTypeExecutor);
			Object object = entityDao.get(EntityId.fromUri(uri));
			if (notNull(object) &&  notNull(entityType) && notNull(entityType.getDescriptionName())) {
				BeanWrapperImpl beanWrapperImpl = new BeanWrapperImpl(object);
				Object value = beanWrapperImpl.getPropertyValue(entityType
						.getDescriptionName());
				if (notNull(value)) {
					parameterValue = value.toString();
				}
			}
			
			return parameterValue;
	}
	
	private String getClassName(String uri) {
        String className = "";

        if (!StringUtils.isBlank(uri)) {
            className = uri.split(":")[0];
        }

        return className;
    }
}
