/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.notificationMaster.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.contact.PhoneNumber;

/**
 * The Interface RoleBasedEmailService.
 *
 * @author Nucleus Software India Pvt Ltd
 */
public interface RoleBasedApplicantService {

    /**
     * Gets the email ids based on role.- Primary Borrower, Secondary Borrower, Gurantor
     *
     * @param contextMap the context map
     * @param applicantRole the applicant role
     * @return the email ids based on role
     */
    List<String> getEmailIdsBasedOnRole(Map contextMap, int applicantRole);

    /**
     * Gets the phone number based on role.
     *
     * @param contextMap the context map
     * @param applicantRole the applicant role
     * @return the phone number based on role
     */
    List<String> getPhoneNumberBasedOnRole(Map contextMap, int applicantRole);
	List<String> getEmailIdsBasedOnAudienceType(Map contextMap,String type, String audienceDetails);
	List<String> getPhoneNumberBasedOnAudienceType(Map contextMap,String type, String audienceDetails);
	List<String> getUserIdsBasedOnAudienceType(Map contextMap, String type,String audienceDetails);
    default List<String> getEmailForReassign(Map contextMap){
        return new ArrayList<>();
    }
	public default String getRecipentsPhoneNumberInCaseOfOTP(Map<Object, Object> contextMap) {
		Object phoneNumObj = contextMap.get("phoneNumber");
		if (null != phoneNumObj && phoneNumObj instanceof PhoneNumber) {
			PhoneNumber phoneNumber = (PhoneNumber) phoneNumObj;
			String number = phoneNumber.getIsdCode() + phoneNumber.getPhoneNumber();
			return StringUtils.deleteWhitespace(number);
		}
		return null;
	}
    default List<String> getPhoneNumbersForReassign(Map contextmap){
        return new ArrayList<>();
    }
    default List<String> getUserIdsForReassign(Map contextmap){
        return new ArrayList<>();
    }

}
