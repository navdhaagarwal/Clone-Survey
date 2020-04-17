/**
 *
 */
package com.nucleus.finnone.pro.lmsbase.domainobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.lmsbase.constants.LMSBaseApplicationConstants.BOD_IN_PROGRESS;
import static com.nucleus.finnone.pro.lmsbase.constants.LMSBaseApplicationConstants.PENDING_BOD;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.CHARACTER;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.DATE;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.STRING_LENGTH_NINETEEN;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.STRING_LENGTH_FIVE_HUNDRED;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.ID;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.STRING_LENGTH_TWELVE;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.NUMERIC_LENGTH_ONE;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.STRING_LENGTH_FIFTY;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.STRING_LENGTH_HUNDRED;
import static com.nucleus.finnone.pro.base.constants.CoreJPADataTypeDefinitionConstants.STRING_LENGTH_FIFTEEN;



import java.util.Date;
import java.util.Locale;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.address.Country;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseTenant;
import com.nucleus.entity.EntityLifeCycleData;
import com.nucleus.finnone.pro.general.constants.YesNoCharacterEnum;

/**
 * @author iss
 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
 */
@Entity 
@DynamicInsert 
@DynamicUpdate
@Synonym(grant="ALL")
@Cacheable
@Inheritance(strategy=InheritanceType.JOINED)
public class Tenant extends BaseTenant{

  
    private static final long serialVersionUID = 1L;
    
	@Column(name = "MICR_CODE")
	private String micrCode;
	
	@Column(name="HOST_BANK_ID", length=STRING_LENGTH_NINETEEN)
	private Long hostBankId;

	@Transient
	private String interBranchAccountId;

	/**
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	@Column(name = "DATE_FORMAT", length = STRING_LENGTH_TWELVE)
	private String dateFormat;

	/**
	 *  Make and Authorize status. Only following status are possible: N - When a new record is added till it is not authorized A - Authorized record.  M - When an authorized record is selected for modification, status of authorized record is marked as �M� and status of this record in temporary block is also shown as �M�.  D - Status is marked as �D� for deletion.
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */	
	@Column(name = "MC_STATUS", length = CHARACTER)
	private Character makeAuthorizeStatus;


	/**
	 *  I-Indian, M-Million
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */	
	@Column(name = "AMOUNT_FORMAT", length = CHARACTER)
	private String amountFormat;

	@Column(name="BUSINESS_DATE", columnDefinition = DATE)
	private Date businessDate;
	
	@Column(name="PREVIOUS_BUSINESS_DATE", columnDefinition = DATE)
	private Date previousBusinessDate;
	
	@Column(name="PROCESSED_TILL_DATE", columnDefinition = DATE)
	private Date processedTillDate;	
	
	@Column(name = "STATUS", length = CHARACTER)
	private Character eodStatus;

	@Column(name = "TENANT_IMAGE_PATH", length = STRING_LENGTH_FIVE_HUNDRED)
	private String imagePath;

	@Column(name = "IP_ADDRESS_FOR_IMAGE", length = STRING_LENGTH_HUNDRED)
	private String imageIPAddress;
	
	@Column(name = "DEFAULT_LOGO_NAME", length = STRING_LENGTH_FIFTY)
	private String defaultLogo;
	
	@Column(name = "DEFAULT_ADV_LOGO_NAME", length = STRING_LENGTH_FIFTY)
	private String defaultAdvLogo;

	@Column(name = "FOOTER_TEXT_KEY", length = STRING_LENGTH_FIFTY)
	private String footerTextKey;

	@Column(name = "REGISTERED_OFFICE", length = STRING_LENGTH_FIVE_HUNDRED)
	private	String	registeredOffice;

	@Column(name = "CORPORATE_OFFICE", length = STRING_LENGTH_FIVE_HUNDRED)	
	private	String	corporateOffice;

	@Column(name = "FIXED_LINE1", length = STRING_LENGTH_FIFTEEN)
	private	String	fixedLine1;

	@Column(name = "FIXED_LINE2", length = STRING_LENGTH_FIFTEEN)
	private	String	fixedLine2;

	@Column(name = "MOBILE", length = STRING_LENGTH_FIFTEEN)
	private	String	mobileNo;

	@Column(name = "EMAIL", length = STRING_LENGTH_FIFTY)
	private	String	email;

	@Column(name = "WEBSITE", length =  STRING_LENGTH_FIFTY)
	private	String	website;

	@Column(name="COUNTRY_ID", columnDefinition = ID)
	private Long countryId;
	
	@ManyToOne
	@JoinColumn(name = "COUNTRY_ID", referencedColumnName="ID",insertable=false, updatable=false)
	private Country country; 
	
	@Column(name="DEFAULT_SEARCH_ON_CIF", columnDefinition = NUMERIC_LENGTH_ONE)
	private Long defaultCif;
	
	@Column(name = "EOD_HDR_PROCESS_GROUP_ID", columnDefinition = ID)
	private Long eodLogProcessGroupId;	
	
	@Embedded
	private EntityLifeCycleData entityLifeCycleData;
	
