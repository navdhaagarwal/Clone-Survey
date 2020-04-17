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
package com.nucleus.user.search;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Class is used to collect criteria to search user/team and to create respective jpa query to fetch users/team.
 * It enables the criteria to be collected from ui by using this class as command object.
 * 
 * @author Nucleus Software Exports Limited
 * 
 */
public class UserSearchCriteria {

    private Set<Long>           teamIds;
    private Set<Long>           roleIds;
    private Set<Long>           orgBranchIds;
    private Set<Long>           businessPartnerIds;
    private String              finalQueryString;
    private Map<String, Object> queryParameterMap;

    public Set<Long> getTeamIds() {
        return teamIds;
    }

    public void setTeamIds(Set<Long> teamIds) {
        this.teamIds = teamIds;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public Set<Long> getOrgBranchIds() {
        return orgBranchIds;
    }

    public void setOrgBranchIds(Set<Long> orgBranchIds) {
        this.orgBranchIds = orgBranchIds;
    }

    public boolean isEmptyCriteria() {
        return ((teamIds == null || teamIds.isEmpty()) && (roleIds == null || roleIds.isEmpty())
                && (orgBranchIds == null || orgBranchIds.isEmpty()) && (businessPartnerIds == null || businessPartnerIds
                .isEmpty()));
    }

    public String getFinalQueryString() {
        return finalQueryString;
    }

    public void setFinalQueryString(String finalQueryString) {
        this.finalQueryString = finalQueryString;
    }

    // will return a copy of queryParameterMap not the actual object.
    public Map<String, Object> getQueryParameterMap() {
        Map<String, Object> emptyMap = Collections.emptyMap();
        return queryParameterMap != null ? new HashMap<String, Object>(queryParameterMap) : emptyMap;
    }

    public void addParameter(String key, Object value) {
        if (queryParameterMap == null) {
            this.queryParameterMap = new HashMap<String, Object>();
        }
        this.queryParameterMap.put(key, value);
    }

    public Set<Long> getBusinessPartnerIds() {
        return businessPartnerIds;
    }

    public void setBusinessPartnerIds(Set<Long> businessPartnerIds) {
        this.businessPartnerIds = businessPartnerIds;
    }

}
