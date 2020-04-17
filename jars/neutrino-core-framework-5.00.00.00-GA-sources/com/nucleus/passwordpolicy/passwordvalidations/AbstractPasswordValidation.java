package com.nucleus.passwordpolicy.passwordvalidations;


import java.util.List;

public abstract class AbstractPasswordValidation{

    public abstract String validate(String password, String username, String configValue, String configErrorMsg, String validationError, List dictWords, List specialChars);

}
