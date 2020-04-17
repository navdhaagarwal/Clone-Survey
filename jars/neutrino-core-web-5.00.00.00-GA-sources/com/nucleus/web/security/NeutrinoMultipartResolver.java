package com.nucleus.web.security;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.util.Assert;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.nucleus.core.NeutrinoSpringAppContextUtil;

public class NeutrinoMultipartResolver extends CommonsMultipartResolver{
	private boolean resolveLazily = false;

	@Inject
	@Named("uploadSizeProvider")
	private UploadSizeProvider uploadSizeProvider;

    public NeutrinoMultipartResolver(){
    	super();
    }

    @Override
    public NeutrinoMultipartHttpServletRequest resolveMultipart(final HttpServletRequest request) {
       Assert.notNull(request, "Request must not be null");
	   if (this.resolveLazily) {
		return new NeutrinoMultipartHttpServletRequest(request) {
			@Override
			protected void initializeMultipart() {
				MultipartParsingResult parsingResult = parseRequest(request);
				setMultipartFiles(parsingResult.getMultipartFiles());
				setMultipartParameters(parsingResult.getMultipartParameters());
				setMultipartParameterContentTypes(parsingResult.getMultipartParameterContentTypes());
				setExludedParameters(XssPatternsAndExcludedURIHolder.getExludedParameters(request)); 
				setParamPatterns(XssPatternsAndExcludedURIHolder.getParamPatterns());
				setHeaderAndParamPatterns(XssPatternsAndExcludedURIHolder.getHeaderAndParamPatterns());
				setEncryptor(NeutrinoSpringAppContextUtil.getBeanByName("stringEncryptor", StandardPBEStringEncryptor.class));
			}
		};
	}
	else {
		MultipartParsingResult parsingResult = parseRequest(request);
		return new NeutrinoMultipartHttpServletRequest( request, parsingResult.getMultipartFiles(),
				parsingResult.getMultipartParameters(), parsingResult.getMultipartParameterContentTypes(),
				XssPatternsAndExcludedURIHolder.getExludedParameters(request),	XssPatternsAndExcludedURIHolder.getParamPatterns(),
				XssPatternsAndExcludedURIHolder.getHeaderAndParamPatterns(), 
				NeutrinoSpringAppContextUtil.getBeanByName("stringEncryptor", StandardPBEStringEncryptor.class) );
	}
  }

	@Override
	protected FileUpload newFileUpload(FileItemFactory fileItemFactory) {
		return new ServletFileUpload(fileItemFactory){
			public long getSizeMaxForThisRequest(RequestContext ctx) {
				return uploadSizeProvider.getMaxAllowedSize(((ServletRequestContext)ctx).getRequest());
			}
		};
	}


}