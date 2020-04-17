package com.nucleus.usersShortCuts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.config.persisted.vo.MyFavorites;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.userShortcutsService.ShortcutConfigurationService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.userShortcuts.RoleToShortcutsMappingVO;
import com.nucleus.userShortcuts.Shortcut;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping(value = "/userShortcuts")
public class UserShortcutsController extends BaseController {

    private final static String                   parentId = "RoleToShortcutsMappingVO";

    @Inject
    @Named("baseMasterService")
    private BaseMasterService              baseMasterService;

    @Inject
    @Named("genericParameterService")
    private GenericParameterService        genericParameterService;

    @Inject
    @Named("entityDao")
    protected EntityDao                    entityDao;

    @Inject
    @Named("shortcutConfigurationservice")
    protected ShortcutConfigurationService shortcutConfigurationService;

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(value = "/mapRoleToShortcuts")
    public String mapRolesToShortCuts(ModelMap map) {
        List<Role> roles = getUnmappedRoles();
        map.put("roles", roles);
        map.put("parentId", parentId);
        map.put("roleToShortcutsMappingVO", new RoleToShortcutsMappingVO());
        return "mapRolesToShortcuts";
    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(value = "/saveRoleToShortcutsMapping", method = RequestMethod.POST)
    public String saveRoleToShortCutsMapping(RoleToShortcutsMappingVO roleToShortcutsMappingVO, ModelMap map,
            BindingResult result,
            @RequestParam(value = "createAnotherMapping", required = false) boolean createAnotherMapping) {
        BaseLoggers.flowLogger.debug("Saving Role to Shortcuts Mapping Details-->"+roleToShortcutsMappingVO.getLogInfo());
        // Validate building object
        RoleToShortcutsMappingValidator roleToShortcutsMappingValidator = new RoleToShortcutsMappingValidator();
        roleToShortcutsMappingValidator.validate(roleToShortcutsMappingVO, result);

        Role role = roleToShortcutsMappingVO.getRole();

        if (result.hasErrors()) {

            List<Role> roles = getUnmappedRoles();
            roles.add(role);

            map.put("roleToShortcutsMappingVO", roleToShortcutsMappingVO);
            map.put("parentId", parentId);
            map.put("roles", roles);
            return "mapRolesToShortcuts";
        }

        // Delete previously saved RoleToShortcutsMapping records for this role
        // if it exists.
        if (roleToShortcutsMappingVO.getRoleId() != null) {
            shortcutConfigurationService.deletePreviouslySavedRoleToShortcutsMappingForGivenRole(roleToShortcutsMappingVO
                    .getRoleId());
        }

        Long[] myFavoritesIds = roleToShortcutsMappingVO.getMyFavoritesIds();

        // Save RoleToShortcutsMapping for each MyFavt
        for (Long myFavoritesId : myFavoritesIds) {
            MyFavorites myFavorites = baseMasterService.getMasterEntityById(MyFavorites.class, myFavoritesId);
            shortcutConfigurationService.saveRoleToShortcutsMapping(role, myFavorites);
        }

        if (createAnotherMapping) {

            List<Role> roles = getUnmappedRoles();
            map.put("roles", roles);
            map.put("parentId", parentId);
            map.put("roleToShortcutsMappingVO", new RoleToShortcutsMappingVO());
            return "mapRolesToShortcuts";
        }

        return "redirect:/app/UserShortcutsDataTable/RoleToShortcutsMappingVO/userShortcuts/loadColumnConfig";

    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('CHECKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('VIEW_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(value = "/view/{id}")
    public String viewRoleToShortCutsMapping(@PathVariable("id") Long roleId, ModelMap map) {
        Role role = baseMasterService.getMasterEntityById(Role.class, roleId);

        List<Long> myFavouritesIdsList = shortcutConfigurationService.fetchRoleBasedFavouritesForGivenRole(roleId);

        RoleToShortcutsMappingVO roleToShortcutsMappingVO = new RoleToShortcutsMappingVO();
        roleToShortcutsMappingVO.setRoleId(roleId);
        roleToShortcutsMappingVO.setRole(role);
        roleToShortcutsMappingVO.setMyFavoritesIds(myFavouritesIdsList.toArray(new Long[myFavouritesIdsList.size()]));

        // Admin's Actions
        map.put("actEdit", false);
        map.put("actDelete", false);

        map.put("roleToShortcutsMappingVO", roleToShortcutsMappingVO);
        map.put("viewable", true);
        List<Role> roles = baseMasterService.getAllApprovedAndActiveEntities(Role.class);
        map.put("roles", roles);
        map.put("parentId", parentId);
        return "mapRolesToShortcuts";

    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(value = "/edit/{id}")
    public String editRoleToShortCutsMapping(@PathVariable("id") Long roleId, ModelMap map) {
        Role role = baseMasterService.getMasterEntityById(Role.class, roleId);

        List<Long> myFavouritesIdsList = shortcutConfigurationService.fetchRoleBasedFavouritesForGivenRole(roleId);

        RoleToShortcutsMappingVO roleToShortcutsMappingVO = new RoleToShortcutsMappingVO();
        roleToShortcutsMappingVO.setRoleId(roleId);
        roleToShortcutsMappingVO.setRole(role);
        roleToShortcutsMappingVO.setMyFavoritesIds(myFavouritesIdsList.toArray(new Long[myFavouritesIdsList.size()]));

        List<Role> roles = getUnmappedRoles();
        roles.add(role);

        // Admin's Actions
        map.put("actDelete", false);

        map.put("roleToShortcutsMappingVO", roleToShortcutsMappingVO);
        map.put("edit", true);

        map.put("roles", roles);
        map.put("parentId", parentId);
        return "mapRolesToShortcuts";

    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(value = "/delete/{id}")
    public String editRoleToShortCutsMapping(@PathVariable("id") Long[] roleIds, ModelMap map) {
        NeutrinoValidator.notNull(roleIds, "Role Id array cannot be null");

        if (roleIds != null && roleIds.length > 0) {
            for (Long roleId : roleIds) {
                shortcutConfigurationService.deletePreviouslySavedRoleToShortcutsMappingForGivenRole(roleId);
            }
        }
        return "redirect:/app/UserShortcutsDataTable/RoleToShortcutsMappingVO/userShortcuts/loadColumnConfig";
    }

    private List<Role> getUnmappedRoles() {

        List<Role> mappedRoleList = shortcutConfigurationService.fetchAllRolesMapped();
        List<Role> roles = baseMasterService.getAllApprovedAndActiveEntities(Role.class);

        // Remove roles from the List Of Roles which have been mapped.
        Iterator<Role> roleIterator = roles.listIterator();
        if (mappedRoleList != null && roles != null) {
            while (roleIterator.hasNext()) {
                Role roleToRemove = roleIterator.next();
                for (Role roleToCheckWith : mappedRoleList) {
                    if (roleToCheckWith.equals(roleToRemove)) {
                        roleIterator.remove();
                        break;
                    }
                }
            }
        }
        return roles;
    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('CHECKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('VIEW_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping("/fetchUserBasedShortcuts")
    @MonitoredWithSpring(name = "USC_FETCH_USR_BASED_SHORTCUTS")
    public String fetchUserBasedShortcuts(ModelMap map) {
        List<Shortcut> userBasedShortcuts = shortcutConfigurationService.fetchUserBasedShortcuts(getUserDetails()
                .getUserEntityId().getUri());
        map.put("userBasedShortcuts", userBasedShortcuts);
        return "myShortcuts";
    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('CHECKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('VIEW_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping("/fetchRoleBasedFavorites")
    @MonitoredWithSpring(name = "USC_FETCH_ROLE_BASED_FAVORITES")
    public String fetchRoleBasedFavorites(ModelMap map) {

        List<MyFavorites> roleBasedFavorites = shortcutConfigurationService.fetchRoleBasedFavourites();
        map.put("roleBasedFavorites", roleBasedFavorites);

        return "manageShortcuts";

    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(value = "/saveShortcuts", method = RequestMethod.POST)
    public @ResponseBody
    void saveShortcuts(@RequestParam Long[] selectedFavs, ModelMap map) {
        String userUri = getUserDetails().getUserEntityId().getUri();
        List<Shortcut> fetchOldShortCuts = shortcutConfigurationService.fetchUserBasedShortcuts(userUri);
        shortcutConfigurationService.deleteExistingShortCuts(fetchOldShortCuts);

        if (selectedFavs != null && selectedFavs.length > 0) {
            MyFavorites favorite = null;
            List<Shortcut> shortcutsToBeSaved = new ArrayList<Shortcut>();
            for (Long selectedFav : selectedFavs) {
                favorite = baseMasterService.getMasterEntityById(MyFavorites.class, selectedFav);
                Shortcut shortcut = new Shortcut();
                shortcut.setMyFavorites(favorite);
                shortcut.setUserUri(userUri);
                shortcutsToBeSaved.add(shortcut);
            }
            shortcutConfigurationService.saveShortcuts(shortcutsToBeSaved);
        }
    }

}
