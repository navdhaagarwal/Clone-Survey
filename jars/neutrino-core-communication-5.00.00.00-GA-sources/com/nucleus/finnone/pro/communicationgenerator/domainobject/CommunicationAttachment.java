package com.nucleus.finnone.pro.communicationgenerator.domainobject;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;
/**
 * @author gajendra.jatav
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name="COM_COMMN_ATTACHMENTS")
@Cacheable
@Synonym(grant="SELECT")
public class CommunicationAttachment extends BaseMasterEntity{

   private static final long serialVersionUID = 1L;
   
   @ManyToOne
   @JoinColumn(name="PARENT_COMMN_ID")
   private CommunicationName parentCommunication;
   
   @OneToOne
   @JoinColumn(name="ATTACHED_COMM_ID")
   private CommunicationName attachedCommunication;
   public CommunicationName getParentCommunication() {
      return parentCommunication;
   }
   public void setParentCommunication(CommunicationName parentCommunication) {
      this.parentCommunication = parentCommunication;
   }
   public CommunicationName getAttachedCommunication() {
      return attachedCommunication;
   }
   public void setAttachedCommunication(CommunicationName attachedCommunication) {
      this.attachedCommunication = attachedCommunication;
   }
    @Override
   protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
       CommunicationAttachment communicationAttachment = (CommunicationAttachment) baseEntity;
           super.populate(communicationAttachment, cloneOptions);
           communicationAttachment.setParentCommunication(parentCommunication);
           communicationAttachment.setAttachedCommunication(attachedCommunication);
           
    }
       
       
   @Override
   protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
      CommunicationAttachment communicationAttachment = (CommunicationAttachment) baseEntity;
           super.populateFrom(communicationAttachment, cloneOptions);
           this.setParentCommunication(communicationAttachment.getParentCommunication());
           this.setAttachedCommunication(communicationAttachment.attachedCommunication);               
       }
      
}
