package com.nucleus.velocity.util;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

public interface VelocityEngineUtils {

	public static void mergeTemplate(VelocityEngine velocityEngine, String templateLocation, String encoding,
			Map<String, Object> model, Writer writer) {
		VelocityContext velocityContext = new VelocityContext(model);
		velocityEngine.mergeTemplate(templateLocation, encoding, velocityContext, writer);
	}

	public static String mergeTemplateIntoString(VelocityEngine velocityEngine, String templateLocation,
			String encoding, Map<String, Object> model) {
		StringWriter result = new StringWriter();
		mergeTemplate(velocityEngine, templateLocation, encoding, model, result);
		return result.toString();
	}
}
