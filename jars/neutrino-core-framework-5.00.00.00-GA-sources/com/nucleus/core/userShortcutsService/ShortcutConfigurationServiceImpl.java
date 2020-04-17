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
package com.nucleus.core.userShortcutsService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.config.persisted.vo.MyFavorites;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.userShortcutsDao.UserShortcutsDao;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import com.nucleus.userShortcuts.RoleToShortcutsMapping;
import com.nucleus.userShortcuts.Shortcut;

/**
 * @author Nucleus Software Exports Limited 
 */
@Named("shortcutConfigurationservice")
public class ShortcutConfigurationServiceImpl extends BaseServiceImpl implements ShortcutConfigurationService {

    @Inject
    @Named("entityDao")
    private EntityDao          entityDao;

    @Inject
    @Named("userService")
    protected UserService      userService;

    @Inject
    @Named("userShortCutsDao")
    protected UserShortcutsDao userShortcutsDao;

    private List<Long> fetchUserRoleIds() {
        User user = getCurrentUser().getUserReference();
        List<Role> userRoles = userService.getRolesFromUserId(user.getId());

        List<Long> roleIdList = new ArrayList<Long>();
        for (Role role : userRoles) {
            roleIdList.add(role.getId());
        }
        return roleIdList;
    }

    @Override
    @MonitoredWithSpring(name = "SCSI_FETCH_ROLE_BASED_FAV")
    public List<MyFavorites> fetchRoleBasedFavourites() {

        List<Long> roleIdList = fetchUserRoleIds();

        NamedQueryExecutor<MyFavorites> executor = new NamedQueryExecutor<MyFavorites>(
                "UserShortcuts.fetchRoleBasedFavouritesQuery").addParameter("roleIdList", roleIdList);
        executor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public void saveShortcuts(List<Shortcut> shortcuts) {
        if (shortcuts != null && !shortcuts.isEmpty()) {
            for (Shortcut shortCut : shortcuts) {
                entityDao.persist(shortCut);
            }
        }
    }

    @Override
    @MonitoredWithSpring(name = "SCSI_USER_BASED_SHORTCUTS")
    public List<Shortcut> fetchUserBasedShortcuts(String userUri) {
        NamedQueryExecutor<Shortcut> executor = new NamedQueryExecutor<Shortcut>(
                "UserShortcuts.fetchUserBasedShortcutsQuery").addParameter("userUri", userUri);
        return entityDao.executeQuery(executor);
    }

    @Override
    public void deleteExistingShortCuts(List<Shortcut> shortcuts) {
        if (shortcuts != null && !shortcuts.isEmpty()) {
            for (Shortcut shortcut : shortcuts) {
                entityDao.delete(shortcut);
            }
        }
    }

    @Override
    public void deletePreviouslySavedRoleToShortcutsMappingForGivenRole(Long roleId) {
        NeutrinoValidator.notNull(roleId, "Role Id cannot be null in RoleToShortcutMappings");
        userShortcutsDao.deletePreviouslySavedRoleToShortcutsMappingForGivenRole(roleId);
    }

    @Override
    public void saveRoleToShortcutsMapping(Role role, MyFavorites myFavorites) {
        NeutrinoValidator.notNull(role, "Role cannot be null in RoleToShortcutsMapping");
        NeutrinoValidator.notNull(myFavorites, "MyFavorites cannot be null in RoleToShortcutsMapping");

        // Set Role and MyFavt explicitly to RoleToShortcutsMapping bean.
        RoleToShortcutsMapping roleToShortcutsMapping = new RoleToShortcutsMapping();
        roleToShortcutsMapping.setRole(role);
        roleToShortcutsMapping.setMyFavorites(myFavorites);
        entityDao.persist(roleToShortcutsMapping);
    }

    @Override
    public List<Object> fetchRoleWithMyFavouritesCount() {
        NamedQueryExecutor<Object> executor = new NamedQueryExecutor<Object>("UserShortcuts.fetchRoleWithMyFavouritesCount");
        executor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        List<Object> objects = entityDao.executeQuery(executor);
        return objects;
    }

    @Override
    public List<Long> fetchRoleBasedFavouritesForGivenRole(Long roleId) {
        NeutrinoValidator.notNull(roleId, "Role Id cannot be null in RoleToShortcutMappings");
        NamedQueryExecutor<Long> executor = new NamedQueryExecutor<Long>("UserShortcuts.fetchMyFavouritesForGivenRole")
                .addParameter("roleId", roleId);
        executor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        List<Long> rstmList = entityDao.executeQuery(executor);
        return rstmList;
    }

    @Override
    public List<Role> fetchAllRolesMapped() {
        NamedQueryExecutor<Role> executor = new NamedQueryExecutor<Role>("UserShortcuts.fetchAllRolesMapped");
        executor.addQueryHint(QueryHint.QUERY_HINT_READONLY, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

}
