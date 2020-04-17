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
package com.nucleus.web.datatable;

import java.util.List;

public class SimpleDataTableJsonHepler {
	private int sEcho;
	private int iTotalRecords;
	private int iTotalDisplayRecords;
	private List<? extends Object> aaData;

	/*
	 * Holds the list of object as key-value pair JSON .... used as data object in
	 * Data table of sAjaxSource property for server side pagination
	 */
	private List<? extends Object> additionalDataMap;

	/*
	 * This is added for the success-full running of the AUTOMATED Scripts in
	 * JMETER,this will not stop the running code, it will not stop the existing
	 * functionality
	 */
	private String taskId;
	/* End of Jmeter Extra Code */

	public int getsEcho() {
		return sEcho;
	}

	public void setsEcho(int sEcho) {
		this.sEcho = sEcho;
	}

	public int getiTotalRecords() {
		return iTotalRecords;
	}

	public void setiTotalRecords(int iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}

	public int getiTotalDisplayRecords() {
		return iTotalDisplayRecords;
	}

	public void setiTotalDisplayRecords(int iTotalDisplayRecords) {
		this.iTotalDisplayRecords = iTotalDisplayRecords;
	}

	public List<? extends Object> getAaData() {
		return aaData;
	}

	public void setAaData(List<? extends Object> aaData) {
		this.aaData = aaData;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public List<? extends Object> getAdditionalDataMap() {
		return additionalDataMap;
	}

	public void setAdditionalDataMap(List<? extends Object> additionalDataMap) {
		this.additionalDataMap = additionalDataMap;
	}
}