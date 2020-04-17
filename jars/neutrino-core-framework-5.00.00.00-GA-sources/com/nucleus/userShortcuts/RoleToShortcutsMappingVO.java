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
package com.nucleus.userShortcuts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.nucleus.core.role.entity.Role;
import com.nucleus.core.system.util.SystemPropertyUtils;

/**
 * @author Nucleus Software Exports Limited
 */
public class RoleToShortcutsMappingVO implements Serializable {

    private static final long       serialVersionUID = 6252332481000182211L;

    private Role                    role;

    private Long[]                  myFavoritesIds;

    private Long                    myFavouritesCount;

    private Long                    roleId;

    private String                  roleName;

    private List<String>            actions;

    private HashMap<String, Object> viewProperties;

    public void addProperty(String key, Object value) {
        if (viewProperties == null) {
            this.viewProperties = new LinkedHashMap<String, Object>();
        }
        this.viewProperties.put(key, value);
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long[] getMyFavoritesIds() {
        return myFavoritesIds;
    }

    public void setMyFavoritesIds(Long[] myFavoritesIds) {
        this.myFavoritesIds = myFavoritesIds;
    }

    public Long getMyFavouritesCount() {
        return myFavouritesCount;
    }

    public void setMyFavouritesCount(Long myFavouritesCount) {
        this.myFavouritesCount = myFavouritesCount;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public HashMap<String, Object> getViewProperties() {
        return viewProperties;
    }

    public void setViewProperties(HashMap<String, Object> viewProperties) {
        this.viewProperties = viewProperties;
    }
    public String getLogInfo(){
        String log=null;
        StringBuffer stf=new StringBuffer();
        if(role!=null){
            stf.append("RoleID:"+role.getId());
        }
        stf.append(SystemPropertyUtils.getNewline());
        for(Long ids : myFavoritesIds){
            stf.append("Authority IDs:"+ids);
            stf.append(SystemPropertyUtils.getNewline());
        }
        log=stf.toString();
        return log;
    }

}
