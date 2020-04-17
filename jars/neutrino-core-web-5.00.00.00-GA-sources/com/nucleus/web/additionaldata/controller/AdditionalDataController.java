/**
@author merajul.ansari
Creation Date: 30/01/2013
Copyright: Nucleus Software Exports Ltd.
Description: Controller for Additional Data functionality
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.web.additionaldata.controller;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.serviceinterface.IAdditionalDataService;
import com.nucleus.finnone.pro.additionaldata.util.AdditionalDataConverterUtility;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.web.binder.MasterMapDataBinder;
import com.nucleus.web.common.controller.BaseController;


public class AdditionalDataController extends BaseController {

	@Inject
	@Named("additionalDataService")
	private IAdditionalDataService additionalDataService;
	
	@Inject
	@Named("additionalDataConverterUtility")
	private AdditionalDataConverterUtility additionalDataConverterUtility;
	
	@Inject
	@Named("genericParameterService")
	private GenericParameterServiceImpl genericParameterService; 
	
	public static final String SPAN_SIX = "span6";
	public static final String ADDITIONAL_DATA = "additionalData";
	
	@RequestMapping(value = "/display", method = RequestMethod.POST)
	public ModelAndView displayAdditionalData(@RequestParam String transactionType, @RequestParam(required=false) Long additionalDataId,
			@RequestParam String viewMode,
			@RequestParam String startTabIndex,
			@RequestParam(required=false) String labelType,
			@RequestParam(required=false) String columnPerRow,
			HttpSession session){
		List<AdditionalDataMetaData> additionalDataMetaDataList = (List)session.getAttribute("additionalDataMetaDataList");
		if(hasNoElements(additionalDataMetaDataList) ||
				! transactionType.equals(additionalDataMetaDataList.get(0).getAdditionalDataTransactionType().getServiceRequestTransactionTypeCode()))
		{
			additionalDataMetaDataList = additionalDataService.getAdditionalDataMetaDataByTransactionType(transactionType);
		}
		AdditionalData additionalData = null;
		if(additionalDataId!=null){
			additionalData = (AdditionalData) session.getAttribute(ADDITIONAL_DATA);
			session.removeAttribute(ADDITIONAL_DATA);
			
			if(additionalData==null  || additionalDataId.compareTo(additionalData.getId())!=0 ){
				additionalData = additionalDataService.getAdditionalData(additionalDataId);
			}
		}
		
		if(additionalData==null){
			additionalData = new AdditionalData();
		}else{
			additionalDataConverterUtility.transformServiceToConsumer(additionalData, additionalDataMetaDataList);
		}
		
		
		ModelAndView modelAndView = new ModelAndView("AdditionalData");
		modelAndView.addObject("additionalData", additionalData);
		session.setAttribute("additionalDataMetaDataList", additionalDataMetaDataList);
		modelAndView.addObject("viewMode", viewMode);
		modelAndView.addObject("labelType",labelType);
		modelAndView.addObject("startTabIndex",startTabIndex);
		setColumnPerRow(modelAndView,labelType,columnPerRow);
		setMasterDataList(modelAndView,additionalDataMetaDataList);
		
		return modelAndView;
	
	}
	
	private void setMasterDataList(ModelAndView modelAndView,List<AdditionalDataMetaData> additionalDataMetaDataList) 
	{
		List<GenericParameter> genericParameters = null;
    	Object masterData = null;
    	List<Object> finalList = null;
    	Map<String,String> map = null;
    	String fieldName = null;
    	
		for(AdditionalDataMetaData additionalDataMetaData : additionalDataMetaDataList)
		{
			if("L".equals(additionalDataMetaData.getCustomFieldDataTypeObject().getCode()))
			{
				try
		    	{
					finalList = new ArrayList<Object>();
					if(additionalDataMetaData.getListOf()!=null){
			    		Class masterClass = Class.forName(additionalDataMetaData.getListOf());
			    		if (GenericParameter.class.isAssignableFrom(masterClass))
			    		{
			    			genericParameters =  genericParameterService.retrieveTypes(masterClass);
			    			for(GenericParameter genericParameter : genericParameters)
			    			{
			    				map = new HashMap<String, String>();
			    				map.put("id", genericParameter.getId().toString());
			    				map.put("value", genericParameter.getName());
			    				finalList.add(map);
			    			}
			    		}
						else if (BaseMasterEntity.class.isAssignableFrom(masterClass))
						{
							fieldName = additionalDataMetaData.getListField();
							masterData = new MasterMapDataBinder(masterClass, new String[] { "id",fieldName}).getData();
							List<Map> masterDataList = (List<Map>)masterData;
							for(Map mastermap : masterDataList)
							{
								map = new HashMap<String, String>();
			    				map.put("id", mastermap.get("id").toString());
			    				map.put("value", mastermap.get(fieldName).toString());
			    				finalList.add(map);
			    			}
				    	}
					}
				}
		    	catch(Exception e)
		    	{
		    		 throw new BaseException("Additional Data Error has occured", e);
		    		
		    	}
				modelAndView.addObject("list"+additionalDataMetaData.getMappingField(),finalList);
			}
		}
	}
	
	
	/**
	 * Method to set CSS class and no of columns per row
	 * @param modelAndView
	 * @param labelType
	 * @param columnPerRow
	 */
	private void setColumnPerRow(ModelAndView modelAndView, String labelType, String columnPerRow){
		String inputCSSClass = SPAN_SIX;
		String noOfColumns=columnPerRow;
		if("KEY".equals(labelType)){
			if("1".equals(columnPerRow)){
				inputCSSClass = "span12";
			}else if("3".equals(columnPerRow)){
				inputCSSClass = "span4";
			}else{
				inputCSSClass = SPAN_SIX;
				noOfColumns="2";
			}
		}else
		{
			if("1".equals(columnPerRow)){
				inputCSSClass = SPAN_SIX;
			}else if("3".equals(columnPerRow)){
				inputCSSClass = "span2";
			}else{
				inputCSSClass = "span3";
				noOfColumns="2";
			}
		}
		modelAndView.addObject("inputCSSClass",inputCSSClass);
		modelAndView.addObject("columnPerRow",noOfColumns);
	}
	
	public void setAdditionalDataService(
			IAdditionalDataService additionalDataService) {
		this.additionalDataService = additionalDataService;
	}
	

}