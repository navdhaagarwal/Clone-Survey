package com.nucleus.authenticationToken;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


@Entity
@DynamicUpdate
@DynamicInsert
public class ApprovalLinkToken extends AuthenticationToken {
	
	private static final long serialVersionUID = 1196901653588799999L;
	
	   private String            taskId;

	/**
	 * @return the taskId
	 */
	public String getTaskId() {
		return taskId;
	}

	/**
	 * @param taskId the taskId to set
	 */
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	

}

