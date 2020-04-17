package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.ID_LENGTH_FOUR;
import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.ID_LENGTH_NINETEEN;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@Cacheable
@DynamicInsert
@DynamicUpdate
@Table(name = "COMM_EVENT_MAP_DTL")
@Synonym(grant = "ALL")
public class CommunicationEventMappingDetail extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "COMMUNICATION_MST_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long communicationCodeId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMMUNICATION_MST_ID", columnDefinition = ID_LENGTH_NINETEEN, insertable = false, updatable = false, referencedColumnName = "ID")
	private CommunicationName communicationName;

	@Column(name = "COMMUNICATION_TYPE", length = 255)
	private String communicationType;

	@OrderBy("priority")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "COMM_EVENT_DTL_TEMPLATE_MAP_ID", columnDefinition = ID_LENGTH_NINETEEN, referencedColumnName = "ID")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	private List<CommunicationEventTemplateMapping> communicationEventTemplateMappings;

	@Column(name = "PRIORITY", columnDefinition = ID_LENGTH_FOUR)
	private Long priority = 1L;

	@Transient
	private Boolean emailFlag = false;

	public Boolean getEmailFlag() {
		return emailFlag;
	}

	public void setEmailFlag(Boolean emailFlag) {
		this.emailFlag = emailFlag;
	}

	public Long getCommunicationCodeId() {
		return communicationCodeId;
	}

	public void setCommunicationCodeId(Long communicationCodeId) {
		this.communicationCodeId = communicationCodeId;
	}

	public CommunicationName getCommunicationName() {
		return communicationName;
	}

	public void setCommunicationName(CommunicationName communicationCode) {
		this.communicationName = communicationCode;
	}

	public String getCommunicationType() {
		return communicationType;
	}

	public void setCommunicationType(String communicationType) {
		this.communicationType = communicationType;
	}

	public List<CommunicationEventTemplateMapping> getCommunicationEventTemplateMappings() {
		return communicationEventTemplateMappings;
	}

	public void setCommunicationEventTemplateMappings(
			List<CommunicationEventTemplateMapping> communicationEventTemplateMapping) {
		this.communicationEventTemplateMappings = communicationEventTemplateMapping;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationEventMappingDetail communicationEventMappingDetail = (CommunicationEventMappingDetail) baseEntity;
		super.populate(communicationEventMappingDetail, cloneOptions);
		communicationEventMappingDetail.setCommunicationCodeId(getCommunicationCodeId());
		communicationEventMappingDetail.setCommunicationType(getCommunicationType());
		communicationEventMappingDetail.setCommunicationName(getCommunicationName());
		communicationEventMappingDetail.setPriority(getPriority());
		communicationEventMappingDetail
				.setCommunicationEventTemplateMappings(new ArrayList<CommunicationEventTemplateMapping>());
		if (communicationEventTemplateMappings != null) {
			for (CommunicationEventTemplateMapping communicationEventTemplateMapping : communicationEventTemplateMappings) {
				if (communicationEventMappingDetail.getCommunicationEventTemplateMappings() != null
						&& communicationEventTemplateMapping != null) {
					communicationEventMappingDetail.getCommunicationEventTemplateMappings()
							.add((CommunicationEventTemplateMapping) communicationEventTemplateMapping
									.cloneYourself(cloneOptions));
				}
			}
		}
		communicationEventMappingDetail.setTenantId(getTenantId());
		communicationEventMappingDetail.setMakeBusinessDate(getMakeBusinessDate());
		communicationEventMappingDetail.getEntityLifeCycleData()
				.setLastUpdatedTimeStamp(getEntityLifeCycleData().getLastUpdatedTimeStamp());
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationEventMappingDetail communicationEventMappingDetail = (CommunicationEventMappingDetail) baseEntity;
		super.populateFrom(communicationEventMappingDetail, cloneOptions);
		this.setCommunicationCodeId(getCommunicationCodeId());
		this.setCommunicationType(getCommunicationType());
		this.setCommunicationName(getCommunicationName());
		this.setPriority(getPriority());
		if (communicationEventMappingDetail.getCommunicationEventTemplateMappings() != null) {
			for (CommunicationEventTemplateMapping communicationEventTemplateMapping : communicationEventMappingDetail
					.getCommunicationEventTemplateMappings()) {
				if (this.getCommunicationEventTemplateMappings() != null && communicationEventTemplateMapping != null) {
					this.getCommunicationEventTemplateMappings()
							.add((CommunicationEventTemplateMapping) communicationEventTemplateMapping
									.cloneYourself(cloneOptions));
				}
			}
		}
		this.setCommunicationEventTemplateMappings(getCommunicationEventTemplateMappings());
		this.setTenantId(getTenantId());
		this.setMakeBusinessDate(communicationEventMappingDetail.getMakeBusinessDate());
		this.getEntityLifeCycleData().setLastUpdatedTimeStamp(
				communicationEventMappingDetail.getEntityLifeCycleData().getLastUpdatedTimeStamp());
	}
}
