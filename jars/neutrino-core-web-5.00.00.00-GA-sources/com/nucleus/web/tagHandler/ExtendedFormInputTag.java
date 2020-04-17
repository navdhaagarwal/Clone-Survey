package com.nucleus.web.tagHandler;

import javax.servlet.jsp.JspException;

import org.springframework.web.servlet.tags.form.InputTag;
import org.springframework.web.servlet.tags.form.TagWriter;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.security.masking.MaskingUtility;

public class ExtendedFormInputTag extends InputTag{


	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String maskingPolicyCode;
	

	@Override
	protected void writeValue(TagWriter tagWriter) throws JspException {
		String value = getDisplayString(getBoundValue(), getPropertyEditor());
		String type = hasDynamicTypeAttribute() ? (String) getDynamicAttributes().get("type") : getType();
		String finalvalue = processFieldValue(getName(), value, type);
		MaskingUtility  maskingUtility=NeutrinoSpringAppContextUtil.getBeanByName("maskingUtility", MaskingUtility.class);
		if ((finalvalue != null && !finalvalue.isEmpty()) && maskingPolicyCode != null
				&& !maskingPolicyCode.isEmpty()) {
			finalvalue = maskingUtility.getMaskedValue(maskingPolicyCode, finalvalue);
		}
		
		
			tagWriter.writeAttribute("value", finalvalue);
		
	}
	private boolean hasDynamicTypeAttribute() {
		return getDynamicAttributes() != null && getDynamicAttributes().containsKey("type");
	}
	public String getMaskingPolicyCode() {
		return maskingPolicyCode;
	}
	public void setMaskingPolicyCode(String maskingPolicyCode) {
		this.maskingPolicyCode = maskingPolicyCode;
	}
	
	
	
}
