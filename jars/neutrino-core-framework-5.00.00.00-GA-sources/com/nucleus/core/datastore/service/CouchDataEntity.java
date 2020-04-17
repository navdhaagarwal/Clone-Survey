package com.nucleus.core.datastore.service;


import org.ektorp.Attachment;
import org.ektorp.support.CouchDbDocument;

public class CouchDataEntity extends CouchDbDocument {


    private static final long serialVersionUID = -8957168561295513349L;

    public CouchDataEntity() {
        super();
    }
     
    @Override
    public void addInlineAttachment(Attachment a) {
        super.addInlineAttachment(a);
    }  
    @Override
    protected void removeAttachment(String id){
       super.removeAttachment(id);
       
    }
	
	   
}