package com.nucleus.web.communicationname.master;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
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

import com.nucleus.core.datastore.service.DocumentMetaData;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.constants.CustomerInternalFlag;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationAttachment;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationParameter;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationNameService;
import com.nucleus.finnone.pro.communicationgenerator.serviceinterface.ICommunicationTemplateService;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationNameVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationTemplateVo;
import com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.master.BaseMasterUtils;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

@Transactional
@Controller
@RequestMapping(value = "/CommunicationName")
public class CommunicationNameController extends BaseController {
	private static final String COMMUNICATION_NAME_CONSTANT = "communicationName";
	private static final String MASTER_ID_CONSTANT = "masterID";
	private static final String SELECTED_ITEMS = "selectedCommunicationAttachments";
	private static final String COM_ATTACHMENTS = "communicationAttachmentList";
	private static final String COMMUNICATION_NAME_VO = "communicationNameVO";
	
	@Inject
	@Named("communicationNameService")
	private ICommunicationNameService communicationNameService;

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	private static final String COMMUNICATION_NAMES_FRAGMENT = "communicationNamesList";
	private static final String masterId = "CommunicationName";

	@Inject
	@Named("communicationTemplateService")
	private ICommunicationTemplateService communicationTemplateService;

	private static final String STATUS = "status";
	private static final String MESSAGE = "message";
	private static final String STATUS_TRUE = "true";
	private static final String STATUS_FALSE = "false";

	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONNAME')")
	@RequestMapping(value = "/create")
	public String createCommunicationName(ModelMap map) {
		return createAnotherMaster(map);
	}

	private void prepareParametersMap(ModelMap map) {
		Map<Long, String> parametersMap = communicationNameService.getApprovedParametersIdAndName();
		map.put("parametersMap", parametersMap);
	}

	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONNAME')")
	@RequestMapping(value = "/getCommunications", method = RequestMethod.GET)
	public String getUnMappedCommunicationsBasedOnModule(
			@RequestParam(value = "sourceProduct", required = false) String sourceProductId, ModelMap map)
			throws IOException {

		prepareAvailableAttachmentList(sourceProductId, map);
		return COMMUNICATION_NAMES_FRAGMENT;
	}

	private void prepareAvailableAttachmentList(String sourceProductId, ModelMap map) {
		SourceProduct sourceProduct = genericParameterService.findById(Long.parseLong(sourceProductId),
				SourceProduct.class);
		CommunicationType communicationTypeLetter = genericParameterService.findByCode(CommunicationType.LETTER,
				CommunicationType.class);
		communicationTypeLetter.setCode(CommunicationType.LETTER);
		List<CommunicationName> communicationAttachmentList = communicationNameService
				.getCommunicationNamesBasedOnCommunicationType(communicationTypeLetter, sourceProduct);
		if (hasNoElements(communicationAttachmentList)) {
			communicationAttachmentList = new ArrayList<CommunicationName>();
		}
		map.put(COM_ATTACHMENTS, communicationAttachmentList);
		map.put(MASTER_ID_CONSTANT, masterId);
	}

