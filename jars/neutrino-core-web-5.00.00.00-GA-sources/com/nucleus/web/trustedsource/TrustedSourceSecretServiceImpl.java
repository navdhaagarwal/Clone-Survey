package com.nucleus.web.trustedsource;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import com.nucleus.core.misc.util.PasswordEncryptorUtil;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.persistence.EntityDao;
import com.nucleus.security.oauth.domainobject.OauthClientDetails;
import com.nucleus.web.security.AesUtil;

@Named("trustedSourceSecretService")
class TrustedSourceSecretServiceImpl implements TrustedSourceSecretService{

	@Inject
	@Named("entityDao")
	private EntityDao                   entityDao;
	
	
	@Override
	@Transactional
	public void persistTrustedSource(OauthClientDetails trustedSource) {
		
		entityDao.persist(trustedSource);
	}

	@Override
	public void setDefaultData(OauthClientDetails trustedSource) {
		String currentTime = String.valueOf(System.currentTimeMillis());
		String passPhrase = AesUtil.encrypt(currentTime,
				OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE);
		trustedSource.setHashKey(currentTime);
		String originalClientSecret = UUID.randomUUID().toString();
		trustedSource.setPassPhrase(passPhrase);
		trustedSource.setEncryptedSecret(AesUtil.encrypt(originalClientSecret,
				OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE));
		try {
			trustedSource.setClientSecret(PasswordEncryptorUtil.encryptPassword(

			originalClientSecret, trustedSource.getHashKey()));

		} catch (NoSuchAlgorithmException e) {

			throw new SystemException(e);
		}
	}

	@Override
	public void editClientSecret(OauthClientDetails trustedSource) {
		String modifiedSecret = trustedSource.getClientSecret();
		if (notNull(modifiedSecret) && !modifiedSecret.isEmpty()) {

			try {
				trustedSource.setClientSecret(PasswordEncryptorUtil
						.encryptPassword(modifiedSecret,
								trustedSource.getHashKey()));
			} catch (NoSuchAlgorithmException e) {
				throw new SystemException(e);
			}
			trustedSource.setEncryptedSecret(AesUtil.encrypt(
					modifiedSecret,
					OauthClientDetails.SHARED_OAUTH_ENCYPTION_PASS_PHRASE));

		}
	}
	
}
