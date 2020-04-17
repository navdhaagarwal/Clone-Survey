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

import java.util.List;

import com.nucleus.config.persisted.vo.MyFavorites;
import com.nucleus.core.role.entity.Role;
import com.nucleus.service.BaseService;
import com.nucleus.userShortcuts.Shortcut;

/**
 * @author Nucleus Software Exports Limited
 */
public interface ShortcutConfigurationService extends BaseService {

    public List<MyFavorites> fetchRoleBasedFavourites();

    public void saveShortcuts(List<Shortcut> shortcuts);
    
    public List<Shortcut> fetchUserBasedShortcuts(String userUri);
    
    public void deleteExistingShortCuts(List<Shortcut> shortcuts);
    
    public void deletePreviouslySavedRoleToShortcutsMappingForGivenRole(Long roleId);
    
    public void saveRoleToShortcutsMapping(Role role, MyFavorites myFavorites);
    
    public List<Object> fetchRoleWithMyFavouritesCount();
    
    public List<Long> fetchRoleBasedFavouritesForGivenRole(Long roleId);
    
    public List<Role> fetchAllRolesMapped();
    
    
}
