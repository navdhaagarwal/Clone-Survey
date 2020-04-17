/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.document.core.entity;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;

/**
 * Document package represents top level entity for document definition.
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class DocumentPackage extends BaseEntity {

    private static final long        serialVersionUID = 7589954340868292136L;

    /*JoinTable name given to keep table name under 30 chars for oracle*/
    @ManyToMany
    @JoinTable(name = "JT_DP_CHILD_DOC_PACKAGES")
    private Set<DocumentPackage>    childDocumentPackages;

    /*JoinTable name given to keep table name under 30 chars for oracle*/
    @ManyToMany
    @JoinTable(name = "JT_DP_CHILD_DOC_DEFS")
    private Set<DocumentDefinition> documentDefinitions;

    public Set<DocumentDefinition> getDocumentDefinitions() {
        return documentDefinitions;
    }

    public void setDocumentDefinitions(Set<DocumentDefinition> documentDefinition) {
        this.documentDefinitions = documentDefinition;
    }

    public Set<DocumentPackage> getChildDocumentPackages() {
        return childDocumentPackages;
    }

    public void setChildDocumentPackages(Set<DocumentPackage> documentPackages) {
        this.childDocumentPackages = documentPackages;
    }

}
