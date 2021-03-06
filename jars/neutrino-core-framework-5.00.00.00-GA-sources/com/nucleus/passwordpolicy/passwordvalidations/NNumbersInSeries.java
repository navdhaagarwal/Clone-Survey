package com.nucleus.passwordpolicy.passwordvalidations;


import java.util.List;

public class NNumbersInSeries extends AbstractPasswordValidation {



    public String validate(String password, String username, String configValue, String configErrorMsg, String validationError, List dictWords, List specialChars) {
        String error="";

        if(configValue==null){
            return configErrorMsg;
        }
        else{
            char prevChar = password.charAt(0);
            int count = 1;
            for(int i=1;i<password.length();i++){
                if(Character.isDigit(prevChar) && prevChar == password.charAt(i))
                    count++;
                else {
                    count = 1;
                    prevChar = password.charAt(i);
                }
                if(count==Integer.valueOf(configValue)){
                    error+= validationError;
                    break;
                }
            }
            return error;
        }
    }
}
