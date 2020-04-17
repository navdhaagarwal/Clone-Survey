/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.master;

import com.nucleus.web.utility.ControllerConstants;

/**
 * Utility class to hold all util methods for maker checker on web layer.
 * @author Nucleus Software Exports Limited
 */
public class MakerCheckerWebUtils {

    /**
     * @param
     * @return String
     * @throws
     * @description For returning Approval Staus Description
     */
    public static String getApprovalStatus(int approvalStatus) {
        String statusDescription = "";

        switch (approvalStatus) {

            case 0:
                statusDescription = ControllerConstants.APPROVED;
                break;
            case 1:
                statusDescription = ControllerConstants.NEW_RECORD;
                break;
            case 2:
                statusDescription = ControllerConstants.PENDING_APPROVAL;
                break;
            case 3:
                statusDescription = ControllerConstants.ORIGINAL_RECORD;
                break;
            case 4:
                statusDescription = ControllerConstants.MARKED_FOR_DELETION;
                break;
            case 5:
                statusDescription = ControllerConstants.PENDING_APPROVAL;
                break;
            case 6:
                statusDescription = ControllerConstants.DELETE_PENDING_APPROVAL;
                break;
            case 7:
                statusDescription = ControllerConstants.NEW_RECORD;
                break;
            case 8:
                statusDescription = ControllerConstants.EDITED;
                break;
            case 9:
                statusDescription = ControllerConstants.UNAPPROVED_IN_PROGRESS;
                break;
            case 10:
                statusDescription = ControllerConstants.UNAPPROVED_HISTORY;
                break;
            case 11:
                statusDescription = ControllerConstants.APPROVED_HISTORY;
                break;
            case 12:
                statusDescription = ControllerConstants.CLONED;
                break;

        }

        return statusDescription;
    }
    
    public static int getApprovalStatusCode(String statusDescription) {
        statusDescription = statusDescription.toUpperCase();
        int statusCode;

        if ((ControllerConstants.APPROVED).toUpperCase().startsWith(statusDescription)) {
            statusCode = 0;
        } else if ((ControllerConstants.NEW_RECORD).toUpperCase().startsWith(statusDescription)) {
            statusCode = 7; // Also 1, but record with status 1 is not shown in the grid
        } else if ((ControllerConstants.PENDING_APPROVAL).toUpperCase().startsWith(statusDescription)) {
            statusCode = 2; // Also 5, but record with status 5 is not shown in the grid
        } else if ((ControllerConstants.ORIGINAL_RECORD).toUpperCase().startsWith(statusDescription)) {
            statusCode = 3;
        } else if ((ControllerConstants.MARKED_FOR_DELETION).toUpperCase().startsWith(statusDescription)) {
            statusCode = 4;
        } else if ((ControllerConstants.DELETE_PENDING_APPROVAL).toUpperCase().startsWith(statusDescription)) {
            statusCode = 6;
        } else if ((ControllerConstants.EDITED).toUpperCase().startsWith(statusDescription)) {
            statusCode = 8;
        } else if ((ControllerConstants.UNAPPROVED_IN_PROGRESS).toUpperCase().startsWith(statusDescription)) {
            statusCode = 9;
        } else if ((ControllerConstants.UNAPPROVED_HISTORY).toUpperCase().startsWith(statusDescription)) {
            statusCode = 10;
        } else if ((ControllerConstants.APPROVED_HISTORY).toUpperCase().startsWith(statusDescription)) {
            statusCode = 11;
        } else if ((ControllerConstants.CLONED).toUpperCase().startsWith(statusDescription)) {
            statusCode = 12;
        } else {
            statusCode = -1;
        }
        return statusCode;
    }

}
