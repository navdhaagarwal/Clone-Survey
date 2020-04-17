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

import java.util.List;
import java.util.Map;

import javax.inject.Named;

import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.user.User;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("userSearchDao")
public class UserSearchDao extends BaseDaoImpl<User> {

    private final static String USER_SEARCH_QUERY                = "select distinct(u) from User u";
    private final static String USER_ID_NAME_MAP_QUERY           = "select new Map(u.id as id,u.username as username) from User u";
    private final static String SPACE                            = " ";
    private final static String SPACE_AND_SPACE                  = " and ";
    private final static String USERS_IN_ROLE_FILTER             = "u.id in(select ru.id from Role r,IN(r.users) ru where r.id in (:roleIds) AND r.activeFlag = true)";
    private final static String USERS_IN_TEAM_FILTER             = "u.id in(select tu.id from Team t,IN(t.users) tu where t.id in (:teamIds))";
    private final static String USERS_IN_BRANCH_FILTER           = "u.id in(select uobm.associatedUser.id from UserOrgBranchMapping uobm where uobm.organizationBranch.id in (:organizationBranchIds))";
    private final static String USERS_IN_BUSINESS_PARTNER_FILTER = "u.id in( select ubpm.associatedUser.id from UserBPMapping ubpm where ubpm.businessPartner.id in (:businessPartnerIds))";
    private final static String USERS_APPROVAL_STATUS 			 = "u.masterLifeCycleData.approvalStatus in (0,3)";
    
    private UserSearchCriteria getExtendedCriteria(UserSearchCriteria userSearchCriteria, String baseQuery) {

        StringBuilder stringBuilder = new StringBuilder(baseQuery);
        boolean flag = false;

        if (!userSearchCriteria.isEmptyCriteria()) {
            stringBuilder.append(SPACE);
            stringBuilder.append("where");
            if (userSearchCriteria.getTeamIds() != null && !userSearchCriteria.getTeamIds().isEmpty()) {

                stringBuilder.append(flag ? SPACE_AND_SPACE : SPACE);
                stringBuilder.append(USERS_IN_TEAM_FILTER);
                userSearchCriteria.addParameter("teamIds", userSearchCriteria.getTeamIds());
                flag = true;
            }
            if (userSearchCriteria.getRoleIds() != null && !userSearchCriteria.getRoleIds().isEmpty()) {

                stringBuilder.append(flag ? SPACE_AND_SPACE : SPACE);
                stringBuilder.append(USERS_IN_ROLE_FILTER);
                userSearchCriteria.addParameter("roleIds", userSearchCriteria.getRoleIds());
                flag = true;
            }
            if (userSearchCriteria.getOrgBranchIds() != null && !userSearchCriteria.getOrgBranchIds().isEmpty()) {
                stringBuilder.append(flag ? SPACE_AND_SPACE : SPACE);
                stringBuilder.append(USERS_IN_BRANCH_FILTER);
                userSearchCriteria.addParameter("organizationBranchIds", userSearchCriteria.getOrgBranchIds());
                flag = true;
            }
            if (userSearchCriteria.getBusinessPartnerIds() != null && !userSearchCriteria.getBusinessPartnerIds().isEmpty()) {
                stringBuilder.append(flag ? SPACE_AND_SPACE : SPACE);
                stringBuilder.append(USERS_IN_BUSINESS_PARTNER_FILTER);
                userSearchCriteria.addParameter("businessPartnerIds", userSearchCriteria.getBusinessPartnerIds());
                flag = true;
            }
        }
        
        if(flag){
            stringBuilder.append(SPACE_AND_SPACE);
        }else{
            stringBuilder.append(SPACE);
        	stringBuilder.append("where");
            stringBuilder.append(SPACE);
        }
        stringBuilder.append(USERS_APPROVAL_STATUS);
        
        userSearchCriteria.setFinalQueryString(stringBuilder.toString());
        return userSearchCriteria;

    }

    public List<User> getUsersByCriteria(UserSearchCriteria userSearchCriteria) {

        userSearchCriteria = getExtendedCriteria(userSearchCriteria, USER_SEARCH_QUERY);
        JPAQueryExecutor<User> jpaQueryExecutor = new JPAQueryExecutor<User>(userSearchCriteria.getFinalQueryString());

        userSearchCriteria.getQueryParameterMap().keySet();
        for (String key : userSearchCriteria.getQueryParameterMap().keySet()) {
            jpaQueryExecutor.addParameter(key, userSearchCriteria.getQueryParameterMap().get(key));
        }

        return executeQuery(jpaQueryExecutor);
    }

    public List<Map<String, Object>> getUsersIdNameMapByCriteria(UserSearchCriteria userSearchCriteria) {

        userSearchCriteria = getExtendedCriteria(userSearchCriteria, USER_ID_NAME_MAP_QUERY);
        JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(
                userSearchCriteria.getFinalQueryString());

        userSearchCriteria.getQueryParameterMap().keySet();
        for (String key : userSearchCriteria.getQueryParameterMap().keySet()) {
            jpaQueryExecutor.addParameter(key, userSearchCriteria.getQueryParameterMap().get(key));
            jpaQueryExecutor.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        }

        return executeQuery(jpaQueryExecutor);
    }
}
