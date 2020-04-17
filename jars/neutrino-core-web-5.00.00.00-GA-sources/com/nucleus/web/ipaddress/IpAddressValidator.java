package com.nucleus.web.ipaddress;

import com.nucleus.core.misc.util.IpAddressUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.user.ipaddress.IpAddress;
import com.nucleus.web.common.controller.CASValidationUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpAddressValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        // TODO Auto-generated method stub
        return IpAddress.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "ipAddress", "label.required.ipAddress");

         IpAddress ipAddress = (IpAddress) target;

         if(!validIP(ipAddress.getIpAddress())){
            errors.rejectValue("ipAddress","label.invalid.ip.address");
        }


    }

    private boolean validIP(String ipAddress){

        String regex = "^(([0-9]|[*]|[1-9][0-9]|[*]|1[0-9]{2}|[*]|2[0-4][0-9]|[*]|25[0-5])\\.){3}([0-9]|[*]|[1-9][0-9]|[*]|1[0-9]{2}|[*]|2[0-4][0-9]|[*]|25[0-5])$";
        return ipAddress.matches(regex);


    }
}
