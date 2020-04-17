/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.ID_LENGTH_NINETEEN;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.event.EventCode;
import com.nucleus.entity.BaseEntity;
import com.nucleus.rules.model.SourceProduct;

/**
 * Entity for the EventCriteriaTypeMapping
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@Cacheable
@Table(name = "EVENT_COMM_CRITERIA_MAP", indexes = {
		@Index(name = "EVENT_COMM_CRITERIA_MAP_IDX1", columnList = "SOURCE_PRODUCT_ID,EVENT_ID") })
@DynamicUpdate
@DynamicInsert
@Synonym(grant = "ALL")
@NamedQueries({
		@NamedQuery(name = "getEventCodesBySourceProductId", query = "Select eventCode from EventCode eventCode, EventCommCriteriaTypeMapping eventCodeMap where eventCodeMap.sourceProductId=:sourceProductId and eventCodeMap.eventCodeId=eventCode.id and eventCode.activeFlag = true"),
		@NamedQuery(name = "getCriteriaTypeBySourceAndEvent", query = "Select eventCodeMap from EventCommCriteriaTypeMapping eventCodeMap where eventCodeMap.sourceProductId=:sourceProductId and eventCodeMap.eventCodeId=:eventCodeId") })
public class EventCommCriteriaTypeMapping extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "SOURCE_PRODUCT_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long sourceProductId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_PRODUCT_ID", columnDefinition = ID_LENGTH_NINETEEN, updatable = false, insertable = false, referencedColumnName = "ID")
	private SourceProduct sourceProduct;

	@Column(name = "COMM_CRITERIA_TYPE_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long commCriteriaTypeId;

	@OneToOne
	@JoinColumn(name = "COMM_CRITERIA_TYPE_ID", columnDefinition = ID_LENGTH_NINETEEN, updatable = false, insertable = false, referencedColumnName = "ID")
	private CommunicationCriteriaType commCriteriaType;

	@Column(name = "EVENT_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long eventCodeId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EVENT_ID", columnDefinition = ID_LENGTH_NINETEEN, insertable = false, updatable = false, referencedColumnName = "ID")
	private EventCode eventCode;

	public Long getSourceProductId() {
		return sourceProductId;
	}

	public void setSourceProductId(Long sourceProductId) {
		this.sourceProductId = sourceProductId;
	}

	public SourceProduct getSourceProduct() {
		return sourceProduct;
	}

	public void setSourceProduct(SourceProduct sourceProduct) {
		this.sourceProduct = sourceProduct;
	}

	public Long getCommCriteriaTypeId() {
		return commCriteriaTypeId;
	}

	public void setCommCriteriaTypeId(Long commCriteriaTypeId) {
		this.commCriteriaTypeId = commCriteriaTypeId;
	}

	public CommunicationCriteriaType getCommCriteriaType() {
		return commCriteriaType;
	}

	public void setCommCriteriaType(CommunicationCriteriaType commCriteriaType) {
		this.commCriteriaType = commCriteriaType;
	}

	public Long getEventCodeId() {
		return eventCodeId;
	}

	public void setEventCodeId(Long eventCodeId) {
		this.eventCodeId = eventCodeId;
	}

	public EventCode getEventCode() {
		return eventCode;
	}

	public void setEventCode(EventCode eventCode) {
		this.eventCode = eventCode;
	}

}
