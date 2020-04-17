
package com.nucleus.cfi.ws.client.stub.mailMessageService;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="messageDeliveryReports" type="{http://www.nucleus.com/schemas/integration/MailMessageService}messageDeliveryReport" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "messageDeliveryReports"
})
@XmlRootElement(name = "deliveryReportResponse")
public class DeliveryReportResponse {

    @XmlElement(nillable = true)
    protected List<MessageDeliveryReport> messageDeliveryReports;

    /**
     * Gets the value of the messageDeliveryReports property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the messageDeliveryReports property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMessageDeliveryReports().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link MessageDeliveryReport }
     * 
     * 
     */
    public List<MessageDeliveryReport> getMessageDeliveryReports() {
        if (messageDeliveryReports == null) {
            messageDeliveryReports = new ArrayList<MessageDeliveryReport>();
        }
        return this.messageDeliveryReports;
    }

}
