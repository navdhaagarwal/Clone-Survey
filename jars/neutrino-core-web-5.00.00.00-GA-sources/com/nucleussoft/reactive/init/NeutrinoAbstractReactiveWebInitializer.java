package com.nucleussoft.reactive.init;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.web.server.adapter.AbstractReactiveWebInitializer;

/**
 * 
 * @author gajendra.jatav
 *
 */
public abstract class NeutrinoAbstractReactiveWebInitializer extends AbstractReactiveWebInitializer{

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		String servletName = getServletName();
		Assert.hasLength(servletName, "getServletName() must not return null or empty");

		ApplicationContext applicationContext = createApplicationContext();
		Assert.notNull(applicationContext, "createApplicationContext() must not return null");

//		refreshApplicationContext(applicationContext);
		registerCloseListener(servletContext, applicationContext);

		NeutrinoServletHttpHandlerAdapter servlet = new NeutrinoServletHttpHandlerAdapter();

		ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, servlet);
		if (registration == null) {
			throw new IllegalStateException("Failed to register servlet with name '" + servletName + "'. " +
					"Check if there is another servlet registered under the same name.");
		}
		registration.setLoadOnStartup(1);
		registration.addMapping(getServletMapping());
		registration.setAsyncSupported(true);
	}
}