	@PreAuthorize("hasAuthority('VIEW_COMMUNICATIONNAME') or hasAuthority('MAKER_COMMUNICATIONNAME') or hasAuthority('CHECKER_COMMUNICATIONNAME')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewCommunicationName(@PathVariable("id") Long id, ModelMap map) {

		UserInfo currentUser = getUserDetails();
		CommunicationName communicationName = baseMasterService.getMasterEntityWithActionsById(CommunicationName.class,
				id, currentUser.getUserEntityId().getUri());
		prepareCommunicationTemplatesBeforeViewAndEdit(id, communicationName, map);
		if (ValidatorUtils.notNull(communicationName.getViewProperties())) {
			List<String> actions = (ArrayList<String>) communicationName.getViewProperties().get("actions");
			if (ValidatorUtils.notNull(actions)) {
				for (String act : actions) {
					String actionString = "act" + act;
					map.put(actionString.replaceAll(" ", ""), false);
				}

			}
		}
		prepareParametersMap(map);
		map.put("viewable", true);
		map.put("codeViewMode", true);
		return COMMUNICATION_NAME_CONSTANT;
	}

	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONNAME')")
	@RequestMapping(value = "/edit/{id}")
	public String editCommunicationName(@PathVariable("id") Long id, ModelMap map) {
		CommunicationName communicationName =   (CommunicationName) BaseMasterUtils.getMergeEditedRecords(CommunicationName.class, id);
		prepareCommunicationTemplatesBeforeViewAndEdit(id, communicationName, map);
		map.put("edit", true);
		return COMMUNICATION_NAME_CONSTANT;
	}

	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONNAME')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveCommunicationName(@Validated CommunicationNameVO communicationNameVO, BindingResult result,
			ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

		if (communicationNameVO != null && hasElements(communicationNameVO.getCommunicationTemplateVoList())) {
			validateTemplateFields(communicationNameVO, result, map);
		}
		CommunicationName communicationName = prepareDataForSaveOrSaveAndSendForApproval(communicationNameVO);
		List<String> colNameList = preProcessCommunicationNameBeforeSaveCommunication(communicationName);
		Boolean duplicatesFlag = validateCommunicationTemplateCode(result, communicationName);
		if (result.hasErrors() || (ValidatorUtils.hasElements(colNameList)) || duplicatesFlag) {
			prepareParametersMap(map);
			map.put(COMMUNICATION_NAME_VO, communicationNameVO);
			map.put(MASTER_ID_CONSTANT, masterId);
			prepareForApprovedRecords(map, communicationName);

			for (String c : colNameList) {
				result.rejectValue(c, "label." + c + ".validation.exists");
			}

			return COMMUNICATION_NAME_CONSTANT;

		}
		List<CommunicationTemplate> modifiedCommunicationTemplates = (List<CommunicationTemplate>) BaseMasterUtils
				.getUpdatedChildRecordsFromChangedChildList(communicationName, "communicationTemplates",
						communicationName.getCommunicationTemplates(), "communication");
		communicationName.setCommunicationTemplates(modifiedCommunicationTemplates);

