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
package com.nucleus.web.ldap.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.user.User;

/**
 * @author Nucleus Software Exports Limited
 */
public class NeutrinoLdapServiceImpl implements NeutrinoLdapService {

    @Value(value = "#{'${core.web.config.activeDirectoryAuthenticationProvider.domain.value}'}")
    private String                    domain;

    private FilterBasedLdapUserSearch ldapFilterUserSearch;

    /*
     * TODO:Password encryption functionality in property file is still left.
     */
    @Override
    public User searchUserByUsername(String userName) {
        NeutrinoValidator.notNull(userName, "User Name can't be null.");
        if (StringUtils.isNotBlank(domain)) {
            String userNameWithDomain = userName.concat("@" + domain);
            DirContextOperations ctx = ldapFilterUserSearch.searchForUser(userNameWithDomain);
            User user = new User();
            user.setUsername(ctx.getStringAttribute("name"));
            return user;
        } else {
            throw new InvalidDataException("Domain name can't be left blank.");
        }

    }

    public void setLdapFilterUserSearch(FilterBasedLdapUserSearch ldapFilterUserSearch) {
        this.ldapFilterUserSearch = ldapFilterUserSearch;
    }

}
