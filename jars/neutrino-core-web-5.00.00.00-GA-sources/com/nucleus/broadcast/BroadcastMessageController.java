package com.nucleus.broadcast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.broadcast.entity.BroadcastMessage;
import com.nucleus.broadcast.service.BroadcastMessageService;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

@Controller
@Transactional
@RequestMapping(value = "/BroadcastMessage")
public class BroadcastMessageController extends BaseController {

	private static final String masterId = "BroadcastMessage";

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;

	@Inject
	@Named("broadcastMessageService")
	private BroadcastMessageService broadcastMessageService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	private static final String STATUS = "status";
	private static final String MESSAGE = "message";
	private static final String STATUS_TRUE = "true";
	private static final String STATUS_FALSE = "false";
	private static final String VIEWABLE = "viewable";
	private static final String MODULE_NAMES = "moduleNames";
	private static final String BROADCAST_MESSAGE = "broadcastMessage";
	private static final String MASTER_ID = "masterID";

	@PreAuthorize("hasAuthority('MAKER_BROADCASTMESSAGE')")
	@RequestMapping(value = "/create")
	public String createBroadcastMessage(ModelMap map) {

		map.put(MODULE_NAMES, getSourceProductList());
		map.put(BROADCAST_MESSAGE, new BroadcastMessage());
		map.put(MASTER_ID, masterId);
		map.put(VIEWABLE, false);
		return BROADCAST_MESSAGE;
	}

	@PreAuthorize("hasAuthority('MAKER_BROADCASTMESSAGE')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApproval(@Validated BroadcastMessage broadcastMessage, BindingResult result,
			ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

		broadcastMessage.setModule(null);

		Map<String, Object> validateMap = new HashMap<>();
		validateMap.put("messageCode", broadcastMessage.getMessageCode());
		validateMap.put("message", broadcastMessage.getMessage());
		validateMap.put("priority", broadcastMessage.getPriority());
		List<String> colNameList = checkValidationForDuplicates(broadcastMessage, BroadcastMessage.class, validateMap);
		if (!colNameList.isEmpty()) {
			prepareForApprovedRecords(map, broadcastMessage);
			for (String c : colNameList) {
				result.rejectValue(c, "label." + c + ".validation.exists");
				BaseLoggers.exceptionLogger.error("label." + c + ".validation.exists");
			}
			map.put(MODULE_NAMES, getSourceProductList());
			map.put(BROADCAST_MESSAGE, broadcastMessage);
			map.put(MASTER_ID, masterId);
			map.put(VIEWABLE, false);
			return BROADCAST_MESSAGE;
		}

		if (createAnotherMaster) {
			BroadcastMessage broadcastMessageForAnotherMaster = new BroadcastMessage();
			map.put(BROADCAST_MESSAGE, broadcastMessageForAnotherMaster);
			map.put(MASTER_ID, masterId);
			return BROADCAST_MESSAGE;
		}
		map.put(MASTER_ID, masterId);

		User user = getUserDetails().getUserReference();
		if (user != null) {
			makerCheckerService.saveAndSendForApproval(broadcastMessage, user);
		}

		return "redirect:/app/grid/BroadcastMessage/BroadcastMessage/loadColumnConfig";
	}

	@PreAuthorize("hasAuthority('MAKER_BROADCASTMESSAGE')")
	@RequestMapping(value = "/edit/{id}")
	public String editBroadcastMessage(@PathVariable("id") Long id, ModelMap map) {
		UserInfo currentUser = getUserDetails();
		BroadcastMessage broadcastMessage = baseMasterService.getMasterEntityWithActionsById(BroadcastMessage.class, id,
				currentUser.getUserEntityId().getUri());
		if (broadcastMessage.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
			BroadcastMessage prevBroadcastMessage = (BroadcastMessage) baseMasterService
					.getLastApprovedEntityByUnapprovedEntityId(broadcastMessage.getEntityId());
			map.put("prevBroadcastMessage", prevBroadcastMessage);
			map.put("editLink", false);
		}

		prepareForApprovedRecords(map, broadcastMessage);
		map.put(BROADCAST_MESSAGE, broadcastMessage);
		map.put("approvalStatus", broadcastMessage.getApprovalStatus());

		map.put(MODULE_NAMES, getSourceProductList());
		map.put(VIEWABLE, false);
		map.put("edit", true);
		map.put(MASTER_ID, masterId);
		return BROADCAST_MESSAGE;

	}

	@PreAuthorize("hasAuthority('MAKER_BROADCASTMESSAGE')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveBroadcastMessage(@Validated BroadcastMessage broadcastMessage, BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		broadcastMessage.setModule(null);

		Map<String, Object> validateMap = new HashMap<>();
		validateMap.put("messageCode", broadcastMessage.getMessageCode());
		validateMap.put("message", broadcastMessage.getMessage());
		validateMap.put("priority", broadcastMessage.getPriority());
		List<String> colNameList = checkValidationForDuplicates(broadcastMessage, BroadcastMessage.class, validateMap);
		if (!colNameList.isEmpty()) {
			prepareForApprovedRecords(map, broadcastMessage);

			for (String c : colNameList) {
				result.rejectValue(c, "label." + c + ".validation.exists");
				BaseLoggers.exceptionLogger.error("label." + c + ".validation.exists");
			}
			map.put(MODULE_NAMES, getSourceProductList());
			map.put(BROADCAST_MESSAGE, broadcastMessage);
			map.put(MASTER_ID, masterId);
			map.put(VIEWABLE, false);
			return BROADCAST_MESSAGE;
		}

		User user = getUserDetails().getUserReference();
		if (user != null) {
			makerCheckerService.masterEntityChangedByUser(broadcastMessage, user);
		}

		if (createAnotherMaster) {
			BroadcastMessage broadcastMessageForAnotherMaster = new BroadcastMessage();
			map.put(BROADCAST_MESSAGE, broadcastMessageForAnotherMaster);
			map.put(MASTER_ID, masterId);
			return BROADCAST_MESSAGE;
		}

		map.put(MASTER_ID, masterId);

		return "redirect:/app/grid/BroadcastMessage/BroadcastMessage/loadColumnConfig";
	}

