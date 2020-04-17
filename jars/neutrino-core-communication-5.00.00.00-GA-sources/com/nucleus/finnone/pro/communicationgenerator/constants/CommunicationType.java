package com.nucleus.finnone.pro.communicationgenerator.constants;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@Entity
@DynamicUpdate
@DynamicInsert
public class CommunicationType extends GenericParameter {

	/**	
	 * 
	 */
	private static final long serialVersionUID = -4400133154601121878L;
	
	public static final String SMS = "S";
	public static final String EMAIL = "E";
	public static final String LETTER = "L";
	public static final String WHATSAPP = "W";

}
