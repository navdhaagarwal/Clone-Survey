package com.nucleus.finnone.pro.lov;

import java.io.Serializable;

public class LOVKeyOptrValueHolder  implements Serializable{

	private static final long serialVersionUID = 2146163992L;
	protected String key;
	protected Integer operator;
	protected Object value;

	public LOVKeyOptrValueHolder(String key, Integer operator, Object value)
	{
	    this.key = key;
	    this.operator = operator;
	    this.value = value;
	}

}
