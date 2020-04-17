/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - ï¿½ 2012. All rights
 * reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.master;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import com.nucleus.core.dynamicform.service.FormService;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.Parameter;
import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.nucleus.html.util.HtmlUtils;
import org.xml.sax.SAXException;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.money.entity.Money;
import com.nucleus.core.money.utils.MoneyUtils;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.MasterDownloadEnableConfiguration;
import com.nucleus.finnone.pro.fileconsolidator.serviceinterface.IMasterDownloadService;
import com.nucleus.grid.IGridService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ActionConfiguration;
import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.GridDataUtility;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.template.TemplateService;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.datatable.DataTableJsonHepler;
import com.nucleus.web.formatter.MoneyFormatter;

import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;
import flexjson.transformer.Transformer;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.DOWNLOAD_FORMAT_TYPE1_ENABLED;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.DOWNLOAD_FORMAT_TYPE2_ENABLED;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.DOWNLOAD_FORMAT_TYPE1;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.DOWNLOAD_FORMAT_TYPE2;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.DOWNLOAD_ENABLED;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.DEFAULT_DOWNLOAD_FORMAT_TYPE;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.XLS_DOWNLOAD_ENABLED ;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.XLSX_DOWNLOAD_ENABLED ;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.CSV_DOWNLOAD_ENABLED ;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.MAX_PARENT_RECORD_DOWNLOAD_LIMIT ;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.USER_FORMAT_NAME ;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.PROCESS_NAME;

import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.XLS ;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.XLSX ;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.CSV ;


/**
 * The Class MasterController.
 * 
 * @author Nucleus Software Exports Limited
 */
@Transactional
@Controller
@RequestMapping(value = "grid")
public class GridDataController extends BaseController {

    private static final String         MASTER_ACTIONS_XML_NAME = "MastersCommonActions";
    private static final String         XML_FILE_KEY_NAME       = "xmlFileKeyName";
    private static final String         MASTER_ID               = "masterID";
    private static Integer minCharToBeginSearch;
    
    @Inject
	@Named("masterDownloadService")
	private IMasterDownloadService masterDownloadService;
    
    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;

    @Inject
    @Named("templateService")
    private TemplateService             templateService;

    @Inject
    @Named("moneyFormatter")
    MoneyFormatter                      moneyFormatter;
    
    @Inject
    @Named("gridDataUtility")
    GridDataUtility gridDataUtility;

    @Inject
    @Named(value = "formConfigService")
    private FormService formService;

    private static Map<String, MasterDownloadEnableConfiguration> masterDownloadEnableConfigurationCacheMap = new HashMap<>();

    private static ConcurrentHashMap<String, Class<?>> entityClassCache           = new ConcurrentHashMap<>();

    private final Transformer           date_transformer        = new AbstractTransformer() {
                                                                    @Override
                                                                    public void transform(Object object) {
                                                                        getContext().write(
                                                                                "\"" + getFormattedDate((DateTime) object)
                                                                                        + "\"");
                                                                    }
                                                                };
    private final Transformer           money_transformer       = new AbstractTransformer() {
                                                                    @Override
                                                                    public void transform(Object object) {
                                                                        Money money = (Money) object;
                                                                        getContext().write(
                                                                                "\"" + moneyFormatter.print(money, null)
                                                                                        + "\"");
                                                                    }
                                                                };

