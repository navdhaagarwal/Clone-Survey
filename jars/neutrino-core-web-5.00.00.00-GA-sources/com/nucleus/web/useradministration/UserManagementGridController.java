package com.nucleus.web.useradministration;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.entity.ApprovalStatus;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.web.master.MakerCheckerWebUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.support.WebApplicationContextUtils;
import com.nucleus.html.util.HtmlUtils;
import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ActionConfiguration;
import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.datatable.DataTableJsonHepler;

import flexjson.JSONSerializer;

@Transactional
@Controller
@RequestMapping(value = "/UserInfo")
public class UserManagementGridController extends BaseController {

    @Inject
    @Named("masterConfigurationRegistry")
    private MasterConfigurationRegistry masterConfigurationRegistry;
    
    @Inject
    @Named("entityDao")
    private EntityDao                   entityDao;

    @Inject
    @Named("userGridServiceCore")
    private UserGridServiceImpl         userGridService;
    
    @Inject
    @Named("userService")
    protected UserService               userService;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    private static Class<?>  					cachedEntityClass;
    
    private static final String			USER_INFO = "UserInfo";
    private static final String     CONFIGURATION_QUERY      = "Configuration.getPropertyValueFromPropertyKey";
    private static final String USER_SHOW_DOWNLOAD_UPLOAD="config.show.user.download.upload";

    @SuppressWarnings("unchecked")
    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY') or hasAuthority('MAKER_USER') or hasAuthority('CHECKER_USER') or hasAuthority('VIEW_USER')")    
    //security added
    @RequestMapping(value = "/loadColumnConfig")
    public String loadUserInfoMainPage(@RequestParam(value = "filteredStatus", required = false) boolean filteredStatus,
            ModelMap map, HttpServletRequest request) throws IOException {
    	
        Map<String, Object> masterMap = createDataTableFromEntityMapper(USER_INFO, request);
        String recordURL = masterConfigurationRegistry.getRecordURL(USER_INFO);
        String key = masterConfigurationRegistry.getKey(USER_INFO);
        Boolean processingType = masterConfigurationRegistry.getProcessingType(USER_INFO);
        if (processingType == null) {
            processingType = true;
        }
        Boolean hrefBool = masterConfigurationRegistry.getHrefBool(USER_INFO);
        if (hrefBool == null) {
            hrefBool = Boolean.TRUE;
        }
        String productShowDownloadUpload = configurationService.getPropertyValueByPropertyKey(USER_SHOW_DOWNLOAD_UPLOAD, CONFIGURATION_QUERY);
        if(StringUtils.isNotEmpty(productShowDownloadUpload)){
            List<String> productCodes= Arrays.asList(productShowDownloadUpload.split(","));
            if(productCodes.contains(ProductInformationLoader.getProductCode())){
                map.put("configShowDownloadUpload", true);
            }else {
                map.put("configShowDownloadUpload", false);
            }
        }else {
            map.put("configShowDownloadUpload", true);
        }
        map.put("masterId", "UserInfo");
        masterMap.put("recordURL", recordURL);
        map.put("bFilter", true);
        map.put("bInfo", true);
        map.put("bSort", true);
        map.put("bLengthChange", true);
        map.put("bJQueryUI", false);
        map.put("serverSide", processingType);
        map.put("hrefBoolType", hrefBool);
        map.put("bPaginate", true);
        map.put("filteredStatus", filteredStatus);
        map.put("Key", key);
        map.putAll(masterMap);
        return "userManagementMainPage";

    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/loadPage/{filteredStatus}")
    public String loadUserInfoDetailsPage(@PathVariable boolean filteredStatus, ModelMap map, HttpServletRequest request)
            throws IOException {
        Map<String, Object> masterMap = createDataTableFromEntityMapper(USER_INFO, request);
        String recordURL = masterConfigurationRegistry.getRecordURL(USER_INFO);
        String key = masterConfigurationRegistry.getKey(USER_INFO);
        map.put("masterId", "UserInfo");
        masterMap.put("recordURL", recordURL);
        map.put("bFilter", true);
        map.put("bInfo", true);
        map.put("bSort", true);
        map.put("bLengthChange", true);
        map.put("bJQueryUI", false);
        map.put("serverSide", false);
        map.put("bPaginate", true);
        map.put("filteredStatus", filteredStatus);
        map.put("Key", key);
        map.putAll(masterMap);
        return "userManagementDatatable";

    }

    @PreAuthorize("hasAuthority('ADMIN_AUTHORITY') or hasAuthority('MAKER_USER') or hasAuthority('CHECKER_USER') or hasAuthority('VIEW_USER')")
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/loadData/{filteredStatus}")
    public @ResponseBody
    	String loadUserInfoDetailData(@PathVariable boolean filteredStatus,
    		 @RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
             @RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
             @RequestParam(value = "sSortDir_0", required = false) String sSortDir_0,
             @RequestParam(value = "iSortCol_0", required = false) Integer iSortCol_0,
             @RequestParam(value = "sSearch", required = false) String sSearch,
             @RequestParam(value = "sEcho", required = false) Integer sEcho,ModelMap map, HttpServletRequest request)
             throws IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
    	
    	String sortColName = null;
        if (iDisplayLength != null) {
            if (iDisplayLength == -1) {
                iDisplayLength = 10;

            }
        }
        
        String serviceName = masterConfigurationRegistry.getServiceBean(USER_INFO);
        UserGridService gridService = (UserGridService) WebApplicationContextUtils.getWebApplicationContext(
                request.getSession().getServletContext()).getBean(serviceName);
        
        Integer recordCount = 0;
        Integer totalRecordCount = 0;
        DataTableJsonHepler jsonHelper = new DataTableJsonHepler();
        List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry
                .getColumnConfigurationList(USER_INFO);
        
        //Map<String, Object> masterMap = createDataTableFromEntityMapper("UserInfo", request);
        
        UserInfo currentUser = getUserDetails();
        if (cachedEntityClass == null) {
        	cachedEntityClass = getEntityClass(USER_INFO);;
        }
        
        List<Object> entities = null;

        	//Searching
	        Map<String, Object> searchMap = new HashMap<String, Object>();
	        if (sSearch != null && !sSearch.isEmpty() && sSearch.trim().length() >= 3) {
	            String searchTerm = sSearch.trim();
	            
	            if (CollectionUtils.isNotEmpty(columnConfigurationList)) {
	                for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
	                    if (columnConfiguration.getSearchable() == true) {
	                        searchMap.put(columnConfiguration.getDataField(), searchTerm.toLowerCase());
	                    }
                        if("viewProperties.createdBy".equals(columnConfiguration.getDataField())) {
                            searchMap.remove(columnConfiguration.getDataField());
                            searchMap.put("entityLifeCycleData.createdByUri", searchTerm.toLowerCase());
                            searchMap.put("entityLifeCycleData.lastUpdatedByUri", searchTerm.toLowerCase());
                        } else if("viewProperties.approvalStatus".equals(columnConfiguration.getDataField())) {
                            searchMap.remove(columnConfiguration.getDataField());
                            searchMap.put("masterLifeCycleData.approvalStatus", MakerCheckerWebUtils.getApprovalStatusCode(searchTerm.toUpperCase()));
                        } else if( !filteredStatus && "viewProperties.loggedInModules".equals(columnConfiguration.getDataField())) {
                            searchMap.remove(columnConfiguration.getDataField());
                        }
	                }
	            }
	                
	        }