		makerCheckerService.masterEntityChangedByUser(communicationName, getUserDetails().getUserReference());
		if (createAnotherMaster) {
			return createAnotherMaster(map);
		}
		return "redirect:/app/grid/CommunicationName/CommunicationName/loadColumnConfig";

	}

	private void prepareForApprovedRecords(ModelMap map, CommunicationName communicationName) {
		if (communicationName.getId() != null) {
			CommunicationName communicationNameById = baseMasterService.getMasterEntityById(CommunicationName.class,
					communicationName.getId());
			if (!(ApprovalStatus.UNAPPROVED_ADDED == communicationNameById.getApprovalStatus()
					|| ApprovalStatus.CLONED == communicationNameById.getApprovalStatus())) {
				map.put("codeViewMode", true);
			}
		}
	}

	private Boolean validateCommunicationTemplateCode(BindingResult result, CommunicationName communicationName) {
		Boolean duplicatesFlag = false;
		if (hasElements(communicationName.getCommunicationTemplates())) {
			List<String> codeList = new ArrayList<>();
			List<CommunicationTemplate> communicationTemplates = new ArrayList<>();
			int index = 0;
			for (CommunicationTemplate communicationTemplate : communicationName.getCommunicationTemplates()) {
				communicationTemplate.setOfflineFlag(communicationName.getOfflineFlag());
				String code = communicationTemplate.getCommunicationTemplateCode();
				if (!(codeList.contains(code))) {
					codeList.add(code);
				} else {
					duplicatesFlag = true;
					result.rejectValue("communicationTemplateVoList[" + index + "].communicationTemplateCode",
							"label.duplicateCommunicationTemplateCode");
				}
				if (communicationTemplate.getCommunicationTemplateCode() != null
						&& communicationTemplate.getCommunicationTemplateName() != null) {
					communicationTemplate.setCommunication(communicationName);
					communicationTemplates.add(communicationTemplate);
				}
				index++;
			}
			communicationName.setCommunicationTemplates(communicationTemplates);
		}
		return duplicatesFlag;
	}

	private void validateTemplateFields(CommunicationNameVO communicationNameVO, BindingResult result, ModelMap map) {
		if (communicationNameVO.getId() == null) {
			List<CommunicationTemplateVo> communicationTemplateVos = communicationNameVO
					.getCommunicationTemplateVoList();
			int index = 0;
			for (CommunicationTemplateVo communicationTemplateVo : communicationTemplateVos) {
				if (communicationTemplateVo.getCommunicationTemplateFile() == null
						&& communicationTemplateVo.getUploadedTemplate() != null
						&& communicationTemplateVo.getUploadedTemplate().getSize() == 0) {
					result.rejectValue("communicationTemplateVoList[" + index + "].communicationTemplateFile",
							"label.communicationname.communicationTemplate.file.upload.required");
					result.rejectValue("communicationTemplateVoList[" + index + "].uploadedTemplate",
							"label.communicationname.communicationTemplate.file.upload.required");
				}
				index++;
			}

		} else {
			List<CommunicationTemplateVo> communicationTemplateVos = communicationNameVO
					.getCommunicationTemplateVoList();
			int index = 0;
			for (CommunicationTemplateVo communicationTemplateVo : communicationTemplateVos) {
				if (communicationTemplateVo.getCommunicationTemplateFile() == null
						&& communicationTemplateVo.getUploadedTemplate() != null
						&& communicationTemplateVo.getUploadedTemplate().getSize() == 0
						&& communicationTemplateVo.getUploadedDocumentId() == null) {
					result.rejectValue("communicationTemplateVoList[" + index + "].communicationTemplateFile",
							"label.communicationname.communicationTemplate.file.upload.required");
					result.rejectValue("communicationTemplateVoList[" + index + "].uploadedTemplate",
							"label.communicationname.communicationTemplate.file.upload.required");
				}
				index++;
			}
		}
	}

	@PreAuthorize("hasAuthority('MAKER_COMMUNICATIONNAME')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApproval(@Validated CommunicationNameVO communicationNameVO, BindingResult result,
			ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

		if (communicationNameVO != null && hasElements(communicationNameVO.getCommunicationTemplateVoList())) {
			validateTemplateFields(communicationNameVO, result, map);
		}
		CommunicationName communicationName = prepareDataForSaveOrSaveAndSendForApproval(communicationNameVO);
		List<String> colNameList = preProcessCommunicationNameBeforeSaveCommunication(communicationName);
		Boolean duplicatesFlag = validateCommunicationTemplateCode(result, communicationName);

		if (duplicatesFlag || result.hasErrors() || (ValidatorUtils.hasElements(colNameList))) {
			prepareParametersMap(map);
			map.put(COMMUNICATION_NAME_CONSTANT, communicationName);
			map.put(MASTER_ID_CONSTANT, masterId);
			prepareForApprovedRecords(map, communicationName);
			for (String c : colNameList) {
				result.rejectValue(c, "label." + c + ".validation.exists");
			}

			return COMMUNICATION_NAME_CONSTANT;

		}
		List<CommunicationTemplate> modifiedCommunicationTemplates = BaseMasterUtils
				.getUpdatedChildRecordsFromChangedChildList(communicationName, "communicationTemplates",
						communicationName.getCommunicationTemplates(), "communication");
		communicationName.setCommunicationTemplates(modifiedCommunicationTemplates);
		makerCheckerService.saveAndSendForApproval(communicationName, getUserDetails().getUserReference());

		if (createAnotherMaster) {
			return createAnotherMaster(map);
		}
		return "redirect:/app/grid/CommunicationName/CommunicationName/loadColumnConfig";
	}

	private CommunicationName prepareDataForSaveOrSaveAndSendForApproval(CommunicationNameVO communicationNameVO) {
		CommunicationName communicationName = communicationNameService.convertToCommunicationName(communicationNameVO);
		communicationName.setAttachments(null);
		prepareCommunicationNameWithAttachmentsBeforeSave(communicationName, communicationNameVO.getAttachmentIds());
		prepareCommunicatioNameWithParametersBeforeSave(communicationNameVO, communicationName);
		return communicationName;
	}

	private void prepareCommunicatioNameWithParametersBeforeSave(CommunicationNameVO communicationNameVO,
			CommunicationName communicationName) {
		Long[] communicationParameterIds = communicationNameVO.getCommunicationParameters();
		List<CommunicationParameter> communicationParameters = new ArrayList<>(communicationParameterIds.length);
		if (communicationParameterIds != null && communicationParameterIds.length > 0) {
			for (Long parameterId : communicationParameterIds) {
				CommunicationParameter communicationParameter = baseMasterService
						.getMasterEntityById(CommunicationParameter.class, parameterId);
				communicationParameters.add(communicationParameter);
			}

		}
		communicationName.setCommunicationParameters(communicationParameters);
	}

	private void prepareCommunicationNameWithAttachmentsBeforeSave(CommunicationName parentCommunication,
			Long[] attachmentIds) {
		if (attachmentIds != null) {
			List<CommunicationAttachment> listOfAttachments = new ArrayList<>(attachmentIds.length);

			for (Long attachmentId : attachmentIds) {
				CommunicationName attachmentCN = baseMasterService.getMasterEntityById(CommunicationName.class,
						attachmentId);
				CommunicationAttachment attachment = new CommunicationAttachment();
				attachment.setAttachedCommunication(attachmentCN);
				attachment.setParentCommunication(parentCommunication);
				listOfAttachments.add(attachment);
			}
			parentCommunication.setAttachments(listOfAttachments);
		} else {
			parentCommunication.setAttachments(null);
		}

	}

	private List<String> preProcessCommunicationNameBeforeSaveCommunication(CommunicationName communicationName) {
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put("communicationCode", communicationName.getCommunicationCode());
		return checkValidationForDuplicates(communicationName, CommunicationName.class, validateMap);

	}

	@ResponseBody
	@RequestMapping(value = "/CheckCommunicationCode/{code}", method = RequestMethod.GET)
	public Map<String, String> checkCommunicationCode(@PathVariable String code, HttpServletRequest request) {

		Map<String, String> map = new HashMap<>();
		if (isThisCodePresent(code, CommunicationName.class, "communicationCode")) {
			map.put(STATUS, STATUS_FALSE);
			map.put(MESSAGE, messageSource.getMessage("label.communicationCode.validation.exists", null,
					RequestContextUtils.getLocale(request)));
		} else {
			map.put(STATUS, STATUS_TRUE);
			map.put(MESSAGE, messageSource.getMessage("label.communicationCode.available", null,
					RequestContextUtils.getLocale(request)));
		}
		return map;
	}

	@ResponseBody
	@RequestMapping(value = "/CheckCommunicationTemplateCode/{code}", method = RequestMethod.GET)
	public Map<String, String> checkCommunicationTemplateCode(@PathVariable String code, HttpServletRequest request) {

		Map<String, String> map = new HashMap<>();
		if (isThisCodePresent(code, CommunicationTemplate.class, "communicationTemplateCode")) {
			map.put(STATUS, STATUS_FALSE);
			map.put(MESSAGE, messageSource.getMessage("label.communicationCode.validation.exists", null,
					RequestContextUtils.getLocale(request)));
		} else {
			map.put(STATUS, STATUS_TRUE);
			map.put(MESSAGE, messageSource.getMessage("label.communicationTemplateCode.available", null,
					RequestContextUtils.getLocale(request)));
		}
		return map;
	}

	private <T extends BaseMasterEntity> boolean isThisCodePresent(String code, Class<T> entityClass,
			String attributeName) {
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put(attributeName, code);
		return ValidatorUtils.hasElements(baseMasterService.hasEntity(entityClass, validateMap));
	}

	private String createAnotherMaster(ModelMap map) {
		CommunicationNameVO communicationNameVO = new CommunicationNameVO();
		List<CommunicationTemplateVo> communicationTemplateVoList = new ArrayList<>();
		communicationNameVO.setCommunicationTemplateVoList(communicationTemplateVoList);
		prepareParametersMap(map);
		map.put(COMMUNICATION_NAME_VO, communicationNameVO);
		List<CommunicationType> communicationTypeList = genericParameterService.retrieveTypes(CommunicationType.class);
		List<CustomerInternalFlag> customerInternalFlagList = genericParameterService
				.retrieveTypes(CustomerInternalFlag.class);
		map.put("communicationTypeList", communicationTypeList);
		map.put("CustomerInternalFlagList", customerInternalFlagList);
		map.put(MASTER_ID_CONSTANT, masterId);
		map.put("create", true);
		map.put("viewable", false);
		return COMMUNICATION_NAME_CONSTANT;
	}

	private void prepareCommunicationTemplatesBeforeViewAndEdit(Long id, CommunicationName communicationName,
			ModelMap map) {
		List<CommunicationTemplate> communicationTemplates = null;
		if (ApprovalStatus.UNAPPROVED_MODIFIED == communicationName.getApprovalStatus()
				|| ApprovalStatus.WORFLOW_IN_PROGRESS == communicationName.getApprovalStatus()) {
			CommunicationName originalCommunicationName = (CommunicationName) baseMasterService
					.getLastApprovedEntityByUnapprovedEntityId(communicationName.getEntityId());
			if (ValidatorUtils.notNull(originalCommunicationName)) {
				communicationTemplates = communicationNameService
						.getCommunicationTemplatesAssociatedWithComunicationName(originalCommunicationName.getId());
			} else {
				communicationTemplates = communicationNameService
						.getCommunicationTemplatesAssociatedWithComunicationName(id);
			}
		} else {
			communicationTemplates = communicationNameService
					.getCommunicationTemplatesAssociatedWithComunicationName(id);
		}

		if ((CommunicationType.EMAIL.equals(communicationName.getCommunicationType().getCode()) ||
				CommunicationType.WHATSAPP.equals(communicationName.getCommunicationType().getCode()))
				&& ValidatorUtils.hasElements(communicationName.getAttachments())) {
			List<CommunicationName> selectedCommunicationAttachmentList = new ArrayList<>();
			for (CommunicationAttachment cna : communicationName.getAttachments()) {
				selectedCommunicationAttachmentList.add(cna.getAttachedCommunication());
			}
			map.put(SELECTED_ITEMS, selectedCommunicationAttachmentList);
			CommunicationType communicationTypeLetter = genericParameterService.findByCode(CommunicationType.LETTER,
					CommunicationType.class);
			communicationTypeLetter.setCode(CommunicationType.LETTER);
			List<CommunicationName> communicationAttachmentList = communicationNameService
					.getCommunicationNamesBasedOnCommunicationType(communicationTypeLetter,
							communicationName.getSourceProduct());
			map.put(COM_ATTACHMENTS, communicationAttachmentList);
		}removeChildDeletedRecord(communicationName); // removing deleted child record which were kept for history
		CommunicationNameVO communicationNameVO = communicationNameService
				.convertToCommunicationNameVo(communicationName);
		List<CommunicationParameter> communicationParameters = communicationName.getCommunicationParameters();
		if (hasElements(communicationParameters)) {
			Long communicationParamterIds[] = new Long[communicationParameters.size()];
			int index = 0;
			for (CommunicationParameter communicationParameter : communicationParameters) {
				communicationParamterIds[index] = communicationParameter.getId();
				index++;
			}
			communicationNameVO.setCommunicationParameters(communicationParamterIds);
		}
		
		map.put(COMMUNICATION_NAME_VO, communicationNameVO);
		map.put("communicationTemplates", communicationTemplates);
		map.put(MASTER_ID_CONSTANT, masterId);
		map.put("approvalStatus", communicationName.getApprovalStatus());
		prepareForApprovedRecords(map, communicationName);
		prepareParametersMap(map);

	}

	private void removeChildDeletedRecord(CommunicationName communicationName) {

	  	  List<CommunicationTemplate> templatesList=communicationName.getCommunicationTemplates();
	        if(CollectionUtils.isNotEmpty(templatesList))
	        		{
	        			Iterator<CommunicationTemplate> iterator=templatesList.iterator();
	        			while(iterator.hasNext())
	        			{
	        				CommunicationTemplate template=iterator.next();
	        				if(template.getApprovalStatus()==ApprovalStatus.DELETED_APPROVED_IN_HISTORY)
	        				{
	        					iterator.remove();
	        				}
	        			}
	        		}
	  	
	  
	}

	@PreAuthorize("hasAuthority('VIEW_COMMUNICATIONNAME') or hasAuthority('MAKER_COMMUNICATIONNAME') or hasAuthority('CHECKER_COMMUNICATIONNAME')")
	@RequestMapping(value = "/generateTemplate", method = RequestMethod.POST)
	@ResponseBody
	public String generateTemplate(@RequestParam Long id, ModelMap model, HttpServletResponse response,
			HttpServletRequest request) throws IOException {

		Map<String, Object> responseMap = new HashMap<>();
		try {
			CommunicationTemplate communicationTemplate = communicationTemplateService.getCommunicationTemplateById(id);
			if (communicationTemplate == null) {
				throw new SystemException("Template with given id: " + id + " not found");
			}
			String documentId = communicationTemplate.getUploadedDocumentId();
			boolean uploadBasedTemplate = documentId != null ? true : false;
			DocumentMetaData documentMetaData = null;
			if (uploadBasedTemplate) {
				documentMetaData = this.communicationNameService.getTemplateFromStorageService(documentId);
			} else {
				documentMetaData = communicationTemplateService.getFileFromCommunicationTemplate(communicationTemplate);
			}
			if (documentMetaData == null) {
				throw new SystemException("Error in retrieving template file");
			}
			sendDownloadedFileToBrowser(documentMetaData.getContent(),
					documentMetaData.getFileName().concat(".").concat(documentMetaData.getFileExtension()), response,
					model, documentMetaData.getMimeType());
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception in downloading file:", e);
			responseMap.put("error", "Exception in downloading file");
			return convertToJSONString(responseMap);
		}

		String ret = convertToJSONString(responseMap);
		return ret;

	}

	private void sendDownloadedFileToBrowser(byte[] fileContent, String fileName, HttpServletResponse response,
			ModelMap model, String contentType) {

		if (notNull(fileContent) && fileContent.length > 0) {
			if (notNull(model)) {
				model.addAttribute("result", fileContent);
			}
			response.addHeader("Content-Disposition", " attachment;filename=" + fileName);
			response.setHeader("Set-Cookie", "fileDownload=true; path=/");
			response.setContentType(contentType);
			response.setContentLength(fileContent.length);
			OutputStream out = null;
			try {
				out = response.getOutputStream();
				out.write(fileContent);
				out.close();
			} catch (IOException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				throw ExceptionBuilder
						.getInstance(BusinessException.class, MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD,
								"IO error while downloading file ")
						.setMessage(CoreUtility.prepareMessage(MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD))
						.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).setOriginalException(e)
						.build();
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (IOException e) {
						BaseLoggers.exceptionLogger.error(e.getMessage(), e);
						throw new SystemException("Exception in closing stream", e);
					}
				}
			}
		} else {
			throw new SystemException("Template File content is empty");
		}
	}

}
