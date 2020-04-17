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
package com.nucleus.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.ldap.core.DirContextOperations;

import com.nucleus.authority.Authority;
import com.nucleus.service.BaseService;
/**
 * @author Nucleus Software Exports Limited
 * This service is to be used only when complete userInfo object loaded with authorities and preferences is required.
 * For e.g : User management Service or loading complete userInfo object just after login.
 * Developers need to use userService API to retrieve light weight userInfo objects for no logged in users.
 * In future this may be shipped under a separate war.  
 */

public interface UserSecurityService extends BaseService {
    
    public static final String SOURCE_LDAP = "ldap";
    public static String NEUTRINO_SYSTEM_USER = "system";
    public static String         MINIMUM_YEAR_FOR_DATE_PROPERTY = "config.user.date.minimum.year";
    public static String         MAXIMUM_YEAR_FOR_DATE_PROPERTY = "config.user.date.maximum.year";

    public UserInfo getCompleteUserFromUsername(String username);

    /**
     * 
     * This method is exclusively meant for fetching userInfo from user name while authenticating from Active directory.
     * The implementation also checks for any mismatch between the roles persisted in CAS application DB vs Active directory.
     * @param username
     * @param roleNames
     * @return
     */
    public UserInfo populateUserFromUsername(String username, List<String> roleNames);
    
    /**
     * 
     * This method is exclusively meant for fetching userInfo from user name while authenticating from Active directory.
     * The implementation also gets roles from ldap.
     *
     * @param username
     * @param roleNames
     * @param ctx {@link DirContextOperations}
     * @return
     */
    public UserInfo populateUserFromLDAP(String username, List<String> roleNames, DirContextOperations ctx, Boolean ldapUserisAtLogin);

    public void loadContextWithSystemUser();

	public Set<Authority> syncUserRoles(User user, List<String> userRoleNamesinAD);
	
	public Set<Authority> syncUserRoles(User user, List<String> userRoleNamesinAD, Boolean ldapUserisAtLogin);
    
    public UserInfo populateUserFromMap(String username, List<String> userRoleNamesinAD, Map<String, Object> map, Boolean ldapUserisAtLogin);
    
//added for ICICI Ldap user
    Set<Authority> syncUserRolesforCards(User user, List<String> userRoleNamesinAD);

    UserInfo checkAndUpdateUserIfAlreadyExists(String username, List<String> userRoleNamesinAD, Map<String, Object> map,Boolean ldapUserisAtLogin);

	UserInfo getUserFromUsernameWithOutLoginDtl(String username);

    Set<Authority> loadUserAuthorities(User user);

}
