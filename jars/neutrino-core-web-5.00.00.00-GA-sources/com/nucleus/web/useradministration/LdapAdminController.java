package com.nucleus.web.useradministration;

import static com.nucleus.web.security.AesUtil.PASS_PHRASE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.user.User;
import com.nucleus.user.UserSecurityService;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.ldap.LdapService;
import com.nucleus.web.security.CustomUsernamePasswordAuthenticationFilter;

@Controller
@RequestMapping(value = "/ldapAdmin")
public class LdapAdminController extends BaseController{

    @Inject
    @Named("ldapService")
    private LdapService ldapService;
   
    @Inject
    @Named("customUsernamePasswordAuthenticationFilter")
    AbstractAuthenticationProcessingFilter customUsernamePasswordAuthenticationFilter;
    
    @Inject
    @Named(value = "userSecurityService")
    UserSecurityService userSecurityService;
    
    @Inject
    @Named(value = "userService")
    private UserService userService;
    
    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService        makerCheckerService;

    
    //Get attributes from ldap
    @Value(value = "#{'${core.web.config.default.branchCode.ldap}'}")
    private String               code;
    @Value(value = "#{'${core.web.config.ldap.default.nickname}'}")
    private String               nickname;
    @Value(value = "#{'${core.web.config.ldap.default.initials}'}")
    private String               initials;
    @Value(value = "#{'${core.web.config.attribute.mailid}'}")
    private String               mailId;
    @Value(value = "#{'${core.web.config.attribute.firstname}'}")
    private String               firstname;
    @Value(value = "#{'${core.web.config.attribute.lastname}'}")
    private String               lastname;
    @Value(value = "#{'${core.web.config.attribute.mobileNo}'}")
    private String               mobileNo;
    @Value(value = "#{'${core.web.config.attribute.phoneNo}'}")
    private String               phoneNo;
    @Value(value = "#{'${core.web.config.attribute.company}'}")
    private String               company;
    @Value(value = "#{'${core.web.config.attribute.department}'}")
    private String               department;
    @Value(value = "#{'${core.web.config.attribute.title}'}")
    private String               title;
    @Value(value = "#{'${core.web.config.attribute.empCode}'}")
    private String               employeeId;
    @Value(value = "#{'${core.web.config.ldap.default.role}'}")
    private String               defaultLdapRole;
    @Value(value = "#{'${core.web.config.ldap.default.role.enable}'}")
    private Boolean              defaultLdapRoleEnable;
    @Value(value = "#{'${core.web.config.group.role}'}")
    private String               roles;
    @Value(value = "#{'${core.web.config.ldap.default.thumbnailPhoto}'}")
    private String                  thumbnailPhoto;
    
