package com.nucleussoft.reactive.init;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurationSupport;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@ComponentScan(basePackages = "com.nucleussoft.reactive")
@PropertySource("classpath:core-framework-config/reactive-config.properties")
@ImportResource({ "classpath:spring-config/app/external-system-property-configurer.xml",
		"classpath:spring-config/app/external-system-property-placeholder-configurer.xml" })
@EnableWebFlux
public class ReactiveServletConfig extends WebFluxConfigurationSupport {

	@Bean(name = "FluxRequestMappingHandlerAdapter")
	public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
		RequestMappingHandlerAdapter adapter = createRequestMappingHandlerAdapter();
		adapter.setMessageReaders(serverCodecConfigurer().getReaders());
		adapter.setWebBindingInitializer(getConfigurableWebBindingInitializer());
		adapter.setReactiveAdapterRegistry(webFluxAdapterRegistry());

		ArgumentResolverConfigurer configurer = new ArgumentResolverConfigurer();
		configureArgumentResolvers(configurer);
		adapter.setArgumentResolverConfigurer(configurer);

		return adapter;
	}

}
