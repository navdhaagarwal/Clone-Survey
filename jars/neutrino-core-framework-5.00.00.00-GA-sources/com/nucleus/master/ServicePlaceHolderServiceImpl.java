package com.nucleus.master;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.entities.ServicePlaceholderMapping;
import com.nucleus.persistence.ServicePlaceHolderdao;
import com.nucleus.service.BaseServiceImpl;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * @author amit.parashar
 * 
 */
@Named("servicePlaceHolderService")
public class ServicePlaceHolderServiceImpl extends BaseServiceImpl implements ServicePlaceHolderService {

    @Inject
    @Named("servicePlaceHolderdao")
    private ServicePlaceHolderdao         servicePlaceHolderdao;
    
    @Override
    public List<ServicePlaceholderMapping> getServicePlaceHolderByServiceIdentifierID(Long id) {
    	List<Integer> statusList = new ArrayList<Integer>();
        /*statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);*/
        return servicePlaceHolderdao.getServicePlaceholderMappingListFromServiceIdentifier(id);
    }
}
