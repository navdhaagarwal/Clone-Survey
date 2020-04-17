package com.nucleus.web.district.service;


import com.nucleus.web.district.vo.DistrictVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

@Service
@Named("districtUploadService")
public class DistrictUploadService implements IDistrictUploadService {

    @Inject
    private IDistrictUploadBusinessObj districtUploadBusinessObj;

    @Override
    @Transactional
    public DistrictVO uploadDistrict(DistrictVO districtVO) {

        return districtUploadBusinessObj.uploadDistrict(districtVO);
    }
}
