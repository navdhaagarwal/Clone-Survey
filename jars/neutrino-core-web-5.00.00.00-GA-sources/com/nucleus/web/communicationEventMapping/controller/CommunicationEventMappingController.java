package com.nucleus.web.communicationEventMapping.controller;

import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.MAKER_COMMUNICATIONEVENTMAPPINGHEADER;
import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.VIEW_COMMUNICATIONEVENTMAPPINGHEADER;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.event.EventCode;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationCategory;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationCriteriaType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.service.CommunicationEventMappingService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationEventLoggerService;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingHelper;
import com.nucleus.lms.web.common.MessageOutput;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.user.User;
import com.nucleus.web.common.controller.BaseController;

@Transactional(propagation = Propagation.REQUIRED)
@Controller
@RequestMapping(value = "/CommunicationEventMappingHeader")
public class CommunicationEventMappingController extends BaseController {

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("communicationEventMappingService")
	private CommunicationEventMappingService communicationEventMappingService;

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;

	  
	  @Inject
	  @Named("communicationEventLoggerService")
	  private ICommunicationEventLoggerService communicationEventLoggerService;
	  
	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/create")
	public String createCommunicationEventmapping(ModelMap modelMap) {
		modelMap.put(CommunicationEventMappingConstants.SOURCE_PRODUCTS,
				communicationEventMappingService.getSourceProductList());
		modelMap.put(CommunicationEventMappingConstants.COMM_CODE_DISABLED, Boolean.TRUE);
		modelMap.put(CommunicationEventMappingConstants.TEMPLATE_GRID_CURRENT_INDEX, 0);
		modelMap.put(CommunicationEventMappingConstants.TEMPLATE_GRID_SIZE, 1);
		modelMap.put(CommunicationEventMappingConstants.COMM_TEMPLATE_MAP_ACCORD_INDEX, 0);
		modelMap.put(CommunicationEventMappingConstants.COMM_TEMPLATE_MAP_ACCORD_SIZE, 1);
		modelMap.put("priority", 1);
		modelMap.put(CommunicationEventMappingConstants.ATTACHMENT_TEMPLATES,
				communicationEventMappingService.getCommunicationTemplateByCommunicationTypeId(
						genericParameterService.findByCode(CommunicationType.LETTER, CommunicationType.class).getId()));

		CommunicationEventMappingHelper.prepareUIDisplayData(modelMap);
		return CommunicationEventMappingConstants.COMM_EVENT_MAPPING;
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/populateCommunicationCodeAutoComplete/{sourceProductId}")
	public String populateCommunicationCodeAutoComplete(@PathVariable(value = "sourceProductId") Long sourceProductId,
			ModelMap map, @RequestParam(value = "value") String searchValue,
			@RequestParam(value = "i_label") String iLabel, @RequestParam(value = "idCurr") String idCurr,
			@RequestParam(value = "content_id") String contentId, @RequestParam(value = "page") int page) {

		List<CommunicationName> communicationCodes = communicationEventMappingService
				.getCommunicationCodesBySourceProductId(sourceProductId);
		List<Map<String, String>> list = CommunicationEventMappingHelper.getCommunicationCodes(communicationCodes,
				searchValue);
		CommunicationEventMappingHelper.getListOfData(map, page, list, idCurr, iLabel, contentId);
		return CommunicationEventMappingConstants.AUTOCOMPLETE;

	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/getCommunicationType")
	@ResponseBody
	public Map<String, Object> getCommunicationType(
			@RequestParam(value = "communicationCodeId") Long communicationCodeId) {
		Map<String, Object> map = new HashMap<>();
		CommunicationType communicationType = communicationEventMappingService
				.getCommunicationTypeByCommunicationCodeId(communicationCodeId);
		map.put(CommunicationEventMappingConstants.COMM_TYPE_NAME, communicationType.getName());
		map.put(CommunicationEventMappingConstants.IS_EMAIL,
				communicationType.getCode().equals(CommunicationType.EMAIL));
		return map;
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/populateCommunicationTemplate/{communicationCodeId}")
	public String populateCommunicationTemplate(@PathVariable(value = "communicationCodeId") Long communicationCodeId,
			ModelMap map, @RequestParam(value = "value") String searchValue,
			@RequestParam(value = "i_label") String iLabel, @RequestParam(value = "idCurr") String idCurr,
			@RequestParam(value = "content_id") String contentId, @RequestParam(value = "page") int page) {

		List<CommunicationTemplate> templates = communicationEventMappingService
				.getCommunicationTemplateByCommunicationCodeId(communicationCodeId);
		List<Map<String, String>> list = CommunicationEventMappingHelper.prepareCommunicationTemplates(templates,
				searchValue);
		CommunicationEventMappingHelper.getListOfData(map, page, list, idCurr, iLabel, contentId);
		return CommunicationEventMappingConstants.AUTOCOMPLETE;

	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/addRow")
	public String addTemplateMappingRecord(@RequestParam(value = "endSize") Long endSize,
			@RequestParam(value = "communicationCodeId") Long communicationCodeId,
			@RequestParam(value = "commTemplateMapIndex") Long commTemplateMapIndex, ModelMap modelMap) {
		modelMap.put(CommunicationEventMappingConstants.END_SIZE, endSize);
		modelMap.put(CommunicationEventMappingConstants.COMM_CODE_ID, communicationCodeId);
		modelMap.put(CommunicationEventMappingConstants.COMM_TEMPLATE_MAP_INDEX, commTemplateMapIndex);
		modelMap.put("priority", endSize + 1);
		modelMap.put(CommunicationEventMappingConstants.ATTACHMENT_TEMPLATES,
				communicationEventMappingService.getCommunicationTemplateByCommunicationTypeId(
						genericParameterService.findByCode(CommunicationType.LETTER, CommunicationType.class).getId()));

		CommunicationEventMappingHelper.prepareUIDisplayData(modelMap);
		return "communicationEventMapping/templateMappingRow";
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/addCommEventMappingRow")
	public String addCommEventMappingRow(@RequestParam(value = "endSize") Long endSize,
			@RequestParam(value = "sourceProductId") Long sourceProductId, ModelMap modelMap) {

		modelMap.put(CommunicationEventMappingConstants.SOURCE_PRODUCTS,
				communicationEventMappingService.getSourceProductList());
		modelMap.put(CommunicationEventMappingConstants.COMM_CODE_DISABLED, Boolean.FALSE);
		
		modelMap.put(CommunicationEventMappingConstants.TEMPLATE_GRID_CURRENT_INDEX, 0);
		modelMap.put(CommunicationEventMappingConstants.TEMPLATE_GRID_SIZE, 1);
		modelMap.put(CommunicationEventMappingConstants.COMM_TEMPLATE_MAP_ACCORD_NEXT_INDEX, endSize);

		modelMap.put(CommunicationEventMappingConstants.SOURCE_PRODUCT_ID, sourceProductId);
		modelMap.put("priority", 1);
		modelMap.put(CommunicationEventMappingConstants.ATTACHMENT_TEMPLATES,
				communicationEventMappingService.getCommunicationTemplateByCommunicationTypeId(
						genericParameterService.findByCode(CommunicationType.LETTER, CommunicationType.class).getId()));

		CommunicationEventMappingHelper.prepareUIDisplayData(modelMap);
		return "communicationEventMapping/commEventMappingRow";
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveCommunicationEventMapping(@Validated CommunicationEventMappingHeader communicationEventMapping,
			ModelMap modelMap, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {		
		communicationEventMapping.setEventCode(genericParameterService.findById(communicationEventMapping.getEventCodeId(), EventCode.class));
		communicationEventMappingService.saveCommunicationEventmapping(communicationEventMapping,
				getUserDetails().getUserReference());
		if (createAnotherMaster) {
			return createCommunicationEventmapping(modelMap);
		}
		modelMap.put(CommunicationEventMappingConstants.MASTER_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);
		modelMap.put(CommunicationEventMappingConstants.PARENT_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);
		return "redirect:/app/grid/CommunicationEventMappingHeader/CommunicationEventMappingHeader/loadColumnConfig";
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApprovalCommunicationEventMapping(
			@Validated CommunicationEventMappingHeader communicationEventMapping, ModelMap modelMap,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		User user = getUserDetails().getUserReference();
		communicationEventMapping.setEventCode(genericParameterService.findById(communicationEventMapping.getEventCodeId(), EventCode.class));
		BaseMasterEntity returnedEntity = communicationEventMappingService
				.saveCommunicationEventmapping(communicationEventMapping, user);
		makerCheckerService.startMakerCheckerFlow(returnedEntity.getEntityId(), user.getEntityId());
		if (createAnotherMaster) {
			return createCommunicationEventmapping(modelMap);
		}
		modelMap.put(CommunicationEventMappingConstants.MASTER_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);
		modelMap.put(CommunicationEventMappingConstants.PARENT_ID,
				CommunicationEventMappingConstants.COMM_EVENT_MAPPING_HDR);
		return "redirect:/app/grid/CommunicationEventMappingHeader/CommunicationEventMappingHeader/loadColumnConfig";
	}

	@PreAuthorize(VIEW_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewCommunicationEventMapping(@PathVariable("id") Long id, ModelMap modelMap) {
		CommunicationEventMappingHeader communicationEventMapping = this.baseMasterService
				.getMasterEntityWithActionsById(CommunicationEventMappingHeader.class, id,
						getUserDetails().getUserEntityId().getUri());
		setViewAndEditCommonData(modelMap, communicationEventMapping, true, false);

		if (communicationEventMapping.getViewProperties() != null) {
			CommunicationEventMappingHelper.addActions(communicationEventMapping, modelMap);
		}
		return CommunicationEventMappingConstants.COMM_EVENT_MAPPING;
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/edit/{id}")
	public String editCommunicationEventMapping(@PathVariable("id") Long id, ModelMap modelMap) {
		CommunicationEventMappingHeader communicationEventMapping = this.baseMasterService
				.getMasterEntityWithActionsById(CommunicationEventMappingHeader.class, id,
						getUserDetails().getUserEntityId().getUri());
		setViewAndEditCommonData(modelMap, communicationEventMapping, false, true);

		CommunicationEventMappingHelper.prepareFieldsToBeDisabled(communicationEventMapping, modelMap);
		CommunicationEventMappingHelper.addActions(communicationEventMapping, modelMap);
		return CommunicationEventMappingConstants.COMM_EVENT_MAPPING;
	}

	private void setViewAndEditCommonData(ModelMap modelMap, CommunicationEventMappingHeader communicationEventMapping,
			boolean view, boolean edit) {
		modelMap.put(CommunicationEventMappingConstants.SOURCE_PRODUCTS,
				communicationEventMappingService.getSourceProductList());
		modelMap.put(CommunicationEventMappingConstants.ATTACHMENT_TEMPLATES,
				communicationEventMappingService.getCommunicationTemplateByCommunicationTypeId(
						genericParameterService.findByCode(CommunicationType.LETTER, CommunicationType.class).getId()));
		modelMap.put(CommunicationEventMappingConstants.SELECT_CLAUSE, CommunicationEventMappingConstants.CRITERIA_BASE_SELECT_CLAUSE);
		String communicationCategory = communicationEventMapping.getCommunicationCategory().getCode();
		Long sourceProductId = communicationEventMapping.getSourceProductId();
		Long eventCodeId = communicationEventMapping.getEventCodeId();
		setQueryOrRuleCriteriaTypeFlag(modelMap, communicationCategory, sourceProductId, eventCodeId);
		CommunicationEventMappingHelper.setViewAndEditCommonData(modelMap, communicationEventMapping, view, edit);
	}

	private void setQueryOrRuleCriteriaTypeFlag(Map<String, Object> modelMap, String communicationCategory, Long sourceProductId,
			Long eventCodeId) {
		boolean queryCriteriaFlag = false;
		boolean ruleCriteriaFlag = false;
		String criteriaType = null;
		if (CommunicationCategory.COMM_CATEGORY_IMMEDIATE.equals(communicationCategory)) {
			ruleCriteriaFlag = true;
		} else {
			criteriaType = communicationEventMappingService.getCommunicationCriteriaType(
					sourceProductId, eventCodeId);
			if (CommunicationCriteriaType.BOTH_CRITERIA.equals(criteriaType)) {
				queryCriteriaFlag = true;
				ruleCriteriaFlag = true;
			} else {
				ruleCriteriaFlag = CommunicationCriteriaType.RULE_CRITERIA.equals(criteriaType);
				queryCriteriaFlag = !ruleCriteriaFlag;
			}
		}
		modelMap.put("criteriaType", criteriaType);
		modelMap.put("queryCriteriaFlag", queryCriteriaFlag);
		modelMap.put("ruleCriteriaFlag", ruleCriteriaFlag);
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/validateIfCommunicationEventMappingExists", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, Object> validateIfCommunicationEventMappingExists(
			@Validated CommunicationEventMappingHeader communicationEventMapping) {

		Map<String, Object> modelMap = new HashMap<>();
		if (communicationEventMappingService.checkDuplicateCommunicationEventMapping(communicationEventMapping.getId(),
				communicationEventMapping.getSourceProductId(), communicationEventMapping.getEventCodeId())) {
			List<Message> errorMessages = CommunicationEventMappingHelper
					.prepareMessageList(CommunicationEventMappingConstants.COMMUNICATION_EVENT_MAPPING_ALREADY_EXIST);
			modelMap.put(CommunicationEventMappingConstants.ERROR, getWebMessageList(errorMessages));
		} else {
			modelMap.put("criteriaType", communicationEventMappingService.getCommunicationCriteriaType(
					communicationEventMapping.getSourceProductId(), communicationEventMapping.getEventCodeId()));
		}
		return modelMap;
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/selectQueryOrRuleCriteriaType", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> selectQueryOrRuleCriteriaType(@RequestParam("eventCodeId") Long eventCodeId,
			@RequestParam("sourceProductId") Long sourceProductId,
			@RequestParam("communicationCategory") String communicationCategory) {
		Map<String, Object> map = new HashMap<>();
		if (CommunicationCategory.COMM_CATEGORY_IMMEDIATE.equals(communicationCategory)
				|| CommunicationCategory.COMM_CATEGORY_SCHEDULED.equals(communicationCategory)) {
			setQueryOrRuleCriteriaTypeFlag(map, communicationCategory, sourceProductId, eventCodeId);
		}
		return map;
	}
	
	private List<MessageOutput> getWebMessageList(List<Message> errorMessages) {
		List<MessageOutput> webMessageList = new ArrayList<>();
		MessageOutput messageOutput;
		try {
			for (Message message : errorMessages) {
				String i18Value = messageSource.getMessage(message.getI18nCode(), message.getMessageArguments(),
						message.getI18nCode(), getUserLocale());
				messageOutput = new MessageOutput(message, i18Value);
				webMessageList.add(messageOutput);
			}
		} catch (Exception exception) {
			BaseLoggers.exceptionLogger.error(exception.getMessage(), exception);
		}
		return webMessageList;
	}

	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/populateEventCodeAutoComplete/{sourceProductId}")
	public String populateEventCodeAutoComplete(@PathVariable(value = "sourceProductId") Long sourceProductId,
			ModelMap map, @RequestParam(value = "value") String searchValue,
			@RequestParam(value = "i_label") String iLabel, @RequestParam(value = "idCurr") String idCurr,
			@RequestParam(value = "content_id") String contentId, @RequestParam(value = "page") int page) {

		List<EventCode> eventCodes = communicationEventMappingService.getEventCodesBySourceProductId(sourceProductId);
		List<Map<String, String>> list = CommunicationEventMappingHelper.getEventCodes(eventCodes, searchValue);
		list.sort((o1, o2) -> {
			if(o1.get("code").equalsIgnoreCase(o2.get("code"))) {
				return o1.get("name").compareToIgnoreCase(o2.get("name"));
			}else {
				return o1.get("code").compareToIgnoreCase(o2.get("code"));
			}
		});
		CommunicationEventMappingHelper.getListOfData(map, page, list, idCurr, iLabel, contentId);
		return CommunicationEventMappingConstants.AUTOCOMPLETE;

	}
	
	@PreAuthorize(MAKER_COMMUNICATIONEVENTMAPPINGHEADER)
	@RequestMapping(value = "/getQueryBaseSelectClause")
	@ResponseBody
	public String getQueryBaseSelectClause(@RequestParam(value = "sourceProductId") Long sourceProductId) {
		return communicationEventMappingService.getQueryBaseSelectClause(sourceProductId);
	}
}
