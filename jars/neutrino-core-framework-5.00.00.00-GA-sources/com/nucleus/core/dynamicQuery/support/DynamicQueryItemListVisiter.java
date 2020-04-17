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
package com.nucleus.core.dynamicQuery.support;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;

import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.exceptions.InvalidDataException;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class DynamicQueryItemListVisiter implements ItemsListVisitor {

    ExpressionTransformVisiter expressionVisiterImpl;
    QueryToken            queryToken;

    public DynamicQueryItemListVisiter(ExpressionTransformVisiter expressionVisiterImpl, QueryToken queryToken) {
        this.expressionVisiterImpl = expressionVisiterImpl;
        this.queryToken = queryToken;
    }

    @Override
    public void visit(SubSelect subSelect) {

        throw new InvalidDataException(String.format("Invalid expression type SubSelect (%s) found in InExpressionItemList",
                subSelect));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void visit(ExpressionList expressionList) {

        List items = expressionList.getExpressions();
        List newItems = new ArrayList();

        for (Object object : items) {
            if (object instanceof StringValue) {
                Expression actualValueExp = expressionVisiterImpl.getNewValue((StringValue) object, queryToken);
                newItems.add(actualValueExp);
            }
        }
        expressionList.setExpressions(newItems);
    }
}
