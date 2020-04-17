package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;

@Entity
@DynamicInsert 
@DynamicUpdate
@Table(name="COM_COMMN_EVENT_REQUEST_HST")
@Synonym(grant="ALL")
public class CommunicationEventRequestHistory extends CommunicationEventRequestBase {

	private static final long serialVersionUID = 1L;
}
