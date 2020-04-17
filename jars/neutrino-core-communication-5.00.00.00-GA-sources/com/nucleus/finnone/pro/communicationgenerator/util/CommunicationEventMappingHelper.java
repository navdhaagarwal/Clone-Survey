package com.nucleus.finnone.pro.communicationgenerator.util;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.ui.ModelMap;

import com.nucleus.core.event.EventCode;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventTemplateMapping;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.standard.context.NeutrinoExecutionContextHolder;

@Named("communicationEventMappingHelper")
public class CommunicationEventMappingHelper {

	public static final String[] SQL_SELECT_QUERY_KEYWORDS = { "SELECT", "FROM", "WHERE", "AND", "ASC", "DESC", "NOT",
			"=", "!=", "<>", "~=", ">", "<", ">=", "<=" };
	public static final String[] SQL_SELECT_QUERY_KEYWORDS_MASK = { "&A9Z@", "&B8Z$", "&C7Z@", "&D6Z$", "&E5Z@",
			"&F4Z$", "&G3Z@", "&H2Z$", "&I1Z@", "&J9Z$", "&K8Z@", "&L7Z$", "&M6Z@", "&N5Z$" };

	@Inject
	@Named("neutrinoExecutionContextHolder")
	private NeutrinoExecutionContextHolder neutrinoExecutionContextHolder;

	public static void prepareUIDisplayData(ModelMap modelMap) {
		modelMap.put(CommunicationEventMappingConstants.MASTER_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);
		modelMap.put(CommunicationEventMappingConstants.PARENT_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);
		modelMap.put(CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR, new CommunicationEventMappingHeader());
		modelMap.put(CommunicationEventMappingConstants.IS_EMAIL, Boolean.FALSE);
		modelMap.put("SQL_SELECT_QUERY_KEYWORDS",
				StringUtils.join(CommunicationEventMappingHelper.SQL_SELECT_QUERY_KEYWORDS, ","));
		modelMap.put("SQL_SELECT_QUERY_KEYWORDS_MASK",
				StringUtils.join(CommunicationEventMappingHelper.SQL_SELECT_QUERY_KEYWORDS_MASK, ","));

	}

	public static List<Map<String, String>> getCommunicationCodes(List<CommunicationName> communicationCodes,
			String searchValue) {
		List<Map<String, String>> communicationCodesList = new ArrayList<>();
		Iterator<CommunicationName> itr = communicationCodes.iterator();
		while (itr.hasNext()) {
			CommunicationName communicationCode = itr.next();
			createAutoCompleteMap(searchValue, communicationCode.getCommunicationName(),
					communicationCode.getCommunicationCode(), communicationCode.getId(), communicationCodesList);
		}
		return communicationCodesList;
	}

	private static void createAutoCompleteMap(String searchValue, String name, String code, Long id,
			List<Map<String, String>> communicationCodesList) {
		Map<String, String> map = new LinkedHashMap<>();
		if (name.toLowerCase().contains(searchValue.toLowerCase())
				|| code.toLowerCase().contains(searchValue.toLowerCase()) || searchValue.matches("[%]+")) {

			map.put("name", name);
			map.put("code", code);
			map.put("id", id.toString());
			communicationCodesList.add(map);
		}
	}

	public static List<Map<String, String>> getEventCodes(List<EventCode> eventCodes, String searchValue) {
		List<Map<String, String>> eventCodeList = new ArrayList<>();
		Iterator<EventCode> itr = eventCodes.iterator();
		while (itr.hasNext()) {
			EventCode eventCode = itr.next();
			createAutoCompleteMap(searchValue, eventCode.getName(), eventCode.getCode(), eventCode.getId(),
					eventCodeList);
		}
		return eventCodeList;
	}

	public static void getListOfData(ModelMap map, int page, List<Map<String, String>> tmpList, String idCurr,
			String label, String contentId) {
		List<Map<String, String>> list = tmpList;
		String iLabel = label;
		if (hasElements(list)) {
			map.put("size", list.size());
			map.put("page", page);
			if (list.size() / 3 == page && list.size() % 3 == 1)
				list = list.subList(3 * page, 3 * page + 1);
			else if (list.size() / 3 == page && list.size() % 3 == 2)
				list = list.subList(3 * page, 3 * page + 2);
			else
				list = list.subList(3 * page, 3 * page + 3);
		}
		if (iLabel != null && iLabel.contains(".")) {
			iLabel = iLabel.replace(".", "");
		}
		map.put("data", list);
		map.put("idCurr", idCurr);
		map.put("i_label", iLabel);
		map.put("content_id", contentId);
	}

	public static List<Integer> getApprovalStatusList() {
		List<Integer> approvalStatusList = new ArrayList<>();
		approvalStatusList.add(ApprovalStatus.APPROVED);
		approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
		approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
		approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
		return approvalStatusList;
	}

