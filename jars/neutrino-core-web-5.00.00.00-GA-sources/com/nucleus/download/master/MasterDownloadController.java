package com.nucleus.download.master;

import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.CONTENT_TYPE_VND_EXCEL;
import static com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants.ERROR;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.nucleus.team.teamUploadDownloadService.TeamUploadDownloadUtility;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.download.master.rule.RuleUploadDownloadUtility;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.fileconsolidator.constants.MasterDataDownloadConstants;
import com.nucleus.finnone.pro.fileconsolidator.serviceinterface.IMasterDownloadService;
import com.nucleus.finnone.pro.fileconsolidator.vo.FileUploadDownloadContentVO;
import com.nucleus.finnone.pro.fileconsolidator.vo.MasterDownloadVO;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.Condition;
import com.nucleus.rules.model.Parameter;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.service.ExpressionBuilder;
import com.nucleus.rules.service.ParameterService;
import com.nucleus.rules.service.RuleService;
import com.nucleus.rules.service.SQLRuleExecutor;
import com.nucleus.web.common.controller.NonTransactionalBaseController;

/**
 * @author rohit.singh
 *
 */
@Controller
@RequestMapping(value = "/masterDownload")
public class MasterDownloadController extends NonTransactionalBaseController{

	@Inject
	@Named("masterDownloadService")
	private IMasterDownloadService masterDownloadService;

	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	@Inject
	@Named(value = "expressionBuilder")
	private ExpressionBuilder expressionBuilder;

	@Inject
	@Named("ruleService")
	private RuleService ruleService;

	@Inject
	@Named("sQLRuleExecutor")
	SQLRuleExecutor sqlRuleExecutor;

	@Inject
	@Named("parameterService")
	private ParameterService parameterService;

	@Inject
	@Named("ruleUploadDownloadUtility")
	private RuleUploadDownloadUtility ruleUploadDownload;

	@Inject
	@Named("teamUploadDownloadUtility")
	private TeamUploadDownloadUtility teamUploadDownload;

	/**
	 * @param masterDownloadVO
	 * @param model
	 * @param response
	 * @param request
	 * @return
	 */
	@RequestMapping(method = { RequestMethod.POST }, value = "/download")
	@ResponseBody
	public String downloadMasterDataFile(MasterDownloadVO masterDownloadVO, ModelMap model,
			HttpServletResponse response, HttpServletRequest request) {
		Map<String, Object> responseMap = new HashMap<>();
		try {
			callToDownloadMasterData(masterDownloadVO, response, model);
		} catch (BusinessException be) {
			BaseLoggers.exceptionLogger.error("Business Exception in downloading file:", be);
			responseMap.put(ERROR, prepareMessageOutputs(be.getExceptionCode(), MessageType.ERROR, request, null));
			return convertToJSONString(responseMap);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception in downloading file:", e);
			responseMap.put(ERROR, "Exception in downloading file");
			return convertToJSONString(responseMap);
		}
		
		return convertToJSONString(responseMap);
	}

	/**
	 * @param masterDownloadVO
	 * @param response
	 * @param model
	 */
	private void callToDownloadMasterData(MasterDownloadVO masterDownloadVO, HttpServletResponse response,
			ModelMap model) {

		MasterDownloadVO updatedMasterDownloadVO = masterDownloadService.getDownloadedContent(masterDownloadVO);

		if (notNull(updatedMasterDownloadVO.getFileUploadDownloadInformation())
				&& notNull(updatedMasterDownloadVO.getFileUploadDownloadContentVO())) {
			sendDownloadedFileToBrowser(updatedMasterDownloadVO.getFileUploadDownloadContentVO(),
					updatedMasterDownloadVO.getFileUploadDownloadInformation().getOriginalFileName(), response, model);
		}

	}

	/**
	 * @param fileUploadDownloadContentVO
	 * @param fileName
	 * @param response
	 * @param model
	 */
	private void sendDownloadedFileToBrowser(FileUploadDownloadContentVO fileUploadDownloadContentVO, String fileName,
			HttpServletResponse response, ModelMap model) {

		if (notNull(fileUploadDownloadContentVO.getFileContent())
				&& fileUploadDownloadContentVO.getFileContent().length > 0) {
			OutputStream out;
			if (notNull(model)) {
				model.addAttribute("result", fileUploadDownloadContentVO.getFileContent());
			}
			response.addHeader("Content-Disposition", " attachment;filename=" + fileName);
			response.setHeader("Set-Cookie", "fileDownload=true; path=/");
			response.setContentType (CONTENT_TYPE_VND_EXCEL);
			response.setContentLength((fileUploadDownloadContentVO.getFileContent()).length);
			try {
				out = response.getOutputStream();
				out.write((byte[]) fileUploadDownloadContentVO.getFileContent());
				out.close();
			} catch (IOException e) {
				BaseLoggers.exceptionLogger.error(e.getMessage(), e);
				throw ExceptionBuilder
						.getInstance(BusinessException.class, MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD,
								"IO error while downloading file ")
						.setMessage(CoreUtility.prepareMessage(MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD))
						.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
			}
		}
	}


