package com.nucleus.regional;

import javax.persistence.CascadeType;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import java.io.Serializable;
import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Store;
import org.joda.time.DateTime;
import javax.persistence.Embedded;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;

import com.nucleus.core.bridge.DateTimeOneWayBridge;
import com.nucleus.core.hibernate.search.MoneyToStringOneWayBridge;
import com.nucleus.core.money.entity.Money;

@Embeddable
public class RegionalData implements Serializable{
	
	@Transient
	private static final long serialVersionUID = 6695115179730767897L;

	}