	@Column(name="BANK_BRANCH_MST_ID", columnDefinition= ID)
	private Long bankBranchId;
	
	@Column(name="COLLECTION_PROCESSED_TILL_DATE", columnDefinition = DATE)
	private Date collectionProcessTillDate;

	@Column(name="CASH_FLOW_PROCESS_RUN_DATE", columnDefinition = DATE)
	private Date lastCashFlowProcessRunDate;
	 
	@Column(name="DEFAULT_CUSTODIAN_ID", columnDefinition=ID)
	private Long defaultCustodianId;
	
	@Column(name = "TIME_FORMAT", length = STRING_LENGTH_TWELVE)
	private String timeFormat;
	  
	@Column(name = "COLL_BUSINESS_DATE", columnDefinition = DATE)
	private Date collBusinessDate;
	  
	@Column(name = "COLL_PROCESS_TILL_DATE", columnDefinition = DATE)
	private Date processDate;
	  
	@Column(name = "COLL_PREV_FLOW_MIS_DATE", columnDefinition = DATE)
	private Date previousFlowMISDate;
	  
	@Column(name = "COLL_CURRENT_FLOW_MIS_DATE", columnDefinition = DATE)
	private Date currentFlowMISDate;
	  
	@Column(name = "COLL_BOD_STATUS", length = CHARACTER)
	private Character bodStatus;
	  
	
	@Column(name = "COLL_BOD_REFERENCE_NO", columnDefinition = ID)
	private Long collBODReferenceNo;

	/**
	 * @return the amountFormat
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public String getAmountFormat() {
		return amountFormat;
	}

	/**
	 * @return the businessDate
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public Date getBusinessDate() {
		return businessDate;
	}

	public Date getProcessedTillDate() {
		return processedTillDate;
	}

	public String getCorporateOffice() {
		return corporateOffice;
	}


	/**
	 * @return the dateFormat
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public String getDateFormat() {
		return dateFormat;
	}


	public String getDefaultAdvLogo() {
		return defaultAdvLogo;
	}


	public String getDefaultLogo() {
		return defaultLogo;
	}

	public String getEmail() {
		return email;
	}

	public Character getEodStatus() {
		return eodStatus;
	}

	public String getFixedLine1() {
		return fixedLine1;
	}

	

	public String getFixedLine2() {
		return fixedLine2;
	}

	/**
	 * @return the footerTextKey
	 */
	public String getFooterTextKey() {
		return footerTextKey;
	}

	/**
	 * @return the id
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */

	
	public String getImageIPAddress() {
		return imageIPAddress;
	}
	
	public String getImagePath() {
		return imagePath;
	}
	
	public String getInterBranchAccountId() {
		return interBranchAccountId;
	}

