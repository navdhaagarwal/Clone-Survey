package com.nucleus.address;

import java.util.List;

public interface MasterGeographicService {

    List<State> getAllStatesInCountry(Long countryId);

    List<City> getAllCitiesInState(Long stateId);

    List<ZipCode> getAllZipCodesInAState(Long stateId);

    List<Area> getAllAreaFromCityorZipCode(Long cityId, Long zipCodeId);

    List<Country> getAllCountriesInReagion(Long regionId);

    List<IntraCountryRegion> getAllIntraCountryRegionsOfCountryById(Long countryId);
}
