package com.nucleus.web.security.servlet.api;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.nucleus.web.security.NeutrinoRequestParamHolder;



/**
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoRequestDispatcherWrapper implements RequestDispatcher{

	private RequestDispatcher requestDispatcher;
	
	private NeutrinoRequestParamHolder paramHolder;

	private String path;

	public NeutrinoRequestDispatcherWrapper(RequestDispatcher requestDispatcher,NeutrinoRequestParamHolder paramHolder, String path){
		this.requestDispatcher=requestDispatcher;
		this.paramHolder=paramHolder;
		this.path = path;
	}

	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		this.paramHolder.preForward(path);
		this.requestDispatcher.forward(request, response);
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		this.paramHolder.preForward(path);
		this.requestDispatcher.include(request, response);
	}

}
