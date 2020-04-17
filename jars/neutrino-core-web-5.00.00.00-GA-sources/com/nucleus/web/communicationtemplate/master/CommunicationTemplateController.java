package com.nucleus.web.communicationtemplate.master;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
import org.springframework.web.servlet.ModelAndView;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationNameService;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationTemplateVo;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;
import com.nucleus.web.master.MakerCheckerWebUtils;
import com.nucleus.core.datastore.service.DatastorageService;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;

@Transactional
@Controller
@RequestMapping(value = "/CommunicationTemplate")
public class CommunicationTemplateController extends BaseController{
	private static final String COMMUNICATION_NAMES_APPROVED = "communicationNamesApproved";
	private static final String COMMUNICATION_TEMPLATE = "communicationTemplate";
	private static final String COMMUNICATION_TEMPLATE_VO = "communicationTemplateVo";
	private static final String MASTER_ID = "masterID";
	@Inject
    @Named("messageSource")
    protected MessageSource           messageSource;
	
	
	
	@Inject
	@Named("communicationNameService")
	private ICommunicationNameService communicationNameService;
	
	@Autowired
	private MakerCheckerService makerCheckerService;

	@Autowired
	private BaseMasterService baseMasterService;
	
	private static final String masterId   = "CommunicationTemplate";
	
	@Inject
	@Named("couchDataStoreDocumentService")
	private DatastorageService couchDataStoreDocumentService;
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONTEMPLATE')")
	@RequestMapping(value = "/create")
	public String createCommunicationTemplate(ModelMap map) {
		CommunicationTemplateVo communicationTemplateVo = new CommunicationTemplateVo();
		
		map.put(COMMUNICATION_NAMES_APPROVED, communicationNameService.getApprovedCommunicationNames());
		map.put(COMMUNICATION_TEMPLATE_VO, communicationTemplateVo);
		map.put(MASTER_ID, masterId);
		map.put("viewable", false);
		return COMMUNICATION_TEMPLATE;
	}

