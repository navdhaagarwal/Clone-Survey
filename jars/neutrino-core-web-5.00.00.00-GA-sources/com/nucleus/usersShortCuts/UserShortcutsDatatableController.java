package com.nucleus.usersShortCuts;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nucleus.html.util.HtmlUtils;
import org.xml.sax.SAXException;

import com.nucleus.core.role.entity.Role;
import com.nucleus.core.userShortcutsService.ShortcutConfigurationService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ActionConfiguration;
import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.userShortcuts.RoleToShortcutsMappingVO;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.datatable.DataTableJsonHepler;

import flexjson.JSONSerializer;

@Transactional
@Controller
@RequestMapping(value = "/UserShortcutsDataTable")
public class UserShortcutsDatatableController extends BaseController {

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry    masterConfigurationRegistry;

    @Inject
    @Named("shortcutConfigurationservice")
    protected ShortcutConfigurationService shortcutConfigurationService;

    @Inject
    @Named("entityDao")
    protected EntityDao                    entityDao;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService              baseMasterService;

    private static ConcurrentHashMap<String, String>      threadSafeMap = new ConcurrentHashMap<String, String>();

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('CHECKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('VIEW_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/{xmlFileKeyName}/{masterID}/loadColumnConfig")
    public String displayGrid(@PathVariable("xmlFileKeyName") String xmlFileKeyName,
            @PathVariable("masterID") String masterEntity, ModelMap map, HttpServletRequest request) {
        setEntityPath(masterEntity, masterConfigurationRegistry.getEntityClass(xmlFileKeyName));
        List<ColumnConfiguration> columnConfiguration = masterConfigurationRegistry
                .getColumnConfigurationList(xmlFileKeyName);
        String recordURL = masterConfigurationRegistry.getRecordURL(xmlFileKeyName);
        List<ActionConfiguration> actionConfiguration = masterConfigurationRegistry
                .getActionConfigurationList(xmlFileKeyName);
        String jspName = masterConfigurationRegistry.getjspName(xmlFileKeyName);

        String key = masterConfigurationRegistry.getKey(xmlFileKeyName);
        Map<String, Object> masterMap = new HashMap<String, Object>();
        masterMap.put("dataTableRecords", columnConfiguration);
        masterMap.put("actionConfiguration", actionConfiguration);
        masterMap.put("recordURL", recordURL);
        map.put("masterId", masterEntity);
        map.put("bFilter", true);
        map.put("bInfo", true);
        map.put("bSort", true);
        map.put("bLengthChange", true);
        map.put("bJQueryUI", false);
        map.put("serverSide", false);
        map.put("bPaginate", true);
        map.put("authority", masterEntity.toUpperCase());
        map.put("Key", key);
        map.putAll(masterMap);
        return jspName;
    }

    /**
     * Sets the entity path.
     * 
     * @param entityPath
     *            the new entity path
     */
    private void setEntityPath(String masterEntity, String entityPath) {
        threadSafeMap.putIfAbsent(masterEntity, entityPath);
    }

    @PreAuthorize("hasAuthority('MAKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('CHECKER_MAP_ROLE_TO_SHORTCUT') or hasAuthority('VIEW_MAP_ROLE_TO_SHORTCUT')")
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/{Key}/loadPaginatedInformation")
    @MonitoredWithSpring(name = "USDC_ENTITY_UPDATE_INFO_LIST")
    public @ResponseBody
    <T> String getEntityUpdateInfoList(@PathVariable("Key") String key, HttpServletRequest request) throws IOException,
            ParserConfigurationException, SAXException, ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry.getColumnConfigurationList(key);

        setEntityPath(key, masterConfigurationRegistry.getEntityClass(key));
        List<RoleToShortcutsMappingVO> entities = getEntityData();
        List<List<Object>> columnDataList = new LinkedList<List<Object>>();

        for (RoleToShortcutsMappingVO entity : entities) {
            List<Object> rowDataList = new LinkedList<Object>();
            rowDataList.add(null);
            Object columnValue = null;
            for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                try {
                    columnValue = PropertyUtils.getNestedProperty(entity, columnConfiguration.getDataField());
                    if(columnValue instanceof String){                    	
                    	columnValue=HtmlUtils.htmlEscape((String)columnValue);
                    }

                } catch (NestedNullException e) {
                    columnValue = null;
                    BaseLoggers.exceptionLogger
                            .error("Exception occured while accessing nested property for column configuration '"
                                    + columnConfiguration.getTitleKey() + "' :" + e.getMessage());
                }

                rowDataList.add(columnValue);
            }
            columnDataList.add(rowDataList);
        }

        DataTableJsonHepler jsonHelper = new DataTableJsonHepler();
        jsonHelper.setAaData(columnDataList);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        return jsonString;
    }

    public List<RoleToShortcutsMappingVO> getEntityData() {

        List<RoleToShortcutsMappingVO> roleMappingVOs = new ArrayList<RoleToShortcutsMappingVO>();

        // Fetching all the role name and role ids with Total No of MyFavourites mapped to it.
        List<Object> entities = shortcutConfigurationService.fetchRoleWithMyFavouritesCount();

        if (entities != null) {
            Map<Object, Object> roleToMyFavtCountMap = new HashMap<Object, Object>();

            for (Object entity : entities) {
                // Type casting "entity" object to array of Objects as Each object in "entities" List is an array of Objects
                Object[] entityArray = (Object[]) entity;
                roleToMyFavtCountMap.put(entityArray[0], entityArray[1]);
            }

            List<Role> rolesToFilter = baseMasterService.getAllApprovedAndActiveEntities(Role.class);

            // Filter roles to only those which are already mapped
            Iterator<Role> roleIterator = rolesToFilter.listIterator();
            while (roleIterator.hasNext() && roleToMyFavtCountMap.size() > 0) {
                Role role = roleIterator.next();

                for (Object roleId : roleToMyFavtCountMap.keySet()) {
                    roleId = (Long) roleId;
                    if (roleId.equals(role.getId())) {
                        // Add mapped role to the list so that these can be displayed on grid
                        roleMappingVOs.add(getRoleToShortcutsMappingVO(role, roleToMyFavtCountMap.get(roleId)));
                        break;
                    }
                }
            }
        }
        return roleMappingVOs;
    }

    private RoleToShortcutsMappingVO getRoleToShortcutsMappingVO(Role role, Object myFavtCount) {
        RoleToShortcutsMappingVO roleToShortcutsMappingVO = new RoleToShortcutsMappingVO();
        roleToShortcutsMappingVO.setMyFavouritesCount((Long) myFavtCount);
        roleToShortcutsMappingVO.setRoleId(role.getId());
        roleToShortcutsMappingVO.setRoleName(role.getName());

        ArrayList<String> actions = new ArrayList<String>();
        actions.add("Edit");
        actions.add("Delete");

        roleToShortcutsMappingVO.addProperty("actions", actions);
        return roleToShortcutsMappingVO;
    }

}