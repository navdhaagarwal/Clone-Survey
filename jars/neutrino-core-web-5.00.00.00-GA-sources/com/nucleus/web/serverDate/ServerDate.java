package com.nucleus.web.serverDate;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Component;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.SystemEntity;

@Component("serverDate")
public class ServerDate {

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	public String getServerCurrentDate() {
		Date date = new Date();
		ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.date.formats");
		SimpleDateFormat formatter = null;
		if(configVo!=null && configVo.getPropertyValue()!=null){
			formatter = new SimpleDateFormat(configVo.getPropertyValue());
		}else{
			formatter = new SimpleDateFormat("dd/MM/yyyy");
		}
		String serverCurrentDate = formatter.format(date);
		return serverCurrentDate;
	}

	public boolean serverDateFlag() {
		boolean serverFlag = false;
	
		ConfigurationVO configVo = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "show.server.date");
		if (configVo != null && configVo.getPropertyValue()!=null) {
			String[] serverDateFlag = configVo.getPropertyValue().split(",");
			List<String> prodList = Arrays.asList(serverDateFlag);

			ProductInformationLoader productInformationLoader = new ProductInformationLoader();
			String prodCode = productInformationLoader.getProductCode();
			serverFlag = prodList.contains(prodCode);
		}
		
		return serverFlag;

	}

}
