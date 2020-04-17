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
package com.nucleus.core.dynamicQuery.service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.support.DynamicQueryWrapper;
import com.nucleus.core.dynamicQuery.support.ExpressionHelper;
import com.nucleus.core.dynamicQuery.support.ExpressionTransformVisiter;
import com.nucleus.core.dynamicQuery.support.MapSelectExpressionItem;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Named("dynamicQueryTranslatorService")
@MonitoredWithSpring(name = "dynamicQueryTranslatorService_IMPL_")
public class DynamicQueryTranslatorServiceImpl implements DynamicQueryTranslatorService {

    @Inject
    @Named(value = "entityDao")
    EntityDao                   entityDao;

    @Inject
    @Named(value = "expressionConversionService")
    ExpressionConversionService conversionService;

    @Inject
    @Named(value = "dynamicQueryMetadataService")
    DynamicQueryMetadataService dynamicQueryMetadataService;

    @SuppressWarnings("unchecked")
    @Override
    public DynamicQueryWrapper processQuery(String whereClause, QueryContext queryContext, List<Long> selectItemIds,
            boolean selectIdColumnByDefault) {

        String nql = dynamicQueryMetadataService.getBaseQuery().concat(whereClause);

        List<QueryToken> tokens = dynamicQueryMetadataService.findAllTokensByContextCode(queryContext.getQueryCode());
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Select select;
        try {
            select = (Select) parserManager.parse(new StringReader(nql));
        } catch (JSQLParserException e) {
            BaseLoggers.exceptionLogger.info("Error in parsing dynamic query: " + nql);
            throw new SystemException("Error in parsing dynamic query", e);
        }
        PlainSelect plain = (PlainSelect) select.getSelectBody();

        // it will process where and from clause(create joins)
        // TODO:pass join type also(inner,outer,cross)
        ExpressionTransformVisiter visiterImpl = new ExpressionTransformVisiter(this, plain, queryContext, tokens);
        plain.getWhere().accept(visiterImpl);
        Map<String, Object> hqlQueryParameters = visiterImpl.getHqlQueryParameters();
        Map<String, String> unresolvedHqlQueryParameters = visiterImpl.getHqlQueryParametersUnresolved();
        // it will post-process where clause to add any constraints specified in context
        postProcessPlainSelect(plain, queryContext, hqlQueryParameters, unresolvedHqlQueryParameters);

        // populate select clause.it also returns tokens selected in query
        Map<Long, QueryToken> selectedTokens = populateSelectClause(plain, queryContext, selectItemIds,
                visiterImpl.getProcessedTokens(), selectIdColumnByDefault);
        // set distinct to avoid duplicate rows
        plain.setDistinct(new Distinct());
        // populate dynamicQueryWrapper with raw HQL string
        DynamicQueryWrapper dynamicQueryWrapper = new DynamicQueryWrapper(plain.toString(), hqlQueryParameters,
                selectedTokens);
        dynamicQueryWrapper.setHqlQueryParametersUnresolved(unresolvedHqlQueryParameters);
        // populate dynamicQueryWrapper with Map HQL string
        plain.setSelectItems(Arrays.asList(new MapSelectExpressionItem(plain.getSelectItems())));
        dynamicQueryWrapper.setHqlMapQueryString(plain.toString());
        return dynamicQueryWrapper;
    }

    private Map<Long, QueryToken> populateSelectClause(PlainSelect plianSelect, QueryContext queryContext,
            List<Long> selectItemIds, Set<QueryToken> processedTokens, boolean selectIdColumnByDefault) {

        List<SelectExpressionItem> selectItems = new ArrayList<SelectExpressionItem>();

        Map<Long, QueryToken> selectedTokens = new HashMap<Long, QueryToken>();

        // if nothing is selected or asked explicitly select id of root entity
        if (selectItemIds == null || selectItemIds.isEmpty() || selectIdColumnByDefault) {

            SelectExpressionItem expressionItem = new SelectExpressionItem();
            // using Column as SelectExpressionItem's expression
            Column column = ExpressionHelper.getColumnWithTableAndColumn(queryContext.getRootEntityAlias(),
                    queryContext.getIdPropertyName());
            expressionItem.setExpression(column);
            expressionItem.setAlias(queryContext.getIdPropertyName());
            selectItems.add(expressionItem);
        }
        Map<Long, QueryToken> selectableTokens = new HashMap<Long, QueryToken>();
        if (selectItemIds != null && !selectItemIds.isEmpty()) {

            selectableTokens.putAll(dynamicQueryMetadataService.getIdTokenMapWithContextIdAndType(queryContext.getId(),
                    Arrays.asList(QueryToken.SELECT_TYPE, QueryToken.BOTH)));

            for (Long id : selectItemIds) {
                QueryToken queryToken = findToken(selectableTokens, id);
                selectedTokens.put(queryToken.getId(), queryToken);
                // 1. create select item for this token
                if (queryToken.isEntityTypeProperty()) {
                    SelectExpressionItem expressionItem = new SelectExpressionItem();
                    // using Column as SelectExpressionItem's expression
                    Column column = ExpressionHelper.getColumnWithTableAndColumn(null, queryToken.getAlias());
                    expressionItem.setExpression(column);
                    expressionItem.setAlias(queryToken.getAlias());
                    selectItems.add(expressionItem);
                } else if (queryToken.isToken()) {
                    SelectExpressionItem expressionItem = new SelectExpressionItem();
                    // using Column as SelectExpressionItem's expression
                    Column column = ExpressionHelper.getColumnWithTableAndColumn(queryToken.getOwner().getAlias(),
                            queryToken.getTokenProertyName());
                    expressionItem.setExpression(column);
                    expressionItem.setAlias(queryToken.getAlias());
                    selectItems.add(expressionItem);
                }

                // 2. create join for this token
                List<QueryToken> ownerList = ExpressionHelper.getEligiblePropertiesForJoin(queryToken, queryContext,
                        processedTokens);
                ExpressionHelper.createJoins(ownerList, plianSelect);

            }
        }
        plianSelect.setSelectItems(selectItems);
        return selectableTokens;
    }

