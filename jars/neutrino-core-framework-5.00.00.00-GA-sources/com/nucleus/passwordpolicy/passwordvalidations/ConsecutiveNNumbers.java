package com.nucleus.passwordpolicy.passwordvalidations;


import java.util.List;

public class ConsecutiveNNumbers extends AbstractPasswordValidation {



    public String validate(String password, String username, String configValue, String configErrorMsg, String validationError, List dictWords, List specialChars) {
        String error="";

        if(configValue==null){
            return configErrorMsg;
        }
        else{
            int count = 1;
            int i=0;
            int j=1;
            while(j<password.length()){
                if(!Character.isDigit(password.charAt(i))){
                    i++;
                    j=i+1;
                    count=1;
                }else {
                    int ascii_i = password.charAt(i);
                    int ascii_j = password.charAt(j);
                    if (ascii_j == ascii_i + count) {
                        count++;
                        j++;
                    } else {
                        i++;
                        j = i + 1;
                        count = 1;
                    }
                    if (count == Integer.valueOf(configValue)) {
                        error += validationError;
                        break;
                    }
                }
            }
            return error;
        }
    }
}
