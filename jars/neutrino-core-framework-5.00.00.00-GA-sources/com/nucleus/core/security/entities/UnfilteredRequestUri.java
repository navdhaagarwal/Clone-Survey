/**
 *
 */
package com.nucleus.core.security.entities;

import java.util.Set;

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
public class UnfilteredRequestUri extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private String applicationURI;

	private String description;
	
	@OneToMany(fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	@JoinColumn(name = "unfiltered_request_uri")
	private Set<UnfilteredParameter> parameters;

	public String getApplicationURI() {
		return applicationURI;
	}

	public void setApplicationURI(String applicationURI) {
		this.applicationURI = applicationURI;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<UnfilteredParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Set<UnfilteredParameter> parameters) {
		this.parameters = parameters;
	}
	
}
