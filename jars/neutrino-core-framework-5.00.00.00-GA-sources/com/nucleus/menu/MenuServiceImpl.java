package com.nucleus.menu;

import com.nucleus.authority.Authority;
import com.nucleus.autocomplete.AutocompleteService;
import com.nucleus.core.misc.util.StringUtil;
import com.nucleus.core.role.entity.Role;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.batch.support.ReflectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.nucleus.core.initialization.ProductInformationLoader;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.initialization.ProductInformationLoader;
import org.xlsx4j.sml.Col;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.*;

@Named("menuService")
public class MenuServiceImpl extends BaseServiceImpl implements IMenuService{

    @Inject
    @Named("userService")
    UserService userService;

    @Inject
    @Named("messageSource")
    private MessageSource messageSource;

    @Inject
    @Named("autocompleteService")
    private AutocompleteService autocompleteService;

    @Value(value = "${dynamic.menu.levels}")
    private String  menuLevelProperty;

    @Value(value = "${one.level.count}")
    private String  oneLevelCount;

    @Value(value = "${two.level.count}")
    private String  twoLevelCount;

    @Value(value = "${three.level.count}")
    private String  threeLevelCount;

    @Value(value = "${menu.type.selected}")
    private String menuTypeSelected;

    @Value(value= "${menu.locale.config}")
    private String menuLocaleConfig;

    @Value(value = "${allow.text.select}")
    private String allowTextSelect;

    @Value(value = "${allow.text.copy}")
    private String allowTextCopy;

    @Value(value = "${allow.text.paste}")
    private String allowTextPaste;

    private boolean isAuthority;
    //private MenuRootVO mr;
    private Map<String, Object> menuRootConfig = new HashMap<>();
    private String MENU_ROOT_KEY="MENU_ROOT";
    private String MENU_LABEL_ISSET_KEY="MENU_LABEL_ISSET";

    @Override
    public Set<Authority> getAuthoritiesOfUserFromRole(UserInfo user){
        Set<Authority> authorities = new HashSet<>();
        List<Role> userRoles = rolesAssociatedWithCurrentUser(user);
        return getAuthoritiesFromRoles(userRoles);
    }

    private Set<Authority> getAuthoritiesFromRoles(List<Role> userRoles) {
        Set<Authority> authorities = new HashSet<>();
        if(userRoles!=null) {
            for (Role userRole : userRoles) {
                if(userRole.getAuthorities() != null) {
                    authorities.addAll(userRole.getAuthorities());
                }
            }
        }
        return authorities;
    }

    private List<Role> rolesAssociatedWithCurrentUser(UserInfo user){
        List<Role> userRoles = null;
        UserInfo currentUser = user;
        if(currentUser!=null) {
            userRoles = userService.getRolesFromUserId(currentUser.getId());
        }
        return userRoles;
    }


    @Override
    public List<MenuVO> getAllValidMenuList(String productName) {
        List<MenuEntity> authorityListFromEntity = getValidDatabaseRecords(productName);
        List<MenuVO> menuVOList = new ArrayList<>();
        MenuRootVO menuRootVO = new MenuRootVO();
        for(MenuEntity me : authorityListFromEntity){
            MenuVO menuVO = new MenuVO();
            menuVO.setId(me.getId());
            menuVO.setMenuCode(me.getMenuCode());
            menuVO.setMenuName(me.getMenuName());
            menuVO.setAuth(me.getAuth());
            menuVO.setToolTip(me.getToolTip());
            menuVO.setUrl(me.getUrl());
            menuVO.setMenuOrder(me.getMenuOrder());
            menuVO.setMenuLevel(me.getMenuLevel());
            menuVO.setProduct(me.getProduct());
            menuVO.setParent(me.getParent());
            menuVO.setShortcut(me.getShortcut());
            if((me.getEntityLifeCycleData()!=null)
                    && me.getEntityLifeCycleData().getSystemModifiableOnly()!=null
                    && me.getEntityLifeCycleData().getSystemModifiableOnly()) {
                menuVO.setSystemDefined(me.getEntityLifeCycleData().getSystemModifiableOnly());
            } else {
                menuVO.setSystemDefined(false);
            }
            menuVO.setMovable(me.isMovable());
            menuVO.setActive(me.isActive());
            menuVO.setLinkedFunction(me.getLinkedFunction());
            menuVO.setDivided(me.isDivided());
            menuVO.setImageLinkedFunction(me.getImageLinkedFunction());
            menuVO.setImageLinkedUrl(me.getImageLinkedUrl());
            menuVO.setIconClassName(me.getIconClassName());
            menuVO.setElementId(me.getElementId());
            menuVO.setDividedBtnId(me.getDividedBtnId());
            menuVOList.add(menuVO);
        }
        return menuVOList;
    }

