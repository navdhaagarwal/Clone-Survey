
package com.nucleus.cfi.ws.client.stub.mailMessageService;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for deliveryReportQuery.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="deliveryReportQuery">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="DELIVERED_ONLY"/>
 *     &lt;enumeration value="FAILED_ONLY"/>
 *     &lt;enumeration value="DELAYED_ONLY"/>
 *     &lt;enumeration value="ALL"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "deliveryReportQuery")
@XmlEnum
public enum DeliveryReportQuery {

    DELIVERED_ONLY,
    FAILED_ONLY,
    DELAYED_ONLY,
    ALL;

    public String value() {
        return name();
    }

    public static DeliveryReportQuery fromValue(String v) {
        return valueOf(v);
    }

}
