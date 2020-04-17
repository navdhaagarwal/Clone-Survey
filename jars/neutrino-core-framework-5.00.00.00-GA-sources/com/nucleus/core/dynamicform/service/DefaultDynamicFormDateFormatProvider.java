package com.nucleus.core.dynamicform.service;

import com.nucleus.service.BaseServiceImpl;
import org.springframework.stereotype.Service;

import javax.inject.Named;

/**
 * Created by gajendra.jatav on 11/22/2019.
 */
@Named("dynamicFormDateFormatProvider")
public class DefaultDynamicFormDateFormatProvider extends BaseServiceImpl implements DynamicFormDateFormatProvider{


    @Override
    public String getDateFormat() {
        return getSystemDateFormat();
    }
}
