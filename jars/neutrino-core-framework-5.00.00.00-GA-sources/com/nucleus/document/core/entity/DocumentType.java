package com.nucleus.document.core.entity;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
public class DocumentType extends GenericParameter {
	private static final long serialVersionUID = 7807376737539208428L;
	public static final String DOCUMENT_TYPE_GROUP = "DocumentTypeGroupDocument";
	public static final String DOCUMENT_TYPE_FINANCIAL_STATEMENT = "FinancialStatement";
	public static final String DOCUMENT_TYPE_INDIVIDUAL = "DocumentTypeIndividualDocument";

}
