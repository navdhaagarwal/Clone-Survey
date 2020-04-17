package com.nucleus.core.ipaddress.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.ipaddress.vo.IpAddressVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Named("ipAddressUploadService")
public class IpAddressUploadService implements IIpAddressUploadService{

    @Inject
    private IIpAddressUploadBusinessObj  ipAddressUploadBusinessObj;

    @Override
    @Transactional
    public IpAddressVO uploadIpAddress(IpAddressVO ipAddressVO) {
        return ipAddressUploadBusinessObj.uploadIpAddress(ipAddressVO);
    }
}