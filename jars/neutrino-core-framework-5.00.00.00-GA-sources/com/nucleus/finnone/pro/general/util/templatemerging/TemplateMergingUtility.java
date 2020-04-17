/*
 * Author: Merajul Hasan Ansari
 * Creation Date: 13-May-2013
 * Copyright: Nucleus Software Exports Ltd.
 * Description: This utility class for merging actual values in template
 *
 * ------------------------------------------------------------------------------------------------------------------------------------
 * Revision:  Version         Last Revision Date                   Name                Function / Module affected  Modifications Done
 * ------------------------------------------------------------------------------------------------------------------------------------
 *                1.0             13/05/2013                    Merajul Hasan Ansari             Initial Version created 
 
 */
package com.nucleus.finnone.pro.general.util.templatemerging;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.velocity.app.VelocityEngine;

import com.nucleus.velocity.util.VelocityEngineUtils;

import fr.opensagres.xdocreport.core.io.internal.ByteArrayOutputStream;

@Named("templateMergingUtility")
public class TemplateMergingUtility {
	/**
	 * Default character set as UTF-8
	 */
	private static final String DEFAULT_CHARSET=Charset.forName( "UTF-8" ).name();
	@Inject 
	private VelocityEngine velocityEngine;
	/**
	 * Returns merged template content as string
	 * @param templateLocation
	 * @param model
	 * @return
	 */
	public String mergeTemplateIntoString(String templateLocation, Map<String, Object> model){
		String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateLocation, DEFAULT_CHARSET, model);
        return text;
	}
	/**
	 * Returns merged template content as byte[]
	 * @param templateLocation
	 * @param model
	 * @return
	 */
	public byte[] mergeTemplateIntoByteArray(String templateLocation, Map<String, Object> model){
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream));
		
		VelocityEngineUtils.mergeTemplate(velocityEngine, templateLocation, DEFAULT_CHARSET, model, writer);
		return outputStream.toByteArray();
	}
}