    /**
     * Gets the entity update info list.
     * 
     * @param <T>
     * 
     * @param xmlFilePath
     *            the xml file path
     * @param masterEntity
     *            the master entity
     * @param mId
     *            the m id
     * @param map
     *            the map
     * @param request
     *            the request
     * @return the entity update info list
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws ParserConfigurationException
     *             the parser configuration exception
     * @throws SAXException
     *             the sAX exception
     * @throws ClassNotFoundException
     *             the class not found exception
     * 
     *             processingType shows that Data table is server side
     *             processing or client side. By default for masters processing
     *             is server side. For client side processing we need to give
     *             processing type value false in our XML file.
     */
    @PreAuthorize("(hasAuthority('MAKER_'+#xmlFileKeyName.toUpperCase()) or hasAuthority('CHECKER_'+#xmlFileKeyName.toUpperCase()) or hasAuthority('VIEW_'+#xmlFileKeyName.toUpperCase()))")
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/{xmlFileKeyName}/{masterID}/loadColumnConfig")
    public <T> String getGridColumnConfig(@PathVariable(XML_FILE_KEY_NAME) String xmlFileKeyName,
            @PathVariable(MASTER_ID) String masterEntity, ModelMap map, HttpServletRequest request) {

        List<ColumnConfiguration> columnConfiguration = masterConfigurationRegistry
                .getColumnConfigurationList(xmlFileKeyName);

        boolean disableMasterCreation=masterConfigurationRegistry.getDisableMasterCreation(xmlFileKeyName);

         Set<ActionConfiguration> actionConfiguration = new LinkedHashSet<ActionConfiguration>(masterConfigurationRegistry.getActionConfigurationList(xmlFileKeyName));

        boolean removeCommonActions = masterConfigurationRegistry.isRemoveCommonActions(xmlFileKeyName);

        Set<ActionConfiguration> commonMasterActionConfigurations = new LinkedHashSet<ActionConfiguration>();

        if (!removeCommonActions) {
            commonMasterActionConfigurations.addAll(masterConfigurationRegistry
                    .getActionConfigurationList(MASTER_ACTIONS_XML_NAME));
        }

        String recordURL = masterConfigurationRegistry.getRecordURL(xmlFileKeyName);

        Map<String, String> templateResolutionMap = new HashMap<String, String>();
        templateResolutionMap.put(MASTER_ID, masterEntity);
        templateResolutionMap.put(XML_FILE_KEY_NAME, xmlFileKeyName);
        try {
            for (ActionConfiguration ac : commonMasterActionConfigurations) {
                String action = ac.getAction();
                String actionUrl = ac.getActionUrl();
                String templateAction = "";
                templateAction = templateService.getResolvedStringFromTemplate(action + "-template-key", actionUrl,
                        templateResolutionMap);
                ac.setActionUrl(templateAction);
            }
            actionConfiguration.addAll(commonMasterActionConfigurations);
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("Error in fetching actions from " + XML_FILE_KEY_NAME + ".xml");
        }
        String jspName = masterConfigurationRegistry.getjspName(xmlFileKeyName);
        Boolean processingType = masterConfigurationRegistry.getProcessingType(xmlFileKeyName);
        if (processingType == null) {
            processingType = true;
        }
        Boolean hrefBool = masterConfigurationRegistry.getHrefBool(xmlFileKeyName);
        if (hrefBool == null) {
            hrefBool = Boolean.TRUE;
        }
		Boolean isUploadAvailable = masterConfigurationRegistry.isUploadAvailable(xmlFileKeyName);
        if (isUploadAvailable == null) {
            isUploadAvailable = Boolean.FALSE;
        }
        Boolean isFormatDownloadAvailable = masterConfigurationRegistry.isFormatDownloadAvailable(xmlFileKeyName);
        if (isFormatDownloadAvailable == null) {
        	isFormatDownloadAvailable = Boolean.FALSE;
        }
       /* Boolean showApprovedVersion=false;
     try{
 showApprovedVersion=masterConfigurationRegistry.getEntityShowApprovedVersion(getEntityClass(xmlFileKeyName));
     }
       catch(SystemException e)
       {
    	   BaseLoggers.exceptionLogger.error(e.getMessage());
       }*/
        String key = masterConfigurationRegistry.getKey(xmlFileKeyName);
        minCharToBeginSearch = masterConfigurationRegistry.getMinCharToBeginSearch(xmlFileKeyName);
        Map<String, Object> masterMap = new HashMap<String, Object>();
        masterMap.put("dataTableRecords", columnConfiguration);
        masterMap.put("actionConfiguration", actionConfiguration);
        masterMap.put("recordURL", recordURL);
        masterMap.put("disableButton",disableMasterCreation);
        masterMap.put("minCharToBeginSearch", minCharToBeginSearch);
        map.put("masterId", masterEntity);
        map.put("bFilter", true);
      //  map.put("showApprovedVersion", showApprovedVersion);
        map.put("bInfo", true);
        map.put("bSort", true);
        map.put("bLengthChange", true);
        map.put("bJQueryUI", false);
        map.put("serverSide", processingType);
        map.put("hrefBoolType", hrefBool);
        map.put("bPaginate", true);
        map.put("authority", masterEntity.toUpperCase());
        map.put("Key", key);
        
        MasterDownloadEnableConfiguration masterDownloadEnableConfiguration =  masterDownloadEnableConfigurationCacheMap.get(xmlFileKeyName);
        if(isNull(masterDownloadEnableConfiguration)){
            masterDownloadEnableConfiguration = masterDownloadService.getMasterDownloadEnableConfiguration(xmlFileKeyName);
            if(notNull(masterDownloadEnableConfiguration)){
            	masterDownloadEnableConfigurationCacheMap.put(xmlFileKeyName, masterDownloadEnableConfiguration);
            }else{
            	masterDownloadEnableConfiguration = new MasterDownloadEnableConfiguration();
            	masterDownloadEnableConfigurationCacheMap.put(xmlFileKeyName, masterDownloadEnableConfiguration);
            }
        }
        
        Boolean downloadEnabled = masterDownloadEnableConfiguration.isDownloadEnabled();
        map.put(DOWNLOAD_ENABLED, downloadEnabled);
       
            	
        Boolean xlsDownloadEnabled = false;
        Boolean xlsxDownloadEnabled  = false;
        Boolean csvDownloadEnabled = false;
        int maxParentRecordDwnldLimit = 0;
        String userFormatName = null;
        String processName = null;
        
        String defaultDownloadFormatType = null;
        String downloadFormatType1 = null;
        String downloadFormatType2 = null;
        Boolean downloadFormatType1Enabled = false;
        Boolean downloadFormatType2Enabled = false;
        if(notNull(downloadEnabled) && downloadEnabled){
        	xlsDownloadEnabled = masterDownloadEnableConfiguration.isXlsFormatSupported();
        	xlsxDownloadEnabled = masterDownloadEnableConfiguration.isXlsxFormatSupported();
        	csvDownloadEnabled = masterDownloadEnableConfiguration.isCsvFormatSupported();
        	maxParentRecordDwnldLimit = masterDownloadEnableConfiguration.getMaxParentRecordDwnldLimit();
        	userFormatName = masterDownloadEnableConfiguration.getUserFormatName();
        	processName = masterDownloadEnableConfiguration.getProcessName();
        	
        	defaultDownloadFormatType = masterDownloadEnableConfiguration.getDefaultDownloadFormatType();
        	if(notNull(defaultDownloadFormatType) && !defaultDownloadFormatType.isEmpty()){
        		Map<String,Object> dynamicNonDefaultDownloadFormatDetailMap = new HashMap<>();
        		getDynamicNonDefaultDownloadFormatDetails(defaultDownloadFormatType, xlsDownloadEnabled, xlsxDownloadEnabled, 
        													csvDownloadEnabled, dynamicNonDefaultDownloadFormatDetailMap);
        		downloadFormatType1Enabled = (Boolean) dynamicNonDefaultDownloadFormatDetailMap.get(DOWNLOAD_FORMAT_TYPE1_ENABLED);
        		downloadFormatType2Enabled = (Boolean) dynamicNonDefaultDownloadFormatDetailMap.get(DOWNLOAD_FORMAT_TYPE2_ENABLED);
        		downloadFormatType1 = (String) dynamicNonDefaultDownloadFormatDetailMap.get(DOWNLOAD_FORMAT_TYPE1);
        		downloadFormatType2 = (String) dynamicNonDefaultDownloadFormatDetailMap.get(DOWNLOAD_FORMAT_TYPE2);
        	}
        	
        }
        map.put(DEFAULT_DOWNLOAD_FORMAT_TYPE, defaultDownloadFormatType);
        map.put(DOWNLOAD_FORMAT_TYPE1, downloadFormatType1);
        map.put(DOWNLOAD_FORMAT_TYPE2, downloadFormatType2);
        map.put(DOWNLOAD_FORMAT_TYPE1_ENABLED, downloadFormatType1Enabled);
        map.put(DOWNLOAD_FORMAT_TYPE2_ENABLED, downloadFormatType2Enabled);        
        map.put(XLS_DOWNLOAD_ENABLED, xlsDownloadEnabled);
        map.put(XLSX_DOWNLOAD_ENABLED, xlsxDownloadEnabled);
        map.put(CSV_DOWNLOAD_ENABLED, csvDownloadEnabled);
        map.put(MAX_PARENT_RECORD_DOWNLOAD_LIMIT, maxParentRecordDwnldLimit);
        map.put(USER_FORMAT_NAME, userFormatName);
        map.put(PROCESS_NAME, processName);
        map.put("isUploadAvailable", isUploadAvailable);
        map.put("isFormatDownloadAvailable", isFormatDownloadAvailable);

        map.putAll(masterMap);
        return jspName;
    }
                                                               
