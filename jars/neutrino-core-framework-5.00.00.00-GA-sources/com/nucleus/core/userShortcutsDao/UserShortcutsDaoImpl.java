package com.nucleus.core.userShortcutsDao;

import javax.inject.Named;
import javax.persistence.Query;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.persistence.BaseDaoImpl;
import com.nucleus.userShortcuts.RoleToShortcutsMapping;

@Named("userShortCutsDao")
public class UserShortcutsDaoImpl extends BaseDaoImpl<RoleToShortcutsMapping> implements UserShortcutsDao {

    @Override
    public void deletePreviouslySavedRoleToShortcutsMappingForGivenRole(Long roleId) {
        NeutrinoValidator.notNull(roleId, "Role Id cannot be null in RoleToShortcutMappings");
        String qlString = "delete from RoleToShortcutsMapping rtsm where rtsm.role.id=:roleId";
        Query qry = getEntityManager().createQuery(qlString);
        qry.setParameter("roleId", roleId);
        qry.executeUpdate();
    }

}
