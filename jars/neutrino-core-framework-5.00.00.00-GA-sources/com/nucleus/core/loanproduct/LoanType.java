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
package com.nucleus.core.loanproduct;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

/**
 * Entity class to represent loan type which comes on the basis of product type.
 * 
 * @deprecated - {@link ContractType} should be used onwards and modules taking impact of deprecating this class should adopt the
 * changes as soon as possible.
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Deprecated
@Synonym(grant="ALL")
@Table(indexes={@Index(name="loanTypeName_index",columnList="loanTypeName"),@Index(name="product_type_fk_index",columnList="product_type_fk")})
public class LoanType extends BaseEntity {
    private static final long serialVersionUID = 7807376737539208428L;
    private String            loanTypeName;
    private String            loanDescription;

    @ManyToOne
    @JoinColumn(name = "product_type_fk")
    private ProductType       productType;

    /**
     * 
     * Getter for the ProductType property
     * @return
     */
    public ProductType getProductType() {
        return productType;
    }

    /**
     * 
     * Setter for ProductType Property
     * @param productType
     */
    public void setProductType(ProductType product) {
        this.productType = product;
    }

    /**
     * 
     * Getter for the LoanTypeName property
     * @return
     */
    public String getLoanTypeName() {
        return loanTypeName;
    }

    /**
     * 
     * Setter for LoanTypeName Property
     * @param loanTypeName
     */
    public void setLoanTypeName(String loanTypeName) {
        this.loanTypeName = loanTypeName;
    }

    /**
     * 
     * Getter for the loanDescription property
     * @return
     */
    public String getLoanDescription() {
        return loanDescription;
    }

    /**
     * 
     * Setter for Loan Description Property
     * @param loanDescription
     */
    public void setLoanDescription(String loanDescription) {
        this.loanDescription = loanDescription;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        LoanType loanType = (LoanType) baseEntity;
        super.populate(loanType, cloneOptions);
        loanType.setLoanDescription(loanDescription);
        loanType.setLoanTypeName(loanTypeName);
        loanType.setProductType(productType);

    }
}
