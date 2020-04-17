package com.nucleus.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.nucleus.file.InMemoryFileResource;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class RemoteResourcesLoader implements FactoryBean<Resource> {

	private static final Logger LOGGER                 = LoggerFactory.getLogger(RemoteResourcesLoader.class);
	
    public static final String REMOTE_CONFIG_PROFILE="remote-config-enabled";

	private String fileName;

	private static Map<String, byte[]> remoteResponseCache=new ConcurrentHashMap<>();
	
	public static void cleanCache(){
		remoteResponseCache.clear();
	}
	private RemotePropertiesResolver remotePropertiesResolver;

	public RemotePropertiesResolver getRemotePropertiesResolver() {
		return remotePropertiesResolver;
	}

	public void setRemotePropertiesResolver(RemotePropertiesResolver remotePropertiesResolver) {
		this.remotePropertiesResolver = remotePropertiesResolver;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Override
	public Resource getObject() throws Exception {
		return new InMemoryFileResource(rest(), "remote-config.properties");
	}

	@Override
	public Class<?> getObjectType() {
		return Resource.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public byte[] rest() {
		
		LOGGER.debug("Fetching remote properties with following profile appName {}, envName {}, nodeName {}, version {}",
				remotePropertiesResolver.getAppName(),remotePropertiesResolver.getEnvironmentName(),
				remotePropertiesResolver.getNodeName(),remotePropertiesResolver.getVersion());
		String cacheKey=getKey();
		if(remoteResponseCache.get(cacheKey)!=null){
			return remoteResponseCache.get(cacheKey);
		}
		
		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		

		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(remotePropertiesResolver.getConfigManagerUri())
        .queryParam("appName",  remotePropertiesResolver.getAppName())
        .queryParam("envName", remotePropertiesResolver.getEnvironmentName())
        .queryParam("nodeName", remotePropertiesResolver.getNodeName())
        .queryParam("version", remotePropertiesResolver.getVersion());
		ResponseEntity<byte[]> response=null;
		try{
			response = rest.getForEntity(builder.toUriString(),
					byte[].class);
			byte[] propertiesResponse=response.getBody();
			remoteResponseCache.put(cacheKey, propertiesResponse);
			return propertiesResponse;
		}catch (Exception e) {
			LOGGER.error("Error occurred while requesting config manager for properties ",e);
			throw e;
		}
		
	}

	private String getKey() {
		return String.join(remotePropertiesResolver.getAppName(), remotePropertiesResolver.getEnvironmentName(),
				remotePropertiesResolver.getNodeName(), remotePropertiesResolver.getVersion());
	}

}
