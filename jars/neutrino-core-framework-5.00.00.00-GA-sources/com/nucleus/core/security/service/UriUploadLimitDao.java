package com.nucleus.core.security.service;

import com.nucleus.core.security.entities.UriUploadLimitConfiguration;
import com.nucleus.persistence.EntityDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Created by gajendra.jatav on 9/15/2019.
 */
@Named("uriUploadLimitDao")
public class UriUploadLimitDao {

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    public List<UriUploadLimitConfiguration> getAllUploadLimitConfiguration(){
        return entityDao.findAll(UriUploadLimitConfiguration.class);
    }
}
