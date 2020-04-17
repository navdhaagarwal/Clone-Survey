package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.master.BaseMasterEntity;

/**
 * @author mukul.kumar
 * Maps Letters with their parameters
 */
@Entity
@Cacheable
@DynamicInsert 
@DynamicUpdate
@Table(name = "COM_COMMUNICATION_PARAM_DTL")
@NamedQuery(name="getParameterListByCommunication",
			query="select communicationParamaterMappingDetail from CommunicationParamaterMappingDetail communicationParamaterMappingDetail" +
			" where communicationParamaterMappingDetail.communicationMasterId=:communicationMasterId" +
			" and communicationParamaterMappingDetail.communicationParameter.isImage=:isImage" +
			" and communicationParamaterMappingDetail.masterLifeCycleData.approvalStatus in (:approvalStatusTypes)" +
			" and communicationParamaterMappingDetail.activeFlag=:activeFlag")
@Synonym(grant="SELECT")
public class CommunicationParamaterMappingDetail extends BaseMasterEntity implements Cloneable {


	private static final long serialVersionUID = -9182608545946899493L;
	
	@Column(name = "COMMUNICATION_MST_ID", nullable = false)
	private Long communicationMasterId;
	
	@Column(name = "PARAMETER_MST_ID", nullable = false)
	private Long parameterId;
	
	@ManyToOne
	@JoinColumn(name="COMMUNICATION_MST_ID",referencedColumnName = "ID", insertable=false, updatable = false)
	private CommunicationName communication;

	@ManyToOne
	@JoinColumn(name="PARAMETER_MST_ID",referencedColumnName = "ID", insertable=false, updatable = false)
	private CommunicationParameter communicationParameter;


	public Long getParameterId() {
		return parameterId;
	}

	public void setParameterId(Long parameterId) {
		this.parameterId = parameterId;
	}

	public Long getCommunicationMasterId() {
		return communicationMasterId;
	}

	public void setCommunicationMasterId(Long communicationMasterId) {
		this.communicationMasterId = communicationMasterId;
	}

	public CommunicationName getCommunication() {
		return communication;
	}

	public void setCommunication(CommunicationName communication) {
		this.communication = communication;
	}

	public CommunicationParameter getCommunicationParameter() {
		return communicationParameter;
	}

	public void setCommunicationParameter(
			CommunicationParameter communicationParameter) {
		this.communicationParameter = communicationParameter;
	}

	public CommunicationParamaterMappingDetail clone() throws CloneNotSupportedException{

		CommunicationParamaterMappingDetail cloneCommunicationParamaterMappingDetail=null;
	
		cloneCommunicationParamaterMappingDetail = (CommunicationParamaterMappingDetail)super.clone();
	return cloneCommunicationParamaterMappingDetail;
	}

}
