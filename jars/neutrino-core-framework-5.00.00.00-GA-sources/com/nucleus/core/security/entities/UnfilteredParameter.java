/**
 * 
 */
package com.nucleus.core.security.entities;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class UnfilteredParameter extends BaseEntity {


	private static final long serialVersionUID = 1L;

	private String 							name;

	private String 							description;
	
	@ManyToOne
	@JoinColumn(name = "unfiltered_request_uri")
    private UnfilteredRequestUri       unfilteredRequestUri;

	@ManyToOne
	@JoinColumn(name="HTML_SANT_POLICY",nullable=true)
	private HtmlSanitizerPolicy htmlSanitizerPolicy;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UnfilteredRequestUri getUnfilteredRequestUri() {
		return unfilteredRequestUri;
	}

	public void setUnfilteredRequestUri(UnfilteredRequestUri unfilteredRequestUri) {
		this.unfilteredRequestUri = unfilteredRequestUri;
	}

	public HtmlSanitizerPolicy getHtmlSanitizerPolicy() {
		return htmlSanitizerPolicy;
	}

	public void setHtmlSanitizerPolicy(HtmlSanitizerPolicy htmlSanitizerPolicy) {
		this.htmlSanitizerPolicy = htmlSanitizerPolicy;
	}

	
}
