package com.nucleus.finnone.pro.general.util.documentgenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Named;

import org.apache.commons.io.IOUtils;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.logging.BaseLoggers;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.TemplateEngineKind;

/**
 * 
 * @author gajendra.jatav
 *
 */


@Named("xDoxReportManager")
public class XDoxReportManager implements IXDoxReportManager{

	private ConcurrentHashMap<String,ReportTemplateInfo> reportInfoCache =new ConcurrentHashMap<String, ReportTemplateInfo>();
	
	public static String FILEPATH="filepath";
	
	
	@Override
	public IXDocReport getReport(String path,String lookUpAtClassPathOrFilePath) {
		
		String reportId=getIdFromPath(path);
		if (XDocReportRegistry.getRegistry().existsReport(reportId)
				&& reportInfoCache.containsKey(reportId))		{
			return getFromCache(path,reportId,lookUpAtClassPathOrFilePath);
		}
		else
		{
			synchronized(reportInfoCache){
				return createAndCacheReport(path,reportId,lookUpAtClassPathOrFilePath);
			}
		}
	}

	
	private String getIdFromPath(String path) {
		
		return Integer.toString(path.hashCode());
	}


	private IXDocReport getFromCache(String path,String reportId,String lookUpAtClassPathOrFilePath) {
		
		
		ReportTemplateInfo reportTemplateInfo=reportInfoCache.get(reportId);
		if(reportTemplateInfo!=null && reportTemplateInfo.isNotTemplateModified())
		{
			return XDocReportRegistry.getRegistry().getReport(reportId);
		}
		else
		{
			synchronized(reportInfoCache){
				return createAndCacheReport(path,reportId,lookUpAtClassPathOrFilePath);
			}
		}
	}

	private synchronized IXDocReport createAndCacheReport(String path, String reportId,
			String lookUpAtClassPathOrFilePath) {

		
		if(!FILEPATH.equalsIgnoreCase(lookUpAtClassPathOrFilePath))
    	{
    		DocumentGeneratorException de = new DocumentGeneratorException("classpath template location not supported", DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_SUPPORTED);
    		List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_SUPPORTED, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de;
    	}
		
		
		ReportTemplateInfo existingReportTemplateInfo=reportInfoCache.get(reportId);
		if(existingReportTemplateInfo!=null && existingReportTemplateInfo.isNotTemplateModified())
		{
			return  XDocReportRegistry.getRegistry().getReport(reportId);
		}
		XDocReportRegistry.getRegistry().unregisterReport(reportId);
		IXDocReport report=null;
		try
	    {
			InputStream templateStream = null;
			File file=new File(path);
			URL url =file.toURI().toURL(); 
			templateStream=getTemplateStream(url);
			report = XDocReportRegistry.getRegistry().loadReport(templateStream, reportId,
					TemplateEngineKind.Freemarker, true);
			ReportTemplateInfo reportTemplateInfo=new ReportTemplateInfo();
			reportTemplateInfo.setFile(file);
			reportTemplateInfo.setLastModifiedTimeStamp(file.lastModified());
			processAndCacheTemplate(report,path);
			reportInfoCache.put(reportId, reportTemplateInfo);

			return report;
	    }catch (MalformedURLException e) {
	    	DocumentGeneratorException de = new DocumentGeneratorException("template path not valid",e, DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_VALID);
	    	List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_VALID, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de;
	    } catch (IOException e) {
	    	DocumentGeneratorException de = new DocumentGeneratorException("unable to load template stream",e, DocumentGeneratorMessageConstants.TEMPLATE_STREAM_NOT_LOADED);
	    	List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_STREAM_NOT_LOADED, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de;
	    } catch (XDocReportException e) {
		    DocumentGeneratorException de = new DocumentGeneratorException("template engine cannot be loaded",e, DocumentGeneratorMessageConstants.TEMPLATE_ENGINE_NOT_LOADED);
		    List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_ENGINE_NOT_LOADED, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de;
	    } 
		
		
	}

	private void processAndCacheTemplate(IXDocReport report, String path) {
		
		   try (ByteArrayOutputStream outputStream=new ByteArrayOutputStream()){
			   	
				report.process(report.createContext(), outputStream);
			}catch (Exception e) {
				
				BaseLoggers.exceptionLogger
				.error("Not able to process template with report at path "
						+ path, e);

			}
	
	}

	private InputStream getTemplateStream(URL url) {
		
		try(InputStream stream =  url.openStream()){
			byte[] bytes = IOUtils.toByteArray(stream);
			return new ByteArrayInputStream(bytes);
		} catch (IOException e) {
			DocumentGeneratorException de = new DocumentGeneratorException("template path not valid",e, DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_VALID);
			List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_VALID, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de;
		}

		
	}

}
