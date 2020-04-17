package com.nucleus.security.masking.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;
import com.nucleus.master.BaseMasterEntity;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant = "SELECT")
public class MaskingDefinition extends BaseMasterEntity {

	private static final long serialVersionUID = 1L;

	/** input       			expression		IsMasked		isSpacesToBeCounted		isIndexBased		maskingCharacter		output	   
	 * 1234 5678 9012 3456 		01:02:01 			true				false			false				X						X234 567X X012 345X  
	 * 1234 5678 9012 3456 		5-8 				true				false			true				X						1234 XXXX 9012 3456
	 * 1234 5678 9012 3456		01:01:01 			false				false			false				X						1XXX XXX8 9XXX XXX6
	 * 1234 5678 9012 3456		00:00:04			false				false			false				X						XXXX XXXX XXXX 3456
	 * 1234567890				01:01:01			false				false			false				X						1XXXX5XXXX0
	 * Ojas Srivastava			04:00:00			true				false			false				X						XXXX Srivastava
	 * Ojas						04:00:00			true				false			false				X						XXXX
	 * Ram						04:00:00			true				false			false				X						XXX
	 * Ram Kumar Srivastava		04:00:00			true				true			false				*						****Kumar Srivastava
	 * Ram Kumar				04:00:00			false				true			false				X						Ram XXXXX
	 * Ram						04:00:00			false				false			false				X						Ram
	 * Ram						5-8					false				false			true				X						Ram
	 *  
	 * 
	 * 
	 * **/

	private String name;

	private String expression;

	private boolean spacesToBeCounted;

	private boolean masked;

	private String maskingCharacter;

	@ManyToOne(fetch = FetchType.LAZY)
	private MaskingType type;
	
	private boolean enabled;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public boolean isSpacesToBeCounted() {
		return spacesToBeCounted;
	}

	public void setSpacesToBeCounted(boolean spacesToBeCounted) {
		this.spacesToBeCounted = spacesToBeCounted;
	}

	public boolean isMasked() {
		return masked;
	}

	public void setMasked(boolean masked) {
		this.masked = masked;
	}


	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getMaskingCharacter() {
		return maskingCharacter;
	}

	public void setMaskingCharacter(String maskingCharacter) {
		this.maskingCharacter = maskingCharacter;
	}

	public MaskingType getType() {
		return type;
	}

	public void setType(MaskingType type) {
		this.type = type;
	}

	@Override
	protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {

		MaskingDefinition maskingDefinition = (MaskingDefinition) baseEntity;
		super.populate(maskingDefinition, cloneOptions);

		maskingDefinition.setName(name);
		maskingDefinition.setExpression(expression);
		maskingDefinition.setSpacesToBeCounted(spacesToBeCounted);
		maskingDefinition.setMasked(masked);
		maskingDefinition.setEnabled(enabled);
		maskingDefinition.setMaskingCharacter(maskingCharacter);
		maskingDefinition.setType(type);
	}

	@Override
	protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {
		MaskingDefinition maskingDefinition = (MaskingDefinition) baseEntity;
		super.populateFrom(maskingDefinition, cloneOptions);

		this.setName(maskingDefinition.getName());
		this.setExpression(maskingDefinition.getExpression());
		this.setSpacesToBeCounted(maskingDefinition.isSpacesToBeCounted());
		this.setMasked(maskingDefinition.isMasked());
		this.setEnabled(maskingDefinition.isEnabled());
		this.setMaskingCharacter(maskingDefinition.getMaskingCharacter());
		this.setType(maskingDefinition.getType());
	}


	
}
