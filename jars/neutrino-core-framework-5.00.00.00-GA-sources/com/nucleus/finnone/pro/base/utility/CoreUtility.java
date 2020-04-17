/**
 * 
 */
package com.nucleus.finnone.pro.base.utility;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.security.BlackListPatternHolder;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.validation.domainobject.ValidationRuleResult;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.core.session.NeutrinoMapSessionRepository;
import com.nucleus.standard.context.NeutrinoExecutionContextHolder;
import com.nucleus.user.UserInfo;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;


/**
 * @author Vivekanand.Jha
 *
 */
@Named("coreUtility")
public class CoreUtility {

	public static String CACHE_MODE_REDIS="cache-mode-redis";
	public static String CACHE_MODE_SENTINEL="redis-sentinel";
	public static String CACHE_MODE_INFINISPAN="cache-mode-infinispan";
	private static String OPENING_BRACES="(";
	private static String CLOSING_BRACES=")";
	private static final String ASCENDING="asc";	
	public static final Comparator<GenericParameter> GENERIC_PARAMETER_COMPARATAOR_BY_VALUE_ASC = new GenericParameterComparatorByValue(ASCENDING);				//CAS and LMS synchronization changes for Generic Parameter

	@Autowired
	private Environment environment;

	private String cacheMode;
	private String serverNodeId;
	private Boolean isApiManagerEnabled = null;
	
	private static final String UUID_SEPARATOR ="-";
	
	/**
	 * static method to add list of Validation Rule Result in another List after
	 * checking null and size
	 * 
	 * @param mainList
	 * @param listToBeAdded
	 * @return
	 */
	public static List addAllToList(List mainList, List listToBeAdded) {
		if (listToBeAdded == null || listToBeAdded.size() == 0) {
			return mainList;
		} else {
			mainList.addAll(listToBeAdded);
		}
		return mainList;
	
	}

	public static List addToList(List mainList, Object toBeAdded) {
		if (toBeAdded == null) {
			return mainList;
		}
		mainList.add(toBeAdded);
		return mainList;
	}
	
	public static <T> List<T> addObjectToList(List<T> mainList, T toBeAdded) {
		if (toBeAdded == null) {
			return mainList;
		}
		mainList.add(toBeAdded);
		return mainList;
	}

	


	


	public static Message prepareMessage(String i18nCode, MessageType messageType, String... arguments) {
		return new Message(i18nCode, messageType, arguments);
	}

	public static Message prepareMessage(String i18nCode, String... arguments) {
		Message message = new Message();
		message.setI18nCode(i18nCode);
		message.setMessageArguments(arguments);
		return message;
	}

