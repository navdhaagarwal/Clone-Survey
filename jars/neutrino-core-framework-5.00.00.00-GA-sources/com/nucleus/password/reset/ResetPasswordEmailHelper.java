package com.nucleus.password.reset;

import com.nucleus.finnone.pro.general.util.templatemerging.TemplateMergingUtility;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.Map;

/**
 * Created by gajendra.jatav on 7/18/2019.
 */
@Named("resetPasswordEmailHelper")
public class ResetPasswordEmailHelper {

    private String emailTemplateRootPath = "email/resetPassword/";

    @Inject
    @Named("templateMergingUtility")
    private TemplateMergingUtility templateMergingUtility;

    private String forgotPasswordEmailSubject;

    @Value(value = "#{'${email.forgot.password.subject}'}")
	public void setForgotPasswordEmailSubject(String emailSubject) {
		if (org.apache.commons.lang.StringUtils.isEmpty(emailSubject)
				|| "${email.forgot.password.subject}".equalsIgnoreCase(emailSubject)) {
			this.forgotPasswordEmailSubject = "Reset Password Link";
		} else {
			this.forgotPasswordEmailSubject = emailSubject;
		}
	}

    public String getForgotPasswordEmailSubject() {
        return forgotPasswordEmailSubject;
    }

    public String getEmailBody(Map<String, Object> dataMap,String templateName) {

        return templateMergingUtility.mergeTemplateIntoString(emailTemplateRootPath+templateName, dataMap);
    }

}
