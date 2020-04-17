package com.nucleus.web.search;

/**
 * advance search controller
 */
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.persistence.HibernateUtils;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.searchframework.entity.SearchAttributeBean;
import com.nucleus.core.searchframework.entity.SearchRequest;
import com.nucleus.core.searchframework.service.SearchConfigProcessor;
import com.nucleus.core.searchframework.service.SearchException;
import com.nucleus.core.searchframework.service.SearchFrameworkService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;

@Transactional
@Controller
@RequestMapping(value = "/searchFramwork")
// @SessionAttributes(value = {"searchAttributeBeanMap","searchAttributeBeanList"})
public class AdvanceSearchController extends BaseController {

	@Inject
	@Named("searchConfigProcessor")
	private SearchConfigProcessor  searchConfigProcessor;

	@Inject
	@Named("searchFrameworkService")
	private SearchFrameworkService searchFrameworkService;

	@Inject
	@Named("userService")
	private UserService            userService;

	@Inject
	@Named("messageSource")
	protected MessageSource        messageSource;

	private static String          SCHEME_REQUESTID = "schemes";
    private static Map<String, Object> dataMap = new HashMap<>();
    private static Map<String, Object> maskedDataMap = new HashMap<>();
	/**
	 * to start advance search page
	 * @param entityName
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/advanceSearch/{EntityName}")
	public String getAdvanceSearch(@PathVariable("EntityName") String entityName, ModelMap map, HttpServletRequest request) {
		SearchAttributeListBean attributeList = new SearchAttributeListBean();
		attributeList
		.setSearchAttributeList(searchConfigProcessor.prepareSearchAttributesForSearchConfiguration(entityName));

		attributeList.setSearchRequestEntityId(entityName);

		Locale loc = RequestContextUtils.getLocale(request);
		String entityNameLabel = messageSource.getMessage("label.search." + entityName, null, loc);

		map.put("searchAttributeBeanList", attributeList);
		map.put("EntityName", entityName);
		map.put("EntityNameLabel", entityNameLabel);
		return "advanceSearch";
	}

	/**
	 * To get result of advance search
	 * @param attributList
	 * @param map
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/search", method = RequestMethod.POST)
	public String startAdvanceSearch(@ModelAttribute("searchAttributeBeanList") SearchAttributeListBean attributList,
			ModelMap map) {
		try {

			if (CollectionUtils.isNotEmpty(attributList.getSearchAttributeList())) {
				Iterator<SearchAttributeBean> searchItr = attributList.getSearchAttributeList().iterator();
				// List<SearchAttributeBean> searchAttrList = new ArrayList<SearchAttributeBean>();
				while (searchItr.hasNext()) {
					SearchAttributeBean sab = searchItr.next();
					if (sab.getField().equalsIgnoreCase("addedBy") && !sab.getValue().isEmpty()) {
						String useruri = userService.getUserUriByUserName(sab.getValue());
						sab.setValue(useruri);
					}

				}
			}
			SearchRequest searchRequest = searchConfigProcessor.getSearchRequestData(attributList.getSearchAttributeList(),
					attributList.getSearchRequestEntityId());

			List<String> selectFieldList = searchRequest.getFieldList();               	
			List<Map> list = searchFrameworkService.executeSearchRequest(searchRequest);
			List customerList =new ArrayList<Map>();
			if (list != null) {
				Iterator<Map> itr = list.iterator();
				while (itr.hasNext()) {
					Object obj = itr.next();
					if (obj instanceof Map) {
						Map temp = (Map)obj;
						if(temp.get("addedBy") != null){
							String username = userService.getUserNameByUserUri(temp.get("addedBy").toString());
							temp.put("addedBy", username);
						}
					}	
					Object result=searchFrameworkService.customizeSearchResult(obj);
					if(result!=null)
					{	
						customerList.add(result);
					}
				/*if(list.size()>1)
				{
					if(attributList.getSearchRequestEntityId().equalsIgnoreCase("loanApplication") && list.contains(obj))
					{
						itr.remove();
					}
				}*/


				}
			}
			if (attributList.getSearchRequestEntityId().equals(SearchAttributeListBean.CUSTOMER)) {
				selectFieldList.add("id");
				selectFieldList.add("customerNumber");
				selectFieldList.add("cIFNumber");
				selectFieldList.add("customerName");
				list=customerList;
			}


			map.put("ResultList", list);
			map.put("EntityId", attributList.getSearchRequestEntityId());
			map.put("selectFieldList", selectFieldList);

		} catch (SearchException se) {
			map.put("searchTypeFlag", "Search Type Not Supported");
			BaseLoggers.exceptionLogger.error("SearchException while executing search request", se.getMessage());
		}
		return "advanceSearchResult";
	}

	/**
	 * method to view detail of all other masters
	 * @param entityId
	 * @param attributList
	 * @return
	 */
	@RequestMapping(value = "/viewDetail")
	public @ResponseBody
	String viewDetailOfEntity(@RequestParam("EntityId") String entityId,
			@RequestParam("searchRequestEntityId") String searchRequestEntityId) {
		if (searchRequestEntityId.equals(SCHEME_REQUESTID)) {
			return "/app/LoanScheme/view/" + entityId;
		}
		return null;

	}

	@RequestMapping(value = "/restAdvanceSearch", method = RequestMethod.POST,consumes = "application/json")
	public @ResponseBody
	Map<String,Object> restAdvanceSearch(@RequestBody SearchAttributeListBean attributList) {
		Map<String,Object> resMap = new HashMap<String,Object>();
		resMap.put("result","success");
		resMap.put("searchCount",0);
		try {
			if (CollectionUtils.isNotEmpty(attributList.getSearchAttributeList())) {
				Iterator<SearchAttributeBean> searchItr = attributList.getSearchAttributeList().iterator();
				while (searchItr.hasNext()) {
					SearchAttributeBean sab = searchItr.next();
					if (sab.getField().equalsIgnoreCase("addedBy") && !sab.getValue().isEmpty()) {
						String useruri = userService.getUserUriByUserName(sab.getValue());
						sab.setValue(useruri);
					}
				}
			}
			SearchRequest searchRequest = searchConfigProcessor.getSearchRequestData(attributList.getSearchAttributeList(),
					attributList.getSearchRequestEntityId());
            List<String> selectFieldList = searchRequest.getFieldList();
			List<Map> list = searchFrameworkService.executeSearchRequest(searchRequest);

			List<Map<String,Object>> resultMapList = new ArrayList<Map<String,Object>>();
			if(CollectionUtils.isNotEmpty(list)){

                Map<String,String> fieldsMap = new HashMap<>();
                fieldsMap.put("applicationDetails.applicationNumber","applicationNumber");
                fieldsMap.put("parties.customer.personInfo.fullName","name");
                fieldsMap.put("parties.customer.personInfo.gender.name","gender");
                fieldsMap.put("parties.partyType","partyType");
                fieldsMap.put("parties.customer.personInfo.dateOfBirth","dateOfBirth");
                fieldsMap.put("parties.customer.customerNumber","primaryApplicantCustNumber");
                fieldsMap.put("parties.customer.personInfo.photoUrl","photoUrl");
                fieldsMap.put("applicationStage.name","applicationStage");
                fieldsMap.put("subLoans.scheme.schemeName","schemeName");
                fieldsMap.put("subLoans.loanAmountRequested.baseAmount.baseValue","loanAmountRequested");
                fieldsMap.put("subLoans.productType.description","productType");
                fieldsMap.put("parties.customer.personInfo.transientMaskingMap.fullName","maskedName");

               for(int listIndex=0; listIndex<list.size();listIndex++){
                   dataMap = new HashMap<>();
                   maskedDataMap= new HashMap<>();
                    executeJson(list.get(listIndex),fieldsMap);
                    if(!maskedDataMap.isEmpty()){
                        dataMap.putAll(maskedDataMap);
                    }
				resultMapList.add(dataMap);
			}

			}

			resMap.put("searchCount",list.size());
			resMap.put("resultList", resultMapList);
			resMap.put("entityId", attributList.getSearchRequestEntityId());
		} catch (SearchException se) {
			resMap.put("result", "failure");
			BaseLoggers.exceptionLogger.error("Advance Search Exception while executing search request", se.getMessage());
		}
		return resMap;
	}

	@RequestMapping(value = "/getAdvanceSearchAttributes/{EntityName}",produces = "application/json",method = RequestMethod.GET)
	public @ResponseBody  Map<String,Object> getAdvanceSearchAttributes(@PathVariable("EntityName") String entityName, HttpServletRequest request) {
		Map<String,Object> resMap = new HashMap<>();
		SearchAttributeListBean attributeList = new SearchAttributeListBean();
		attributeList.setSearchAttributeList(searchConfigProcessor.prepareSearchAttributesForSearchConfiguration(entityName));
		attributeList.setSearchRequestEntityId(entityName);

        Locale loc = RequestContextUtils.getLocale(request);
		for(SearchAttributeBean each : attributeList.getSearchAttributeList()){
			each.setDisplayName(messageSource.getMessage("label.searchcriteria."+each.getId(), null, loc));
		}

		resMap.put("searchAttributeBeanList", attributeList);
		resMap.put("EntityName", entityName);
		resMap.put("result","success");
		return resMap;
	}
	private void executeJson(Object loanAppObject,Map<String,String> fieldsMap) {
        dataMap = new HashMap<>();
        Class clazz = loanAppObject.getClass();

        for (Map.Entry<String,String> entry : fieldsMap.entrySet()){
            String[] fieldNameArr = entry.getKey().split("\\.",-1);
            int count = 0;
            executeJsonOnSingleField(fieldNameArr, clazz, loanAppObject, count, entry.getKey(), entry.getValue());
        }

            /*for(String fieldName : fieldNameList) {
			String[] fieldNameArr = fieldName.split("\\.",-1);
			int count = 0;
			executeJsonOnSingleField(fieldNameArr, clazz, loanAppObject, count, fieldName);
		}*/
	}

	private void executeJsonOnSingleField(String[] fieldNameArr, Class clazz, Object loanAppObject, int count, String fieldNameKey, String fieldNameLabel) {
		Field field = null;
		Object object = null;
		do {
			try {
			    if(count>0 && fieldNameArr[count-1].equals("transientMaskingMap") && loanAppObject!=null && loanAppObject instanceof LinkedHashMap<?,?>){
			        loanAppObject = HibernateUtils.initializeAndUnproxy(loanAppObject);  
			        addTransientMaskingMap((LinkedHashMap<String, Object>)loanAppObject);
			    }else{
                loanAppObject = HibernateUtils.initializeAndUnproxy(loanAppObject);
				if(clazz.getName() == "com.nucleus.core.party.Party"){
					String methodName = "getPartyRole";
					Method method = loanAppObject.getClass().getDeclaredMethod(methodName);
					Integer result = (Integer) method.invoke(loanAppObject);
					if(result != 0) {
						continue;
					}
				}
				field = clazz.getDeclaredField(fieldNameArr[count]);
				field.setAccessible(Boolean.TRUE);
				object = field.get(loanAppObject);
			    }
				break;
			} catch (IllegalAccessException | NoSuchFieldException | NullPointerException e) {
				BaseLoggers.flowLogger.error(String.format("Field Not Present %s", fieldNameArr[count]));
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		} while((clazz = clazz.getSuperclass()) != null);

		if(field != null) {
			if(count == fieldNameArr.length-1){
				if(object instanceof Collection)
					evaluateForCollection((Collection)object, fieldNameArr[count], fieldNameKey,fieldNameLabel);
				else {
					evaluate(object, fieldNameLabel);
				}
			}

		}
		if (object instanceof Collection && count != fieldNameArr.length-1) {
			int tempCount = ++count;
			for (Object elem : (Collection) object) {
				executeJsonOnSingleField(fieldNameArr, elem.getClass(), elem, tempCount, fieldNameKey,fieldNameLabel);
			}
		}
		if (count < fieldNameArr.length - 1 && object != null) {
			executeJsonOnSingleField(fieldNameArr, field.getType(), object,++count, fieldNameKey,fieldNameLabel);
		}
	}
	private void splitFieldName(String fieldName, Object loanAppObjet) {
		String[] fieldArr = fieldName.split("\\.",-1);

	}

	private void evaluateForCollection(Collection obj, String fieldName, String fieldNameKey, String fieldNameLabel) {
		obj.forEach(elem -> evaluate(elem, fieldNameLabel));
	}

	private void evaluate(Object elem, String fieldNameLabel) {
        dataMap.put(fieldNameLabel, elem);
	}
	
	/**
	 * 
	 * TODO -> prakhar.varshney Add comment to method
	 * @param transientMaskingMap
	 * Generically keys of field on jsp should be same as entityFieldName , for Instances like name and fullName 
	 * extraCondition to be applied
	 */
	private void addTransientMaskingMap(LinkedHashMap<String,Object> transientMaskingMap) {
	  if(!transientMaskingMap.isEmpty()){
	      if(transientMaskingMap.get("fullName")!=null){
	          maskedDataMap.put("name", transientMaskingMap.get("fullName"));
	      }
	      maskedDataMap.putAll(transientMaskingMap);
	  }
    }
}
