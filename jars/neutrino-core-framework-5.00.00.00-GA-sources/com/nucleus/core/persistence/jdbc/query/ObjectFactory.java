
package com.nucleus.core.persistence.jdbc.query;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.nucleus.core.persistence.jdbc.query package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.nucleus.core.persistence.jdbc.query
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link NeutrinoJdbcQueries }
     * 
     */
    public NeutrinoJdbcQueries createNeutrinoJdbcQueries() {
        return new NeutrinoJdbcQueries();
    }

    /**
     * Create an instance of {@link NeutrinoJdbcQueries.NamedSqlQuery }
     * 
     */
    public NeutrinoJdbcQueries.NamedSqlQuery createNeutrinoJdbcQueriesNamedSqlQuery() {
        return new NeutrinoJdbcQueries.NamedSqlQuery();
    }

    /**
     * Create an instance of {@link NeutrinoJdbcQueries.NamedSqlQuery.Query }
     * 
     */
    public NeutrinoJdbcQueries.NamedSqlQuery.Query createNeutrinoJdbcQueriesNamedSqlQueryQuery() {
        return new NeutrinoJdbcQueries.NamedSqlQuery.Query();
    }

}