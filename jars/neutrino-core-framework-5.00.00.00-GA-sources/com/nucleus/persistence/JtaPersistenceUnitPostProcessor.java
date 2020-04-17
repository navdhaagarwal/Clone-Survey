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
package com.nucleus.persistence;

import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitPostProcessor;

import com.nucleus.logging.BaseLoggers;

/**
 * Injects the dataSource in the {@link javax.persistence.spi.PersistenceUnitInfo}
 * according to the chosen mode. By default, JTA is not enabled and the dataSource
 * is injected as a non-jta aware data source. If the {@link #setJtaEnabled(boolean)}
 * flag is set to <tt>true</tt> the dataSource is injected as a jta-aware data source.
 */
public class JtaPersistenceUnitPostProcessor implements PersistenceUnitPostProcessor {

    private final Logger logger     = BaseLoggers.flowLogger;

    private DataSource   dataSource;
    private boolean      jtaEnabled = false;

    /**
     * Enriches the PersistenceUnitInfo read from the <tt>persistence.xml</tt>
     * configuration file according to the <tt>jtaEnabled</tt> flag. Registers
     * the <tt>dataSource</tt> as a jta data source if it is <tt>true</tt> or
     * as a regular, non-jta data source otherwise.
     *
     * @see PersistenceUnitPostProcessor#postProcessPersistenceUnitInfo(org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo)
     */
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo mutablePersistenceUnitInfo) {
        if (jtaEnabled) {
            logger.info("Enriching the persistence unit info [" + mutablePersistenceUnitInfo.getPersistenceUnitName()
                    + "] with the jta aware data source.");
            mutablePersistenceUnitInfo.setJtaDataSource(dataSource);
            mutablePersistenceUnitInfo.setTransactionType(PersistenceUnitTransactionType.JTA);
        } else {
            logger.info("Enriching the persistence unit info [" + mutablePersistenceUnitInfo.getPersistenceUnitName()
                    + "] with the non-jta aware data source.");
            mutablePersistenceUnitInfo.setNonJtaDataSource(dataSource);
            mutablePersistenceUnitInfo.setTransactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL);
        }
    }

    /**
     * Sets the {@link DataSource} to be used by the entity manager factory
     *
     * @param dataSource the data source to use
     */
    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Specifies if the data source should be injected as a jta-aware dataSource
     * in the entity manager.
     *
     * @param jtaEnabled <tt>true</tt> to enable jta support
     */
    public void setJtaEnabled(boolean jtaEnabled) {
        this.jtaEnabled = jtaEnabled;
    }

}
