package com.nucleus.passwordpolicy.passwordvalidations;


import java.util.List;

public class MinimumNSpecialCharacters extends AbstractPasswordValidation {



    public String validate(String password, String username, String configValue, String configErrorMsg, String validationError, List dictWords, List specialChars) {

        String error="";
        if(configValue==null){
            return configErrorMsg;
        }
        else{

            int count =0;

            for(int i=0;i<password.length();i++){
                if(specialChars.contains(Character.toString(password.charAt(i))))
                    count++;

            }
            if(count<Integer.valueOf(configValue))
                error += validationError;
            return error;
        }
    }
}
