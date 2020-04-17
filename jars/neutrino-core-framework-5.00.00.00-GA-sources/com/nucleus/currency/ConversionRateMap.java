package com.nucleus.currency;

import java.math.BigDecimal;
import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import javax.persistence.Column;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant="ALL")
public class ConversionRateMap extends BaseEntity {

    private static final long serialVersionUID = 5896241914572201391L;
    
    @Column(precision = 25, scale = 12)
    private BigDecimal convRate;
    
    private String currency;
    
    private Long subLoanId;

	public BigDecimal getConvRate() {
		return convRate;
	}

	public void setConvRate(BigDecimal convRate) {
		this.convRate = convRate;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	public Long getSubLoanId() {
		return subLoanId;
	}

	public void setSubLoanId(Long subLoanId) {
		this.subLoanId = subLoanId;
	}

	@Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
		ConversionRateMap conversionRateMap = (ConversionRateMap) baseEntity;
		super.populate(conversionRateMap, cloneOptions);
		conversionRateMap.setConvRate(convRate);
		conversionRateMap.setCurrency(currency);
		conversionRateMap.setSubLoanId(subLoanId);
    }
    
}
