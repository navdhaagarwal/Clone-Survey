package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;

@Entity 
@DynamicInsert 
@DynamicUpdate
@Table(name ="COM_COMMN_GENERATION_HST", indexes = {        
        @Index(name = "COM_COMMN_GENERATION_HST_IDX2", columnList = "COMMUNICATION_CODE"),
        @Index(name = "COM_COMMN_GENERATION_HST_IDX3", columnList = "UNIQUE_REQUEST_ID"),
        @Index(name = "COM_COMMN_GENERATION_HST_IDX4", columnList = "SUBJECT_REFERENCE_NUMBER")})
@Synonym(grant="ALL")
public class CommunicationGenerationDetailHistory extends CommunicationRequestBase {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;    
    

    @Column(name="PARENT_COMMN_DTL_ID")
    private Long parentCommunicationGenerationDetailHistoryId;

    
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="PARENT_COMMN_DTL_ID",referencedColumnName="ID", insertable = false, updatable = false)
    private CommunicationGenerationDetailHistory parentCommunicationGenerationDetailHistory;


    public CommunicationGenerationDetailHistory getParentCommunicationGenerationDetailHistory() {
		return parentCommunicationGenerationDetailHistory;
	}

	public void setParentCommunicationGenerationDetailHistory(
			CommunicationGenerationDetailHistory parentCommunicationGenerationDetailHistory) {
		this.parentCommunicationGenerationDetailHistory = parentCommunicationGenerationDetailHistory;
	}

	public Long getParentCommunicationGenerationDetailHistoryId() {
		return parentCommunicationGenerationDetailHistoryId;
	}

	public void setParentCommunicationGenerationDetailHistoryId(
			Long parentCommunicationGenerationDetailHistoryId) {
		this.parentCommunicationGenerationDetailHistoryId = parentCommunicationGenerationDetailHistoryId;
	}

	
    
    
}
