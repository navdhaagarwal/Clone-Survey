/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.rules.service;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.transaction.TransactionPostCommitWorker;
import com.nucleus.entity.SystemEntity;
import com.nucleus.persistence.HibernateUtils;
import com.nucleus.rules.model.*;
import com.nucleus.rules.model.assignmentMatrix.*;
import com.nucleus.rules.utils.DataContext;
import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hibernate.Hibernate;
import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.rules.assignmentmatrix.service.AssignmentMatrixService;
import com.nucleus.rules.exception.RuleException;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Named("compiledExpressionBuilder")
public class CompiledExpressionBuilderImpl extends BaseRuleServiceImpl implements CompiledExpressionBuilder {

	@Inject
	@Named(value = "ruleExpressionBuilder")
	private RuleExpressionBuilder ruleExpressionBuilder;
	@Inject
	@Named(value = "assignmentMatrixService")
	private AssignmentMatrixService assignmentMatrixService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;
	
	@Inject
	@Named("sQLRuleExecutor")
	private SQLRuleExecutor sqlRuleExecutor;

	@Inject
	@Named("ruleService")
	RuleService                        ruleService;

	@Inject
	@Named("ruleExceptionLoggingPostCommitWorker")
	private RuleExceptionLoggingPostCommitWorker ruleErrorLoggingPostCommitWorker;

	@Lazy
	@Inject
	@Named("ruleExceptionLoggingServiceImpl")
	private RuleExceptionLoggingService ruleExceptionLoggingService;

	@Override
	@MonitoredWithSpring(name = "CEBI_BUILD_AND_COMPILE_RULE_EXPR")
	public void buildAndCompileRuleExpression(long ruleId) {
		/*
		 * Get Instance Of Rule From ruleID
		 */

		String nullSafeExpression = null;
		String normalExpression = null;

		Set<Parameter> parameters = new HashSet<Parameter>();
		Set<String> objectGraph = new HashSet<String>();
		Set<String> nullCheckParams = new HashSet<String>();

		Rule rule = entityDao.find(Rule.class, ruleId);

		try {
			if (null != rule && !rule.isCriteriaRuleFlag() && !(rule instanceof ScriptRule) && !(rule instanceof SQLRule)
					&& null != rule.getRuleExpression()) {

				/*
				 * Build Expression From Rule
				 */
                nullSafeExpression = buildRuleExpressionToCompile(rule.getRuleExpression(), parameters, objectGraph, true,
                        nullCheckParams);

                normalExpression = buildRuleExpressionToCompile(rule.getRuleExpression(), parameters, objectGraph, false,
                        nullCheckParams);

                saveRuleRuntimeMapping(normalExpression, nullSafeExpression, rule, parameters, objectGraph, true);
            }

            if (null != rule && rule instanceof ScriptRule
                    && ((ScriptRule) rule).getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT) {

                ScriptRule scriptRule = (ScriptRule) rule;
                normalExpression = scriptRule.getScriptCode();
                saveRuleRuntimeMapping(normalExpression, null, rule, null, null, true);
            }

        } catch (Exception exception) {
            BaseLoggers.exceptionLogger.debug("Error in compiling Rule expression for rule : " + rule.getName(),
                    exception.getMessage());
        }
    }

