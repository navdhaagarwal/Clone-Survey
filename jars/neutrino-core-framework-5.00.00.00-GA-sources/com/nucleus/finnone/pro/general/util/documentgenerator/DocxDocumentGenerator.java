/**
 * 
 */
package com.nucleus.finnone.pro.general.util.documentgenerator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.docx4j.convert.out.pdf.PdfConversion;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.springframework.beans.factory.annotation.Value;

import com.nucleus.core.barcode.generator.service.BarcodeGeneratorService;
import com.nucleus.core.multilanguageletter.OpenOfficeInputStream;
import com.nucleus.core.multilanguageletter.OpenOfficeOutputStream;
import com.nucleus.core.multilanguageletter.OpenOfficeStreamConverterUtility;
import com.nucleus.finnone.pro.base.Message;
import com.nucleus.pdf.IDocx2PdfConvertor;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.ByteArrayImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * @author shivani.aggarwal
 *
 */

public class DocxDocumentGenerator implements IDocumentGenerator {

	public static final String BARCODE_IMAGE_PARAMETER_CODE= "barcodeImage";
	
	private String templatePath;
	private Map<String,String> imageVariablesWithPathMap;
	private String lookUpAtClassPathOrFilePath;
	
	private String openOfficeFilterName="writer_pdf_Export";
	
	@Value("${is.multiplelanguage.support.enabled.for.letters}")
    private String                                 isMultipleLanguageSupportEnabledForLetters;
	
	@Value("${xdoc.image.provider.use.default.size.barcode.image}")
	private boolean  useBarcodeImageDefaultSize;
	
	@Inject
	@Named("openOfficeConverterUtility")
	private OpenOfficeStreamConverterUtility openOfficeStreamConverterUtility;
	
	@Inject
	@Named("barcodeGeneratorService")
	private BarcodeGeneratorService barcodeGeneratorService;

	@Inject
	@Named("docx2PdfConvertor")
	private IDocx2PdfConvertor docx2PdfConvertor;

	@Value("${convert.pdf.using.OpenPDF:true}")
	private boolean  convertPDFUsingOpenPDF = true;
	
	public String getLookUpAtClassPathOrFilePath() {
		return lookUpAtClassPathOrFilePath;
	}


	public void setLookUpAtClassPathOrFilePath(String lookUpAtClassPathOrFilePath) {
		this.lookUpAtClassPathOrFilePath = lookUpAtClassPathOrFilePath;
	}

	@Override
	public String getTemplatePath() {
		return templatePath;
	}

	@Override
	public Map<String,String> getImageVariablesWithPathMap() {
		return Collections.unmodifiableMap(imageVariablesWithPathMap);
	}
	
	
	@Override
	public byte[] getPDFOutput(Map<String, Object> data) {
		return getPDFOutput(data,imageVariablesWithPathMap);
	}

	@Override
	public byte[] getPDFOutput(Map<String, Object> data,Map<String, String> imageVariablesWithPath) {
		return getPDFOutput(data,imageVariablesWithPath,null,null);
	}

	/**
	 * This method converts the docx file to PDF
	 */
	@Override
	public byte[] getPDFOutput(Map<String, Object> data,Map<String, String> imageVariablesWithPath,List<TableDataVO> tableDataVOList,List<String> variableList) 
	{
		byte[] docxBytes = getDocxOutput(data, imageVariablesWithPath,tableDataVOList,variableList);
		if( convertPDFUsingOpenPDF){
			try {
				return docx2PdfConvertor.convert(docxBytes);
			}catch (Exception e) {
				DocumentGeneratorException de = new DocumentGeneratorException("cannot convert to pdf using openPdf ",e, DocumentGeneratorMessageConstants.CANNOT_CONVERT_TO_PDF);
				List<Message> list = de.getMessages();
				list.add(new Message(DocumentGeneratorMessageConstants.CANNOT_CONVERT_TO_PDF, Message.MessageType.ERROR));
				de.setMessages(list);
				throw de;
			}
		}
		if("Y".equalsIgnoreCase(isMultipleLanguageSupportEnabledForLetters)){
			return getPDFOutputForMultiLingualDocuments(docxBytes);
		}else{
			return getPDFOutput(docxBytes);
		}
	}


	private byte[] getPDFOutputForMultiLingualDocuments(byte[] docxBytes) {
		byte[] outputBytes=null;
		try {
			outputBytes= openOfficeStreamConverterUtility.convert(new OpenOfficeInputStream(docxBytes), new OpenOfficeOutputStream(), openOfficeFilterName);
		} catch (Exception e) {
			DocumentGeneratorException de = new DocumentGeneratorException("cannot convert to pdf",e, DocumentGeneratorMessageConstants.CANNOT_CONVERT_TO_PDF);
			List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.CANNOT_CONVERT_TO_PDF, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de; 
		}
		return outputBytes;
	}


