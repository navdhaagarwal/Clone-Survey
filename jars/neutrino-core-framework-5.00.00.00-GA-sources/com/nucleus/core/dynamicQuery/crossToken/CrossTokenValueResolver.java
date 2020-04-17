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
package com.nucleus.core.dynamicQuery.crossToken;

import java.util.Set;

import com.nucleus.core.dynamicQuery.entity.QueryContext;
import com.nucleus.core.dynamicQuery.entity.QueryToken;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public interface CrossTokenValueResolver {

    boolean canResolve(QueryToken queryToken, QueryContext context);

    Set<Long> resolveEquals(QueryToken queryToken, QueryContext context, String value);

    Set<Long> resolveIn(QueryToken queryToken, QueryContext context, Set<String> values);

    Set<Long> resolveBetween(QueryToken queryToken, QueryContext context, String fromValue, String toValue);

    Set<Long> resolveIsNull(QueryToken queryToken, QueryContext context);

    Set<Long> resolveLessThanEquals(QueryToken queryToken, QueryContext context, String values);

    Set<Long> resolveLessThan(QueryToken queryToken, QueryContext context, String values);

    Set<Long> resolveGreaterThanEquals(QueryToken queryToken, QueryContext context, String values);

    Set<Long> resolveGreaterThan(QueryToken queryToken, QueryContext context, String values);

    /**
     * Method is used to post process query results.Only required if we allow cross type tokens in
     * SELECT clause.
     * @param queryToken
     * @param context
     * @param id
     * @return
     */
    Object getTokenValueForRootEntity(QueryToken queryToken, QueryContext context, Long rootEntityId);

}