    @Override
    public List<MenuVO> getUserBasedMenuList(boolean isAuthorityToBeAdded, UserInfo user, List<MenuVO> menuVOList){
        isAuthority = isAuthorityToBeAdded;
        List<MenuVO> validMenuList = new ArrayList<>();
        if(isAuthority){
            Set<Authority> auth = getAuthoritiesOfUserFromRole(user); //Authorities of user (eg casadmin)
            List<String> authCodesOfUser = new ArrayList<>(); //will contain list of authority code that exists in every authority class
            auth.forEach(item -> authCodesOfUser.add(item.getAuthCode())); //get authorities code in roleAuthorities
            for (MenuVO menuVO : menuVOList) {
                Set<String> setOfAuthoritiesInDatabase = new TreeSet<>();
                if(menuVO.getAuth()!=null){
                    setOfAuthoritiesInDatabase.addAll(Arrays.asList(menuVO.getAuth().split(",")));}
                boolean checkMenuLevel = isNotEmpty(menuVO.getMenuLevel()) && (menuVO.getMenuLevel().equalsIgnoreCase("one") || menuVO.getMenuLevel().equalsIgnoreCase("two"));
                boolean checkURLexists = (isNotEmpty(menuVO.getUrl()) || isNotEmpty(menuVO.getLinkedFunction()));
                if ( (CollectionUtils.containsAny(authCodesOfUser, setOfAuthoritiesInDatabase) && checkURLexists) ||
                        (checkMenuLevel && !checkURLexists)) {
                    validMenuList.add(menuVO);
                }
            }
            return validMenuList;
        }
        else {
            return menuVOList;
        }
    }

    @Override
    public MenuRootVO getFinalMenu(String productName) {
        /*if(menuRootConfig.containsKey(MENU_ROOT_KEY)){
            MenuRootVO mr = (MenuRootVO) menuRootConfig.get(MENU_ROOT_KEY);
            String labelIsSet = (String) menuRootConfig.get(MENU_LABEL_ISSET_KEY);
            if(labelIsSet.equals("false")) {
                setLabelMenuItems(mr.getMenuEntities());
                menuRootConfig.put(MENU_ROOT_KEY, mr);
                menuRootConfig.put(MENU_LABEL_ISSET_KEY, "true");
            }
            return mr;
        }*/
        MenuRootVO menuRootVO = setFinalMenuWithoutCache(productName);
        //menuRootVO = (MenuRootVO) menuRootConfig.get(MENU_ROOT_KEY);
        setLabelMenuItems(menuRootVO.getMenuEntities());
        return menuRootVO;
    }

    @Override
    public MenuRootVO filterMenuByAuthorities(MenuRootVO menuRootVOExisiting, UserInfo user){
        //Set<Authority> auth = getAuthoritiesOfUserFromRole(user);
        if(user==null && user.getAuthorities()==null){
            BaseLoggers.flowLogger.error("Either user was null or authority was null");
        }

        MenuRootVO menuRootVO = menuRootVOExisiting;
        if(menuRootVO == null) {
            //menuRootVO = (MenuRootVO) menuRootConfig.get(MENU_ROOT_KEY);
            menuRootVO = setFinalMenuWithoutCache(ProductInformationLoader.getProductName());
            setLabelMenuItems(menuRootVO.getMenuEntities());
        }

        Set<Authority> auth = user.getUserAuthorities();//Authorities of user (eg casadmin)
        return filterMenuByListOfAuthorities(menuRootVO, auth);
    }

