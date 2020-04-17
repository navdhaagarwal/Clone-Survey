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
package com.nucleus.query.constants;

/**
 * Constants to represent the JPA compliant persistence provider hints which can be passed to enable
 * specific query behaviours on Queries  
 * @author Nucleus Software Exports Limited
 *
 */
public class QueryHint {

    // Setting this hint to true will make hibernate never dirty-check them or make changes persistent , default to false
    public static final String QUERY_HINT_READONLY    = "org.hibernate.readOnly";
    // Query timeout in seconds
    public static final String QUERY_HINT_TIMEOUT     = "org.hibernate.timeout";
    // Number of rows fetched by the JDBC driver per roundtrip
    public static final String QUERY_HINT_FETCHSIZE   = "org.hibernate.fetchSize";
    // Add a comment to the SQL query
    public static final String QUERY_HINT_COMMENT     = "org.hibernate.comment";
    // Whether or not a query is cacheable, defaults to false
    public static final String QUERY_HINT_CACHEABLE   = "org.hibernate.cacheable";
    // Override the cache mode for the query
    public static final String QUERY_HINT_CACHEMODE   = "org.hibernate.cacheMode";
    // Cache region of the query
    public static final String QUERY_HINT_CACHEREGION = "org.hibernate.cacheRegion";
    // Flush mode used for the query
    public static final String QUERY_HINT_FLUSHMODE   = "org.hibernate.flushMode";

}
