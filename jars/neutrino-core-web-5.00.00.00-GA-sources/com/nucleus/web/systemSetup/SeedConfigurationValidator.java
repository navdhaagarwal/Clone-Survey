package com.nucleus.web.systemSetup;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.license.content.model.LicenseFeature;
import com.nucleus.license.model.ApplicationLicense;
import com.nucleus.license.model.LicenseText;
import com.nucleus.license.model.LicenseValidator;
import com.nucleus.license.model.ValidationStatus;

@Named(value = "seedConfigurationValidator")
public class SeedConfigurationValidator {

    @Inject
    @Named("messageSource")
    protected MessageSource messageSource;

    private final String    MSG_ERROR   = "error";
    private final String    MSG_SUCCESS = "success";

    public String validateLicenseKeyText(String licensePublicKey, String licenseEnteredText,
            CommonsMultipartFile attachedLicenseKeyFile, CommonsMultipartFile attachedLicenseTextFile,
            HttpServletRequest request) throws JAXBException {

        LicenseText licenseTextObj = null;
        String message = null;
        Locale loc = RequestContextUtils.getLocale(request);
        String encryptedLicenseKey = null, encryptedLicenseText = null;

        /*
         * 
         * To ensure that both file upload and writing contents in box individually for key and text fields are not allowed. 
         * 
         */
        if ((licensePublicKey != null && !licensePublicKey.isEmpty())
                && (attachedLicenseKeyFile != null && !attachedLicenseKeyFile.isEmpty())) {
            message = messageSource.getMessage("label.license.either.upload.file.or.enter.key", null, loc);
            message += "," + MSG_ERROR;
            return message;
        }

        if ((licenseEnteredText != null && !licenseEnteredText.isEmpty())
                && (attachedLicenseTextFile != null && !attachedLicenseTextFile.isEmpty())) {
            message = messageSource.getMessage("label.license.either.upload.file.or.enter.text", null, loc);
            message += "," + MSG_ERROR;
            return message;
        }

        /*
         * 
         * Validating varying combinations of uploaded files or entered key/string
         * 
         */
        try {

            boolean isKeyFileEmpty, isTextFileEmpty, isKeyEmpty, isTextEmpty;

            isKeyFileEmpty = (attachedLicenseKeyFile == null || attachedLicenseKeyFile.isEmpty());
            isTextFileEmpty = (attachedLicenseTextFile == null || attachedLicenseTextFile.isEmpty());
            isKeyEmpty = (licensePublicKey == null || licensePublicKey.isEmpty());
            isTextEmpty = (licenseEnteredText == null || licenseEnteredText.isEmpty());

            if ((!isKeyFileEmpty || !isKeyEmpty) && (!isTextFileEmpty || !isTextEmpty)) {
                if (!isKeyEmpty) {
                    encryptedLicenseKey = licensePublicKey;
                } else {
                    InputStream keyFileInputStream = attachedLicenseKeyFile.getInputStream();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(keyFileInputStream, writer, "UTF-8");
                    encryptedLicenseKey = writer.toString();
                }

                if (!isTextEmpty) {
                    encryptedLicenseText = licenseEnteredText;
                } else {
                    InputStream textFileInputStream = attachedLicenseTextFile.getInputStream();
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(textFileInputStream, writer, "UTF-8");
                    encryptedLicenseText = writer.toString();
                }

                licenseTextObj = new LicenseText(encryptedLicenseText, encryptedLicenseKey);
            } else {
                message = messageSource.getMessage("label.license.key.string.combination."
                        + ValidationStatus.LICENSE_INVALID.toString(), null, loc);
                message += "," + MSG_ERROR;
                return message;
            }
        } catch (Exception ex) {
            message = messageSource.getMessage("label.license.corrupt." + ValidationStatus.LICENSE_INVALID.toString(), null,
                    loc);
            message += "," + MSG_ERROR;
            return message;
        }
        licenseTextObj.getLicenseProperties();
        String productCode = ProductInformationLoader.getProductCode();
        String productVersion = ProductInformationLoader.getProductVersion();

        JAXBContext jaxbContext = JAXBContext.newInstance(LicenseFeature.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(licenseTextObj.getLicenseProperties().getProperty("licf-custom"));
        LicenseFeature licFeature = (LicenseFeature) unmarshaller.unmarshal(reader);
        DateTime expiryDateTime = licFeature.getLicenseDetail().getExpiryDate();
        if(licFeature.getLicenseDetail().getGracePeriod()!=null)
        {
        	expiryDateTime=expiryDateTime.plusDays(licFeature.getLicenseDetail().getGracePeriod());
        }
        Date  expiryDate=expiryDateTime.toDate();
       
        
      ApplicationLicense applicationLicense = LicenseValidator.validate(encryptedLicenseText, encryptedLicenseKey,
    		  productCode, productCode, productVersion, expiryDate,licFeature);
        applicationLicense.getPublicKey();
        applicationLicense.getActivationStatus();

        ValidationStatus appliedLicenseStatus = applicationLicense.getValidationStatus();

        if (appliedLicenseStatus.equals(ValidationStatus.LICENSE_VALID)) {
            message = messageSource.getMessage("label.license.status." + appliedLicenseStatus.toString(), null, loc);
            message += "," + MSG_SUCCESS;
            return message;
        } else {
            message = messageSource.getMessage("label.license.status." + appliedLicenseStatus.toString(), null, loc);
            message += "," + MSG_ERROR;
            return message;
        }

    }

}
