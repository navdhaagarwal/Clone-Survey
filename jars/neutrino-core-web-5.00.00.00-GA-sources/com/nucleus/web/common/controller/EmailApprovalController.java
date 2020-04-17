/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.common.controller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.authenticationToken.AuthenticationTokenConstants;
import com.nucleus.authenticationToken.AuthenticationTokenService;
import com.nucleus.authenticationToken.TokenDetails;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.mail.MailService;
import com.nucleus.template.TemplateService;

/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Controller
@Transactional
@RequestMapping(value = "/emailApproval")
public class EmailApprovalController extends BaseController {

    @Inject
    @Named("authenticationTokenService")
    protected AuthenticationTokenService authenticationTokenService;

    @Inject
    @Named("mailService")
    private MailService                  mailService;

    @Inject
    @Named("templateService")
    private TemplateService              templateService;
    
    @Inject
    @Named("genericParameterService")
    private GenericParameterService      genericParameterService;

    @Value("${core.web.config.email.validation.token.validity.time.millis}")
    private String                       emailValidationTokenvalidityInMilliSecond;
    
    @Value(value = "#{'${config.proxy.address}'}")
    private String                    proxyAddress;
    
    @Value(value = "#{'${config.proxy.port}'}")
    private String                    proxyPort;
    
    @Value(value = "#{'${config.proxy.username}'}")
    private String                    proxyUserName;
    
    @Value(value = "#{'${config.proxy.password}'}")
    private String                    proxyPassword;
    
    @Value(value = "#{'${emailvalidation.mail.from}'}")
    private String                    fromEmaiId;
    
    
    @RequestMapping(value = "/approveEmail/{timeTokenID}")
    public String emailApproval(@PathVariable String timeTokenID, ModelMap model) {
        TokenDetails details = authenticationTokenService.getUserTaskAndStatusMapFromTokenId(timeTokenID);
        String taskId = null;
        Long userId = null;
        String status = details.getStatus();

        if (AuthenticationTokenConstants.INVALID_TOKEN.equals(status)) {
            model.put("invalidToken", "True");
        } else if (AuthenticationTokenConstants.PAGE_EXPIRED.equals(status)) {
            model.put("taskId", details.getTaskId());
            model.put("pageExpired", "True");
        } else {
            taskId = details.getTaskId();
            userId = details.getUserId();
            model.put("userId", userId);
            model.put("taskId", taskId);
            BaseLoggers.flowLogger.debug("Opening Application : " + taskId);
        }

        return "dummyApprovalPage";
    }

    /**
     * This method will be called when user click of verify email. The a
     * emailAuthentication token will be generated and persisted and email will
     * be sent to user of validation
     * 
     * @param emailAddress
     * @param request
     * @return
     */
    @RequestMapping(value = "/sendVerificationMail", method = RequestMethod.POST)
    public @ResponseBody
    String sendVerificationEmail(@RequestParam("emailAddress") String emailAddress, HttpServletRequest request) {
    	
        if (emailAddress != null && !emailAddress.isEmpty()) {
            TokenDetails tokenDetails = new TokenDetails();
            tokenDetails.setEmailId(emailAddress);
            String timeToken = authenticationTokenService.generateEmailAuthenticationToken(tokenDetails,
                    emailValidationTokenvalidityInMilliSecond);
            Map<String, String> keyMap = new HashMap<String, String>();
            String url = "" + request.getRequestURL();
            String urlPart[] = url.split(request.getContextPath());
            keyMap.put("APPPATH", urlPart[0] + request.getContextPath() + "/app/emailApproval/authenticateEmail/"
                    + timeToken);
            
            try {
                String subjectLine = templateService.getResolvedStringFromResourceBundle("emailvalidation.mail.subject",
                        null, keyMap);
                String htmlBody = templateService.getResolvedStringFromResourceBundle("emailvalidation.mail.body", null,
                        keyMap);
                BaseLoggers.flowLogger.debug("Sending Mail to the Verifying Email ID");
                mailService.sendMail(htmlBody, subjectLine, emailAddress, fromEmaiId);
                BaseLoggers.flowLogger.debug("Mail Sent to the Verifying Email ID");
                return "success";
            } catch (Exception e) {
                BaseLoggers.exceptionLogger
                        .error("Exception in Email Verification -- Could not send email" + e.getMessage());
            }
        }
        return "failure";
    }

    @RequestMapping(value = "/verifyEmailAddress", method = RequestMethod.POST)
    public @ResponseBody
    String verifyEmailAddress(@RequestParam("emailAddress") String emailAddress, HttpServletRequest request) {
        if (emailAddress == null || emailAddress.isEmpty()) {
            return "failure";
        }
        String hostName = StringUtils.substringAfter(emailAddress, "@");
        if (hostName == null) {
            return "failure";
        } else {
            try {
                InetAddress.getByName(hostName);
            } catch (UnknownHostException e) {
                return "failure";
            }
        }
        return "success";
    }

