package com.nucleus.core.dynamicQuery.support;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.entity.QueryTokenValue;
import com.nucleus.core.dynamicQuery.service.DynamicQueryTranslatorService;
import com.nucleus.core.exceptions.SystemException;

public class ExpressionTransformVisiter extends AbstractExpressionVisiter {

    private static final Logger           LOGGER                          = LoggerFactory
                                                                                  .getLogger(ExpressionTransformVisiter.class);

    private static final String           HQL_NAMED_PARAMETER_PREFIX      = ":";

    private PlainSelect                   plianSelect;

    private ArrayDeque<Expression>        expressions                     = new ArrayDeque<Expression>();

    private Set<QueryToken>               processedTokens                 = new HashSet<QueryToken>();
    private List<QueryToken>              queryTokens;

    private QueryContext                  context;
    private DynamicQueryTranslatorService service                         = null;

    private Map<String, Object>           hqlQueryParameters              = new HashMap<String, Object>();

    // for running report as job we need to resolve date-time parameters later
    private Map<String, String>           hqlQueryParametersUnresolved    = new HashMap<String, String>();

    private String                        CROSS_TOKEN_CONVERT_EXP_MESSAGE = "Unable to convert cross token (%s) to a valid expression";

    @SuppressWarnings("rawtypes")
    public ExpressionTransformVisiter(DynamicQueryTranslatorService service, PlainSelect plianSelect,
            QueryContext queryContext, List<QueryToken> queryTokens) {
        super();
        this.plianSelect = plianSelect;
        this.queryTokens = queryTokens;
        this.context = queryContext;
        this.service = service;
        // set from root entity and empty join list
        plianSelect.setJoins(new ArrayList());
        Table item = new Table();
        item.setName(queryContext.getRootEntity());
        item.setAlias(queryContext.getRootEntityAlias());
        plianSelect.setFromItem(item);
        expressions.add(plianSelect.getWhere());
    }