	public static List<Map<String, String>> prepareCommunicationTemplates(List<CommunicationTemplate> templates,
			String searchValue) {
		List<java.util.Map<String, String>> communicationTemplates = new ArrayList<>();
		Iterator<CommunicationTemplate> itr = templates.iterator();
		while (itr.hasNext()) {
			CommunicationTemplate template = itr.next();

			java.util.Map<String, String> map = new LinkedHashMap<>();

			if (template.getCommunicationTemplateName().toLowerCase().contains(searchValue.toLowerCase())
					|| template.getCommunicationTemplateCode().toLowerCase().contains(searchValue.toLowerCase())
					|| searchValue.matches("[%]+")) {

				map.put("name", template.getCommunicationTemplateName());
				map.put("code", template.getCommunicationTemplateCode());
				map.put("id", template.getId().toString());
				communicationTemplates.add(map);
			}

		}
		communicationTemplates.sort((o1, o2) -> {
			if(o1.get("code").toString().equalsIgnoreCase(o2.get("code").toString())){
				return o1.get("name").toString().compareToIgnoreCase(o2.get("name").toString());
			}else{
				return o1.get("code").toString().compareToIgnoreCase(o2.get("code").toString());
			}
		});
		return communicationTemplates;
	}

	public static void setViewAndEditCommonData(ModelMap modelMap,
			CommunicationEventMappingHeader communicationEventMapping, boolean viewable, boolean edit) {
		Hibernate.initialize(communicationEventMapping.getEventCode());
		Hibernate.initialize(communicationEventMapping.getSourceProduct());
		Hibernate.initialize(communicationEventMapping.getCommunicationCategory());
		Hibernate.initialize(communicationEventMapping.getCommunicationEventMappingDetails());

		int accordionSize = communicationEventMapping.getCommunicationEventMappingDetails().size();
		modelMap.put(CommunicationEventMappingConstants.COMM_TEMPLATE_MAP_ACCORD_INDEX, accordionSize - 1);
		modelMap.put(CommunicationEventMappingConstants.COMM_TEMPLATE_MAP_ACCORD_SIZE, accordionSize);

		for (CommunicationEventMappingDetail evtMapDtl : communicationEventMapping
				.getCommunicationEventMappingDetails()) {
			Hibernate.initialize(evtMapDtl.getCommunicationEventTemplateMappings());
			setEventTemplateMapData(evtMapDtl);
		}

		modelMap.put(CommunicationEventMappingConstants.SOURCE_PRODUCT_ID,
				communicationEventMapping.getSourceProductId());

		modelMap.put(CommunicationEventMappingConstants.MASTER_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);
		modelMap.put(CommunicationEventMappingConstants.PARENT_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);

		modelMap.put(CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR, communicationEventMapping);
		modelMap.put(CommunicationEventMappingConstants.APPROVAL_STATUS, communicationEventMapping.getApprovalStatus());
		modelMap.put(CommunicationEventMappingConstants.TYPE_SIZE, 0);
		modelMap.put(CommunicationEventMappingConstants.VIEWABLE, viewable);
		modelMap.put(CommunicationEventMappingConstants.EDIT, edit);

		modelMap.put("SQL_SELECT_QUERY_KEYWORDS",
				StringUtils.join(CommunicationEventMappingHelper.SQL_SELECT_QUERY_KEYWORDS, ","));
		modelMap.put("SQL_SELECT_QUERY_KEYWORDS_MASK",
				StringUtils.join(CommunicationEventMappingHelper.SQL_SELECT_QUERY_KEYWORDS_MASK, ","));

	}

	private static void setEventTemplateMapData(CommunicationEventMappingDetail evtMapDtl) {
		evtMapDtl.setEmailFlag(
				evtMapDtl.getCommunicationName().getCommunicationType().getCode().equals(CommunicationType.EMAIL) ||
				evtMapDtl.getCommunicationName().getCommunicationType().getCode().equals(CommunicationType.WHATSAPP));
		for (CommunicationEventTemplateMapping evtTemplateMap : evtMapDtl.getCommunicationEventTemplateMappings()) {
			Hibernate.initialize(evtTemplateMap.getRule());
			if (notNull(evtTemplateMap.getAttachmentTemplateIds())) {
				String[] arrayAttachmentIds = evtTemplateMap.getAttachmentTemplateIds().split(",");
				List<Long> attachmentIds = new ArrayList<>(arrayAttachmentIds.length);
				for (int i = 0; i < arrayAttachmentIds.length; i++) {
					attachmentIds.add(Long.parseLong(arrayAttachmentIds[i]));
				}
				evtTemplateMap.setAttachmentIds(attachmentIds);
			}
		}
	}

	public static void addActions(CommunicationEventMappingHeader communicationEventMapping, ModelMap map) {
		@SuppressWarnings("unchecked")
		List<String> actions = (List<String>) communicationEventMapping.getViewProperties().get("actions");
		if (actions != null) {
			for (String act : actions) {
				String actionString = "act" + act;
				map.put(actionString.replaceAll(" ", ""), Boolean.FALSE);
			}
		}
	}

