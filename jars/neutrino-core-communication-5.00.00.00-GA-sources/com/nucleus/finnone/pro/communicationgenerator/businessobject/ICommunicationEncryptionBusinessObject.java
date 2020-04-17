package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGroupCriteriaVO;

public interface ICommunicationEncryptionBusinessObject {

    void encryptAttachments(CommunicationName communication,
            CommunicationTemplate communicationTemplate,
            Map<String, Object> dataMap,
            CommunicationGroupCriteriaVO communicationGroupCriteriaVO);

    String maskCommunicationParameter(String value, String formatMask);

}