    private QueryToken findToken(Map<Long, QueryToken> selectableTokens, Long id) {

        QueryToken queryToken = selectableTokens.get(id);

        if (queryToken != null) {
            if (queryToken.isCrossToken()) {
                throw new SystemException("Cross type token is not allowed in select clause for now.");
            }
            return queryToken;
        }
        throw new SystemException("No token of type SELECT_TYPE or BOTH found with id:" + id
                + ". Only tokens with specified types(0 or 2) are allowed in select clause.");

    }

    // add constraints specified in context
    private void postProcessPlainSelect(PlainSelect plain, QueryContext queryContext,
    		Map<String, Object> hqlQueryParameters, Map<String, String> unresolvedHqlQueryParameters) {

        if (queryContext.getConstraintPropertyPath() != null && !queryContext.getConstraintPropertyPath().isEmpty()) {

            String val = queryContext.getConstraintValue();
            String valType = queryContext.getConstraintValueType();

            if (StringUtils.isNotBlank(val) && StringUtils.isNotBlank(valType)) {
                Expression expression = plain.getWhere();
                Parenthesis parenthesis = new Parenthesis(expression);
                EqualsTo equalsTo = new EqualsTo();
                Column column = new Column();
                Table table = new Table();
                table.setName(queryContext.getRootEntityAlias());
                column.setTable(table);
                column.setColumnName(queryContext.getConstraintPropertyPath());
                equalsTo.setLeftExpression(column);

                Expression valExp = null;

                if (valType.equalsIgnoreCase(QueryToken.STRING)) {
                    valExp = new StringValue("'".concat(val).concat("'"));
                } else if (valType.equalsIgnoreCase(QueryToken.NUMBER)) {
                    valExp = new LongValue(val);
                } else if (valType.equalsIgnoreCase(QueryToken.FLOAT) || valType.equalsIgnoreCase(QueryToken.MONEY)) {
                    valExp = new DoubleValue(val);
                } else if (valType.equalsIgnoreCase(QueryToken.BOOLEAN)) {
                    valExp = new StringValue("'".concat(val).concat("'"));
                }  else if (valType.equalsIgnoreCase(QueryToken.DATE_TIME) || valType.equalsIgnoreCase(QueryToken.LOCAL_DATE)
                        || valType.equalsIgnoreCase(QueryToken.CALENDAR)) {
                    Function function = new Function();
                    String paramName = StringUtils.substringBefore(val, "(").concat(

                            String.valueOf(hqlQueryParameters.size() + 1));
                    String givenVal = valType.concat(":").concat(val);
                    hqlQueryParameters.put(paramName, getMetadataService().getDateTimeValueForTokenValue(givenVal));
                    unresolvedHqlQueryParameters.put(paramName, givenVal);
                    function.setName(paramName);
                    valExp = function;
                } else {

                    throw new SystemException("Invalid ConstraintValueType specified for queryContext:" + queryContext);
                }
                equalsTo.setRightExpression(valExp);
                AndExpression andExpression = new AndExpression(parenthesis, equalsTo);
                plain.setWhere(andExpression);

            } else {
                throw new SystemException("No ConstraintValueType or ConstraintValue specified for ConstraintPropertyPath:"
                        + queryContext.getConstraintPropertyPath());
            }
        }
    }

    @Override
    public ExpressionConversionService getConversionService() {
        return conversionService;
    }

    @Override
    public DynamicQueryMetadataService getMetadataService() {
        return dynamicQueryMetadataService;
    }

}
