package com.nucleus.security.oauth;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.entity.SystemEntity;
import com.nucleus.web.security.AesUtilHelper;

@Named("passwordEncodingUtil")
public class PasswordEncodingUtil {

	private static final String IV_KEY = "config.encryption.iv";
	private static final String SALT_KEY = "config.encryption.salt";
	private static final String KEY_SIZE_KEY = "config.encryption.keysize";
	private static final String ITERATION_COUNT_KEY = "config.encryption.iterationcount";

	private String iv;
	private String salt;
	private int keysize;
	private int iterationCount;

	public String encryptPassword(String password, String passphrase) {
		AesUtilHelper util = new AesUtilHelper(keysize, iterationCount);
		return util.encrypt(salt, iv, passphrase, password);
	}

	@PostConstruct
	public void init() {
		ConfigurationService config = NeutrinoSpringAppContextUtil.getBeanByName("configurationService",
				ConfigurationService.class);
		Map<String, ConfigurationVO> conf = config.getFinalConfigurationForEntity(SystemEntity.getSystemEntityId());
		iv = conf.get(IV_KEY).getPropertyValue();
		salt = conf.get(SALT_KEY).getPropertyValue();
		keysize = Integer.parseInt(conf.get(KEY_SIZE_KEY).getPropertyValue());
		iterationCount = Integer.parseInt(conf.get(ITERATION_COUNT_KEY).getPropertyValue());
	}
}