	private byte[] getPDFOutput(byte[] docxBytes) {
		ByteArrayOutputStream pdfByteOutputStream = new ByteArrayOutputStream();			
        WordprocessingMLPackage wordMLPackage=null;
       		try {
				wordMLPackage = WordprocessingMLPackage.load(new ByteArrayInputStream(docxBytes));
			} catch (Docx4JException e) {
				DocumentGeneratorException de = new DocumentGeneratorException("unable to load the output stream",e, DocumentGeneratorMessageConstants.OUTPUTSTREAM_NOT_LOADED);
				List<Message> list = de.getMessages();
	    		list.add(new Message(DocumentGeneratorMessageConstants.OUTPUTSTREAM_NOT_LOADED, Message.MessageType.ERROR));
	    		de.setMessages(list);
	    		throw de;
			}
    		PdfSettings pdfSettings = new PdfSettings();
    		PdfConversion converter = new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordMLPackage);
   			try {
				converter.output(pdfByteOutputStream, pdfSettings);
			} catch (Docx4JException e) {
				DocumentGeneratorException de = new DocumentGeneratorException("cannot convert to pdf",e, DocumentGeneratorMessageConstants.CANNOT_CONVERT_TO_PDF);
				List<Message> list = de.getMessages();
	    		list.add(new Message(DocumentGeneratorMessageConstants.CANNOT_CONVERT_TO_PDF, Message.MessageType.ERROR));
	    		de.setMessages(list);
	    		throw de;
			}
	   return pdfByteOutputStream.toByteArray();
		
	}


	@Override
	public byte[] getDocxOutput(Map<String, Object> data) {
		return getDocxOutput(data,imageVariablesWithPathMap);
	}


	@Override
	public byte[] getDocxOutput(Map<String, Object> data,Map<String, String> imageVariablesWithPath) {
		return getDocxOutput(data,imageVariablesWithPath,null,null);
	}

	/**
	 * This method converts the data variables in the docx file with their actual value
	 * and returns an array of bytes
	 */
	
	@Override
	public byte[] getDocxOutput(Map<String, Object> data,Map<String, String> imageVariablesWithPath,List<TableDataVO> tableDataVOList,List<String> variableList) 
	{
		IXDocReport report = validateTemplatePathAndAddDefaultImage();
		IContext context=null;
		FieldsMetadata metadata = new FieldsMetadata();
		if(data==null){
			 DocumentGeneratorException de = new DocumentGeneratorException("data not provided", DocumentGeneratorMessageConstants.DATA_NOT_PROVIDED);
			 List<Message> list = de.getMessages();
	    		list.add(new Message(DocumentGeneratorMessageConstants.DATA_NOT_PROVIDED, Message.MessageType.ERROR));
	    		de.setMessages(list);
	    		throw de;
		}
		try {
			context = report.createContext();
			metadata = addImageMetaData(report, imageVariablesWithPathMap,metadata);
		} catch (XDocReportException e1) {
			DocumentGeneratorException de = new DocumentGeneratorException("context cannot be created",e1, DocumentGeneratorMessageConstants.CONTEXT_NOT_CREATED);
			 List<Message> list = de.getMessages();
	    		list.add(new Message(DocumentGeneratorMessageConstants.CONTEXT_NOT_CREATED, Message.MessageType.ERROR));
	    		de.setMessages(list);
	    		throw de;
		}
		
		if(imageVariablesWithPathMap!=null&&imageVariablesWithPathMap.size()>0){
			for(Map.Entry<String, String> dataEntry:imageVariablesWithPathMap.entrySet()){         
		        context.put(dataEntry.getKey(),getImageProvider(dataEntry.getValue()));		 	   	
			}
		}
		//context = updateDataImageAndTableInContext(data,imageVariablesWithPath,tableDataVOList,variableList,context,report);
		for(Map.Entry<String, Object> dataEntry:data.entrySet()){
			context.put(dataEntry.getKey(),dataEntry.getValue());
		}
		
		if(imageVariablesWithPath!=null&&imageVariablesWithPath.size()>0){
			
			metadata = addImageMetaData(report,imageVariablesWithPath,metadata);
			for(Map.Entry<String, String> dataEntry:imageVariablesWithPath.entrySet()){  
		        if(BARCODE_IMAGE_PARAMETER_CODE.equals(dataEntry.getKey())) {
		    		context.put(BARCODE_IMAGE_PARAMETER_CODE,getByteArrayImageProvider(barcodeGeneratorService.createBarcodeImage(imageVariablesWithPath.get(BARCODE_IMAGE_PARAMETER_CODE))));
		        }else {
			        context.put(dataEntry.getKey(),getImageProvider(dataEntry.getValue()));	
		        }
			}
		}
		
		if(tableDataVOList!=null && variableList!=null)
		{
			addTableMetaData(report,variableList,metadata);
			for(TableDataVO tableDataVO: tableDataVOList)
			{
				context.put(tableDataVO.getTableKey(),tableDataVO.getTableData());
			}
		}		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	   try {
			report.process(context, outputStream);
		} catch (XDocReportException e) {
			DocumentGeneratorException de = new DocumentGeneratorException("context cannot be processed",e, DocumentGeneratorMessageConstants.CONTEXT_NOT_PROCESSED);
			 List<Message> list = de.getMessages();
	    		list.add(new Message(DocumentGeneratorMessageConstants.CONTEXT_NOT_PROCESSED, Message.MessageType.ERROR));
	    		de.setMessages(list);
	    		throw de;
		} catch (IOException e) {
			DocumentGeneratorException de = new DocumentGeneratorException("context cannot be processed",e, DocumentGeneratorMessageConstants.CONTEXT_NOT_PROCESSED);
			List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.CONTEXT_NOT_PROCESSED, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de;
		}
		return outputStream.toByteArray();
	}

	private IImageProvider getByteArrayImageProvider(byte[] barcodeImage) {
		IImageProvider imageProvider = new ByteArrayImageProvider(barcodeImage);
		if(useBarcodeImageDefaultSize) {
			imageProvider.setUseImageSize(useBarcodeImageDefaultSize);
		}
		
		return imageProvider;
	}

	protected IContext updateDataImageAndTableInContext(Map<String, Object> data,Map<String, String> imageVariablesWithPath,
			List<TableDataVO> tableDataVOList, List<String> variableList,IContext context,IXDocReport report) {
		
		
		return context;
	}


	protected FieldsMetadata addImageMetaData (IXDocReport report, Map<String, String> imageVariablesWithPath,FieldsMetadata metadata){
		if(imageVariablesWithPath!=null&&imageVariablesWithPath.size()>0){
			for(Map.Entry<String, String> dataEntry:imageVariablesWithPath.entrySet()){
						metadata.addFieldAsImage(dataEntry.getKey());
			}
			report.setFieldsMetadata(metadata);
		}
		return metadata;
	}
	protected IImageProvider getImageProvider(String pathToImage){
		IImageProvider image=null;
		if("filepath".equalsIgnoreCase(lookUpAtClassPathOrFilePath))
		{
	    	image = new fr.opensagres.xdocreport.document.images.FileImageProvider(new File(pathToImage),true);
		}else{
			//TODO
			DocumentGeneratorException de = new DocumentGeneratorException("classpath template location not supported", DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_SUPPORTED);
			List<Message> list = de.getMessages();
    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_SUPPORTED, Message.MessageType.ERROR));
    		de.setMessages(list);
    		throw de;
		}	
		return image;
	}
	
	protected void addTableMetaData(IXDocReport report, List<String> variableList,FieldsMetadata metadata){
			for(String list: variableList)
			{
				metadata.addFieldAsList(list);
			}
				report.setFieldsMetadata(metadata);
	}
	
	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}
	
	public void setImageVariablesWithPathMap(
			Map<String, String> imageVariablesWithPathMap) {
		this.imageVariablesWithPathMap = imageVariablesWithPathMap;
	}

	protected IXDocReport validateTemplatePathAndAddDefaultImage()
	{
		IXDocReport report=null;
		try
	    {
	    	if(templatePath.equals("")){
	    		DocumentGeneratorException de = new DocumentGeneratorException("template path not found", DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_PROVIDED);
	    		List<Message> list = de.getMessages();
	    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_PROVIDED, Message.MessageType.ERROR));
	    		de.setMessages(list);
	    		throw de;
	    	}
	    	InputStream templateStream = null;
	    	if("filepath".equalsIgnoreCase(lookUpAtClassPathOrFilePath))
	    	{
	    		URL url =new File(templatePath).toURL(); 
	    		try {
					templateStream= url.openStream();
				} catch (IOException e) {
					DocumentGeneratorException de = new DocumentGeneratorException("template path not valid",e, DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_VALID);
					List<Message> list = de.getMessages();
		    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_VALID, Message.MessageType.ERROR));
		    		de.setMessages(list);
		    		throw de;
				}
	    	}else{
	    		//TODO to be provided
	    		DocumentGeneratorException de = new DocumentGeneratorException("classpath template location not supported", DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_SUPPORTED);
	    		List<Message> list = de.getMessages();
	    		list.add(new Message(DocumentGeneratorMessageConstants.TEMPLATE_PATH_NOT_SUPPORTED, Message.MessageType.ERROR));
	    		de.setMessages(list);
	    		throw de;
	    	}
	     report = NeutrinoXDocReportRegistry.getRegistry().loadReport(templateStream, TemplateEngineKind.Freemarker,false);
	    
	    } catch (MalformedURLException e) {
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
		return report;
	}
	
}
