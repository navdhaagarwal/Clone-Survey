package com.nucleus.master.audit;

import java.io.Serializable;
import java.util.List;

import org.joda.time.DateTime;

public class MasterChangeVO implements Serializable{

	private String typeOfAction; //view or edit
	
	private String dateOfAction; //date time for action
	
	private String userName;
	
	private String submittedBy;
	
	private List<String> actionMessages;

	public String getTypeOfAction() {
		return typeOfAction;
	}

	public void setTypeOfAction(String typeOfAction) {
		this.typeOfAction = typeOfAction;
	}

	

	public String getDateOfAction() {
		return dateOfAction;
	}

	public void setDateOfAction(String dateOfAction) {
		this.dateOfAction = dateOfAction;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public List<String> getActionMessages() {
		return actionMessages;
	}

	public void setActionMessages(List<String> actionMessages) {
		this.actionMessages = actionMessages;
	}

	public String getSubmittedBy() {
		return submittedBy;
	}

	public void setSubmittedBy(String submittedBy) {
		this.submittedBy = submittedBy;
	}

	
	
}
