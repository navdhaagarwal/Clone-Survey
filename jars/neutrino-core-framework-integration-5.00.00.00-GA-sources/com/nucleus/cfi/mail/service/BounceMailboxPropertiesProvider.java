package com.nucleus.cfi.mail.service;

import java.util.Properties;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Value;

@Named("bounceMailboxPropertiesProvider")
public class BounceMailboxPropertiesProvider {
	
    @Value("${bounce.mail.pop3.host}")
    private String  pop3Host = "";

    @Value("${bounce.mail.pop3.port}")
    private Integer pop3Port = 0;

    @Value("${bounce.mail.pop3.starttls.enable}")
    private Boolean tlsEnabled = Boolean.TRUE;

    @Value("${bounce.mail.store.protocol}")
    private String mailStoreProtocol = "";   // values like pop3s.

    @Value("${bounce.mail.username}")
    private String  username = "";

    @Value("${bounce.mail.password}")
    private String  bounceMailpassd = "";
    
    public Properties getDefaultProperties() {
    	Properties properties = new Properties();
		properties.put("mail.pop3.host", pop3Host);
		properties.put("mail.pop3.port", pop3Port);
		properties.put("mail.pop3.starttls.enable", tlsEnabled);
		properties.put("mail.store.protocol", mailStoreProtocol);
		properties.put("username", username);
		properties.put("password", bounceMailpassd);
		return properties;
    }
	
}
