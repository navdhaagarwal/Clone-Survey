package com.nucleus.web.common.controller;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nucleus.rules.exception.RuleException;
import org.activiti.engine.task.Task;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.security.web.util.ThrowableCauseExtractor;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.exceptions.InvalidDataException;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.core.team.entity.Team;
import com.nucleus.core.team.service.TeamService;
import com.nucleus.exceptionLogging.ExceptionLoggingService;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.WorkflowSuspendException;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.lms.web.common.MessageOutput;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;

public class NonTransactionalBaseController {

	@Autowired
	protected MessageSource messageSource;

	
	@Inject
	@Named("baseMasterService")
	protected BaseMasterService baseMasterService;

	@Inject
	@Named("userService")
	private UserService userService;

	@Inject
	@Named("exceptionLoggingService")
	protected ExceptionLoggingService exceptionLoggingService;

	private final ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

	protected final String QUERYACCESSDENIED = "accessDenied";

	@Inject
	@Named("teamService")
	protected TeamService teamService;
	private String mdcContextKey = "UUID";

	@ExceptionHandler(AccessDeniedException.class)
	public String handleAccessDeniedException(ModelMap map, AccessDeniedException ase, HttpServletRequest request) {
		map.put("msg", "label.resource.access.denied");
    	return "httpError";
	}

	@ExceptionHandler({WorkflowSuspendException.class})
	public String handleWorkflowSuspendException(WorkflowSuspendException wse, HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.getWriter().append(wse.getMessage());
		response.addHeader("message", wse.getMessage());
		response.sendError(204, wse.getMessage());
		return null;
	}

	@ExceptionHandler(Exception.class)
	public String getExceptionPage(Exception e, HttpServletRequest request, HttpServletResponse res)
			throws IOException {
		BaseLoggers.exceptionLogger.error("Exception in Base Controller", e);
		try {
			UserInfo loggedInUser = getUserDetails();
			exceptionLoggingService.saveDebuggingDataOfException(loggedInUser, request, e);
		} catch (Exception t) {
			BaseLoggers.exceptionLogger.error("Exception occured while saving exception data", t);
		}
		if (isAjax(request)) {

			BaseLoggers.flowLogger.error(e.toString());
			// changes done for internal server error. A proper message is
			// displayed in place of internal server error.
			String transactionId = StringUtils.hasText(MDC.get(mdcContextKey)) ? MDC.get(mdcContextKey)
					: "CASTXN-ID_NOT_AVAILABLE";



			if (transactionId != null && transactionId.trim().length() > 0) {
				if(e instanceof RuleException)
				{
					res.getWriter().append("Error occured while evaluating Rules.");
					res.addHeader("message", "Please find the transaction Id : "+ transactionId+".");
					res.sendError(403,"Error occured while evaluating Rules.Check Rule Execution Set for error Log."+"Please find the transaction Id : "+ transactionId+".");
					return null;
				}else {
				res.sendError(500,
						"Some error has occured. Please contact System Administrator. Please share this transaction Id "
								+ transactionId);
				}
			}
			else
				res.sendError(500, "Some error has occured. Please contact System Administrator");

			// ModelAndView model = new
			// ModelAndView("forward:/app/webExceptionHandler/ajaxErrorRedirectPage");
			// request.setAttribute("errorMessageObject", e.toString());
			return null;

		} else {
			// This is done to avoid forward to WebExceptionHandler which was
			// leading to stackoverflow.
			// refer jira CAS-18797 for more details.
			Exception exception = null;
			if (e instanceof TransactionSystemException) {
				TransactionSystemException systemException = (TransactionSystemException) e;
				if (systemException.getApplicationException() != null) {
					exception = (Exception) systemException.getApplicationException();
					BaseLoggers.exceptionLogger.error("Exception in BaseController " + e);
				}
			} else if (e instanceof InvocationTargetException) {
				InvocationTargetException targetException = (InvocationTargetException) e;
				if (targetException.getTargetException() != null) {
					exception = (Exception) targetException.getTargetException();
					BaseLoggers.exceptionLogger.error("Exception in BaseController " + exception);
				}

			} else {
				exception = e;
			}

			if (exception instanceof InvalidDataException) {
				request.setAttribute("errorMessageCode", ((InvalidDataException) exception).getI18nCode());
			}
			request.setAttribute("errorMessageObject", exception.toString());
			request.setAttribute("errorMessage", "Internal Server Error");
			return "error";
		}

		/* return errorModelAndView(e); */
	}

