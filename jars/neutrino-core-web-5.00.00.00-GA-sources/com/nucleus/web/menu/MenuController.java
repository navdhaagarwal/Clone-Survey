package com.nucleus.web.menu;

import com.nucleus.menu.FrequentMenuVO;
import com.nucleus.authority.Authority;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.menu.IMenuService;
import com.nucleus.menu.MenuRootVO;
import com.nucleus.menu.MenuVO;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.UserInfo;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.nucleus.master.BaseMasterUtils.getCurrentUser;
import com.nucleus.web.common.controller.BaseController;

@Controller
@RequestMapping("/menuController")
public class MenuController extends BaseController{

    @Inject
    @Named("menuService")
    IMenuService menuService;

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named(value = "configurationService")
    private ConfigurationService configurationService;

    @RequestMapping(value = "/menuJsp")
    public String menuJsp(){
        return "menuGenerator";
    }

    @RequestMapping(value = "/menuPreview")
    @ResponseBody
    public Map menuPreview(){
        return menuJson(false);
    }

    @RequestMapping(value = "/menuAuth")
    @ResponseBody
    public Map menuAuth(){
        return menuJson(true);
    }

    private static final String        IS_BROWSER_CONSOLE_DISABLED     = "config.browser.console.disabled";
    private static final String        CONFIGURATION_QUERY             = "Configuration.getPropertyValueFromPropertyKey";
    private static final String        IS_PRINT_SCREEN_DISABLED        = "config.browser.printScreen.disabled";
    private static final String        CAPS_FIELD_CONFIG               = "config.capsField.enabled";

    private UserInfo getCurrentLoggedInUserName(){
        UserInfo user = getCurrentUser();
        return user;
    }

    private Map menuJson(boolean isAuthorityToBeAdded) {
        MenuRootVO mr = menuService.getFinalMenu(ProductInformationLoader.getProductName());
        if(isAuthorityToBeAdded){
            mr = menuService.filterMenuByAuthorities(mr, getCurrentLoggedInUserName());
            menuService.nullifyUnwantedValuesForMenuTab(mr);
        }
        String jsonString = menuService.objectToJson(mr);
        Map<String, Object> map = new HashedMap();
        map = menuService.returnProperties(jsonString);
        map.put("productList",productList());
        if(configurationService.getPropertyValueByPropertyKey(IS_BROWSER_CONSOLE_DISABLED, CONFIGURATION_QUERY)!=null &&
                configurationService.getPropertyValueByPropertyKey(IS_BROWSER_CONSOLE_DISABLED, CONFIGURATION_QUERY).equalsIgnoreCase("true")) {
            map.put("isDisabled", true);
        }
        else{
            map.put("isDisabled", false);
        }
        if(configurationService.getPropertyValueByPropertyKey(IS_PRINT_SCREEN_DISABLED, CONFIGURATION_QUERY)!=null &&
                configurationService.getPropertyValueByPropertyKey(IS_PRINT_SCREEN_DISABLED, CONFIGURATION_QUERY).equalsIgnoreCase("true")) {
            map.put("isPrintScreenDisabled", true);
        }
        else{
            map.put("isPrintScreenDisabled", false);
        }
        if(configurationService.getPropertyValueByPropertyKey(CAPS_FIELD_CONFIG, CONFIGURATION_QUERY)!=null &&
                configurationService.getPropertyValueByPropertyKey(CAPS_FIELD_CONFIG, CONFIGURATION_QUERY).equalsIgnoreCase("true")){
            map.put("capsFieldConfig", true);
        }
        else{
            map.put("capsFieldConfig", false);
        }
        return map;
    }

    @RequestMapping(value = "/saveMenu", method = RequestMethod.POST)
    @ResponseBody
    public String saveMenu(@RequestParam("jsonString") String jsonString) {
        String message = "Unable to create menu item";
        try {
            message = menuService.jsonToObject(java.net.URLDecoder.decode(jsonString, "UTF-8"));
            return message;
        } catch (UnsupportedEncodingException e) {
            BaseLoggers.flowLogger.error("Json decoding error", e);
        }
        return message;
    }

    @RequestMapping(value = "/getAutoCompleteJsp", method = RequestMethod.GET)
    public String getAutoCompleteJsp(ModelMap map) {
        map.put("menuVO",new MenuVO());
        return "/menu/menuAutoComplete";
    }

    @RequestMapping(value = "/getMenuNameForEditMode", method = RequestMethod.GET)
    public String getMenuNameForEditMode(ModelMap map){
        map.put("menuVO",new MenuVO());
        return "/menu/menuNameForEditMode";
    }

    @RequestMapping(value = "/getMenuAuthForEditMode", method = RequestMethod.GET)
    public String getMenuAuthForEditMode(ModelMap map){
        map.put("authCodes", entityDao.findAll(Authority.class));
        return "/menu/menuAuthForEditMode";
    }

    @RequestMapping(value = "/deleteMenu", method = RequestMethod.GET)
    public String softDelete(@RequestParam("id") Long id) {
        try {
            return menuService.softDelete(id);
        } catch (IOException e) {
            return "Some Error Occurred";
        }
    }


    /*@RequestMapping(value = "/checkIfMenuNameExists", method = RequestMethod.GET)
    public boolean checkIfMenuNameExists(@RequestParam("menuName") String menuName) {
            return menuService.checkIfMenuNameExists(menuName);
    } */

    public Set<String> productList(){
        UserInfo currentUser = getUserDetails();
        Set<String> productList = new HashSet<>();
        if(currentUser.getLoanProductInfoList()!=null) {
            currentUser.getLoanProductInfoList().forEach(item -> productList.add(item.getProductTypeShortName()));
        }
        return  productList;
    }

	@RequestMapping(value = "/getFreqMenu", method = RequestMethod.GET)
    @ResponseBody
    public FrequentMenuVO getFreqMenu(){
        try{
            return menuService.getFreqMenu(getCurrentLoggedInUserName());
        }catch (Exception e){
            BaseLoggers.flowLogger.debug("Exception occurr while getting Frequent menu "+e.getMessage());
            BaseLoggers.flowLogger.error("Exception occurr while getting Frequent menu "+e.getMessage());
            return null;
        }
    }

    @RequestMapping(value="/saveFreqMenu", method = RequestMethod.POST, consumes = "application/json", produces = "application/json" )
    public @ResponseBody boolean saveFreqMenu(@RequestBody FrequentMenuVO frequentMenuVO){
        try{
            menuService.setFreqMenu(frequentMenuVO,getCurrentLoggedInUserName());
            return true;
        }catch (Exception e){
            BaseLoggers.flowLogger.debug("Exception occurr while saving Frequent menu "+e.getMessage());
            BaseLoggers.flowLogger.error("Exception occurr while saving Frequent menu "+e.getMessage());
            return false;
        }

    }
	
}