package com.nucleus.address;

import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.grid.IGridService;
import com.nucleus.service.BaseService;
import com.nucleus.tehsil.entity.Tehsil;
import com.nucleus.user.User;

public interface AddressTagService extends BaseService, IGridService {

    /**
     * Find Country ZipCode By CountryIsoCode
     * 
     * @param countryIsoCode
     */
    public List<PostalCode> findCountryZipCodeByCountryIsoCode(String countryIsoCode);

    /**

    * Find Country  By ZipCode
    * 
    * @param postalCode
    */
    public Country findCountryByZipCode(String zipCode);

    /**

     * Find City  By ZipCode
     * 
     * @param zipCode
     */
    public Country findCountryByCityCode(String cityCode);

    /**

     * Find State  By stateCode
     * 
     * @param stateCode
     */
    public Country findCountryByStateCode(String stateCode);

    /**
     * Find Area by ZipCode and By CountryIsoCode
     * 
     * @param countryIsoCode
     *            ,postalCode
     */
    public List<PostalCode> findAreaByPostalCode(String countryIsoCode, String postalCode);

    /**
     * Find Rest By ZipCode,CountryIsoCode and Area
     * 
     * @param countryIsoCode
     *            ,placeName,postalCode
     */
    public List<PostalCode> findOthers(String countryIsoCode, String placeName, String postalCode);

    /**
     * Find Country Ids from master database matching the name with Geographic
     * database
     * 
     * @param countryIsoCode
     */
    public List<Country> findCountryId(String countryIsoCode);

    /**
     * Find State Ids from master database matching the name with Geographic
     * database
     * 
     * @param State
     *            Name stataName
     */
    public List<State> findStateId(String stateName);

    /**
     * Find City Ids from master database matching the name with Geographic
     * database
     * 
     * @param City
     *            Name cityName
     */
    public List<City> findCityId(String cityName);

    /**
     * Find Area Ids from master database matching the name with Geographic
     * database
     * 
     * @param Area
     *            Name areaName
     */
    public List<Area> findAreaId(String areaName);

    /**
     * Find ZipCode Ids from master database matching the name with Geographic
     * database
     * 
     * @param ZipCode
     *            Name zipcode
     */
    public List<ZipCode> findZipcodeId(String zipcode);

    /**
     * Find address with specified id
     * 
     * @param addressId
     *            Name id
     */
    public Address findAddressById(Long id);

    /**
     * 
     * Return Address of the given customer present contactInfo 
     * @param id Customer's Id
     * @param user
     * @return
     */
    public List<Address> loadAddressForCustomer(Long id, User user);

    /**
     * 
     * Return List of Address of the business partner
     * @param id
     * @param user
     * @param bpType
     * @return
     */
//    public List<Address> loadAddressForBusinessPartner(Long id, User user, String bpType);

    /**
     * 
     * Return List of Address of the Builder Project
     * @param id
     * @param user
     * @return
     */
    public List<Address> loadAddressForBuilderProject(Long id, User user);
    
    /**
     * 
     * Return List of AddressAED of the AdditionalEmployerDetails
     * @param id
     * @param user
     * @return
     */

    public List<Address> loadAddressForCompanyDetails(Long id,User user);

    /**
     * Returns List of Address of the Company Details
     * @param id
     * @param user
     * @return
     */



    public List<Address> loadAddressForAdditionalEmployerDetails(Long id, User user);
    

    /**
     * 
     * Return list of approved district based on District Name
     * @param districtName
     * @return
     */
    public List<District> findDistrictId(String districtName);

    /**
     * 
     * Return list of approved IntraCountryRegions based on Region Name
     * @param regionName
     * @return
     */
    public List<IntraCountryRegion> findRegionId(String regionName);

    /**
     * 
     * Return list of approved IntraCountryRegions based on Country Id
     * @param countryId
     * @return
     */
    public List<IntraCountryRegion> findIntraCountryRegionByCountryId(Long countryId);

    /**
     * 
     * Return list of approved states inside InfraCountryRegion with it's id
     * @param regionId InfraCountryRegion's id
     * @return
     */

    public List<State> findAllStateInIntraCountryRegion(Long regionId);

    /**
     * 
     * Return list of approved Cities inside State with it's id
     * @param stateId State's Id
     * @return
     */

    public List<City> findAllCityInState(Long stateId);

    /**
     * 
     * Return list of approved District inside State with it's id
     * @param stateId States'id
     * @return
     */

    public List<District> findAllDistrictInState(Long stateId);

    /**
     * 
     * Return list of approved Areas inside City with it's id
     * @param cityId City's Id
     * @return
     */

    public List<Area> findAllAreaInCity(Long cityId);

    /**
     * 
     * Return list of approved Areas with Zipcode 
     * @param zipCodeId ZipCode's Id
     * @return
     */

