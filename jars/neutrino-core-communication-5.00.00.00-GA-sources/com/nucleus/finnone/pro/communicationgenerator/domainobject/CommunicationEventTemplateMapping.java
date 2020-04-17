package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.ID_LENGTH_FOUR;
import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.ID_LENGTH_NINETEEN;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingHelper;
import com.nucleus.rules.model.Rule;

@Entity
@Cacheable
@DynamicInsert
@DynamicUpdate
@Table(name = "COMM_EVENT_TEMPLATE_MAP")
@Synonym(grant = "ALL")
public class CommunicationEventTemplateMapping extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMM_EVENT_DTL_TEMPLATE_MAP_ID")
	private CommunicationEventMappingDetail communicationEventMappingDetail;
	
	@Transient
	private List<Long> attachmentIds;

	@Column(name = "TEMPLATE_MST_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long communicationTemplateId;

	@OneToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "TEMPLATE_MST_ID", columnDefinition = ID_LENGTH_NINETEEN, insertable = false, updatable = false, referencedColumnName = "ID")
	private CommunicationTemplate communicationTemplate;

	@Column(name = "PRIORITY", columnDefinition = ID_LENGTH_FOUR)
	private Long priority = 1L;

	@Column(name = "CRITERIA", length = 4000)
	private String criteria;

	@Column(name = "RULE_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long ruleId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "RULE_ID", columnDefinition = ID_LENGTH_NINETEEN, updatable = false, insertable = false, referencedColumnName = "ID")
	private Rule rule;

	@Column(name = "ATTACHMENT_TEMPLATE_IDS", length = 255)
	private String attachmentTemplateIds;

	public CommunicationEventMappingDetail getCommunicationEventMappingDetail() {
		return communicationEventMappingDetail;
	}

	public void setCommunicationEventMappingDetail(CommunicationEventMappingDetail communicationEventMappingDetail) {
		this.communicationEventMappingDetail = communicationEventMappingDetail;
	}

	public Long getRuleId() {
		return ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}

	public Rule getRule() {
		return rule;
	}

	public void setRule(Rule rule) {
		this.rule = rule;
	}

	public Long getCommunicationTemplateId() {
		return communicationTemplateId;
	}

	public void setCommunicationTemplateId(Long communicationTemplateId) {
		this.communicationTemplateId = communicationTemplateId;
	}

	public CommunicationTemplate getCommunicationTemplate() {
		return communicationTemplate;
	}

	public void setCommunicationTemplate(CommunicationTemplate communicationTemplate) {
		this.communicationTemplate = communicationTemplate;
	}

	public Long getPriority() {
		return priority;
	}

	public void setPriority(Long priority) {
		this.priority = priority;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public String getAttachmentTemplateIds() {
		return attachmentTemplateIds;
	}

	public void setAttachmentTemplateIds(String attachmentTemplateIds) {
		this.attachmentTemplateIds = attachmentTemplateIds;
	}

	public List<Long> getAttachmentIds() {
		if (notNull(this.getAttachmentTemplateIds())) {
			String[] arrayAttachmentIds = this.getAttachmentTemplateIds().split(",");
			List<Long> attachIds = new ArrayList<Long>(arrayAttachmentIds.length);
			for (int i = 0; i < arrayAttachmentIds.length; i++) {
				attachIds.add(Long.parseLong(arrayAttachmentIds[i]));
			}
			this.attachmentIds = attachIds;
		}
		return attachmentIds;
	}

	public void setAttachmentIds(List<Long> attachmentIds) {
		this.attachmentIds = attachmentIds;
	}

	public String getDecodedCriteria() {
		return CommunicationEventMappingHelper.decodeSQLCriteria(this.criteria);
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationEventTemplateMapping communicationEventMappingDetail = (CommunicationEventTemplateMapping) baseEntity;
		super.populate(communicationEventMappingDetail, cloneOptions);
		communicationEventMappingDetail.setCommunicationTemplateId(communicationTemplateId);
		communicationEventMappingDetail.setCommunicationTemplate(communicationTemplate);
		communicationEventMappingDetail.setPriority(priority);
		communicationEventMappingDetail.setAttachmentTemplateIds(attachmentTemplateIds);
		communicationEventMappingDetail.setCriteria(criteria);
		communicationEventMappingDetail.setRule(rule);
		communicationEventMappingDetail.setRuleId(ruleId);
		communicationEventMappingDetail.setTenantId(getTenantId());
		communicationEventMappingDetail.setMakeBusinessDate(getMakeBusinessDate());
		communicationEventMappingDetail.getEntityLifeCycleData()
				.setLastUpdatedTimeStamp(getEntityLifeCycleData().getLastUpdatedTimeStamp());
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationEventTemplateMapping communicationEventMappingDetail = (CommunicationEventTemplateMapping) baseEntity;
		super.populateFrom(communicationEventMappingDetail, cloneOptions);
		this.setCommunicationTemplateId(communicationEventMappingDetail.getCommunicationTemplateId());
		this.setCommunicationTemplate(communicationEventMappingDetail.getCommunicationTemplate());
		this.setPriority(communicationEventMappingDetail.getPriority());
		this.setAttachmentTemplateIds(communicationEventMappingDetail.getAttachmentTemplateIds());
		this.setCriteria(communicationEventMappingDetail.getCriteria());
		this.setRule(communicationEventMappingDetail.getRule());
		this.setRuleId(communicationEventMappingDetail.getRuleId());
		this.setTenantId(getTenantId());
		this.setMakeBusinessDate(communicationEventMappingDetail.getMakeBusinessDate());
		this.getEntityLifeCycleData().setLastUpdatedTimeStamp(
				communicationEventMappingDetail.getEntityLifeCycleData().getLastUpdatedTimeStamp());
	}
}
