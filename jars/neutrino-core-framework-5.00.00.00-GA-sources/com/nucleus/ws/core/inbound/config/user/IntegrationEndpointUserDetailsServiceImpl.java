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
package com.nucleus.ws.core.inbound.config.user;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.persistence.EntityDao;

/**
 * @author Nucleus Software Exports Limited
 */
@Named("integrationEndpointUserDetailsService")
public class IntegrationEndpointUserDetailsServiceImpl implements IntegrationEndpointUserDetailsService {

    private static final String SYSTEM_USER_BY_USERNAME_QUERY = "from SystemUser su where upper(su.username)=upper(:username) and (su.entityLifeCycleData.snapshotRecord IS NULL OR su.entityLifeCycleData.snapshotRecord = false) AND su.activeFlag = true";

    @Inject
    @Named("entityDao")
    private EntityDao           entityDao;

    @Override
    public IntegrationEndpointUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (StringUtils.isNotBlank(username)) {
            JPAQueryExecutor<SystemUser> queryExecutor = new JPAQueryExecutor<SystemUser>(SYSTEM_USER_BY_USERNAME_QUERY);
            queryExecutor.addParameter("username", username);
            SystemUser systemUser = entityDao.executeQueryForSingleValue(queryExecutor);
            if (systemUser != null) {
                return new SystemUserInfo(systemUser);
            }
        }
        throw new UsernameNotFoundException("No System user found with name:" + username);
    }
}
