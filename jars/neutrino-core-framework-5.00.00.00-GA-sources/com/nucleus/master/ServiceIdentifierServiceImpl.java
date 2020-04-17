package com.nucleus.master;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.dynamicform.entities.ServicePlaceholderMapping;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.ServiceIdentifierDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.ws.core.entities.ServiceFieldType;
import com.nucleus.ws.core.entities.ServiceIdentifier;


/**
 * @author amit.parashar
 * 
 */
@Named("serviceIdentifierService")
public class ServiceIdentifierServiceImpl extends BaseServiceImpl implements ServiceIdentifierService {

    @Inject
    @Named("serviceIdentifierDao")
    private ServiceIdentifierDao serviceIdentifierDao;
    
    @Override
    public List<ServiceIdentifier> getUnmappedServiceIdentifiersListForFields() {
    	List<Integer> statusList = new ArrayList<Integer>();
        return serviceIdentifierDao.getUnmappedServiceIdentifiersListDaoForFields(ServiceIdentifier.class, statusList);
    }
    
    @Override
    public List<ServiceIdentifier> getUnmappedServiceIdentifiersListForPlaceholders() {
    	List<Integer> statusList = new ArrayList<Integer>();
        return serviceIdentifierDao.getUnmappedServiceIdentifiersListDaoForPlaceholders(ServiceIdentifier.class, statusList);
    }

	@Override
	public List<ServiceIdentifier> getServiceIdentifiersForSend() {
		List<ServiceIdentifier> listOfServices = getUnmappedServiceIdentifiersListForFields();
		return includeServiceIdentifierForType(listOfServices, ServiceFieldType.SEND);
	}
	
	@Override
	public List<ServiceIdentifier> getServiceIdentifiersForReceive() {
		List<ServiceIdentifier> listOfServices = getUnmappedServiceIdentifiersListForFields();
		return includeServiceIdentifierForType(listOfServices, ServiceFieldType.RECEIVE);
	}
	
	private List<ServiceIdentifier> includeServiceIdentifierForType(List<ServiceIdentifier> listOfServices, String fieldType) {
		List<ServiceIdentifier> listOfServicesIdentifier = new ArrayList<>();
		if (ValidatorUtils.hasElements(listOfServices)) {
			for (ServiceIdentifier serviceIdentifier : listOfServices) {
				if (serviceIdentifier.getServiceFieldType() == null
						|| ServiceFieldType.SEND_RECEIVE.equalsIgnoreCase(serviceIdentifier.getServiceFieldType().getCode())
						|| fieldType.equalsIgnoreCase(serviceIdentifier.getServiceFieldType().getCode()))				
				{
					listOfServicesIdentifier.add(serviceIdentifier);
				}
			}
		}
		return listOfServicesIdentifier;
	}

	@Override
	public ServiceIdentifier getServiceIdentifierByCode(String code) {
		return serviceIdentifierDao.getServiceIdentifierByCode(code);
	}
    
    
}