	@PreAuthorize("hasAuthority('VIEW_BROADCASTMESSAGE') or hasAuthority('MAKER_BROADCASTMESSAGE') or hasAuthority('CHECKER_BROADCASTMESSAGE')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewBroadcastMessage(@PathVariable("id") long id, ModelMap map) {
		UserInfo currentUser = getUserDetails();
		BroadcastMessage broadcastMessage = baseMasterService.getMasterEntityWithActionsById(BroadcastMessage.class, id,
				currentUser.getUserEntityId().getUri());

		prepareForApprovedRecords(map, broadcastMessage);
		map.put("approvalStatus", broadcastMessage.getApprovalStatus());
		map.put(MODULE_NAMES, getSourceProductList());
		map.put(BROADCAST_MESSAGE, broadcastMessage);
		map.put(MASTER_ID, masterId);
		map.put(VIEWABLE, true);
		return BROADCAST_MESSAGE;
	}

	@RequestMapping(value = "/delete/{id}")
	@PreAuthorize("hasAuthority('MAKER_BROADCASTMESSAGE')")
	public String deleteBroadcastMessage(@PathVariable("id") Long[] messageIds, ModelMap map) {

		for (long messageId : messageIds) {
			BroadcastMessage broadcastMessage = broadcastMessageService.getMessageById(messageId);
			broadcastMessageService.deleteBroadcastMessage(broadcastMessage);
		}

		return "redirect:/app/grid/BroadcastMessage/BroadcastMessage/loadColumnConfig";
	}

	private List<SourceProduct> getSourceProductList() {
		List<SourceProduct> sourceProductList = new ArrayList<SourceProduct>();
		List<Long> sourceProductIdList = new ArrayList<>();
		sourceProductIdList.add(30001L);
		sourceProductIdList.add(30002L);
		sourceProductIdList.add(99999L);
		sourceProductIdList.add(1000025L);
		sourceProductIdList.add(2000002L);
		SourceProduct sourceProduct = new SourceProduct();
		sourceProduct.setId(-1L);
		sourceProduct.setName(BroadcastMessage.ALL);
		sourceProduct.setCode(BroadcastMessage.ALL);
		sourceProductList.add(sourceProduct);
		sourceProductIdList.stream().forEach(entry -> {
			SourceProduct product = genericParameterService.findById(entry, SourceProduct.class);
			if(product!=null) {
			sourceProductList.add(product);
			}});
		return sourceProductList;
	}

	@ResponseBody
	@RequestMapping(value = "/checkMessageCode/{code}", method = RequestMethod.GET)
	public Map<String, String> checkMessageCode(@PathVariable String code, HttpServletRequest request) {

		Map<String, String> map = new HashMap<>();
		if (isThisFieldPresent(code, BroadcastMessage.class, "messageCode")) {
			map.put(STATUS, STATUS_FALSE);
			map.put(MESSAGE, messageSource.getMessage("label.broadcastmessagecode.validation.exists", null,
					RequestContextUtils.getLocale(request)));
		} else {
			map.put(STATUS, STATUS_TRUE);
			map.put(MESSAGE, messageSource.getMessage("label.broadcastmessagecode.available", null,
					RequestContextUtils.getLocale(request)));
		}
		return map;
	}
	
	@ResponseBody
	@RequestMapping(value = "/checkPriority/{priority}", method = RequestMethod.GET)
	public Map<String, String> checkPriority(@PathVariable String priority, HttpServletRequest request) {

		Long priorityInLong = Long.parseLong(priority);
		Map<String, String> map = new HashMap<>();
		if (isThisFieldPresent(priorityInLong, BroadcastMessage.class, "priority")) {
			map.put(STATUS, STATUS_FALSE);
			map.put(MESSAGE, messageSource.getMessage("label.broadcastmessagepriority.validation.exists", null,
					RequestContextUtils.getLocale(request)));
		} else {
			map.put(STATUS, STATUS_TRUE);
			map.put(MESSAGE, messageSource.getMessage("label.broadcastmessagepriority.available", null,
					RequestContextUtils.getLocale(request)));
		}
		return map;
	}

	
	private <T extends BaseMasterEntity> boolean isThisFieldPresent(String code, Class<T> entityClass,
			String attributeName) {
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put(attributeName, code);
		return ValidatorUtils.hasElements(baseMasterService.hasEntity(entityClass, validateMap));
	}
	
	private <T extends BaseMasterEntity> boolean isThisFieldPresent(long code, Class<T> entityClass,
			String attributeName) {
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put(attributeName, code);
		return ValidatorUtils.hasElements(baseMasterService.hasEntity(entityClass, validateMap));
	}

	private void prepareForApprovedRecords(ModelMap map, BroadcastMessage broadcastMessage) {
		if (broadcastMessage.getId() != null) {
			BroadcastMessage broadcastmessageNameById = baseMasterService.getMasterEntityById(BroadcastMessage.class,
					broadcastMessage.getId());
			if (!(ApprovalStatus.UNAPPROVED_ADDED == broadcastmessageNameById.getApprovalStatus()
					|| ApprovalStatus.CLONED == broadcastmessageNameById.getApprovalStatus())) {
				map.put("codeViewMode", true);
			}
		}
	}

}