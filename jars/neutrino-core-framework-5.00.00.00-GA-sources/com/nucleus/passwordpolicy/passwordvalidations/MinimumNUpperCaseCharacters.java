package com.nucleus.passwordpolicy.passwordvalidations;

import java.util.List;

public class MinimumNUpperCaseCharacters extends AbstractPasswordValidation {



    public String validate(String password, String errorMsg, String configValue, String configErrorMsg, String validationError, List dictWords, List specialChars) {
        String error="";

        if(configValue==null){
            return configErrorMsg;
        }
        else{
            int count =0;
            for(int i=0;i<password.length();i++){
                if(Character.isUpperCase(password.charAt(i)))
                    count++;
            }
            if(count<Integer.valueOf(configValue))
                error += validationError;
            return error;
        }
    }
}