	@PreAuthorize("hasAuthority('VIEW_COMMUNICATIONTEMPLATE') or hasAuthority('MAKER_COMMUNICATIONTEMPLATE') or hasAuthority('CHECKER_COMMUNICATIONTEMPLATE')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewCommunicationTemplate(@PathVariable("id") Long id, ModelMap map) 
	{

		UserInfo currentUser = getUserDetails();
		CommunicationTemplate communicationTemplate = baseMasterService.getMasterEntityWithActionsById(CommunicationTemplate.class, id, currentUser.getUserEntityId().getUri());
		prepareCommunicationTemplates(map, communicationTemplate);
		map.put("viewable", true);
		if (ValidatorUtils.notNull(communicationTemplate.getViewProperties())) {
			List<String> actions = (ArrayList<String>) communicationTemplate.getViewProperties().get("actions");
			if (ValidatorUtils.notNull(actions)) {
				for (String act : actions) {
					String actionString = "act" + act;
					map.put(actionString.replaceAll(" ", ""), false);
				}

			}

		}
		return COMMUNICATION_TEMPLATE;
	}

	
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONTEMPLATE')")
	@RequestMapping(value = "/edit/{id}")
	public String editCommunicationTemplate(@PathVariable("id") Long id, ModelMap map) {
		CommunicationTemplate communicationTemplate = baseMasterService.getMasterEntityById(CommunicationTemplate.class, id);
		prepareCommunicationTemplates(map, communicationTemplate);
		
		map.put("approvalStatus",communicationTemplate.getApprovalStatus());
		
		map.put("edit", true);
		return COMMUNICATION_TEMPLATE;
	}
	
	private void prepareCommunicationTemplates(ModelMap map,
			CommunicationTemplate communicationTemplate) {
		List<CommunicationTemplate> communicationTemplates = communicationNameService.getCommunicationTemplatesAssociatedWithComunicationName(communicationTemplate.getCommunicationMasterId());
		int indexOfThisCommunicationTemplate = communicationTemplates.indexOf(communicationTemplate);
		communicationTemplates.remove(indexOfThisCommunicationTemplate);
		
		Map<Long, String> approvalStatusDescriptionForCommunicationTemplates =  new HashMap<Long, String>();
		for(CommunicationTemplate temp : communicationTemplates)
		{
			approvalStatusDescriptionForCommunicationTemplates.put(temp.getId(), MakerCheckerWebUtils.getApprovalStatus(temp.getApprovalStatus()));
		}
		
		map.put("approvalStatus",communicationTemplate.getApprovalStatus());
		map.put(COMMUNICATION_NAMES_APPROVED, communicationNameService.getApprovedCommunicationNames());
		map.put("approvalStatusDescriptionForCommunicationTemplates", approvalStatusDescriptionForCommunicationTemplates);
		map.put("communicationTemplates", communicationTemplates);
		map.put(COMMUNICATION_TEMPLATE, communicationTemplate);
		map.put(MASTER_ID, masterId);
	}
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONTEMPLATE')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveCommunicationTemplate(@Validated CommunicationTemplate communicationTemplate, BindingResult result, ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) 
	{
		List<String> colNameList = preProcessCommunicationTemplateBeforeSave(communicationTemplate);
		if (result.hasErrors() || (ValidatorUtils.hasElements(colNameList))) 
		{
			map.put(COMMUNICATION_NAMES_APPROVED, communicationNameService.getApprovedCommunicationNames());
			map.put(COMMUNICATION_TEMPLATE, communicationTemplate);
			map.put(MASTER_ID, masterId);
			map.put("viewable", false);
			if (ValidatorUtils.hasElements(colNameList)) 
			{
				for (String c : colNameList) 
				{
					result.rejectValue(c, "label." + c + ".validation.exists");
				}
			}
			return COMMUNICATION_TEMPLATE;
		}
		
		makerCheckerService.masterEntityChangedByUser(communicationTemplate, getUserDetails().getUserReference());

		if (createAnotherMaster) 
		{
			map.put(COMMUNICATION_NAMES_APPROVED, communicationNameService.getApprovedCommunicationNames());
			map.put(COMMUNICATION_TEMPLATE, new CommunicationTemplate());
			map.put(MASTER_ID, masterId);
			map.put("viewable", false);
			return COMMUNICATION_TEMPLATE;
		}
		return "redirect:/app/grid/CommunicationTemplate/CommunicationTemplate/loadColumnConfig";

	}

	private List<String> preProcessCommunicationTemplateBeforeSave(
			CommunicationTemplate communicationTemplate) {
		BaseLoggers.flowLogger.debug("Saving CommunicationTemplate Details-->"+communicationTemplate);
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put("communicationTemplateCode", communicationTemplate.getCommunicationTemplateCode());
		validateMap.put("communicationTemplateName", communicationTemplate.getCommunicationTemplateName());
		
		return checkValidationForDuplicates(communicationTemplate, CommunicationTemplate.class, validateMap);
	}
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONTEMPLATE')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApproval(@Validated CommunicationTemplate communicationTemplate, BindingResult result, ModelMap map,
			@RequestParam("createAnotherMaster") boolean createAnotherMaster) {
			List<String> colNameList = preProcessCommunicationTemplateBeforeSave(communicationTemplate);
			if (result.hasErrors() || (ValidatorUtils.hasElements(colNameList))) 
			{
				map.put(COMMUNICATION_NAMES_APPROVED, communicationNameService.getApprovedCommunicationNames());
				map.put(COMMUNICATION_TEMPLATE, communicationTemplate);
				map.put(MASTER_ID, masterId);
				map.put("viewable", false);

				if (ValidatorUtils.hasElements(colNameList)) 
				{
					for (String c : colNameList) 
					{
						result.rejectValue(c, "label." + c + ".validation.exists");
					}
				}
				return COMMUNICATION_TEMPLATE;
			}
			
			
			makerCheckerService.saveAndSendForApproval(communicationTemplate, getUserDetails().getUserReference());
			
			if (createAnotherMaster) 
			{
				
				
				map.put(COMMUNICATION_NAMES_APPROVED, communicationNameService.getApprovedCommunicationNames());
				map.put(COMMUNICATION_TEMPLATE, new CommunicationTemplate());
				map.put(MASTER_ID, masterId);
				map.put("viewable", false);
				return COMMUNICATION_TEMPLATE;
			}
			return "redirect:/app/grid/CommunicationTemplate/CommunicationTemplate/loadColumnConfig";
		}
	
	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONTEMPLATE')")
	@RequestMapping(value = "/loadTemplatesList", method = RequestMethod.POST)
	  public ModelAndView onLoadTemplatesForCommunicationName(@RequestParam("communicationNameId") Long id) {
	    Map<String, Object> onLoadScreenMap = new HashMap<String, Object>();
	    List<CommunicationTemplate> communicationTemplates = communicationNameService.getCommunicationTemplatesAssociatedWithComunicationName(id);
	    
	    Map<Long, String> approvalStatusDescriptionForCommunicationTemplates =  new HashMap<Long, String>();
		for(CommunicationTemplate temp : communicationTemplates)
		{
			approvalStatusDescriptionForCommunicationTemplates.put(temp.getId(), MakerCheckerWebUtils.getApprovalStatus(temp.getApprovalStatus()));
		}
		onLoadScreenMap.put("approvalStatusDescriptionForCommunicationTemplates", approvalStatusDescriptionForCommunicationTemplates);
		onLoadScreenMap.put("communicationTemplates", communicationTemplates);
	    return new ModelAndView("communicationTemplateListForCommunicationName", onLoadScreenMap);
	  }
	  
	
	}
