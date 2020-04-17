
package com.nucleus.cfi.ws.client.stub.mailMessageService;

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
 *         &lt;element name="MessageMetadata" type="{http://www.nucleus.com/schemas/integration/MailMessageService}mailMessageMetadata"/>
 *         &lt;element name="MessageContent" type="{http://www.nucleus.com/schemas/integration/MailMessageService}mailMessageContent"/>
 *         &lt;element name="asyncRequest" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "messageMetadata",
    "messageContent",
    "asyncRequest"
})
@XmlRootElement(name = "mailSendRequest")
public class MailSendRequest {

    @XmlElement(name = "MessageMetadata", required = true)
    protected MailMessageMetadata messageMetadata;
    @XmlElement(name = "MessageContent", required = true)
    protected MailMessageContent messageContent;
    protected Boolean asyncRequest;

    /**
     * Gets the value of the messageMetadata property.
     * 
     * @return
     *     possible object is
     *     {@link MailMessageMetadata }
     *     
     */
    public MailMessageMetadata getMessageMetadata() {
        return messageMetadata;
    }

    /**
     * Sets the value of the messageMetadata property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailMessageMetadata }
     *     
     */
    public void setMessageMetadata(MailMessageMetadata value) {
        this.messageMetadata = value;
    }

    /**
     * Gets the value of the messageContent property.
     * 
     * @return
     *     possible object is
     *     {@link MailMessageContent }
     *     
     */
    public MailMessageContent getMessageContent() {
        return messageContent;
    }

    /**
     * Sets the value of the messageContent property.
     * 
     * @param value
     *     allowed object is
     *     {@link MailMessageContent }
     *     
     */
    public void setMessageContent(MailMessageContent value) {
        this.messageContent = value;
    }

    /**
     * Gets the value of the asyncRequest property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isAsyncRequest() {
        return asyncRequest;
    }

    /**
     * Sets the value of the asyncRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setAsyncRequest(Boolean value) {
        this.asyncRequest = value;
    }

}
