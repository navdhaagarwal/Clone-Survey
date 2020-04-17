package com.nucleus.download.master.rule;

import static com.nucleus.rules.service.RulesConverterUtility.commaDelimitesString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.web.common.controller.BaseController;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.EntityId;
import com.nucleus.persistence.DaoUtils;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.CompoundParameter;
import com.nucleus.rules.model.Condition;
import com.nucleus.rules.model.ConstantParameter;
import com.nucleus.rules.model.DerivedParamFilterCriteria;
import com.nucleus.rules.model.DerivedParameter;
import com.nucleus.rules.model.EntityType;
import com.nucleus.rules.model.ObjectGraphParameter;
import com.nucleus.rules.model.ObjectGraphTypes;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.ParameterType;
import com.nucleus.rules.model.PlaceHolderParameter;
import com.nucleus.rules.model.QueryParameter;
import com.nucleus.rules.model.QueryParameterAttribute;
import com.nucleus.rules.model.ReferenceParameter;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.RuleType;
import com.nucleus.rules.model.SQLParameter;
import com.nucleus.rules.model.SQLParameterMapping;
import com.nucleus.rules.model.SQLRule;
import com.nucleus.rules.model.SQLRuleParameterMapping;
import com.nucleus.rules.model.ScriptParameter;
import com.nucleus.rules.model.ScriptRule;
import com.nucleus.rules.service.ExpressionBuilder;
import com.nucleus.rules.service.ExpressionValidationConstants;
import com.nucleus.rules.service.ParameterService;
import com.nucleus.rules.service.RuleConstants;
import com.nucleus.rules.service.RuleService;
import com.nucleus.rules.service.SQLRuleExecutor;
import com.nucleus.web.common.controller.NonTransactionalBaseController;

@Component("ruleUploadDownloadUtility")
public class RuleUploadDownloadUtility extends BaseController {

	
	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	@Inject
	@Named(value = "expressionBuilder")
	private ExpressionBuilder expressionBuilder;

	@Inject
	@Named("ruleService")
	private RuleService ruleService;

	@Inject
	@Named("sQLRuleExecutor")
	SQLRuleExecutor sqlRuleExecutor;

	@Inject
	@Named("parameterService")
	private ParameterService parameterService;
	

