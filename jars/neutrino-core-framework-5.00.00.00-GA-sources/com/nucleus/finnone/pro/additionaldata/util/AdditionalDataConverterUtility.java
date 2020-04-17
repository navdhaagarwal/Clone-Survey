/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd.
Description: Data conversion utility for Additional Field
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.util;

import java.text.SimpleDateFormat;

import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.additionaldata.constants.AdditionalDataConstants;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalDataMetaData;
import com.nucleus.finnone.pro.additionaldata.domainobject.CustomFieldDataType;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.UserInfo;

@Named("additionalDataConverterUtility")
public class AdditionalDataConverterUtility {
	
	 @Inject
	 @Named("genericParameterService")
	 private GenericParameterService genericParameterService;	
	 
	 @Autowired
	 protected MessageSource messageSource;
	 
	 @Inject
	 @Named("configurationService")
	 private ConfigurationService configurationService; 
	 
	/** 
	 *  Method to convert additional field data according to Additional Data Meta Data to display on UI
	 * @param additionalData 
	 * @param additionalDataMetaData 
	 * @return 
	 * @param requestSerivceContext 
	 */
	public AdditionalData transformServiceToConsumer(AdditionalData additionalData,	List<AdditionalDataMetaData> additionalDataMetaDataList) 
	{
		if(additionalData==null || additionalData.getTransactionType()==null || additionalData.isEmpty()){
			return additionalData;
		}
		
		 String datePattern = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
	                "config.date.formats").getText();
		 
		 Locale locale=getSystemLocale();    
		
		boolean formatDate = false;
		SimpleDateFormat dateFormat = null;
		 SimpleDateFormat requiredDateFormat = new SimpleDateFormat(datePattern);
		if(! AdditionalDataConstants.DATE_FORMAT.equalsIgnoreCase(datePattern)){
			dateFormat = new SimpleDateFormat(AdditionalDataConstants.DATE_FORMAT);
			formatDate = true;
		}
		