            //Sorting
            if (sSortDir_0 != null && iSortCol_0 != null) {
                if ("asc".equals(sSortDir_0)) {
                    sSortDir_0 = "ASC";
                }
                if ("desc".equals(sSortDir_0)) {
                    sSortDir_0 = "DESC";
                }

                ColumnConfiguration columnConfigurationIndex = columnConfigurationList.get(iSortCol_0 - 1);
                sortColName = columnConfigurationIndex.getDataField();
                if("viewProperties.createdBy".equals(columnConfigurationIndex.getDataField())) {
                    sortColName = "entityLifeCycleData.createdByUri";
                } else if("viewProperties.approvalStatus".equals(columnConfigurationIndex.getDataField())) {
                    sortColName = "masterLifeCycleData.approvalStatus";
                }
            } else {
                // We are here because we want default sorting on the column
                // provided in our XML
                String defaultSortableColumn = masterConfigurationRegistry.getDefaultSortableColumn(USER_INFO);
                String sortableDirection = masterConfigurationRegistry.getSortDirection(USER_INFO);
                if (defaultSortableColumn != null && CollectionUtils.isNotEmpty(columnConfigurationList)) {
                    for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                        if (columnConfiguration.getDataField().equals(defaultSortableColumn) && columnConfiguration.getSortable()) {
                            sSortDir_0 = sortableDirection;
                            sortColName = defaultSortableColumn;
                        }

                    }
                }
            }

            //Set Grid Info to GridVO
            GridVO gridVO = new GridVO();
            gridVO.setiDisplayStart(iDisplayStart);
            gridVO.setiDisplayLength(iDisplayLength);
            gridVO.setSortDir(sSortDir_0);
            gridVO.setSortColName(sortColName);
            gridVO.setSearchMap(searchMap);
            
            Map<String, Object> entityMap = getEntityData(gridVO, gridService, cachedEntityClass, filteredStatus);
            
            entities = (List<Object>) entityMap.get("entityList");
            recordCount = (Integer) entityMap.get("recordCount");
            totalRecordCount = (Integer) entityMap.get("totalRecordCount");
            
        
        entities = entities == null ? Collections.emptyList() : entities;
        List<List<Object>> columnDataList = new LinkedList<List<Object>>();
        
        for (Object entity : entities) {
            int approvalStatus = 0;
            if(entity instanceof BaseMasterEntity) {
                approvalStatus = ((BaseMasterEntity) entity).getApprovalStatus();
            }
            boolean checkReviewedBy = (approvalStatus == ApprovalStatus.APPROVED || approvalStatus == ApprovalStatus.APPROVED_MODIFIED || approvalStatus == ApprovalStatus.APPROVED_DELETED);
            User entityUser = (User) entity;
            UserInfo loggedInUser=userService.getUserById(entityUser.getId());
            
            String userUri = loggedInUser.getCreatedBy();
            if (loggedInUser !=null && loggedInUser.getUsername() !=null && loggedInUser.getUsername().equals(currentUser.getUsername())) {
            	
            	
                continue;
            }
            /*Following two line of Code is used to change record of Created By Column in Logged In Users  
             * previously it was showing Uri, now It will show user name
             */
            if (loggedInUser.getCreatedBy() != null && filteredStatus == true) {
                User user = (User) entityDao.get(EntityId.fromUri(loggedInUser.getCreatedBy()));
				if (user != null) {
					loggedInUser.setCreatedByLoggedInUsersByName(user.getUsername());
				} else {
					loggedInUser.setCreatedByLoggedInUsersByName(null);
				}
            }
            List<Object> rowDataList = new LinkedList<Object>();
            rowDataList.add(null);
            Object columnValue = null;
            for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                try {
                    columnValue = PropertyUtils.getNestedProperty(entity, columnConfiguration.getDataField());
                    
                    if("entityLifeCycleData.creationTimeStamp".equals(columnConfiguration.getDataField()) && columnValue == null) {
                        columnValue = new DateTime(2008, 1, 1, 12, 0);
                    }
                    if (columnValue instanceof DateTime) {
                        columnValue = getFormattedDateTime((DateTime) columnValue);
                    }
					if (columnValue instanceof String) {
						columnValue=("viewProperties.createdBy".equals(columnConfiguration.getDataField()) || 
                				"viewProperties.reviewedBy".equals(columnConfiguration.getDataField()))?columnValue:HtmlUtils.htmlEscape((String)columnValue);
					}
					
					if("viewProperties.createdBy".equals(columnConfiguration.getDataField()) && (columnValue == null || columnValue == "")){
						columnValue = "Ms Alliya Mishra [seed_maker]";
					}
					
					if(checkReviewedBy && "viewProperties.reviewedBy".equals(columnConfiguration.getDataField()) && (columnValue == null || columnValue == "")){
						columnValue = "Lao Zhang [seed_checker]";
					}
					
                    if (columnConfiguration.getDataField().equalsIgnoreCase("userStatus") && null != columnValue) {
                        if (columnValue.equals(0)) {
                            columnValue = "Active";
                        } else if (columnValue.equals(1)) {
                            columnValue = "Inactive";
                        } else if (columnValue.equals(2)) {
                            columnValue = "Locked";
                        } else if (columnValue.equals(3)) {
                            columnValue = "Deleted";
                        }
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
            if (loggedInUser.getCreatedBy() != null && filteredStatus == true)
                loggedInUser.setCreatedByLoggedInUsersByName(userUri);
        }
        
        /*recordCount = (Integer) entityMap.get("totalRecordCount");
        if (sEcho != null) {
            jsonHelper.setsEcho(sEcho);
            jsonHelper.setiTotalDisplayRecords(recordCount);
            jsonHelper.setiTotalRecords(recordCount);
        }*/
        if (sEcho != null) {
            jsonHelper.setsEcho(sEcho);
            jsonHelper.setiTotalDisplayRecords(recordCount);
            jsonHelper.setiTotalRecords(totalRecordCount);
        }
        jsonHelper.setAaData(columnDataList);
        JSONSerializer iSerializer = new JSONSerializer();
        /*map.put("masterId", "User");
        map.put("bFilter", true);
        map.put("bInfo", true);
        map.put("bSort", true);
        map.put("bLengthChange", true);
        map.put("bJQueryUI", false);
        map.put("serverSide", false);
        map.put("bPaginate", true);
        map.put("filteredStatus", filteredStatus);
        map.putAll(masterMap);*/
        return iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        

    }

    public Map<String, Object> getEntityData(GridVO gridVO, UserGridService gridService,Class<?> entityName, boolean filteredStatus) {
        Class<?> genericEntityName = entityName;
        UserInfo currentUser = getUserDetails();
        Map<String, Object> childData = gridService.loadPaginatedData(gridVO, genericEntityName, currentUser.getUserEntityId()
                .getUri(), 0L, filteredStatus);
        return childData;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Map createDataTableFromEntityMapper(String masterEntity, HttpServletRequest request) throws IOException {
        Map masterMap = new HashMap<String, Object>();
        List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry
                .getColumnConfigurationList(masterEntity);
        List<ActionConfiguration> actionConfiguration = masterConfigurationRegistry.getActionConfigurationList(masterEntity);
        masterMap.put("actionConfiguration", actionConfiguration);
        masterMap.put("dataTableRecords", columnConfigurationList);
        return masterMap;
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

}
