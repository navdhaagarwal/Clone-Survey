package com.nucleus.web.fileupload;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadInformation;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadProcessDefinition;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadUserFormat;
import com.nucleus.finnone.pro.fileconsolidator.serviceinterface.IFileFormatDownloadService;
import com.nucleus.finnone.pro.fileconsolidator.serviceinterface.IFileUploadDownloadService;
import com.nucleus.finnone.pro.fileconsolidator.util.FileFormatDownloadResponseVo;
import com.nucleus.finnone.pro.fileconsolidator.util.FileUploadDownloadConstants;
import com.nucleus.finnone.pro.fileconsolidator.util.ProcessingModeType;
import com.nucleus.finnone.pro.fileconsolidator.vo.FcUploadVO;
import com.nucleus.finnone.pro.lmsbase.domainobject.Tenant;
import com.nucleus.lms.web.common.MessageOutput;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import com.nucleus.user.UserInfo;
import com.nucleus.web.common.controller.BaseController;

import fr.opensagres.xdocreport.core.io.IOUtils;

@Controller("commonFileUploadController")
@RequestMapping(value = "/commonFileUploadController")
public class CommonFileUploadController extends BaseController {
	@Inject
	private IFileUploadDownloadService fileConsolidatorService;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("fileFormatDownloadService")
	private IFileFormatDownloadService fileFormatDownloadService;

	@Inject
	@Named("neutrinoExecutionContextHolder")
	INeutrinoExecutionContextHolder neutrinoExecutionContextHolder;

	public static final String UPLOAD_DOWNLOAD_FORMAT_FILE_EXCEPTION = "Error occured while downloading Format file";
	public static final String ERROR = "error";
	public static final String FILE_EXTENSION_CSV = "csv";
	public static final String FILE_EXTENSION_XLS = "xls";
	public static final String FILE_EXTENSION_XLSX = "xlsx";
	public static final String FILE_EXTENSION_TXT = "txt";
	public static final String FILE_EXTENSION_INVALID = "msg.00003453";
	public static final String FILE_EXTENSION_CSV_OR_EXCEL = "csv or excel";
	public static final String INVALID_FORMAT_ID = "Invalid formatId";
	public static final String USER_FORMAT_IS_INVALID = "msg.00004161";

	private static Random randomTxnIdGenerator = new Random();
	
	@PreAuthorize("hasAnyAuthority(#processName) or hasAnyAuthority(#maker_entity)")
	@RequestMapping(value = "/fileUploadCommon", method = RequestMethod.GET)
	public String initCommonUploadScreen(Model model, @RequestParam(name = "process") String processName,
			@RequestParam(name = "entityName") String entityName, @P("maker_entity") String masterEntity,
			Boolean isFormatDownloadAvailable, HttpServletRequest request) {
		FcUploadVO uploadVO = new FcUploadVO();
		FileUploadDownloadProcessDefinition processDefinition = getProcessDefinition(processName);
		uploadVO.setProcessDefinition(processDefinition);
		// List of userFormats available
		List<FileUploadDownloadUserFormat> userformatsList = fetchUserFormats(uploadVO.getProcessDefinition().getId());
		// In case only one userFormat is available
		if (userformatsList != null && userformatsList.size() == 1) {
			model.addAttribute("singleUserFormatExists", true);
			model.addAttribute("userFormatId", userformatsList.get(0).getId());
		}
		model.addAttribute("processName", processName);
		model.addAttribute("processingModeId", uploadVO.getProcessDefinition().getProcessingModeId());
		model.addAttribute("FcUploadVO", uploadVO);
		model.addAttribute("formatList", userformatsList);
		model.addAttribute("entityName", entityName);
		model.addAttribute("masterEntity", masterEntity);
		model.addAttribute("isFormatDownloadAvailable", isFormatDownloadAvailable);
		return "masterCommonUpload";
	}