	/**
	 * @return the makeAuthorizeStatus
	 * @generated "UML to Java V5.0 (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public Character getMakeAuthorizeStatus() {
		return makeAuthorizeStatus;
	}
	
	
	
	public String getMicrCode() {
		return micrCode;
	}
	
	public String getMobileNo() {
		return mobileNo;
	}
	
	
	public String getRegisteredOffice() {
		return registeredOffice;
	}
	
	
	public String getWebsite() {
		return website;
	}
	
	
	public Long getBankBranchId() {
		return bankBranchId;
	}

	public boolean isStatusNo(){
		return (YesNoCharacterEnum.NO.getEnumValue()).equals(eodStatus);
	}
	
	public boolean isStatusYes(){
		return (YesNoCharacterEnum.YES.getEnumValue()).equals(eodStatus);
	}
	
	public boolean isStatusPendingBOD(){
		return (PENDING_BOD).equals(eodStatus);
	}
	
	public void markStatusNo(){
		setEodStatus(YesNoCharacterEnum.NO.getEnumValue());
	}

	public void markStatusYes(){
		setEodStatus(YesNoCharacterEnum.YES.getEnumValue());
	}

	public void markStatusPendingBOD(){
		setEodStatus(PENDING_BOD);
	}
	
	public void markStatusBODInProgress(){
		setEodStatus(BOD_IN_PROGRESS);
	}
	
	public void setAmountFormat(String theAmountFormat) {
		amountFormat = theAmountFormat;
	}

	
	public void setBusinessDate(Date theBusinessDate) {
		businessDate = theBusinessDate;
	}

	public void setProcessedTillDate(Date processedTillDate) {
		this.processedTillDate = processedTillDate;
	}

	public void setCorporateOffice(String corporateOffice) {
		this.corporateOffice = corporateOffice;
	}
	
	
	public void setDateFormat(String theDateFormat) {
		dateFormat = theDateFormat;
	}


	public void setDefaultAdvLogo(String defaultAdvLogo) {
		this.defaultAdvLogo = defaultAdvLogo;
	}

	public void setDefaultLogo(String defaultLogo) {
		this.defaultLogo = defaultLogo;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEodStatus(Character theStatus) {
		eodStatus = theStatus;
	}

	public void setFixedLine1(String fixedLine1) {
		this.fixedLine1 = fixedLine1;
	}

	public void setFixedLine2(String fixedLine2) {
		this.fixedLine2 = fixedLine2;
	}

	public void setFooterTextKey(String footerTextKey) {
		this.footerTextKey = footerTextKey;
	}

	public void setImageIPAddress(String imageIPAddress) {
		this.imageIPAddress = imageIPAddress;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}
	
	public void setInterBranchAccountId(String interBranchAccountId) {
		this.interBranchAccountId = interBranchAccountId;
	}


	public void setMakeAuthorizeStatus(Character theMakeAuthorizeStatus) {
		makeAuthorizeStatus = theMakeAuthorizeStatus;
	}

	public void setMicrCode(String micrCode) {
		this.micrCode = micrCode;
	}

	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}


	public void setRegisteredOffice(String registeredOffice) {
		this.registeredOffice = registeredOffice;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public Long getCountryId() {
		return countryId;
	}

	public void setCountryId(Long countryId) {
		this.countryId = countryId;
	}

	public Country getCountry() {
		return country;
	}

	public void setCountry(Country country) {
		this.country = country;
	}
	
	public EntityLifeCycleData getEntityLifeCycleData() {

	  return isNull(this.entityLifeCycleData)?new EntityLifeCycleData():this.entityLifeCycleData;
	}

	public void setEntityLifeCycleData(EntityLifeCycleData entityLifeCycleData){
	    this.entityLifeCycleData = entityLifeCycleData;
	}
	
	public Locale getDefaultLocale(){
		return  new Locale(getLocale());
	}
	


	public Boolean getDefaultCif() {
		if (defaultCif == null) {
			return Boolean.FALSE;
		}
		return defaultCif.equals(1L) ? Boolean.TRUE : Boolean.FALSE;
	}

	public void setDefaultCif(Boolean defaultCif) {
		if (defaultCif == null) {
			this.defaultCif = null;
		}
		else {
			this.defaultCif = defaultCif ? 1L : 0L;
		}
	}

	public Long getEodLogProcessGroupId() {
		return this.eodLogProcessGroupId;
	}

	public void setEodLogProcessGroupId(Long eodLogProcessGroupId) {
		this.eodLogProcessGroupId = eodLogProcessGroupId;
	}

	public void setBankBranchId(Long bankBranchId) {
		this.bankBranchId = bankBranchId;
	}

	public Date getCollectionProcessTillDate() {
		return collectionProcessTillDate;
	}

	public void setCollectionProcessTillDate(Date collectionProcessTillDate) {
		this.collectionProcessTillDate = collectionProcessTillDate;
	}

	public Date getLastCashFlowProcessRunDate() {
		return lastCashFlowProcessRunDate;
	}

	public void setLastCashFlowProcessRunDate(Date lastCashFlowProcessRunDate) {
		this.lastCashFlowProcessRunDate = lastCashFlowProcessRunDate;
	}
	
	public Long getHostBankId() {
		return hostBankId;
	}

	public void setHostBankId(Long hostBankId) {
		this.hostBankId = hostBankId;
	}

    public Long getDefaultCustodianId() {
      return defaultCustodianId;
    }
  
    public void setDefaultCustodianId(Long defaultCustodianId) {
      this.defaultCustodianId = defaultCustodianId;
    }

	public Date getPreviousBusinessDate() {
		return previousBusinessDate;
	}

	/**
	 * This Date should be set only at the Initiate EOD event. The purpose of this attribute is to store 
	 * the date on which EOD was initiated. This further helps in identifying holidays and reporting dates
	 * that have fallen between the initiated business date and future business date
	 * 
	 * No action required on Initiate 
	 * 		Initiate BOD, 
	 * 		Restart EOD,
	 *      Restart BOD
	 * 		Value Dated EOD
	 *  	Ad-Hoc EOD
	 *  
	 * @param previousBusinessDate
	 */
	public void setPreviousBusinessDate(Date previousBusinessDate) {
		this.previousBusinessDate = previousBusinessDate;
	}

    public String getTimeFormat() {
    return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
    this.timeFormat = timeFormat;
    }

    public Date getCollBusinessDate() {
    return collBusinessDate;
    }

    public void setCollBusinessDate(Date collBusinessDate) {
    this.collBusinessDate = collBusinessDate;
    }

    public Date getProcessDate() {
    return processDate;
    }

    public void setProcessDate(Date processDate) {
    this.processDate = processDate;
    }

    public Date getPreviousFlowMISDate() {
    return previousFlowMISDate;
    }

    public void setPreviousFlowMISDate(Date previousFlowMISDate) {
    this.previousFlowMISDate = previousFlowMISDate;
    }

    public Date getCurrentFlowMISDate() {
    return currentFlowMISDate;
    }

    public void setCurrentFlowMISDate(Date currentFlowMISDate) {
    this.currentFlowMISDate = currentFlowMISDate;
    }

    public Character getBodStatus() {
    return bodStatus;
    }

    public void setBodStatus(Character bodStatus) {
    this.bodStatus = bodStatus;
   }

    public Long getCollBODReferenceNo() {
      return collBODReferenceNo;
    }

    public void setCollBODReferenceNo(Long collBODReferenceNo) {
      this.collBODReferenceNo = collBODReferenceNo;
    }

	
    
    
}