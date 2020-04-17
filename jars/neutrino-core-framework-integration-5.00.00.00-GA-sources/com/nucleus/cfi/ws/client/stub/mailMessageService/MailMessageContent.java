
package com.nucleus.cfi.ws.client.stub.mailMessageService;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for mailMessageContent complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="mailMessageContent">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="messageContentEncoding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="messageContentByteStream" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "mailMessageContent", propOrder = {
    "messageContentEncoding",
    "messageContentByteStream"
})
public class MailMessageContent {

    protected String messageContentEncoding;
    @XmlElement(required = true)
    @XmlMimeType("message/rfc822")
    protected DataHandler messageContentByteStream;

    /**
     * Gets the value of the messageContentEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessageContentEncoding() {
        return messageContentEncoding;
    }

    /**
     * Sets the value of the messageContentEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessageContentEncoding(String value) {
        this.messageContentEncoding = value;
    }

    /**
     * Gets the value of the messageContentByteStream property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getMessageContentByteStream() {
        return messageContentByteStream;
    }

    /**
     * Sets the value of the messageContentByteStream property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setMessageContentByteStream(DataHandler value) {
        this.messageContentByteStream = value;
    }

}