    /**
     * @param defaultDownloadFormatType
     * @param xlsDownloadEnabled
     * @param xlsxDownloadEnabled
     * @param csvDownloadEnabled
     * @param dynamicNonDefaultDownloadFormatDetailMap
     */
    private void getDynamicNonDefaultDownloadFormatDetails(String defaultDownloadFormatType,Boolean xlsDownloadEnabled,
    					Boolean xlsxDownloadEnabled,Boolean csvDownloadEnabled, 
    					Map<String,Object> dynamicNonDefaultDownloadFormatDetailMap){
    	
    	Boolean downloadFormatType1Enabled = false;
        Boolean downloadFormatType2Enabled = false;
        String downloadFormatType1 = null;
        String downloadFormatType2 = null;
        
		if(XLS.equalsIgnoreCase(defaultDownloadFormatType)){
    		if(csvDownloadEnabled){
    			downloadFormatType1Enabled=true;
    			downloadFormatType1 = CSV;            			
    		}
    		if(xlsxDownloadEnabled){
    			downloadFormatType2Enabled=true;
    			downloadFormatType2 = XLSX;
    		}
    	}else if (XLSX.equalsIgnoreCase(defaultDownloadFormatType)){
    		if(csvDownloadEnabled){
    			downloadFormatType1Enabled=true;
    			downloadFormatType1 = CSV;            			
    		}
    		if(xlsDownloadEnabled){
    			downloadFormatType2Enabled=true;
    			downloadFormatType2 = XLS;
    		}
    	}else if (CSV.equalsIgnoreCase(defaultDownloadFormatType)){
    		if(xlsxDownloadEnabled){
    			downloadFormatType1Enabled=true;
    			downloadFormatType1 = XLSX;            			
    		}
    		if(xlsDownloadEnabled){
    			downloadFormatType2Enabled=true;
    			downloadFormatType2 = XLS;
    		}
    	}
		
		dynamicNonDefaultDownloadFormatDetailMap.put(DOWNLOAD_FORMAT_TYPE1_ENABLED, downloadFormatType1Enabled);
		dynamicNonDefaultDownloadFormatDetailMap.put(DOWNLOAD_FORMAT_TYPE2_ENABLED, downloadFormatType2Enabled);
		dynamicNonDefaultDownloadFormatDetailMap.put(DOWNLOAD_FORMAT_TYPE1, downloadFormatType1);
		dynamicNonDefaultDownloadFormatDetailMap.put(DOWNLOAD_FORMAT_TYPE2, downloadFormatType2);
	
    }
    
