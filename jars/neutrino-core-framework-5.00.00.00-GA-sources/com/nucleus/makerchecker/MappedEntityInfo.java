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

import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 * TODO -> amit.parashar Add documentation to class
 */
public class MappedEntityInfo {
    
    private String         masterEntityName;

    List<EntityUpdateInfo> EntityUpdateInfoList;
    
    /**
     * autoAuthorizationFlag : if set to 'Y' for a given entity , A user with create access can auto approve. 
     */
    private String         autoAuthorizationFlag;
    
    public String getMasterEntityName() {
        return masterEntityName;
    }

    public void setMasterEntityName(String masterEntityName) {
        this.masterEntityName = masterEntityName;
    }

    public List<EntityUpdateInfo> getEntityUpdateInfoList() {
        return EntityUpdateInfoList;
    }

    public void setEntityUpdateInfoList(List<EntityUpdateInfo> entityUpdateInfoList) {
        EntityUpdateInfoList = entityUpdateInfoList;
    }

    public String getAutoAuthorizationFlag() {
        return autoAuthorizationFlag;
    }

    public void setAutoAuthorizationFlag(String autoAuthorizationFlag) {
        this.autoAuthorizationFlag = autoAuthorizationFlag;
    }



}
