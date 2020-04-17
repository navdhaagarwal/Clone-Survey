/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.web.dynamicReport;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Nucleus Software Exports Limited
 */
public class DynamicReportPojo implements Serializable {

    private static final long serialVersionUID = -176346540555135437L;

    private byte[]            reportData;
    private String            mediaType;
    private String            fileName;

    public DynamicReportPojo(byte[] reportData, String mediaType, String fileName) {
        super();
        this.reportData = reportData;
        this.mediaType = mediaType;
        this.fileName = StringUtils.replace(fileName, " ", "_");
    }

    public byte[] getReportData() {
        return reportData;
    }

    public String getMediaType() {
        return mediaType;
    }

    public String getFileName() {
        return fileName;
    }

}
