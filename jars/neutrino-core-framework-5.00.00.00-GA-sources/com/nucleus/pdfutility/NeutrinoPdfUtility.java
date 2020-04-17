package com.nucleus.pdfutility;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.logging.BaseLoggers;
/*
 * @author gajendra.jatav
 */
public class NeutrinoPdfUtility {

	/**
	 * 
	 * @param ownerPassword
	 *            The owner's password.
	 * @param userPassword
	 *            The users's password.
	 * @param permissions
	 *            The access permissions given to the user.
	 * @return encripted pdf
	 * 
	 */
	public static byte[] encryptPdfContent(byte[] inputContent, String ownerPassword, String userPassword) {
		PDDocument pdDocument=null;

		if (ownerPassword == null|| userPassword == null){
			return inputContent;
		}
		
		byte[] output = null;
		try {
			pdDocument = PDDocument.load(inputContent);
			AccessPermission ap = new AccessPermission();
			int keyLength = 128;			
			StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPassword, userPassword, ap);
			spp.setEncryptionKeyLength(keyLength);
			spp.setPermissions(ap);
			pdDocument.protect(spp);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			pdDocument.save(outputStream);
			output = outputStream.toByteArray();
		} catch (IOException e) {
			BaseLoggers.flowLogger.error("Error in password encryption of attachment "+e);
			throw new SystemException(e);
		} finally {
			IOUtils.closeQuietly(pdDocument);
		}
		return output;
	}
}
