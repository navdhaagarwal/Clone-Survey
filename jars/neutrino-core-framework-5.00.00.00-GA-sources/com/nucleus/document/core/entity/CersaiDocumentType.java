package com.nucleus.document.core.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CersaiDocumentType extends GenericParameter {

    private static final long  serialVersionUID                = -7332576033229298932L;

    public static final String CERSAI_DOCUMENT_TYPE_SALE_DEED  = "Sale_Deed";
    public static final String CERSAI_DOCUMENT_TYPE_Lease_DEED = "Lease_Deed";
    public static final String CERSAI_DOCUMENT_TYPE_Award      = "Award";
    public static final String CERSAI_DOCUMENT_TYPE_Others     = "Others";

}
