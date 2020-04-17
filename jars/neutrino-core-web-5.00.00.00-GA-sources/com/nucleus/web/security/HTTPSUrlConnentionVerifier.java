package com.nucleus.web.security;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.springframework.beans.factory.annotation.Value;

import com.nucleus.logging.BaseLoggers;

/**
 * @author Nucleus Software Exports Limited.
 * Java follows the HTTPS specification strictly when it comes to the server identity verification (RFC 2818, Section 3.1) and IP addresses.
 * While communicating with Remote servers over HTTPS protocol, it is possible that remote server is mis-configured for e.g. the remote server uses a certificate generated at some other system.
 * This class provides a quick workaround to by pass the validation check by java, which validates that IP of the remote server in the request URL must match the server name maintained the certificate being sent by the remote server.
 * This would be helpful in development stage of the application.    
 */
@Named
public class HTTPSUrlConnentionVerifier {

    // comma separated list of IPs.
    @Value(value = "#{'${ignoreCertificateMatchFromServers}'}")
    private String ignoreCertificateMatchFromServers;

    @PostConstruct
    public void setVerificationStrategy() {
        BaseLoggers.securityLogger.info("setting setDefaultHostnameVerifier for HTTPS requests");
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession arg1) {
                if (ignoreCertificateMatchFromServers.contains(hostname)) {
                    BaseLoggers.securityLogger.debug("The HTTPSUrlConnentionVerifier verified the request");
                    return true;
                }
                BaseLoggers.securityLogger.warn("The HTTPSUrlConnentionVerifier did not verify the request");
                return false;
            }
        });
    }

}
