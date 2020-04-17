/**
@author merajul.ansari
Creation Date: 29/01/2013
Copyright: Nucleus Software Exports Ltd
Description: Entity for Additional Field Transaction Data
Program Specs Referred: 
----------------------------------------------------------------------------------------------------------------
Revision:  Version	Last Revision Date	 	Name		Function / Module affected       Modifications Done
----------------------------------------------------------------------------------------------------------------	
	       1.0		29/01/2013				Merajul Hasan Ansari 	initial version      
----------------------------------------------------------------------------------------------------------------
 *
 */
package com.nucleus.finnone.pro.additionaldata.domainobject;

import static com.nucleus.finnone.pro.additionaldata.constants.AdditionalDataConstants.ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.additionaldata.constants.AdditionalDataConstants;

@MappedSuperclass
public abstract class AdditionalDataBasic  extends BaseEntity{
	
	@Transient
	private static final long serialVersionUID = 1L;
	
	@Column(name = "REF_TRANSACTION_TYPE", nullable = false, length=AdditionalDataConstants.ADDL_DATA_STRING_LENGTH_TWO)
	private String transactionType;
	
	@Column(name = "ADDL_FIELD_01", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField1;
	
	@Column(name = "ADDL_FIELD_02", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField2;
	
	@Column(name = "ADDL_FIELD_03", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField3;
	
	@Column(name = "ADDL_FIELD_04", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField4;
	
	@Column(name = "ADDL_FIELD_05", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField5;
	
	@Column(name = "ADDL_FIELD_06", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField6;
	
	@Column(name = "ADDL_FIELD_07", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField7;
	
	@Column(name = "ADDL_FIELD_08", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField8;
	
	@Column(name = "ADDL_FIELD_09", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField9;
	
	@Column(name = "ADDL_FIELD_10", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField10;
	
	@Column(name = "ADDL_FIELD_11", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField11;
	
	@Column(name = "ADDL_FIELD_12", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField12;
	
	@Column(name = "ADDL_FIELD_13", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField13;
	
	@Column(name = "ADDL_FIELD_14", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField14;
	
	@Column(name = "ADDL_FIELD_15", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField15;
	
	@Column(name = "ADDL_FIELD_16", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField16;
	
	@Column(name = "ADDL_FIELD_17", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField17;
	
	@Column(name = "ADDL_FIELD_18", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField18;
	
	@Column(name = "ADDL_FIELD_19", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField19;
	
	@Column(name = "ADDL_FIELD_20", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField20;
	
	@Column(name = "ADDL_FIELD_21", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField21;
	
	@Column(name = "ADDL_FIELD_22", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField22;
	
	@Column(name = "ADDL_FIELD_23", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField23;
	
	@Column(name = "ADDL_FIELD_24", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField24;
	
	@Column(name = "ADDL_FIELD_25", length=ADDL_DATA_STRING_LENGTH_FOUR_HUNDRED)
	private String additionalField25;
	
	public AdditionalDataBasic(){
		
	}

	public AdditionalDataBasic(AdditionalDataBasic additionalData) {
		this.transactionType = additionalData.getTransactionType();
		this.additionalField1 = additionalData.getAdditionalField1();
		this.additionalField2 = additionalData.getAdditionalField2();
		this.additionalField3 = additionalData.getAdditionalField3();
		this.additionalField4 = additionalData.getAdditionalField4();
		this.additionalField5 = additionalData.getAdditionalField5();
		this.additionalField6 = additionalData.getAdditionalField6();
		this.additionalField7 = additionalData.getAdditionalField7();
		this.additionalField8 = additionalData.getAdditionalField8();
		this.additionalField9 = additionalData.getAdditionalField9();
		this.additionalField10 = additionalData.getAdditionalField10();
		this.additionalField11 = additionalData.getAdditionalField11();
		this.additionalField12 = additionalData.getAdditionalField12();
		this.additionalField13 = additionalData.getAdditionalField13();
		this.additionalField14 = additionalData.getAdditionalField14();
		this.additionalField15 = additionalData.getAdditionalField15();
		this.additionalField16 = additionalData.getAdditionalField16();
		this.additionalField17 = additionalData.getAdditionalField17();
		this.additionalField18 = additionalData.getAdditionalField18();
		this.additionalField19 = additionalData.getAdditionalField19();
		this.additionalField20 = additionalData.getAdditionalField20();
		this.additionalField21 = additionalData.getAdditionalField21();
		this.additionalField22 = additionalData.getAdditionalField22();
		this.additionalField23 = additionalData.getAdditionalField23();
		this.additionalField24 = additionalData.getAdditionalField24();
		this.additionalField25 = additionalData.getAdditionalField25();
	}

	/** 
	 * @return the additionalField1
	 */
	public String getAdditionalField1() {
		// begin-user-code
		return additionalField1;
		// end-user-code
	}

	/** 
	 * @return the additionalField10
	 */
	public String getAdditionalField10() {
		// begin-user-code
		return additionalField10;
		// end-user-code
	}

	/** 
	 * @return the additionalField11
	 */
	public String getAdditionalField11() {
		// begin-user-code
		return additionalField11;
		// end-user-code
	}

	/** 
	 * @return the additionalField12
	 */
	public String getAdditionalField12() {
		// begin-user-code
		return additionalField12;
		// end-user-code
	}

	/** 
	 * @return the additionalField13
	 */
	public String getAdditionalField13() {
		// begin-user-code
		return additionalField13;
		// end-user-code
	}

	/** 
	 * @return the additionalField14
	 */
	public String getAdditionalField14() {
		// begin-user-code
		return additionalField14;
		// end-user-code
	}

	/** 
	 * @return the additionalField15
	 */
	public String getAdditionalField15() {
		// begin-user-code
		return additionalField15;
		// end-user-code
	}

	/** 
	 * @return the additionalField16
	 */
	public String getAdditionalField16() {
		// begin-user-code
		return additionalField16;
		// end-user-code
	}

	/** 
	 * @return the additionalField17
	 */
	public String getAdditionalField17() {
		// begin-user-code
		return additionalField17;
		// end-user-code
	}

	/** 
	 * @return the additionalField18
	 */
	public String getAdditionalField18() {
		// begin-user-code
		return additionalField18;
		// end-user-code
	}

	/** 
	 * @return the additionalField19
	 */
	public String getAdditionalField19() {
		// begin-user-code
		return additionalField19;
		// end-user-code
	}

	/** 
	 * @return the additionalField2
	 */
	public String getAdditionalField2() {
		// begin-user-code
		return additionalField2;
		// end-user-code
	}

	/** 
	 * @return the additionalField20
	 */
	public String getAdditionalField20() {
		// begin-user-code
		return additionalField20;
		// end-user-code
	}

	/** 
	 * @return the additionalField21
	 */
	public String getAdditionalField21() {
		// begin-user-code
		return additionalField21;
		// end-user-code
	}

	/** 
	 * @return the additionalField22
	 */
	public String getAdditionalField22() {
		// begin-user-code
		return additionalField22;
		// end-user-code
	}

	/** 
	 * @return the additionalField23
	 */
	public String getAdditionalField23() {
		// begin-user-code
		return additionalField23;
		// end-user-code
	}

	/** 
	 * @return the additionalField24
	 */
	public String getAdditionalField24() {
		// begin-user-code
		return additionalField24;
		// end-user-code
	}

	/** 
	 * @return the additionalField25
	 */
	public String getAdditionalField25() {
		// begin-user-code
		return additionalField25;
		// end-user-code
	}

	/** 
	 * @return the additionalField3
	 */
	public String getAdditionalField3() {
		// begin-user-code
		return additionalField3;
		// end-user-code
	}

	/** 
	 * @return the additionalField4
	 */
	public String getAdditionalField4() {
		// begin-user-code
		return additionalField4;
		// end-user-code
	}

	/** 
	 * @return the additionalField5
	 */
	public String getAdditionalField5() {
		// begin-user-code
		return additionalField5;
		// end-user-code
	}

	/** 
	 * @return the additionalField6
	 */
	public String getAdditionalField6() {
		// begin-user-code
		return additionalField6;
		// end-user-code
	}

	/** 
	 * @return the additionalField7
	 */
	public String getAdditionalField7() {
		// begin-user-code
		return additionalField7;
		// end-user-code
	}

	/** 
	 * @return the additionalField8
	 */
	public String getAdditionalField8() {
		// begin-user-code
		return additionalField8;
		// end-user-code
	}

	/** 
	 * @return the additionalField9
	 */
	public String getAdditionalField9() {
		// begin-user-code
		return additionalField9;
		// end-user-code
	}

	

	/** 
	 * @return the transactionType
	 */
	public String getTransactionType() {
		// begin-user-code
		return transactionType;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField1 the additionalField1 to set
	 */
	public void setAdditionalField1(String theAdditionalField1) {
		// begin-user-code
		additionalField1 = theAdditionalField1;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField10 the additionalField10 to set
	 */
	public void setAdditionalField10(String theAdditionalField10) {
		// begin-user-code
		additionalField10 = theAdditionalField10;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField11 the additionalField11 to set
	 */
	public void setAdditionalField11(String theAdditionalField11) {
		// begin-user-code
		additionalField11 = theAdditionalField11;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField12 the additionalField12 to set
	 */
	public void setAdditionalField12(String theAdditionalField12) {
		// begin-user-code
		additionalField12 = theAdditionalField12;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField13 the additionalField13 to set
	 */
	public void setAdditionalField13(String theAdditionalField13) {
		// begin-user-code
		additionalField13 = theAdditionalField13;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField14 the additionalField14 to set
	 */
	public void setAdditionalField14(String theAdditionalField14) {
		// begin-user-code
		additionalField14 = theAdditionalField14;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField15 the additionalField15 to set
	 */
	public void setAdditionalField15(String theAdditionalField15) {
		// begin-user-code
		additionalField15 = theAdditionalField15;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField16 the additionalField16 to set
	 */
	public void setAdditionalField16(String theAdditionalField16) {
		// begin-user-code
		additionalField16 = theAdditionalField16;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField17 the additionalField17 to set
	 */
	public void setAdditionalField17(String theAdditionalField17) {
		// begin-user-code
		additionalField17 = theAdditionalField17;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField18 the additionalField18 to set
	 */
	public void setAdditionalField18(String theAdditionalField18) {
		// begin-user-code
		additionalField18 = theAdditionalField18;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField19 the additionalField19 to set
	 */
	public void setAdditionalField19(String theAdditionalField19) {
		// begin-user-code
		additionalField19 = theAdditionalField19;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField2 the additionalField2 to set
	 */
	public void setAdditionalField2(String theAdditionalField2) {
		// begin-user-code
		additionalField2 = theAdditionalField2;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField20 the additionalField20 to set
	 */
	public void setAdditionalField20(String theAdditionalField20) {
		// begin-user-code
		additionalField20 = theAdditionalField20;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField21 the additionalField21 to set
	 */
	public void setAdditionalField21(String theAdditionalField21) {
		// begin-user-code
		additionalField21 = theAdditionalField21;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField22 the additionalField22 to set
	 */
	public void setAdditionalField22(String theAdditionalField22) {
		// begin-user-code
		additionalField22 = theAdditionalField22;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField23 the additionalField23 to set
	 */
	public void setAdditionalField23(String theAdditionalField23) {
		// begin-user-code
		additionalField23 = theAdditionalField23;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField24 the additionalField24 to set
	 */
	public void setAdditionalField24(String theAdditionalField24) {
		// begin-user-code
		additionalField24 = theAdditionalField24;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField25 the additionalField25 to set
	 */
	public void setAdditionalField25(String theAdditionalField25) {
		// begin-user-code
		additionalField25 = theAdditionalField25;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField3 the additionalField3 to set
	 */
	public void setAdditionalField3(String theAdditionalField3) {
		// begin-user-code
		additionalField3 = theAdditionalField3;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField4 the additionalField4 to set
	 */
	public void setAdditionalField4(String theAdditionalField4) {
		// begin-user-code
		additionalField4 = theAdditionalField4;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField5 the additionalField5 to set
	 */
	public void setAdditionalField5(String theAdditionalField5) {
		// begin-user-code
		additionalField5 = theAdditionalField5;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField6 the additionalField6 to set
	 */
	public void setAdditionalField6(String theAdditionalField6) {
		// begin-user-code
		additionalField6 = theAdditionalField6;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField7 the additionalField7 to set
	 */
	public void setAdditionalField7(String theAdditionalField7) {
		// begin-user-code
		additionalField7 = theAdditionalField7;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField8 the additionalField8 to set
	 */
	public void setAdditionalField8(String theAdditionalField8) {
		// begin-user-code
		additionalField8 = theAdditionalField8;
		// end-user-code
	}

	/** 
	 * @param theAdditionalField9 the additionalField9 to set
	 */
	public void setAdditionalField9(String theAdditionalField9) {
		// begin-user-code
		additionalField9 = theAdditionalField9;
		// end-user-code
	}

	
	/** 
	 * @param theTransactionType the transactionType to set
	 */
	public void setTransactionType(String theTransactionType) {
		// begin-user-code
		transactionType = theTransactionType;
		// end-user-code
	}
	
	
}