	@RequestMapping(value = "/downloadConditionMapping/{masterIds}")
	public @ResponseBody
	HttpEntity<byte[]> generateXLSConditionData(@PathVariable("masterIds") List<Long> masterIds, 
			HttpServletRequest request, HttpSession session, ModelMap map,@RequestParam("format") String format) throws Exception {


		List<Condition> conditionMappingMasterList = ruleUploadDownload.getRecordsByMasterIdsforDownloadCond(masterIds);

		try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
			ByteArrayOutputStream bos =new ByteArrayOutputStream()){
			Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
			sheet.setDefaultColumnWidth(30);
			Row header = sheet.createRow(0);


			ruleUploadDownload.createHeaderForCondition(header);

			ruleUploadDownload.createDataForCondition(conditionMappingMasterList,sheet);


			outputEntitiesWorkBook.write(bos);
			byte[] bytes = bos.toByteArray();

			MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(mediaType);
			responseHeaders.setContentDispositionFormData("attachment", "DownloadCondition"+"."+format);
			HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(bytes, responseHeaders);
			return fileEntity;
		} catch (Exception e){
			BaseLoggers.exceptionLogger.error("Exception in Condition download",e);
		}
		return null;
	}





	@RequestMapping(value = "/downloadConditionTemplate")
	@ResponseBody
	public HttpEntity<byte[]> generateXLSforCondition(@RequestParam("isTemplate") Boolean isTemplate,@RequestParam("format") String format) {
		if(isTemplate) {
			try (Workbook workbook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
				 ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
				Sheet sheet = workbook.createSheet("Download");
				sheet.setDefaultColumnWidth(30);
				Row header = sheet.createRow(0);
				ruleUploadDownload.createHeaderForCondition(header);

				workbook.write(bos);
				byte[] bytes = bos.toByteArray();
				MediaType mediaType = MediaType.parseMediaType("application/octet-stream");
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(mediaType);
				responseHeaders.setContentDispositionFormData("attachment", "ConditionTemplate."+format);
				return new HttpEntity<>(bytes, responseHeaders);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("Exception in condition tempate download",e);
			}
		}
		return null;
	}


	@Transactional(readOnly = true)
	@RequestMapping( value="/downloadAllCondition/" , method = RequestMethod.GET)
	public @ResponseBody
	String downloadCondition(Model model,@RequestParam("format") String format,HttpServletResponse httpServletResponse) throws Exception
	{

		NamedQueryExecutor<Condition> executor = new NamedQueryExecutor<>("condition.findConditionsByApprovalCodes");
		List<Condition> conditionList=entityDao.executeQuery(executor);



		if(CollectionUtils.isNotEmpty(conditionList)) {

			try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
				ByteArrayOutputStream bos =new ByteArrayOutputStream()){
				Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
				sheet.setDefaultColumnWidth(30);
				Row header = sheet.createRow(0);

				ruleUploadDownload.createHeaderForCondition(header);

				ruleUploadDownload.createDataForCondition(conditionList,sheet);


				outputEntitiesWorkBook.write(bos);
				byte[] bytes = bos.toByteArray();
				OutputStream out;

				httpServletResponse.addHeader("Content-Disposition", " attachment;filename=" + "DownloadCondition."+format);
				httpServletResponse.setHeader("Set-Cookie", "fileDownload=true; path=/");
				httpServletResponse.setContentType (CONTENT_TYPE_VND_EXCEL);
				httpServletResponse.setContentLength(bytes.length);
				try {
					out = httpServletResponse.getOutputStream();
					out.write(bytes);
					out.close();
				} catch (IOException e) {
					BaseLoggers.exceptionLogger.error(e.getMessage(), e);
					throw ExceptionBuilder
							.getInstance(BusinessException.class, MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD,
									"IO error while downloading file ")
							.setMessage(CoreUtility.prepareMessage(MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD))
							.setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
				}
				return null;
			}catch (Exception e){
				BaseLoggers.exceptionLogger.error("Exception in download all condition",e);
			}
			return null;
		}
		return null;
	}


	@RequestMapping(value = "/downloadParameterMapping/{masterIds}")
	public @ResponseBody
	HttpEntity<byte[]> generateXLSParameterData(@PathVariable("masterIds") List<Long> masterIds, HttpServletRequest request, HttpSession session, ModelMap map,@RequestParam("format") String format) throws Exception {

		try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
			ByteArrayOutputStream bos =new ByteArrayOutputStream()){
			Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
			sheet.setDefaultColumnWidth(30);
			Row header = sheet.createRow(0);

			ruleUploadDownload.createHeaderForParameter(header);

			ruleUploadDownload.createDataForParameter(masterIds,sheet);


			outputEntitiesWorkBook.write(bos);
			byte[] bytes = bos.toByteArray();

			MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(mediaType);
			responseHeaders.setContentDispositionFormData("attachment", "DownloadParameter"+"."+format);
			HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(bytes, responseHeaders);
			return fileEntity;
		}catch (Exception e){
			BaseLoggers.exceptionLogger.error("Exception in parameter download",e);
		}

		return null;

	}



	@RequestMapping(value = "/downloadParameterTemplate")
	@ResponseBody
	public HttpEntity<byte[]> generateXLSforParameter(@RequestParam("isTemplate") Boolean isTemplate,@RequestParam("format") String format) {
		if(isTemplate) {
			try (Workbook workbook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
				 ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
				Sheet sheet = workbook.createSheet("Download");
				sheet.setDefaultColumnWidth(30);
				Row header = sheet.createRow(0);
				ruleUploadDownload.createHeaderForParameter(header);

				workbook.write(bos);
				byte[] bytes = bos.toByteArray();
				MediaType mediaType = MediaType.parseMediaType("application/octet-stream");
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(mediaType);
				responseHeaders.setContentDispositionFormData("attachment", "ParameterTemplate."+format);
				return new HttpEntity<>(bytes, responseHeaders);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("Exception in parameter tempate download",e);
			}
		}
		return null;
	}


	@Transactional(readOnly = true)
	@RequestMapping( value="/downloadAllParameter/" , method = RequestMethod.GET)
	public @ResponseBody
	String download(Model model,@RequestParam("format") String format,HttpServletResponse httpServletResponse) throws Exception
	{
			try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
				ByteArrayOutputStream bos =new ByteArrayOutputStream()){
				Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
				sheet.setDefaultColumnWidth(30);
				Row header = sheet.createRow(0);

				ruleUploadDownload.createHeaderForParameter(header);

				ruleUploadDownload.createDataForParameter(null,sheet);


				outputEntitiesWorkBook.write(bos);
                byte[] bytes = bos.toByteArray();
                OutputStream out;

                httpServletResponse.addHeader("Content-Disposition", " attachment;filename=" + "DownloadParameter."+format);
                httpServletResponse.setHeader("Set-Cookie", "fileDownload=true; path=/");
                httpServletResponse.setContentType (CONTENT_TYPE_VND_EXCEL);
                httpServletResponse.setContentLength(bytes.length);
                try {
                    out = httpServletResponse.getOutputStream();
                    out.write(bytes);
                    out.close();
                } catch (IOException e) {
                    BaseLoggers.exceptionLogger.error(e.getMessage(), e);
                    throw ExceptionBuilder
                            .getInstance(BusinessException.class, MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD,
                                    "IO error while downloading file ")
                            .setMessage(CoreUtility.prepareMessage(MasterDataDownloadConstants.IO_ERROR_IN_FILE_DOWNLOAD))
                            .setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
                }
				return null;
			}catch (Exception e){
				BaseLoggers.exceptionLogger.error("Exception in download all parameter",e);
			}
			return null;

	}


	@RequestMapping(value = "/downloadRuleMapping/{masterIds}")
	public @ResponseBody
	HttpEntity<byte[]> generateXLSRuleData(@PathVariable("masterIds") List<Long> masterIds, HttpServletRequest request, HttpSession session, ModelMap map,@RequestParam("format") String format) throws Exception {




		try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
			ByteArrayOutputStream bos =new ByteArrayOutputStream()){
			Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
			sheet.setDefaultColumnWidth(30);
			Row header = sheet.createRow(0);

			ruleUploadDownload.createHeaderForRule(header);

			ruleUploadDownload.createDataForRule(masterIds,sheet);

			outputEntitiesWorkBook.write(bos);
			byte[] bytes = bos.toByteArray();

			MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(mediaType);
			responseHeaders.setContentDispositionFormData("attachment", "DownloadRuleMaster."+format);
			HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(bytes, responseHeaders);
			return fileEntity;
		}
		catch (Exception e){
			BaseLoggers.exceptionLogger.error(e.getMessage());
		}
		return null;
	}



	@RequestMapping(value = "/downloadRuleTemplate")
	@ResponseBody
	public HttpEntity<byte[]> generateXLSforRule(@RequestParam("isTemplate") Boolean isTemplate,@RequestParam("format") String format) {
		if(isTemplate) {
			try (Workbook workbook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
				 ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
				Sheet sheet = workbook.createSheet("Download");
				sheet.setDefaultColumnWidth(30);
				Row header = sheet.createRow(0);
				ruleUploadDownload.createHeaderForRule(header);

				workbook.write(bos);
				byte[] bytes = bos.toByteArray();
				MediaType mediaType = MediaType.parseMediaType("application/octet-stream");
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(mediaType);
				responseHeaders.setContentDispositionFormData("attachment", "RuleTemplate."+format);
				return new HttpEntity<>(bytes, responseHeaders);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error(e.getMessage());
			}
		}
		return null;
	}



	@RequestMapping( value="/downloadAllRule/" , method = RequestMethod.GET)
	public @ResponseBody
	HttpEntity<byte[]> downloadRule(Model model,@RequestParam("format") String format) throws Exception
	{

			try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
				ByteArrayOutputStream bos =new ByteArrayOutputStream()){
				Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
				sheet.setDefaultColumnWidth(30);
				Row header = sheet.createRow(0);
				ruleUploadDownload.createHeaderForRule(header);
				ruleUploadDownload.createDataForRule(null,sheet);

				outputEntitiesWorkBook.write(bos);
				byte[] bytes = bos.toByteArray();

				MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(mediaType);
				responseHeaders.setContentDispositionFormData("attachment", "DownloadRuleMaster"+"."+format);
				HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(bytes, responseHeaders);
				return fileEntity;
			}catch (Exception e){
				BaseLoggers.exceptionLogger.error("Excpetion in download all rule",e);
			}
			return null;
	}

	@RequestMapping(value = "/downloadTeamTemplate")
	@ResponseBody
	public HttpEntity<byte[]> generateXLSforTeam(@RequestParam("isTemplate") Boolean isTemplate,@RequestParam("format") String format) {
		if(isTemplate) {
			try (Workbook workbook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
				 ByteArrayOutputStream bos = new ByteArrayOutputStream();) {
				Sheet sheet = workbook.createSheet("Download");
				sheet.setDefaultColumnWidth(30);
				Row header = sheet.createRow(0);
				teamUploadDownload.createHeaderForTeam(header);

				workbook.write(bos);
				byte[] bytes = bos.toByteArray();
				MediaType mediaType = MediaType.parseMediaType("application/octet-stream");
				HttpHeaders responseHeaders = new HttpHeaders();
				responseHeaders.setContentType(mediaType);
				responseHeaders.setContentDispositionFormData("attachment", "TeamTemplate."+format);
				return new HttpEntity<>(bytes, responseHeaders);
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("Exception in Team tempate download",e);
			}
		}
		return null;
	}

	@RequestMapping( value="/downloadAllTeam/" , method = RequestMethod.GET)
	public @ResponseBody
	HttpEntity<byte[]> downloadAllTeam(Model model,@RequestParam("format") String format) throws Exception
	{

		try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
			ByteArrayOutputStream bos =new ByteArrayOutputStream()){
			Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
			sheet.setDefaultColumnWidth(30);
			Row header = sheet.createRow(0);
			teamUploadDownload.createHeaderForTeam(header);
			teamUploadDownload.createDataForTeam(null,sheet);

			outputEntitiesWorkBook.write(bos);
			byte[] bytes = bos.toByteArray();

			MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(mediaType);
			responseHeaders.setContentDispositionFormData("attachment", "DownloadTeamMaster"+"."+format);
			HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(bytes, responseHeaders);
			return fileEntity;
		}catch (Exception e){
			BaseLoggers.exceptionLogger.error("Excpetion in download all team",e);
		}
		return null;
	}

	@RequestMapping(value = "/downloadTeam/{masterIds}")
	public @ResponseBody HttpEntity<byte[]> generateXLSTeamData(@PathVariable("masterIds") List<Long> masterIds,
    HttpServletRequest request, HttpSession session, ModelMap map,@RequestParam("format") String format) throws Exception {

		try(Workbook outputEntitiesWorkBook = (format.equalsIgnoreCase("xlsx")) ? new XSSFWorkbook() : new HSSFWorkbook();
			ByteArrayOutputStream bos =new ByteArrayOutputStream()){
			Sheet sheet = outputEntitiesWorkBook.createSheet("Download");
			sheet.setDefaultColumnWidth(30);
			Row header = sheet.createRow(0);

			teamUploadDownload.createHeaderForTeam(header);

			teamUploadDownload.createDataForTeam(masterIds,sheet);

			outputEntitiesWorkBook.write(bos);
			byte[] bytes = bos.toByteArray();

			MediaType mediaType = MediaType.parseMediaType("application/octet-stream");

			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(mediaType);
			responseHeaders.setContentDispositionFormData("attachment", "DownloadTeamMaster."+format);
			HttpEntity<byte[]> fileEntity = new HttpEntity<byte[]>(bytes, responseHeaders);
			return fileEntity;
		}
		catch (Exception e){
			BaseLoggers.exceptionLogger.error("Excpetion in downloading team",e);
			BaseLoggers.exceptionLogger.error(e.getMessage());
		}
		return null;
	}
	

}
