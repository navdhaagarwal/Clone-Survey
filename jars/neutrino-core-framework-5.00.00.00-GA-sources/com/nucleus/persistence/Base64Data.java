/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.SerializationUtils;

/**
 * @author Nucleus Software Exports Limited
 */
@Embeddable
public class Base64Data {

    @Column(name = "base64_data", length = 4000)
    private String base64Data;

    public Base64Data() {
    }

    public Base64Data(Serializable data) {
        setData(data);
    }

    public Serializable getData() {
        if (base64Data != null) {
            return (Serializable) SerializationUtils.deserialize(Base64.decodeBase64(base64Data.getBytes()));
        }
        return null;
    }

    public void setData(Serializable data) {
        base64Data = new String(Base64.encodeBase64(SerializationUtils.serialize(data)));
    }

}