	private static boolean isAjax(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}

	/**
	 * @param
	 * @return String
	 * @throws @description
	 *             For returning Loggedin userName
	 */
	public String getUsername() {
		if (SecurityContextHolder.getContext() != null) {
			return SecurityContextHolder.getContext().getAuthentication().getName();
		}
		return "Guest";
	}

	/**
	 * @param
	 * @return UserInfo
	 * @throws @description
	 *             For returning Loggedin UserInfo object
	 */
	public UserInfo getUserDetails() {
		UserInfo userInfo = null;
		SecurityContext securityContext = SecurityContextHolder.getContext();
		if (securityContext != null && securityContext.getAuthentication()!=null) {
			Object principal = securityContext.getAuthentication().getPrincipal();
			if (UserInfo.class.isAssignableFrom(principal.getClass())) {
				userInfo = (UserInfo) principal;
			}
		}
		return userInfo;
	}

	/**
	 * This Method retrieves user's selected date format from user preferences
	 */
	public String getUserDateFormat() {
		String pattern = "dd/MM/yyyy";
		if (getUserDetails() != null && getUserDetails().getUserPreferences() != null) {
			ConfigurationVO confVO = getUserDetails().getUserPreferences().get("config.date.formats");
			if (confVO != null) {
				pattern = confVO.getText();
			}
		}
		return pattern;

	}

	/**
	 * This Method retrieves user's selected date format from user preferences
	 */
	public Locale getUserLocale() {
		Locale locale = null;
		UserInfo ui = getUserDetails();
		if (ui != null && !"anonymous".equals(ui.getUsername())) {
			ConfigurationVO preferences = ui.getUserPreferences().get("config.user.locale");
			String[] localeString = preferences.getText().split("_");
			if (localeString.length >= 2) {
				locale = new Locale(localeString[0], localeString[1]);
			}
		}
		return locale;
	}

	/**
	 * This Method retrieves user's selected date and time format from user
	 * preferences
	 */
	public String getUserDateTimeFormat() {
		return getUserDateFormat() + " " + getAppTimeFormat();

	}

	public String getAppTimeFormat() {
		return DateUtils.DEFAULT_TIME_FORMAT;
	}

	public List<String> checkValidationForDuplicates(BaseMasterEntity baseMasterEntity,
			Class<? extends BaseMasterEntity> entityClass, Map<String, Object> validateMap) {
		List<String> colNameList;
		if (baseMasterEntity.getId() == null) { // Duplicate Code and Name
												// Validation , By Sending the
												// code and Name in a
												// map(key , value) respectively
			colNameList = baseMasterService.hasEntity(entityClass, validateMap);
		} else // Code to check as if any approved state is being modified into
				// another approved record
		{
			colNameList = baseMasterService.getDuplicateColumnNames(entityClass, validateMap, baseMasterEntity.getId());
		}
		return colNameList;
	}

	public String getFormattedDate(DateTime dateTime) {
		DateTimeFormatter format = DateTimeFormat.forPattern(getUserDateFormat());
		return format.print(dateTime);

	}

	public String getFormattedDateTime(DateTime dateTime) {
		DateTimeFormatter format = DateTimeFormat.forPattern(getUserDateTimeFormat());
		return format.print(dateTime);

	}

	public String getFormattedTime(DateTime dateTime) {
		DateTimeFormatter format = DateTimeFormat.forPattern(getAppTimeFormat());
		return format.print(dateTime);

	}

	public DateTime parseDateTime(String dateTime) throws ParseException {
		return userService.parseDateTime(dateTime);

	}