    public List<Area> findAllAreaInZipCode(Long zipCodeId);

    /**
     * 
     * Return list of approved ZipCode in a City 
     * @param cityId City's Id
     * @return
     */

    public List<ZipCode> findAllZipCodeInCity(Long cityId);

    /**
     * 
     * Return list of approved Zipcode in a state
     * @param stateId State's Id
     * @return
     */

    public List<ZipCode> findAllZipCodeInState(Long stateId);

    /**
     * 
     * Return list of approved Zipcode in a country
     * @param countryId Country's Id
     * @return
     */

    public List<ZipCode> findAllZipCodeInCountry(Long countryId);

    /**
     * 
     * Return list of approved states in a country
     * @param countryId country's id
     * @return
     */

    public List<State> findAllStateInCountry(Long countryId);

    /**
     * 
     * Return list of approved city in a country
     * @param countryId country's Id
     * @return
     */

    public List<City> findAllCityInCountry(Long countryId);

    /**
     * 
     * return string containing Latitude and Longitude of the ZipCode present in PostalCode     * 
     * @param zipCode
     * @return
     */

    public List<String> findLatLongByZipCode(String zipCode);

    /**
     * Load address for builder group.
     *
     * @param builderGroupId the builder group id
     * @param userEntityId the user entity id
     * @return the list
     */
    public List<Address> loadAddressForBuilderGroup(Long builderGroupId, User user);

    /**
     * Load address for builder company.
     *
     * @param builderCompanyId the builder company id
     * @param userEntityId the user entity id
     * @return the list
     */
    public List<Address> loadAddressForBuilderCompany(Long builderCompanyId, User user);

    public List<District> findAllDistrictByCountryId(Long countryId);

    public List<Area> findAllAreaInCountry(Long countryId);
    
    Long findVillageIdByName(String name);

    Long findTempVillageIdByName(String name);
    
    void saveTempVillage(TempVillage tempVillage);
	
	Long findZipCodeIdByZipCode(Long zipCode);

    Long findZipCodeIdByZipCode(String zipCode);
    
    public Long findCountryIdByCityId(Long zipCodeId);
    
    public Long findStateIdByCityId(Long zipCodeId);
    
    public Map<String, Object> getCountryDetailsByCountryId(Long countryId);

    public String getCountryNameByCountryId(String countryId);
    
    public Address getAddressDetailsByAddressId(Long addressId);
    //used for featcing the Customer address from maker area for PDE Address Update    
    public AddressTemp findTempCustomerAddressById(Long id);
    //used for featcing the Customer address from maker area for PDE Address Update using GCD Record id
    public AddressTemp findTempCustomerAddressByGcdRecordId(String recordId);

    public Long findZipcodeInCity(String zipcode, Long cityId);
    
    public Map<String,String> validateAddressBeforeSave(Address address) throws XPathExpressionException;

    List<Map<String, ?>> searchZipCodesForAutoComplete(String className, String itemVal, String[] searchColumnList,
        String value,boolean loadApprovedEntityFlag, String listOfItems,boolean strictSearchOnListOfItems, int page,String countryId,String stateId,String cityId);
    List<String> findCountryCodeByISDCode(String isdCode);

    List<State> findAllApprovedStatesInCountry(Long countryId);

    List<District> findAllApprovedDistrictsInState(Long stateId);

    List<City> findAllApprovedCitiesInState(Long stateId);

    public VillageMaster findVillageMasterByName(String name);

    public Tehsil findTehsilByName(String name);

    public Long findTempTehsilByName(String name);

    public void saveTempTehsil(TempTehsil tempTehsil);

    public List<Long> getVillageIdsByDistrictIdAndTehsilName(Long districtId,String tehsilName);

    public List<Long> getVillageIdsByDistrictId(Long districtId);

    public List<Long> getTehsilIdsByDistrictId(Long districtId);

    public List<Long> getVillageIdsByDistrictIdAndTehsilId(Long districtId, Long tehsilId);

    public void handleVillageAndTehsilMaster(Address address);

    public void handleVillageAndTehsilMaster(AddressTemp address);
    
    public State getStateAttributes(Long id);
    
    public int validateCustomPincodeValue(String customPincodeValue, State stateObj);

    public List<City> findAllApprovedCitiesInState(List<Long> stateId);

    public List<VillageMaster> findAllApprovedVillagesInState(List<Long> stateId);

    public default Address getAddressFromAddressDataVo(AddressDataVo addressDataVo){
        return null;
    };

    public default Address prepareAddressToSave( Address address){
        return null;

    }
    public List<Street> findAllStreetInCity(Long cityId);

    public List<Address> addConsolidatedAddressInViewProperties(List<Address> addresseList);

    public default void initializeAddressLazyProperties(Address address){
        return;
    }

    public default Address returnAddressInUpperCase(Address address,String profileName){
        return null;
    }

}
