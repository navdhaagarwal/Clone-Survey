package com.nucleus.core.dynamicQuery.support;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.Parenthesis;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SubSelect;

import com.nucleus.core.dynamicQuery.crossToken.CrossTokenValueResolver;
import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.service.DynamicQueryMetadataService;
import com.nucleus.core.exceptions.InvalidDataException;

public class ExpressionChangeVisiter extends AbstractExpressionVisiter {

    Expression                  oldExpression;
    DynamicQueryMetadataService metadataService;
    CrossTokenValueResolver     tokenValueResolver;
    QueryContext                context;
    QueryToken                  queryToken;

    private boolean             done;

    PlainSelect                 plianSelect;

    public ExpressionChangeVisiter(Expression oldExp, DynamicQueryMetadataService service,
            CrossTokenValueResolver tokenValueResolver, QueryContext context, QueryToken queryToken, PlainSelect plianSelect) {
        oldExpression = oldExp;
        metadataService = service;
        this.tokenValueResolver = tokenValueResolver;
        this.context = context;
        this.queryToken = queryToken;
        this.plianSelect = plianSelect;
    }

    private Expression getNewExp(Expression oldExp) {

        if (oldExp instanceof EqualsTo) {
            Set<Long> ids = tokenValueResolver.resolveEquals(queryToken, context,
                    ExpressionHelper.getStringValue(((EqualsTo) oldExp).getRightExpression()));
            return getNewExpression(ids, false);

        } else if (oldExp instanceof NotEqualsTo) {
            Set<Long> ids = tokenValueResolver.resolveEquals(queryToken, context,
                    ExpressionHelper.getStringValue(((NotEqualsTo) oldExp).getRightExpression()));
            return getNewExpression(ids, true);

        } else if (oldExp instanceof MinorThan) {
            Set<Long> ids = tokenValueResolver.resolveLessThan(queryToken, context,
                    ExpressionHelper.getStringValue(((MinorThan) oldExp).getRightExpression()));
            return getNewExpression(ids, false);

        } else if (oldExp instanceof MinorThanEquals) {
            Set<Long> ids = tokenValueResolver.resolveLessThanEquals(queryToken, context,
                    ExpressionHelper.getStringValue(((MinorThanEquals) oldExp).getRightExpression()));
            return getNewExpression(ids, false);

        } else if (oldExp instanceof GreaterThan) {
            Set<Long> ids = tokenValueResolver.resolveGreaterThan(queryToken, context,
                    ExpressionHelper.getStringValue(((GreaterThan) oldExp).getRightExpression()));
            return getNewExpression(ids, false);

        } else if (oldExp instanceof GreaterThanEquals) {
            Set<Long> ids = tokenValueResolver.resolveGreaterThanEquals(queryToken, context,
                    ExpressionHelper.getStringValue(((GreaterThanEquals) oldExp).getRightExpression()));
            return getNewExpression(ids, false);

        } else if (oldExp instanceof InExpression) {
            Set<Long> ids = tokenValueResolver.resolveIn(queryToken, context, getValuesFromInExp((InExpression) oldExp));
            return getNewExpression(ids, ((InExpression) oldExp).isNot());

        } else if (oldExp instanceof Between) {
            Between btwn = (Between) oldExp;
            Set<Long> ids = tokenValueResolver.resolveBetween(queryToken, context,
                    ExpressionHelper.getStringValue(btwn.getBetweenExpressionStart()),
                    ExpressionHelper.getStringValue(btwn.getBetweenExpressionEnd()));
            return getNewExpression(ids, btwn.isNot());

        } else if (oldExp instanceof IsNullExpression) {
            Set<Long> ids = tokenValueResolver.resolveIsNull(queryToken, context);
            return getNewExpression(ids, ((IsNullExpression) oldExp).isNot());
        }
        return null;
    }

    private Expression getNewExpression(Set<Long> ids, boolean negateExpression) {

        // common left hand expression
        Column column = new Column();
        Table table = new Table();
        table.setName(context.getRootEntityAlias());
        column.setTable(table);
        column.setColumnName(context.getIdPropertyName());

        if (ids != null && !ids.isEmpty()) {
            List<LongValue> longValues = new ArrayList<LongValue>();
            for (Long id : ids) {
                longValues.add(new LongValue(String.valueOf(id)));
            }
            ItemsList itemsList = new ExpressionList(longValues);
            InExpression inExpression = new InExpression(column, itemsList);
            inExpression.setNot(negateExpression);
            return inExpression;
        } else {
            IsNullExpression isNullExpression = new IsNullExpression();
            isNullExpression.setLeftExpression(column);
            isNullExpression.setNot(negateExpression);
            return isNullExpression;
        }

    }

    private Set<String> getValuesFromInExp(InExpression inExpression) {

        final Set<String> strings = new HashSet<String>();

        new ItemsListVisitor() {

            @SuppressWarnings({ "rawtypes" })
            @Override
            public void visit(ExpressionList expressionList) {
                List items = expressionList.getExpressions();
                for (Object object : items) {
                    strings.add(ExpressionHelper.getStringValue((Expression) object));
                }
            }

            @Override
            public void visit(SubSelect subSelect) {
                throw new InvalidDataException(String.format(
                        "Invalid expression type SubSelect (%s) found in InExpressionItemList", subSelect));
            }
        };
        return strings;
    }

    // ============================
    @Override
    public void visit(OrExpression orExpression) {

        if (orExpression.getLeftExpression() == oldExpression) {
            orExpression.setLeftExpression(getNewExp(oldExpression));
            done = true;
        } else if (orExpression.getRightExpression() == oldExpression) {
            orExpression.setRightExpression(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(AndExpression andExpression) {

        if (andExpression.getLeftExpression() == oldExpression) {
            andExpression.setLeftExpression(getNewExp(oldExpression));
            done = true;
        } else if (andExpression.getRightExpression() == oldExpression) {
            andExpression.setRightExpression(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        if (parenthesis.getExpression() == oldExpression) {
            parenthesis.setExpression(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(EqualsTo equalsTo) {
        if (equalsTo == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {

        if (greaterThan == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {

        if (greaterThanEquals == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(InExpression inExpression) {
        if (inExpression == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

        if (isNullExpression == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(MinorThan minorThan) {
        if (minorThan == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {

        if (minorThanEquals == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {

        if (notEqualsTo == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    @Override
    public void visit(Column tableColumn) {

    }

    @Override
    public void visit(Between between) {
        if (between == oldExpression) {
            plianSelect.setWhere(getNewExp(oldExpression));
            done = true;
        }
    }

    public boolean isDone() {
        return done;
    }

}
