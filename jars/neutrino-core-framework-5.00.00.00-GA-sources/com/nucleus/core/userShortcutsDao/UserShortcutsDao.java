package com.nucleus.core.userShortcutsDao;

import com.nucleus.persistence.BaseDao;
import com.nucleus.userShortcuts.RoleToShortcutsMapping;

public interface UserShortcutsDao extends BaseDao<RoleToShortcutsMapping> {

    public void deletePreviouslySavedRoleToShortcutsMappingForGivenRole(Long roleId);

}
