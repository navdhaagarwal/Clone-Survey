package com.nucleus.api.documentation.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.nucleus.logging.BaseLoggers;

import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;

@Component
public class NeutrinoAPIDocumentationBootstrapper implements ApplicationListener<ContextRefreshedEvent> {

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		try {
			DocumentationPluginsBootstrapper bootstrapper = event.getApplicationContext()
					.getBean(DocumentationPluginsBootstrapper.class);
			ExecutorService executor = Executors.newSingleThreadExecutor();

			executor.execute(new Runnable() {
				@Override
				public void run() {
					if (bootstrapper != null) {
						bootstrapper.start();
					}

				}
			});
			
			executor.shutdown();
			
		} catch (NoSuchBeanDefinitionException e) {
			BaseLoggers.flowLogger.debug(e.toString());
		} catch (Exception e) {
			BaseLoggers.flowLogger.debug(e.toString());
		}
	}
}
