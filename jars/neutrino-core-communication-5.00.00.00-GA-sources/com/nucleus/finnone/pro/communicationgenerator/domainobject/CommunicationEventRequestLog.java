package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.finnone.pro.additionaldata.domainobject.AdditionalData;
import com.nucleus.rules.model.SourceProduct;

@Entity
@DynamicInsert 
@DynamicUpdate
@Table(name="COM_COMMN_EVENT_REQUEST_LOG",
indexes={
		@Index(name="COMMN_EVENT_REQ_LOG_IDX1",columnList="SOURCE_PRODUCT_ID"),
		@Index(name="COMMN_EVENT_REQ_LOG_IDX2",columnList="ADDL_FIELD_TXN_ID"),		
		@Index(name="COMMN_EVENT_REQ_LOG_IDX4",columnList="EVENT_CODE"),
		@Index(name="COMMN_EVENT_REQ_LOG_IDX5",columnList="SOURCE_PRODUCT_ID, EVENT_CODE, STATUS")
		})

@NamedQuery(name = "fetchAllInitiatedCommEvent", query = "select a from CommunicationEventRequestLog a where a.status=:status  and a.entityLifeCycleData.persistenceStatus=:persistenceStatus order by sourceProduct")
@Synonym(grant="ALL")
public class CommunicationEventRequestLog extends CommunicationEventRequestBase {

	private static final long serialVersionUID = 1L;
	
	
	
	public CommunicationEventRequestLog(CommunicationEventSearchBuilder communicationEventSearchBuilder){
		super();
		setAdditionalData(communicationEventSearchBuilder.additionalData);
		setApplicablePrimaryEntityURI(communicationEventSearchBuilder.applicablePrimaryEntityURI);
		setSubjectURI(communicationEventSearchBuilder.subjectURI);
		setSubjectReferenceNumber(communicationEventSearchBuilder.subjectReferenceNumber);
		setSubjectReferenceType(communicationEventSearchBuilder.subjectReferenceType);
		setEventCode(communicationEventSearchBuilder.eventCode);
		setSourceProduct(communicationEventSearchBuilder.sourceProduct);
		setStatus(communicationEventSearchBuilder.status);
		setReferenceDate(communicationEventSearchBuilder.referenceDate);
	}
	
	public CommunicationEventRequestLog(){
		super();
	}
	public static class CommunicationEventSearchBuilder implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 321312312311L;
		private SourceProduct sourceProduct;
		private String eventCode;
		private Character status;
		private String subjectURI;
		private String subjectReferenceNumber;
		private String subjectReferenceType;
		private AdditionalData additionalData;
		private String applicablePrimaryEntityURI;
		private Date referenceDate;
		
		
		public CommunicationEventSearchBuilder setSourceProduct(SourceProduct module) {
			this.sourceProduct=module;
			return this;
		}
		
		public CommunicationEventSearchBuilder setEventCode(String eventCode) {
			this.eventCode=eventCode;
			return this;
		}
		
		public CommunicationEventSearchBuilder setStatus(Character status) {
			this.status=status;
			return this;
		}

		public CommunicationEventSearchBuilder setSubjectURI(String subjectURI) {
			this.subjectURI = subjectURI;
			return this;
		}

		public CommunicationEventSearchBuilder setAdditionalData(AdditionalData additionalData) {
			this.additionalData = additionalData;
			return this;
		}

		public CommunicationEventSearchBuilder setApplicablePrimaryEntityURI(String applicablePrimaryEntityURI) {
			this.applicablePrimaryEntityURI = applicablePrimaryEntityURI;
			return this;
		}
		
		public CommunicationEventSearchBuilder getSubjectReferenceNumber(String subjectReferenceNumber) {
			this.subjectReferenceNumber = subjectReferenceNumber;
			return this;
		}
		
		public CommunicationEventSearchBuilder getSubjectReferenceType(String subjectReferenceType) {
			this.subjectReferenceType = subjectReferenceType;
			return this;
		}
		
		public CommunicationEventSearchBuilder setReferenceDate(Date referenceDate) {
			this.referenceDate = referenceDate;
			return this;
		}
		
		
		public CommunicationEventRequestLog build(){
			return new CommunicationEventRequestLog(this);
		}
		
		
	}
}
