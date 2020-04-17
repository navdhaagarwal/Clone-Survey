package com.nucleus.notificationMaster.service;

import java.util.Map;

import com.nucleus.notificationMaster.NotificationMaster;
import com.nucleus.service.BaseService;

import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

public interface AddOnDataProviderForNotificationGeneration extends BaseService {

    /**
     * Method to add additional data for notification generation
     * @param notificationMaster
     * @param contextmap
     * @param metadata
     */
    public void provideDataForCommunicationGeneration(NotificationMaster notificationMaster, Map contextMap,
            FieldsMetadata metadata);
    
    public void reInitializeLoanApplicationData(Map contextMap);

}
