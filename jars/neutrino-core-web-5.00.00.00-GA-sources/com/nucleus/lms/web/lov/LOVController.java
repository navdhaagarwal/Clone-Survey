package com.nucleus.lms.web.lov;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.InterruptedIOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.SAXException;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;
import com.nucleus.finnone.pro.lov.LOVConfigurationLoader;
import com.nucleus.finnone.pro.lov.LOVFilterVO;
import com.nucleus.finnone.pro.lov.LOVJsonUtil;
import com.nucleus.finnone.pro.lov.LOVSearchVO;
import com.nucleus.finnone.pro.lov.LovColumnConfig;
import com.nucleus.finnone.pro.lov.LovConfig;
import com.nucleus.grid.IGridService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.master.MakerCheckerWebUtils;

@Transactional
@Controller
@RequestMapping(value = "/lov")
public class LOVController extends BaseController {
	
	@Inject
	@Named("lovConfigurationLoader")
	private LOVConfigurationLoader lovConfigurationLoader;
	
	@Inject
	private BeanAccessHelper beanAccessHelper;
	
	/**
	 * Injected user service to massage display info in grid related to maker/checker
	 */
	@Inject
	@Named("userService")
	private UserService userService;
	
	@Inject
	@Named("neutrinoExecutionContextHolder")
	private INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;
	
	@RequestMapping(value = "/getLovJson/{key}", method = RequestMethod.GET)
	@ResponseBody
	public LovConfig getLovJson(@PathVariable("key") String key) {
		return lovConfigurationLoader.getConfiguration(key);
	}
	
