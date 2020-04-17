/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 */
package com.nucleus.web.label;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nucleus.html.util.HtmlUtils;
import org.xml.sax.SAXException;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.locale.LanguageInfoReader;
import com.nucleus.core.locale.LanguageInfoVO;
import com.nucleus.core.messageSource.MessageResource;
import com.nucleus.core.messageSource.MessageResourceGridServiceImpl;
import com.nucleus.core.messageSource.MessageResourceService;
import com.nucleus.core.messageSource.MessageResourceValue;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.ActionConfiguration;
import com.nucleus.makerchecker.ColumnConfiguration;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.security.oauth.constants.TrustedSourceRegistrationConstant;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.datatable.DataTableJsonHepler;

import flexjson.JSONSerializer;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Controller
@Transactional
@RequestMapping(value = "/messageResource")
public class MessageResourceController extends BaseController {

	@Inject
	@Named("masterConfigurationRegistry")
	private MasterConfigurationRegistry masterConfigurationRegistry;

	@Inject
	@Named("messageResourceService")
	private MessageResourceService messageResourceService;

	@Inject
	@Named("messageResourceGridService")
	private MessageResourceGridServiceImpl gridServiceImpl;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Inject
	@Named("languageInfoReader")
	private LanguageInfoReader languageInfoReader;

	private final static String XML_FILE_KEY_NAME = "MessageResource";
	private final static String MASTER_ENTITY 	= "MessageResource";
	private final static String MESSAGE_RESOURCE_STRING_KEY = "messageResource";

