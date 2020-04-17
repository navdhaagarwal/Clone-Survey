/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.configuration.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.formsConfiguration.PersistentFormData;
import com.nucleus.entity.BaseEntity;

/**
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class CommonConfiguration extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String            name;
    
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private PersistentFormData      persistentFormData;
    
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

	/**
	 * @return the persistentFormData
	 */
	public PersistentFormData getPersistentFormData() {
		return persistentFormData;
	}

	/**
	 * @param persistentFormData the persistentFormData to set
	 */
	public void setPersistentFormData(PersistentFormData persistentFormData) {
		this.persistentFormData = persistentFormData;
	}
    
    

}