	public static void prepareFieldsToBeDisabled(CommunicationEventMappingHeader communicationEventMapping,
			ModelMap modelMap) {
		if (communicationEventMapping.getApprovalStatus() == ApprovalStatus.APPROVED
				|| communicationEventMapping.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
				|| communicationEventMapping.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
			modelMap.put(CommunicationEventMappingConstants.SOURCE_PRODUCT_DISABLED, Boolean.TRUE);
			modelMap.put(CommunicationEventMappingConstants.EVENT_CODE_DISABLED, Boolean.TRUE);
		}
	}

	public static List<Message> prepareMessageList(String errorMessage) {
		Message msg = new Message();
		List<Message> messageList = new ArrayList<>();
		msg.setIsParent(Boolean.TRUE);
		msg.setI18nCode(errorMessage);
		messageList.add(msg);
		return messageList;
	}

	public void prepareCommEventMappingDetails(CommunicationEventMappingHeader communicationEventMapping) {
		if (CollectionUtils.isNotEmpty(communicationEventMapping.getCommunicationEventMappingDetails())) {
			for (CommunicationEventMappingDetail eventMapping : communicationEventMapping
					.getCommunicationEventMappingDetails()) {
				if (CollectionUtils.isNotEmpty(eventMapping.getCommunicationEventTemplateMappings())) {
					for (CommunicationEventTemplateMapping eventTemplateMapping : eventMapping
							.getCommunicationEventTemplateMappings()) {
						eventTemplateMapping.setAttachmentTemplateIds(
								setAttachmentTemplateIds(eventTemplateMapping.getAttachmentIds()));
					}
				}
			}
		}
	}

	private String setAttachmentTemplateIds(List<Long> attachmentId) {
		if (CollectionUtils.isNotEmpty(attachmentId)) {
			int length = attachmentId.size();
			if (notNull(attachmentId) && length > 0) {
				String[] attachmentIds = new String[length];
				for (int i = 0; i < length; i++) {
					attachmentIds[i] = attachmentId.get(i).toString();
				}
				return String.join(",", attachmentIds);
			}
		}
		return null;
	}

	public static String decodeSQLCriteria(String queryStringToBeDecoded) {
		String queryString = queryStringToBeDecoded;
		if (StringUtils.isNotEmpty(queryStringToBeDecoded)) {
			String encodedQueryStringUpperCase = queryStringToBeDecoded.toUpperCase();
			if (ValidatorUtils.notNull(queryString)) {
				for (int i = 0; i < SQL_SELECT_QUERY_KEYWORDS_MASK.length; ++i) {
					int keywordLength = SQL_SELECT_QUERY_KEYWORDS_MASK[i].length();
					while (encodedQueryStringUpperCase.indexOf(SQL_SELECT_QUERY_KEYWORDS_MASK[i]) != -1) {
						int indexOfQueryKeywordMask = encodedQueryStringUpperCase
								.indexOf(SQL_SELECT_QUERY_KEYWORDS_MASK[i]);
						encodedQueryStringUpperCase = encodedQueryStringUpperCase
								.replace(SQL_SELECT_QUERY_KEYWORDS_MASK[i], SQL_SELECT_QUERY_KEYWORDS[i]);
						queryString = queryString.replace(
								queryString.substring(indexOfQueryKeywordMask, indexOfQueryKeywordMask + keywordLength),
								SQL_SELECT_QUERY_KEYWORDS[i]);

					}
				}
			}
		}
		return queryString;
	}

	public static String encodeSQLCriteria(String queryString) {
		String queryStringEncoded = queryString;
		if (ValidatorUtils.notNull(queryString)) {
			for (int i = 0; i < SQL_SELECT_QUERY_KEYWORDS.length; ++i) {
				if (queryStringEncoded.contains(SQL_SELECT_QUERY_KEYWORDS[i])) {
					int specCharCount = getSpecCharCount(queryStringEncoded, SQL_SELECT_QUERY_KEYWORDS[i]);
					for (int j = 0; j < specCharCount; ++j) {
						queryStringEncoded = queryStringEncoded.toUpperCase().replace(SQL_SELECT_QUERY_KEYWORDS[i],
								SQL_SELECT_QUERY_KEYWORDS_MASK[i]);
					}
				}
			}
		}
		return queryStringEncoded;
	}

	private static int getSpecCharCount(String requestParamValue, String specialCharacterMask) {
		int lastIndex = 0;
		int count = 0;
		while (lastIndex != -1) {
			lastIndex = requestParamValue.indexOf(specialCharacterMask, lastIndex);
			if (lastIndex != -1) {
				count++;
				lastIndex += specialCharacterMask.length();
			}
		}
		return count;
	}
}
