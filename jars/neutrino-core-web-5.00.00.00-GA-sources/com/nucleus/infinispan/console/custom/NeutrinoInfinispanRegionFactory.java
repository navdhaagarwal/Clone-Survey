package com.nucleus.infinispan.console.custom;

import java.util.Properties;
import java.util.ServiceLoader;

import org.hibernate.service.ServiceRegistry;
import org.infinispan.hibernate.cache.main.InfinispanRegionFactory;
import org.infinispan.hibernate.cache.spi.EmbeddedCacheManagerProvider;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class NeutrinoInfinispanRegionFactory extends InfinispanRegionFactory {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected EmbeddedCacheManager createCacheManager(Properties properties, ServiceRegistry serviceRegistry) {
		for (EmbeddedCacheManagerProvider provider : ServiceLoader.load(EmbeddedCacheManagerProvider.class,
				EmbeddedCacheManagerProvider.class.getClassLoader())) {
			EmbeddedCacheManager cacheManager = provider.getEmbeddedCacheManager(properties);
			if (cacheManager != null) {
				return cacheManager;
			}
		}
		return new NeutrinoDefaultCacheManagerProvider(serviceRegistry).getEmbeddedCacheManager(properties);
	}
}
