package com.nucleus.userotpcount.entity;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@NamedQueries({
		@NamedQuery(name = "getUserOtpCountByUsername", query = "select userOtpCount from UserOTPCount userOtpCount where userOtpCount.userName = :userName ") })


@Cacheable
@Synonym(grant="ALL")
@Table(
		name="USER_OTP_COUNT",
	    uniqueConstraints=
	        @UniqueConstraint(columnNames={"userName"})
	)
public class UserOTPCount extends BaseEntity {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	
	private String userName;
	private int numberOfOTPSendAttempts;

	private int numberOfFailedOTPAttempts;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getNumberOfOTPSendAttempts() {
		return numberOfOTPSendAttempts;
	}

	public void setNumberOfOTPSendAttempts(int numberOfOTPSendAttempts) {
		this.numberOfOTPSendAttempts = numberOfOTPSendAttempts;
	}

	public int getNumberOfFailedOTPAttempts() {
		return numberOfFailedOTPAttempts;
	}

	public void setNumberOfFailedOTPAttempts(int numberOfFailedOTPAttempts) {
		this.numberOfFailedOTPAttempts = numberOfFailedOTPAttempts;
	}

}
