package com.nucleus.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

public class RemotePropertiesResolver {
	
	private static final Logger LOGGER                 = LoggerFactory.getLogger(RemotePropertiesResolver.class);

	private Environment environment;

	private Properties prop;
	
	private static final  String CONFIG_MANAGER_URI="remote.config.manager.uri";

	private static final  String CLIENT_APP_NAME="remote.client.app.name";

	private static final  String CLIENT_APP_ENV="remote.client.environment.name";

	private static final  String CLIENT_APP_VERSION="remote.client.version";

	private static final  String CLIENT_APP_NODE="remote.client.node.name";

	
	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public Properties getProp() {
		return prop;
	}

	public void setProp(Properties prop) {
		this.prop = prop;
	}

	public RemotePropertiesResolver() {
		prop = new Properties();
		environment=new StandardEnvironment();
		try (InputStream input = new ClassPathResource("remote-config-manager.properties").getInputStream()) {
			prop.load(input);
		} catch (IOException ex) {
			LOGGER.error("Could not read remote-config-manager.properties ",ex);
		}
	}

	public String getConfigManagerUri() {

		return getProperty(CONFIG_MANAGER_URI);
	}

	private String getProperty(String property) {
		return environment==null || environment.getProperty(property) == null ? prop.getProperty(property)
				: environment.getProperty(property);
	}

	public String getAppName() {
		return getProperty(CLIENT_APP_NAME);
	}

	public String getEnvironmentName() {
		return getProperty(CLIENT_APP_ENV);
	}

	public String getVersion() {
		return getProperty(CLIENT_APP_VERSION);
	}

	public String getNodeName() {
		return getProperty(CLIENT_APP_NODE);
	}

}
