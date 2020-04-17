/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.core.event.service;

import java.io.Serializable;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * 
 * @author Nucleus Software Exports Limited Implementation class for rule
 *         service
 *         Class added to resolve the template containing collections
 */
public class EventExecutionVO implements Serializable {

    private static final long serialVersionUID = 1L;
    private FieldsMetadata    metadata;
    private boolean 						   auditingEnabled=true;
    private boolean 						   purgingRequired=false;

    /**
     * @return the metadata
     */
    public FieldsMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata the metadata to set
     */
    public void setMetadata(FieldsMetadata metadata) {
        this.metadata = metadata;
    }

	public boolean isAuditingEnabled() {
		return auditingEnabled;
	}

	public void setAuditingEnabled(boolean auditingEnabled) {
		this.auditingEnabled = auditingEnabled;
	}

	public boolean isPurgingRequired() {
		return purgingRequired;
	}

	public void setPurgingRequired(boolean purgingRequired) {
		this.purgingRequired = purgingRequired;
	}
    
    

}
