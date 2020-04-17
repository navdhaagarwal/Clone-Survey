/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.purging.api;

import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * @author Nucleus Software Exports Limited
 * Interface to be implemented by classes to purge a specific component like activiti data,rules data,event data.
 * These components are assumed to be safe for purging independently.
 * 
 */
public interface ArchiveAndPurgeTask {

    /**
     * Tables should be added to supplied collection in strict order of deletion
     * @param tableNames
     */
    public void addTablesInDeletionOrder(Set<PurgeTableInfo> tableNames);

    /**
     * 
     * with a transaction aware connection.No need to control the transaction in this method. 
     * @param loanAppIds - Loan application ids
     * @return Map containing table name and ids to purge/archive
     */

    /**
     * Method to select table wise primary keys for archiving and purging for a group of tables related
     * to some functional component like event,rules,workflow etc
     * @param purgeContext - used to access loanAppIds, JDBC connection, entityDao and jdbcTemplate 
     * @return - A map containing tables names and ids for purging
     */
    public Map<String, List<Object>> findRecordsForArchiveAndPurge(PurgeContext purgeContext);

}
