package com.nucleussoft.reactive.init;

import javax.servlet.ServletContext;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.web.context.ConfigurableWebApplicationContext;

/**
 * This ApplicationContextInitializer will be invoked on creation of DispatcherServlet context creation.
 * 
 * @author gajendra.jatav
 *
 */
public class ReactiveApplicationContextInitializer
		implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {

	@Override
	public void initialize(ConfigurableWebApplicationContext applicationContext) {
		ServletContext servletContext = applicationContext.getServletContext();
		ReactiveWebInitializer reactiveWebInitializer = (ReactiveWebInitializer) servletContext
				.getAttribute(ReactiveWebInitializer.NEUTRINO_REACTIVE_WEB_INITIALIZER);
		
		reactiveWebInitializer.registerParentRefreshListner(applicationContext);
	}

}