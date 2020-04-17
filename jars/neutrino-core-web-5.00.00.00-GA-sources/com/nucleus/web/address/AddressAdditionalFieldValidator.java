package com.nucleus.web.address;

public interface AddressAdditionalFieldValidator {

    boolean validateAdditionalDropDownField1(String countryIsoCode, String addressTypeCode, String filterCode, String additionalFieldCode);

    boolean validateAdditionalDropDownField2(String countryIsoCode, String addressTypeCode, String filterCode, String additionalFieldCode);
}
