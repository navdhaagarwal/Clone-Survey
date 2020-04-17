/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.database.initializer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.nucleus.core.exceptions.SystemException;

/**
 * Delegate class that wraps the Spring database initializer. See InitializeDatabaseBeanDefinitionParser class inside spring-jdbc jar which is invoked
 * from <jdbc:initialize-database> tag.
 * @author Nucleus Software Exports Limited
 */
public class SpringDatabaseExecutorDelegate {

    private DataSource dataSource;
    private boolean    ignoreAllFailures;
    private boolean    ignoreDropStatementFailures;
    private String     scriptEncoding;
    private Resource[]   resources;
    private boolean    enabled   = true;
    private String     commentPrefix;
    private String     statementSeparator;
    private String     execution = "INIT";

    @PostConstruct
    public void invokeDelegate() {
        if (enabled) {
            DataSourceInitializer initializer = new DataSourceInitializer();
            initializer.setDataSource(dataSource);
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(ignoreAllFailures);
            populator.setIgnoreFailedDrops(ignoreDropStatementFailures);
            populator.setScripts(resources);
            if (statementSeparator != null) {
                populator.setSeparator(statementSeparator);
            }
            if (commentPrefix != null) {
                populator.setCommentPrefix(commentPrefix);
            }
            if (scriptEncoding != null) {
                populator.setSqlScriptEncoding(scriptEncoding);
            }
            if (execution.trim().equalsIgnoreCase("init")) {
                initializer.setDatabasePopulator(populator);
            } else if (execution.trim().equalsIgnoreCase("destroy")) {
                initializer.setDatabaseCleaner(populator);
            }
            initializer.afterPropertiesSet();
        }
    }

    /**
     * Set true/false if the script execution is enabled or not. Default value is true
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Set true if all failures should be ignored
     */
    public void setIgnoreAllFailures(boolean ignoreAllFailures) {
        this.ignoreAllFailures = ignoreAllFailures;
        if (ignoreAllFailures) {
            this.ignoreDropStatementFailures = true;
        }
    }

    /**
     * Set script encoding. 
     */
    public void setScriptEncoding(String scriptEncoding) {
        this.scriptEncoding = scriptEncoding;
    }

    /**
     * Set DataSource for making connections
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Set true if drop statements should be ignored.
     */
    public void setIgnoreDropStatementFailures(boolean ignoreDropStatementFailures) {
        this.ignoreDropStatementFailures = ignoreDropStatementFailures;
    }

    /**
     * Resource which is to be executed
     */
    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    /**
     *Set SQL comment prefix. Default is '--'
     */
    public void setCommentPrefix(String commentPrefix) {
        this.commentPrefix = commentPrefix;
    }

    /**
     * Set SQL statement separator. Default value is ';'
     */
    public void setStatementSeparator(String statementSeparator) {
        this.statementSeparator = statementSeparator;
    }

    /**
     * Set INIT or DESTROY to indicate when the script should be executed (i.e. at initialization or destruction of Spring container). Default is INIT.
     */
    public void setExecution(String execution) {
        if (!execution.trim().equalsIgnoreCase("init") || !execution.trim().equalsIgnoreCase("destroy")) {
            throw new SystemException("The execution can be only INIT or DESTROY");
        }
        this.execution = execution;
    }
}
