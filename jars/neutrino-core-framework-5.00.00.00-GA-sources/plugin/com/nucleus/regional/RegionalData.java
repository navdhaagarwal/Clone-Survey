package com.nucleus.regional;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import com.nucleus.core.money.entity.Money;

@Embeddable
public class RegionalData implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 6695115179730767897L;

	@Column(name = "REGION_FIELD_01")
	private String regionalField1;

	@Column(name = "REGION_FIELD_02")
	private String regionalField2;

	@Column(name = "REGION_FIELD_03")
	private String regionalField3;

	@Column(name = "REGION_FIELD_04")
	private String regionalField4;

	@Column(name = "REGION_FIELD_05")
	private String regionalField5;

	@Column(name = "REGION_FIELD_06")
	private String regionalField6;

	@Column(name = "REGION_FIELD_07")
	private String regionalField7;

	@Column(name = "REGION_FIELD_08")
	private String regionalField8;

	@Column(name = "REGION_FIELD_09")
	private String regionalField9;

	@Column(name = "REGION_FIELD_10")
	private String regionalField10;

	@Column(name = "REGION_FIELD_11")
	private Integer regionalField11;

	@Column(name = "REGION_FIELD_12")
	private Integer regionalField12;

	@Column(name = "REGION_FIELD_13")
	private Integer regionalField13;

	@Column(name = "REGION_FIELD_14")
	private Integer regionalField14;

	@Column(name = "REGION_FIELD_15")
	private Integer regionalField15;

	@Column(name = "REGION_FIELD_16", columnDefinition = "Numeric(19,0)")
	private Long regionalField16;

	@Column(name = "REGION_FIELD_17", columnDefinition = "Numeric(19,0)")
	private Long regionalField17;

	@Column(name = "REGION_FIELD_18", columnDefinition = "Numeric(19,0)")
	private Long regionalField18;

	@Column(name = "REGION_FIELD_19", columnDefinition = "Numeric(19,0)")
	private Long regionalField19;

	@Column(name = "REGION_FIELD_20", columnDefinition = "Numeric(19,0)")
	private Long regionalField20;

	@Column(name = "REGION_FIELD_21" )
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime regionalField21;

	@Column(name = "REGION_FIELD_22" )
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime regionalField22;

	@Column(name = "REGION_FIELD_23" )
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime regionalField23;

	@Column(name = "REGION_FIELD_24" )
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime regionalField24;

	@Column(name = "REGION_FIELD_25" )
	@Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime regionalField25;
	
	    @Embedded
		@AttributeOverrides({
			@AttributeOverride(name="baseAmount.baseValue",column=@Column(name="rField26_base_value",precision = 25, scale = 7)),
			@AttributeOverride(name="baseAmount.baseCurrencyCode",column=@Column(name="rField26_base_curr_code")),
			@AttributeOverride(name="nonBaseAmount.nonBaseValue",column=@Column(name="rField26_non_base_value",precision = 25, scale = 7)), 
			@AttributeOverride(name="nonBaseAmount.nonBasecurrencyCode",column=@Column(name="rField26_nonbasecur_code")) }) 
    private Money  regionalField26;
	
	    @Embedded
		@AttributeOverrides({
			@AttributeOverride(name="baseAmount.baseValue",column=@Column(name="rField27_base_value",precision = 25, scale = 7)),
			@AttributeOverride(name="baseAmount.baseCurrencyCode",column=@Column(name="rField27_base_curr_code")),
			@AttributeOverride(name="nonBaseAmount.nonBaseValue",column=@Column(name="rField27_non_base_value",precision = 25, scale = 7)), 
			@AttributeOverride(name="nonBaseAmount.nonBasecurrencyCode",column=@Column(name="rField27_nonbasecur_code")) }) 
    private Money  regionalField27;
	
	
	    @Embedded
		@AttributeOverrides({
			@AttributeOverride(name="baseAmount.baseValue",column=@Column(name="rField28_base_value",precision = 25, scale = 7)),
			@AttributeOverride(name="baseAmount.baseCurrencyCode",column=@Column(name="rField28_base_curr_code")),
			@AttributeOverride(name="nonBaseAmount.nonBaseValue",column=@Column(name="rField28_non_base_value",precision = 25, scale = 7)), 
			@AttributeOverride(name="nonBaseAmount.nonBasecurrencyCode",column=@Column(name="rField28_nonbasecur_code")) }) 
    private Money  regionalField28;
	
	    @Embedded
		@AttributeOverrides({
			@AttributeOverride(name="baseAmount.baseValue",column=@Column(name="rField29_base_value",precision = 25, scale = 7)),
			@AttributeOverride(name="baseAmount.baseCurrencyCode",column=@Column(name="rField29_base_curr_code")),
			@AttributeOverride(name="nonBaseAmount.nonBaseValue",column=@Column(name="rField29_non_base_value",precision = 25, scale = 7)), 
			@AttributeOverride(name="nonBaseAmount.nonBasecurrencyCode",column=@Column(name="rField29_nonbasecur_code")) }) 
    private Money  regionalField29;
	
	    @Embedded
		@AttributeOverrides({
			@AttributeOverride(name="baseAmount.baseValue",column=@Column(name="rField30_base_value",precision = 25, scale = 7)),
			@AttributeOverride(name="baseAmount.baseCurrencyCode",column=@Column(name="rField30_base_curr_code")),
			@AttributeOverride(name="nonBaseAmount.nonBaseValue",column=@Column(name="rField30_non_base_value",precision = 25, scale = 7)), 
			@AttributeOverride(name="nonBaseAmount.nonBasecurrencyCode",column=@Column(name="rField30_nonbasecur_code")) }) 
    private Money  regionalField30;

	public Money getRegionalField26() {
		return regionalField26;
	}

	public void setRegionalField26(Money regionalField26) {
		this.regionalField26 = regionalField26;
	}

	public Money getRegionalField27() {
		return regionalField27;
	}

	public void setRegionalField27(Money regionalField27) {
		this.regionalField27 = regionalField27;
	}

	public Money getRegionalField28() {
		return regionalField28;
	}

	public void setRegionalField28(Money regionalField28) {
		this.regionalField28 = regionalField28;
	}

	public Money getRegionalField29() {
		return regionalField29;
	}

	public void setRegionalField29(Money regionalField29) {
		this.regionalField29 = regionalField29;
	}

	public Money getRegionalField30() {
		return regionalField30;
	}

	public void setRegionalField30(Money regionalField30) {
		this.regionalField30 = regionalField30;
	}

	public RegionalData() {

	}

	public RegionalData(RegionalData regionalData) {
		this.regionalField1 = regionalData.getRegionalField1();
		this.regionalField2 = regionalData.getRegionalField2();
		this.regionalField3 = regionalData.getRegionalField3();
		this.regionalField4 = regionalData.getRegionalField4();
		this.regionalField5 = regionalData.getRegionalField5();
		this.regionalField6 = regionalData.getRegionalField6();
		this.regionalField7 = regionalData.getRegionalField7();
		this.regionalField8 = regionalData.getRegionalField8();
		this.regionalField9 = regionalData.getRegionalField9();
		this.regionalField10 = regionalData.getRegionalField10();
		this.regionalField11 = regionalData.getRegionalField11();
		this.regionalField12 = regionalData.getRegionalField12();
		this.regionalField13 = regionalData.getRegionalField13();
		this.regionalField14 = regionalData.getRegionalField14();
		this.regionalField15 = regionalData.getRegionalField15();
		this.regionalField16 = regionalData.getRegionalField16();
		this.regionalField17 = regionalData.getRegionalField17();
		this.regionalField18 = regionalData.getRegionalField18();
		this.regionalField19 = regionalData.getRegionalField19();
		this.regionalField20 = regionalData.getRegionalField20();
		this.regionalField21 = regionalData.getRegionalField21();
		this.regionalField22 = regionalData.getRegionalField22();
		this.regionalField23 = regionalData.getRegionalField23();
		this.regionalField24 = regionalData.getRegionalField24();
		this.regionalField25 = regionalData.getRegionalField25();
		
		this.regionalField26 = regionalData.getRegionalField26();
		this.regionalField27 = regionalData.getRegionalField27();
		this.regionalField28 = regionalData.getRegionalField28();
		this.regionalField29 = regionalData.getRegionalField29();
		this.regionalField30 = regionalData.getRegionalField30();
	}

	public String getRegionalField1() {
		return regionalField1;
	}

	public void setRegionalField1(String regionalField1) {
		this.regionalField1 = regionalField1;
	}

	public String getRegionalField2() {
		return regionalField2;
	}

	public void setRegionalField2(String regionalField2) {
		this.regionalField2 = regionalField2;
	}

	public String getRegionalField3() {
		return regionalField3;
	}

	public void setRegionalField3(String regionalField3) {
		this.regionalField3 = regionalField3;
	}

	public String getRegionalField4() {
		return regionalField4;
	}

	public void setRegionalField4(String regionalField4) {
		this.regionalField4 = regionalField4;
	}

	public String getRegionalField5() {
		return regionalField5;
	}

	public void setRegionalField5(String regionalField5) {
		this.regionalField5 = regionalField5;
	}

	public String getRegionalField6() {
		return regionalField6;
	}

	public void setRegionalField6(String regionalField6) {
		this.regionalField6 = regionalField6;
	}

	public String getRegionalField7() {
		return regionalField7;
	}

	public void setRegionalField7(String regionalField7) {
		this.regionalField7 = regionalField7;
	}

	public String getRegionalField8() {
		return regionalField8;
	}

	public void setRegionalField8(String regionalField8) {
		this.regionalField8 = regionalField8;
	}

	public String getRegionalField9() {
		return regionalField9;
	}

	public void setRegionalField9(String regionalField9) {
		this.regionalField9 = regionalField9;
	}

	public String getRegionalField10() {
		return regionalField10;
	}

	public void setRegionalField10(String regionalField10) {
		this.regionalField10 = regionalField10;
	}

	public Integer getRegionalField11() {
		return regionalField11;
	}

	public void setRegionalField11(Integer regionalField11) {
		this.regionalField11 = regionalField11;
	}

	public Integer getRegionalField12() {
		return regionalField12;
	}

	public void setRegionalField12(Integer regionalField12) {
		this.regionalField12 = regionalField12;
	}

	public Integer getRegionalField13() {
		return regionalField13;
	}

	public void setRegionalField13(Integer regionalField13) {
		this.regionalField13 = regionalField13;
	}

	public Integer getRegionalField14() {
		return regionalField14;
	}

	public void setRegionalField14(Integer regionalField14) {
		this.regionalField14 = regionalField14;
	}

	public Integer getRegionalField15() {
		return regionalField15;
	}

	public void setRegionalField15(Integer regionalField15) {
		this.regionalField15 = regionalField15;
	}

	public Long getRegionalField16() {
		return regionalField16;
	}

	public void setRegionalField16(Long regionalField16) {
		this.regionalField16 = regionalField16;
	}

	public Long getRegionalField17() {
		return regionalField17;
	}

	public void setRegionalField17(Long regionalField17) {
		this.regionalField17 = regionalField17;
	}

	public Long getRegionalField18() {
		return regionalField18;
	}

	public void setRegionalField18(Long regionalField18) {
		this.regionalField18 = regionalField18;
	}

	public Long getRegionalField19() {
		return regionalField19;
	}

	public void setRegionalField19(Long regionalField19) {
		this.regionalField19 = regionalField19;
	}

	public Long getRegionalField20() {
		return regionalField20;
	}

	public void setRegionalField20(Long regionalField20) {
		this.regionalField20 = regionalField20;
	}

	public DateTime getRegionalField21() {
		return regionalField21;
	}

	public void setRegionalField21(DateTime regionalField21) {
		this.regionalField21 = regionalField21;
	}

	public DateTime getRegionalField22() {
		return regionalField22;
	}

	public void setRegionalField22(DateTime regionalField22) {
		this.regionalField22 = regionalField22;
	}

	public DateTime getRegionalField23() {
		return regionalField23;
	}

	public void setRegionalField23(DateTime regionalField23) {
		this.regionalField23 = regionalField23;
	}

	public DateTime getRegionalField24() {
		return regionalField24;
	}

	public void setRegionalField24(DateTime regionalField24) {
		this.regionalField24 = regionalField24;
	}

	public DateTime getRegionalField25() {
		return regionalField25;
	}

	public void setRegionalField25(DateTime regionalField25) {
		this.regionalField25 = regionalField25;
	}

	
	
	
	
	

	

}