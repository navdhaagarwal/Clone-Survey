package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;

/**
 * @author yamini.agarwal
 * 
 */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "COM_COMMN_REQUEST_LOG_DTL", indexes = {
		@Index(name = "COM_CN_RT_LOG_DTL_IDX", columnList = "SOURCE_PRODUCT_ID, COMMUNICATION_CODE"),
		@Index(name = "COM_CN_RT_LOG_DTL_IDX1", columnList = "REQUEST_REFERENCE_ID"),
		@Index(name = "COM_CN_RT_LOG_DTL_IDX2", columnList = "PARENT_COMMN_DTL_ID"),
		@Index(name = "COM_CN_RT_LOG_DTL_IDX3", columnList = "UNIQUE_REQUEST_ID")})
@NamedQuery(name = "deleteGeneratedCommunication", query = "delete from CommunicationRequestDetail a where a.id=:id")
@Synonym(grant="ALL")
public class CommunicationRequestDetail extends CommunicationRequestBase {
    
    private static final long serialVersionUID = 1L;

    @Column(name="PARENT_COMMN_DTL_ID")
    private Long parentCommunicationRequestDetailId;
    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PARENT_COMMN_DTL_ID",referencedColumnName="ID", insertable = false, updatable = false)
    private CommunicationRequestDetail parentCommunicationRequestDetail;

    private transient Map<String, byte[]> onDemandAttachments;
    
	private transient boolean skipStorageForLetter;
	
	public Long getParentCommunicationRequestDetailId() {
        return parentCommunicationRequestDetailId;
    }

    public void setParentCommunicationRequestDetailId(
            Long parentCommunicationRequestDetailId) {
        this.parentCommunicationRequestDetailId = parentCommunicationRequestDetailId;
    }

    public CommunicationRequestDetail getParentCommunicationRequestDetail() {
        return parentCommunicationRequestDetail;
    }

    public void setParentCommunicationRequestDetail(
            CommunicationRequestDetail parentCommunicationRequestDetail) {
        this.parentCommunicationRequestDetail = parentCommunicationRequestDetail;
    }

	public Map<String, byte[]> getOnDemandAttachments() {
		return onDemandAttachments;
	}

	public void setOnDemandAttachments(Map<String, byte[]> onDemandAttachments) {
		this.onDemandAttachments = onDemandAttachments;
	}

	public boolean isSkipStorageForLetter() {
		return skipStorageForLetter;
	}

	public void setSkipStorageForLetter(boolean skipStorageForLetter) {
		this.skipStorageForLetter = skipStorageForLetter;
	}
	
}
