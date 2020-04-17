package com.nucleus.core.rules.parameter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;


@Service
@Named("parameterUploadService")
public class ParameterUploadService implements IParameterUploadService{

    @Inject
    private IParameterUploadBusinessObj parameterUploadBusinessObj;

    @Override
    @Transactional
    public ParameterVO uploadParameter(ParameterVO parameterVO) {
        return parameterUploadBusinessObj.uploadParameter(parameterVO);
    }
}
