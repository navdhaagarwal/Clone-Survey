package com.nucleus.usersShortCuts;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.nucleus.userShortcuts.RoleToShortcutsMappingVO;
import com.nucleus.web.common.controller.CASValidationUtils;

public class RoleToShortcutsMappingValidator extends CASValidationUtils implements Validator{

    @Override
    public boolean supports(Class<?> clazz) {
        return RoleToShortcutsMappingVO.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RoleToShortcutsMappingVO roleToShortcutsMappingVO = (RoleToShortcutsMappingVO) target;

        if(roleToShortcutsMappingVO.getRole() == null || roleToShortcutsMappingVO.getRole().getId()==null) {
            errors.reject("role", "label.common.required");
        }

        if(roleToShortcutsMappingVO.getMyFavoritesIds()==null || roleToShortcutsMappingVO.getMyFavoritesIds().length<1) {
            errors.reject("myFavoritesIds", "label.common.required");
        }
        
    }
}