    @Override
    public MenuRootVO filterMenuByListOfAuthorities(MenuRootVO menuRootVO, Set<Authority> auth) {
        Set<String> authCodesOfUser = new HashSet<String>(); //will contain list of authority code that exists in every authority class
        List<MenuVO> validMenuList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(auth)) {
            for (Authority authCode: auth) {
                authCodesOfUser.add(authCode.getAuthority());
            }
            for (MenuVO item : menuRootVO.getMenuEntities()) {
                MenuVO newItem = item;
                internalFilterMenuByAuthorities(item.getSubLevel(), authCodesOfUser, newItem);
                if (CollectionUtils.isNotEmpty(newItem.getSubLevel()) || StringUtils.isNotEmpty(newItem.getUrl()) || StringUtils.isNotEmpty(newItem.getLinkedFunction())) {
                    validMenuList.add(newItem);
                }
            }
        }
        MenuRootVO finalObj = new MenuRootVO();
        finalObj.setMenuEntities(validMenuList);
        return finalObj;
    }

    @Override
    public List<MenuVO> simpleFilterBasedOnAuthorities(List<MenuVO> menuList,Set<Authority> authSet){
        if(CollectionUtils.isEmpty(menuList)){
            return menuList;
        }
        List<String> userAuthorities=getAuthorityNamesFromSet(authSet);
        List<MenuVO> filteredList=new ArrayList<>();
        for(MenuVO item:menuList){
            if(StringUtils.isEmpty(item.getAuth()) || CollectionUtils.containsAny(userAuthorities,Arrays.asList(item.getAuth().split(",")))){
                filteredList.add(item);
            }
        }
        return filteredList;
    }

    @Override
    public List<String> getAuthorityNamesFromSet(Set<Authority> authSet){
        List<String> userAuthorities=new ArrayList<>();
        if(CollectionUtils.isEmpty(authSet)){
            return userAuthorities;
        }
        for(Authority authority:authSet){
            userAuthorities.add(authority.getAuthCode());
        }
        return userAuthorities;
    }

    @Override
    public void nullifyUnwantedValuesForMenuTab(MenuRootVO menuRootVO){
        for (MenuVO item : menuRootVO.getMenuEntities()) {

            if (CollectionUtils.isNotEmpty(item.getSubLevel()) || StringUtils.isNotEmpty(item.getUrl()) || StringUtils.isNotEmpty(item.getLinkedFunction())) {
                setUnrequiredValuesNullForMenuTab(item);
            }
        }
    }

    private void internalFilterMenuByAuthorities(List<MenuVO> menuItems, Set<String> authCodesOfUser, MenuVO validMenuItem){
        if(CollectionUtils.isNotEmpty(menuItems)){
            List<MenuVO> elem = new ArrayList<MenuVO>();
            for (MenuVO item : menuItems) {
                MenuVO newItem = item;
                List<MenuVO> subMenuItems = item.getSubLevel();
                Set<String> setOfAuthoritiesInDatabase = new TreeSet<>();
                if(item.getAuth()!=null){
                    setOfAuthoritiesInDatabase.addAll(Arrays.asList(item.getAuth().split(",")));
                }
                boolean checkMenuLevel = isNotEmpty(item.getMenuLevel()) && (item.getMenuLevel().equalsIgnoreCase("one") || item.getMenuLevel().equalsIgnoreCase("two"));
                boolean checkURLexists = (isNotEmpty(item.getUrl()) || isNotEmpty(item.getLinkedFunction()));

                if( (CollectionUtils.containsAny(authCodesOfUser, setOfAuthoritiesInDatabase) && checkURLexists) ||
                        (checkMenuLevel && !checkURLexists) || ((isEmpty(item.getAuth())))) {
                    List<MenuVO> subValidMenuList = new ArrayList<>();
                    internalFilterMenuByAuthorities(subMenuItems, authCodesOfUser, newItem);
                    if (!newItem.getMenuLevel().equalsIgnoreCase("three") &&
                            (CollectionUtils.isNotEmpty(newItem.getSubLevel()) ||  checkURLexists)
                            && (CollectionUtils.containsAny(authCodesOfUser, setOfAuthoritiesInDatabase))){
                        setUnrequiredValuesNullForMenuTab(newItem);
                        elem.add(newItem);
                    }
                    else if(!newItem.getMenuLevel().equalsIgnoreCase("three") &&
                            (CollectionUtils.isNotEmpty(newItem.getSubLevel()) ||  checkURLexists)
                            && (isEmpty(item.getAuth()))){
                        setUnrequiredValuesNullForMenuTab(newItem);
                        elem.add(newItem);
                    }
                    else if (newItem.getMenuLevel().equalsIgnoreCase("three") && (isEmpty(item.getAuth()))){
                        setUnrequiredValuesNullForMenuTab(newItem);
                        elem.add(newItem);
                    }
                    else if (newItem.getMenuLevel().equalsIgnoreCase("three") && (CollectionUtils.containsAny(authCodesOfUser, setOfAuthoritiesInDatabase) && checkURLexists)) {
                        setUnrequiredValuesNullForMenuTab(newItem);
                        elem.add(newItem);
                    }
                }
            }
            validMenuItem.setSubLevel(elem);
        }
    }

    private void setLabelMenuItems(List<MenuVO> menuItems){
        if(CollectionUtils.isNotEmpty(menuItems)){
            for (MenuVO item : menuItems) {
                List<MenuVO> subMenuItems = item.getSubLevel();
                setLabelName(item, getUserLocale());
                if(isNotEmpty(item.getParentName())) {
                    String parentName = messageSource.getMessage(item.getParentName(), null, getUserLocale());
                    item.setParentName(parentName);
                }
                if(CollectionUtils.isNotEmpty(subMenuItems)) {
                    setLabelMenuItems(subMenuItems);
                }
            }
        }
    }


    @Override
    public void setFinalMenu() {

    }

    @Override
    public  MenuRootVO setFinalMenuWithoutCache(String productName){
        List<MenuVO> menuItemList = getAllValidMenuList(productName);
        List<MenuVO> parentWithoutChild = new ArrayList<>();
        List<MenuVO> finalMenuList = new ArrayList<>();
        finalList(menuItemList);
        MenuRootVO mr = new MenuRootVO();
        List<MenuVO> previousMenuList = null;
        String[] definedMenulevels = menuLevelProperty.split(",");
        if (definedMenulevels.length > 0) {
            for (String menuLvl : definedMenulevels) {
                List<MenuVO> childFilteredMenu = new ArrayList<MenuVO>();
                for (MenuVO childFilteredItems : menuItemList) {
                    if(childFilteredItems.getMenuLevel().equalsIgnoreCase(menuLvl)){
                        childFilteredMenu.add(childFilteredItems);
                    }
                }
                /*List<MenuVO> childFilteredMenu = menuItemList.stream()
                        .filter(item -> item.getMenuLevel().equalsIgnoreCase(menuLvl))
                        .collect(Collectors.toList());*/
                setChildMenuItems(mr, previousMenuList, childFilteredMenu);
                previousMenuList = childFilteredMenu;
            }
        }
        if (mr.getMenuEntities() != null && mr.getMenuEntities().size() > 0) {
            setMenuRootInMemory(mr);
        }
        return mr;
    }

    private void setMenuRootInMemory(MenuRootVO mr){
        menuRootConfig.put(MENU_ROOT_KEY, mr);
        menuRootConfig.put(MENU_LABEL_ISSET_KEY, "false");
    }

    private void setChildMenuItems(MenuRootVO mr, List<MenuVO> parentMenuList, List<MenuVO> childMenuFilteredList) {
        if (parentMenuList == null) {
            parentMenuList = new ArrayList<>();
            Collections.sort(childMenuFilteredList, (o1, o2) -> (o1.getMenuOrder().compareTo(o2.getMenuOrder())));
            parentMenuList.addAll(childMenuFilteredList);
            mr.setMenuEntities(parentMenuList);
        } else {
            for (MenuVO menu : parentMenuList) {
                String currentMenuCode = menu.getMenuCode();
                List<MenuVO> subList = childMenuFilteredList.stream()
                        .filter(c -> c.getParent()!=null)
                        .filter(c -> c.getParent().equalsIgnoreCase(currentMenuCode))
                        .collect(Collectors.toList());
                Collections.sort(subList, (o1, o2) -> (o1.getMenuOrder().compareTo(o2.getMenuOrder())));
                menu.setSubLevel(subList);
            }
        }
    }

    @Override
    public String objectToJson(MenuRootVO mr){
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = null;
        MenuRootVO menuRootVO = new MenuRootVO();

        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            jsonInString = mapper.writeValueAsString(mr);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("**** Error while converting json from object ****", e);
        }
        return jsonInString;
    }

    @Override
    public String jsonToObject(String jsonString){
        ObjectMapper mapper = new ObjectMapper();
        String message = "";
        try {
            MenuRootVO menuRootVO = mapper.readValue(jsonString, MenuRootVO.class);
            List<MenuVO> menuRootList = menuRootVO.getMenuEntities();
            //Collections.sort(menuRootList, (o1, o2) -> (o1.getId().compareTo(o2.getId())));
            List<MenuEntity> menuEntityList = new ArrayList<>();
            List<MenuVO> menuVOList = new ArrayList<>();
            menuVOList = toFlatList(menuRootList, menuVOList);
            for(MenuVO me : menuVOList){
                MenuEntity menuEntity = null;
                if(me.getId() == null){
                    menuEntity = new MenuEntity();
                    if(menuEntity.getEntityLifeCycleData()!=null) {
                        menuEntity.getEntityLifeCycleData().setSystemModifiableOnly(false);
                    }
                    menuEntity.setMovable(me.isMovable());
                    menuEntity.setActive(me.isActive());
                    menuEntity.setProduct(ProductInformationLoader.getProductName());
                }
                else {
                    menuEntity = entityDao.find(MenuEntity.class, me.getId());
                }
                menuEntity.setMenuCode(me.getMenuCode());
                menuEntity.setMenuName(me.getMenuName());
                menuEntity.setAuth(me.getAuth());
                menuEntity.setToolTip(me.getToolTip());
                menuEntity.setUrl(me.getUrl());
                menuEntity.setMenuOrder(me.getMenuOrder());
                menuEntity.setMenuLevel(me.getMenuLevel());
                menuEntity.setParent(me.getParent());
                menuEntity.setShortcut(me.getShortcut());
                menuEntity.setLinkedFunction(me.getLinkedFunction());
                menuEntity.setDivided(me.isDivided());
                menuEntity.setImageLinkedFunction(me.getImageLinkedFunction());
                menuEntity.setImageLinkedUrl(me.getImageLinkedUrl());
                menuEntity.setIconClassName(me.getIconClassName());
                menuEntity.setElementId(me.getElementId());
                menuEntity.setDividedBtnId(me.getDividedBtnId());

                saveOrUpdateForMenu(menuEntity);
            }

        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("**** Error while converting object to json ****", e);
            message =  "Error while Saving data!";
            return message;
        }
        menuRootConfig.remove(MENU_ROOT_KEY);
        menuRootConfig.put(MENU_LABEL_ISSET_KEY, "false");
        setFinalMenu();
        message =  "Successfully Saved!";
        return message;
    }

    @Override
    public Map returnProperties(String jsonString){
        Map<String, Object> map = new HashedMap();
        map.put("jsonString",jsonString);
        map.put("menuLevelProperty",menuLevelProperty);
        map.put("oneLevelCount",oneLevelCount);
        map.put("twoLevelCount",twoLevelCount);
        map.put("threeLevelCount",threeLevelCount);
        map.put("menuTypeSelected",menuTypeSelected);
        //map.put("authCodes",getAuthCodes());
        //map.put("messageResource",getMessageResources());
        map.put("menuCodeCount",getTableCount());
        map.put("allowTextPaste",allowTextPaste);
        map.put("allowTextCopy",allowTextCopy);
        map.put("allowTextSelect",allowTextSelect);
        return map;
    }

    private void saveOrUpdateForMenu(MenuEntity menuEntity){
        entityDao.saveOrUpdate(menuEntity);
    }

    @Override
    public List<MenuVO> toFlatList(List<MenuVO> menuVO, List<MenuVO> menuVOList){
        for(MenuVO m:menuVO){
            if(m.getSubLevel()!=null) {
                menuVOList.add(m);
                toFlatList(m.getSubLevel(), menuVOList);
            }
            else{
                menuVOList.add(m);
            }
        }
        return menuVOList;

    }

    private void setLabelName(MenuVO menuVO, Locale locale){
        String label = menuVO.getMenuName();
        String displayName = null;
        if(locale == null){
            locale = Locale.ENGLISH;
        }
        //label.replace("dynaMenu.","");
        displayName = messageSource.getMessage(label, null, locale);
        if(isEmpty(displayName)){
            menuVO.setMenuDisplayName(label);
        } else {
            menuVO.setMenuDisplayName(displayName);
        }
    }

    private void finalList(List<MenuVO> validMenuList){
        Iterator<MenuVO> iter = new ArrayListIterator();
        iter = validMenuList.iterator();
        while(iter.hasNext()) {
            MenuVO menu = iter.next();
            //setLabelName(menu, getUserLocale());
            String currentMenuCode = menu.getMenuCode();
            List<MenuVO> subList = validMenuList.stream()
                    .filter(c -> c.getParent()!=null)
                    .filter(c -> c.getParent().equalsIgnoreCase(currentMenuCode))
                    //.peek(c ->  setLabelName(c, getUserLocale()))
                    .peek(c -> c.setParentName(menu.getMenuName()))
                    .collect(Collectors.toList());
            if(isAuthority && (menu.getUrl()==null) && (subList.size() == 0) && (menu.getMenuLevel()!=null) && (!menu.getMenuLevel().equalsIgnoreCase(menuLevelProperty.substring(menuLevelProperty.lastIndexOf(",")+1)))){
                iter.remove();
            }
        }
    }

    private Long getTableCount(){
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>("Menu.getTableCount");
        Long count = entityDao.executeTotalRowsQuery(executor);
        return count;
    }

    public List<MenuEntity> getValidDatabaseRecords(String productName){
        String paramValue = "%"+ productName;
        NamedQueryExecutor<MenuEntity> executor = new NamedQueryExecutor<MenuEntity>("Menu.getValidDatabaseRecords")
                .addLikeParameter("product", paramValue,true)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        return entityDao.executeQuery(executor);
    }

    @Override
    public String softDelete(Long id) throws IOException {
        if(id!=null) {
            MenuEntity me = new MenuEntity();
            me = entityDao.find(MenuEntity.class, id);
            me.setActive(false);
            entityDao.saveOrUpdate(me);
            menuRootConfig.remove(MENU_ROOT_KEY);
            menuRootConfig.put(MENU_LABEL_ISSET_KEY, "false");
            setFinalMenu();
            return "Deleted";
        }
        return "Item could not be deleted";
    }


    private void setUnrequiredValuesNullForMenuTab(MenuVO newItem){
        newItem.setId(null);
        newItem.setMenuName(null);
        newItem.setAuth(null);
        newItem.setMenuOrder(null);
        newItem.setMenuLevel(null);
        newItem.setProduct(null);
        newItem.setParent(null);
        newItem.setParentName(null);
    }

    @Override
    public List<Map<String, ?>> searchMenu(String className, String itemVal,
                                           String[] searchColumnList, String value,
                                           Boolean loadApprovedEntityFlag, String itemsList,
                                           Boolean strictSearchOnitemsList, int page, List<MenuVO> menuVOList) {
        List<Map<String, ?>> list = new ArrayList<>();
        return list;
    }

    @Override
    public List<MenuVO> getMenuByProductAndRole(String sourceProduct,Long[] roleIds){
        List<MenuVO> menuVOList=new ArrayList<>();
        if(roleIds==null || roleIds.length==0){
            return menuVOList;
        }
        List<Role> roleList=new ArrayList<>();
        for(Long roleId:roleIds){
            roleList.add(userService.getRoleById(roleId));
        }
        Set<Authority> authorities= getAuthoritiesFromRoles(roleList);
        MenuRootVO menuRootVO=setFinalMenuWithoutCache(sourceProduct);

        if(menuRootVO==null || CollectionUtils.isEmpty(menuRootVO.getMenuEntities())){
            return new ArrayList<>();
        }
        menuVOList=menuRootVO.getMenuEntities();
        List<MenuVO> flatMenuList=new ArrayList<>();
        toFlatList(menuVOList,flatMenuList);
        flatMenuList = simpleFilterBasedOnAuthorities(flatMenuList,authorities);
        flatMenuList= filterMenuForClickableItems(flatMenuList);
        setLabelMenuItems(flatMenuList);
        return  flatMenuList;

    }

    @Override
    public List<MenuVO> filterMenuForClickableItems(List<MenuVO> menuVoList) {
        if(CollectionUtils.isEmpty(menuVoList)){
            return new ArrayList<>();
        }
        List<MenuVO> clickableItems=new ArrayList<>();
        for(MenuVO entry:menuVoList){
            if(StringUtils.isNotEmpty(entry.getUrl()) || StringUtils.isNotEmpty(entry.getLinkedFunction())){
                clickableItems.add(entry);
            }
        }
        return clickableItems;
    }

    @Override
    public List<Map<String, ?>> searchMenuItemsForString(String className, String itemVal, String[] searchColumnList, String value, Boolean loadApprovedEntityFlag, String itemsList, Boolean strictSearchOnitemsList, int page, List<MenuVO> menuVOList) {

        List<Map<String, ?>> paginatedList = new ArrayList<>();

        if(CollectionUtils.isEmpty(menuVOList)){
            return new ArrayList<>();
        }
        List<Map<String, ?>> newList = filterForMatchingLabelAndName(menuVOList,value);

        if (!newList.isEmpty()) {
            int startIndex = page * 3;
            int endIndex = (page * 3) + 2;
            if (endIndex < newList.size()) {
                paginatedList.addAll(newList.subList(startIndex, (endIndex + 1)));
            } else {
                endIndex = newList.size() - 1;
                paginatedList.addAll(newList.subList(startIndex, endIndex));
                paginatedList.add(newList.get(endIndex));
            }
        }

        HashMap sizeMap = new HashMap();
        sizeMap.put("size", newList.size());
        paginatedList.add(sizeMap);
        return paginatedList;

    }

    @Override
    public List<Map<String, ?>> filterForMatchingLabelAndName(List<MenuVO> menuVOList,String pattern){
        List<Map<String, ?>> matchingList=new ArrayList<>();
        if(StringUtils.isNotEmpty(pattern)){
            pattern=pattern.trim().replaceAll("%","").toLowerCase();        }
        Map<String, ?> matchingEntry=null;
        LinkedHashMap tempMap=null;
        for(MenuVO entry:menuVOList){
            if(StringUtils.isEmpty(pattern)||entry.getMenuDisplayName().toLowerCase().contains(pattern)) {
                matchingEntry=new LinkedHashMap<>();
                tempMap=new LinkedHashMap<>();
                tempMap.put("id",entry.getId());
                tempMap.put("menuCode",entry.getMenuCode());
                tempMap.put("menuName",entry.getMenuName());
                tempMap.put("menuDisplayName",entry.getMenuDisplayName());
                matchingEntry=tempMap;
                matchingList.add(matchingEntry);
            }

        }
        matchingList.sort((o1, o2) -> {
            if(o1.get("menuCode").toString().equalsIgnoreCase(o2.get("menuCode").toString())){
                if(o1.get("menuName").toString().equalsIgnoreCase(o2.get("menuName").toString())){
                    return o1.get("menuDisplayName").toString().compareToIgnoreCase(o2.get("menuDisplayName").toString());
                }else{
                    return o1.get("menuName").toString().compareToIgnoreCase(o2.get("menuName").toString());
                }
            }else{
                return o1.get("menuCode").toString().compareToIgnoreCase(o2.get("menuCode").toString());
            }
        });
       return matchingList;
    }

    @Override
    public FrequentMenuVO getFreqMenu(UserInfo user){
        FrequentMenuVO frequentMenuVO = new FrequentMenuVO();
        if(user != null && user.getId() != null){
            FrequentMenu freqMenu = getFrequentMenu(user);
            if(freqMenu != null){
                frequentMenuVO.setMenuCode1(freqMenu.getMenuCode1());
                frequentMenuVO.setMenuCode2(freqMenu.getMenuCode2());
                frequentMenuVO.setMenuCode3(freqMenu.getMenuCode3());
                frequentMenuVO.setMenuCode4(freqMenu.getMenuCode4());
                frequentMenuVO.setMenuCode5(freqMenu.getMenuCode5());
                return frequentMenuVO;
            }
        }
        return null;
    }

    private FrequentMenu getFrequentMenu(UserInfo user) {
        NamedQueryExecutor<FrequentMenu> executor = new NamedQueryExecutor<FrequentMenu>("FrequentMenu.getFrequentMenuFromUser")
                .addParameter("user", user.getId())
                .addParameter("product", ProductInformationLoader.getProductCode());
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public void setFreqMenu(FrequentMenuVO frequentMenuVO,UserInfo userInfo){
        //User user = userService.findUserByUsername(userInfo.getUsername());
        FrequentMenu freqMenu = getFrequentMenu(userInfo);
        if(freqMenu == null){
            freqMenu = new FrequentMenu();
            freqMenu.setFreqMenuUser(userInfo.getUserReference());
        }
        freqMenu.setMenuCode1(frequentMenuVO.getMenuCode1());
        freqMenu.setMenuCode2(frequentMenuVO.getMenuCode2());
        freqMenu.setMenuCode3(frequentMenuVO.getMenuCode3());
        freqMenu.setMenuCode4(frequentMenuVO.getMenuCode4());
        freqMenu.setMenuCode5(frequentMenuVO.getMenuCode5());
        freqMenu.setProduct(ProductInformationLoader.getProductCode());
        entityDao.saveOrUpdate(freqMenu);
    }
}
