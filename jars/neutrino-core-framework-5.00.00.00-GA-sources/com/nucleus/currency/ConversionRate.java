package com.nucleus.currency;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="effective_From_index",columnList="effectiveFrom")})
public class ConversionRate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Column
    @Temporal(TemporalType.DATE)
    //@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private Date         effectiveFrom;

    @Column(precision = 25, scale = 12)
    private BigDecimal        conversionFactor;

    public Date getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(Date effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public BigDecimal getConversionFactor() {
        return conversionFactor;
    }

    public void setConversionFactor(BigDecimal conversionFactor) {
        this.conversionFactor = conversionFactor;
    }
    public Boolean isEmpty () {
    	if (this.effectiveFrom == null || this.conversionFactor == null) {
    		return Boolean.TRUE;
    	} else {
    		return Boolean.FALSE;
    	}
    }
    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ConversionRate conversionRate = (ConversionRate) baseEntity;
        super.populate(conversionRate, cloneOptions);
        
        conversionRate.setEffectiveFrom(effectiveFrom);
        conversionRate.setConversionFactor(conversionFactor);
    }
    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
    	ConversionRate conversionRate = (ConversionRate) baseEntity;
        super.populateFrom(conversionRate, cloneOptions);
        this.setEffectiveFrom(conversionRate.getEffectiveFrom());
        this.setConversionFactor(conversionRate.getConversionFactor());
    }

}