    /**
     * This method will be called when url mention in email is clicked and if
     * all thing if fine then email ID will get validated
     * 
     * @param timeTokenID
     * @param model
     * @return
     */
    @RequestMapping(value = "/rejectEmail/{timeTokenID}")
    public String emailRejection(@PathVariable String timeTokenID, ModelMap model) {
        TokenDetails details = authenticationTokenService.getEmailAuthenticationTokenByTokenId(timeTokenID);
        String status = details.getStatus();

        if (AuthenticationTokenConstants.INVALID_TOKEN.equals(status)) {
            model.put("invalidToken", "True");
        } else if (AuthenticationTokenConstants.PAGE_EXPIRED.equals(status)) {
            model.put("pageExpired", "True");
        } else {
            model.put("emailID", details.getEmailId());
            BaseLoggers.flowLogger.debug("Validating Email Id : " + details.getEmailId());
            authenticationTokenService.markEmailAsRejected(details.getEmailId(),details.getEmailUId());
        }
        return "eMailAuthenticationPage";
    }
    
    /**
     * This method will be called when url mention in email is clicked and if
     * all thing if fine then email ID will get validated
     * 
     * @param timeTokenID
     * @param model
     * @return
     */
    @RequestMapping(value = "/authenticateEmail/{timeTokenID}")
    public String emailAuthentication(@PathVariable String timeTokenID, ModelMap model) {
        TokenDetails details = authenticationTokenService.getEmailAuthenticationTokenByTokenId(timeTokenID);
        String status = details.getStatus();

        if (AuthenticationTokenConstants.INVALID_TOKEN.equals(status)) {
            model.put("invalidToken", "True");
        } else if (AuthenticationTokenConstants.PAGE_EXPIRED.equals(status)) {
            model.put("pageExpired", "True");
        } else {
            model.put("emailID", details.getEmailId());
            BaseLoggers.flowLogger.debug("Validating Email Id : " + details.getEmailId());
            authenticationTokenService.markEmailAsVerified(details.getEmailId(),details.getEmailUId());

        }
        return "eMailAuthenticationPage";
    }
    
    @RequestMapping(value = "/validateEmailAddress", method = RequestMethod.POST)
    public @ResponseBody
    String validateEmailAddress(@RequestParam("emailAddress") String emailAddress) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        String message = "success";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(emailAddress);
        String[] emailAddressParts = emailAddress.split("@");
        if (matcher.matches()) {
            if (emailAddressParts[1] != null) {
                emailAddressParts[1] = emailAddressParts[1].toUpperCase();
                if (emailAddressParts[1].length() < 4) {
                    message = "failure";
                }
            }
           /* List<EmailDomain> emailDomainList = genericParameterService.retrieveTypes(EmailDomain.class);
            if (emailDomainList != null && emailDomainList.size() > 0) {
                List<String> domainList = new ArrayList<String>();
                for (EmailDomain emailDomain : emailDomainList) {
                    if (emailDomain != null && emailDomain.getCode() != null) {
                        domainList.add(emailDomain.getCode().toUpperCase());
                    }
                }
                if (!domainList.contains(emailAddressParts[1])) {
                    message = "failure";
                }
            }*/
            /*HttpURLConnection uc = null;
            try {
            	ConfigurationVO configProxy = getUserDetails().getUserPreferences().get("config.proxy.address");
            	String proxyAddress = configProxy.getText();
            	
            	Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, 8080));
            	proxy.
            	URL url = new URL("http://www."+emailAddressParts[1]);
            	uc = (HttpURLConnection)url.openConnection(proxy);

            	uc.connect();
            	int responseCode = uc.getResponseCode();
            	evaluating response codes
            	 * 1. 504 Gateway Timeout Exception
            	 * 2. 503 Service Unavailable
            	 
            	if(responseCode == 504 || responseCode== 503){
            		return "failure";
            	}
            	
                InetAddress inetAddress = InetAddress.getByName(emailAddressParts[1]);
                if (!inetAddress.isReachable(5000)) {
                    message = "failure";
                } 
            } catch (UnknownHostException e) {
                message = "failure";
            } catch (IOException e) {
                message = "failure";
            } finally{
            	uc.disconnect();
            }*/
            if(!proxyAddress.equalsIgnoreCase("none") && !proxyPort.equalsIgnoreCase("none")
            		&& !proxyUserName.equalsIgnoreCase("none") && !proxyPassword.equalsIgnoreCase("none")){
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            credsProvider.setCredentials(
                    new AuthScope(proxyAddress, Integer.parseInt(proxyPort)),
                    new UsernamePasswordCredentials(proxyUserName, proxyPassword));
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider).build();
            try {
                HttpHost target = new HttpHost("www."+emailAddressParts[1], 80, "http");
                HttpHost proxy = new HttpHost(proxyAddress, Integer.parseInt(proxyPort));

                RequestConfig config = RequestConfig.custom()
                    .setProxy(proxy)
                    .build();
                HttpGet httpget = new HttpGet("/");
                httpget.setConfig(config);

                BaseLoggers.flowLogger.debug("Executing request " + httpget.getRequestLine() + " to " + target + " via " + proxy);

                CloseableHttpResponse response = httpclient.execute(target, httpget);
                try {
                	int code = response.getStatusLine().getStatusCode();
                	//Status code of type 2xx represents Success
                	if(code < 200 || code > 299){
                		return "failure";
                	}
                } finally {
                    response.close();
                }
            } catch (IOException e) {
				message = "failure";
			} finally {
                try {
					httpclient.close();
				} catch (IOException e) {
					message = "failure";
				}
            }
            		}
        } else {
            message = "failure";
        }
        return message;
    }
}
