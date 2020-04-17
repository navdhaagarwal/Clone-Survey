package com.nucleus.cfi.message.service;

import com.nucleus.cfi.message.vo.GenericMessage;
import com.nucleus.cfi.message.vo.GenericMessageResponse;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessage;
import com.nucleus.cfi.whatsApp.pojo.WhatsAppMessageSendResponse;

import java.io.IOException;

public interface WhatsappIntegrationService {

    public WhatsAppMessageSendResponse sendWhatsAppMessage(WhatsAppMessage whatsAppMessage);
}
