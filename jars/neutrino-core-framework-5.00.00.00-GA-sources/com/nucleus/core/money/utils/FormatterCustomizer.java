package com.nucleus.core.money.utils;

import com.ibm.icu.text.NumberFormat;

@FunctionalInterface
public interface FormatterCustomizer {
	
	public void customize(NumberFormat format);

}
