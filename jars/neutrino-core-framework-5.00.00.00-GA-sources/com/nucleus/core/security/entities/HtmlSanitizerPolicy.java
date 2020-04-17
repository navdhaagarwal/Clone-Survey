package com.nucleus.core.security.entities;

import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
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
public class HtmlSanitizerPolicy extends BaseEntity{
	
	private static final long serialVersionUID = 1L;
	
	private String name;

	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name = "HTML_SANT_POLICY")
	private List<HtmlElements> htmlelements;
	
	
	public List<HtmlElements> getHtmlelements() {
		return htmlelements;
	}

	public void setHtmlelements(List<HtmlElements> htmlelements) {
		this.htmlelements = htmlelements;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
