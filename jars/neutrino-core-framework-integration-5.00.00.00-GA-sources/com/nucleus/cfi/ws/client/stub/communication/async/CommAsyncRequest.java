
package com.nucleus.cfi.ws.client.stub.communication.async;

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
 *         &lt;element name="communicationAcknowledgement" type="{http://www.nucleus.com/schemas/commAsyncService}communicationAcknowledgement"/>
 *         &lt;element name="isErrorResponse" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
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
    "communicationAcknowledgement",
    "isErrorResponse"
})
@XmlRootElement(name = "commAsyncRequest")
public class CommAsyncRequest {

    @XmlElement(required = true)
    protected CommunicationAcknowledgement communicationAcknowledgement;
    protected Boolean isErrorResponse;

    /**
     * Gets the value of the communicationAcknowledgement property.
     * 
     * @return
     *     possible object is
     *     {@link CommunicationAcknowledgement }
     *     
     */
    public CommunicationAcknowledgement getCommunicationAcknowledgement() {
        return communicationAcknowledgement;
    }

    /**
     * Sets the value of the communicationAcknowledgement property.
     * 
     * @param value
     *     allowed object is
     *     {@link CommunicationAcknowledgement }
     *     
     */
    public void setCommunicationAcknowledgement(CommunicationAcknowledgement value) {
        this.communicationAcknowledgement = value;
    }

    /**
     * Gets the value of the isErrorResponse property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsErrorResponse() {
        return isErrorResponse;
    }

    /**
     * Sets the value of the isErrorResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsErrorResponse(Boolean value) {
        this.isErrorResponse = value;
    }

}
