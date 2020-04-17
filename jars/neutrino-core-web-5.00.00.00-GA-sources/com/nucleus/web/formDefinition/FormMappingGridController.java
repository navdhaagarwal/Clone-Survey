package com.nucleus.web.formDefinition;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

import com.nucleus.core.dynamicform.service.FormDefinitionGridService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.formsConfiguration.FormConfigurationMapping;
import com.nucleus.entity.BaseEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ActionConfiguration;
import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.datatable.DataTableJsonHepler;

import flexjson.JSONSerializer;

@Transactional
@Controller
@RequestMapping(value = "/FormConfigurationMappingGrid")
public class FormMappingGridController extends BaseController {

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

    @Inject
    @Named("formDefinitionGridService")
    private FormDefinitionGridService   formDefinitionGridService;

    private static ConcurrentHashMap<String, String>   threadSafeMap = new ConcurrentHashMap<String, String>();

    private static final String                masterId      = "FormConfigurationMapping";
    
    @PreAuthorize("hasAuthority('MASTER_COMMON')")
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/{xmlFileKeyName}/{masterID}/loadColumnConfig")
    public String displayGrid(@PathVariable("xmlFileKeyName") String xmlFileKeyName,
            @PathVariable("masterID") String masterEntity, ModelMap map, HttpServletRequest request) {
        map.put("masterId", masterId);
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

    @PreAuthorize("hasAuthority('MASTER_COMMON')")
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/{Key}/loadPaginatedInformation")
    @MonitoredWithSpring(name = "FMGC_ENTITY_UPDATE_INFO_LIST")
    public @ResponseBody
    <T> String getEntityUpdateInfoList(@PathVariable("Key") String key, HttpServletRequest request) throws IOException,
            ParserConfigurationException, SAXException, ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry.getColumnConfigurationList(key);

        setEntityPath(key, masterConfigurationRegistry.getEntityClass(key));
        List<Object> entities = null;

        Map<String, Object> entityMap = new HashMap<String, Object>();
        entityMap = getEntityData();
        entities = (List<Object>) entityMap.get("entityList");
        List<List<Object>> columnDataList = new LinkedList<List<Object>>();
        ArrayList<String> actions = new ArrayList<String>();
        actions.add("Edit");
        actions.add("Delete");
        for (Object entity : entities) {
            List<Object> rowDataList = new LinkedList<Object>();
            ((BaseEntity) entity).addProperty("actions", actions);
            rowDataList.add(null);
            Object columnValue = null;
            for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                try {
                    columnValue = PropertyUtils.getNestedProperty(entity, columnConfiguration.getDataField());

                    if (!columnConfiguration.getDataField().equals("viewProperties.actions") && columnValue instanceof List) {
                        List listOfValues = (List) columnValue;
                        columnValue = listOfValues.size();
                    }
                    if (columnValue instanceof DateTime) {
                        columnValue = getFormattedDate((DateTime) columnValue);
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> getEntityData() {
        Map<String, Object> childData = new HashMap<String, Object>();
        List<FormConfigurationMapping> formConfigurationMappingList = formDefinitionGridService.getFormConfiguration();
        childData.put("entityList", formConfigurationMappingList);
        return childData;
    }

}
