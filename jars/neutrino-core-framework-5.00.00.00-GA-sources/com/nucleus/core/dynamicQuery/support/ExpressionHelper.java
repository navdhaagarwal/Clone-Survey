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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;

import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class ExpressionHelper {

    public static String getStringValue(Expression value) {

        if (value instanceof NullValue)
            return null;
        if (value instanceof DoubleValue)
            return String.valueOf(((DoubleValue) value).getValue());
        if (value instanceof LongValue)
            return ((LongValue) value).getStringValue();
        if (value instanceof StringValue)
            return ((StringValue) value).getValue();
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void createJoins(List<QueryToken> ownerList, PlainSelect plainSelect) {
        for (QueryToken queryEntityProperty : ownerList) {
            Join join = new Join();
            join.setLeft(true);
            join.setOuter(true);
            Table table = new Table();
            table.setName(queryEntityProperty.getOwner().getAlias() + "." + queryEntityProperty.getPropertyName());
            table.setAlias(queryEntityProperty.getAlias());
            join.setRightItem(table);
            plainSelect.getJoins().add(join);

        }

    }

    public static List<QueryToken> getEligiblePropertiesForJoin(QueryToken queryToken, QueryContext context,
            Set<QueryToken> processedTokens) {

        List<QueryToken> ownerList = new ArrayList<QueryToken>();

        if (!queryToken.isCrossToken()) {
            // find first entityTypeProperty to start with
            QueryToken entityProperty = null;
            if (queryToken.isToken()) {
                entityProperty = queryToken.getOwner();
            } else if (queryToken.isEntityTypeProperty()) {
                entityProperty = queryToken;
            }

            // don't include root entity
            while (entityProperty.getOwner() != null
                    || !entityProperty.getPropertyEntityName().equalsIgnoreCase(context.getRootEntity())) {
                if (processedTokens.add(entityProperty)) {
                    ownerList.add(entityProperty);
                }
                entityProperty = entityProperty.getOwner();
            }
            Collections.reverse(ownerList);
        }
        return ownerList;
    }

    public static Column getColumnWithTableAndColumn(String tableName, String columnName) {

        Column column = new Column();
        Table table = new Table();
        table.setName(tableName);
        column.setTable(table);
        column.setColumnName(columnName);
        return column;
    }
}
