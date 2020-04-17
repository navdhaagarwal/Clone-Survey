
package com.nucleus.cfi.ws.client.stub.mailMessageService;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


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
 *         &lt;element name="messageOriginatorId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="queryTimestamp" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;choice>
 *           &lt;element name="queryFilter" type="{http://www.nucleus.com/schemas/integration/MailMessageService}deliveryReportQuery"/>
 *           &lt;element name="messageReceiptId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;/choice>
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
    "messageOriginatorId",
    "queryTimestamp",
    "queryFilter",
    "messageReceiptId"
})
@XmlRootElement(name = "deliveryReportRequest")
public class DeliveryReportRequest {

    @XmlElement(required = true)
    protected String messageOriginatorId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar queryTimestamp;
    protected DeliveryReportQuery queryFilter;
    protected String messageReceiptId;

    /**
     * Gets the value of the messageOriginatorId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageOriginatorId() {
        return messageOriginatorId;
    }

    /**
     * Sets the value of the messageOriginatorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageOriginatorId(String value) {
        this.messageOriginatorId = value;
    }

    /**
     * Gets the value of the queryTimestamp property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getQueryTimestamp() {
        return queryTimestamp;
    }

    /**
     * Sets the value of the queryTimestamp property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setQueryTimestamp(XMLGregorianCalendar value) {
        this.queryTimestamp = value;
    }

    /**
     * Gets the value of the queryFilter property.
     * 
     * @return
     *     possible object is
     *     {@link DeliveryReportQuery }
     *     
     */
    public DeliveryReportQuery getQueryFilter() {
        return queryFilter;
    }

    /**
     * Sets the value of the queryFilter property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeliveryReportQuery }
     *     
     */
    public void setQueryFilter(DeliveryReportQuery value) {
        this.queryFilter = value;
    }

    /**
     * Gets the value of the messageReceiptId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageReceiptId() {
        return messageReceiptId;
    }

    /**
     * Sets the value of the messageReceiptId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageReceiptId(String value) {
        this.messageReceiptId = value;
    }

}
