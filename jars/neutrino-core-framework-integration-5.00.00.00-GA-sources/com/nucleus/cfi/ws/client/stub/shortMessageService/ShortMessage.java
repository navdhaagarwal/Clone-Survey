
package com.nucleus.cfi.ws.client.stub.shortMessageService;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.7-b01-
 * Generated source version: 2.1
 * 
 */
@WebService(name = "shortMessage", targetNamespace = "http://www.nucleus.com/schemas/integration/ShortMessageService")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@XmlSeeAlso({
    ObjectFactory.class
})
public interface ShortMessage {


    /**
     * 
     * @param shortMessageSendRequest
     * @return
     *     returns com.nucleus.cfi.ws.client.stub.shortMessageService.ShortMessageSendResponse
     */
    @WebMethod
    @WebResult(name = "shortMessageSendResponse", targetNamespace = "http://www.nucleus.com/schemas/integration/ShortMessageService", partName = "shortMessageSendResponse")
    public ShortMessageSendResponse shortMessageSend(
        @WebParam(name = "shortMessageSendRequest", targetNamespace = "http://www.nucleus.com/schemas/integration/ShortMessageService", partName = "shortMessageSendRequest")
        ShortMessageSendRequest shortMessageSendRequest);

}
