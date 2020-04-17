package com.nucleus.core.dynamicQuery.service;

import java.util.ArrayDeque;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;

public interface ExpressionConversionService {

    public boolean convertExpression(ArrayDeque<Expression> expressions, Expression oldExp, QueryContext context,
            QueryToken queryToken,PlainSelect plianSelect);

}