	@RequestMapping(value = "/showLov/{key}", method = RequestMethod.GET)
	@ResponseBody
	public ModelAndView showLov(@PathVariable("key") String lovKey,@RequestParam(value="searchData", required=false)String searchData,@RequestParam(value="filterData", required=false)String filterData,
								@RequestParam(value="parentInputId", required=false)String parentInputId, @RequestParam(value="parentHiddenId", required=false)String parentHiddenId) 
	{
		LovConfig lovConfig = lovConfigurationLoader.getConfiguration(lovKey);
		
		ModelAndView modelAndView = new ModelAndView("lovModel");
		modelAndView.addObject("lovConfig", lovConfig);
		modelAndView.addObject("searchData", searchData);
		modelAndView.addObject("parentInputId", parentInputId);
		modelAndView.addObject("parentHiddenId", parentHiddenId);
		modelAndView.addObject("filterData", filterData);
		
		return modelAndView;
	}
	
	
	  @RequestMapping(method={org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.POST}, value={"/{Key}/loadLOVData"})
	  @ResponseBody
	  public Map loadLOVData(@PathVariable("Key") String key, @RequestParam(value="start", required=false) Integer iDisplayStart, @RequestParam(value="length", required=false) Integer iDisplayLength, @RequestParam(value="sSortDir_0", required=false) String sSortDir0, @RequestParam(value="iSortCol_0", required=false) Integer iSortCol0, @RequestParam(value="searchData", required=false)String sSearch, @RequestParam(value="draw", required=false) Integer sEcho,@RequestParam(value="filterData", required=false)String filterData, HttpServletRequest request)
	    throws InterruptedIOException, ParserConfigurationException, SAXException, ClassNotFoundException, IllegalAccessException, InvocationTargetException, NoSuchMethodException
	  {
		Map<String,Object> jsonMap = new HashMap<>();
		LOVSearchVO lovSearchVo = new LOVSearchVO();
		Object beanObject;
		List entities;
		Integer iDisplayLengthUpdated=iDisplayLength;
		Integer iDisplayStartUpdated=iDisplayStart;
		if ((iDisplayLength == null) || !(iDisplayLength.intValue() > 0))
		  iDisplayLengthUpdated = Integer.valueOf(10);
	    
		if ((iDisplayStart == null) || !(iDisplayStart.intValue() >= 0))
		  iDisplayStartUpdated = Integer.valueOf(0);
		lovSearchVo.setiDisplayLength(iDisplayLengthUpdated);
		lovSearchVo.setiDisplayStart(iDisplayStartUpdated);
		lovSearchVo.setSortCol(iSortCol0);
		lovSearchVo.setSortDir(sSortDir0);
		
	    LovConfig lovConfig = lovConfigurationLoader.getConfiguration(key);
		List<LovColumnConfig> columnConfiguration = lovConfig.getColumnNameList();
	    String serviceName = lovConfig.getServiceBeanName();
	    String operationName = lovConfig.getServiceOperationName();
	    String interfaceName = lovConfig.getServiceInterfaceName();
	    beanObject = beanAccessHelper.getBean(serviceName, Class.forName(interfaceName));
	
	    Class entityClass = getEntityClass(lovConfig.getEntityClass());
	    
	    Map searchMap = new HashMap();
	    if (notNull(sSearch) && (!(sSearch.isEmpty())) && CollectionUtils.isNotEmpty(columnConfiguration)) 
	    {
	          for(LovColumnConfig lovColumnConfig : columnConfiguration)  
	          { 
	            if (lovColumnConfig.getSearchable().booleanValue()==true)
	              searchMap.put(lovColumnConfig.getDataField(), sSearch);
	          }
	    }  
	    
	    List<LovColumnConfig> applicableToBeSortedLovColumnConfig=new ArrayList<>();
	    if(CollectionUtils.isNotEmpty(columnConfiguration)){
	    	for(LovColumnConfig lovColumnConfig : columnConfiguration)  
	        { 
	    		if (notNull(lovColumnConfig.getSortOrder()) && lovColumnConfig.getSortOrder() > 0)
	        	{
	        	  applicableToBeSortedLovColumnConfig.add(lovColumnConfig);
	        	}
	        }
	    }
	    
	    List<String> sortedList = new ArrayList<>(); 
	    if(CollectionUtils.isNotEmpty(applicableToBeSortedLovColumnConfig))
	    {
	    	Collections.sort(applicableToBeSortedLovColumnConfig, new LOVColumnComparatorSortByOrder());
	    	
		   	for(LovColumnConfig lovColumnConfig : applicableToBeSortedLovColumnConfig)  
		    { 
		       	sortedList.add(lovColumnConfig.getDataField());
		    }
		}
	    
	    List<LOVFilterVO> lovFilterVOList = new ArrayList<>(); 
	    if ((filterData != null) && (!(filterData.isEmpty()))) 
	    	lovFilterVOList = getFilterDataListFromJSONString(filterData); 
	    
	    lovSearchVo.setSearchMap(searchMap);
	    lovSearchVo.setSortedElements(sortedList);
	    lovSearchVo.setFilterVoList(lovFilterVOList);
	    lovSearchVo.setEntityClass(entityClass);
	    
	    

	    Map searchRecordMap = (Map)MethodUtils.invokeMethod(beanObject, operationName, lovSearchVo);
        entities = (List)searchRecordMap.get("searchRecordList");
        Integer totalRecordCount = notNull(searchRecordMap.get("totalRecordListSize"))?(Integer)searchRecordMap.get("totalRecordListSize"):Integer.valueOf(0);
        if (sEcho != null) {
        	
        	jsonMap.put("sEcho",sEcho.intValue());
	    	jsonMap.put("iTotalRecords",totalRecordCount + iDisplayStart);
	    	jsonMap.put("iTotalDisplayRecords",totalRecordCount + iDisplayStart);
        }
	    
        
	   List<Map<String,Object>> lovDataList = new LinkedList<>();
	   if(notNull(entities)){
		   Iterator i = entities.iterator();
		   while(i.hasNext()) 
		   { 
		    	Object entity = i.next();
		    
		    	if(BaseEntity.class.isAssignableFrom(entity.getClass())){
		    		applyCommonViewProperties((BaseEntity)entity);	
		    	}
		    	
		    	Map<String,Object> rowDataMap = new HashMap<>();
		    	Object columnValue;
		    	for(LovColumnConfig lovColumnConfig : columnConfiguration) 
		    	{ 
		    		try 
		    		{
		    			columnValue = PropertyUtils.getNestedProperty(entity, lovColumnConfig.getDataField());
			        }
		    		catch (NestedNullException e)
		    		{
		    			columnValue = null;
		    			BaseLoggers.exceptionLogger.error("Exception occured while accessing nested property for column configuration '" + lovColumnConfig.getTitleKey() + "' :" + e.getMessage());
		    		}
		    		rowDataMap.put(lovColumnConfig.getDataField(), columnValue);
		    		rowDataMap.put(lovColumnConfig.getDataFieldSimpleName(), columnValue);
		    	}
		    	rowDataMap.put("radioId", lovDataList.size()+1);
		    	lovDataList.add(rowDataMap);
		    }
	   }
	   jsonMap.put("iDisplayStart",iDisplayStart);
	   jsonMap.put("iDisplayLength",iDisplayLength);
	   jsonMap.put("aaData", lovDataList);
	   jsonMap.put("lovConfig", lovConfig);
	   return jsonMap;
	  }
	  
