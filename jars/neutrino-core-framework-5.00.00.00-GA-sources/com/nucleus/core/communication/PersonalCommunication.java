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

import com.nucleus.address.Address;

/**
 * The Class PersonalCommunication.
 *
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class PersonalCommunication extends Communication {

    private static final long serialVersionUID = -8968496477749900099L;

    @OneToOne(cascade = CascadeType.ALL, fetch=FetchType.LAZY)
    private Address           visitedAddress;

    /**
     * Gets the visited address.
     *
     * @return the visitedAddress
     */
    public Address getVisitedAddress() {
        return visitedAddress;
    }

    /**
     * Sets the visited address.
     *
     * @param visitedAddress the visitedAddress to set
     */
    public void setVisitedAddress(Address visitedAddress) {
        this.visitedAddress = visitedAddress;
    }

}
