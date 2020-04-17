package com.nucleus.web.security;

import javax.inject.Named;

import org.owasp.html.HtmlChangeListener;

@Named("neutrinoHtmlChangeListener")
public class NeutrinoHtmlChangeListener implements HtmlChangeListener{

	@Override
	public void discardedTag(Object context, String elementName) {
		String []ctx=(String [])context;
		String parameterName=ctx[0];
		String parameterValue=ctx[1];
		NeutrinoSecurityUtility.throwXssException(parameterName, parameterValue);
		
	}

	@Override
	public void discardedAttributes(Object context, String tagName,
			String... attributeNames) {
		String []ctx=(String [])context;
		String parameterName=ctx[0];
		String parameterValue=ctx[1];
		NeutrinoSecurityUtility.throwXssException(parameterName, parameterValue);
	}

}
