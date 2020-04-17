package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import static com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingConstants.ID_LENGTH_NINETEEN;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.event.EventCode;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.rules.model.SourceProduct;

@Entity
@Cacheable
@DynamicInsert
@DynamicUpdate
@Table(name = "COMM_EVENT_MAP_HDR")
@NamedQueries({
		@NamedQuery(name = "getCommunicationCodesBySourceProductId", query = "Select communicationCode from CommunicationName as communicationCode where communicationCode.sourceProduct.id=:sourceProductId and communicationCode.activeFlag = true and communicationCode.templateBased=true order by lower(communicationCode.communicationCode)"),
		@NamedQuery(name = "getCommunicationTypeByCommunicationCodeId", query = "Select communicationCode.communicationType from CommunicationName as communicationCode where communicationCode.id=:communicationCodeId and communicationCode.activeFlag = true"),
		@NamedQuery(name = "getCommunicationTemplateByCommunicationTypeId", query = "Select communicationTemplate from CommunicationTemplate as communicationTemplate where communicationTemplate.communication.communicationType.id=:communicationTypeId and communicationTemplate.activeFlag = true"),
		@NamedQuery(name = "checkDuplicateCommunicationEventMapping", query = "Select eventMapping from CommunicationEventMappingHeader as eventMapping where eventMapping.sourceProductId =:sourceProductId and eventMapping.eventCodeId =:eventCodeId and eventMapping.masterLifeCycleData.approvalStatus in (0,6,2,4,7,8) and eventMapping.activeFlag = true "),
		@NamedQuery(name = "getCommunicationEventMappings", query = "Select  communicationEventMappingHeader from CommunicationEventMappingHeader as communicationEventMappingHeader  where communicationEventMappingHeader.masterLifeCycleData.approvalStatus in (:approvalStatus)  and communicationEventMappingHeader.sourceProductId=:sourceProductId and communicationEventMappingHeader.eventCode.code =:eventCode and communicationEventMappingHeader.activeFlag = true "),
		@NamedQuery(name = "getAllCommunicationEventMappings", query = "Select  communicationEventMappingHeader from CommunicationEventMappingHeader as communicationEventMappingHeader  where communicationEventMappingHeader.masterLifeCycleData.approvalStatus in (:approvalStatus) and communicationEventMappingHeader.activeFlag = true")})

@Synonym(grant = "ALL")
public class CommunicationEventMappingHeader extends BaseMasterEntity {

	private static final long serialVersionUID = 1L;

	private static final String ENTITY_DISPLAY_NAME="Communication Event Mapping";
	private static final String DISPLAY_NAME_PREFIX="for event ";

	@Column(name = "SOURCE_PRODUCT_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long sourceProductId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SOURCE_PRODUCT_ID", columnDefinition = ID_LENGTH_NINETEEN, updatable = false, insertable = false, referencedColumnName = "ID")
	private SourceProduct sourceProduct;

	@Column(name = "COMM_CATEGORY_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long communicationCategoryId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMM_CATEGORY_ID", columnDefinition = ID_LENGTH_NINETEEN, updatable = false, insertable = false, referencedColumnName = "ID")
	private CommunicationCategory communicationCategory;

