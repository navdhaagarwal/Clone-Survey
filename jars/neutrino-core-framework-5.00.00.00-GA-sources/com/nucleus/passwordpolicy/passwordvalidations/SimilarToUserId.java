package com.nucleus.passwordpolicy.passwordvalidations;


import java.util.List;

public class SimilarToUserId extends AbstractPasswordValidation    {


    public String validate(String password, String username, String configValue, String configErrorMsg, String validationError, List dictWords, List specialChars) {

        if(username==null || username.isEmpty())
            return "User name is empty";
        else
            if(password.toLowerCase().contains(username.toLowerCase()))
                return validationError;

        return "";


    }
}