	  private Class<?> getEntityClass(String entityClassPath)
	  {
	    Class entityClass;
	    try {
	      entityClass = Class.forName(entityClassPath);
	    } catch (ClassNotFoundException e) {
	      throw new SystemException(e);
	    }
	    return entityClass;
	  }
	  
	  public Map<String, Object> getEntityData(IGridService gridService, Integer iDisplayStart, Integer iDisplayLength, String sSortDir, String sortColName, Class<?> entityName)
	  {
	    Class genericEntityName = entityName;
	    UserInfo currentUser = getUserDetails();
	    return gridService.loadPaginatedData(genericEntityName, currentUser.getUserEntityId().getUri(), Long.valueOf(0L), iDisplayStart, iDisplayLength, sSortDir, sortColName);
	  }
	  
	private List<LOVFilterVO> getFilterDataListFromJSONString(String jsonString) {
		Tenant tenant = (Tenant) neutrinoExecutionContextHolder.getTenant();

		String dateFormat = tenant.getDateFormat();
		Map<String, ConfigurationVO> preferences = getUserDetails().getUserPreferences();
		ConfigurationVO preferredDateFormat;
		if (preferences != null && !preferences.isEmpty()) {
			preferredDateFormat = preferences.get("config.date.formats");
		} else {
			preferredDateFormat = new ConfigurationVO();
			preferredDateFormat.setPropertyValue(tenant.getDateFormat());
		}

		if (preferredDateFormat != null && preferredDateFormat.getPropertyValue() != null && !"".equals(preferredDateFormat.getPropertyValue()))
			dateFormat = preferredDateFormat.getPropertyValue();

		return LOVJsonUtil.parseJsonToObject(jsonString, List.class, LOVFilterVO.class, dateFormat);

	}
	  
	  
	  /**
	   * Applies common view properties for base master entity
	   * @param baseMasterEntity
	   */
	  private void applyCommonViewPropertiesForBaseMasterEntity(BaseMasterEntity baseMasterEntity){
		  UserInfo reviewedBy = null;
		  EntityId revieworEntityId = baseMasterEntity.getMasterLifeCycleData().getReviewedByEntityId();
	  	  if (revieworEntityId != null)
	  				reviewedBy = this.userService.getUserById(revieworEntityId.getLocalId());
	  	if (reviewedBy != null)
	  		baseMasterEntity.addProperty("reviewedBy", reviewedBy.getUsername());
	  	baseMasterEntity.addProperty("approvalStatus", MakerCheckerWebUtils.getApprovalStatus(baseMasterEntity.getApprovalStatus()));
		  
	  }
	  /**
	   * Applies common view properties for base entity and base master entity(if qualifies)
	   * @param baseEntity
	   */
	  private void applyCommonViewProperties(BaseEntity baseEntity){
	    	UserInfo createdBy = null;
	    
	    	if (baseEntity.getEntityLifeCycleData() != null) 
	    	{
	    		EntityId createorEntityId = baseEntity.getEntityLifeCycleData().getCreatedByEntityId();
	    		if (createorEntityId != null){
	    			createdBy = this.userService.getUserById(createorEntityId.getLocalId());
	    		}
	    		
	    		baseEntity.addProperty("uuid", baseEntity.getEntityLifeCycleData().getUuid());
	    	}
	    	if (createdBy != null){
	    		baseEntity.addProperty("createdBy", createdBy.getUsername());
	    	}
	    	
	    	if(BaseMasterEntity.class.isAssignableFrom(baseEntity.getClass())){
	    		applyCommonViewPropertiesForBaseMasterEntity((BaseMasterEntity)baseEntity);
	    	}
	    	
	  }
	  
	  private static class LOVColumnComparatorSortByOrder implements Comparator<LovColumnConfig> , Serializable{
			
			private static final long serialVersionUID = 1L;
			@Override
			public int compare(LovColumnConfig s1, LovColumnConfig s2) { 
					return s1.getSortOrder().compareTo(s2.getSortOrder());
			}
		}
}