	@PreAuthorize("hasAnyAuthority(#processName) or hasAnyAuthority(#maker_entity)")
	@RequestMapping(value = "/uploadfunctionality/{processName}", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
	@ResponseBody
	public String parseAndUploadFile(FcUploadVO uploadVO, ModelMap model, HttpServletRequest request,
			@PathVariable String processName, @P("maker_entity") @RequestParam String masterEntity) {
		Map<String, Object> responseMap = new HashMap<>();
		Message msg = null;
		List<Message> messageList = null;
		Long batchId = null;
		List<Message> fileExtensionErrormessageList = validateFileExtensionForUpload(uploadVO);	
		ProcessingModeType uploadProcessingModeType = genericParameterService
				.findById(uploadVO.getProcessingModeId(), ProcessingModeType.class);	
		if (hasNoElements(fileExtensionErrormessageList)) {
			try {
				uploadVO.setAttachmentRefId(uploadVO.getAttachmentRefId());
				FileUploadDownloadInformation fileUploadInformation = fileConsolidatorService
						.storeDataAndCreateBatch(uploadVO);
				batchId = fileUploadInformation.getId();
				if(uploadProcessingModeType==null) {
					BaseLoggers.exceptionLogger.error("Processing Mode Type not found for process name: {} ",processName);
					msg = new Message();
					messageList = new ArrayList<>();
					msg.setI18nCode(FileUploadDownloadConstants.PROCESSING_MODE_TYPE_NOT_FOUND);
					msg.setMessageArguments(processName);
					messageList.add(msg);
					responseMap.put(ERROR, getWebMessageList(messageList));
					return fileFormatDownloadService.convertToJsonString(responseMap);
				}
				if (ProcessingModeType.SYNCHRONOUS.equals(uploadProcessingModeType.getCode())) {
					fileConsolidatorService.processUploadedFile(fileUploadInformation.getId());
					msg = new Message();
					messageList = new ArrayList<>();
					msg.setI18nCode(FileUploadDownloadConstants.UPLOAD_EXECUTION_COMPLETED);
					msg.setMessageArguments(uploadVO.getFileData().getOriginalFilename(),
							fileUploadInformation.getId().toString());
					messageList.add(msg);
				} else if (ProcessingModeType.ASYNCHRONOUS.equals(uploadProcessingModeType.getCode())) {
					HttpSession session = request.getSession();
					fileConsolidatorService.processUploadedFileAsynchronously(fileUploadInformation.getId(),
							(UserInfo) session.getAttribute("user_profile"),
							(Tenant) session.getAttribute("user_tenant"),
							neutrinoExecutionContextHolder.getBaseCurrency(),
							Integer.valueOf(randomTxnIdGenerator.nextInt()));
					msg = new Message();
					messageList = new ArrayList<>();
					msg.setI18nCode(FileUploadDownloadConstants.UPLOAD_SUBMISSION);
					msg.setMessageArguments(uploadVO.getFileData().getOriginalFilename(),
							fileUploadInformation.getId().toString());
					messageList.add(msg);

				} else {
					msg = new Message();
					messageList = new ArrayList<>();
					msg.setI18nCode(FileUploadDownloadConstants.UPLOAD_SUBMISSION);
					msg.setMessageArguments(uploadVO.getFileData().getOriginalFilename(),
							fileUploadInformation.getId().toString());
					messageList.add(msg);
				}

			} catch (BaseException e) {
				List<MessageOutput> list = getWebMessageList(e.getMessages());
				responseMap.put(ERROR, list);
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				return fileFormatDownloadService.convertToJsonString(responseMap);
			} catch (Exception ex) {
				BaseLoggers.exceptionLogger.error(ex.getMessage(), ex);
				msg = new Message();
				msg.setI18nCode(FileUploadDownloadConstants.INVALID_FILE_DETAILS);
				List<MessageOutput> list = getWebMessageList(Arrays.asList(msg));
				responseMap.put(ERROR, list);
				return fileFormatDownloadService.convertToJsonString(responseMap);
			}

		}

		if (hasElements(fileExtensionErrormessageList)) {
			responseMap.put(ERROR, getWebMessageList(fileExtensionErrormessageList));
		} else {
			List<MessageOutput> list = getWebMessageList(messageList);
			responseMap.put("success", list);
			responseMap.put("processingMode", uploadProcessingModeType.getCode());
			if (batchId != null) {
				responseMap.put("batchId", batchId);
			}
		}
		return fileFormatDownloadService.convertToJsonString(responseMap);

	}

	public List<Message> validateFileExtensionForUpload(FcUploadVO uploadVO) {
		String fileName = uploadVO.getFileData().getFileItem().getName();
		List<Message> messageList = null;
		if (StringUtils.isNotBlank(fileName)) {
			int lastIndexOf = fileName.lastIndexOf('.');
			String extension = lastIndexOf > -1 ? fileName.substring(lastIndexOf + 1) : null;
			if (StringUtils.isNotBlank(extension) && !Arrays
					.asList(FILE_EXTENSION_CSV, FILE_EXTENSION_XLS, FILE_EXTENSION_XLSX).contains(extension)) {
				messageList = new ArrayList<>();
				Message msg = CoreUtility.prepareMessage(FILE_EXTENSION_INVALID, extension,
						FILE_EXTENSION_CSV_OR_EXCEL);
				messageList.add(msg);
			}
		}
		return messageList;
	}

	protected FileUploadDownloadProcessDefinition getProcessDefinition(String processName) {
		FileUploadDownloadProcessDefinition fileUploadDownloadProcessDefinition = new FileUploadDownloadProcessDefinition();
		fileUploadDownloadProcessDefinition.setProcessName(processName);
		fileUploadDownloadProcessDefinition.setStatus(FileUploadDownloadConstants.STATUS_ACTIVE_INACTIVE);
		return fileConsolidatorService.getProcessDefinition(fileUploadDownloadProcessDefinition);
	}

	protected List<FileUploadDownloadUserFormat> fetchUserFormats(Long processId) {
		return fileConsolidatorService.getAllUserFormats(processId);
	}

	public List<MessageOutput> getWebMessageList(List<Message> messageList) {
		List<MessageOutput> webMessageList = new ArrayList<>();
		MessageOutput messageOutput;
		try {
			if (hasElements(messageList)) {
				for (Message message : messageList) {
					String i18Value = messageSource.getMessage(message.getI18nCode(), message.getMessageArguments(),
							message.getI18nCode(), neutrinoExecutionContextHolder.getDefaultLocale());
					i18Value = message.getI18nCode() + " : " + i18Value;
					messageOutput = new MessageOutput(message, i18Value);
					webMessageList.add(messageOutput);
				}
			}
		} catch (Exception exception) {
			BaseLoggers.exceptionLogger.error(exception.getMessage(), exception);
		}
		return webMessageList;
	}

	@RequestMapping(value = "/downloadFileFormat", method = RequestMethod.GET)
	@ResponseBody
	public String downloadFileFormat(Long formatId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> responseMap = new HashMap<>();
		if (isNull(formatId)) {
			throw ExceptionBuilder.getInstance(BusinessException.class, USER_FORMAT_IS_INVALID, INVALID_FORMAT_ID)
					.build();
		}
		try {
			FileFormatDownloadResponseVo downloadResponseValues = fileFormatDownloadService
					.getFormatDownloadContent(formatId);
			if (!isNull(downloadResponseValues)) {
				response.addHeader("Content-Disposition", downloadResponseValues.getHeaderContentDispositionValue());
				response.setContentType(downloadResponseValues.getContentType());
				response.setContentLength(downloadResponseValues.getResponseByteArray().length);
				response.getOutputStream().write(downloadResponseValues.getResponseByteArray());
			}
		} catch (BaseException e) {
			responseMap.put(ERROR, getWebMessageList(e.getMessages()));
			BaseLoggers.exceptionLogger.error(e.getMessage(), e);
			return fileFormatDownloadService.convertToJsonString(responseMap);
		} catch (Exception ex) {
			BaseLoggers.exceptionLogger.error(ex.getMessage(), ex);
			Message msg = new Message();
			msg.setI18nCode(UPLOAD_DOWNLOAD_FORMAT_FILE_EXCEPTION);
			responseMap.put(ERROR, getWebMessageList(Arrays.asList(msg)));
			return fileFormatDownloadService.convertToJsonString(responseMap);
		} finally {
			try {
				if (notNull(response.getOutputStream()))
					IOUtils.closeQuietly(response.getOutputStream());
			} catch (IOException iOException) {
				BaseLoggers.exceptionLogger.error(iOException.getMessage(), iOException);
			}
		}
		return fileFormatDownloadService.convertToJsonString(responseMap);
	}
}
