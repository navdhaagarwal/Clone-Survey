package com.nucleus.web.master;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nucleus.html.util.HtmlUtils;

import com.nucleus.core.money.entity.Money;
import com.nucleus.core.money.utils.MoneyUtils;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.GridDataUtility;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.ChildMasterService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.datatable.DataTableJsonHepler;
import com.nucleus.web.formatter.MoneyFormatter;

import flexjson.JSONSerializer;

@Controller
public class ChildMasterGridDataController<T extends BaseMasterEntity> extends BaseController {
	
	private static int 							minCharToBeginSearch 	= 3;
	
	private static final String					SORT_DIRECTION_KEY 		= "sortDirection";
	
	private static final String					SORT_COLUMN_NAME_KEY   	= "sortColumnName";
	
	private static final String					ACTIVE_FLAG_STRING 		= "activeFlag";
	
	private static final String					VIEW_ACTIONS 			= "viewProperties.actions";

	@Inject
	@Named("childMasterService")
	private ChildMasterService 					childMasterService;
	
	@Inject
	@Named("masterConfigurationRegistry")
	private MasterConfigurationRegistry 		masterConfigurationRegistry;
	
	@Inject
    @Named("gridDataUtility")
    private GridDataUtility 					gridDataUtility;
	
	
	@Inject
    @Named("moneyFormatter")
    private MoneyFormatter                      moneyFormatter;
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{parentKey}/{childKey}/loadData/{parentId}")
	public @ResponseBody
		 String loadData(ModelMap map, @PathVariable("parentKey") String parentKey,
			@PathVariable("childKey") String childKey,
			@PathVariable("parentId") Long id,
			@RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
		    @RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
		    @RequestParam(value = "sSortDir_0", required = false) String sSortDir,
		    @RequestParam(value = "iSortCol_0", required = false) Integer iSortCol,
		    @RequestParam(value = "sSearch", required = false) String sSearch,
			@RequestParam(value = "viewable", required = false) Boolean view,
			@RequestParam(value = "sEcho", required = false) Integer sEcho, HttpServletRequest request) 
		 throws Exception {
		
		Map<String, Object> masterMap = childMasterService.createDataTableFromEntityMapper(masterConfigurationRegistry, childKey);
		map.putAll(masterMap);
		map.put("masterID", childKey);
		map.put("parentId", parentKey);
		map.put("childId", childKey);
		if (id == null || id.longValue() == 0) {
			return createDataTableGridData(sEcho, Collections.emptyList(), Collections.emptyMap());
		}
		List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry.getColumnConfigurationList(childKey);
		//Searching
        Map<String, Object> searchMap = createSearchMap(columnConfigurationList, sSearch);
        //sorting
        Map<String, String> sortColAndSortDir = getSortDirectionAndCol(sSortDir, iSortCol, childKey, columnConfigurationList);
		GridVO gridVO = new GridVO();
		gridVO.setiDisplayStart(iDisplayStart);
		gridVO.setiDisplayLength(iDisplayLength);
		gridVO.setSortDir(sortColAndSortDir.get(SORT_DIRECTION_KEY));
		gridVO.setSortColName(sortColAndSortDir.get(SORT_COLUMN_NAME_KEY));
		gridVO.setSearchMap(searchMap);
		String userUri = getUserDetails().getUserEntityId().getUri();
		Map<String, Object> gridData = childMasterService.getChildGridData(parentKey, childKey, userUri, id, gridVO, columnConfigurationList);
		List<T> entities = (List<T>) gridData.get("entityList");
		T parentEntity = (T) gridData.get("parentEntity");
		List<List<Object>> columnDataList = new LinkedList<>();
		List<String> actionList = new ArrayList<>(2);
        actionList.add("Edit");
        actionList.add("Delete");
        for (Object entity : entities) {
            List<Object> rowDataList = new LinkedList<>();
            rowDataList.add(null);
            Object columnValue = null;
            if (parentEntity.getApprovalStatus() != 2 && view != null && !view) {
                ((T) entity).addProperty("actions", actionList);
            }
            for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
            	//Consider variety of data in grid. In that case we need to format the value to be showed.
            	columnValue = getFormattedColumnValue(columnConfiguration, entity);
                rowDataList.add(columnValue);
            }
            columnDataList.add(rowDataList);
        }
        return createDataTableGridData(sEcho, columnDataList, gridData);
	}

	private String createDataTableGridData(Integer sEcho, List<List<Object>> columnDataList, Map<String, Object> gridData) {
		DataTableJsonHepler jsonHolder = new DataTableJsonHepler();
		JSONSerializer iSerializer = new JSONSerializer();
		if (sEcho != null) {
			jsonHolder.setsEcho(sEcho);
		}
		jsonHolder.setAaData(columnDataList);
		if (ValidatorUtils.hasNoEntry(gridData)) {
			jsonHolder.setiTotalDisplayRecords(0);
			jsonHolder.setiTotalRecords(0);
			return iSerializer.exclude("*.class").deepSerialize(jsonHolder);
		}
		jsonHolder.setiTotalDisplayRecords((Integer)gridData.get("recordsCount"));
		jsonHolder.setiTotalRecords((Integer)gridData.get("totalRecordsCount"));
		return iSerializer.exclude("*.class").deepSerialize(jsonHolder);
	}

	private Object getFormattedColumnValue(ColumnConfiguration columnConfiguration, Object entity) {
		Object columnValue = null;
		try {
        	columnValue = gridDataUtility.getColumnValueFromColumnConfiguration(entity, columnConfiguration);
            if (isListInstance(columnConfiguration, columnValue)) {
                columnValue = ((Collection<?>)columnValue).size();
            }
            if (isSetInstance(columnConfiguration, columnValue)) {
                columnValue = ((Collection<?>)columnValue).size();
            }
            if (columnValue instanceof DateTime) {
                columnValue = getFormattedDate((DateTime) columnValue);
            }
            if (columnValue instanceof Timestamp) {
                columnValue = getFormattedTimeStampValue(columnValue);
            }
            if (columnValue instanceof Calendar) {
                columnValue = getFormattedStringForCalendar(columnValue);
            }
            if (columnValue instanceof Money) {
                String formattedMoney = moneyFormatter.print((Money) columnValue, null);
                columnValue = formattedMoney.replaceAll(MoneyUtils.MONEY_DELIMITER, " ");
            }
            if (columnValue instanceof String) {                    	
            	columnValue=HtmlUtils.htmlEscape((String)columnValue);
            }
            if (columnConfiguration.getDataField().equalsIgnoreCase(ACTIVE_FLAG_STRING) && null != columnValue) {
            	columnValue = columnValue.equals(true) ? "Active" : "Inactive";
            }
            if (columnConfiguration.getIsPercentage() != null && columnConfiguration.getIsPercentage()) {
            	columnValue = getFormattedPercentage(columnValue);
            }
            if (columnValue instanceof Number) {                    	
            	columnValue= String.valueOf(columnValue);
            }
        } catch (NestedNullException e) {
            columnValue = null;
            BaseLoggers.exceptionLogger
                    .error("Exception occured while accessing nested property for column configuration '"
                            + columnConfiguration.getTitleKey() + "' :" + e.getMessage(), e);
        }
		return columnValue;
	}

	private boolean isListInstance(ColumnConfiguration columnConfiguration, Object columnValue) {
		return !columnConfiguration.getDataField().equals(VIEW_ACTIONS) && columnValue instanceof List;
	}

	private boolean isSetInstance(ColumnConfiguration columnConfiguration, Object columnValue) {
		return !columnConfiguration.getDataField().equals(VIEW_ACTIONS) && columnValue instanceof Set;
	}

	private Object getFormattedStringForCalendar(Object columnValue) {
		String pattern = getUserDetails().getUserPreferences().get("config.date.formats").getText();
        DateFormat dformat = new SimpleDateFormat(pattern);
		return dformat.format(((Calendar) columnValue).getTime());
	}

	private Object getFormattedTimeStampValue(Object columnValue) {
		String pattern = getUserDetails().getUserPreferences().get("config.date.formats").getText();
        DateFormat dformat = new SimpleDateFormat(pattern);
		return dformat.format(((Timestamp) columnValue).getTime());
	}

	private Object getFormattedPercentage(Object columnValue) {
		Object formattedColumnValue;
		if (columnValue == null) {
			formattedColumnValue = String.format("%.2f", Double.valueOf(0));
        } else {
        	formattedColumnValue = String.format("%.2f", columnValue);
        }
		return formattedColumnValue;
	}

	private Map<String, String> getSortDirectionAndCol(String sSortDir, Integer iSortCol, String childKey,
			List<ColumnConfiguration> columnConfigurationList) {
		String sortDirection = null;
		String sortColName = null;
        if (sSortDir != null && iSortCol != null) {
        	sortDirection = sSortDir.toUpperCase();
            ColumnConfiguration columnConfigurationIndex = columnConfigurationList.get(iSortCol - 1);
            sortColName = columnConfigurationIndex.getDataField();
        } else {
            // We are here because we want default sorting on the column provided in our XML.
            String defaultSortableColumn = masterConfigurationRegistry.getDefaultSortableColumn(childKey);
            String sortableDirection = masterConfigurationRegistry.getSortDirection(childKey);
            if (defaultSortableColumn != null && CollectionUtils.isNotEmpty(columnConfigurationList)) {
                for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                    if (columnConfiguration.getDataField().equals(defaultSortableColumn) && columnConfiguration.getSortable()) {
                    	sortDirection = sortableDirection;
                        sortColName = defaultSortableColumn;
                    }
                }
            }
        }
        Map<String, String> sortDirAndColName = new HashMap<>(2);
        sortDirAndColName.put(SORT_DIRECTION_KEY, sortDirection);
        sortDirAndColName.put(SORT_COLUMN_NAME_KEY, sortColName);
		return sortDirAndColName;
	}

	private Map<String, Object> createSearchMap(List<ColumnConfiguration> columnConfigurationList, String sSearch) {
		Map<String, Object> searchMap = new HashMap<>();
		if (sSearch != null && !sSearch.isEmpty() && sSearch.trim().length() >= minCharToBeginSearch) {
            String searchTerm = sSearch.trim();
            if (CollectionUtils.isNotEmpty(columnConfigurationList)) {
                for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
                    if (columnConfiguration.getSearchable() != null && columnConfiguration.getSearchable()) {
                    	addDataFieldBasedOnRegion(columnConfiguration, searchTerm, searchMap);
                    }
                }
            }
        }
		return searchMap;
	}

	private void addDataFieldBasedOnRegion(ColumnConfiguration columnConfiguration, String searchTerm, Map<String, Object> searchMap) {
		if (columnConfiguration.getIsRegional()) {
    		searchMap.put(columnConfiguration.getRegionalDataField(), searchTerm.toUpperCase());
    	} else {
    		searchMap.put(columnConfiguration.getDataField(), searchTerm.toUpperCase());
    	}
	}
}
