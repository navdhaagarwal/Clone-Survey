package com.nucleus.finnone.pro.communicationgenerator.serviceinterface;

import java.util.List;

import com.nucleus.core.event.EventCode;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.rules.model.SourceProduct;

public interface ICommunicationSchedulerService {

List<EventCode> getUnMappedEventCodesBasedOnModule(SourceProduct sourceProduct);

List<EventCode> getEventCodeListFromIds(Long[] eventCodeIds);

List<CommunicationName> getUnMappedCommunicationsBasedOnModule(SourceProduct sourceProduct);

List<CommunicationName> getCommunicationListFromIds(Long[] communicationIds);

String fetchNumberOfDuplicateSchedulers(String schedulerName,boolean eventRequest,SourceProduct sourceProduct,Long id,String uuid);

}
