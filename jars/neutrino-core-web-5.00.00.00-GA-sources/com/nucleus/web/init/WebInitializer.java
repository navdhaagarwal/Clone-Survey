package com.nucleus.web.init;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import com.nucleus.configuration.RemotePropertiesResolver;
import com.nucleus.configuration.RemoteResourcesLoader;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class WebInitializer implements ServletContainerInitializer{

    private static final Logger LOGGER                 = LoggerFactory.getLogger(WebInitializer.class);
	
    public static final String REMOTE_CONFIG_PROFILE="remote-config-enabled";
    
	@Override
	public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
		
		if(isRemoteProfileEnabled()){
			RemotePropertiesResolver propertiesResolver=new RemotePropertiesResolver();
			RemoteResourcesLoader remoteResourcesLoader=new RemoteResourcesLoader();
			remoteResourcesLoader.setRemotePropertiesResolver(propertiesResolver);
			try {
				System.getProperties().load(remoteResourcesLoader.getObject().getInputStream());
			} catch (Exception e) {
				LOGGER.error("Not able to load and set remote properties", e);
				throw new ServletException(e);
			}

		}
	}

	private boolean isRemoteProfileEnabled() {
		Environment environment=new StandardEnvironment();
		return environment.acceptsProfiles(RemoteResourcesLoader.REMOTE_CONFIG_PROFILE);
	}

}
