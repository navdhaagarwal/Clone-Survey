package com.nucleus.core.accesslog.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@NamedQueries({
	@NamedQuery(name="WebURIRepository.getWebUriRepositoryMap",query="SELECT new Map(uriRepo.uri as uri,uriRepo.id as id) from WebURIRepository uriRepo where uriRepo.uri IS NOT NULL")
})
public class WebURIRepository extends BaseMasterEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int LENGTH_FOUR_THOUSAND = 4000;

	@Column(length=LENGTH_FOUR_THOUSAND)
	private String uri;
	private String logicalFunctionName;
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getLogicalFunctionName() {
		return logicalFunctionName;
	}
	public void setLogicalFunctionName(String logicalFunctionName) {
		this.logicalFunctionName = logicalFunctionName;
	}
	
	

}
