package com.nucleus.finnone.pro.communicationgenerator.util;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import javax.inject.Named;

@Named("weakReferencedConfigurationHelper")
public class WeakReferencedConfigurationHelper<K, V> {
	
	//will let it save configurations in weak referenced manner.
	private Map<K, V> weakReferencedConfigurations = new WeakHashMap<>();

	public V putConfigurationInCache(K key, V value) {
		return weakReferencedConfigurations.put(key, value);
	}
	
	public V getFromConfigurations(K key) {
		return weakReferencedConfigurations.get(key);
	}
	
	public V putConfigurationIfAbsent(K key, V value) {
		return weakReferencedConfigurations.putIfAbsent(key, value);
	}
	
	public V computeIfConfigurationAbsent(K key, Function<? super K,? extends V> functionToCompute) {
		return weakReferencedConfigurations.computeIfAbsent(key, functionToCompute);
	}
	
	public V removeConfiguration(K key) {
		return weakReferencedConfigurations.remove(key);
	}
}
