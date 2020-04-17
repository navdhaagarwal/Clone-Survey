package com.nucleus.core.security.entities;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
public class HtmlElements extends BaseEntity{
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * comma separated html tags Ex: a,span
	 */
	private String htmlTags;
	
	@Column(name="HTML_SANT_POLICY")
	private Long htmlPolicyId;

	public List<HtmlAttributes> getHtmlAttributes() {
		return htmlAttributes;
	}


	public void setHtmlAttributes(List<HtmlAttributes> htmlAttributes) {
		this.htmlAttributes = htmlAttributes;
	}


	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name = "HTML_EMEMENT")
	private List<HtmlAttributes> htmlAttributes;

	public String getHtmlTags() {
		return htmlTags;
	}


	public void setHtmlTags(String htmlTags) {
		this.htmlTags = htmlTags;
	}


	public Long getHtmlPolicyId() {
		return htmlPolicyId;
	}


	public void setHtmlPolicyId(Long htmlPolicyId) {
		this.htmlPolicyId = htmlPolicyId;
	}

	
	
	
}
