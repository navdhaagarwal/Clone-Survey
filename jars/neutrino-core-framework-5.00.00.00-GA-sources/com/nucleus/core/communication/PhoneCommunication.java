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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.contact.PhoneNumber;

/**
 * The Class PhoneCommunication.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class PhoneCommunication extends Communication {

    private static final long serialVersionUID = -8281605907566157115L;

    @OneToOne(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
    private PhoneNumber       contactNumber;

    /**
     * Gets the contact number.
     *
     * @return the contactNumber
     */
    public PhoneNumber getContactNumber() {
        return contactNumber;
    }

    /**
     * Sets the contact number.
     *
     * @param contactNumber the contactNumber to set
     */
    public void setContactNumber(PhoneNumber contactNumber) {
        this.contactNumber = contactNumber;
    }

}
