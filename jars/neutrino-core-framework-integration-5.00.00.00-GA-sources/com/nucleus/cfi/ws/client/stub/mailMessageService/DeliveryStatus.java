
package com.nucleus.cfi.ws.client.stub.mailMessageService;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deliveryStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deliveryStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DELIVERED"/>
 *     &lt;enumeration value="FAILED"/>
 *     &lt;enumeration value="DELAYED"/>
 *     &lt;enumeration value="NOT_APPLICABLE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "deliveryStatus")
@XmlEnum
public enum DeliveryStatus {

    DELIVERED,
    FAILED,
    DELAYED,
    NOT_APPLICABLE;

    public String value() {
        return name();
    }

    public static DeliveryStatus fromValue(String v) {
        return valueOf(v);
    }

}