    /**
     * Build Rule Expression By Value
	 * @param rule
	 * @return
	 */
    private String buildRuleExpressionToCompile(String ruleExpression, Set<Parameter> parameters, Set<String> objectGraph,
            boolean isNullSafeExp, Set<String> nullCheckParams) {
		StringBuilder expression = new StringBuilder();

        // splitting expression with space - assuming that expression can have only brackets, and or operator and condition
		// id.
		String[] tokens = ruleExpression.split(" ");
		if (tokens != null && tokens.length > 0) {
			for (String token : tokens) {
				token = token.trim();
				// if token is bracket and operator
				if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
						|| commaDelimitesString(ExpressionValidationConstants.RULE_OPERATORS).indexOf(token) != -1) {
					expression.append(token).append(" ");
				} else {
					try {
						Condition condition = entityDao.find(Condition.class, Long.parseLong(token));
						if (condition != null) {
                            expression.append(
                                    buildConditionExpressionToCompile(condition.getConditionExpression(), parameters,
                                            objectGraph, isNullSafeExp, nullCheckParams)).append(" ");
						} else {
							BaseLoggers.flowLogger.debug("Condition is Null for token :: " + token);
						}
					} catch (Exception e) {
						BaseLoggers.exceptionLogger.debug("Method :buildRuleExpressionToCompile: token = " + token);
					}
				}
			}
		}
		if (expression.length() > 0) {
			return expression.toString();
		}
		return null;

	}

	/**
	 * Build Condition To Compile
	 * @param condition
	 * @param parameters
	 * @return
	 */
	private String buildConditionExpressionToCompile(String conditionExpression, Set<Parameter> parameters,
			Set<String> objectGraph, boolean isNullSafeExp, Set<String> nullCheckParams) {
		StringBuilder expression = new StringBuilder();

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
		// operator and parameter id.
		String[] tokens = conditionExpression.split(" ");
		int currentIndex = 0;

		if (tokens != null && tokens.length > 0) {
			for (String token : tokens) {
				token = token.trim();
				// if token is bracket and operator
				if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
						|| commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1
						|| commaDelimitesString(ExpressionValidationConstants.REL_OPS).indexOf(token) != -1) {
					expression.append(token).append(" ");
				} else {
					try {
						Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
						if (parameter != null) {

                            expression.append(
                                    buildParameterExpressionToCompile(parameter, parameters, objectGraph, isNullSafeExp,
                                            tokens, currentIndex)).append(" ");
						} else {
							BaseLoggers.flowLogger.debug("Parameter is Null for token :: " + token);
						}
					} catch (Exception e) {
                        BaseLoggers.exceptionLogger.debug("Method :buildConditionExpressionToCompile: token = " + token);
					}
				}

				currentIndex++;
			}
		}
		if (expression.length() > 0) {
			return expression.toString();
		}
		return null;

	}

	@Override
    public String buildNullSafeExpressionToCompile(ObjectGraphTypes objectGraph) {
		String paramVal = "";
		Integer dataType = Integer.valueOf(objectGraph.getDataType().getCode());
        if (ParameterDataType.PARAMETER_DATA_TYPE_DATE ==dataType) {
            paramVal = "( ?"
                    + RulesConverterUtility.getNullSafeObjectGraph(objectGraph.getObjectGraph() + RuleConstants.RULE_TIME_IN_MILLIS) + " )";

        } else if (ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE == dataType) {
            paramVal = "( ?"
                    + RulesConverterUtility.getNullSafeObjectGraph(objectGraph.getObjectGraph() + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE) + " )";

        } else if (ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE == dataType) {
            paramVal = "( ?"
                    + RulesConverterUtility.getNullSafeObjectGraph(objectGraph.getObjectGraph() + RuleConstants.RULE_IDS) + " )";
		} else {
            paramVal = "( ?"
                    + RulesConverterUtility.getNullSafeObjectGraph(objectGraph.getObjectGraph()) + " )";
		}
		return paramVal;
	}
	/**
	 * Build Parameter Expression To Compile
	 * @param parameter
	 * @param parameters
	 * @return
	 */
	@Override
    public String buildParameterExpressionToCompile(Parameter parameter, Set<Parameter> parameters, Set<String> objectGraph,
            boolean isNullSafeExp, String[] expression, int currentPos) {

		String paramVal = "";

		if (null != parameter) {
			try {
				if (parameter instanceof ObjectGraphParameter) {

					if (isNullSafeExp) {
                        if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
                            paramVal = "( ?"
                                    + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                            .getObjectGraph() + RuleConstants.RULE_TIME_IN_MILLIS) + " )";

                        } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                            paramVal = "( ?"
                                    + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                            .getObjectGraph() + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE) + " )";

                        } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                            paramVal = "( ?"
                                    + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                            .getObjectGraph() + RuleConstants.RULE_IDS) + " )";
						} else {
                            paramVal = "( ?"
                                    + RulesConverterUtility.getNullSafeObjectGraph(((ObjectGraphParameter) parameter)
                                            .getObjectGraph()) + " )";
						}

					} else {
						String obj = ((ObjectGraphParameter) parameter).getObjectGraph();

                        if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
							if (null != objectGraph) {

                                obj = "( ?"
                                        + RulesConverterUtility.getNullSafeObjectGraph(obj
                                                + RuleConstants.RULE_TIME_IN_MILLIS) + " )";
								objectGraph.add(obj);
							}
							paramVal = obj;

                        } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {

                            obj = "( ?" + RulesConverterUtility.getNullSafeObjectGraph(obj + RuleConstants.RULE_IDS) + " )";
							;
							objectGraph.add(obj);

							paramVal = obj;

						} else {
							if (null != objectGraph) {
								obj = "( ?" + RulesConverterUtility.getNullSafeObjectGraph(obj) + " )";
								;
								objectGraph.add(obj);
							}
							paramVal = obj;
						}
					}
				}

				if (parameter instanceof ConstantParameter) {
					Object val = ((ConstantParameter) parameter).getLiteralValue();
					if (val instanceof java.lang.String) {
						paramVal = "'" + String.valueOf(val) + "'";
					} else {
						paramVal = String.valueOf(val);
					}
				}

				if (parameter instanceof SystemParameter) {
					parameters.add(parameter);
					paramVal = RulesConverterUtility.replaceSpace(parameter.getName()) + RuleConstants.PARAMETER_NAME_ID
							+ parameter.getId();

				}

				if (parameter instanceof CompoundParameter) {
                    paramVal = buildCompoundParameterExpression(((CompoundParameter) parameter).getParameterExpression(),
                            parameters, objectGraph, isNullSafeExp);
				}

				if (parameter instanceof ReferenceParameter) {
					Object val = entityDao.get(((ReferenceParameter) parameter).getReferenceEntityId()).getId();
					paramVal = String.valueOf(val) + "L";
				}

				if (parameter instanceof QueryParameter) {
					parameters.add(parameter);
					paramVal = RulesConverterUtility.replaceSpace(parameter.getName()) + RuleConstants.PARAMETER_NAME_ID
							+ parameter.getId();
				}

				if(parameter instanceof SQLParameter){
					parameters.add(parameter);
					paramVal = RulesConverterUtility.replaceSpace(parameter.getName()) + RuleConstants.PARAMETER_NAME_ID
							+ parameter.getId();
				}

				if (parameter instanceof ScriptParameter) {
					parameters.add(parameter);
					paramVal = RulesConverterUtility.replaceSpace(parameter.getName()) + RuleConstants.PARAMETER_NAME_ID
							+ parameter.getId();
				}

				if (parameter instanceof NullParameter) {
					paramVal = null;
				}

				if (!(parameter instanceof ConstantParameter || parameter instanceof ReferenceParameter
                        || parameter instanceof NullParameter || parameter instanceof SystemParameter || parameter instanceof CompoundParameter)) {
					paramVal = addNullCheckParams(expression, currentPos, paramVal);
				}

			} catch (Exception e) {
                BaseLoggers.exceptionLogger.debug("Method :buildParameterExpressionToCompile: Parameter = "
                        + parameter.getUri());
			}
		}
		return paramVal;
	}

	/**
	 * Build Compound Parameter Expression To Compile
	 * @param parameterExpression
	 * @param parameters
	 * @return
	 */
	private String buildCompoundParameterExpression(String parameterExpression, Set<Parameter> parameters,
			Set<String> objectGraph, boolean isNullSafeExp) {
		StringBuilder expression = new StringBuilder();

        // splitting expression with space - assuming that expression can have only brackets, and arithmetic and relative
		// operator and parameter id.
		String[] tokens = parameterExpression.split(" ");
		int currentIndex = 0;

		if (tokens != null && tokens.length > 0) {
			for (String token : tokens) {
				token = token.trim();
				// if token is bracket and operator
				if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
						|| commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {
					expression.append(token).append(" ");
				} else {

					try {
						Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
						if (parameter != null) {
                            expression.append(
                                    buildParameterExpressionToCompile(parameter, parameters, objectGraph, isNullSafeExp,
                                            tokens, currentIndex)).append(" ");
						} else {
							BaseLoggers.flowLogger.debug("Parameter is Null for token :: " + token);
						}
					} catch (Exception e) {
						BaseLoggers.exceptionLogger.debug("Method :buildCompoundParameterExpression: token = " + token);
					}
				}

				currentIndex++;
			}
		}
		if (expression.length() > 0) {
			return expression.toString();
		}
		return null;

	}

	private void saveRuleRuntimeMapping(String normalExpression, String nullSafeExpression, Rule rule,
			Set<Parameter> parameters, Set<String> objectGraph, boolean persistAllowed) {

        BaseLoggers.flowLogger.debug(rule.getName() + " :: normalExpression = " + normalExpression
                + " :: nullSafeExpression = " + nullSafeExpression + " :: objectGraph = " + objectGraph
                + " :: Parameters = " + parameters);

        BaseLoggers.flowLogger.info(rule.getName() + " :: normalExpression = " + normalExpression
                + " :: nullSafeExpression = " + nullSafeExpression + " :: objectGraph = " + objectGraph
                + " :: Parameters = " + parameters);

		RuntimeRuleMapping runtimeRuleMapping = new RuntimeRuleMapping();
		runtimeRuleMapping.setParameters(parameters);

		runtimeRuleMapping.setObjectGraphs(objectGraph);

		runtimeRuleMapping.setCompiledExpression(compileExpression(normalExpression));
		runtimeRuleMapping.setCompiledNullSafeExpression(compileExpression(nullSafeExpression));

		rule.setRuntimeRuleMapping(runtimeRuleMapping);

		if (persistAllowed) {
			entityDao.persist(rule);
		}
	}

	/**
	 * 
	 * get the serialized object
	 * @param ruleId
	 * @return
	 * @throws IOException
	 */

	private Serializable getCompiledExpression(Rule rule, boolean isNullSafe) throws IOException {
		RuntimeRuleMapping runtimeRuleMapping = rule.getRuntimeRuleMapping();

		if (isNullSafe) {
			return runtimeRuleMapping.getCompiledNullSafeExpression();
		} else {
			return runtimeRuleMapping.getCompiledExpression();
		}
	}

	/**
	 * 
	 * get the cloned serialized object
	 * @param ruleId
	 * @return
	 * @throws IOException
	 */

	private Serializable getClonedCompiledExpression(Rule rule, boolean isNullSafe) throws IOException {
		RuntimeRuleMapping runtimeRuleMapping = rule.getRuntimeRuleMapping();
		Serializable clonedObject = null;
		if (isNullSafe) {
			clonedObject = SerializationUtils.clone(runtimeRuleMapping.getCompiledNullSafeExpression());
			return clonedObject;
		} else {
			clonedObject = SerializationUtils.clone(runtimeRuleMapping.getCompiledExpression());
			return clonedObject;
		}
	}

	/**
     * Method to evaluate Null Safe Expression.
     * Follows simple evaluation of Rule
     * Return character like P, F
	 */
	private char evaluateNullSafeRuleExpression(long ruleId, Map map) {
		Rule rule = null;
		try {
			rule = getRule(ruleId);

			if (rule.getRuntimeRuleMapping() == null) {
				throw new RuleException(RuleConstants.RULE_EXCEPTION_MESSAGE);
			}

			Serializable ruleObject = getClonedCompiledExpression(rule, true);

			evaluateRuntimeParameters(map, rule.getRuntimeRuleMapping().getParameters());

			Boolean result = (Boolean) RuleExpressionMvelEvaluator.evaluateCompiledExpression(ruleObject, map);
			if (result != null && result) {
				return RuleConstants.RULE_RESULT_PASS;
			} else {
				return RuleConstants.RULE_RESULT_FAIL;
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error occured for Rule ::" + rule.getName() + "::" + e);
			throw new RuleException(RuleConstants.RULE_EXCEPTION_MESSAGE + rule.getName(), e);
		}
	}

	@Override
	@MonitoredWithSpring(name = "CEBI_EVALUATE_RULE")
	public char evaluateRule(long ruleId, Map map) {
		return evaluateRule(ruleId, map, false);
	}

	@Override
	@MonitoredWithSpring(name = "CEBI_EVALUATE_RULE_WITH_STRICT_EVAL")
		public char evaluateRule(long ruleId, Map map, boolean isStrictEvaluation) {

		char result = RuleConstants.RULE_RESULT_FAIL;
		Rule rule = getRule(ruleId);
		try {
			// getting start time
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();

			rule = HibernateUtils.initializeAndUnproxy(rule);

			if (rule instanceof ScriptRule) {

				if (((ScriptRule) rule).getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_SHELL_SCRIPT) {
					result = evaluateScriptRule((ScriptRule) rule, map, isStrictEvaluation);

				} else if (((ScriptRule) rule).getScriptCodeType() == RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT) {
					result = evaluateMvelRuleScript((ScriptRule) rule, map, isStrictEvaluation);
				}

			} else if (rule instanceof SQLRule) {
				Hibernate.initialize(((SQLRule) rule).getParamMapping());
				result = sqlRuleExecutor.evaluateSQLRule((SQLRule) rule, map, isStrictEvaluation);

			} else {

				if (isStrictEvaluation) {
					result = evaluateRuleExpression(ruleId, map);

				} else {
					result = evaluateNullSafeRuleExpression(ruleId, map);
				}
			}
			// getting end time
			stopWatch.stop();
			Long elapsedTimeInSecond = stopWatch.getTime();

			map.put(rule.getName() + "_" + rule.getId() + "_" + RuleConstants.RULE_ELAPSED_TIME, elapsedTimeInSecond);


		}catch (Exception e){
			BaseLoggers.exceptionLogger.error("Exception occured while evaluating rule :" ,e);
			RuleExceptionLoggingVO ruleExceptionLoggingVO = new RuleExceptionLoggingVO();
			ruleExceptionLoggingVO.setContextMap(map);
			ruleExceptionLoggingVO.setE(e);
			ruleExceptionLoggingVO.setRule(rule);
			ruleExceptionLoggingVO.setExceptionOwner(RuleConstants.RULE_EXCEPTION);
    		ruleExceptionLoggingService.saveRuleErrorLogs(ruleExceptionLoggingVO);
    		throw new RuleException("Error occured while evaluating rule : "+rule.getCode()+" : ",e);
		}
		return result;
	}

	/**
     * Method to evaluate Normal Expression.
     * Follows strict evaluation of Rule
	 * Return character like P, F , O
	 */
	private char evaluateRuleExpression(long ruleId, Map map) {
		Rule rule = null;
		try {
			if(map instanceof DataContext){
				DataContext dataContext = (DataContext)map;
				dataContext.setExecutionStarted(true);
			}
			rule = getRule(ruleId);
			if (rule.getRuntimeRuleMapping() == null) {
				throw new RuleException(RuleConstants.RULE_EXCEPTION_MESSAGE);
			}
			Serializable ruleObject = getClonedCompiledExpression(rule, false);

			// Load the the set of parameters from the rule
			evaluateRuntimeParameters(map, rule.getRuntimeRuleMapping().getParameters());
			Boolean result = (Boolean) RuleExpressionMvelEvaluator.evaluateCompiledExpression(ruleObject, map);
			if (result != null && result) {
				return RuleConstants.RULE_RESULT_PASS;
			} else {
				Set<String> objectGraphSet = rule.getRuntimeRuleMapping().getObjectGraphs();
				for (String objGraph : objectGraphSet) {
					Object res = MVEL.eval(objGraph, map);
					if (res == null) {
						return RuleConstants.RULE_RESULT_NORESULT;
					}
				}
				return RuleConstants.RULE_RESULT_FAIL;
			}
		} catch (PropertyAccessException e) {
			BaseLoggers.exceptionLogger.error("Rule" + rule.getName() + "returned no result" + e);
			return RuleConstants.RULE_RESULT_NORESULT;
		}  catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error occured for Rule ::" + rule.getName() + "::" + e);
			throw new RuleException(RuleConstants.RULE_EXCEPTION_MESSAGE + rule.getName(), e);
		}
	}

	/**
	 * Retrive the by Id.
	 * 
	 * @return
	 */
	private Rule getRule(long ruleId) {
		return entityDao.find(Rule.class, ruleId);
	}

	private char evaluateMvelRuleScript(ScriptRule scriptRule, Map contextMap, boolean isStrictEvaluation) {

		try {

			if (scriptRule.getRuntimeRuleMapping() == null) {
				throw new RuleException(RuleConstants.RULE_EXCEPTION_MESSAGE);
			}

			Serializable ruleObject = getClonedCompiledExpression(scriptRule, false);
			evaluateRuntimeParameters(contextMap, scriptRule.getRuntimeRuleMapping().getParameters());

			Boolean result = (Boolean) RuleExpressionMvelEvaluator.evaluateCompiledExpression(ruleObject, contextMap);

			if (isStrictEvaluation) {
				if (result == null) {
					return RuleConstants.RULE_RESULT_NORESULT;
				}
				return result.booleanValue() ? RuleConstants.RULE_RESULT_PASS : RuleConstants.RULE_RESULT_FAIL;
			}
			return (result == null || !result.booleanValue()) ? RuleConstants.RULE_RESULT_FAIL
					: RuleConstants.RULE_RESULT_PASS;

		} catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Error occured for Rule ::" + scriptRule.getName() + ":: + and RuleId = "
                    + scriptRule.getId() + e);
			throw new RuleException("Error occured while evaluating Mvel Script Rule :: " + scriptRule.getName()
					+ " and RuleId = " + scriptRule.getId(), e);
		}
	}

	@Override
	public void buildAndCompileMvelScriptparameter(long scriptParameterId) {
		DerivedParameter derivedParameter = entityDao.find(DerivedParameter.class, scriptParameterId);
		if (derivedParameter != null && derivedParameter.getScriptCode() != null
				&& !derivedParameter.getScriptCode().isEmpty()
				&& (derivedParameter.getScriptCodeType() == (RuleConstants.SCRIPTCODETYPE_MVEL_SCRIPT))) {
			derivedParameter.setCompiledExpression(compileExpression(derivedParameter.getScriptCode()));
			entityDao.update(derivedParameter);
		}
	}

	@Override
	public String buildExpressionOfDerivedParameter(DerivedParameter derivedParameter) {
		if (derivedParameter.getTargetObjectGraph().getId() != null) {
			Parameter targetOGNL = getParameter(derivedParameter.getTargetObjectGraph().getId());
			if (targetOGNL != null && targetOGNL instanceof ObjectGraphParameter) {
				String objectGraph = ((ObjectGraphParameter) targetOGNL).getObjectGraph();
				if (targetOGNL.getDataType().equals(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE)) {
					objectGraph = objectGraph + RuleConstants.RULE_IDS;
				}
				if (objectGraph.contains(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)
                        && derivedParameter.getFilterCriterias() != null && derivedParameter.getFilterCriterias().size() > 0) {
                    return convertObjectToExpression(
                            derivedParameter,
                            objectGraph.substring(objectGraph.lastIndexOf(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)
											+ RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE.length()));
				}
			}
		}
		return null;
	}

	private String convertObjectToExpression(DerivedParameter derivedParameter, String returnValue) {
		StringBuilder finalExpression = new StringBuilder();
		// sorting on the basic of order
		Collections.sort(derivedParameter.getFilterCriterias(), new Comparator<DerivedParamFilterCriteria>() {
			@Override
			public int compare(DerivedParamFilterCriteria arg0, DerivedParamFilterCriteria arg1) {
				if (arg0.getOrderSequence() == arg1.getOrderSequence()) {
					return 0;
				}
				return arg0.getOrderSequence() < arg1.getOrderSequence() ? -1 : 1;
			}
		});

		String aggregateFunctionCode = null;
		if (RuleConstants.MVEL_SHELL_SCRIPT_AGGRGRATE_FUNCTION.containsKey(derivedParameter.getAggregateFunction())) {
			aggregateFunctionCode = derivedParameter.getAggregateFunction();
		}

		if (aggregateFunctionCode != null) {
            if(aggregateFunctionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MAX_CODE) || 
            		aggregateFunctionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MIN_CODE)){
            	finalExpression.append(RuleConstants.MVEL_RETURN_VARIABLE).append(RuleConstants.MVEL_EQUAL).append(RuleConstants.EXPRESSION_SCRIPT_NULL_VALUE).append(RuleConstants.MVEL_SEMICOLON);
                }
            else{
				finalExpression.append(RuleConstants.MVEL_RETURN_VARIABLE + RuleConstants.MVEL_EQUAL + " 0"
						+ RuleConstants.MVEL_SEMICOLON);
			}
            finalExpression.append(RuleConstants.MVEL_INDEX_VARIABLE + RuleConstants.MVEL_EQUAL + " 0"
                    + RuleConstants.MVEL_SEMICOLON);
		}

		if (derivedParameter.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN
				&& derivedParameter.getEntityField() == false) {
			finalExpression.append(RuleConstants.MVEL_BOOLEANFLAG_EQUAL_FALSE);
		}

		if (derivedParameter.getFilterCriterias().size() > 0) {
			String collectionName = derivedParameter.getFilterCriterias().get(0).getCollectionName();
			collectionName = collectionName.substring(0,
					collectionName.indexOf(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE));
			creatLoop(finalExpression, new ArrayList<DerivedParamFilterCriteria>(derivedParameter.getFilterCriterias()),
					collectionName, 0, aggregateFunctionCode, returnValue, derivedParameter);

			finalExpression.append(RuleConstants.MVEL_RETURN_STATEMENT_SUM + RuleConstants.MVEL_SEMICOLON);

		}

		return finalExpression.toString();
	}

	private void creatLoop(StringBuilder expression, List<DerivedParamFilterCriteria> derivedParamFilterCriteria,
			String collectionName, int index, String aggregateFunctionCode, String returnValue,
			DerivedParameter derivedParameter) {
		if (derivedParamFilterCriteria.size() > 0) {
			DerivedParamFilterCriteria currentParam = derivedParamFilterCriteria.get(0);
			String currentLoopvariable = RuleConstants.MVEL_LOOPVARIABLE + index;
			boolean isWherePresent = false;

			// added to check for null for collection
			expression.append(RuleConstants.EXPRESSION_SCRIPT_IF_START).append(collectionName)
					.append(RuleConstants.EXPRESSION_SCRIPT_NOT_NULL_CHECK).append(RuleConstants.AND_OPERATOR)
                    .append(RuleConstants.NOT_OPERATOR_ENGLISH)
                    .append(RuleConstants.LEFT_PAREN).append(RuleConstants.LEFT_PAREN).append(collectionName)
					.append(RuleConstants.EXPRESSION_SCRIPT_SIZE_CHECK).append(RuleConstants.RIGHT_PAREN)
					.append(RuleConstants.LEFT_CURLY_BRACES);

			expression.append(RuleConstants.MVEL_FOREACH).append(RuleConstants.LEFT_PAREN);
			expression.append(currentLoopvariable).append(RuleConstants.MVEL_COLON + collectionName);
			expression.append(RuleConstants.RIGHT_PAREN);
			expression.append(RuleConstants.LEFT_CURLY_BRACES);

			if (currentParam.getWhereExpression() != null && !currentParam.getWhereExpression().isEmpty()) {
				isWherePresent = true;
				// where clause is present
				expression.append(RuleConstants.MVEL_IF + RuleConstants.LEFT_PAREN);
				String[] splittedWhereClause = currentParam.getWhereExpression().split("\\s+");
				if (splittedWhereClause.length > 0) {
					for (String token : splittedWhereClause) {
						if (token != null && !token.isEmpty()) {
							expression.append(getStringOfToken(token, currentLoopvariable));
						}
					}
				}
				expression.append(" " + RuleConstants.RIGHT_PAREN + RuleConstants.LEFT_CURLY_BRACES);
			}

			Boolean entityFieldValue = derivedParameter.getEntityField();
			if (entityFieldValue == null) {
				entityFieldValue = false;
			}

			Boolean checkForAllElements = derivedParameter.getValidateOnAll();
			if (checkForAllElements == null) {
				checkForAllElements = false;
			}

			Integer parameterDataType = derivedParameter.getDataType();

			// body of loop start
			// checking is this is the last element or have more
			if (derivedParamFilterCriteria.size() == 1) {
				if (aggregateFunctionCode != null) {
                    expression.append(giveLoopBodyForMathFunction(aggregateFunctionCode, currentLoopvariable, returnValue));
				} else {

					boolean flag = true;

					if (ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN == parameterDataType) {
						if (!entityFieldValue) {
							if (checkForAllElements) {
								expression.append(RuleConstants.MVEL_BOOLEANFLAG_EQUAL_TRUE);
							} else {
								expression.append(RuleConstants.MVEL_RETURN_TRUE);
							}
							flag = false;
						}
					}

					if (flag) {
						expression.append(RuleConstants.MVEL_RETURN_KEYWORD + currentLoopvariable + returnValue
								+ RuleConstants.MVEL_SEMICOLON);
					}
				}

			} else {
				String currentOgnl = derivedParamFilterCriteria.get(0).getCollectionName().replaceAll("\\[\\]", "");
				String nextOgnl = derivedParamFilterCriteria.get(1).getCollectionName().replaceAll("\\[\\]", "");
				nextOgnl = nextOgnl.replaceAll(currentOgnl, currentLoopvariable);
				derivedParamFilterCriteria.remove(0);
                creatLoop(expression, derivedParamFilterCriteria, nextOgnl, index + 1, aggregateFunctionCode, returnValue,
                        derivedParameter);
			}

			// body of loop end
			// closing of where clause if present
			if (isWherePresent) {
                expression.append(RuleConstants.RIGHT_CURLY_BRACES);    // if closing brace
				if (ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN == parameterDataType && !entityFieldValue) {

					expression.append(RuleConstants.MVEL_ELSE_OPEN);
					if (checkForAllElements) {
						expression.append(RuleConstants.EXPRESSION_SCRIPT_RETURN_FALSE);
					} else {
						expression.append(RuleConstants.MVEL_BOOLEANFLAG_EQUAL_FALSE);
					}
                    expression.append(RuleConstants.RIGHT_CURLY_BRACES);    // else closing brace

				}
			}

           

            expression.append(RuleConstants.RIGHT_CURLY_BRACES);        // for Loop closing brace
			if (ParameterDataType.PARAMETER_DATA_TYPE_BOOLEAN == parameterDataType && !entityFieldValue) {
				expression.append(RuleConstants.MVEL_RETURN_KEYWORD + RuleConstants.MVEL_BOOLEANFLAG
						+ RuleConstants.MVEL_SEMICOLON);
			}

			if (aggregateFunctionCode != null) {
				expression.append(giveReturnStatementForFunctions(aggregateFunctionCode));

			} else {
				expression.append(RuleConstants.EXPRESSION_SCRIPT_RETURN_NULL);
			}

			expression.append(RuleConstants.RIGHT_CURLY_BRACES);

			return;
		} else {
			return;
		}
	}

	private String giveReturnStatementForFunctions(String functionCode) {

		if (functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_SUM_CODE)) {
			return RuleConstants.MVEL_RETURN_STATEMENT_SUM + RuleConstants.MVEL_SEMICOLON;

		} else if (functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_AVERAGE_CODE)) {
			return RuleConstants.MVEL_RETURN_STATEMENT_AVERAGE + RuleConstants.MVEL_SEMICOLON;
        }
        else if(functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MAX_CODE) || functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MIN_CODE)){
			StringBuilder returnExpression = new StringBuilder();
			returnExpression.append(RuleConstants.MVEL_RETURN_STATEMENT_SUM).append(RuleConstants.MVEL_SEMICOLON);

			return returnExpression.toString();
		}

		return null;
	}

	private String giveLoopBodyForMathFunction(String functionCode, String currentLoopVariable, String returnVariable) {
		StringBuilder mathExpression = new StringBuilder();

		String[] variablesToCheck = returnVariable.split("\\.");

		mathExpression.append(RuleConstants.EXPRESSION_SCRIPT_IF_START);
		StringBuilder returnVariableBuilder = new StringBuilder();
		for (String variable : variablesToCheck) {
			if (!variable.equals("")) {
				
				returnVariableBuilder.append(".").append(variable);
				mathExpression.append(currentLoopVariable)
						.append(returnVariableBuilder.toString())
						.append(RuleConstants.EXPRESSION_SCRIPT_NOT_NULL_CHECK);
			

					mathExpression.append(RuleConstants.AND_OPERATOR).append(" ");
			
				

			}
		}
		mathExpression.setLength(mathExpression.length() - 3);
		
		
		mathExpression.append(RuleConstants.RIGHT_PAREN).append(RuleConstants.LEFT_CURLY_BRACES);
		
		
		if (functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_SUM_CODE)) {
			
			mathExpression.append(RuleConstants.MVEL_RETURN_VARIABLE).append(RuleConstants.MVEL_EQUAL).append(RuleConstants.MVEL_RETURN_VARIABLE)
					.append(RuleConstants.MVEL_PLUS).append(currentLoopVariable).append(returnVariable).append(RuleConstants.MVEL_SEMICOLON).
					append(RuleConstants.RIGHT_CURLY_BRACES);

			return mathExpression.toString();
		} else if (functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_AVERAGE_CODE)) {

			mathExpression.append(RuleConstants.MVEL_RETURN_VARIABLE).append(RuleConstants.MVEL_EQUAL).append(RuleConstants.MVEL_RETURN_VARIABLE)
			.append(RuleConstants.MVEL_PLUS).append(currentLoopVariable).append(returnVariable).append(RuleConstants.MVEL_SEMICOLON)
			.append(RuleConstants.MVEL_INDEX_VARIABLE).append(RuleConstants.MVEL_EQUAL).append(RuleConstants.MVEL_INDEX_VARIABLE)
			.append(RuleConstants.MVEL_PLUS).append(" 1 ").append(RuleConstants.MVEL_SEMICOLON).append(RuleConstants.RIGHT_CURLY_BRACES);

			return mathExpression.toString();
		}

        else if(functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MAX_CODE) || functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MIN_CODE)){

			String relationalOperator = "";

			if (functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MAX_CODE)) {
				relationalOperator = RuleConstants.MVEL_SMALLER_THAN;
			}

			else if (functionCode.equals(RuleConstants.MVEL_SHELL_SCRIPT_AGGRERATE_MIN_CODE)) {
				relationalOperator = RuleConstants.MVEL_GREATER_THAN;
			}
		
			mathExpression.append(RuleConstants.EXPRESSION_SCRIPT_IF_START).append(RuleConstants.MVEL_RETURN_VARIABLE)
					.append(RuleConstants.EXPRESSION_SCRIPT_NULL_CHECK).append(RuleConstants.RIGHT_PAREN)
					.append(RuleConstants.LEFT_CURLY_BRACES).append(RuleConstants.MVEL_RETURN_VARIABLE)
					.append(RuleConstants.MVEL_EQUAL).append(currentLoopVariable).append(returnVariable)
					.append(RuleConstants.MVEL_SEMICOLON).append(RuleConstants.RIGHT_CURLY_BRACES);
			

			mathExpression.append(RuleConstants.MVEL_ELSE_IF_OPEN).append(RuleConstants.MVEL_RETURN_VARIABLE)
					.append(relationalOperator).append(currentLoopVariable).append(returnVariable)
					.append(RuleConstants.RIGHT_PAREN).append(RuleConstants.LEFT_CURLY_BRACES)
					.append(RuleConstants.MVEL_RETURN_VARIABLE).append(RuleConstants.MVEL_EQUAL)
					.append(currentLoopVariable).append(returnVariable).append(RuleConstants.MVEL_SEMICOLON)
					.append(RuleConstants.RIGHT_CURLY_BRACES).append(RuleConstants.RIGHT_CURLY_BRACES);

			return mathExpression.toString();
		}

		return null;
	}

	private String getStringOfToken(String token, String loopVariable) {
		boolean isTokenBracket = token.equals("(") || token.equals(")");
		if (isTokenBracket) {
			return token;
		}
        boolean isJoinOperator = Arrays.asList(ExpressionValidationConstants.SUPPORTED_CONDITION_JOIN_OPERATORS_MVEL_SCRIPT)
                .contains(token);
		if (isJoinOperator) {
			return token;
		}
		boolean isOperator = RuleConstants.conditionOperatorsForMVELScript.contains(token);
		if (isOperator) {
			return token;
		}
		Parameter param = getParameter(Long.parseLong(token));
		if (param != null) {
			return buildParameterForDerivedParameter(param, false, loopVariable);
		}
		return null;
	}

	private String buildParameterForDerivedParameter(Parameter parameter, boolean isNullSafe, String loopVariable) {
		if (parameter instanceof ObjectGraphParameter) {
			String objectGraph = ((ObjectGraphParameter) parameter).getObjectGraph();
			if (objectGraph.contains(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)) {
				objectGraph = loopVariable
						+ objectGraph.substring(objectGraph.lastIndexOf(RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE)
								+ RuleConstants.MVEL_SHELL_SCRIPT_COLLECTION_TYPE.length());
				if (parameter.getDataType().equals(ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE)) {
					return objectGraph + RuleConstants.RULE_IDS;
				} else {
					return objectGraph;
				}
			}
			if (isNullSafe) {
				if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
					return "( ?" + RulesConverterUtility.getNullSafeObjectGraph(objectGraph)
							+ RuleConstants.RULE_TIME_IN_MILLIS + " )";

                } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
					return "( ?" + RulesConverterUtility.getNullSafeObjectGraph(objectGraph)
					+ RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE + " )";

                } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
                    return "( ?" + RulesConverterUtility.getNullSafeObjectGraph(objectGraph)
                            + RuleConstants.NULL_SAFE_RULE_IDS + " )";
                } else {
                    return "( ?" + RulesConverterUtility.getNullSafeObjectGraph(objectGraph) + " )";
                }
            } else {
                String obj = ((ObjectGraphParameter) parameter).getObjectGraph();

                if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_DATE) {
                    return obj + RuleConstants.RULE_TIME_IN_MILLIS;

                } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_JAVA_UTIL_DATE) {
                    return obj + RuleConstants.RULE_TIME_FOR_JAVA_UTIL_DATE;

                } else if (((ObjectGraphParameter) parameter).getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE) {
					return "( ?" + RulesConverterUtility.getNullSafeObjectGraph(objectGraph)
							+ RuleConstants.NULL_SAFE_RULE_IDS + " )";
				} else {
					return obj;
				}
			}
		}
		if (parameter instanceof ConstantParameter) {
			Object val = ((ConstantParameter) parameter).getLiteralValue();
			if (val instanceof java.lang.String) {
				return "'" + String.valueOf(val) + "'";
			} else {
				return String.valueOf(val);
			}
		}
		if (parameter instanceof ReferenceParameter) {
			Object val = entityDao.get(((ReferenceParameter) parameter).getReferenceEntityId()).getId();
			return String.valueOf(val) + "L";
		}
		return null;
	}

	@Override
	public AssignmentAction buildExpressionForAssignmentActions(AssignmentAction assignmentAction) {

		Set<Parameter> parameters = new HashSet<Parameter>();

		String assignmentExpression = assignmentAction.getLeftValue() + " = ";

		assignmentExpression = assignmentExpression
				+ buildParameterExpressionToCompile(assignmentAction.getRightValue(), parameters, null, true, null, 0);

		BaseLoggers.flowLogger.debug("Rule Action compiling expression :: " + assignmentExpression);
		BaseLoggers.flowLogger.info("Rule Action compiling expression :: " + assignmentExpression);

		assignmentAction.setCompiledExpression(compileExpression(assignmentExpression));
		assignmentAction.setParameters(parameters);

		return assignmentAction;
	}

	@Override
	public void executeAssignmentAction(RuleAction ruleAction, Map<Object, Object> map) {
		if (ruleAction != null && ruleAction.getCompiledExpression() != null) {
			boolean multipleResults = map.get(AssignmentConstants.MULTIPLE_RESULTS)!=null ? (Boolean) map.get(AssignmentConstants.MULTIPLE_RESULTS) : false;
			Serializable compiledExpression = ruleAction.getCompiledExpression();
			if(multipleResults){
				String expression = changeCompiledExpression(ruleAction,map);
				if(StringUtils.isNotEmpty(expression))
					compiledExpression = compileRuleLevelExp(expression);
			}
			Serializable assignmentObjectExpression = SerializationUtils.clone(compiledExpression);
			BaseLoggers.flowLogger.debug("Executing Action :: = " + ruleAction.getId());
			evaluateRuntimeParameters(map, ruleAction.getParameters());

			if (null != assignmentObjectExpression) {
				RuleExpressionMvelEvaluator.evaluateCompiledExpression(assignmentObjectExpression, map);
			}
		}
	}

	private String changeCompiledExpression(RuleAction ruleAction, Map<Object, Object> map){
		String expression=null;
		Integer index = (Integer) map.get(AssignmentConstants.INDEX_REPLACEMENT);
		List<String> multipleResultList = (List<String>)map.get(AssignmentConstants.MULTIPLE_RESULT_CONTEXT);
		multipleResultList.add(index,AssignmentConstants.JUNK_VALUE);
		map.put(AssignmentConstants.MULTIPLE_RESULT_CONTEXT,multipleResultList);
		if(ruleAction instanceof AssignmentMatrixAction) {
			AssignmentMatrixAction assignmentMatrixAction = (AssignmentMatrixAction) ruleAction;
			List<String> valueList = new ArrayList<>();
			String assignmentValue = assignmentMatrixAction.getAssignActionValues();
			StringTokenizer str = new StringTokenizer(assignmentValue,"{\":}",false);
			while(str.hasMoreElements()){
				valueList.add(String.valueOf(str.nextElement()));
			}
			ObjectGraphTypes objectGraphTypes = ruleService.getObjectGraphTypes(Long.valueOf(valueList.get(0)));
			String ognl = objectGraphTypes.getObjectGraph();
			if(StringUtils.isNotEmpty(ognl) && ognl.contains(AssignmentConstants.INDEX_REPLACEMENT)) {
				ognl = ognl.replace(AssignmentConstants.INDEX_REPLACEMENT, index.toString());
			}
			expression = ognl + " = '" + valueList.get(1) + "';";
		}
		return expression;
	}

	@Override
	public void compileMvelScriptRule(ScriptRule rule, Set<Parameter> parameters) {

		saveRuleRuntimeMapping(rule.getScriptCode(), null, rule, parameters, null, false);
	}

	@Override
	public Serializable compileRuleLevelExp(String expression) {

		Serializable compiledExpression = null;

		try {
			compiledExpression = MVEL.compileExpression(expression);
		} catch (Exception e) {
			BaseLoggers.flowLogger.debug("Error in compiling expression :: " + expression);
		}
		return compiledExpression;
	}

	@Override
	public void loadAndCompileRuleGroupExpression() {
		NamedQueryExecutor<RuleGroup> executor = new NamedQueryExecutor<RuleGroup>("Rules.AllApprovedRuleGroup");
		List<RuleGroup> ruleGroupList = entityDao.executeQuery(executor);

		if (null != ruleGroupList && ruleGroupList.size() > 0) {

			for (RuleGroup ruleGroup : ruleGroupList) {
                String ruleGroupNameIdExp = ruleExpressionBuilder.buildRuleLevelRuleExpression(ruleGroup
                        .getRuleGroupExpression());
				ruleGroup.setRuleLevelCompiledExpr(compileRuleLevelExp(ruleGroupNameIdExp));
				entityDao.persist(ruleGroup);
			}
		}

	}

	@Override
	public void getAssigmentActionForCompilation() {
		NamedQueryExecutor<AssignmentAction> ruleExecutor = new NamedQueryExecutor<AssignmentAction>(
				"Rules.AllAssignmentAction");
		List<AssignmentAction> assignmentActionList = entityDao.executeQuery(ruleExecutor);

		if (null != assignmentActionList && assignmentActionList.size() > 0) {
			for (AssignmentAction assignmentAction : assignmentActionList) {
				assignmentAction = buildExpressionForAssignmentActions(assignmentAction);
				entityDao.saveOrUpdate(assignmentAction);
			}
		}

	}

	@Override
	public void getAssigmentMatrixActionForCompilation() {

        List<AssignmentMaster> assignmentMasters = baseMasterService.getAllApprovedAndActiveEntities(AssignmentMaster.class);

        if (CollectionUtils.isNotEmpty(assignmentMasters)) {

            for (AssignmentMaster assignmentMaster : assignmentMasters) {
                List<AssignmentSet> assignmentSets = assignmentMaster.getAssignmentSet();

                if (CollectionUtils.isNotEmpty(assignmentSets)) {

                    for (AssignmentSet assignmentSet : assignmentSets) {
                        List<AssignmentMatrixRowData> assignmentMatrixRowDatas = assignmentSet.getAssignmentMatrixRowData();

						if (CollectionUtils.isNotEmpty(assignmentMatrixRowDatas)) {

							for (AssignmentMatrixRowData matrixRowData : assignmentMatrixRowDatas) {

                                AssignmentMatrixAction assignmentMatrixAction = matrixRowData.getAssignmentMatrixAction();

								if (assignmentMatrixAction.getCompiledExpression() == null) {

									BaseLoggers.flowLogger.debug("AssignmentSet is :: "
											+ (null != assignmentSet ? assignmentSet.getId() : null)
											+ " for AssignMatrix Action :: " + assignmentMatrixAction.getId());

									assignmentMatrixService.compileAndSaveScript(assignmentMatrixAction,
											assignmentSet.getAssignmentActionFieldMetaDataList(), null);
									entityDao.persist(assignmentMatrixAction);
								}
							}
						}
					}
				}

			}
		}
	}

}