    // ===============================LOGICAL(MAIN EXP)
    @Override
    public void visit(Parenthesis parenthesis) {
        LOGGER.debug("parenthesis:---->" + parenthesis);
        expressions.add(parenthesis);
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(AndExpression andExpression) {

        LOGGER.debug("andExpression:---->" + andExpression);
        expressions.add(andExpression);
        andExpression.getLeftExpression().accept(this);
        andExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(OrExpression orExpression) {
        LOGGER.debug("orExpression:---->" + orExpression);
        expressions.add(orExpression);
        orExpression.getLeftExpression().accept(this);
        orExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        LOGGER.debug("equalsTo:---->" + equalsTo);
        if (changeExpressionIfRequired(equalsTo)) {
            return;
        }
        expressions.add(equalsTo);
        equalsTo.getLeftExpression().accept(this);
        equalsTo.getRightExpression().accept(this);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        LOGGER.debug("notEqualsTo:---->" + notEqualsTo);

        if (changeExpressionIfRequired(notEqualsTo)) {
            return;
        }

        expressions.add(notEqualsTo);
        notEqualsTo.getLeftExpression().accept(this);
        notEqualsTo.getRightExpression().accept(this);
    }

    @Override
    public void visit(GreaterThan greaterThan) {

        LOGGER.debug("greaterThan:---->" + greaterThan);

        if (changeExpressionIfRequired(greaterThan)) {
            return;
        }

        expressions.add(greaterThan);
        greaterThan.getLeftExpression().accept(this);
        greaterThan.getRightExpression().accept(this);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

        LOGGER.debug("greaterThanEquals:---->" + greaterThanEquals);

        if (changeExpressionIfRequired(greaterThanEquals)) {
            return;
        }
        expressions.add(greaterThanEquals);
        greaterThanEquals.getLeftExpression().accept(this);
        greaterThanEquals.getRightExpression().accept(this);
    }

    @Override
    public void visit(MinorThan minorThan) {

        LOGGER.debug("minorThan:---->" + minorThan);

        if (changeExpressionIfRequired(minorThan)) {
            return;
        }

        expressions.add(minorThan);
        minorThan.getLeftExpression().accept(this);
        minorThan.getRightExpression().accept(this);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {

        LOGGER.debug("minorThanEquals:---->" + minorThanEquals);

        if (changeExpressionIfRequired(minorThanEquals)) {
            return;
        }
        minorThanEquals.getLeftExpression().accept(this);
        minorThanEquals.getRightExpression().accept(this);
    }

    @Override
    public void visit(Between between) {
        LOGGER.debug("between:---->" + between);
        if (between.getLeftExpression() instanceof Column) {
            QueryToken queryToken = getPropertyForToken(((Column) between.getLeftExpression()).getColumnName());
            if ((between.getBetweenExpressionStart() instanceof StringValue && between.getBetweenExpressionEnd() instanceof StringValue)) {
                StringValue stringValue = (StringValue) between.getBetweenExpressionStart();
                between.setBetweenExpressionStart(getNewValue(stringValue, queryToken));
                StringValue stringValue2 = (StringValue) between.getBetweenExpressionEnd();
                between.setBetweenExpressionEnd(getNewValue(stringValue2, queryToken));
            }
            // must convert any dynamic tokens
            if (queryToken.isCrossToken()) {
                if (service.getConversionService().convertExpression(expressions, between, context, queryToken, plianSelect)) {
                    return;
                }
                throw new IllegalStateException(String.format(CROSS_TOKEN_CONVERT_EXP_MESSAGE, queryToken));
            }
        }
        expressions.add(between);
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(InExpression inExpression) {
        LOGGER.debug("inExpression:---->" + inExpression);

        if (inExpression.getLeftExpression() instanceof Column) {

            QueryToken queryToken = getPropertyForToken(((Column) inExpression.getLeftExpression()).getColumnName());
            inExpression.getItemsList().accept(new DynamicQueryItemListVisiter(this, queryToken));

            // must convert any dynamic tokens
            if (queryToken.isCrossToken()) {
                if (service.getConversionService().convertExpression(expressions, inExpression, context, queryToken,
                        plianSelect)) {
                    return;
                }
                throw new IllegalStateException(String.format(CROSS_TOKEN_CONVERT_EXP_MESSAGE, queryToken));
            }

        }
        expressions.add(inExpression);
        inExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

        LOGGER.debug("isNullExpression:---->" + isNullExpression);

        if (isNullExpression.getLeftExpression() instanceof Column) {
            QueryToken queryToken = getPropertyForToken(((Column) isNullExpression.getLeftExpression()).getColumnName());
            // must convert any dynamic tokens
            if (queryToken.isCrossToken()) {
                if (service.getConversionService().convertExpression(expressions, isNullExpression, context, queryToken,
                        plianSelect)) {
                    return;
                }
                throw new IllegalStateException(String.format(CROSS_TOKEN_CONVERT_EXP_MESSAGE, queryToken));
            }
        }
        expressions.add(isNullExpression);
        isNullExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Column tableColumn) {

        LOGGER.debug("tableColumn:---->" + tableColumn);
        QueryToken queryToken = getPropertyForToken(tableColumn.getColumnName());
        tableColumn.setColumnName(queryToken.getOwner().getAlias() + "." + queryToken.getTokenProertyName());
        // create join for this token
        List<QueryToken> ownerList = ExpressionHelper.getEligiblePropertiesForJoin(queryToken, context, processedTokens);
        ExpressionHelper.createJoins(ownerList, plianSelect);
    }

    // OTHER
    // HELPERSSSSSSSSSSSSSSSSSSS=================================================================================================

    public QueryToken getPropertyForToken(String token) {

        for (QueryToken queryToken : queryTokens) {
            if (queryToken.getTokenName() != null && queryToken.getTokenName().equals(token)) {
                return queryToken;
            }
        }
        return null;

    }

    private boolean changeExpressionIfRequired(BinaryExpression binaryExpression) {

        if (binaryExpression.getLeftExpression() instanceof Column) {

            QueryToken queryToken = getPropertyForToken(((Column) binaryExpression.getLeftExpression()).getColumnName());

            // convert display value to actual value
            if ((binaryExpression.getRightExpression() instanceof StringValue)) {
                StringValue stringValue = (StringValue) binaryExpression.getRightExpression();
                binaryExpression.setRightExpression(getNewValue(stringValue, queryToken));
            }

            // must convert any dynamic tokens
            if (queryToken.isCrossToken()) {
                if (service.getConversionService().convertExpression(expressions, binaryExpression, context, queryToken,
                        plianSelect)) {
                    return true;
                }
                throw new IllegalStateException(String.format(CROSS_TOKEN_CONVERT_EXP_MESSAGE, queryToken));
            }

        }
        return false;
    }

    public Expression getNewValue(StringValue stringValue, QueryToken queryToken) {

        if (queryToken.getValueActualType().equalsIgnoreCase(QueryToken.DATE_TIME)
                || queryToken.getValueActualType().equalsIgnoreCase(QueryToken.LOCAL_DATE)
                || queryToken.getValueActualType().equalsIgnoreCase(QueryToken.CALENDAR)) {
            Function function = new Function();
            String givenVal = stringValue.getValue();
            String paramName = StringUtils.substringBefore(givenVal, "(").concat(
                    String.valueOf(hqlQueryParameters.size() + 1));
            givenVal = queryToken.getValueActualType().concat(":").concat(givenVal);
            hqlQueryParameters.put(paramName, service.getMetadataService().getDateTimeValueForTokenValue(givenVal));
            hqlQueryParametersUnresolved.put(paramName, givenVal);
            function.setName(HQL_NAMED_PARAMETER_PREFIX.concat(paramName));
            return function;
        }
        if (queryToken.getValueActualType().equalsIgnoreCase(QueryToken.BOOLEAN)) {
            Function function = new Function();
            String givenVal = stringValue.getValue();
            // 'TRUE'==>TRUE and 'FALSE'==>FALSE(unquoted boolean literals)
            function.setName(givenVal);
            return function;
        }

        if (queryToken.isFetchValues()) {
            return stringValue;
        } else {
            String newValue = null;
            for (QueryTokenValue queryTokenValue : queryToken.getQueryTokenValues()) {
                if (queryTokenValue.getDisplayName().equalsIgnoreCase(stringValue.getValue())) {
                    newValue = queryTokenValue.getActualValue();
                    break;
                }
            }
            if (queryToken.getValueActualType().equalsIgnoreCase(QueryToken.STRING)) {
                return new StringValue("'".concat(newValue).concat("'"));
            } else if (queryToken.getValueActualType().equalsIgnoreCase(QueryToken.NUMBER)) {
                return new LongValue(newValue);
            } else if (queryToken.getValueActualType().equalsIgnoreCase(QueryToken.FLOAT)
                    || queryToken.getValueActualType().equalsIgnoreCase(QueryToken.MONEY)) {
                return new DoubleValue(newValue);
            } else if (queryToken.getValueActualType().equalsIgnoreCase(QueryToken.BOOLEAN)) {
                return new StringValue("'".concat(newValue).concat("'"));
            }
        }

        throw new SystemException("No actual value found for token:" + queryToken.getDisplayName() + " with display value:"
                + stringValue.getValue());

    }

    public Map<String, Object> getHqlQueryParameters() {
        return hqlQueryParameters;
    }

    public Set<QueryToken> getProcessedTokens() {
        return processedTokens;
    }

    public Map<String, String> getHqlQueryParametersUnresolved() {
        return hqlQueryParametersUnresolved;
    }

}
