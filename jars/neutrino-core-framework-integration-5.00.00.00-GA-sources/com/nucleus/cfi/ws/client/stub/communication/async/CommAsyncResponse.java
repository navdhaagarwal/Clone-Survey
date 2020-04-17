
package com.nucleus.cfi.ws.client.stub.communication.async;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
 *         &lt;element name="callbackResponse" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "callbackResponse"
})
@XmlRootElement(name = "commAsyncResponse")
public class CommAsyncResponse {

    protected String callbackResponse;

    /**
     * Gets the value of the callbackResponse property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCallbackResponse() {
        return callbackResponse;
    }

    /**
     * Sets the value of the callbackResponse property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCallbackResponse(String value) {
        this.callbackResponse = value;
    }

}