	public String buildConditionLevelRuleExpression(String ruleExpression) {
		StringBuilder expression = new StringBuilder();
		if (StringUtils.isNotBlank(ruleExpression)) {
			String[] tokens = ruleExpression.split(" ");
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
							expression.append(condition.getName()).append(" ");
						}
					}
				}
			}
			if (expression.length() > 0) {
				return expression.toString();
			}
		}
		return "";
	}


	protected Map<String, Object> convertMvelScriptIdExpressionToNameExpression(String parameterExp) {
		parameterExp = parameterExp.trim().replaceAll("\\s+", " ").trim();
		String[] tokens = parameterExp.split(" ");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<String> invalidParameters = new ArrayList<String>();

		parameterExp = " " + parameterExp + " ";
		Parameter parameter = null;
		for (String token : tokens) {
			if (!RuleConstants.conditionOperatorsForMVELScript.contains(token)
					&& !Arrays.asList(ExpressionValidationConstants.SUPPORTED_CONDITION_JOIN_OPERATORS_MVEL_SCRIPT)
					.contains(token) && !RuleConstants.LEFT_PAREN.equals(token)
					&& !RuleConstants.RIGHT_PAREN.equals(token)) {

				parameter = ruleService.getParameter(Long.parseLong(token));
				if (parameter != null) {
					parameterExp = parameterExp.replace(" " + token + " ", " " + parameter.getName() + " ");
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


		public void createHeaderForCondition(Row header){

			int index = 0;
			header.createCell(index++).setCellValue("IDENTIFIER");
			header.createCell(index++).setCellValue("Code");
			header.createCell(index++).setCellValue("Name");
			header.createCell(index++).setCellValue("Description");
			header.createCell(index++).setCellValue("Source Product");
			header.createCell(index++).setCellValue("Criteria Condition Flag");
			header.createCell(index++).setCellValue("Condition Expression");
			header.createCell(index++).setCellValue("Module Name");
			header.createCell(index++).setCellValue("Upload OperationType");
		}

        public void createHeaderForParameter(Row header){

            int index = 0;
            header.createCell(index++).setCellValue("IDENTIFIER");
            header.createCell(index++).setCellValue("Code");
            header.createCell(index++).setCellValue("Name");
            header.createCell(index++).setCellValue("Description");
            header.createCell(index++).setCellValue("Data Type");
            header.createCell(index++).setCellValue("Parameter Type");
            header.createCell(index++).setCellValue("Source Product");
            header.createCell(index++).setCellValue("Collection Based");
            header.createCell(index++).setCellValue("Module Name");
            header.createCell(index++).setCellValue("Object Graph");
            header.createCell(index++).setCellValue("Literal");
            header.createCell(index++).setCellValue("Entity Type");
            header.createCell(index++).setCellValue("Reference");
            header.createCell(index++).setCellValue("Parameter Expression");
            header.createCell(index++).setCellValue("Query");
            header.createCell(index++).setCellValue("Context Name");
            header.createCell(index++).setCellValue("Script Code");
            header.createCell(index++).setCellValue("Script Code Type");
            header.createCell(index++).setCellValue("Compiled Expression");
            header.createCell(index++).setCellValue("Aggregate Function");
            header.createCell(index++).setCellValue("Target Object Graph");
            header.createCell(index++).setCellValue("Entity Field");
            header.createCell(index++).setCellValue("Validate On All");
            header.createCell(index++).setCellValue("Sql Query");
            header.createCell(index++).setCellValue("Upload OperationType");
            header.createCell(index++).setCellValue("Query Parameter Name");
            header.createCell(index++).setCellValue("Query Object Graph");
            header.createCell(index++).setCellValue("Collection Name");
            header.createCell(index++).setCellValue("Where Expression");
            header.createCell(index++).setCellValue("Order Sequence");
            header.createCell(index++).setCellValue("Position");
            header.createCell(index++).setCellValue("PlaceHolder Key");
            header.createCell(index++).setCellValue("Parameter");
		}

		public void createHeaderForRule(Row header){
			int index = 0;
			header.createCell(index++).setCellValue("IDENTIFIER");
			header.createCell(index++).setCellValue("Code");
			header.createCell(index++).setCellValue("Name");
			header.createCell(index++).setCellValue("Description");
			header.createCell(index++).setCellValue("Source Product");
			header.createCell(index++).setCellValue("Module Name");
			header.createCell(index++).setCellValue("Rule Type");
			header.createCell(index++).setCellValue("Rule Expression");
			header.createCell(index++).setCellValue("Rule Tag Names");
			header.createCell(index++).setCellValue("Error Message");
			header.createCell(index++).setCellValue("Error Message Key");
			header.createCell(index++).setCellValue("Success Message");
			header.createCell(index++).setCellValue("Success Message Key");
			header.createCell(index++).setCellValue("Criteria Rule Flag");
			header.createCell(index++).setCellValue("Script Code");
			header.createCell(index++).setCellValue("Script Code Type");
			header.createCell(index++).setCellValue("Sql Query");
			header.createCell(index++).setCellValue("Upload OperationType");
			header.createCell(index++).setCellValue("Position");
			header.createCell(index++).setCellValue("PlaceHolder Key");
			header.createCell(index++).setCellValue("Parameter");

		}


	public void createDataForCondition(List<Condition> conditionList,Sheet sheet) throws Exception{

		int rowIndex = 1;
		for(Condition mappingList : conditionList) {
			Row row = sheet.createRow(rowIndex++);

			row.createCell(0).setCellValue("D");
			if (mappingList.getCode() != null) {
				row.createCell(1).setCellValue(mappingList.getCode());
			}
			if (mappingList.getName() != null) {
				row.createCell(2).setCellValue(mappingList.getName());
			}
			if (mappingList.getDescription() != null) {
				row.createCell(3).setCellValue(mappingList.getDescription());
			}
			if (mappingList.getSourceProduct() != null) {
				row.createCell(4).setCellValue(mappingList.getSourceProduct());
			}
			if (mappingList.isCriteriaConditionFlag()) {
				row.createCell(5).setCellValue(mappingList.isCriteriaConditionFlag());
			}else{
				row.createCell(5).setCellValue(mappingList.isCriteriaConditionFlag());
			}
			if (mappingList.getConditionExpression() != null) {
				String conditionExpression = buildConditionExpression(mappingList.getConditionExpression());
				row.createCell(6).setCellValue(conditionExpression);
			}
			if (mappingList.getModuleName() != null) {
				row.createCell(7).setCellValue(mappingList.getModuleName().getCode());
			}


		}

	}

	public void createDataForParameter(List<Long> masterIds,Sheet sheet) throws Exception{
		List<Parameter> parameterMappingMasterList= new ArrayList<>();
		if(CollectionUtils.isNotEmpty(masterIds)) {
			parameterMappingMasterList = getRecordsByMasterIdsforDownloadParam(masterIds);
		}else{
			NamedQueryExecutor<Parameter> executor = new NamedQueryExecutor<>("parameter.findParametersByApprovalCodes");
			parameterMappingMasterList=entityDao.executeQuery(executor);
		}
		if(CollectionUtils.isNotEmpty(parameterMappingMasterList)) {

			int rowIndex = 1;
			for (Parameter mappingList : parameterMappingMasterList) {
				Row row = sheet.createRow(rowIndex++);

				row.createCell(0).setCellValue("D");
				if (mappingList.getCode() != null) {
					row.createCell(1).setCellValue(mappingList.getCode());
				}
				if (mappingList.getName() != null) {
					row.createCell(2).setCellValue(mappingList.getName());
				}
				if (mappingList.getDescription() != null) {
					row.createCell(3).setCellValue(mappingList.getDescription());
				}
				if (mappingList.getDataType() != null) {
					row.createCell(4).setCellValue(mappingList.getDataType());
				}
				if (mappingList.getParamType() != null) {
					row.createCell(5).setCellValue(mappingList.getParamType());
				}
				if (mappingList.getSourceProduct() != null) {
					row.createCell(6).setCellValue(mappingList.getSourceProduct());
				}
				if (mappingList.isCollectionBased()) {
					row.createCell(7).setCellValue(mappingList.isCollectionBased());
				} else {
					row.createCell(7).setCellValue(mappingList.isCollectionBased());
				}
				if (mappingList.getModuleName() != null) {
					row.createCell(8).setCellValue(mappingList.getModuleName().getCode());
				}
				if (mappingList.getParamType() != null && mappingList.getParamType() == ParameterType.PARAMETER_TYPE_OBJECT_GRAPH) {
					if (((ObjectGraphParameter) mappingList).getObjectGraph() != null) {

						List<ObjectGraphTypes> objectGraphTypesList = null;
						if (mappingList.getSourceProduct() != null && mappingList.getModuleName() != null) {
							objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(mappingList.getSourceProduct(), mappingList.getModuleName().getId());
						} else {
							objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(mappingList.getSourceProduct());
						}
						if (CollectionUtils.isNotEmpty(objectGraphTypesList)) {
							for (ObjectGraphTypes objectGraphTypes : objectGraphTypesList) {
								if (objectGraphTypes.getObjectGraph().equals(((ObjectGraphParameter) mappingList).getObjectGraph())) {
									row.createCell(9).setCellValue(objectGraphTypes.getDisplayName());
								}
							}
						}
					}
				}
				if (mappingList.getParamType() != null && mappingList instanceof ConstantParameter) {
					if (((ConstantParameter) mappingList).getLiteral() != null) {
						row.createCell(10).setCellValue(((ConstantParameter) mappingList).getLiteral());
					}
				}
				if (mappingList.getParamType() != null && mappingList.getParamType() == ParameterType.PARAMETER_TYPE_REFERENCE) {
					EntityId en = null;
					try {
						en = ((ReferenceParameter) mappingList).getReferenceEntityId();
					} catch (Exception e) {

					}

					if (en != null) {

						EntityId entityId = ((ReferenceParameter) mappingList).getReferenceEntityId();
						EntityType entityType = ruleService.getEntityTypeData(entityId.getUri().substring(0, entityId.getUri().indexOf(":")));

						String fields = entityType.getFields();
						String entityName = ((ReferenceParameter) mappingList).getReferenceEntityId().getEntityClass().getSimpleName();
						String fieldName = fields.substring(0, fields.indexOf(","));
						Long id = ((ReferenceParameter) mappingList).getReferenceEntityId().getLocalId();
						String qlString = "select " + fieldName + " FROM " + entityName + " WHERE id=" + id;
						Query qry = entityDao.getEntityManager().createQuery(qlString);
						Object o = DaoUtils.executeQuery(entityDao.getEntityManager(), qry).get(0);

						row.createCell(11).setCellValue(entityType.getDisplayEntityName());
						row.createCell(12).setCellValue(o.toString());
					}
				}
				if (mappingList.getParamType() != null && mappingList.getParamType() == ParameterType.PARAMETER_TYPE_COMPOUND) {
					
					if (mappingList instanceof CompoundParameter && ((CompoundParameter) mappingList).getParameterExpression() != null) {
						String compoundExpression = buildParameterExpression(((CompoundParameter) mappingList)
								.getParameterExpression());
						row.createCell(13).setCellValue(compoundExpression);
					}
				}
				if (mappingList.getParamType() != null && mappingList.getParamType() == ParameterType.PARAMETER_TYPE_PLACEHOLDER) {
					if (((PlaceHolderParameter) mappingList).getContextName() != null) {
						row.createCell(15).setCellValue(((PlaceHolderParameter) mappingList).getContextName());
					} else if (((PlaceHolderParameter) mappingList).getObjectGraph() != null) {
						row.createCell(9).setCellValue(((PlaceHolderParameter) mappingList).getObjectGraph());
					}
				}
				if (mappingList.getParamType() != null && mappingList.getParamType() == ParameterType.PARAMETER_TYPE_SCRIPT) {
					if (((ScriptParameter) mappingList).getScriptCode() != null) {
						String scriptCodeValue = null;
						try {
							scriptCodeValue = ruleService.decryptScriptCode((ScriptParameter) mappingList).getScriptCodeValue();
						} catch (Exception e) {
							scriptCodeValue = "";
						}
						row.createCell(16).setCellValue(scriptCodeValue);
						if (((ScriptParameter) mappingList).getScriptCodeType() != null) {
							row.createCell(17).setCellValue(((ScriptParameter) mappingList).getScriptCodeType());
						}

					}
				}

				if (mappingList.getParamType() != null && mappingList.getParamType() == ParameterType.PARAMETER_TYPE_QUERY) {
					if (((QueryParameter) mappingList).getQuery() != null) {
						row.createCell(14).setCellValue(((QueryParameter) mappingList).getQuery());
					}
				}

				if (mappingList.getParamType() != null && mappingList.getParamType() == ParameterType.PARAMETER_TYPE_DERIVED) {

					if (((ScriptParameter) mappingList).getScriptCode() != null) {
						row.createCell(16).setCellValue(((ScriptParameter) mappingList).getScriptCode());
						row.createCell(17).setCellValue(((ScriptParameter) mappingList).getScriptCodeType());
					}
					if (((DerivedParameter) mappingList).getAggregateFunction() != null) {
						row.createCell(19).setCellValue(((DerivedParameter) mappingList).getAggregateFunction());
					}
					if (((DerivedParameter) mappingList).getTargetObjectGraph() != null) {
						Parameter parameter = baseMasterService.findById(Parameter.class, ((DerivedParameter) mappingList).getTargetObjectGraph().getId());
						row.createCell(20).setCellValue(parameter.getName());
					}
					if (((DerivedParameter) mappingList).getEntityField() != null) {
						row.createCell(21).setCellValue(((DerivedParameter) mappingList).getEntityField());
					}
					if (((DerivedParameter) mappingList).getValidateOnAll() != null) {
						row.createCell(22).setCellValue(((DerivedParameter) mappingList).getValidateOnAll());
					}

				}

				if (mappingList instanceof QueryParameter) {

					List<QueryParameterAttribute> queryParameterAttributeList = ((QueryParameter) mappingList).getQueryParameterAttributes();

					for (QueryParameterAttribute q : queryParameterAttributeList) {
						row = sheet.createRow(rowIndex++);
						row.createCell(0).setCellValue("C");
						if (q.getQueryParameterName() != null) {
							row.createCell(25).setCellValue(q.getQueryParameterName());
						}
						if (q.getObjectGraph() != null) {
							List<ObjectGraphTypes> objectGraphTypesList = null;
							if (mappingList.getSourceProduct() != null && mappingList.getModuleName() != null) {
								objectGraphTypesList = ruleService.getOgnlBySourceProductAndModule(mappingList.getSourceProduct(), mappingList.getModuleName().getId());
							} else {
								objectGraphTypesList = ruleService.getApprovedObjectGraphBySourceProduct(mappingList.getSourceProduct());
							}
							if (CollectionUtils.isNotEmpty(objectGraphTypesList)) {
								for (ObjectGraphTypes objectGraphTypes : objectGraphTypesList) {
									if (objectGraphTypes.getObjectGraph().equals(q.getObjectGraph())) {
										row.createCell(26).setCellValue(objectGraphTypes.getDisplayName());
									}
								}
							}
						}
					}
				}
				if (mappingList instanceof DerivedParameter) {
					List<Long> id = new ArrayList<>();
					id.add(mappingList.getId());
					List<Parameter> derivedParameters = getRecordsByMasterIdsforDownloadDerivedParam(id);
					for (Parameter parameter : derivedParameters) {
						if (parameter instanceof DerivedParameter) {
							Hibernate.initialize(((DerivedParameter) parameter).getFilterCriterias());
							List<DerivedParamFilterCriteria> derivedParamFilterCriterias = ((DerivedParameter) parameter).getFilterCriterias();

							for (DerivedParamFilterCriteria filterCriteria : derivedParamFilterCriterias) {
								row = sheet.createRow(rowIndex++);
								row.createCell(0).setCellValue("C");
								if (filterCriteria.getCollectionName() != null) {
									row.createCell(27).setCellValue(filterCriteria.getCollectionName());
								}
								if (filterCriteria.getWhereExpression() != null) {
									Map<String, Object> resultMap = convertMvelScriptIdExpressionToNameExpression(filterCriteria
											.getWhereExpression());
									filterCriteria.setWhereExpressionInName((String) resultMap.get("parameterExp"));
									row.createCell(28).setCellValue(filterCriteria.getWhereExpressionInName());
								}
								if (filterCriteria.getOrderSequence() != null) {
									row.createCell(29).setCellValue(filterCriteria.getOrderSequence());
								}
							}
						}

					}
				}

				if (mappingList instanceof SQLParameter) {
					SQLParameter sqlParameter = (SQLParameter) mappingList;
					parameterService.decryptSQLParam(sqlParameter);
					String sqlQuery = sqlParameter.getSqlQueryPlain();
					if (sqlQuery != null && !sqlQuery.isEmpty()) {
						row.createCell(23).setCellValue(sqlQuery);
					}

					List<SQLParameterMapping> sqlParameterMappings = sqlParameter.getParamMapping();
					if (CollectionUtils.isNotEmpty(sqlParameterMappings)) {

						for (SQLParameterMapping sqlParameterMapping : sqlParameterMappings) {
							row = sheet.createRow(rowIndex++);
							row.createCell(0).setCellValue("C");
							if (sqlParameterMapping.getSeq() != null) {
								row.createCell(30).setCellValue(sqlParameterMapping.getSeq());
							}
							if (sqlParameterMapping.getPlaceHolderName() != null && !sqlParameterMapping.getPlaceHolderName().isEmpty()) {
								row.createCell(31).setCellValue(sqlParameterMapping.getPlaceHolderName());
							}
							if (sqlParameterMapping.getParameter() != null && sqlParameterMapping.getParameter().getCode() != null && !sqlParameterMapping.getParameter().getCode().isEmpty()) {
								row.createCell(32).setCellValue(sqlParameterMapping.getParameter().getCode());
							}
						}
					}
				}
			}
		}

		}


		public void createDataForRule(List<Long> masterIds,Sheet sheet)throws Exception{
			List<Rule> ruleMappingMasterList= new ArrayList<>();
		    if(CollectionUtils.isNotEmpty(masterIds)) {
				ruleMappingMasterList = getRecordsByMasterIdsforDownloadRule(masterIds);
			}else{
				NamedQueryExecutor<Rule> executor = new NamedQueryExecutor<>("rule.findRulesByApprovalCodes");
				ruleMappingMasterList=entityDao.executeQuery(executor);
			}
			if(CollectionUtils.isNotEmpty(ruleMappingMasterList)){
			int rowIndex = 1;
			for(Rule mappingList : ruleMappingMasterList) {
				Row row = sheet.createRow(rowIndex++);

				row.createCell(0).setCellValue("D");
				if (mappingList.getCode() != null) {
					row.createCell(1).setCellValue(mappingList.getCode());
				}
				if (mappingList.getName() != null) {
					row.createCell(2).setCellValue(mappingList.getName());
				}
				if (mappingList.getDescription() != null) {
					row.createCell(3).setCellValue(mappingList.getDescription());
				}
				if (mappingList.getSourceProduct() != null) {
					row.createCell(4).setCellValue(mappingList.getSourceProduct());
				}
				if (mappingList.getModuleName() != null) {
					row.createCell(5).setCellValue(mappingList.getModuleName().getCode());
				}
				if (mappingList.getRuleType() != null) {
					row.createCell(6).setCellValue(mappingList.getRuleType());
				}
				if (mappingList.getRuleTagNames() != null) {
					List<String> ruleTagNames = mappingList.getRuleTagNames();
					StringBuilder s = new StringBuilder();
					for (String name : ruleTagNames) {
						s.append(name + " ");
					}
					row.createCell(8).setCellValue(s.toString());
				}
				if (mappingList.getErrorMessage() != null) {
					row.createCell(9).setCellValue(mappingList.getErrorMessage());
				}
				if (mappingList.getErrorMessageKey() != null) {
					row.createCell(10).setCellValue(mappingList.getErrorMessageKey());
				}
				if (mappingList.getSuccessMessage() != null) {
					row.createCell(11).setCellValue(mappingList.getSuccessMessage());
				}
				if (mappingList.getSuccessMessageKey() != null) {
					row.createCell(12).setCellValue(mappingList.getSuccessMessageKey());
				}
				if (mappingList.isCriteriaRuleFlag()) {
					row.createCell(13).setCellValue(mappingList.isCriteriaRuleFlag());
				} else {
					row.createCell(13).setCellValue(mappingList.isCriteriaRuleFlag());
				}
				if (mappingList.getRuleType() != null && mappingList.getRuleType() == RuleType.RULE_TYPE_SCRIPT_BASED) {
					if (((ScriptRule) mappingList).getScriptCode() != null) {
						String scriptCodeValue = ruleService.decryptScriptCode((ScriptRule) mappingList).getScriptCodeValue();
						row.createCell(14).setCellValue(scriptCodeValue);
						row.createCell(15).setCellValue(((ScriptRule) mappingList).getScriptCodeType());
					}
				}
				if (mappingList.getRuleType() != null && mappingList.getRuleType() == RuleType.RULE_TYPE_SQL_BASED) {
					if (((SQLRule) mappingList).getSqlQuery() != null) {
						String sqlQuery = ruleService.decryptSQLRule((SQLRule) mappingList).getSqlQueryPlain();
						row.createCell(16).setCellValue(sqlQuery);
					}

				}
				if (mappingList.getRuleType() != null && mappingList.getRuleType() == RuleType.RULE_TYPE_EXPRESSION_BASED) {
					if (mappingList.getRuleExpression() != null) {
						String ruleExpression = buildConditionLevelRuleExpression(mappingList.getRuleExpression());
						row.createCell(7).setCellValue(ruleExpression);
					}
				}
				if (mappingList instanceof SQLRule) {

					List<Long> id = new ArrayList<>();
					id.add(mappingList.getId());

					List<Rule> sqlRule = getRecordsByMasterIdsforDownloadSQLRule(id);
					for (Rule rule : sqlRule) {
						if (rule instanceof SQLRule) {
							Hibernate.initialize(((SQLRule) rule).getParamMapping());
							List<SQLRuleParameterMapping> sqlRuleParameterMappingList = (((SQLRule) rule).getParamMapping());

							for (SQLRuleParameterMapping sqlRuleParameterMapping : sqlRuleParameterMappingList) {
								row = sheet.createRow(rowIndex++);
								row.createCell(0).setCellValue("C");
								if (sqlRuleParameterMapping.getSeq() != null) {
									row.createCell(18).setCellValue(sqlRuleParameterMapping.getSeq());
								}
								if (sqlRuleParameterMapping.getPlaceHolderName() != null) {
									row.createCell(19).setCellValue(sqlRuleParameterMapping.getPlaceHolderName());
								}
								if (sqlRuleParameterMapping.getParameter() != null) {
									row.createCell(20).setCellValue(sqlRuleParameterMapping.getParameter().getCode());
								}
							}
						}
					}

				}


			}
			}

		}


		
		public String buildConditionExpression(String conditionExpression) {
			StringBuilder expression = new StringBuilder();

			if (StringUtils.isNotBlank(conditionExpression)) {
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
								expression.append(parameter.getName()).append(" ");
							}
						}
					}
				}
				if (expression.length() > 0) {
					return expression.toString();
				}
			}
			return "";
		}
		
		
		public String buildParameterExpression(String parameterExpression) {
			StringBuilder expression = new StringBuilder();
			if (StringUtils.isNotBlank(parameterExpression)) {

				String[] tokens = parameterExpression.split(" ");
				if (tokens != null && tokens.length > 0) {
					for (String token : tokens) {
						token = token.trim();
						// if token is bracket and operator
						if (token.equals(RuleConstants.LEFT_PAREN) || token.equals(RuleConstants.RIGHT_PAREN)
								|| commaDelimitesString(ExpressionValidationConstants.ARITHMETIC_OPS).indexOf(token) != -1) {
							expression.append(token).append(" ");
						} else {
							Parameter parameter = entityDao.find(Parameter.class, Long.parseLong(token));
							if (parameter != null) {
								expression.append(parameter.getName()).append(" ");
							}
						}
					}
				}
				if (expression.length() > 0) {
					return expression.toString();
				}
			}
			return "";
		}

	public List<Condition> getRecordsByMasterIdsforDownloadCond(List<Long> masterIds){
		NamedQueryExecutor<Condition> executorr = new NamedQueryExecutor<Condition>(
				"ConditionMaster.getRecordsByMasterIdsforDownload").addParameter(
				"conditionChildIdsList",masterIds);
		List<Condition> mappingList=entityDao.executeQuery(executorr);
		return mappingList;

	}

	public List<Parameter> getRecordsByMasterIdsforDownloadParam(List<Long> masterIds){
			NamedQueryExecutor<Parameter> executorr = new NamedQueryExecutor<Parameter>(
					"ParameterMaster.getRecordsByMasterIdsforDownload").addParameter(
					"parameterChildIdsList",masterIds);
			List<Parameter> mappingList=entityDao.executeQuery(executorr);
			return mappingList;

		}

		public List<Parameter> getRecordsByMasterIdsforDownloadDerivedParam(List<Long> masterIds){
			NamedQueryExecutor<Parameter> executorr = new NamedQueryExecutor<Parameter>(
					"ParameterDerivedMaster.getRecordsByMasterIdsforDownload").addParameter(
					"parameterChildIdsList",masterIds);
			List<Parameter> mappingList=entityDao.executeQuery(executorr);
			return mappingList;

		}



		public List<Rule> getRecordsByMasterIdsforDownloadRule(List<Long> masterIds){
			NamedQueryExecutor<Rule> executorr = new NamedQueryExecutor<Rule>(
					"RuleMaster.getRecordsByMasterIdsforDownload").addParameter(
					"ruleChildIdsList",masterIds);
			List<Rule> mappingList=entityDao.executeQuery(executorr);
			return mappingList;

		}

		public List<Rule> getRecordsByMasterIdsforDownloadSQLRule(List<Long> masterIds){
			NamedQueryExecutor<Rule> executorr = new NamedQueryExecutor<Rule>(
					"RuleSQLMaster.getRecordsByMasterIdsforDownload").addParameter(
					"ruleChildIdsList",masterIds);
			List<Rule> mappingList=entityDao.executeQuery(executorr);
			return mappingList;

		}

}
