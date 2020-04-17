/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - ï¿½ 2012. All rights
 * reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.loanproduct;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Sortable;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;


/**
 * Entity class to represent product type (ProductType = ProductCategory) in the system.
 * 
 * The values will come from database seeding and therefore there are no setters.
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Cacheable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Synonym(grant = "ALL")
@Table(indexes={@Index(name="shortName_index",columnList="shortName"), @Index(name="description_index",columnList="description") })

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = ProductType.class
)

public class ProductType extends BaseEntity {

    private static final long   serialVersionUID       = 1172715856164345328L;
    /** As per discussion with Ankur Kamra we are adding one new field called 'Code' and our existing system will work on
    // short names, short name will not change for predefined constant as there are 657 references to these constants, code will be according to story CAS-6530
    */
    // Constant for short name Consumer Vehicle Loan
    public static final String  MOVABLE_ASSET_LOAN     = "MAL";
    // Constant for short name Personal Financial Loan
    public static final String  PERSONAL_LOAN          = "PL";
    // Constant for short name Consumer Durable Loan
    public static final String  CONSUMER_LOAN          = "CL";
    // Constant for short name Home Loan
    public static final String  MORTGAGE_LOAN          = "ML";
    public static final String  CREDIT_CARD            = "CC";
    public static final String  LOAN_AGAINST_PROPERTY  = "LAP";
    public static final String  EQUIPMENT              = "EQUIPMNT";
    public static final String  COMMERCIAL_VEHICLE     = "CV";
    public static final String  MEDICAL_TREATMENT      = "MT";
    public static final String  EDUCATION              = "EDU";
    public static final String  TOURISM                = "TOURISM";
    public static final String  MICRO_HOUSING_LOAN     = "MHL";
    public static final String  APLUS_HOME_LOAN        = "AHL";
    public static final String  FARM_EQUIPMENT_LOAN    = "FE";
    public static final String  AGRICULTURE_LOAN       = "AGRL";
    public static final String  KISAN_CREDIT_CARD_LOAN = "KCC";
    public static final String  SELF_HELP_GROUP        = "SHG";
    public static final String  JOINT_LIABILITY_GROUP  = "JLG";
    public static final String  COMMERCIAL_EQUIPMENT   = "CEQ";

    public static final String  ACTIVE_PRODUCT         = "Y";

    public static final String  OMNI_LOAN              = "OMNI";
    public static final String  GOLD_LOAN              = "GL";
    public static final String  CONSUMER_VEHICLE       = "CON_VEH";
    
    public static final String  CUSTOMER_FIRST_LIMIT       = "Cust_Limit";
    public static final String  FINANCE_AGAINST_SECURITY       = "FAS";

    @Sortable
    private String              shortName;

    private String              code;

    @Sortable
    private String              description;

    private String              retailCorporateOptionFlag;

    private String              fundBasedFlag;

    private String              securedFlag;

    private String              assetCollateralOptionFlag;

    private String              installmentBasedFlag;

    private String              insuranceBasedFlag;

    private boolean             multiline;

    @ManyToMany
    @JoinTable(name = "JT_PROD_MANDATORY_POLICY_TYPE")
    private Set<LoanPolicyType> mandatoryPolicyTypes;

    @OneToMany
    @JoinColumn(name = "product_type_fk")
    private List<LoanType>      loanTypes;

    @ManyToOne(fetch = FetchType.LAZY)
    private ProductCategory     productCategory;

    // Possible values (N,n) for InActive Product Type.For all other values the Product Type will remain Active
    private String              isActive;

    private Boolean             offlineFlag            = Boolean.FALSE;


    public Boolean getOfflineFlag() {
        return notNull(offlineFlag) ? offlineFlag : Boolean.FALSE;
    }

    public void setOfflineFlag(Boolean offlineFlag) {
        this.offlineFlag = notNull(offlineFlag) ? offlineFlag : Boolean.FALSE;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public ProductType() {
    }

    public ProductType(String shortName) {
        this.shortName = shortName;
    }

    /**
     * 
     * Getter for the code property
     * @return
     */
    public String getCode() {
        return code;
    }

    /**
     * 
     * Getter for the shortName property
     * @return
     */

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getShortName() {
        return shortName;
    }

    /**
     * 
     * Getter for the description property
     * @return
     */
    public String getDescription() {
        return description;
    }

    public boolean isForRetail() {
        return retailCorporateOptionFlag.equalsIgnoreCase("r");
    }

    public boolean isForCorporate() {
        return retailCorporateOptionFlag.equalsIgnoreCase("c");
    }

    public boolean isForBothRetailAndCorporate() {
        return retailCorporateOptionFlag.equalsIgnoreCase("b");
    }

    public boolean isFundBased() {
        return fundBasedFlag.equalsIgnoreCase("y");
    }

    public boolean isSecuredLoanProduct() {
        return securedFlag.equalsIgnoreCase("y");
    }

    public boolean isAssetBased() {
        return assetCollateralOptionFlag.equalsIgnoreCase("a");
    }

    public boolean isCollateralBased() {
        return assetCollateralOptionFlag.equalsIgnoreCase("c");
    }

    public boolean isInstallmentBased() {
        return installmentBasedFlag.equalsIgnoreCase("y");
    }

    public boolean isInsuranceBased() {
        return insuranceBasedFlag.equalsIgnoreCase("y");
    }

    /**
     * 
     * Getter for the mandatoryPolicyTypes property
     * @return
     */
    public Set<LoanPolicyType> getMandatoryPolicyTypes() {
        return mandatoryPolicyTypes;
    }

    /**
     * 
     * Setter for MandatoryPolicyTypes Property
     * @param mandatoryPolicyTypes
     */
    public void setMandatoryPolicyTypes(Set<LoanPolicyType> mandatoryPolicyType) {
        this.mandatoryPolicyTypes = mandatoryPolicyType;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        ProductType productType = (ProductType) baseEntity;
        super.populate(productType, cloneOptions);
        productType
                .setMandatoryPolicyTypes((mandatoryPolicyTypes != null && mandatoryPolicyTypes.size() > 0) ? mandatoryPolicyTypes
                        : null);
        productType.assetCollateralOptionFlag = assetCollateralOptionFlag;
        productType.description = description;
        productType.fundBasedFlag = fundBasedFlag;
        productType.installmentBasedFlag = installmentBasedFlag;
        productType.insuranceBasedFlag = insuranceBasedFlag;
        productType.loanTypes = loanTypes;
        productType.multiline = multiline;
        productType.retailCorporateOptionFlag = retailCorporateOptionFlag;
        productType.securedFlag = securedFlag;
        productType.shortName = shortName;
        productType.code = code;
        if (null != loanTypes && loanTypes.size() > 0) {
            productType.setLoanTypes(new ArrayList<LoanType>(loanTypes));
        }
        productType.setProductCategory(productCategory);
    }

    public List<LoanType> getLoanTypes() {
        return loanTypes;
    }

    public void setLoanTypes(List<LoanType> loanTypes) {
        this.loanTypes = loanTypes;
    }

    public String getRetailCorporateOptionFlag() {
        return retailCorporateOptionFlag;
    }

    public String getFundBasedFlag() {
        return fundBasedFlag;
    }

    public String getSecuredFlag() {
        return securedFlag;
    }

    public String getAssetCollateralOptionFlag() {
        return assetCollateralOptionFlag;
    }

    public String getInstallmentBasedFlag() {
        return installmentBasedFlag;
    }

    public String getInsuranceBasedFlag() {
        return insuranceBasedFlag;
    }

    public boolean isMultiline() {
        return multiline;
    }

    @Override
    public String getDisplayName() {
        return shortName;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

}