	/*
	 * public List<String>
	 * checkValidationForDuplicatesInPropertyMasters(PropertyMasterEntity
	 * propertyMasterEntity, Class<? extends PropertyMasterEntity> entityClass,
	 * Map<String, Object> validateMap) { List<String> colNameList; if
	 * (propertyMasterEntity.getId() == null) { // Duplicate Code and Name
	 * Validation , By Sending the code and Name in a // map(key , value)
	 * respectively colNameList = propertyMasterService.hasEntity(entityClass,
	 * validateMap); } else // Code to check as if any approved state is being
	 * modified into another approved record { colNameList =
	 * propertyMasterService.getDuplicateColumnNames(entityClass, validateMap,
	 * propertyMasterEntity.getId()); } return colNameList; }
	 */

	public List<String> checkValidationForDuplicateChildEntities(BaseMasterEntity baseMasterEntity,
			Class<? extends BaseMasterEntity> entityClass, Map<String, Object> validateMap) {
		List<String> colNameList = baseMasterService.hasEntityCheckForChildEntity(entityClass, validateMap);

		return colNameList;
	}

	public boolean userCheck(Task task, String stageName, UserInfo user, boolean userCheck) {

		if (stageName != null && user != null && "DISBURSAL_AUTHOR".equalsIgnoreCase(stageName)) 
		{
			List<Team> userAssociatedTeams=teamService.getTeamsAssociatedToUserByUserId(user.getId());
			if(ValidatorUtils.hasElements(userAssociatedTeams))
			{
				return false;
			}
		}

		return userCheck;
	}

	private static final class DefaultThrowableAnalyzer extends ThrowableAnalyzer {
		/**
		 * @see org.springframework.security.web.util.ThrowableAnalyzer#initExtractorMap()
		 */
		@Override
		protected void initExtractorMap() {
			super.initExtractorMap();

			registerExtractor(ServletException.class, new ThrowableCauseExtractor() {
				@Override
				public Throwable extractCause(Throwable throwable) {
					ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
					return ((ServletException) throwable).getRootCause();
				}
			});
		}

	}

	/**
	 * This method convert object passed to JSON string.
	 * 
	 * @param object
	 * @return
	 */
	protected String convertToJSONString(Object object) {

		String jsonString = null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			jsonString = mapper.writeValueAsString(object);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception:" + e.getMessage(), e);
		}
		return jsonString;
	}

	/**
	 * Helper method to create list of output based on single i18n key
	 * 
	 * @param i18nCode
	 * @param messageType
	 * @param request
	 * @param str
	 * @return
	 */
	protected MessageOutput prepareMessageOutputs(String i18nCode, Message.MessageType messageType,
			HttpServletRequest request, String... str) {
		List<Message> messageList = new ArrayList<>();
		Message msg = new Message(i18nCode, messageType, str);
		messageList.add(msg);
		return getWebMessageList(messageList, request).get(0);
	}

	public List<MessageOutput> getWebMessageList(List<Message> messageList, HttpServletRequest request) {
		List<MessageOutput> webMessageList = new ArrayList<>();
		try {

			MessageOutput messageOutput;
			for (Message message : messageList) {
				String i18Value = messageSource.getMessage(message.getI18nCode(), message.getMessageArguments(),
						message.getI18nCode(), RequestContextUtils.getLocale(request));
				i18Value = message.getI18nCode() + " : " + i18Value;
				messageOutput = new MessageOutput(message, i18Value);
				webMessageList.add(messageOutput);
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("exception:", e);
		}
		return webMessageList;
	}

	public List<String> getWebMessageValuesList(List<Message> messageList, HttpServletRequest request)
	{
		List<String> webMessageList;
		webMessageList = new ArrayList<>();
		try{
			for (Message message : messageList)
			{
				String i18Value = messageSource.getMessage(message.getI18nCode(),message.getMessageArguments(),message.getI18nCode(), RequestContextUtils.getLocale(request));
				webMessageList.add(i18Value);
			}
		}catch(Exception exception){
			BaseLoggers.exceptionLogger.error(exception.getMessage(), exception);
		}
		return webMessageList;
	}
	
	protected String messasgeFromMessageResource(String messageKey) {
		return messageSource.getMessage(messageKey, null, getUserLocale());
	}
}
