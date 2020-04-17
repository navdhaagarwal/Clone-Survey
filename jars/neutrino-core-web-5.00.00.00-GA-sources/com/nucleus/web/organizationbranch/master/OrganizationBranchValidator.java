package com.nucleus.web.organizationbranch.master;

import org.joda.time.DateTime;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;


import java.util.HashMap;
import java.util.Map;

import com.nucleus.core.organization.calendar.BranchCalendar;
import com.nucleus.core.organization.calendar.DailySchedule;
import com.nucleus.core.organization.entity.OrganizationBranch;
import com.nucleus.web.common.controller.CASValidationUtils;

import com.nucleus.jsMessageResource.service.JsMessageResourceService;
import com.nucleus.user.UserService;

import javax.inject.Inject;
import javax.inject.Named;

public class OrganizationBranchValidator implements Validator {

	 @Inject
    @Named("jsMessageResourceService")
    public JsMessageResourceService jsMessageResourceService;

    @Inject
    @Named("userService")
    public UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return OrganizationBranch.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "branchCode", "label.required.branchcode");
        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "label.required.branchname");
        /*        CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "parentOrganizationBranch.id", "label.required.parentbranch");
        */CASValidationUtils.rejectIfEmptyOrWhitespace(errors, "organizationType", "label.required.branchtype");
        OrganizationBranch organizationBranch = (OrganizationBranch) target;
        if (!CASValidationUtils.isValidBranchCode(organizationBranch.getBranchCode())) {
            errors.rejectValue("branchCode", "label.for.validBranchCode");
        }
		String regexForOrgBranchName=jsMessageResourceService.getAppendedPropertyForKeys("allowed.specChars.orgBranch.name","core.web.validation.config.customValidatorForOrgBranchName");
        if (!CASValidationUtils.isSpecialCharsAndRegex(organizationBranch.getName(),regexForOrgBranchName)) {
            errors.rejectValue("name", "label.for.alphanumeric");
        }
        if (organizationBranch.getSignatureAuthority().getFullName() != null) {
            if (!CASValidationUtils.isCharactersOnly(organizationBranch.getSignatureAuthority().getFullName())) {
                errors.rejectValue("signatureAuthority", "label.for.character");
            }
        }
        if (organizationBranch.getProductOffered() != null) {
            if (!CASValidationUtils.isAlphaNumeric(organizationBranch.getProductOffered())) {
                errors.rejectValue("productOffered", "label.for.alphanumeric");
            }
        }

        if (organizationBranch.getPrimaryContactPerson().getFullName() != null) {
            if (!CASValidationUtils.isCharactersOnly(organizationBranch.getPrimaryContactPerson().getFullName())) {
                errors.rejectValue("primaryContactPerson", "label.for.character");
            }
        }
        // if (organizationBranch.getContactInfo().getPhoneNumber().getPhoneNumber() != null) {
        // if (!CASValidationUtils.isDigitOnly(organizationBranch.getContactInfo().getPhoneNumber().getPhoneNumber())) {
        // errors.rejectValue("contactInfo", "label.for.digit");
        // }
        // }
        addValidatorsForDailyScheduleTimings(organizationBranch,errors);
    }
    
    private void addValidatorsForDailyScheduleTimings(OrganizationBranch organizationBranch,Errors errors) {
		BranchCalendar branchCalendar = organizationBranch.getBranchCalendar();

		if(branchCalendar != null) {				 
			Map<String,DailySchedule> dailyScheduleMap = new HashMap<>();
			dailyScheduleMap.put("Sunday", branchCalendar.getSundaySchedule());
			dailyScheduleMap.put("Monday", branchCalendar.getMondaySchedule());
			dailyScheduleMap.put("Tuesday", branchCalendar.getTuesdaySchedule());
			dailyScheduleMap.put("Wednesday", branchCalendar.getWednesdaySchedule());
			dailyScheduleMap.put("Thursday", branchCalendar.getThursdaySchedule());
			dailyScheduleMap.put("Friday", branchCalendar.getFridaySchedule());
			dailyScheduleMap.put("Saturday", branchCalendar.getSaturdaySchedule());
			dailyScheduleMap.put("EvenSaturday", branchCalendar.getEvenSaturdaySchedule());

			for(Map.Entry<String, DailySchedule> dailyScheduleEntry:dailyScheduleMap.entrySet()) {
				String day = dailyScheduleEntry.getKey();
				DailySchedule dailySchedule = dailyScheduleEntry.getValue();
				
				if(dailySchedule != null && dailySchedule.isWorkingDay()) {
					validateDailyScheduleTimings(dailySchedule,day,errors);
				}					
			}			
		}			
	}

	private void validateDailyScheduleTimings(DailySchedule dailySchedule, String day, Errors errors) {
        	
        	DateTime  openingTime = dailySchedule.getOpeningTime();
        	DateTime  closingTime = dailySchedule.getClosingTime();        	

        	DateTime  lunchFromTime = dailySchedule.getLunchFrom();
        	DateTime  lunchToTime = dailySchedule.getLunchTo();
        	
        	if(openingTime == null || closingTime == null) {
            	errors.reject("label.dailySchedule.opening.closing.time.empty",new String[] {day}, day+" opening time and closing time can not be empty");
        	}else {
        		long opeingTimeMillis = openingTime.getMillis();
        		long closingTimeMillis = closingTime.getMillis();
        		if (opeingTimeMillis >= closingTimeMillis){
	            	errors.reject("label.dailySchedule.closing.time.greaterThan.opening.time",new String[] {day}, day+" closing time should be greater than "+day+" opening time");
        		}
        		
        		if(lunchFromTime != null &&  lunchToTime != null) {
        			long lunchFromTimeMillis = lunchFromTime.getMillis();
            		long lunchToTimeMillis = lunchToTime.getMillis();
            		
            		if(lunchFromTimeMillis <= opeingTimeMillis || lunchFromTimeMillis >= closingTimeMillis){
		            	errors.reject("label.dailySchedule.lunchFrom.time.inBetween.opening.closing.time",new String[] {day}, day+" lunch from time should be in between "+day+" opening and closing time");
            		}else if(lunchToTimeMillis <= opeingTimeMillis || lunchToTimeMillis >= closingTimeMillis){
		            	errors.reject("label.dailySchedule.lunchTo.time.inBetween.opening.closing.time",new String[] {day}, day+" lunch to time should be in between "+day+" opening and closing time");
            		}else if(lunchFromTimeMillis>=lunchToTimeMillis){
		            	errors.reject("label.dailySchedule.lunchTo.time.greaterThan.lunchFrom.time",new String[] {day}, day+" lunch to time should be greater than "+day+" lunch from time");
            		}       
        		}
        		
        		if(lunchFromTime != null &&  lunchToTime == null) {
	            	errors.reject("label.dailySchedule.lunchTo.empty.lunchFrom.not.empty",new String[] {day}, day+" lunch to time is required because "+day+" lunch from time has mentioned");
            	}else if(lunchFromTime == null &&  lunchToTime != null) {
	            	errors.reject("label.dailySchedule.lunchFrom.empty.lunchTo.not.empty",new String[] {day}, day+" lunch from time is required because "+day+" lunch to time has mentioned");
            	}          		
        	}       	
		
	}
    
}