	@Column(name = "EVENT_ID", columnDefinition = ID_LENGTH_NINETEEN)
	private Long eventCodeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "EVENT_ID", columnDefinition = ID_LENGTH_NINETEEN, insertable = false, updatable = false, referencedColumnName = "ID")
	private EventCode eventCode;

	@OrderBy("priority")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "COMM_EVENT_HDR_MAP_DTL_ID", columnDefinition = ID_LENGTH_NINETEEN, referencedColumnName = "ID")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
	private List<CommunicationEventMappingDetail> communicationEventMappingDetails;

	public Long getCommunicationCategoryId() {
		return communicationCategoryId;
	}

	public void setCommunicationCategoryId(Long communicationCategoryId) {
		this.communicationCategoryId = communicationCategoryId;
	}

	public CommunicationCategory getCommunicationCategory() {
		return communicationCategory;
	}

	public void setCommunicationCategory(CommunicationCategory communicationCategory) {
		this.communicationCategory = communicationCategory;
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

	public List<CommunicationEventMappingDetail> getCommunicationEventMappingDetails() {
		return communicationEventMappingDetails;
	}

	public void setCommunicationEventMappingDetails(
			List<CommunicationEventMappingDetail> communicationEventMappingDetails) {
		this.communicationEventMappingDetails = communicationEventMappingDetails;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationEventMappingHeader communicationEventMappingHeader = (CommunicationEventMappingHeader) baseEntity;
		super.populate(communicationEventMappingHeader, cloneOptions);
		communicationEventMappingHeader.setSourceProductId(sourceProductId);
		communicationEventMappingHeader.setSourceProduct(sourceProduct);
		communicationEventMappingHeader.setEventCodeId(eventCodeId);
		communicationEventMappingHeader.setCommunicationCategoryId(communicationCategoryId);
		communicationEventMappingHeader.setEventCode(eventCode);
		communicationEventMappingHeader
				.setCommunicationEventMappingDetails(new ArrayList<CommunicationEventMappingDetail>());
		if (communicationEventMappingDetails != null) {
			for (CommunicationEventMappingDetail communicationEventMappingDetail : communicationEventMappingDetails) {
				if (communicationEventMappingHeader.getCommunicationEventMappingDetails() != null
						&& communicationEventMappingDetail != null) {
					communicationEventMappingHeader.getCommunicationEventMappingDetails()
							.add((CommunicationEventMappingDetail) communicationEventMappingDetail
									.cloneYourself(cloneOptions));
				}
			}
		}
		communicationEventMappingHeader.setTenantId(getTenantId());
		communicationEventMappingHeader.setActiveFlag(isActiveFlag());
		communicationEventMappingHeader.setMakeBusinessDate(getMakeBusinessDate());
		communicationEventMappingHeader.getEntityLifeCycleData()
				.setLastUpdatedTimeStamp(getEntityLifeCycleData().getLastUpdatedTimeStamp());
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		CommunicationEventMappingHeader communicationEventMappingHeader = (CommunicationEventMappingHeader) baseEntity;
		super.populateFrom(communicationEventMappingHeader, cloneOptions);
		this.setSourceProductId(communicationEventMappingHeader.getSourceProductId());
		this.setSourceProduct(communicationEventMappingHeader.getSourceProduct());
		this.setEventCodeId(communicationEventMappingHeader.getEventCodeId());
		this.setCommunicationCategoryId(communicationEventMappingHeader.getCommunicationCategoryId());
		this.setEventCode(communicationEventMappingHeader.getEventCode());
		this.getCommunicationEventMappingDetails().clear();
		if (communicationEventMappingHeader.getCommunicationEventMappingDetails() != null) {
			for (CommunicationEventMappingDetail communicationEventMappingDetail : communicationEventMappingHeader
					.getCommunicationEventMappingDetails()) {
				if (this.getCommunicationEventMappingDetails() != null && communicationEventMappingDetail != null) {
					this.getCommunicationEventMappingDetails()
							.add((CommunicationEventMappingDetail) communicationEventMappingDetail
									.cloneYourself(cloneOptions));
				}
			}
		}
		this.setTenantId(getTenantId());
		this.setActiveFlag(isActiveFlag());
		this.setMakeBusinessDate(communicationEventMappingHeader.getMakeBusinessDate());
		this.getEntityLifeCycleData().setLastUpdatedTimeStamp(
				communicationEventMappingHeader.getEntityLifeCycleData().getLastUpdatedTimeStamp());
	}

	/*
	 * returns the CommunicationEventMappingDetail list for the top priority if
	 * deliveryPriority is ZERO or NULL, otherwise returns the list for the priority
	 * next the given delivery priority.
	 */
	public List<CommunicationEventMappingDetail> getCommunicationEvtMapDtls(Long deliveryPriority) {
		TreeMap<Long, List<CommunicationEventMappingDetail>> map = new TreeMap<>();
		for (CommunicationEventMappingDetail evtMapDtl : this.getCommunicationEventMappingDetails()) {
			if (map.containsKey(evtMapDtl.getPriority())) {
				map.get(evtMapDtl.getPriority()).add(evtMapDtl);
			} else {
				List<CommunicationEventMappingDetail> list = new ArrayList<>();
				list.add(evtMapDtl);
				map.put(evtMapDtl.getPriority(), list);
			}
		}
		if (deliveryPriority == null || deliveryPriority == 0) {
			return map.get(map.firstKey());
		}
		return map.get(map.higherKey(deliveryPriority));

	}

	
	@Override
	public String getDisplayName() {
		return this.eventCode!=null ? DISPLAY_NAME_PREFIX.concat(this.eventCode.getCode()):super.getDisplayName();
	}
	
	@Override
	public String getEntityDisplayName() {		
		return ENTITY_DISPLAY_NAME;
	}
	
	

}
