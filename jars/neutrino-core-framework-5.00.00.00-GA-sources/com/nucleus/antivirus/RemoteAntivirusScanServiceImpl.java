package com.nucleus.antivirus;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.nucleus.core.datastore.service.AntiVirusStatus;
import com.nucleus.file.InMemoryFileResource;
import com.nucleus.logging.BaseLoggers;

/**
 * 
 * @author gajendra.jatav
 *
 */
public class RemoteAntivirusScanServiceImpl extends AbstractAntivirusScanServiceImpl {

	@Value(value = "#{'${antivirus.remoteantivirus.service.url}'}")
	private String remoteAntivirusServiceUrl;

    @Inject
    @Named("neutrinoRestTemplate")
	private RestTemplate restTemplate;

	public String getRemoteAntivirusServiceUrl() {
		return remoteAntivirusServiceUrl;
	}

	public void setRemoteAntivirusServiceUrl(String remoteAntivirusServiceUrl) {
		this.remoteAntivirusServiceUrl = remoteAntivirusServiceUrl;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}


	@Override
	public AntiVirusStatus fileScanner(InputStream fileInputStream, String fileName){

		
		byte[] fileContent;
		try {
			ResponseEntity<Integer> response;
			fileContent = IOUtils.toByteArray(fileInputStream);
			LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
			map.add("file", new InMemoryFileResource(fileContent, fileName));
			map.add("fileName", fileName);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<LinkedMultiValueMap<String, Object>> request = new HttpEntity<>(
					map, headers);
			BaseLoggers.flowLogger.info(" ===== making request testFileContentSubmission========= ");
			response = restTemplate.postForEntity(remoteAntivirusServiceUrl, request,
					Integer.class);
			
			if(response==null || response.getBody() ==null )
			{
				return AntiVirusStatus.PROBLEM_OCCURED;
			}

			BaseLoggers.flowLogger.info("===== response ======={}==", response.getBody());
			return AntiVirusStatus.fromValue(response.getBody());

		} catch (IOException e) {
			BaseLoggers.exceptionLogger.debug("Exception while remote file scanning",e);
			return AntiVirusStatus.PROBLEM_OCCURED;
		}
	}

}
