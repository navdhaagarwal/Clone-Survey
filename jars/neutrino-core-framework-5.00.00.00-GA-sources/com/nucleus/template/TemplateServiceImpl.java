/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */
package com.nucleus.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.MessageSource;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.Message.MessageType;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.ServiceInputException;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.neutrinoTemplateLoader.NeutrinoDBTemplateLoader;
import com.nucleus.service.BaseServiceImpl;

import freemarker.cache.StringTemplateLoader;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Named("templateService")
public class TemplateServiceImpl extends BaseServiceImpl implements TemplateService {

    Configuration            configForStoredTemplates = new Configuration();
    Configuration            configForStringTempaltes = new Configuration();

    StringTemplateLoader     stringTemplateLoader     = new StringTemplateLoader();

    private static final String TEMPLATE_PROCESSING_EXCEPTION="TemplateException while processing Template with name : ";
    private static final String TEMPLATE_NOT_LOADED="Exception occured while loading File Template from Configuration for template name :";
    @Inject
    @Named("frameworkMessageSource")
    protected MessageSource  messageSource;

    @Inject
    @Named("neutrinoTemplateLoader")
    NeutrinoDBTemplateLoader neutrinoDBTemplateLoader;

    @Override
    public String getResolvedStringFromTemplate(String templateKey, String templateValue, Map<String, String> map)
            throws IOException {
        return getTemplateString(templateKey, templateValue, map);

    }

    @Override
    public String getResolvedStringFromResourceBundle(String messageResourceKey, Locale locale, Map<String, String> map)
            throws IOException {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        String templateCacheKey = messageResourceKey + locale.toString();
        String stringTemplate = messageSource.getMessage(messageResourceKey, null, locale);
        return getTemplateString(templateCacheKey, stringTemplate, map);
        
        
    }

    private String getTemplateString(String templateCacheKey, String templateName, Object map)
            throws IOException {
        Template template = null;
        try {
            template = getStringTemplateFromConfiguration(templateCacheKey, templateName);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error(
                    "Exception occured while loading String Template from Configuration for template name:"
                            + templateCacheKey + ", eventKey ", e.getMessage());
        }
        String eventStringRepresentation = null;
        StringWriter sw = new StringWriter();
        try {
            template.process(map, sw);
            eventStringRepresentation=sw.toString();
        } catch (TemplateException e) {
        	String message = "TemplateException while processing template -" + e.getMessage();
            BaseLoggers.exceptionLogger.error(message,e);
            eventStringRepresentation=message;
        }    
        sw = null;
        return eventStringRepresentation;

    }

    @Override
    public String getResolvedStringFromFTL(String templateName, Map<String, Object> map) throws IOException {
        NeutrinoValidator.notNull(templateName, "Template name cannot be null");
        Template template = null;
        try {
            template = getStoredTemplatesFromConfiguration(templateName);
        } catch (Exception e) {
        	   throw ExceptionBuilder.getInstance(SystemException.class,TEMPLATE_NOT_LOADED+templateName,TEMPLATE_NOT_LOADED+templateName).setOriginalException(e)
               .setMessage(TEMPLATE_NOT_LOADED+templateName).build();
           }
        String eventStringRepresentation = null;
        try {
            eventStringRepresentation = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        } catch (TemplateException e) {
            throw ExceptionBuilder.getInstance(SystemException.class,TEMPLATE_PROCESSING_EXCEPTION+templateName,TEMPLATE_PROCESSING_EXCEPTION+templateName).setOriginalException(e)
            .setMessage(TEMPLATE_PROCESSING_EXCEPTION+templateName).build();
        }

        return eventStringRepresentation;
    }

    private synchronized Template getStringTemplateFromConfiguration(String templateCacheKey, String templateName)
            throws IOException {
        Template template = null;
        try {configForStringTempaltes.setDefaultEncoding("UTF-8");
            template = configForStringTempaltes.getTemplate(templateCacheKey);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.error("IOException while getting Template from template cache for templateCache:"
                    + templateCacheKey + " : " + e.getMessage());
            String stringTemplate = null;
            stringTemplate = templateName;
            stringTemplateLoader.putTemplate(templateCacheKey, stringTemplate);
            configForStringTempaltes.setTemplateLoader(stringTemplateLoader);
            template = configForStringTempaltes.getTemplate(templateCacheKey);
        }
        return template;
    }

    private synchronized Template getStoredTemplatesFromConfiguration(String templateName) throws IOException {
        Template template = null;
        try {configForStoredTemplates.setDefaultEncoding("UTF-8");
            template = configForStoredTemplates.getTemplate(templateName);
        } catch (ParseException e) {
            BaseLoggers.exceptionLogger
                    .error("Exception occured while processing Template from template cache for templateCacheKey:"
                            + templateName + " " + e.getMessage());
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.error("IOException while getting Template from template cache for templateCacheKey:"
                    + templateName + " : " + e.getMessage());
            /*String fileTemplateLoaderPath = messageSource.getMessage("FILE_TEMPLATE_LOADER_PATH", null,
                    Locale.getDefault());
            FileTemplateLoader ftl = new FileTemplateLoader(new File("D:\\Templates"));*/
            configForStoredTemplates.setTemplateLoader(neutrinoDBTemplateLoader);
            template = configForStoredTemplates.getTemplate(templateName);
        } catch (Exception e) {
            BaseLoggers.exceptionLogger
                    .error("Unknown exception occured while getting Template from template cache for templateCacheKey:"
                            + templateName + " : " + e.getMessage());
        }
        return template;
    }

	@Override
	public String getStringFromTemplateString(String templateKey, String templateValue, Map<String, Object> map) throws IOException {
		
		return getTemplateString(templateKey, templateValue, map);
	}
}
