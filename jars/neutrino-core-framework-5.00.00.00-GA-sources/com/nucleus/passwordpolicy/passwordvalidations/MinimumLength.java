package com.nucleus.passwordpolicy.passwordvalidations;


import java.util.List;

public class MinimumLength extends AbstractPasswordValidation {



    public String validate(String password, String username, String configValue, String configErrorMsg, String validationError, List dictWords, List specialChars) {
        String error="";

        if(configValue==null){
            return configErrorMsg;
        }
        else{
            if(password.length()<Integer.valueOf(configValue))
                error += validationError;
            return error;
        }
    }

}
