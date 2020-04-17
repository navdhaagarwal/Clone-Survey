package com.nucleus.infinispan.console.custom;

import java.util.Properties;

import org.hibernate.service.ServiceRegistry;
import org.infinispan.hibernate.cache.commons.DefaultCacheManagerProvider;
import org.infinispan.manager.EmbeddedCacheManager;

public class NeutrinoDefaultCacheManagerProvider extends DefaultCacheManagerProvider {

	private final ServiceRegistry registry;

	public NeutrinoDefaultCacheManagerProvider(ServiceRegistry registry) {
		super(registry);
		this.registry = registry;
	}

	@Override
	public EmbeddedCacheManager getEmbeddedCacheManager(Properties properties) {
		return new NeutrinoDefaultCacheManager(loadConfiguration(this.registry, properties), true);
	}
}
