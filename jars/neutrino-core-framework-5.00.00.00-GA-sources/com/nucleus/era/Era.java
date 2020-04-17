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
package com.nucleus.era;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.core.system.util.SystemPropertyUtils;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

/**
 * Entity to represent a ERA for Japanese Year in the system.
 * 
 * @author Nucleus Software Exports Limited
 */

@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="SELECT")
public class Era extends BaseMasterEntity {

    @Transient
    private static final long serialVersionUID = 1L;
    /**
     * Attribute to hold name of the era 
     */
    @Sortable
    private String            eraName;
    /**
     * Attribute to hold symbol of the era 
     */
    private Character         eraSymbol;
    /**
     * Attribute to hold start year of the era 
     */
    private Integer           startYear;

    /**
     * @return the eraName
     */
    public String getEraName() {
        return eraName;
    }

    /**
     * @param EraName the EraName to set
     */
    public void setEraName(String eraName) {
        this.eraName = eraName;
    }

    /**
     * @return the eraSymbol
     */
    public Character getEraSymbol() {
        return eraSymbol;
    }

    /**
     * @param EraSymbol the EraSymbol to set
     */
    public void setEraSymbol(Character eraSymbol) {
        this.eraSymbol = eraSymbol;
    }

    /**
     * @return the startYear of era
     */
    public Integer getStartYear() {
        return startYear;
    }

    /**
     * @param StartYear the StartYear to set
     */
    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Era era = (Era) baseEntity;
        super.populate(era, cloneOptions);
        era.setEraName(eraName);
        era.setEraSymbol(eraSymbol);
        era.setStartYear(startYear);
    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
        Era era = (Era) baseEntity;
        super.populateFrom(era, cloneOptions);
        this.setEraName(era.getEraName());
        this.setEraSymbol(era.getEraSymbol());
        this.setStartYear(era.getStartYear());

    }

    @Override
    public String getDisplayName() {
        return eraName;
    }

    /**
     *to print the logger
     */

    public String getLogInfo() {
        String log = null;
        StringBuffer stf = new StringBuffer();
        stf.append("Era Master Object received to be saved ------------> ");
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Era Name : " + eraName);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Era Symbol :" + eraSymbol);
        stf.append(SystemPropertyUtils.getNewline());
        stf.append("Era Start Year :" + startYear);
        log = stf.toString();
        return log;
    }
}
