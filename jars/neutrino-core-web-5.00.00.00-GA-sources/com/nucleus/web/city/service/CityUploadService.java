package com.nucleus.web.city.service;


import com.nucleus.web.city.vo.CityVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

@Service
@Named("cityUploadService")
public class CityUploadService implements ICityUploadService {

    @Inject
    private ICityUploadBusinessObj cityUploadBusinessObj;

    @Override
    @Transactional
    public CityVO uploadCity(CityVO cityVO) {

        return cityUploadBusinessObj.uploadCity(cityVO);
    }
}