	public static BigDecimal replaceNullWithDefaulValue(BigDecimal value, BigDecimal defaultValue) {
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	public static Integer replaceNullWithZero(Integer value) {
		if (value != null) {
			return value;
		} else {
			return 0;
		}
	}
	
	public static Long replaceNullWithZero(Long value) {
		if (value != null) {
			return value;
		} else {
			return 0L;
		}
	}
	public static BigDecimal replaceNullWithZero(BigDecimal value) {
		if (value != null) {
			return value;
		} else {
			return  BigDecimal.ZERO;
		}
	}

	/**
	 * Method to return zero in case of NULL
	 * 
	 * @param number
	 * @return number
	 */
	public static <T extends Number> T replaceNullWithZero(T number) {
		return (T) (number == null ? BigDecimal.ZERO : number);
	}

	public static Boolean validateNullAndBlank(final String requestString) {
		return (requestString != null && !"".equals(requestString));
	}
	


	
	
	
	
	
	
	
	public static String getUniqueId() {
		return String.valueOf(System.nanoTime()).concat(UUID_SEPARATOR).concat(String.valueOf(Math.random()));
	}
	
	
	
	public static String removeCommaFromString(String str){
		return str.replaceAll("(?<=\\d),(?=\\d)|\\$", "");
	}
	
	
	
	
	
	
	
	/**
	 * This method add all elements of setToBeAdded in the mainSet. If setToBeAdded is empty than mainSet will be return.
	 * @param mainSet
	 * @param setToBeAdded
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set addAllToSet(Set mainSet, Set setToBeAdded) {
		if (setToBeAdded.isEmpty()) {
			return mainSet;
		} else {
		    mainSet.addAll(setToBeAdded);
		}
		return mainSet;
	
	}

	/**
	 * This method add element toBeAdded in mainSet. If element is null than no operation will be performed and mainSet will be return.
	 * @param mainSet
	 * @param toBeAdded
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set addToSet(Set mainSet, Object toBeAdded) {
		if (toBeAdded == null) {
			return mainSet;
		}
		mainSet.add(toBeAdded);
		return mainSet;
	}
	
	/**
	 * This method add toBeAdded object of type <T> in the mainSet of type<T>. If toBeAdded object is null than no operation will be performed and mainSet will be return.
	 * @param mainSet
	 * @param toBeAdded
	 * @return
	 */
	public static <T> Set<T> addObjectToSet(Set<T> mainSet, T toBeAdded) {
		if (toBeAdded == null) {
			return mainSet;
		}
		mainSet.add(toBeAdded);
		return mainSet;
	}
	
	private static class GenericParameterComparatorByValue implements Comparator<GenericParameter> , Serializable{
		
		public GenericParameterComparatorByValue(String sortDirection){
			this.sortDirection = sortDirection;
		}
		private static final long serialVersionUID = 1L;
		private String sortDirection;
		public int compare(GenericParameter s1, GenericParameter s2){
			if(sortDirection.equals(ASCENDING)){
				return s1.getName().compareTo(s2.getName());
			}else{
				return s2.getName().compareTo(s1.getName());
			}
		}
	
	}
	
	public static UserInfo getUserDetails()
	{
		NeutrinoExecutionContextHolder	neutrinoExecutionContextHolder=NeutrinoSpringAppContextUtil.getBeanByName("neutrinoExecutionContextHolder", NeutrinoExecutionContextHolder.class);
        UserInfo userInfo = neutrinoExecutionContextHolder.getLoggedInUserDetails();
        return userInfo;
	}

	public static void syncSecurityContextHolderInSession(String sessionId)
	{
		if(StringUtils.isNotEmpty(sessionId)){
			NeutrinoMapSessionRepository sessionRepository = NeutrinoSpringAppContextUtil.getBeanByName("sessionRepository", NeutrinoMapSessionRepository.class);
			sessionRepository.syncSessionAttribute(sessionId, HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
		}
	}
	
	
	public static Date getBusinessDate()
	{
		NeutrinoExecutionContextHolder	neutrinoExecutionContextHolder=NeutrinoSpringAppContextUtil.getBeanByName("neutrinoExecutionContextHolder", NeutrinoExecutionContextHolder.class);
        return neutrinoExecutionContextHolder.getBusinessDate();
	}
	
	public static String getUserUri() {
		 UserInfo userInfo=getUserDetails();
        if(userInfo!=null){
        	return userInfo.getUserReference().getUri();
        }
        return null;
    }
	 public static void setBaseEntityDefaultMakerData(BaseEntity baseEntity, Date processDate) {
		    baseEntity.getEntityLifeCycleData().setSystemModifiableOnly(true);
		    baseEntity.getEntityLifeCycleData().setSnapshotRecord(false);
		    baseEntity.getEntityLifeCycleData().setPersistenceStatus(PersistenceStatus.INACTIVE);
		    baseEntity.getEntityLifeCycleData().setCreatedByUri(getUserUri());
		    baseEntity.getEntityLifeCycleData().setUuid(baseEntity.getUuid());
		    baseEntity.setMakeBusinessDate(processDate);
		  }
	 public static ValidationRuleResult prepareValidationRuleResult(String i18nCode, String... arguments) {
		    ValidationRuleResult validationRuleResult = new ValidationRuleResult();
		    Message message = new Message();
		    message.setI18nCode(i18nCode);
		    message.setMessageArguments(arguments);
		    validationRuleResult.setI18message(message);
		    return validationRuleResult;
		  }
	 public static String replaceNullWithBlank(String inputString) {
		    return inputString == null ? "" : inputString;
		  }
	 public static List<ValidationRuleResult> addToList(List<ValidationRuleResult> mainList, ValidationRuleResult toBeAdded) {
		    if (toBeAdded == null) {
		      return mainList;
		    }
		    mainList.add(toBeAdded);
		    return mainList;
		  }
	 public static List<Message> prepareMessages(List<ValidationRuleResult> validationRuleResults) {
		    List<Message> validationMessages = new ArrayList<Message>();
		    if (hasElements(validationRuleResults)) {
		      
		      for (ValidationRuleResult validationRuleResult : validationRuleResults) {
		        validationMessages.add(validationRuleResult.getI18message());
		      }
		      
		    }
		    return validationMessages;
		  }
	 public static void handleValidationExceptions(List<ValidationRuleResult> validationRuleResults, String i18ValidationCode, String exceptionMessage) {
		    if (hasElements(validationRuleResults)) {
		      List<Message> validationMessages = new ArrayList<Message>();
		      for (ValidationRuleResult validationRuleResult : validationRuleResults) {
		        validationMessages.add(validationRuleResult.getI18message());
		      }
		      throw ExceptionBuilder.getInstance(BusinessException.class, i18ValidationCode, exceptionMessage)
		      .setMessages(validationMessages)
		      .setSeverity(ExceptionSeverityEnum.SEVERITY_MEDIUM.getEnumValue()).build();
		    }
		  }
	
	  public static String sanitize(String inputString) {
		  int charAdded=0; 
		  if(org.apache.commons.lang3.StringUtils.isBlank(inputString)) 
		  {
			  return inputString;
		  }
		  Map<String, Pattern> patternsMap =  BlackListPatternHolder.getCsvBlackListPattern();
		  Pattern patternWithOutComma=patternsMap.get(BlackListPatternHolder.CSV_FORMULA_PATTERN_WITHOUT_COMMA_CODE);
		  if(notNull(patternWithOutComma))
		  {

			  if(patternWithOutComma.matcher(inputString).lookingAt())
			  {

				  inputString=	new StringBuilder("'").append(inputString).toString();
			  }

		  }
		  //   Pattern patternWithComma=patternsMap.get(BlackListPatternHolder.CSV_FORMULA_PATTERN_WITH_COMMA_CODE);
		  Pattern patternWithComma=   Pattern.compile("[,|][\\s]*[+=-][\\s]*[a-zA-Z]+");
		  if(notNull(patternWithComma))
		  {



			  Matcher m=   patternWithComma.matcher(inputString);
			  while(m.find())
			  {
				  int start=m.start();
				  int end=m.end();
				  String matchedString=m.group();
				  inputString=	new StringBuilder(inputString).replace(start+1+charAdded, end+charAdded,new StringBuilder("'").append( matchedString.substring(1)).toString()).toString();
				  charAdded++;
			  } 

		  }


		  return inputString;
	  }
	  
	  /**
	   * Returns true/false if SSO profile is enabled 
	   * if no active profile found as SSO, this will validate the default profiles
	   * @return true/false
	   * 
	   * */
	  
	  public boolean isSsoEnabled(){
		  String [] activeProfiles = environment.getActiveProfiles();
		  if(activeProfiles.length == 0){
			  activeProfiles = environment.getDefaultProfiles();
		  }
		  return (activeProfiles != null) && (activeProfiles.length > 0) && (Arrays.asList(activeProfiles).contains("sso"));
	  }
	


	public boolean isApiManagerEnabled() {
		if (isApiManagerEnabled == null) {
			String[] defaultProfiles = environment.getDefaultProfiles();
			String[] activeProfiles = environment.getActiveProfiles();

			// check if profile is api-manager-enabled then this filter is not required.
			if (defaultProfiles != null && defaultProfiles.length > 0
					&& (Arrays.asList(defaultProfiles).contains("api-manager-enabled"))
					|| (activeProfiles != null && activeProfiles.length > 0
							&& (Arrays.asList(activeProfiles).contains("api-manager-enabled")))) {
				isApiManagerEnabled = Boolean.TRUE;
			} else {
				isApiManagerEnabled = Boolean.FALSE;
			}
		}
		return isApiManagerEnabled;
	}

	public String getCacheMode() {
		if (StringUtils.isEmpty(cacheMode)) {
			String[] activeProfiles = environment.getActiveProfiles();
			String mode = CACHE_MODE_INFINISPAN;
			if (activeProfiles.length == 0) {
				activeProfiles = environment.getDefaultProfiles();
			}

			if ((activeProfiles != null) && (activeProfiles.length > 0)) {
				List<String> list = Arrays.asList(activeProfiles);
				if (list.contains(CACHE_MODE_REDIS)) {
					mode = CACHE_MODE_REDIS;
					if (list.contains(CACHE_MODE_SENTINEL)) {
						mode = CACHE_MODE_SENTINEL;
					}
				}
			}
			cacheMode = mode;

		}

		return cacheMode;
	}

	public String getServerNodeId() {
		if (StringUtils.isEmpty(serverNodeId)) {
			StringBuilder nodeId = new StringBuilder(ProductInformationLoader.getProductCode());
			nodeId.append("_");
			try {
				InetAddress inetaddress = InetAddress.getLocalHost();
				String ip = inetaddress.getHostAddress();
				String[] ipFields = ip.split("\\.");
				for (int i = ipFields.length - 1; i >= 0; i--) {
					nodeId.append(Integer.parseInt(ipFields[i]) * 353);
					if (i != 0) {
						nodeId.append("#");
					}
				}
				nodeId.append("#");
				nodeId.append(RandomStringUtils.randomAlphabetic(4));

			} catch (UnknownHostException e) {
				BaseLoggers.exceptionLogger.error("Server IP could not be traced", e);
			}
			serverNodeId = nodeId.toString();
			BaseLoggers.flowLogger.info("Server Node ID : " + serverNodeId);
		}
		return serverNodeId;
	}

	}







