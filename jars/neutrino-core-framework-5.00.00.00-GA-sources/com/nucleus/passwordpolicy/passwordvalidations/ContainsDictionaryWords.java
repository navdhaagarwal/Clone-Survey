package com.nucleus.passwordpolicy.passwordvalidations;


import java.util.ArrayList;
import java.util.List;

public class ContainsDictionaryWords extends AbstractPasswordValidation{


    @Override
    public String validate(String password, String username, String configValue, String configErrorMsg, String validationError,List dictWords, List specialChars) {
        String err = "";
        if(configValue==null){
            return configErrorMsg;
        }
        password = password.toLowerCase();
        for(Object str : dictWords){
            if(password.contains(str.toString())) {
                err+= validationError;
                break;
            }
        }
        return err;
    }
}
