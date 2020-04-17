package com.nucleus.core.dynamicQuery.support;

import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.InverseExpression;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.statement.select.SubSelect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleus.core.exceptions.InvalidDataException;

public abstract class AbstractExpressionVisiter implements ExpressionVisitor {

    private static final Logger LOGGER                 = LoggerFactory.getLogger(AbstractExpressionVisiter.class);

    private static final String UNEXPECTED_EXP_MESSAGE = "Unexpected expression type %s (%s) found in Dyanmic query";

    @Override
    public void visit(InverseExpression inverseExpression) {
        LOGGER.debug("inverseExpression" + inverseExpression);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "InverseExpression", inverseExpression));
    }

    // A '?' in a statement
    @Override
    public void visit(JdbcParameter jdbcParameter) {
        LOGGER.debug("jdbcParameter" + jdbcParameter);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "JdbcParameter", jdbcParameter));
    }

    /**
     * A Date in the form {d 'yyyy-mm-dd'}
     */
    @Override
    public void visit(DateValue dateValue) {
        LOGGER.debug("dateValue:---->" + dateValue);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "DateValue", dateValue));
    }

    /**
     * A Time in the form {t 'hh:mm:ss'}
     */
    @Override
    public void visit(TimeValue timeValue) {
        LOGGER.debug("dateValue:---->" + timeValue);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "TimeValue", timeValue));
    }

    @Override
    public void visit(TimestampValue timestampValue) {
        LOGGER.debug("timestampValue:---->" + timestampValue);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "TimestampValue", timestampValue));
    }

    // =================VALUE-TYPE-EXPRESSIONS

    @Override
    public void visit(Function function) {
        // is used to create dateTime hql params
        LOGGER.debug("function" + function);
    }

    @Override
    public void visit(NullValue nullValue) {
        LOGGER.debug("nullValue" + nullValue);
    }

    @Override
    public void visit(DoubleValue doubleValue) {
        LOGGER.debug("doubleValue:--->" + doubleValue);
    }

    @Override
    public void visit(LongValue longValue) {
        LOGGER.debug("longValue:---->" + longValue);
    }

    @Override
    public void visit(StringValue stringValue) {
        LOGGER.debug("stringValue:---->" + stringValue);
    }


    // =========NOT REQUIRED and not allowed/should throw exception
    @Override
    public void visit(LikeExpression likeExpression) {

        LOGGER.debug("likeExpression:---->" + likeExpression);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "LikeExpression", likeExpression));

    }

    @Override
    public void visit(SubSelect subSelect) {
        LOGGER.debug("subSelect:---->" + subSelect);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "subSelect", subSelect));
    }

    @Override
    public void visit(CaseExpression caseExpression) {

        LOGGER.debug("caseExpression:---->" + caseExpression);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "caseExpression", caseExpression));
    }

    @Override
    public void visit(WhenClause whenClause) {

        LOGGER.debug("whenClause:---->" + whenClause);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "whenClause", whenClause));
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        LOGGER.debug("existsExpression:---->" + existsExpression);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "existsExpression", existsExpression));
    }

    @Override
    public void visit(AllComparisonExpression allComparisonExpression) {
        LOGGER.debug("allComparisonExpression:---->" + allComparisonExpression);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "AllComparisonExpression",
                allComparisonExpression));
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {

        LOGGER.debug("anyComparisonExpression:---->" + anyComparisonExpression);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "anyComparisonExpression",
                anyComparisonExpression));
    }

    @Override
    public void visit(Concat concat) {
        LOGGER.debug("concat:---->" + concat);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "concat", concat));
    }

    @Override
    public void visit(Matches matches) {

        LOGGER.debug("matches:---->" + matches);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "matches", matches));
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {

        LOGGER.debug("bitwiseAnd:---->" + bitwiseAnd);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "BitwiseAnd", bitwiseAnd));
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {

        LOGGER.debug("bitwiseOr:---->" + bitwiseOr);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "bitwiseOr", bitwiseOr));
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {

        LOGGER.debug("bitwiseXor:---->" + bitwiseXor);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "bitwiseXor", bitwiseXor));
    }

    @Override
    public void visit(Addition addition) {
        LOGGER.debug("addition:---->" + addition);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "addition", addition));
    }

    @Override
    public void visit(Division division) {

        LOGGER.debug("division:---->" + division);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "division", division));
    }

    @Override
    public void visit(Multiplication multiplication) {
        LOGGER.debug("multiplication:---->" + multiplication);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "multiplication", multiplication));

    }

    @Override
    public void visit(Subtraction subtraction) {

        LOGGER.debug("multiplication:---->" + subtraction);
        throw new InvalidDataException(String.format(UNEXPECTED_EXP_MESSAGE, "subtraction", subtraction));
    }

}