    private void putEntityClassInCache(String masterEntity, Class<?> entityPath) {
    	entityClassCache.putIfAbsent(masterEntity, entityPath);
    }
    
    private Class<?> getEntityClassFromCache(String masterEntity) {
        return entityClassCache.get(masterEntity);
    }

    @PreAuthorize("(hasAuthority('MAKER_'+#key.toUpperCase()) or hasAuthority('CHECKER_'+#key.toUpperCase()) or hasAuthority('VIEW_'+#key.toUpperCase()))")
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/{Key}/loadPaginatedInformation")
    @MonitoredWithSpring(name = "GDC_ENTITY_UPDATE_INFO_LIST")
    public @ResponseBody String getEntityUpdateInfoList(@PathVariable("Key") String key,
            @RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
            @RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
            @RequestParam(value = "sSortDir_0", required = false) String sSortDir_0,
            @RequestParam(value = "iSortCol_0", required = false) Integer iSortCol_0,
            @RequestParam(value = "sSearch", required = false) String sSearch,
            @RequestParam(value = "sEcho", required = false) Integer sEcho, HttpServletRequest request) throws IOException,
            ParserConfigurationException, SAXException, ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        String sortColName = null;
        if (iDisplayLength != null) {
            if (iDisplayLength == -1) {
                iDisplayLength = 10;

            }
        }
        DataTableJsonHepler jsonHelper = new DataTableJsonHepler();
        String serviceName = masterConfigurationRegistry.getServiceBean(key);
        IGridService gridService = (IGridService) WebApplicationContextUtils.getWebApplicationContext(
                request.getSession().getServletContext()).getBean(serviceName);

