package com.nucleus.passwordpolicy;

import com.nucleus.passwordpolicy.passwordvalidations.*;

public enum PasswordCreationConfigurationFactory {

    //Password should not contain consecutive $$ Alphabets.Eg-ABC,EFG,XYZ.
    NON_CONSECUTIVE_ALPHABETS(new ConsecutiveNAlphabets()),

    //Password should not contain consecutive $$ Numbers.Eg-123.
    NON_CONSECUTIVE_NUMBERS(new ConsecutiveNNumbers()),

    //Password should contain minimum $$ Lower case character.
    MINIMUM_N_LOWER_CASE_CHARACTERS(new MinimumNLowerCaseCharacters()),

    //Password should be of maximum length $$.
    MAXIMUM_LENGTH(new MaximumLength()),

    //Password should be of minimum length $$ .
    MINIMUM_LENGTH(new MinimumLength()),

    //Password should contain minimum $$ Special characters.
    MINIMUM_N_SPECIAL_CHARACTERS(new MinimumNSpecialCharacters()),

    //Password should contain minimum $$ Upper case character.
    MINIMUM_N_UPPER_CASE_CHARACTERS(new MinimumNUpperCaseCharacters()),

    //Password should contain minimum $$ numbers.
    MINIMUM_N_NUMBERS(new MinimumNNumbers()),

    //Password should not similar to user Id.
    NOT_SIMILAR_TO_USER_ID(new SimilarToUserId()),

    //Password should not contain $$ same number in series.Eg-111,222
    N_SAME_NUMBER_IN_SERIES(new NNumbersInSeries()),

    //Password should not contain $$ same alphabet in series.Eg-AAA,BBB
    N_SAME_ALPHABET_IN_SERIES(new NAlphabetsInSeries()),

    CONTAINS_DICTIONARY_WORDS(new ContainsDictionaryWords()),

    DEFAULT ("DEFAULT");

    private AbstractPasswordValidation abstractPasswordValidation;
    private String name;

    private PasswordCreationConfigurationFactory(AbstractPasswordValidation abstractPasswordValidation){
        this.abstractPasswordValidation = abstractPasswordValidation;

    }

    private PasswordCreationConfigurationFactory(String name){
        this.name=name;
    }

    public static AbstractPasswordValidation getPasswordValidationInstance(String key){
        for(PasswordCreationConfigurationFactory passwordCreationConfigurationFactory : PasswordCreationConfigurationFactory.values()){
            if(passwordCreationConfigurationFactory.name().equals(key)){
                return passwordCreationConfigurationFactory.getAbstractPasswordValidation();
            }
        }
        return DEFAULT.getAbstractPasswordValidation();
    }

    public AbstractPasswordValidation getAbstractPasswordValidation(){
        return abstractPasswordValidation;
    }


}
