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
package com.nucleus.makerchecker;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> amit.parashar Add documentation to class
 */
public class EntityUpdateInfo {
    
    
    private String updateFieldName;
    
    private String updateEntityName;
    
    private String includeApprovalStatuses;
    
    private String includePersistenceStatuses;
    
    private boolean softReference;

    /**
     * @return the updateFieldName
     */
    public String getUpdateFieldName() {
        return updateFieldName;
    }

    /**
     * @param updateFieldName the updateFieldName to set
     */
    public void setUpdateFieldName(String updateFieldName) {
        this.updateFieldName = updateFieldName;
    }

    /**
     * @return the updateEntityName
     */
    public String getUpdateEntityName() {
        return updateEntityName;
    }

    /**
     * @param updateEntityName the updateEntityName to set
     */
    public void setUpdateEntityName(String updateEntityName) {
        this.updateEntityName = updateEntityName;
    }

    /**
     * @return the includeApprovalStatuses
     */
    public String getIncludeApprovalStatuses() {
        return includeApprovalStatuses;
    }

    /**
     * @param includeApprovalStatuses the includeApprovalStatuses to set
     */
    public void setIncludeApprovalStatuses(String includeApprovalStatuses) {
        this.includeApprovalStatuses = includeApprovalStatuses;
    }

    /**
     * @return the includePersistenceStatuses
     */
    public String getIncludePersistenceStatuses() {
        return includePersistenceStatuses;
    }

    /**
     * @param includePersistenceStatuses the includePersistenceStatuses to set
     */
    public void setIncludePersistenceStatuses(String includePersistenceStatuses) {
        this.includePersistenceStatuses = includePersistenceStatuses;
    }

    /**
     * @return the softReference
     */
    public boolean isSoftReference() {
        return softReference;
    }

    /**
     * @param softReference the softReference to set
     */
    public void setSoftReference(boolean softReference) {
        this.softReference = softReference;
    }


    
}
