package com.nucleus.core.security.entities;



import javax.persistence.Entity;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;

@NamedQuery(
		name="getAdditionalBlackListPattern",
		query = "SELECT additionalBlackListPattern FROM  AdditionalBlackListPattern  additionalBlackListPattern where additionalBlackListPattern.entityLifeCycleData.persistenceStatus = :status or additionalBlackListPattern.entityLifeCycleData.persistenceStatus IS NULL"
				 
	)
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class AdditionalBlackListPattern extends BaseEntity{
	
	/**
	 * Possible values for type are I and HI.
	 * I: pattern applicable for input parameters only.
	 * HI: pattern applicable for input parameters and header as well.
	 * 
	 */
	private String type;
	/**
	 * Regex pattern
	 */
	private String pattern;
	/**
	 * Possible values for flag are  combination of [UNIX_LINES,
	CASE_INSENSITIVE ,
	COMMENTS ,
	MULTILINE ,
	LITERAL ,
	DOTALL ,
	UNICODE_CASE ,
	CANON_EQ ]
	With help of | (pipe).
	for Ex1: CASE_INSENSITIVE | DOTALL 
		Ex2 CANON_EQ | CASE_INSENSITIVE | DOTALL
		Ex3 MULTILINE | DOTALL | CASE_INSENSITIVE
		For details refer java.util.regex.Pattern
	 * 
	 */
	private String code;
	
	private String flags;
	public static final String PARAM="I";
	public static final String HEADERANDPARAM="HI";
	
	private String indexOfPart;
	
	public String getIndexOfPart() {
		return indexOfPart;
	}

	public void setIndexOfPart(String indexOfPart) {
		this.indexOfPart = indexOfPart;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		if(PARAM.equalsIgnoreCase(type)||HEADERANDPARAM.equalsIgnoreCase(type))
		{
			this.type = type;
		}
		else
		{
			throw new ServiceInputException("Only 'I' and 'HI' are supported for type");
		}
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	

}
