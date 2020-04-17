/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cas.sequence;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.service.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 */

public class CasSequenceServiceImpl extends BaseServiceImpl implements CasSequenceService {
    private static final String NEO_DEFAULT_APP_NUMBER_FORMAT = "NEO-DEFAULT";
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    @Value(value = "${label.fas.simulation.prefix}")
    public String fasSimPrefix;

    public static final String              SYSTEM_ENTITY_URI          = "com.nucleus.entity.SystemEntity:1";
    public static final String              FIX_FORMAT          	   = "FIX";
    public static final String              DATE_FORMAT                = "DATE";
    public static final String              PRODUCTTYPE_FORMAT         = "PRODUCTTYPE";
    public static final String              SEQUENCE_FORMAT            = "SEQUENCE";

    @Override
    public String generateNextApplicationNumber() {
        return "APPL" + pad(entityDao.getNextValue("application_sequence"), 8);
    }

    @Override
    public String generateNextApplicationNumberConfig(String applicationNumberConfig, String productType) {
        return createPrefix(applicationNumberConfig, productType, null);
    }

    private String createPrefix(String applicationNumberConfig, String productType, Long incrementBy) {

        try {
            String[] appNumberFormatParts = applicationNumberConfig.split("::");
            if (appNumberFormatParts != null && appNumberFormatParts.length > 0) {
            	return generateAppNumber(appNumberFormatParts, productType, incrementBy);
            } else {
                BaseLoggers.flowLogger.info("Wrong Format is provided,creating application number of default format");
                return generateUpdatedAppNumber(incrementBy, new StringBuilder(), true);
            }
        }catch (Exception e){
            BaseLoggers.flowLogger.error("Some Error Occured",e);
            return generateUpdatedAppNumber(incrementBy, new StringBuilder(), true);
        }
    }

    private String generateAppNumber(String[] appNumberFormatParts, String productType, Long incrementBy) {
        StringBuilder appNumber = new StringBuilder();
        for (int i = 0; i < appNumberFormatParts.length; i++) {
            String[] keyValue = appNumberFormatParts[i].split(":");
            if (FIX_FORMAT.equals(keyValue[0]) && keyValue.length == 2) {
                appNumber.append(keyValue[1]);
            } else if (DATE_FORMAT.equals(keyValue[0]) && keyValue.length == 2 && validDateFormat(keyValue[1])) {
                Date date = new Date();
                SimpleDateFormat dateFormat = new SimpleDateFormat(keyValue[1]);
                appNumber.append(dateFormat.format(date));
            } else if (PRODUCTTYPE_FORMAT.equals(keyValue[0]) && productType != null && keyValue.length==1) {
                appNumber.append(productType);
            } else if (SEQUENCE_FORMAT.equals(keyValue[0])  && keyValue.length==1) {
                generateUpdatedAppNumber(incrementBy, appNumber, false);
            } else {
                BaseLoggers.flowLogger.info("Wrong Format is provided,creating application number of default format");
                return generateUpdatedAppNumber(incrementBy, new StringBuilder(), true);
            }
        }
        return appNumber.toString();
    }
    
	private boolean validDateFormat(String dateFormat){
        String validDateFormats="";
        try {
             validDateFormats = configurationService.getConfigurationPropertyFor(
                    EntityId.fromUri(SYSTEM_ENTITY_URI), "config.application.number.date.format").getPropertyValue();
        }
        catch (Exception e){
            BaseLoggers.flowLogger.error("Error in Date Format");
            return false;
        }
        if(StringUtils.isNotEmpty(validDateFormats)) {
            List<String> validDateFormatList = new ArrayList<>(Arrays.asList(validDateFormats.split(",")));
            return CollectionUtils.isNotEmpty(validDateFormatList) && validDateFormatList.contains(dateFormat);
        }
        else{
            return false;
        }
    }
	
	private String generateUpdatedAppNumber(Long incrementBy, StringBuilder appNumber, boolean isApplAppended) {
		if(isApplAppended){
			appNumber.append("APPL");
		}
		
		if (incrementBy == null) {
            appNumber.append(pad(entityDao.getNextValue("application_sequence"), 12));
        } else {
            appNumber.append(pad(entityDao.getNextValue("application_sequence", incrementBy.intValue()), 12));
        }
		
		return appNumber.toString();
	}

    @Override
    public String generateNextGenericWfNumber() {
        return "WF" + pad(entityDao.getNextValue("generic_wf_sequence"), 8);
    }

    @Override
    public String generateNextProposalNumber() {
        return pad(entityDao.getNextValue("proposal_sequence"), 8);
    }

    @Override
    public String generateNextCollateralNumber() {
        return "COLL" + pad(entityDao.getNextValue("collateral_sequence"), 8);
    }

    @Override
    public String generateNextCustomerNumber() {
        return "CUST" + pad(entityDao.getNextValue("customer_sequence"), 8);
    }

    @Override
    public String generateNextShowroomNumber() {
        return "SHOWROOM" + pad(entityDao.getNextValue("showroom_sequence"), 8);
    }

    @Override
    public String generateNextConsumerDurableNumber() {
        return "CD" + pad(entityDao.getNextValue("proposal_sequence"), 8);
    }

    protected String pad(Long input, int targetDigits) {
        return StringUtils.leftPad(String.valueOf(input), targetDigits, '0');
    }

    protected String pad(Integer input, int targetDigits) {
        return StringUtils.leftPad(String.valueOf(input), targetDigits, '0');
    }

    @Override
    public String generateNextInstaCardGroupNumber() {
        return "INSTA" + pad(entityDao.getNextValue("instaCardGroup_sequence"), 8);
    }

    @Override
    public String[] generateNextApplicationNumbersRange(int incrementBy) {
        String[] appNumbers= new String[2];
        Long startnumber =  entityDao.getNextValue("application_sequence",incrementBy);
        appNumbers[0]= "APPL" + pad(startnumber, 8);
        appNumbers[1]= "APPL" + pad(startnumber+incrementBy,8);
        return appNumbers;
    }

    @Override
    public String[] generateNextApplicationNumbersRange(String applicationConfig,String productType,int incrementBy) {
        String[] appNumbers= new String[2];
        Long startnumber =  entityDao.getNextValue("application_sequence",incrementBy);
        appNumbers[0]= createPrefix(applicationConfig,productType,startnumber);
        appNumbers[1]= createPrefix(applicationConfig,productType,startnumber+incrementBy);
        return appNumbers;
    }

    @Override
    public boolean isNeoDefaultFormatConfig(ConfigurationVO appNumberFormatConfig) {
        boolean returnValue = true;
        if(appNumberFormatConfig!=null){
            String propertyValue = appNumberFormatConfig.getPropertyValue();
            if(StringUtils.isNotBlank(propertyValue)){
                returnValue = propertyValue.equalsIgnoreCase(NEO_DEFAULT_APP_NUMBER_FORMAT);
            }
        }
        return returnValue;
    }


    @Override
    public String generateNextRequestCode() {
        return "ST" + pad(entityDao.getNextValue("application_sequence"), 8);
    }

    @Override
    public String generateNextFasSimulationNumber() {
        if(com.mchange.v2.lang.StringUtils.nonEmptyString(fasSimPrefix)){
            return fasSimPrefix + pad(entityDao.getNextValue("FAS_SMLT_DETAILS_SEQ"),8);
        }
        else{
            return "SIM" + pad(entityDao.getNextValue("FAS_SMLT_DETAILS_SEQ"),8);
        }
    }
}
