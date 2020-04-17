package com.nucleus.entity;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.search.annotations.DocumentId;

import com.nucleus.core.annotations.Synonym;
import io.swagger.annotations.ApiModelProperty;

@Entity
@Cacheable
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Synonym(grant="ALL")
public class BaseTenant implements Serializable {

	private static final long serialVersionUID = 6702498681862129717L;

	@Id
	@GenericGenerator(name = "sequencePerEntityGenerator", strategy = "com.nucleus.core.generator.NeutrinoSequenceGenerator", parameters = {
			@Parameter(name = "prefer_sequence_per_entity", value = "true"),
			@Parameter(name = "sequence_per_entity_suffix", value = "_seq"),
			@Parameter(name = "initial_value", value = "250000") })
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "sequencePerEntityGenerator")
	@DocumentId
	private Long id;

	private String name;

	private String shortName;

	private String code;

	private String locale;
	
	@Transient
	@ApiModelProperty(hidden=true)
	private Character digitGroupingSymbol;
	
	@Transient
	@ApiModelProperty(hidden=true)
	private Character decimalSeparatorSymbol;

	private transient Locale localeObject;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
		this.decimalSeparatorSymbol=null;
		this.digitGroupingSymbol=null;
	}

	public Character getDigitGroupingSymbol() {
		if(digitGroupingSymbol==null) {
			String localeParts[]=this.locale.split("[_-]");
			DecimalFormat decimalFormat=(DecimalFormat) NumberFormat.getInstance(new Locale(localeParts[0],localeParts[1]));
			digitGroupingSymbol=decimalFormat.getDecimalFormatSymbols().getGroupingSeparator();
		}
		
		return digitGroupingSymbol;
	}

	public Locale getLocaleObject() {
		if(this.localeObject!=null) {
			return this.localeObject;
		}
		String localeParts[]=this.locale.split("[_-]");
		this.localeObject= new Locale(localeParts[0],localeParts[1]);
		return localeObject;
	}
	
	public void setDigitGroupingSymbol(Character digitGroupingSymbol) {
		this.digitGroupingSymbol = digitGroupingSymbol;
	}

	public Character getDecimalSeparatorSymbol() {
		if(decimalSeparatorSymbol==null) {
			String localeParts[]=this.locale.split("[_-]");
			DecimalFormat decimalFormat=(DecimalFormat) NumberFormat.getInstance(new Locale(localeParts[0],localeParts[1]));
			decimalSeparatorSymbol=decimalFormat.getDecimalFormatSymbols().getDecimalSeparator();
		}
		return decimalSeparatorSymbol;
	}

	public void setDecimalSeparatorSymbol(Character decimalSeparatorSymbol) {
		this.decimalSeparatorSymbol = decimalSeparatorSymbol;
	}

}
