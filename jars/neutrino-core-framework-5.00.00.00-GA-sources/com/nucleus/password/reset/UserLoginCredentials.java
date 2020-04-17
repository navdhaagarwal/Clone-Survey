package com.nucleus.password.reset;

import java.io.Serializable;

/**
 * @author shivendra.kumar
 *
 */
public class UserLoginCredentials implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String username;

	private String newPassword;

	private String oldPassword;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}



}
