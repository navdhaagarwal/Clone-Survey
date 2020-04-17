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
package com.nucleus.core.communication;

import java.util.Comparator;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.joda.time.DateTimeComparator;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.document.core.entity.Document;
import com.nucleus.entity.BaseEntity;

/**
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
@Table(indexes={@Index(name="comm_fk_index_CommTrails",columnList="communication_fk")})
public class CommunicationTrail extends BaseEntity {

    private static final long   serialVersionUID = -7059781626595268194L;

    private boolean             isCustomerTranscript;

    private String              communicationTranscript;

    @ManyToOne
    private CommunicationStatus status;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "communication_fk")
    private List<Document>      attachedDocuments;
    
    /**
     * @return the isCustomerTranscript
     */
    public boolean getIsCustomerTranscript() {
        return isCustomerTranscript;
    }

    /**
     * @param isCustomerTranscript the isCustomerTranscript to set
     */
    public void setIsCustomerTranscript(boolean isCustomerTranscript) {
        this.isCustomerTranscript = isCustomerTranscript;
    }

    /**
     * @return the communicationTranscript
     */
    public String getCommunicationTranscript() {
        return communicationTranscript;
    }

    /**
     * @param communicationTranscript the communicationTranscript to set
     */
    public void setCommunicationTranscript(String communicationTranscript) {
        this.communicationTranscript = communicationTranscript;
    }

    /**
     * @return the status
     */
    public CommunicationStatus getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(CommunicationStatus status) {
        this.status = status;
    }

    /**
     * @return the attachedDocuments
     */
    public List<Document> getAttachedDocuments() {
        return attachedDocuments;
    }

    /**
     * @param attachedDocuments the attachedDocuments to set
     */
    public void setAttachedDocuments(List<Document> attachedDocuments) {
        this.attachedDocuments = attachedDocuments;
    }
}
