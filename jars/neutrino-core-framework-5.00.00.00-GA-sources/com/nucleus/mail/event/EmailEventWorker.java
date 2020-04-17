package com.nucleus.mail.event;

import java.util.List;
import java.util.Map;

import com.nucleus.core.event.NeutrinoEvent;
import com.nucleus.core.event.NeutrinoEventPublisher;
import com.nucleus.core.event.NeutrinoEventWorker;
import com.nucleus.document.core.entity.Document;
import com.nucleus.mail.MimeMailMessageBuilder;

public class EmailEventWorker extends NeutrinoEventWorker {

    private Map                    contextmap;
    private List<String>           emailList;
    private Document               attachedDocument;
    private String                 emailSubject;
    private String                 emailBody;
    private MimeMailMessageBuilder builder;

    public EmailEventWorker(String name) {
        super(name);
    }

    public Map getContextmap() {
        return contextmap;
    }

    public void setContextmap(Map contextmap) {
        this.contextmap = contextmap;
    }

    public List<String> getEmailList() {
        return emailList;
    }

    public void setEmailList(List<String> emailList) {
        this.emailList = emailList;
    }

    public Document getAttachedDocument() {
        return attachedDocument;
    }

    public void setAttachedDocument(Document attachedDocument) {
        this.attachedDocument = attachedDocument;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    @Override
    public NeutrinoEvent createNeutrinoEvent(NeutrinoEventPublisher publisher) {
        EmailEvent event = new EmailEvent(publisher, emailSubject, this);
        return event;
    }

    public MimeMailMessageBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(MimeMailMessageBuilder builder) {
        this.builder = builder;
    }

}
