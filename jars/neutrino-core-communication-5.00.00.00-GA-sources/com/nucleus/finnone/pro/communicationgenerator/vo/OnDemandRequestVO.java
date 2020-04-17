package com.nucleus.finnone.pro.communicationgenerator.vo;

public class OnDemandRequestVO extends RequestVO {
	
	//if this is false then only letter will be generated at following location.
	private boolean returnGeneratedLetterContentOnly = true;

	public boolean isReturnGeneratedLetterContentOnly() {
		return returnGeneratedLetterContentOnly;
	}

	/**
	 * Set this to false for generating letter even in case of on Demand.
	 * @param returnGeneratedLetterContentOnly
	 */
	public void setReturnGeneratedLetterContentOnly(boolean returnGeneratedLetterContentOnly) {
		this.returnGeneratedLetterContentOnly = returnGeneratedLetterContentOnly;
	}
	
}
