package com.nucleus.web.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
//import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.person.entity.SalutationType;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserSecurityService;

/**
 * Implementation of the Spring Security's UserDetailsService Interface, it loads 
 * the User information to authenticate the User.
 * 
 *  @author Nucleus Software Exports Limited
 */
@Named(value = "assersionUserDetailService")
public class AssersionUserDetailsService extends AbstractCasAssertionUserDetailsService {

    // Get attributes from ldap
    @Value(value = "#{'${core.web.config.default.branchCode.ldap}'}")
    private String              code;
    @Value(value = "#{'${core.web.config.ldap.default.nickname}'}")
    private String              nickname;
    @Value(value = "#{'${core.web.config.ldap.default.initials}'}")
    private String              initials;
    @Value(value = "#{'${core.web.config.attribute.mailid}'}")
    private String              mailId;
    @Value(value = "#{'${core.web.config.attribute.firstname}'}")
    private String              firstname;
    @Value(value = "#{'${core.web.config.attribute.lastname}'}")
    private String              lastname;
    @Value(value = "#{'${core.web.config.attribute.mobileNo}'}")
    private String              mobileNo;
    @Value(value = "#{'${core.web.config.attribute.phoneNo}'}")
    private String              phoneNo;
    @Value(value = "#{'${core.web.config.attribute.company}'}")
    private String              company;
    @Value(value = "#{'${core.web.config.attribute.department}'}")
    private String              department;
    @Value(value = "#{'${core.web.config.attribute.title}'}")
    private String              title;
    @Value(value = "#{'${core.web.config.attribute.empCode}'}")
    private String              employeeId;
    @Value(value = "#{'${core.web.config.ldap.default.role}'}")
    private String              defaultLdapRole;
    @Value(value = "#{'${core.web.config.ldap.default.role.enable}'}")
    private Boolean             defaultLdapRoleEnable;
    @Value(value = "#{'${core.web.config.ldap.default.thumbnailPhoto}'}")
    private String              thumbnailPhoto;
    @Value(value = "#{'${core.web.config.create.update.user.enable}'}")
    private boolean             createUpdateUserEnable;
    @Value(value = "#{'${core.web.config.group.role}'}")
    private String              groupforRole;
    @Inject
    @Named("userSecurityService")
    private UserSecurityService userSecurityService;

    @Inject
    @Named("genericParameterService")
    GenericParameterService     genericParameterService;

    @SuppressWarnings({ "deprecation", "unchecked" })
    @MonitoredWithSpring(name = "UDC_LOAD_USR_BY_USRNAME")
    @Override
    protected UserInfo loadUserDetails(Assertion assertion) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            map.put(thumbnailPhoto, assertion.getPrincipal().getAttributes().get(thumbnailPhoto));
        } catch (Exception e) {
        }

        try {
            map.put(mailId, assertion.getPrincipal().getAttributes().get(mailId));

        } catch (Exception e) {
        }

        try {
            map.put(firstname, assertion.getPrincipal().getAttributes().get(firstname));
        } catch (Exception e) {
        }

        try {
            map.put(lastname, assertion.getPrincipal().getAttributes().get(lastname));
        } catch (Exception e) {
        }

        try {
            map.put(nickname, assertion.getPrincipal().getAttributes().get(nickname));
        } catch (Exception e) {
        }

        try {
            map.put(initials, assertion.getPrincipal().getAttributes().get(initials));
        } catch (Exception e) {
        }

        try {
            map.put(mobileNo, assertion.getPrincipal().getAttributes().get(mobileNo));

        } catch (Exception e) {
        }
        try {
            map.put(phoneNo, assertion.getPrincipal().getAttributes().get(phoneNo));

        } catch (Exception e) {
        }
        try {
            map.put(company, assertion.getPrincipal().getAttributes().get(company));

        } catch (Exception e) {
        }
        try {
            map.put(department, assertion.getPrincipal().getAttributes().get(department));

        } catch (Exception e) {
        }
        try {
            map.put(title, assertion.getPrincipal().getAttributes().get(title));

        } catch (Exception e) {
        }
        try {
            map.put(employeeId, assertion.getPrincipal().getAttributes().get(employeeId));
        } catch (Exception e) {
        }

        
        
        List<String> roles = (List<String>) assertion.getPrincipal().getAttributes().get(groupforRole);
        NeutrinoValidator.notNull(roles,"Roles cannot be empty");
        List<String> userRoleNamesinAD = new ArrayList<String>();
        for(String role:roles){
            userRoleNamesinAD.add(new DistinguishedName(role).removeLast().getValue());
        }
        return userSecurityService.checkAndUpdateUserIfAlreadyExists(assertion.getPrincipal().getName(), userRoleNamesinAD, map,true);
        
        
    }

}
