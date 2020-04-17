package com.nucleus.cfi.message.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nucleus.cfi.message.vo.GenericMessage;
import com.nucleus.cfi.message.vo.GenericMessageResponse;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessage;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessageSendResponse;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.security.oauth.service.RESTfulAuthenticationService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

/**
 * @author Nucleus Software Exports Limited
 *
 */
@Named("whatsappIntegrationService")
public class WhatsAppIntegrationServiceImpl implements WhatsappIntegrationService {


    @Value("${INTG_BASE_URL}/app/restservice/notification/sendWhatsAppMessage")
    private String whatsappIntegrationServiceUrl;

    @Inject
    @Named("oauthauthenticationService")
    private RESTfulAuthenticationService oauthauthenticationService;

    @Value("${soap.service.trusted.client.id}")
    private String clientID;



    protected final Logger LOGGER = BaseLoggers.integrationLogger;;

    public static final String FAILED = "FAILED";

    public static final String FAILED_TO_SEND = "FAILED_TO_SEND";


    private RestTemplate restTemplate;

    public static final String ACCESS_TOKEN = "access_token";



    @Override
    public WhatsAppMessageSendResponse sendWhatsAppMessage(WhatsAppMessage whatsAppMessage){

        BaseLoggers.exceptionLogger.error("-------------------- The request for sendWhatsAppMessage "
                + "is being sent to this URL ------------------------> " + whatsappIntegrationServiceUrl);
        ResponseEntity<WhatsAppMessageSendResponse> responseEntity=getResponseEntity(whatsappIntegrationServiceUrl, whatsAppMessage, HttpMethod.POST, WhatsAppMessageSendResponse.class, oauthauthenticationService.getSecurityToken(clientID));
        WhatsAppMessageSendResponse whatsAppMessageSendResponse = responseEntity.getBody();
        return whatsAppMessageSendResponse;
    }

    /**
     *
     * @param url
     * @param requestEntity
     * @param httpMethod
     * @param responseType
     * @param token
     * @return
     */

    private <T> ResponseEntity<T> getResponseEntity(String url, Object requestEntity, HttpMethod httpMethod, Class<T> responseType, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ACCESS_TOKEN,token);
        HttpEntity<Object> entityReq = new HttpEntity<Object>(requestEntity, headers);

        return restTemplate.exchange(url,httpMethod,entityReq,responseType);

    }


    /**
     *
     * @param responseString
     * @return
     * @throws IOException
     */

    private GenericMessageResponse getObjectFromResponse(String responseString) throws IOException{

        GenericMessageResponse whatsAppMessageSendResponse;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            whatsAppMessageSendResponse= objectMapper.readValue(responseString, GenericMessageResponse.class);
        } catch (JsonParseException e) {
            whatsAppMessageSendResponse = null;
            BaseLoggers.flowLogger.debug("Exception occured while converting Response String to whatsAppMessageSendResponse : ", e);
            BaseLoggers.flowLogger.info("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);

        } catch (JsonMappingException e) {
            whatsAppMessageSendResponse = null;
            BaseLoggers.flowLogger.debug("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);
            BaseLoggers.flowLogger.info("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);
            throw e;

        } catch (IOException e) {
            whatsAppMessageSendResponse = null;
            BaseLoggers.flowLogger.debug("Exception occuredwhile converting Response String to whatsAppMessageSendResponse : " , e);
            BaseLoggers.flowLogger.info("Exception occured while converting Response String to whatsAppMessageSendResponse : " , e);
            throw e;

        }
        return whatsAppMessageSendResponse;

    }

    @PostConstruct
    void initializeRestTemplate() {
        this.restTemplate = new RestTemplate();
    }



}
