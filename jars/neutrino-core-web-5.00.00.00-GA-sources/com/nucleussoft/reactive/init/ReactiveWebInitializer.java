package com.nucleussoft.reactive.init;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SourceFilteringListener;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class ReactiveWebInitializer extends NeutrinoAbstractReactiveWebInitializer implements PriorityOrdered {

	private ServletContext servletContext;

	public static final String NEUTRINO_REACTIVE_CONTEXT = "NEUTRINO_REACTIVE_CONTEXT";

	public static final String NEUTRINO_REACTIVE_CONTEXT_CONFIG_CLASSES = "neutrinoReactiveContextConfigClasses";

	public static final String NEUTRINO_REACTIVE_SERVLET_MAPPINGS = "neutrinoReactiveServletMappings";

	public static final String NEUTRINO_REACTIVE_WEB_INITIALIZER = "NEUTRINO_REACTIVE_WEB_INITIALIZER";

	public static final String NEUTRINO_REACTIVE_SERVLET_NAME = "NEUTRINO_REACTIVE_SERVLET_NAME";

    private static final Logger LOGGER                 = LoggerFactory.getLogger(ReactiveWebInitializer.class);
	
	private AtomicBoolean isReactiveContextCreated = new AtomicBoolean(false);

	private ApplicationContext reactiveApplicationContext;

	private Boolean closeListnerRegistered = false;

	@Override
	protected String getServletName() {
		return NEUTRINO_REACTIVE_SERVLET_NAME;
	}

	@Override
	protected Class<?>[] getConfigClasses() {
		return new Class[] { ReactiveServletConfig.class };
	}

	@Override
	protected String getServletMapping() {
		return "/react/*";
	}

	@Override
	public int getOrder() {
		return PriorityOrdered.LOWEST_PRECEDENCE;
	}

	/**
	 * Creates reactive context
	 */
	@Override
	protected ApplicationContext createApplicationContext() {
		if (reactiveApplicationContext != null) {
			return reactiveApplicationContext;
		}
		AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
		context.setServletContext(servletContext);
		if (!StringUtils.isEmpty(this.servletContext.getInitParameter(NEUTRINO_REACTIVE_CONTEXT_CONFIG_CLASSES))) {
			context.setConfigLocation(this.servletContext.getInitParameter(NEUTRINO_REACTIVE_CONTEXT_CONFIG_CLASSES));
		}
		Class<?>[] configClasses = getConfigClasses();
		Assert.notEmpty(configClasses, "No Spring configuration provided through getConfigClasses()");
		context.register(configClasses);
		servletContext.setAttribute(NEUTRINO_REACTIVE_CONTEXT, context);
		reactiveApplicationContext = context;
		ReactiveContextUtil.setReactiveAppContext(context);
		return context;
	}

	/**
	 * Close listner is already registered in onStartup so no need to do the
	 * same again
	 */
	@Override
	protected void registerCloseListener(ServletContext servletContext, ApplicationContext applicationContext) {
		if (!closeListnerRegistered) {
			super.registerCloseListener(servletContext, applicationContext);
			closeListnerRegistered = true;
		}
	}

	/**
	 * set NEUTRINO_REACTIVE_WEB_INITIALIZER and create reactive context,
	 * register close Listener
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		servletContext.log("Adding reactive servlet and other reactive handlers");
		setReactiveInitializerReference(servletContext);
		registerCloseListener(servletContext, createApplicationContext());
		super.onStartup(servletContext);
		setAdditionalServletMappings();
	}

	private void setReactiveInitializerReference(ServletContext servletContext) {
		this.servletContext = servletContext;
		this.servletContext.setAttribute(NEUTRINO_REACTIVE_WEB_INITIALIZER, this);
	}

	/**
	 * Get notification of parent application refresh and post that refresh
	 * reactive context
	 * 
	 * @author gajendra.jatav
	 *
	 */
	public class ContextRefreshListener implements ApplicationListener<ContextRefreshedEvent> {

		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			if (!isReactiveContextCreated.get()) {
				onParentContextRefresh(event);
				isReactiveContextCreated.compareAndSet(false, true);
			}
		}
	}

	public void onParentContextRefresh(ContextRefreshedEvent event) {
		((ConfigurableApplicationContext) this.reactiveApplicationContext).setParent(event.getApplicationContext());
		((ConfigurableWebEnvironment)reactiveApplicationContext.getEnvironment())
				.initPropertySources(servletContext, null);
		servletContext.log("Refreshing reactive application context");
		refreshApplicationContext(reactiveApplicationContext);
	}

	/**
	 * Add additional mapping to reactive servlet from context param
	 * neutrinoReactiveServletMappings
	 */
	public void setAdditionalServletMappings() {
		if (!StringUtils.isEmpty(this.servletContext.getInitParameter(NEUTRINO_REACTIVE_SERVLET_MAPPINGS))) {
			servletContext.getServletRegistration(getServletName())
					.addMapping(StringUtils.tokenizeToStringArray(
							this.servletContext.getInitParameter(NEUTRINO_REACTIVE_SERVLET_MAPPINGS),
							ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
		}
	}

	/**
	 * To be invoked when parent application context is created
	 * 
	 * @param context
	 *            parent application context
	 */
	protected void registerParentRefreshListner(ApplicationContext context) {

		((ConfigurableApplicationContext) context)
				.addApplicationListener(new SourceFilteringListener(context, new ContextRefreshListener()));
	}

}
