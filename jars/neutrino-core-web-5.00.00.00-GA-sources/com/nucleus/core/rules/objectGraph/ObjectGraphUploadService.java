package com.nucleus.core.rules.objectGraph;

import com.nucleus.core.ipaddress.service.IIpAddressUploadBusinessObj;
import com.nucleus.core.ipaddress.vo.IpAddressVO;
import com.nucleus.rules.model.ObjectGraphTypes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 
 */
@Service
@Named("objectGraphUploadService")
public class ObjectGraphUploadService implements IObjectGraphUploadService{

    @Inject
    private IObjectGraphUploadBusinessObj objectGraphUploadBusinessObj;

    @Override
    @Transactional
    public ObjectGraphTypes uploadObjectGraph(ObjectGraphTypes objectGraphTypes) {
        return objectGraphUploadBusinessObj.uploadObjectGraph(objectGraphTypes);
    }
}