        List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry.getColumnConfigurationList(key);

        boolean isDynamicWC = false;
        boolean isWorkflowConfig = false;
        if(key.equals("DynamicWorkflowConfiguration")) {
            isDynamicWC = true;
            key = "WorkflowConfiguration";
        } else if(key.equals("WorkflowConfiguration")){
            isWorkflowConfig = true;
        }

        Class<?> entityClass = getEntityClassFromCache(key);
        if (entityClass == null) {
        	entityClass = getEntityClass(key);
        	putEntityClassInCache(key, entityClass);
        }
        List<Object> entities = null;
        Integer recordCount = 0;
        Integer totalRecordCount = 0;
        

        //Searching
        Map<String, Object> searchMap = new HashMap<String, Object>();
        if("TaskAssignmentMaster".equalsIgnoreCase(key) || "DynaMetaConfigurator".equalsIgnoreCase(key)){
            searchMap.put("isProductFilteringRequired", true);
        }
        if (sSearch != null && !sSearch.isEmpty() && sSearch.trim().length() >= minCharToBeginSearch) {
            String searchTerm = sSearch.trim();
            if (CollectionUtils.isNotEmpty(columnConfigurationList)) {
                for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                    if (columnConfiguration.getSearchable() == true) {
                    	if(columnConfiguration.getIsRegional()){
                    		searchMap.put(columnConfiguration.getRegionalDataField(), searchTerm.toUpperCase());
                    	}else{
                    		searchMap.put(columnConfiguration.getDataField(), searchTerm.toUpperCase());
                    	}

                    	if("viewProperties.createdBy".equals(columnConfiguration.getDataField())) {
                            searchMap.remove(columnConfiguration.getDataField());
                            searchMap.put("entityLifeCycleData.createdByUri", searchTerm.toUpperCase());
                            searchMap.put("entityLifeCycleData.lastUpdatedByUri", searchTerm.toUpperCase());
                        } else if("viewProperties.reviewedBy".equals(columnConfiguration.getDataField())) {
                            searchMap.remove(columnConfiguration.getDataField());
                            searchMap.put("masterLifeCycleData.reviewedByUri", searchTerm.toUpperCase());
                        } else if("viewProperties.approvalStatus".equals(columnConfiguration.getDataField())) {
                            searchMap.remove(columnConfiguration.getDataField());
                            searchMap.put("masterLifeCycleData.approvalStatus", MakerCheckerWebUtils.getApprovalStatusCode(searchTerm.toUpperCase()));
                        } else if("class.simpleName".equals(columnConfiguration.getDataField())) {
                            searchMap.remove(columnConfiguration.getDataField());
                            searchMap.put("class", searchTerm.toUpperCase());
                        }else if("parameterTypeName".equals(columnConfiguration.getDataField()) && key.equals("Parameter")) { 
                            searchMap.remove(columnConfiguration.getDataField());
                            if(CollectionUtils.isNotEmpty(getParamTypeInteger(searchTerm.toUpperCase()))){
                                searchMap.put("paramType", getParamTypeInteger(searchTerm.toUpperCase()));
                            }
                        }

                    }

                }
            }
        }
        
        //sorting
        if (sSortDir_0 != null && iSortCol_0 != null) {
            if (sSortDir_0.equals("asc")) {
                sSortDir_0 = "ASC";
            }
            if (sSortDir_0.equals("desc")) {
                sSortDir_0 = "DESC";
            }

            ColumnConfiguration columnConfigurationIndex = columnConfigurationList.get(iSortCol_0 - 1);
            sortColName = columnConfigurationIndex.getDataField();
            if("viewProperties.createdBy".equals(columnConfigurationIndex.getDataField())) {
                sortColName = "entityLifeCycleData.lastUpdatedByUri";
            } else if("viewProperties.reviewedBy".equals(columnConfigurationIndex.getDataField())) {
                sortColName = "masterLifeCycleData.reviewedByUri";
            } else if("viewProperties.approvalStatus".equals(columnConfigurationIndex.getDataField())) {
                sortColName = "masterLifeCycleData.approvalStatus";
            } else if("class.simplename".equals(columnConfigurationIndex.getDataField())) {
                sortColName = "class";
            }
        } else {
            // We are here because we want default sorting on the column
            // provided in our XML
            String defaultSortableColumn = masterConfigurationRegistry.getDefaultSortableColumn(key);
            String sortableDirection = masterConfigurationRegistry.getSortDirection(key);
            if (defaultSortableColumn != null && CollectionUtils.isNotEmpty(columnConfigurationList)) {
                for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                    if (columnConfiguration.getDataField().equals(defaultSortableColumn) && columnConfiguration.getSortable()) {
                        sSortDir_0 = sortableDirection;
                        sortColName = defaultSortableColumn;
                    }

                }
            }
        }
       boolean containsSearchEnabled = masterConfigurationRegistry.isContainsSearchEnabled(key);
        Map<String, Object> entityMap = getEntityData(gridService, iDisplayStart, iDisplayLength, sSortDir_0,
                sortColName, containsSearchEnabled, entityClass, searchMap, isDynamicWC, isWorkflowConfig);

        entities = (List<Object>) entityMap.get("entityList");
        recordCount = (Integer) entityMap.get("recordCount");
        totalRecordCount = (Integer) entityMap.get("totalRecordCount");
        if (sEcho != null) {
            jsonHelper.setsEcho(sEcho);
            jsonHelper.setiTotalDisplayRecords(recordCount);
            jsonHelper.setiTotalRecords(totalRecordCount);
        }
        
        entities = entities == null ? Collections.emptyList() : entities;
        List<List<Object>> columnDataList = new LinkedList<List<Object>>();
        for (Object entity : entities) {
            int approvalStatus = 0;
            if(entity instanceof BaseMasterEntity) {
                approvalStatus = ((BaseMasterEntity) entity).getApprovalStatus();
            }
            boolean checkReviewedBy = (approvalStatus == ApprovalStatus.APPROVED || approvalStatus == ApprovalStatus.APPROVED_MODIFIED || approvalStatus == ApprovalStatus.APPROVED_DELETED);
            List<Object> rowDataList = new LinkedList<Object>();
            rowDataList.add(null);
            Object columnValue = null;
            for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                try {
                	columnValue =gridDataUtility.getColumnValueFromColumnConfiguration(entity, columnConfiguration);
               
                    if (!columnConfiguration.getDataField().equals("viewProperties.actions") && columnValue instanceof List) {
                        List listOfValues = (List) columnValue;
                        columnValue = listOfValues.size();
                    }

                    if (!columnConfiguration.getDataField().equals("viewProperties.actions") && columnValue instanceof Set) {
                        Set setOfValues = (Set) columnValue;
                        columnValue = setOfValues.size();
                    }
                    
                    if("entityLifeCycleData.creationTimeStamp".equals(columnConfiguration.getDataField()) && columnValue == null) {
                        columnValue = new DateTime(2008, 1, 1, 12, 0);
                    }

                    if (columnValue instanceof DateTime) {
                        if (columnConfiguration.getColumnType().equals("date")) {
                            columnValue = getFormattedDate((DateTime) columnValue);
                        } else {
                            columnValue = getFormattedDateTime((DateTime) columnValue);
                        }
                    }

                    if (columnValue instanceof Timestamp) {
                        String pattern = ((ConfigurationVO) getUserDetails().getUserPreferences().get("config.date.formats"))
                                .getText();
                        DateFormat dformat = new SimpleDateFormat(pattern);
                        columnValue = dformat.format(((Timestamp) columnValue).getTime());

                    }

                    if (columnValue instanceof Calendar) {
                        String pattern = getUserDetails().getUserPreferences().get("config.date.formats").getText();
                        final DateFormat dformat = new SimpleDateFormat(pattern);
                        columnValue = dformat.format(((Calendar) columnValue).getTime());
                    }
                    if (columnValue instanceof Money) {
                        String formattedMoney = moneyFormatter.print((Money) columnValue, null);
                        columnValue = formattedMoney.replaceAll(MoneyUtils.MONEY_DELIMITER, " ");
                    }
                    if(columnValue instanceof String){                    	
                    	columnValue=("viewProperties.createdBy".equals(columnConfiguration.getDataField()) || 
                				"viewProperties.reviewedBy".equals(columnConfiguration.getDataField()))?columnValue:HtmlUtils.htmlEscape((String)columnValue);
                    }
                    if (columnConfiguration.getDataField().equalsIgnoreCase("activeFlag") && null != columnValue) {
                        if (columnValue.equals(true)) {
                            columnValue = "Active";
                        } else {
                            columnValue = "Inactive";
                        }
                    }
                    if (columnConfiguration.getDataField().equalsIgnoreCase("active") && null != columnValue) {
                        if (columnValue.equals(true)) {
                            columnValue = "Active";
                        } else {
                            columnValue = "Inactive";
                        }
                    }
					if (columnConfiguration.getDataField().equalsIgnoreCase("isWorkflowSuspended") && null != columnValue) {
                        if (columnValue.equals(true)) {
                            columnValue = "Yes";
                        } else {
                            columnValue = "No";
                        }
                    }
                    if (columnConfiguration.getDataField().equalsIgnoreCase("isSubProcess") && null != columnValue) {
                        if (columnValue.equals(true)) {
                            columnValue = "Yes";
                        } else {
                            columnValue = "No";
                        }
                    }
                    if (columnConfiguration.getDataField().equalsIgnoreCase("formMandatory") && null != columnValue) {
                        if (columnValue.equals(true)) {
                            columnValue = "Yes";
                        } else {
                            columnValue = "No";
                        }
                    }
                    if (columnConfiguration.getDataField().equalsIgnoreCase("dynamicWorkflowContext") && null != columnValue) {
                        String columnVal = columnValue.toString();
                        if(StringUtils.isNotEmpty(columnVal)){
                            try {
                                Class clazz = Class.forName(columnVal);
                                columnValue = clazz.getSimpleName();
                            } catch (ClassNotFoundException e){
                                columnValue = "";
                            }
                        }
                    }
                    if (columnConfiguration.getDataField().equalsIgnoreCase("formuuid") && null != columnValue) {
                        columnValue = formService.getFormNameByuuid(String.valueOf(columnValue));
                    }

                    if (columnConfiguration.getIsPercentage() != null && columnConfiguration.getIsPercentage() == true) {
                        if (columnValue == null) {
                            columnValue = String.format("%.2f", Double.valueOf(0));
                        } else {
                            columnValue = String.format("%.2f", columnValue);
                        }
                    }
                    
                    if("viewProperties.createdBy".equals(columnConfiguration.getDataField()) && (columnValue == null || columnValue == "")){
						columnValue = "Ms Alliya Mishra [seed_maker]";
					}
					
					if(checkReviewedBy && "viewProperties.reviewedBy".equals(columnConfiguration.getDataField()) && (columnValue == null || columnValue == "")){
						columnValue = "Lao Zhang [seed_checker]";
					}
                    
                    if(columnValue instanceof Number){                    	
                    	columnValue= String.valueOf(columnValue);
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

        jsonHelper.setAaData(columnDataList);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").transform(date_transformer, DateTime.class)
                .transform(money_transformer, Money.class).deepSerialize(jsonHelper);
        return jsonString;
    }

    private Class<?> getEntityClass(String keyName) {
        Class<?> entityClass;
        String entityPath = masterConfigurationRegistry.getEntityClass(keyName);
        try {
            entityClass = Class.forName(entityPath);
        } catch (ClassNotFoundException e) {
            throw new SystemException(e);
        }
        return entityClass;
    }


    public Map<String, Object> getEntityData(IGridService gridService, Integer iDisplayStart, Integer iDisplayLength,
                                             String sSortDir, String sortColName, boolean containsSearchEnabled, Class<?> entityName, Map<String, Object> searchMap ) {
       return getEntityData(gridService, iDisplayStart, iDisplayLength, sSortDir,
                  sortColName, containsSearchEnabled, entityName, searchMap, false, false);
    }

    /**
     * Gets the master entity data.
     * 
     * @param mId
     *            the m id
     * @param entityName
     *            the entity name
     * @return the master entity data
     */
    public Map<String, Object> getEntityData(IGridService gridService, Integer iDisplayStart, Integer iDisplayLength,
            String sSortDir, String sortColName, boolean containsSearchEnabled, Class<?> entityName, Map<String, Object> searchMap, boolean isDynamicWC, boolean isWorkflowConfig) {
        Class<?> genericEntityName = entityName;
        UserInfo currentUser = getUserDetails();
        GridVO gridVO = new GridVO();
        gridVO.setiDisplayStart(iDisplayStart);
        gridVO.setiDisplayLength(iDisplayLength);
        gridVO.setSortDir(sSortDir);
        gridVO.setSortColName(sortColName);

        if(isDynamicWC) {
            searchMap.put("isDynamicWorkflow", true);
        } else if(isWorkflowConfig) {
            searchMap.put("isDynamicWorkflow", false);
        }

        gridVO.setSearchMap(searchMap);
        gridVO.setContainsSearchEnabled(containsSearchEnabled);
        return gridService.loadPaginatedData(gridVO, genericEntityName, currentUser.getUserEntityId().getUri(), 0L);
    }

    private List<Integer> getParamTypeInteger(String s){

        return Parameter.getParameterTypeValue(s);
    }

}
