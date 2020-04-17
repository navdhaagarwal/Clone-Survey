
package com.nucleus.cfi.ws.client.stub.mailMessageService;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.nucleus.cfi.ws.client.stub.mailMessageService package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _TransactionId_QNAME = new QName("http://www.nucleus.com/schemas/integration/MailMessageService", "transactionId");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.nucleus.cfi.ws.client.stub.mailMessageService
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MailMessageMetadata }
     * 
     */
    public MailMessageMetadata createMailMessageMetadata() {
        return new MailMessageMetadata();
    }

    /**
     * Create an instance of {@link MailSendResponse }
     * 
     */
    public MailSendResponse createMailSendResponse() {
        return new MailSendResponse();
    }

    /**
     * Create an instance of {@link MessageDeliveryReport }
     * 
     */
    public MessageDeliveryReport createMessageDeliveryReport() {
        return new MessageDeliveryReport();
    }

    /**
     * Create an instance of {@link DeliveryReportRequest }
     * 
     */
    public DeliveryReportRequest createDeliveryReportRequest() {
        return new DeliveryReportRequest();
    }

    /**
     * Create an instance of {@link SMTPServerInfo }
     * 
     */
    public SMTPServerInfo createSMTPServerInfo() {
        return new SMTPServerInfo();
    }

    /**
     * Create an instance of {@link MailSendRequest }
     * 
     */
    public MailSendRequest createMailSendRequest() {
        return new MailSendRequest();
    }

    /**
     * Create an instance of {@link DeliveryReportResponse }
     * 
     */
    public DeliveryReportResponse createDeliveryReportResponse() {
        return new DeliveryReportResponse();
    }

    /**
     * Create an instance of {@link MailMessageContent }
     * 
     */
    public MailMessageContent createMailMessageContent() {
        return new MailMessageContent();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.nucleus.com/schemas/integration/MailMessageService", name = "transactionId")
    public JAXBElement<String> createTransactionId(String value) {
        return new JAXBElement<String>(_TransactionId_QNAME, String.class, null, value);
    }

}
