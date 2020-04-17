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
package com.nucleus.core.persistence.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;

import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.PostgreSQL82Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.SybaseDialect;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.xml.transform.StringSource;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.misc.util.ExceptionUtility;
import com.nucleus.core.persistence.jdbc.query.NeutrinoJdbcQueries;
import com.nucleus.core.persistence.jdbc.query.NeutrinoJdbcQueries.NamedSqlQuery;
import com.nucleus.core.persistence.jdbc.query.NeutrinoJdbcQueries.NamedSqlQuery.Query;
import com.nucleus.core.persistence.jdbc.query.TargetDatabase;
import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("neutrinoJdbcNamedQueryResolver")
public class NeutrinoJdbcNamedQueryResolverImpl implements NeutrinoJdbcNamedQueryResolver, InitializingBean {

    private static final MultiKeyMap<String, String>          NAMED_QUERY_REGISTRY             = new MultiKeyMap<String, String>();

    private static final ConcurrentHashMap<String, String>    DEFAULT_NAMED_QUERY_REGISTRY     = new ConcurrentHashMap<String, String>();
    private static final Map<DialectClassKey, TargetDatabase> DIALECT_TO_DATABASE_NAME_MAPPING = new HashMap<DialectClassKey, TargetDatabase>();
    private static final String                               RESOURCE_PATTERN                 = "/META-INF/neutrino-jdbc-queries/*.xml";
    private static final String                               CLASSPATH_ALL_URL_PREFIX         = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;

    private static final ResourcePatternResolver              PATTERN_RESOLVER                 = new PathMatchingResourcePatternResolver();

    private static final Jaxb2Marshaller                      MARSHALLER                       = new Jaxb2Marshaller();
    static {
        MARSHALLER.setContextPath("com.nucleus.core.persistence.jdbc.query");
        /*  Resource schemaResource = new ClassPathResource("/META-INF/neutrino-jdbc-queries/neutrino-jdbc-queries-schema.xsd");
          MARSHALLER.setSchema(schemaResource);*/

        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(Oracle8iDialect.class), TargetDatabase.ORACLE);
        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(MySQLDialect.class), TargetDatabase.MYSQL);
        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(PostgreSQL82Dialect.class), TargetDatabase.POSTGRES);
        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(HSQLDialect.class), TargetDatabase.HSQL);
        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(H2Dialect.class), TargetDatabase.H_2);
        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(DB2Dialect.class), TargetDatabase.DB_2);
        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(SQLServerDialect.class), TargetDatabase.SQL_SERVER);
        DIALECT_TO_DATABASE_NAME_MAPPING.put(new DialectClassKey(SybaseDialect.class), TargetDatabase.SYBASE);
    }

    @Override
    public String resolveNamedQuery(String queryName, Class<? extends Dialect> dialectClass) {
        TargetDatabase targetDatabase = DIALECT_TO_DATABASE_NAME_MAPPING.get(new DialectClassKey(dialectClass));
        return doResolve(queryName, targetDatabase);
    }

    private String doResolve(String queryName, TargetDatabase targetDatabase) {

        String finalSQLQuery = NAMED_QUERY_REGISTRY.get(queryName, targetDatabase != null ? targetDatabase.name() : null);
        if (finalSQLQuery == null) {
            finalSQLQuery = DEFAULT_NAMED_QUERY_REGISTRY.get(queryName);
        }
        if (StringUtils.isBlank(finalSQLQuery)) {
            throw new SystemException("No named sql query found with name " + queryName);
        }
        return finalSQLQuery;
    }

    @Override
    public String resolveNamedQuery(String queryName, TargetDatabase targetDatabase) {
        return doResolve(queryName, targetDatabase);
    }

    public void loadQueriesFromXml() {
        try {
            Resource[] resources = PATTERN_RESOLVER.getResources(CLASSPATH_ALL_URL_PREFIX + RESOURCE_PATTERN);

            for (int i = 0 ; i < resources.length ; i++) {
                Resource resource = resources[i];
                BaseLoggers.flowLogger.debug("Loading sql queries from class path resource:{}", resource.getFilename());
                NeutrinoJdbcQueries jdbcQueries = (NeutrinoJdbcQueries) MARSHALLER.unmarshal(new StringSource(IOUtils
                        .toString(resource.getInputStream())));
                List<NamedSqlQuery> namedSqlQueries = jdbcQueries.getNamedSqlQuery();

                for (NamedSqlQuery namedSqlQuery : namedSqlQueries) {

                    String namedQueryName = namedSqlQuery.getName();
                    String defaultQuery = namedSqlQuery.getDefaultQuery();
                    if (StringUtils.isBlank(namedQueryName) || StringUtils.isBlank(defaultQuery)) {
                        throw new IllegalStateException(
                                "Neutrino sql query name and defaultQuery must not be null.Error in class path resource "
                                        + resource.getFilename());
                    }
                    if (DEFAULT_NAMED_QUERY_REGISTRY.containsKey(namedQueryName) && !DEFAULT_NAMED_QUERY_REGISTRY.get(namedQueryName).equalsIgnoreCase(defaultQuery)) {
                        throw new IllegalStateException("Duplicate sql named query found with name " + namedQueryName);
                    }
                    DEFAULT_NAMED_QUERY_REGISTRY.put(namedQueryName, defaultQuery);
                    List<Query> queries = namedSqlQuery.getQuery();
                    for (Query query : queries) {
                        if (query.getTargetDatabase() == null || StringUtils.isBlank(query.getValue())) {
                            throw new IllegalStateException(
                                    "TargetDatabase and sql query value must not be null.Error in class path resource "
                                            + resource.getFilename());
                        }

                        NAMED_QUERY_REGISTRY.put(namedQueryName, query.getTargetDatabase().name(), query.getValue());
                    }
                }
            }

        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("Error while loading named sql queries.", e);
            ExceptionUtility.rethrowSystemException(e);

        }
    }

    private static class DialectClassKey implements Comparable<DialectClassKey> {

        public DialectClassKey(Class<? extends Dialect> dialectClass2) {
            super();
            this.dialectClass = dialectClass2;
        }

        private Class<? extends Dialect> dialectClass;

        @Override
        public int compareTo(DialectClassKey o) {
            if (dialectClass.isAssignableFrom(o.dialectClass))
                return 0;
            return -1;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            // only for 8 to 10 entries
            return prime;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj != null && obj instanceof DialectClassKey) {
                DialectClassKey dialectClassKey = (DialectClassKey) obj;
                if (dialectClassKey.dialectClass != null && dialectClassKey.dialectClass.isAssignableFrom(dialectClass)) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        loadQueriesFromXml();
    }

}