    /*
     * 
     * @param username
     * @param password
     * @param request
     * This method authenticates a user when 
     * import user from LDAP page gets loaded.
     * @return
     */
    @PreAuthorize("hasAuthority('LDAP_CHECKNAME')")
    @RequestMapping(value = "/authenticateUser")
    public @ResponseBody boolean authenticateUser(String username,String password,HttpServletRequest request){
        
        DirContext ctx = null;
        try {
        	if(!(username.isEmpty()) && !(password.isEmpty())){
            
        		if (customUsernamePasswordAuthenticationFilter instanceof CustomUsernamePasswordAuthenticationFilter) {
                CustomUsernamePasswordAuthenticationFilter new_name = (CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter;
                password = new_name.decryptPass(password  , (String) request.getSession(false).getAttribute(PASS_PHRASE));
                
        		}
        		ctx = ldapService.bindAsUser(username, password);
        		return true;
        }
        	else{
        		return false;
        	}
            
        } catch (SystemException e) {
            BaseLoggers.securityLogger.info("A communication exception ocuured while connecting to Active directory",
                    e.getCause());
            
            throw ldapService.badCredentials();
            
        }
    }
    
    /*
     * 
     * @param checkName
     * @param username
     * @param password
     * @param request     * 
     * This method re-authenticates the user when the entered 
     * credentials are used to fetch the details of user to be imported from LDAP.
     * @return
     */
    @PreAuthorize("hasAuthority('LDAP_CHECKNAME')")
    @RequestMapping(value = "/checkname")
    public @ResponseBody Map<String,String> checkName(String checkName,String username,String password,HttpServletRequest request){
        Map<String,String> map = new HashMap<String,String>();
        
        DirContext ctx = null;
        try {
            
            if (customUsernamePasswordAuthenticationFilter instanceof CustomUsernamePasswordAuthenticationFilter) {
                CustomUsernamePasswordAuthenticationFilter new_name = (CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter;
                password = new_name.decryptPass(password  , (String) request.getSession(false).getAttribute(PASS_PHRASE));
            }
            
            
            ctx = ldapService.bindAsUser(username, password);
        } catch (SystemException e) {
            BaseLoggers.securityLogger.info("A communication exception ocuured while connecting to Active directory",
                    e.getCause());
            throw ldapService.badCredentials();
        }

        DirContextOperations searchForUser=null;
        try {
            searchForUser = ldapService.searchForUser(ctx, checkName);
        } catch (NamingException e) {
            BaseLoggers.exceptionLogger.error("Failed to locate directory entry for authenticated user: " + checkName, e);
            throw ldapService.badCredentials(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }
        try {
            map.put("mailId" , searchForUser.getAttributes().get(this.mailId).get().toString());
        } catch (Exception e) {
        }
        try {
            map.put("firstname", searchForUser.getAttributes().get(this.firstname).get().toString());
        } catch (Exception e) {
        }
        try {
            map.put("lastname", searchForUser.getAttributes().get(this.lastname).get().toString());
        } catch (Exception e) {
        }
        try {
            map.put("mobileNo", searchForUser.getAttributes().get(this.mobileNo).get().toString());
        } catch (Exception e) {
        }
        
        //commented as per discussion with gaurav to remove landline number
        /*try {
            map.put("telephonenumber",searchForUser.getAttributes().get(this.phoneNo).get().toString());
        } catch (Exception e) {
        }*/
        try {
            map.put("company", searchForUser.getAttributes().get(this.company).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put("department", searchForUser.getAttributes().get(this.department).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put("title", searchForUser.getAttributes().get(this.title).get().toString());

        } catch (Exception e) {
        }
        try {
            map.put("employeeId", searchForUser.getAttributes().get(this.employeeId).get().toString());
        } catch (Exception e) {
        }
        try {
            map.put("thumbnailPhoto", new String(Base64.encodeBase64((byte[])searchForUser.getAttributes().get(thumbnailPhoto).get())));
        } catch (Exception e) {
        }
        
        try {
            String[] roles = searchForUser.getStringAttributes(this.roles);
            StringBuilder act_roles=new StringBuilder("");
            for(String role:roles){
                act_roles.append(new DistinguishedName(role).removeLast().getValue()).append(",");
            }
            act_roles.setCharAt(act_roles.length()-1, '\0');
            map.put("roles", act_roles.toString());
        } catch (Exception e) {
        }
        return map;
    }
    
    @PreAuthorize("hasAuthority('LDAP_IMPORT_USER')")
    @RequestMapping("/importUser")
    public @ResponseBody ResponseEntity<Map<String, String>> importUser(String checkName,String username,String password,HttpServletRequest request){
        DirContext ctx = null;
        Map<String,String> responseMap = new HashMap<>();
        responseMap.put("status", "success");
        try {
            User dbUserWithSameUserName = userService.findUserByUsername(checkName, true);
        	if (dbUserWithSameUserName!= null && ("db").equals(dbUserWithSameUserName.getSourceSystem())) {
				responseMap.put("message", "DB user with same username present!");
				responseMap.put("status", "error");
            	return new ResponseEntity<Map<String,String>>(responseMap, HttpStatus.OK);
            }
        	
            if (customUsernamePasswordAuthenticationFilter instanceof CustomUsernamePasswordAuthenticationFilter) {
                CustomUsernamePasswordAuthenticationFilter new_name = (CustomUsernamePasswordAuthenticationFilter) customUsernamePasswordAuthenticationFilter;
                password = new_name.decryptPass(password  , (String) request.getSession(false).getAttribute(PASS_PHRASE));
            }
			
            
            ctx = ldapService.bindAsUser(username, password);
        } catch (SystemException e) {
            BaseLoggers.securityLogger.info("A communication exception ocuured while connecting to Active directory",
                    e.getCause());
            throw ldapService.badCredentials();
        }
        

        DirContextOperations ldapUser=null;
        try {
            ldapUser = ldapService.searchForUser(ctx, checkName);
        } catch (NamingException e) {
            BaseLoggers.exceptionLogger.error("Failed to locate directory entry for authenticated user: " + checkName, e);
            throw ldapService.badCredentials(e);
        } finally {
            LdapUtils.closeContext(ctx);
        }
        
        Map<String,Object> updatedOrCreatedUserDetails = ldapService.createUpdateUserFromLdap(checkName, ldapUser);
        User updatedOrCreatedUser = (User)updatedOrCreatedUserDetails.get("user");
        List<String> act_roles=new ArrayList<String>();
        try {
            String[] roles = ldapUser.getStringAttributes(this.roles);
            for(String role:roles){
                act_roles.add(new DistinguishedName(role).removeLast().getValue());
            }
        } catch (Exception e) {
        }
        userSecurityService.syncUserRoles(updatedOrCreatedUser, act_roles);
        responseMap.put("message", updatedOrCreatedUserDetails.get("UserStatus").toString());
        return new ResponseEntity<Map<String,String>>(responseMap, HttpStatus.OK);
    }
    
    @PreAuthorize("hasAuthority('LDAP_CHECKNAME') or hasAuthority('LDAP_IMPORT_USER')")
    @RequestMapping("/newLdapUser")
    public String newLdapUser(){
        return "newLdapUser";
    }
}
