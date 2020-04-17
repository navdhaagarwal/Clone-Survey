package com.nucleus.core.security.entities;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class HtmlAttributes extends BaseEntity{

	private static final long serialVersionUID = 1L;

	/**
	 * Comma separated html attributes  
	 */
	private String htmlAttributes;
	
	@Column(name="HTML_EMEMENT")
	private Long elementId;

	private String attributeValue;
	
	
	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}

	public Long getElementId() {
		return elementId;
	}

	public void setElementId(Long elementId) {
		this.elementId = elementId;
	}

	public String getHtmlAttributes() {
		return htmlAttributes;
	}

	public void setHtmlAttributes(String htmlAttributes) {
		this.htmlAttributes = htmlAttributes;
	}

	
	
	
	
}
