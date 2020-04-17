package com.nucleus.web.street.service;


import com.nucleus.web.street.vo.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import javax.inject.*;

@Service
@Named("streetUploadService")
public class StreetUploadService implements IStreetUploadService{

    @Inject
    @Named("streetUploadBusinessObj")
    private IStreetUploadBusinessObj streetUploadBusinessObj;

    @Override
    @Transactional
    public StreetVO uploadStreet(StreetVO streetVO) {

        return streetUploadBusinessObj.uploadStreet(streetVO);
    }


}
