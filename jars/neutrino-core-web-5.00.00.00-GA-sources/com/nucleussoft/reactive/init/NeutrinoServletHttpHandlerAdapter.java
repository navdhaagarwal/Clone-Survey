package com.nucleussoft.reactive.init;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * 
 * ServletHttpHandlerAdapter does not have any default constructor and some
 * servers create Servlet object with default constructor ex weblogic, so adding
 * default constructor.
 * Reported to spring with issue id SPR-17249
 * @author gajendra.jatav
 *
 */
public class NeutrinoServletHttpHandlerAdapter extends ServletHttpHandlerAdapter {

	public NeutrinoServletHttpHandlerAdapter() {
		super(new DeligatingHttpHandler());
	}

	private static HttpHandler gerHttpAdapter() {
		return WebHttpHandlerBuilder.applicationContext(ReactiveContextUtil.getReactiveAppContext()).build();
	}

}
