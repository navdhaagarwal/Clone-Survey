package com.nucleus.infinispan.console.custom;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.manager.DefaultCacheManager;

import com.nucleus.infinispan.console.event.ClusterEventHistoryLoggerListner;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoDefaultCacheManager extends DefaultCacheManager{

	private static Set<DefaultCacheManager> availableCacheManager=new HashSet<>();
	
	public NeutrinoDefaultCacheManager() {
		super();
		postCacheManagerCreation();
	}

	protected void postCacheManagerCreation() {
		availableCacheManager.add(this);
		this.addListener(new ClusterEventHistoryLoggerListner());
	}

	public NeutrinoDefaultCacheManager(boolean start) {
		super(start);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(Configuration defaultConfiguration, boolean start) {
		super(defaultConfiguration, start);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(Configuration defaultConfiguration) {
		super(defaultConfiguration);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(ConfigurationBuilderHolder holder, boolean start) {
		super(holder, start);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(GlobalConfiguration globalConfiguration, boolean start) {
		super(globalConfiguration, start);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(GlobalConfiguration globalConfiguration, Configuration defaultConfiguration,
			boolean start) {
		super(globalConfiguration, defaultConfiguration, start);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(GlobalConfiguration globalConfiguration, Configuration defaultConfiguration) {
		super(globalConfiguration, defaultConfiguration);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(GlobalConfiguration globalConfiguration) {
		super(globalConfiguration);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(InputStream configurationStream, boolean start) throws IOException {
		super(configurationStream, start);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(InputStream configurationStream) throws IOException {
		super(configurationStream);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(String configurationFile, boolean start) throws IOException {
		super(configurationFile, start);
		postCacheManagerCreation();
	}

	public NeutrinoDefaultCacheManager(String configurationFile) throws IOException {
		super(configurationFile);
		postCacheManagerCreation();
	}

	public static Set<DefaultCacheManager> getAvailableCacheManager(){
		return availableCacheManager;
	}
	
	public static void addCacheManager(DefaultCacheManager cacheManager){
		availableCacheManager.add(cacheManager);
	}


}
