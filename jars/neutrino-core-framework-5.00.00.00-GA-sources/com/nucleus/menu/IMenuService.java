package com.nucleus.menu;


import com.nucleus.authority.Authority;
import com.nucleus.user.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IMenuService {

    Set<Authority> getAuthoritiesOfUserFromRole(UserInfo user);
    public List<MenuVO> getAllValidMenuList(String productName);
    public MenuRootVO filterMenuByAuthorities(MenuRootVO menuRootVO, UserInfo user);
    MenuRootVO filterMenuByListOfAuthorities(MenuRootVO menuRootVO, Set<Authority> auth);
    List<MenuVO> simpleFilterBasedOnAuthorities(List<MenuVO> menuList,Set<Authority> authSet);
    List<String> getAuthorityNamesFromSet(Set<Authority> authSet);
    void nullifyUnwantedValuesForMenuTab(MenuRootVO menuRootVO);
    public List<MenuVO> getUserBasedMenuList(boolean isAuthorityToBeAdded, UserInfo user, List<MenuVO> menuVOList);
    public void setFinalMenu();
    MenuRootVO setFinalMenuWithoutCache(String productName);
    public MenuRootVO getFinalMenu(String productName);
    public String objectToJson(MenuRootVO mr);
    public String jsonToObject(String jsonString);
    public Map returnProperties(String jsonString);

    List<MenuVO> toFlatList(List<MenuVO> menuVO, List<MenuVO> menuVOList);

    public String softDelete(Long id) throws IOException;
    List<Map<String, ?>> searchMenu(String className, String itemVal,
                                    String[] searchColumnList, String value,
                                    Boolean loadApprovedEntityFlag, String itemsList,
                                    Boolean strictSearchOnitemsList, int page, List<MenuVO> menuVOList);
    List<MenuVO> getMenuByProductAndRole(String sourceProduct,Long[] roleIds);
    List<MenuVO> filterMenuForClickableItems(List<MenuVO> menuVoList);

    List<Map<String,?>> searchMenuItemsForString(String className, String itemVal, String[] searchColumnList, String value, Boolean loadApprovedEntityFlag, String itemsList, Boolean strictSearchOnitemsList, int page, List<MenuVO> menuVOList);
    List<Map<String, ?>> filterForMatchingLabelAndName(List<MenuVO> menuVOList,String pattern);

    public FrequentMenuVO getFreqMenu(UserInfo user);
    public void setFreqMenu(FrequentMenuVO frequentMenuVO,UserInfo user);
}