	/**
	 * Method call to load message resource into datatable
	 * @param map
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/checkDuplicateMessageKey/{messageKey}/{uuid}", method = RequestMethod.POST)
	public String checkDuplicateMessageKey(
			 @PathVariable("messageKey") String messageKey,
			@PathVariable("uuid") String uuid) {
		
		Long count = messageResourceService.getCountOfMessageResourceByKey(messageKey, uuid);
		if (count >1) {
			return "true";			
			
		}
		
		return "false";
	}
	@PreAuthorize("hasAuthority('MAKER_MESSAGES') or hasAuthority('CHECKER_MESSAGES') or hasAuthority('VIEW_MESSAGES')")
	@RequestMapping(value = "/loanAllLabels")
	public String loanGrid(ModelMap map) {
		List<ColumnConfiguration> columnConfiguration = masterConfigurationRegistry
				.getColumnConfigurationList(XML_FILE_KEY_NAME);
		String recordURL = masterConfigurationRegistry.getRecordURL(XML_FILE_KEY_NAME);
		List<ActionConfiguration> actionConfiguration = masterConfigurationRegistry
				.getActionConfigurationList(XML_FILE_KEY_NAME);
		String jspName = masterConfigurationRegistry.getjspName(XML_FILE_KEY_NAME);

		String key = masterConfigurationRegistry.getKey(XML_FILE_KEY_NAME);
		Map<String, Object> masterMap = new HashMap<>();
		masterMap.put("dataTableRecords", columnConfiguration);
		masterMap.put("actionConfiguration", actionConfiguration);
		masterMap.put("recordURL", recordURL);
		map.put("masterId", MASTER_ENTITY);
		map.put("bFilter", true);
		map.put("bInfo", true);
		map.put("bSort", true);
		map.put("bLengthChange", true);
		map.put("bJQueryUI", false);
		map.put("serverSide", true);
		map.put("bPaginate", true);
		map.put("authority", MASTER_ENTITY.toUpperCase());
		map.put("Key", key);
		map.putAll(masterMap);
		return jspName;

	}

	/**
	 * Method called when searcing is done on message resource grid
	 * @param key
	 * @param iDisplayStart
	 * @param iDisplayLength
	 * @param sSortDir_0
	 * @param iSortCol_0
	 * @param sSearch
	 * @param sEcho
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST }, value = "/{Key}/loadPaginatedInformation")
    public @ResponseBody
    <T> String getEntityUpdateInfoList(@PathVariable("Key") String key,
			@RequestParam(value = "iDisplayStart", required = false) Integer iDisplayStart,
			@RequestParam(value = "iDisplayLength", required = false) Integer iDisplayLength,
			@RequestParam(value = "sSortDir_0", required = false) String sSortDir_0,
			@RequestParam(value = "iSortCol_0", required = false) Integer iSortCol_0,
			@RequestParam(value = "sSearch", required = false) String sSearch,
            @RequestParam(value = "sEcho", required = false) Integer sEcho, HttpServletRequest request) throws IOException,
            ParserConfigurationException, SAXException, ClassNotFoundException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

		List<ColumnConfiguration> columnConfigurationList = masterConfigurationRegistry.getColumnConfigurationList(key);
		List<MessageResource> entities = new ArrayList<>();
		UserInfo currentUser = getUserDetails();
		DataTableJsonHepler jsonHelper = new DataTableJsonHepler();
		Map<String, Object> searchMap = new HashMap<String, Object>();
		Integer recordCount = 0;
		Integer totalRecordCount = 0;
		String sortDirection = null;
		String sortColName = null;
 		if (iSortCol_0 != null && sSortDir_0 != null) {
			ColumnConfiguration columnConfigurationIndex = columnConfigurationList.get(iSortCol_0 - 1);
	        sortColName = columnConfigurationIndex.getDataField();
	        sortDirection = sSortDir_0.toLowerCase();
		}
		if (sSearch != null && !sSearch.isEmpty()) {
			Pattern regex = Pattern.compile("[$&%+,:;=?@#|]");
			Matcher matcher = regex.matcher(sSearch);
			Long count = messageResourceService.getCountOfAllMessage();
			if (count != null) {
				totalRecordCount = Integer.parseInt(count.toString());
			}
			if (matcher.find()) {
				if (sEcho != null) {
					jsonHelper.setsEcho(sEcho);
					jsonHelper.setiTotalDisplayRecords(0);
					jsonHelper.setiTotalRecords(totalRecordCount);
				}
			} else {

				for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
					if (columnConfiguration.getSearchable()) {
						searchMap.put(columnConfiguration.getDataField(), sSearch);
					}

				}
                Map<String, Object> searchRecordMap = gridServiceImpl.findEntity(searchMap, iDisplayStart, iDisplayLength, sortColName, sortDirection);
				entities = (List<MessageResource>) searchRecordMap.get("searchRecordList");
				recordCount = (Integer) searchRecordMap.get("searchRecordListSize");
				totalRecordCount = (Integer) searchRecordMap.get("totalRecordListSize");
				if (sEcho != null) {
					jsonHelper.setsEcho(sEcho);
					jsonHelper.setiTotalDisplayRecords(recordCount);
					jsonHelper.setiTotalRecords(totalRecordCount);
				}
			}
		} else {
			Map<String, Object> result = gridServiceImpl.loadPaginatedData(null, currentUser.getUserEntityId().getUri(),
					null, iDisplayStart, iDisplayLength, sortColName, sortDirection);
			entities = (List<MessageResource>) result.get("entity");
			if (result.get("count") == null) {
				jsonHelper.setiTotalDisplayRecords(0);
			} else {
				jsonHelper.setiTotalDisplayRecords(Integer.parseInt(((Long) result.get("count")).toString()));
			}
		}

		List<List<Object>> columnDataList = new LinkedList<>();
		ArrayList actions = new ArrayList();
		actions.add("Edit");
		for (MessageResource entity : entities) {
			entity.addProperty("actions", actions);
			List<Object> rowDataList = new LinkedList<>();
			rowDataList.add(null);
			Object columnValue = null;
			for (ColumnConfiguration columnConfiguration : columnConfigurationList) {
				try {
					columnValue = PropertyUtils.getNestedProperty(entity, columnConfiguration.getDataField());
					if (columnValue instanceof String) {
						columnValue = HtmlUtils.htmlEscape((String) columnValue);
					}

				} catch (NestedNullException e) {
					columnValue = null;
					BaseLoggers.exceptionLogger
							.error("Exception occured while accessing nested property for column configuration '"
									+ columnConfiguration.getTitleKey() + "' :" + e.getMessage(), e);
				}

				rowDataList.add(columnValue);
			}
			columnDataList.add(rowDataList);
		}

		jsonHelper.setAaData(columnDataList);
		jsonHelper.setsEcho(sEcho);
		JSONSerializer iSerializer = new JSONSerializer();
		return iSerializer.exclude("*.class").deepSerialize(jsonHelper);
	}

	/**
	 * Method called when message resource is open in view mode
	 * @param id
	 * @param map
	 * @return
	 */
	@PreAuthorize("hasAuthority('MAKER_MESSAGES') or hasAuthority('CHECKER_MESSAGES') or hasAuthority('VIEW_MESSAGES')")
	@RequestMapping(value = "/view/{id}")
	public String viewLabel(@PathVariable("id") Long id, ModelMap map) {
		if (id != null) {
			MessageResource messageResource = messageResourceService.getMessageResourceById(id);
			map.put(MESSAGE_RESOURCE_STRING_KEY, messageResource);
            map.put("viewable", true);
		}
		return MESSAGE_RESOURCE_STRING_KEY;
	}