		Long dataType = 0L;
		String mappingField = "";
		String fieldName = "";
		String fieldLabelId = "";
		String formattedData = null;
	    String unFormattedData = null;
	    Object fieldVlaue=null;
	    for(AdditionalDataMetaData additionalDataMetaData:additionalDataMetaDataList){
			try {
				dataType = additionalDataMetaData.getDataType();
				mappingField = additionalDataMetaData.getMappingField();
				fieldName = additionalDataMetaData.getFieldName();
				fieldLabelId = additionalDataMetaData.getFieldLabelId();
				fieldVlaue = PropertyUtils.getProperty(additionalData,mappingField);
				if(fieldVlaue==null){
					PropertyUtils.setProperty(additionalData, mappingField, "");
					continue;
				}
				unFormattedData = fieldVlaue.toString();
				GenericParameter genericParameter=genericParameterService.findByCode(CustomFieldDataType.DATE,CustomFieldDataType.class);
				//A	Alphanumeric,	N	Amount (Numeric),	D	Date,	I	Integer,	R  Rate			
				/*if(genericParameter!=null)
				{*/
					if((genericParameter!=null) && (formatDate && dataType.equals(genericParameter.getId()))){
						java.util.Date tempDate = dateFormat.parse(unFormattedData);
						formattedData = requiredDateFormat.format(tempDate);
						PropertyUtils.setProperty(additionalData, mappingField, formattedData);
					}
				/*}*/
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("transformServiceToConsumer",e);
				String i18Value = messageSource.getMessage(fieldLabelId,null,fieldLabelId,locale);
				Message validationMessage = new Message(AdditionalDataConstants.ADDL_DATA_GETVAL_EXCEPTION,Message.MessageType.ERROR,i18Value);
				throw ExceptionBuilder.getInstance(BusinessException.class, AdditionalDataConstants.ADDL_DATA_GETVAL_EXCEPTION, "Error in getting value of Additional Field").setMessage(validationMessage).build();
			}
				
		}
		return additionalData;
	}
	

	/** 
	 *  Method to convert Additional Field Data into string to save in DB
	 * @param additionalData 
	 * @param additionalDataMetaData 
	 * @param requestSerivceContext 
	 * @return 
	 */
	public AdditionalData transformConsumerToService(AdditionalData additionalData,	List<AdditionalDataMetaData> additionalDataMetaDataList) {
		
		boolean unFormatDate = false;
		SimpleDateFormat dateFormat = null;
		String digitGroupingConstant=null;
		String decimalGroupingConstant=null;
		String datePattern=null;


		ConfigurationVO configVOForDigitGrouping=configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),AdditionalDataConstants.CONFIG_DIGIT_GROUPING_CONSTANT);
		
		if(ValidatorUtils.isNull(configVOForDigitGrouping))
		{
			digitGroupingConstant=AdditionalDataConstants.DEFAULT_DIGIT_GROUPING_CONSTANT;
		}
		else
		{
			digitGroupingConstant=configVOForDigitGrouping.getText();
		}
		ConfigurationVO configVOForDecimalGrouping=configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),AdditionalDataConstants.CONFIG_DECIMAL_GROUPING_CONSTANT);
		if(ValidatorUtils.isNull(configVOForDecimalGrouping))
		{
			decimalGroupingConstant=AdditionalDataConstants.DEFAULT_DECIMAL_GROUPING_CONSTANT;
		}
		else
		{
			decimalGroupingConstant=configVOForDecimalGrouping.getText();
		}
		ConfigurationVO configVOForDatePattern=configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),AdditionalDataConstants.CONFIG_DATE_FORMATS);
		if(ValidatorUtils.isNull(configVOForDatePattern))
		{
			datePattern=AdditionalDataConstants.DEFAULT_DATE_PATTERN;
		}
		else
		{
			datePattern=configVOForDatePattern.getText();
			
		}
	
		Locale locale=getSystemLocale();
	    
		
		 SimpleDateFormat requiredDateFormat = new SimpleDateFormat(datePattern);
		if(! datePattern.equalsIgnoreCase(AdditionalDataConstants.DATE_FORMAT)){
			dateFormat = new SimpleDateFormat(AdditionalDataConstants.DATE_FORMAT);
			unFormatDate = true;
		}
		
		
		Long dataType = 0L;
		String mappingField = "";
		String fieldName = "";
		String fieldLabelId = "";
		String formattedData = "";
	    String unFormattedData = "";
	    for(AdditionalDataMetaData additionalDataMetaData:additionalDataMetaDataList){
			try {
				dataType = additionalDataMetaData.getDataType();
				mappingField = additionalDataMetaData.getMappingField();
				fieldName = additionalDataMetaData.getFieldName();
				fieldLabelId = additionalDataMetaData.getFieldLabelId();
				formattedData = (String)PropertyUtils.getProperty(additionalData, mappingField);
				if(ValidatorUtils.isNull(formattedData)
						||("").equals(formattedData)){
					continue;
				}
				GenericParameter genericParameterAmount=genericParameterService.findByCode(CustomFieldDataType.AMOUNT,CustomFieldDataType.class);
				GenericParameter genericParameterInteger=genericParameterService.findByCode(CustomFieldDataType.INTEGER,CustomFieldDataType.class);
				GenericParameter genericParameterFloatingNumber=genericParameterService.findByCode(CustomFieldDataType.RATE,CustomFieldDataType.class);
				GenericParameter genericParameterDate=genericParameterService.findByCode(CustomFieldDataType.DATE,CustomFieldDataType.class);
				//A	Alphanumeric,	N	Amount (Numeric),	D	Date,	I	Integer,	R Rate
				if(ValidatorUtils.notNull(dataType) && (dataType.equals(genericParameterAmount.getId())||dataType.equals(genericParameterInteger.getId())||dataType.equals(genericParameterFloatingNumber.getId()))){
					unFormattedData = formattedData.replaceAll("\\"+digitGroupingConstant, "");
					unFormattedData = unFormattedData.replaceAll("\\"+decimalGroupingConstant, ".");
					PropertyUtils.setProperty(additionalData, mappingField, unFormattedData);
				}else if(unFormatDate && dataType.equals(genericParameterDate.getId())) // Date stored is in different format other than tenant format
				{
					java.util.Date tempDate = requiredDateFormat.parse(formattedData);
					unFormattedData = dateFormat.format(tempDate);
					PropertyUtils.setProperty(additionalData, mappingField, unFormattedData);
				}
				
			} catch (Exception e) {
				BaseLoggers.exceptionLogger.error("transformConsumerToService",e);
				String i18Value = messageSource.getMessage(fieldLabelId,null,fieldLabelId,locale);
				Message validationMessage = new Message(AdditionalDataConstants.ADDL_DATA_GETVAL_EXCEPTION,Message.MessageType.ERROR,i18Value);
				throw ExceptionBuilder.getInstance(BusinessException.class, AdditionalDataConstants.ADDL_DATA_GETVAL_EXCEPTION, "Error in getting value of Additional Field").setMessage(validationMessage).build();
			} 
				
		}
		return additionalData;
	}
	
	 public UserInfo getUserDetails() {
	        UserInfo userInfo = null;
	        SecurityContext securityContext = SecurityContextHolder.getContext();
	        if (securityContext != null) {
	            Object principal = securityContext.getAuthentication().getPrincipal();
	            if (UserInfo.class.isAssignableFrom(principal.getClass())) {
	                userInfo = (UserInfo) principal;
	            }
	        }
	        return userInfo;
	    }	

	 public Locale getSystemLocale() {
	        Locale locale = null;
	        UserInfo ui = getUserDetails();
	        if (ui != null) {
	            ConfigurationVO preferences = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
		                "config.user.locale");
	            String[] localeString = preferences.getText().split("_");
	            if (localeString.length >= 2) {
	                locale = new Locale(localeString[0], localeString[1]);
	            }
	        }
	        return locale;
	    }
	 
}