	/**
	 * Method called when message resource is open in edit mode
	 * @param id
	 * @param map
	 * @return
	 */
	@PreAuthorize("hasAuthority('MAKER_MESSAGES')")
	@RequestMapping(value = "/edit/{id}")
	public String editLabel(@PathVariable("id") Long id, ModelMap map) {
		if (id != null) {
			MessageResource messageResource = messageResourceService.getMessageResourceById(id);
			map.put(MESSAGE_RESOURCE_STRING_KEY, messageResource);
			map.put("edit", true);
		}
		return MESSAGE_RESOURCE_STRING_KEY;
	}

	/**
	 * Method called when message resource is open in new mode
	 * @param id
	 * @param map
	 * @return
	 */
	@PreAuthorize("hasAuthority('MAKER_MESSAGES')")
	@RequestMapping(value = "/newLabel")
	public String newLabel(ModelMap map) {
		MessageResource messageResource = new MessageResource();
		Map<String, LanguageInfoVO> localeLanguageInfoMap = languageInfoReader.getAvailableLocaleLanguageInfoMap();
		List<String> localeKeyList = new ArrayList<>();
		localeKeyList.add("default_message");
		if (!localeLanguageInfoMap.isEmpty()) {
			localeKeyList.addAll(localeLanguageInfoMap.keySet());
		}
		List<MessageResourceValue> messageResourceValues = new ArrayList<>();
		for (String string : localeKeyList) {
			MessageResourceValue messageResourceValue = new MessageResourceValue();
			messageResourceValue.setLocaleKey(string);
			messageResourceValues.add(messageResourceValue);
		}
		messageResource.setMessageResourceValues(messageResourceValues);
		map.put(MESSAGE_RESOURCE_STRING_KEY, messageResource);
		return MESSAGE_RESOURCE_STRING_KEY;

	}

	/**
	 * Method called when message resource is save
	 * @param id
	 * @param map
	 * @return
	 */
	
	
	
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveLabel( @Validated  MessageResource messageResource,BindingResult result, ModelMap map) {
	
		String uuid=messageResource.getEntityLifeCycleData().getUuid();
		if(uuid==null)
			uuid="null";
		Long count = messageResourceService.getCountOfMessageResourceByKey(messageResource.getMessageKey(), uuid);
		if (count > 1) {
			result.rejectValue("messageKey","label.message.already.exist");
			map.put("error", "label.message.already.exist");


			
			return MESSAGE_RESOURCE_STRING_KEY;
		}
		
		if (messageResource.getEntityLifeCycleData().getUuid() == null) {
			messageResource.getEntityLifeCycleData().setUuid(UUID.randomUUID().toString());
		}
		// we need to get below logged in user from session
		User user = getUserDetails().getUserReference();
		messageResourceService.saveMessageResource(messageResource, user);
		// updating cache map
		messageResourceService.updateMessageResourceIntoCache(messageResource);
		return "redirect:/app/messageResource/loanAllLabels";
	}

	@RequestMapping(value = "/clearEntireCache", method = RequestMethod.POST)
	public String clearEntireCache() {
		configurationService.clearEntireCache();
		return "success";
	}


}