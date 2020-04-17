/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */

package com.nucleus.address;

import static com.nucleus.finnone.pro.base.utility.CoreUtility.getUserDetails;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.lang.reflect.*;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.xpath.XPathExpressionException;

import com.nucleus.config.persisted.service.*;
import com.nucleus.config.persisted.vo.*;
import com.nucleus.contact.PhoneNumber;
import com.nucleus.contact.PhoneNumberType;
import com.nucleus.core.additionalEmployerDetails.entity.AddressTypeAED;
import com.nucleus.core.additionalEmployerDetails.entity.OwnershipStatus;
import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.entity.*;
import com.nucleus.internetchannel.AccomodationType;
import com.nucleus.internetchannel.ResidenceType;
import com.nucleus.persistence.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.autocomplete.AutocompleteLoadedEntitiesMap;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.core.villagemaster.entity.VillageMaster;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.GridVO;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.query.constants.QueryHint;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.tehsil.entity.Tehsil;
import com.nucleus.user.User;
import org.apache.commons.lang3.StringUtils;
import com.nucleus.query.constants.QueryHint;
import net.bull.javamelody.MonitoredWithSpring;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

@Named(value = "addressService")
@MonitoredWithSpring(name = "Address_Service_IMPL_")
public class AddressTagServiceImpl extends BaseServiceImpl implements AddressTagService {

    public static final String COUNTRY_ISO_CODE = "countryIsoCode";
    public static final String ZIP_CODE = "zipCode";
    public static final String APPROVAL_STATUS = "approvalStatus";
    public static final String PARENT_ID = "parentId";
    public static final String THE_USER_ID = "The user id :";
    public static final String REQUESTING_ALL_ENTITIES_IT_DOES_NOT_MATCH_WITH_LOGGED_IN_USER_S_ID = " requesting all entities. It does not match with logged in user's ID :";
    public static final String COUNTRY_ID = "countryId";
    public static final String STATE_ID = "stateId";
    public static final String DISTRICT_ID = "districtId";
    private static final String CONSOLIDATED_ADDRESS_SEPARATOR = " ";
    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;
    
    @Inject
    @Named("addressTagUtil")
    protected AddressTagUtil addressTagUtil;

    @Inject
    @Named("genericParameterService")
    private GenericParameterServiceImpl genericParameterService;
    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    
    private final int DEFAULT_PAGE_SIZE = 3;
    private final String NULL_STRING="null";
    
    @Override
    public List<PostalCode> findCountryZipCodeByCountryIsoCode(String countryIsoCode) {
        NeutrinoValidator.notNull(countryIsoCode);
        NamedQueryExecutor<PostalCode> postalCode = new NamedQueryExecutor<PostalCode>(
                "address.findCountryZipCodeByCountryIsoCode").addParameter(COUNTRY_ISO_CODE, countryIsoCode).addQueryHint(
                QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<PostalCode> postalCodeList = entityDao.executeQuery(postalCode);
        if (postalCodeList.size() > 0)
            return postalCodeList;
        else
            return null;

    }

    @Override
    public Country findCountryByZipCode(String zipCode) {
        NeutrinoValidator.notNull(zipCode);
        NamedQueryExecutor<ZipCode> zipCodeQuery = new NamedQueryExecutor<ZipCode>("address.findCountryByZipCode")
                .addParameter(ZIP_CODE, zipCode);

        List<ZipCode> zipCodeList = entityDao.executeQuery(zipCodeQuery);
        if (zipCodeList.size() > 0)
            return zipCodeList.get(0).getCountry();
        else
            return null;

    }

    @Deprecated
    @Override
    public Long findZipCodeIdByZipCode(String zipCode) {
        NeutrinoValidator.notNull(zipCode);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Long> zipCodeQuery = new NamedQueryExecutor<Long>("address.findZipCodeIdByZipCode")
                .addParameter("zipcode", zipCode).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        List<Long> zipCodeList = entityDao.executeQuery(zipCodeQuery);
        if (zipCodeList.size() > 0)
            return zipCodeList.get(0);
        else
            return null;

    }
	
	@Override
    public Long findZipCodeIdByZipCode(Long zipCode) {
        NeutrinoValidator.notNull(zipCode);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Long> zipCodeQuery = new NamedQueryExecutor<Long>("address.findZipCodeIdByZipCode")
                .addParameter("zipcode", String.valueOf(zipCode)).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);

        List<Long> zipCodeList = entityDao.executeQuery(zipCodeQuery);
        if (zipCodeList.size() > 0)
            return zipCodeList.get(0);
        else
            return null;

    }

    @Override
    public Country findCountryByCityCode(String cityCode) {
        NeutrinoValidator.notNull(cityCode);
        NamedQueryExecutor<City> city = new NamedQueryExecutor<City>("address.findCountryByCityCode").addParameter(
                "cityCode", cityCode);

        List<City> cityList = entityDao.executeQuery(city);
        if (cityList.size() > 0)
            return cityList.get(0).getCountry();
        else
            return null;

    }

    @Override
    public Country findCountryByStateCode(String stateCode) {
        NeutrinoValidator.notNull(stateCode);
        NamedQueryExecutor<State> state = new NamedQueryExecutor<State>("address.findCountryByStateCode").addParameter(
                "stateCode", stateCode);

        List<State> stateList = entityDao.executeQuery(state);
        if (stateList.size() > 0)
            return stateList.get(0).getCountry();
        else
            return null;

    }

    @Override
    public List<PostalCode> findAreaByPostalCode(String countryIsoCode, String postalCode) {
        NeutrinoValidator.notNull(countryIsoCode);
        NeutrinoValidator.notNull(postalCode);
        NamedQueryExecutor<PostalCode> area = new NamedQueryExecutor<PostalCode>("address.findAreaByPostalCode")
                .addParameter(COUNTRY_ISO_CODE, countryIsoCode).addParameter("postalCode", postalCode);
        List<PostalCode> areaList = entityDao.executeQuery(area);
        if (areaList.size() > 0)
            return areaList;
        else
            return new ArrayList<>();

    }

    @Override
    public List<PostalCode> findOthers(String countryIsoCode, String placeName, String postalCode) {
        NeutrinoValidator.notNull(countryIsoCode);
        NeutrinoValidator.notNull(placeName);
        NeutrinoValidator.notNull(postalCode);
        NamedQueryExecutor<PostalCode> remainingList = new NamedQueryExecutor<PostalCode>("address.findRemainingFields")
                .addParameter(COUNTRY_ISO_CODE, countryIsoCode).addParameter("placeName", placeName)
                .addParameter("postalCode", postalCode);
        List<PostalCode> remainingCompleteList = entityDao.executeQuery(remainingList);
        if (remainingCompleteList.size() > 0)
            return remainingCompleteList;
        else
            return new ArrayList<>();
    }

    @Override
    public List<Country> findCountryId(String countryIsoCode) {
        NeutrinoValidator.notNull(countryIsoCode);
        NamedQueryExecutor<Country> countryId = new NamedQueryExecutor<Country>("address.findCountryId").addParameter(
                COUNTRY_ISO_CODE, countryIsoCode).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Country> countryIdList = entityDao.executeQuery(countryId);
        return countryIdList;
    }

    @Override
    public List<State> findStateId(String stateName) {
        NeutrinoValidator.notNull(stateName);
        NamedQueryExecutor<State> stateId = new NamedQueryExecutor<State>("address.findStateId").addParameter("state",
                stateName);
        List<State> stateIdList = entityDao.executeQuery(stateId);
        return stateIdList;
    }

    @Override
    public List<City> findCityId(String cityName) {
        NeutrinoValidator.notNull(cityName);
        NamedQueryExecutor<City> cityId = new NamedQueryExecutor<City>("address.findCityId").addParameter("city", cityName);
        List<City> cityIdList = entityDao.executeQuery(cityId);
        return cityIdList;
    }

    @Override
    public List<Area> findAreaId(String areaName) {
        NeutrinoValidator.notNull(areaName);
        NamedQueryExecutor<Area> areaId = new NamedQueryExecutor<Area>("address.findAreaId").addParameter("area", areaName);
        List<Area> areaIdList = entityDao.executeQuery(areaId);
        return areaIdList;
    }

    @Override
    public List<ZipCode> findZipcodeId(String zipcode) {
        NeutrinoValidator.notNull(zipcode);
        NamedQueryExecutor<ZipCode> zipCodeId = new NamedQueryExecutor<ZipCode>("address.findZipCodeId").addParameter(
                "zipcode", zipcode);
        List<ZipCode> zipCodeIdList = entityDao.executeQuery(zipCodeId);
        return zipCodeIdList;
    }
    
@Override
    public Long findZipcodeInCity(String zipcode, Long cityId) {
        NeutrinoValidator.notNull(zipcode);
        NeutrinoValidator.notNull(cityId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<Long> zipCodeInCity = new NamedQueryExecutor<Long>("address.findZipcodeInCity").addParameter(
                "zipcode", zipcode).addParameter("cityId", cityId).addParameter(APPROVAL_STATUS, approvalStatusList);
        
        List<Long> zipcodeIds = entityDao.executeQuery(zipCodeInCity);
        Long zipcodeId= null;
        if(CollectionUtils.isNotEmpty(zipcodeIds)){
        	zipcodeId = zipcodeIds.get(0);
        }
        return zipcodeId;
    }

    @Override
    public Address findAddressById(Long id) {
        NeutrinoValidator.notNull(id);
        return entityDao.find(Address.class, id);
    }

    @Override
    public Map<String, Object> loadPaginatedData(Class entityName, String userUri, Long parentId, Integer iDisplayStart,
            Integer iDisplayLength, String sortColName, String sortDir) {
        NeutrinoValidator.notNull(parentId);
        // TODO data needs to be paginated
        Map<String, Object> addressRecordMap = new HashMap<String, Object>();
        NamedQueryExecutor<Object> address = new NamedQueryExecutor<Object>("address.findAllCustomerAddress").addParameter(
                PARENT_ID, parentId);
        List<Object> addressList = entityDao.executeQuery(address);
        if (addressList.size() > 0) {
            for (int i = 0 ; i < addressList.size() ; i++) {
                addressRecordMap.put("addressRecord" + i, addressList.get(i));
            }

            return addressRecordMap;
        } else {
            return null;
        }

    }

    @Override
    public List<Address> loadAddressForCustomer(Long id, User user) {
        NeutrinoValidator.notNull(id);
        EntityId userEntityId = user.getEntityId();
        if (!(getCurrentUser().getId().equals(userEntityId.getLocalId()))) {
            throw new SystemException(THE_USER_ID + userEntityId.getLocalId()
                    + REQUESTING_ALL_ENTITIES_IT_DOES_NOT_MATCH_WITH_LOGGED_IN_USER_S_ID + getCurrentUser().getId());
        }

        NamedQueryExecutor<Address> address = new NamedQueryExecutor<Address>("address.findAllCustomerAddress")
                .addParameter(PARENT_ID, id);
        return entityDao.executeQuery(address);
    }

    @Override
    public List<Address> loadAddressForBuilderGroup(Long builderGroupId, User user) {
        NeutrinoValidator.notNull(builderGroupId);
        EntityId userEntityId = user.getEntityId();
        if (!(getCurrentUser().getId().equals(userEntityId.getLocalId()))) {
            throw new SystemException(THE_USER_ID + userEntityId.getLocalId()
                    + REQUESTING_ALL_ENTITIES_IT_DOES_NOT_MATCH_WITH_LOGGED_IN_USER_S_ID + getCurrentUser().getId());
        }
        NamedQueryExecutor<Address> address = new NamedQueryExecutor<Address>("address.findAllBuilderGroupAddress")
                .addParameter(PARENT_ID, builderGroupId);
        return entityDao.executeQuery(address);
    }

    @Override
    public List<Address> loadAddressForBuilderCompany(Long builderCompanyId, User user) {
        NeutrinoValidator.notNull(builderCompanyId);
        EntityId userEntityId = user.getEntityId();
        if (!(getCurrentUser().getId().equals(userEntityId.getLocalId()))) {
            throw new SystemException(THE_USER_ID + userEntityId.getLocalId()
                    + REQUESTING_ALL_ENTITIES_IT_DOES_NOT_MATCH_WITH_LOGGED_IN_USER_S_ID + getCurrentUser().getId());
        }
        NamedQueryExecutor<Address> address = new NamedQueryExecutor<Address>("address.findAllBuilderCompanyAddress")
                .addParameter(PARENT_ID, builderCompanyId);
        return entityDao.executeQuery(address);
    }

    public List<Address> loadAddressForCompanyDetails(Long companyDetailsId,User user){
        NeutrinoValidator.notNull(companyDetailsId);
        EntityId userEntityId=user.getEntityId();
        if(!(getCurrentUser().getId().equals(userEntityId.getLocalId()))){
            throw new SystemException(THE_USER_ID + userEntityId.getLocalId()+REQUESTING_ALL_ENTITIES_IT_DOES_NOT_MATCH_WITH_LOGGED_IN_USER_S_ID+ getCurrentUser().getId());
        }
        NamedQueryExecutor<Address> address = new NamedQueryExecutor<Address>("address.findAllCompanyDetailsAddress").addParameter(PARENT_ID,companyDetailsId);
        return entityDao.executeQuery(address);
    }

    

    @Override
    public Map<String, Object> findEntity(Class entityClass, String userUri, Integer iDisplayStart, Integer iDisplayLength,
            Map<String, Object> queryMap) {
        return null;
    }

    @Override
    public List<District> findDistrictId(String districtName) {
        NeutrinoValidator.notNull(districtName);
        NamedQueryExecutor<District> districtId = new NamedQueryExecutor<District>("address.findDistrictId").addParameter(
                "district", districtName);
        List<District> districtIdList = entityDao.executeQuery(districtId);
        return districtIdList;
    }

    @Override
    public List<IntraCountryRegion> findRegionId(String regionName) {
        NeutrinoValidator.notNull(regionName);
        NamedQueryExecutor<IntraCountryRegion> regionId = new NamedQueryExecutor<IntraCountryRegion>("address.findRegionId")
                .addParameter("region", regionName);
        List<IntraCountryRegion> regionIdList = entityDao.executeQuery(regionId);
        return regionIdList;
    }

    @Override
    public List<IntraCountryRegion> findIntraCountryRegionByCountryId(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<IntraCountryRegion> regionId = new NamedQueryExecutor<IntraCountryRegion>(
                "address.getAllIntraCountryRegionsFromCountry").addParameter(COUNTRY_ID, countryId)
                .addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<IntraCountryRegion> regionIdList = entityDao.executeQuery(regionId);
        return regionIdList;
    }

    @Override
    public List<State> findAllStateInIntraCountryRegion(Long regionId) {
        NeutrinoValidator.notNull(regionId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<State> stateIdLis = new NamedQueryExecutor<State>("address.getAllStateInIntraCountryRegions")
                .addParameter("regionId", regionId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<State> stateIdList = entityDao.executeQuery(stateIdLis);
        Collections.sort(stateIdList, new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return o1.getStateName().compareTo(o2.getStateName());
            }
        });
        return stateIdList;

    }

    @Override
    public List<City> findAllCityInState(Long stateId) {
        NeutrinoValidator.notNull(stateId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<City> cityIdLis = new NamedQueryExecutor<City>("address.getAllCityInState")
                .addParameter(STATE_ID, stateId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<City> cityIdList = entityDao.executeQuery(cityIdLis);
        Collections.sort(cityIdList, new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getCityName().compareTo(o2.getCityName());
            }
        });
        return cityIdList;
    }

    @Override
    public List<District> findAllDistrictInState(Long stateId) {
        NeutrinoValidator.notNull(stateId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<District> districtIdLis = new NamedQueryExecutor<District>("address.getAllDistrictInState")
                .addParameter(STATE_ID, stateId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        ;
        List<District> districtIdList = entityDao.executeQuery(districtIdLis);
        return districtIdList;
    }

    @Override
    public List<Area> findAllAreaInCity(Long cityId) {
        NeutrinoValidator.notNull(cityId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<Area> areaIdLis = new NamedQueryExecutor<Area>("address.getAllAreaFromCity").addParameter(
                "cityId", cityId).addParameter(APPROVAL_STATUS, approvalStatusList);
        List<Area> areaIdList = entityDao.executeQuery(areaIdLis);

        Collections.sort(areaIdList, new Comparator<Area>() {
            @Override
            public int compare(Area o1, Area o2) {
                return o1.getAreaName().compareTo(o2.getAreaName());
            }
        });
        return areaIdList;

    }

    @Override
    public List<ZipCode> findAllZipCodeInCity(Long cityId) {
        NeutrinoValidator.notNull(cityId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<ZipCode> zipCodeIdLis = new NamedQueryExecutor<ZipCode>("address.getAllZipCodeInCity")
                .addParameter("cityId", cityId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        ;
        List<ZipCode> zipCodeList = entityDao.executeQuery(zipCodeIdLis);
        return zipCodeList;
    }

    @Override
    public List<ZipCode> findAllZipCodeInState(Long stateId) {
        NeutrinoValidator.notNull(stateId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<ZipCode> zipCodeIdLis = new NamedQueryExecutor<ZipCode>("address.AllZipCodesInAState")
                .addParameter(STATE_ID, stateId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<ZipCode> zipCodeList = entityDao.executeQuery(zipCodeIdLis);

        Collections.sort(zipCodeList, new Comparator<ZipCode>() {
            @Override
            public int compare(ZipCode o1, ZipCode o2) {
                return o1.getZipCode().compareTo(o2.getZipCode());
            }
        });
        return zipCodeList;
    }

    @Override
    public List<ZipCode> findAllZipCodeInCountry(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<ZipCode> zipCodeIdLis = new NamedQueryExecutor<ZipCode>("address.getAllZipCodesInCountry")
                .addParameter(COUNTRY_ID, countryId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        ;
        List<ZipCode> zipCodeList = entityDao.executeQuery(zipCodeIdLis);
        return zipCodeList;
    }

    @Override
    public List<Area> findAllAreaInZipCode(Long zipCodeId) {
        NeutrinoValidator.notNull(zipCodeId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<Area> areaIdLis = new NamedQueryExecutor<Area>("address.getAllAreaFromZipCode")
                .addParameter("zipCodeId", zipCodeId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Area> areaIdList = entityDao.executeQuery(areaIdLis);

        Collections.sort(areaIdList, new Comparator<Area>() {
            @Override
            public int compare(Area o1, Area o2) {
                return o1.getAreaName().compareTo(o2.getAreaName());
            }
        });
        return areaIdList;
    }

    @Override
    public List<State> findAllStateInCountry(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<State> stateIdLis = new NamedQueryExecutor<State>("address.findAllStatesInCoutry")
                .addParameter(COUNTRY_ID, countryId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<State> stateIdList = entityDao.executeQuery(stateIdLis);

        Collections.sort(stateIdList, new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return o1.getStateName().compareTo(o2.getStateName());
            }
        });

        return stateIdList;

    }
    
    @Override
    public List<State> findAllApprovedStatesInCountry(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<State> stateIdLis = new NamedQueryExecutor<State>("address.findAllApprovedStatesInCountry")
                .addParameter(COUNTRY_ID, countryId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<State> stateIdList = entityDao.executeQuery(stateIdLis);

        Collections.sort(stateIdList, new Comparator<State>() {
            @Override
            public int compare(State o1, State o2) {
                return o1.getStateName().compareTo(o2.getStateName());
            }
        });

        return stateIdList;

    }

    @Override
    public List<District> findAllApprovedDistrictsInState(Long stateId) {
        NeutrinoValidator.notNull(stateId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<District> districtIdLis = new NamedQueryExecutor<District>("address.getAllDistrictInState")
                .addParameter(STATE_ID, stateId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<District> districtIdList = entityDao.executeQuery(districtIdLis);

        districtIdList.sort(Comparator.comparing(District::getDisplayName));

        return districtIdList;

    }
    
    @Override
    public List<City> findAllApprovedCitiesInState(Long stateId) {
        NeutrinoValidator.notNull(stateId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<City> cityIdLis = new NamedQueryExecutor<City>("address.getAllApprovedCityInState")
                .addParameter(STATE_ID, stateId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<City> cityIdList = entityDao.executeQuery(cityIdLis);
        Collections.sort(cityIdList, new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getCityName().compareTo(o2.getCityName());
            }
        });
        return cityIdList;
    }

    @Override
    public List<City> findAllCityInCountry(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<City> cityIdLis = new NamedQueryExecutor<City>("address.findAllCitiesInCountry")
                .addParameter(COUNTRY_ID, countryId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<City> cityIdList = entityDao.executeQuery(cityIdLis);

        Collections.sort(cityIdList, new Comparator<City>() {
            @Override
            public int compare(City o1, City o2) {
                return o1.getCityName().compareTo(o2.getCityName());
            }
        });
        return cityIdList;
    }

    @Override
    public List<String> findLatLongByZipCode(String zipCode) {
        NeutrinoValidator.notNull(zipCode);
        NamedQueryExecutor<String> latLong = new NamedQueryExecutor<String>("address.findLocationByZipCode").addParameter(
                ZIP_CODE, zipCode);
        List<String> latLongList = entityDao.executeQuery(latLong);
        return latLongList;
    }

    @Override
    public List<Address> loadAddressForBuilderProject(Long id, User user) {
        NeutrinoValidator.notNull(id);
        EntityId userEntityId = user.getEntityId();
        if (!(getCurrentUser().getId().equals(userEntityId.getLocalId()))) {
            throw new SystemException(THE_USER_ID + userEntityId.getLocalId()
                    + REQUESTING_ALL_ENTITIES_IT_DOES_NOT_MATCH_WITH_LOGGED_IN_USER_S_ID + getCurrentUser().getId());
        }

        NamedQueryExecutor<Address> address = new NamedQueryExecutor<Address>("address.findAllBuilderProjectAddress")
                .addParameter(PARENT_ID, id);
        return entityDao.executeQuery(address);

    }

  /*  for addressAED only*/
    
  @Override
    public List<Address> loadAddressForAdditionalEmployerDetails(Long id, User user) {
        NeutrinoValidator.notNull(id);
        EntityId userEntityId = user.getEntityId();
        if (!(getCurrentUser().getId().equals(userEntityId.getLocalId()))) {
            throw new SystemException(THE_USER_ID + userEntityId.getLocalId()
                    + REQUESTING_ALL_ENTITIES_IT_DOES_NOT_MATCH_WITH_LOGGED_IN_USER_S_ID + getCurrentUser().getId());
        }

        NamedQueryExecutor<Address> address = new NamedQueryExecutor<Address>("address.findAllAdditionalEmployerDetailsAddress")
                .addParameter(PARENT_ID, id);
        return entityDao.executeQuery(address);

    }
    
    
    @Override
    public List<District> findAllDistrictByCountryId(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        NamedQueryExecutor<District> districtId = new NamedQueryExecutor<District>("address.findAllDistrictByCountryId")
                .addParameter(COUNTRY_ID, countryId).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<District> districtIdList = entityDao.executeQuery(districtId);
        return districtIdList;
    }

    @Override
    public List<Area> findAllAreaInCountry(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        NamedQueryExecutor<Area> areaIdLis = new NamedQueryExecutor<Area>("address.getAllAreaFromCountry")
                .addParameter(COUNTRY_ID, countryId).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<Area> areaIdList = entityDao.executeQuery(areaIdLis);
        Collections.sort(areaIdList, new Comparator<Area>() {
            @Override
            public int compare(Area o1, Area o2) {
                return o1.getAreaName().compareTo(o2.getAreaName());
            }
        });
        return areaIdList;

    }

    @Override
    public Long findVillageIdByName(String name) {
        NeutrinoValidator.notNull(name, "village name can not be null");
        return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<Long>("address.findVillageByName").addParameter(
                "villageName", name));

    }

    @Override
    public VillageMaster findVillageMasterByName(String name) {
        NeutrinoValidator.notNull(name, "village name can not be null");
        return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<VillageMaster>("address.findVillaMastergeByName").addParameter(
                "villageName", name));

    }

    @Override
    public Tehsil findTehsilByName(String name) {
        NeutrinoValidator.notNull(name, "Tehsil name can not be null");
        return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<Tehsil>("address.findTehsilByName").addParameter(
                "tehsilName", name));
    }

    @Override
    public Long findTempTehsilByName(String name) {
        NeutrinoValidator.notNull(name, "Tehsil name can not be null");
        return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<Long>("address.findTempTehsilIdByName").addParameter(
                "tehsilName", name));
    }

    @Override
    public void saveTempTehsil(TempTehsil tempTehsil) {
        entityDao.saveOrUpdate(tempTehsil);
    }

    @Override
    public List<Long> getVillageIdsByDistrictIdAndTehsilName(Long districtId, String tehsilName) {
        NeutrinoValidator.notNull(tehsilName);
        NeutrinoValidator.notNull(districtId);
        NamedQueryExecutor<Long> query = new NamedQueryExecutor<Long>("address.findVillageIdsByDistrictIdAndTehsilName")
                .addParameter("districtId", districtId).addParameter("tehsilName", tehsilName);
        List<Long> villageIds = entityDao.executeQuery(query);
        return villageIds;
    }

    @Override
    public List<Long> getVillageIdsByDistrictIdAndTehsilId(Long districtId, Long tehsilId) {
        NeutrinoValidator.notNull(tehsilId);
        NeutrinoValidator.notNull(districtId);
        NamedQueryExecutor<Long> query = new NamedQueryExecutor<Long>("address.findVillageIdsByDistrictIdAndTehsilId")
                .addParameter("districtId", districtId).addParameter("tehsilId", tehsilId);
        List<Long> villageIds = entityDao.executeQuery(query);
        return villageIds;
    }

    @Override
    public List<Long> getVillageIdsByDistrictId(Long districtId) {
        NeutrinoValidator.notNull(districtId);
        NamedQueryExecutor<Long> query = new NamedQueryExecutor<Long>("address.findVillageIdsByDistrictId")
                .addParameter("districtId", districtId);
        List<Long> villageIds = entityDao.executeQuery(query);
        return villageIds;
    }

    @Override
    public List<Long> getTehsilIdsByDistrictId(Long districtId) {
        NeutrinoValidator.notNull(districtId);
        NamedQueryExecutor<Long> query = new NamedQueryExecutor<Long>("address.findTehsilIdsByDistrictId")
                .addParameter("districtId", districtId);
        List<Long> tehsilIds = entityDao.executeQuery(query);
        return tehsilIds;
    }

    @Override
    public Long findTempVillageIdByName(String name) {
        return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<Long>("address.findTempVillageByName")
                .addParameter("village", name));

    }

    @Override
    public void saveTempVillage(TempVillage tempVillage) {
        entityDao.saveOrUpdate(tempVillage);

    }

    @Override
    public Long findCountryIdByCityId(Long cityId) {
        NeutrinoValidator.notNull(cityId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<Long>("address.findCountryIdByCityId")
                .addParameter("cityId", cityId).addParameter(APPROVAL_STATUS, approvalStatusList));

    }

    @Override
    public Long findStateIdByCityId(Long cityId) {
        NeutrinoValidator.notNull(cityId);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<Long>("address.findStateIdByCityId")
                .addParameter("cityId", cityId).addParameter(APPROVAL_STATUS, approvalStatusList));

    }
    
    @Override
    public List<String> findCountryCodeByISDCode(String isdCode){
      if (StringUtils.isBlank((CharSequence)isdCode)) {
            return Collections.emptyList();
        }
        return (List<String>)this.entityDao.executeQuery(
		new NamedQueryExecutor("address.findCountryCodeByISDCode")
		.addParameter("isdCode", isdCode)
		.addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE)
		);
    }
    
    @Override
    public Map<String, Object> getCountryDetailsByCountryId(Long countryId) {
        NeutrinoValidator.notNull(countryId);
        Country country = entityDao.find(Country.class, countryId);
        if(country != null) {
        	Map<String, Object> countryMap = new HashMap<>();
            countryMap.put("countryISOCode",country.getCountryISOCode());
            countryMap.put("countryISDCode",country.getCountryISDCode());
            return countryMap;
        }
        
        NamedQueryExecutor<Map<String, Object>> executor = new NamedQueryExecutor<Map<String, Object>>(
                "address.getCountryDetailsByCountryId").addParameter(COUNTRY_ID, countryId);
        return entityDao.executeQueryForSingleValue(executor);
    }

    @Override
    public String getCountryNameByCountryId(String countryId) {
        NeutrinoValidator.notNull(countryId);
        Long CountryID = Long.parseLong(countryId);
        NamedQueryExecutor<String> executor = new NamedQueryExecutor<String>(
                "address.getCountryNameByCountryId").addParameter(COUNTRY_ID, CountryID);
        return entityDao.executeQueryForSingleValue(executor);
    }
    
    @Override
    public Address getAddressDetailsByAddressId(Long addressId) {
        NeutrinoValidator.notNull(addressId);       
        NamedQueryExecutor<Address> executor = new NamedQueryExecutor<Address>(
                "address.getAddressDetailsByAddressId").addParameter("addressId", addressId);
        return entityDao.executeQueryForSingleValue(executor);
    }
 
    
    //used for fetching the Customer address from maker area for PDE Address Update
    @Override
    public AddressTemp findTempCustomerAddressById(Long id){
    	NeutrinoValidator.notNull(id);
        return entityDao.find(AddressTemp.class, id);
    }
    //used for fetching the Customer address from maker area for PDE Address Update using GCD Record id
    @Override
    public AddressTemp findTempCustomerAddressByGcdRecordId(String recordId){
    	NeutrinoValidator.notNull(recordId);    	
    	return entityDao.executeQueryForSingleValue(new NamedQueryExecutor<AddressTemp>("address.findAddressTempByGcdAddressRecordId")
                .addParameter("recordId", recordId).addParameter(APPROVAL_STATUS, "T"));        
    }
    @Override
   public Map<String,String> validateAddressBeforeSave(Address address) throws XPathExpressionException{
     Map<String,String> map = addressTagUtil.validateAddressBeforeSave(address);
     return map;
   }
   
   @Override
   public List<Map<String, ?>> searchZipCodesForAutoComplete(String className, String itemVal, String[] searchColumnList,
                                                 String value, boolean loadApprovedEntityFlag, String listOfItems,boolean strictSearchOnListOfItems, int page,String countryId,String stateId,String cityId) {
              NeutrinoValidator.notNull(className, "Class name cannot be null");
              NeutrinoValidator.notNull(searchColumnList, "Columns List cannot be null");
              NeutrinoValidator.notNull(itemVal, "Item value cannot be null");
              Class entityClass;
              List<Map<String, ?>> finalResult = new ArrayList<Map<String, ?>>();
              String[] classList = className.split(",");
              
              List<String> itemsId = new ArrayList<>();
              String itemsIds;
              int counter = 0;
              long totalRecords = 0;
              for (String tempClass : classList) {
                entityClass = AutocompleteLoadedEntitiesMap.getClassFromMap(tempClass);
                StringBuilder sb = new StringBuilder();
                boolean isFirstClause = true;
                
                MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(searchColumnList).addQueryColumns(itemVal);
                
                if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
                  if (loadApprovedEntityFlag) {
                    executor.addAndClause("masterLifeCycleData.approvalStatus IN :approvalStatus");
                    executor.addBoundParameter(APPROVAL_STATUS, ApprovalStatus.APPROVED_RECORD_STATUS_LIST_EXCLUDING_APPROVED_DELETED);
                  }
                }
                
                StringBuilder whereClause = new StringBuilder();
                
                if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
                  whereClause.append("(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = :snapshotRecord) and activeFlag = :activeFlag ");
                  executor.addBoundParameter("activeFlag", true);
                  executor.addBoundParameter("snapshotRecord", Boolean.FALSE);
                } else {
                  whereClause.append("(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = :snapshotRecord) ");
                  executor.addBoundParameter("snapshotRecord", Boolean.FALSE);
                }
                if (listOfItems != null) {
                  itemsId = getItemListIds(listOfItems);
                }
                
                if (itemsId != null) {
                  if (!itemsId.isEmpty()) {
                    whereClause.append(" and id IN (:itemsId)");
                    executor.addBoundParameter("itemsId", itemsId);
                  } else if (strictSearchOnListOfItems) {
                    /* In case strict search on listOfItems is enabled and list of items is empty, return empty list.*/
                    return new ArrayList<Map<String, ?>>();
                  }
                }
                
                executor.addAndClause(whereClause.toString());
                
                for (String search_col : searchColumnList) {
                  if (isFirstClause) {
                    sb.append(" (lower(" + search_col + ") like " + "lower(:"+getSearchParam(search_col)+")");
                    isFirstClause = false;
                  } else {
                    sb.append(" or " + "lower(" + search_col + ") like " + "lower(:"+getSearchParam(search_col)+")");
                  }
                  
                }
                sb.append(")");
                executor = executor.addAndClause(sb.toString()); 
                addSearchParamValue(executor,searchColumnList,value);
                if(notNull(cityId)&& !cityId.isEmpty() && !NULL_STRING.equals(cityId)){
                  executor.addAndClause("city.id=:cityId ");
                  executor.addBoundParameter("cityId", Long.valueOf(cityId));
                } 
                if(notNull(stateId)&&!stateId.isEmpty() && !NULL_STRING.equals(stateId)){
                  executor.addAndClause("state.id=:stateId ");

                  executor.addBoundParameter(STATE_ID, Long.valueOf(stateId));
                }
                if(notNull(countryId)&&!countryId.isEmpty() && !NULL_STRING.equals(countryId)){
                  executor.addAndClause("country.id=:countryId ");

                  executor.addBoundParameter(COUNTRY_ID, Long.valueOf(countryId));
                }
                
                executor.addOrderByClause("order by lower(" + searchColumnList[0]+")");
                
                List<Map<String, ?>> result = entityDao.executeQuery(executor, page * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);

                for (Map<String, ?> temp : result) {
                    finalResult.add(counter, temp);
                    counter++;
                }
                totalRecords = totalRecords + entityDao.executeTotalRowsQuery(executor);
              }
              Map<String, Long> sizeMap = new HashMap<String, Long>();
              sizeMap.put("size", totalRecords);
              finalResult.add(counter, sizeMap);
              if (finalResult != null) {
                  BaseLoggers.flowLogger.debug("size of finalResult :" + finalResult.size());
              }
              return finalResult;
            }
	
   private void addSearchParamValue(MapQueryExecutor executor, String[] searchColumnList,String value) {
   	
		if (searchColumnList==null || searchColumnList.length==0) {
			return ;
		}
       for (String search_col : searchColumnList) {
       	executor.addBoundParameter(getSearchParam(search_col), value+"%");
       }			
	}

	private String getSearchParam(String searchParam) {
		
		return searchParam.replace('.', '_');
	}

   private List<String> getItemListIds(String listItems) {

     String iList = listItems;
     iList = iList.substring(1, iList.length() - 1);
     List<String> listOfIds = new ArrayList<String>();
     if (StringUtils.isNoneEmpty(iList)) {
         String[] list = iList.split(",");
         for (int i = 0 ; i < list.length ; i++) {
             String[] subList = list[i].split(":");
             listOfIds.add(subList[1]);

         }
     }
     return listOfIds;

 }

	@Override
	public Map<String, Object> loadPaginatedData(GridVO gridVO, Class entityName,
			String userUri, Long parentId) {
		return this.loadPaginatedData(entityName, userUri, parentId,
				gridVO.getiDisplayStart(), gridVO.getiDisplayLength(),
				gridVO.getSortColName(), gridVO.getSortDir());
	}

    @Override
    public void handleVillageAndTehsilMaster(Address address) {
        String newVillageName = address.getVillage();
        VillageMaster villageMaster = null;
        if (address.getVillageMaster() != null && address.getVillageMaster().getId() != null) {
            villageMaster = entityDao.find(VillageMaster.class,address.getVillageMaster().getId());
        }
        if(villageMaster != null && villageMaster.getName().equalsIgnoreCase(address.getVillage())) {
            address.setVillageMaster(villageMaster);
            address.setVillage(villageMaster.getName());
        }
        else{
            if(StringUtils.isNotEmpty(newVillageName)){
                Long tempVillageId = this.findTempVillageIdByName(newVillageName);
                address.setVillageMaster(null);
                if (tempVillageId == null) {
                    TempVillage village = new TempVillage();
                    village.setVillageName(newVillageName);
                    address.setVillage(newVillageName);
                    this.saveTempVillage(village);
                }else{
                    address.setVillage(newVillageName);
                }
            }else{
                address.setVillageMaster(null);
                address.setVillage(null);
            }
        }
        String newTehsilName = address.getTaluka();
        Tehsil tehsil = null;
        if (address.getTehsil() != null && address.getTehsil().getId() != null) {
            tehsil = entityDao.find(Tehsil.class,address.getTehsil().getId());
        }
        if(tehsil != null && tehsil.getName().equalsIgnoreCase(address.getTaluka())){
            address.setTehsil(tehsil);
            address.setTaluka(tehsil.getName());
        }else{
            if(StringUtils.isNotEmpty(newTehsilName)){
                Long tempTehsilId = this.findTempTehsilByName(newTehsilName);
                if (tempTehsilId == null) {
                    TempTehsil tempTehsil = new TempTehsil();
                    tempTehsil.setName(newTehsilName);
                    address.setTaluka(newTehsilName);
                    address.setTehsil(null);
                    this.saveTempTehsil(tempTehsil);
                }else{
                    address.setTehsil(null);
                    address.setTaluka(newTehsilName);
                }
            }else{
                address.setTehsil(null);
                address.setTaluka(null);
            }
        }
    }

    @Override
    public void handleVillageAndTehsilMaster(AddressTemp address) {
        String newVillageName = address.getVillage();
        VillageMaster villageMaster = null;
        if (address.getVillageMaster() != null && address.getVillageMaster().getId() != null) {
            villageMaster = entityDao.find(VillageMaster.class,address.getVillageMaster().getId());
        }
        if(villageMaster != null && villageMaster.getName().equalsIgnoreCase(address.getVillage())) {
            address.setVillageMaster(villageMaster);
            address.setVillage(villageMaster.getName());
        }
        else{
            if(StringUtils.isNotEmpty(newVillageName)){
                address.setVillage(newVillageName);
            }else{
                address.setVillage(null);
            }
            address.setVillageMaster(null);
        }
        String newTehsilName = address.getTaluka();
        Tehsil tehsil = null;
        if (address.getTehsil() != null && address.getTehsil().getId() != null) {
            tehsil = entityDao.find(Tehsil.class,address.getTehsil().getId());
        }
        if(tehsil != null && tehsil.getName().equalsIgnoreCase(address.getTaluka())){
            address.setTehsil(tehsil);
            address.setTaluka(tehsil.getName());
        }else{
            if(StringUtils.isNotEmpty(newTehsilName)){
                address.setTaluka(newTehsilName);
            }else{
                address.setTaluka(null);
            }
            address.setTehsil(null);
        }
    }
    
    
   @Override
   public State getStateAttributes(Long id){
	   State stateObj = null;
	   NamedQueryExecutor<State> stateQuery = new NamedQueryExecutor<State>("customPincode.findStateForCustomValidation")
               .addParameter("id",id);
       List<State> stateList = entityDao.executeQuery(stateQuery);    
       if(!stateList.isEmpty()){
    	   stateObj = stateList.get(0);
       }
       return stateObj;
   }

   
   @Override
   public int validateCustomPincodeValue(String customPincodeValue, State stateObj){
	   
	   int returnValue = 0;

	   if(stateObj.getMinimumLength() != null){
		   if(customPincodeValue.length() < stateObj.getMinimumLength()){
			   return -1;
		   }
	   }
	   
	   if(stateObj.getMaximumLength() != null){
		   if(customPincodeValue.length() > stateObj.getMaximumLength()){
			   return -1;
		   }
	   }
	   
	   if(stateObj.getPincodeType() != null){
		   
		   if(stateObj.getPincodeType() == true){			   
			   String numericRegexp = "^[0-9]*";
			   if(!customPincodeValue.matches(numericRegexp)){
				   return -1;
			   }
		   }
		   
	   }
	   
	   if(stateObj.getValidationType() != null){
		   
		   if(stateObj.getValidationType().equals("startsEndsWith")){
			   
			   if(stateObj.getPincodeStart() != null){
				   
				   if(pincodeStartValidation(customPincodeValue, stateObj) == -1){
					   return -1;
				   }   
			   }
			   if(stateObj.getPincodeEnd() != null){
				   
				   if(pincodeEndValidation(customPincodeValue, stateObj) == -1){
					   return -1;
				   }
				   
			   }
			   
		   } else if(stateObj.getValidationType().equals("range")){
			   
			   if(stateObj.getPincodeRange() != null){
				   
				   if(pincodeRangeValidation(customPincodeValue, stateObj) == -1){
					   return -1;
				   }
				   
			   }
			   
		   }
		   
	   }
	   
	   return returnValue;
	   
   }
   
   public int pincodeStartValidation(String customPincodeValue, State stateObj){
	   
	   int validationFlag = -1;
	   String tempStartArray[] = stateObj.getPincodeStart().split(",");
	   
	   for(int i=0; i<tempStartArray.length; i++){
		   if(customPincodeValue.substring(0,tempStartArray[i].length()).equals(tempStartArray[i])){
			   validationFlag = 1;
		   }
	   }
	   return validationFlag;
	   
   }
   
   public int pincodeEndValidation(String customPincodeValue, State stateObj){
	   
	   int validationFlag = -1;
	   String tempEndArray[] = stateObj.getPincodeEnd().split(",");
	   
	   for(int i=0; i<tempEndArray.length; i++){
		   if(customPincodeValue.substring(customPincodeValue.length()-tempEndArray[i].length(), customPincodeValue.length()).equals(tempEndArray[i])){
			   validationFlag = 1;
		   }
	   }
	   return validationFlag;
	   
   }
   
   public int pincodeRangeValidation(String customPincodeValue, State stateObj){
	   
	   int validationFlag = -1;
	   String tempRangeArray[] = stateObj.getPincodeRange().split(",");
	   
	   for(int i=0; i< tempRangeArray.length; i++){
		   String currentRange[] = tempRangeArray[i].split("-");			 
		   if(Integer.parseInt(currentRange[0].trim()) <= Integer.parseInt(customPincodeValue.trim()) && Integer.parseInt(currentRange[1].trim()) >= Integer.parseInt(customPincodeValue.trim())){
			   validationFlag = 1;
		   } 
	   }
	   return validationFlag;
	   
   }
   @Transactional(readOnly = true)
   @Override
    public  List<City> findAllApprovedCitiesInState(List<Long> stateList){
        {
            NeutrinoValidator.notNull(stateList);
            List<Integer> approvalStatusList = new ArrayList<Integer>();
            approvalStatusList.add(ApprovalStatus.APPROVED);
            approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
            approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
            approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
            NamedQueryExecutor<City> cityIdLis = new NamedQueryExecutor<City>("address.getAllApprovedCityUsingStates")
                    .addParameter("stateList", stateList).addParameter(APPROVAL_STATUS, approvalStatusList)
                    .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            List<City> cityIdList = entityDao.executeQuery(cityIdLis);
            if(CollectionUtils.isEmpty(cityIdList)){
                return Collections.emptyList();
            }
            Collections.sort(cityIdList, new Comparator<City>() {
                @Override
                public int compare(City o1, City o2) {
                    return o1.getCityName().compareTo(o2.getCityName());
                }
            });
            return cityIdList;
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<VillageMaster> findAllApprovedVillagesInState(List<Long> stateList) {
        NeutrinoValidator.notNull(stateList);
        List<Integer> approvalStatusList = new ArrayList<Integer>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
        approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<VillageMaster> villageIdList = new NamedQueryExecutor<VillageMaster>("address.getAllApprovedVillagesUsingStates")
                .addParameter("stateList", stateList).addParameter(APPROVAL_STATUS, approvalStatusList)
                .addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
        List<VillageMaster> villageList = entityDao.executeQuery(villageIdList);
        if(CollectionUtils.isEmpty(villageList)){
            return Collections.emptyList();
        }
        Collections.sort(villageList, new Comparator<VillageMaster>() {
            @Override
            public int compare(VillageMaster o1, VillageMaster o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return villageList;
    }

    @Override
    public Address getAddressFromAddressDataVo(AddressDataVo addressDataVo) {
        Address address = new Address();
        if(Objects.isNull(addressDataVo)){
            return address;
        }
        if(Objects.nonNull(addressDataVo.getAdditionalAddressPurpose()) && StringUtils.isNotEmpty(addressDataVo.getAdditionalAddressPurpose())){
            AdditionalAddressPurpose additionalAddressPurpose = genericParameterService.findByCode(addressDataVo.getAdditionalAddressPurpose(), AdditionalAddressPurpose.class);
            address.setAdditionalAddressPurpose(additionalAddressPurpose);
        }
        if(Objects.nonNull(addressDataVo.getAddressTypeAED()) && StringUtils.isNotEmpty(addressDataVo.getAddressTypeAED())){
            AddressTypeAED addressTypeAED = genericParameterService.findByCode(addressDataVo.getAddressTypeAED(),AddressTypeAED.class);
            address.setAddressTypeAED(addressTypeAED);
        }


        if(Objects.nonNull(addressDataVo.getOwnershipStatus()) && StringUtils.isNotEmpty(addressDataVo.getOwnershipStatus())){
            OwnershipStatus ownershipStatus = genericParameterService.findByCode(addressDataVo.getOwnershipStatus(),OwnershipStatus.class);
            address.setOwnershipStatus(ownershipStatus);
        }
        address.setStreet(addressDataVo.getStreet());
        address.setPoBox(addressDataVo.getPoBox());

        if(Objects.nonNull(addressDataVo.getIsCopiedAddress()) && StringUtils.isNotEmpty(addressDataVo.getIsCopiedAddress())){
            Boolean isCopiedAddress = BooleanUtils.toBoolean(addressDataVo.getIsCopiedAddress());
            address.setIsCopiedAddress(isCopiedAddress);
        }
        address.setIsMappedToMultipleAddrTypes(addressDataVo.getMappedToMultipleAddrTypes());

        if(Objects.nonNull(addressDataVo.getOtherAddressTypeList()) && CollectionUtils.isNotEmpty(addressDataVo.getOtherAddressTypeList())){
            List<AddressType> otherAddressTypeList = new ArrayList<>();
            addressDataVo.getOtherAddressTypeList().forEach(addressType ->otherAddressTypeList.add(genericParameterService.findByCode(addressType,AddressType.class)));
            address.setOtherAddressTypeList(otherAddressTypeList);
        }

        /*if(Objects.nonNull(addressDataVo.getOtherAsAddressTypeListIds()) && CollectionUtils.isNotEmpty(addressDataVo.getOtherAsAddressTypeListIds())){

            address.setOtherAsAddressTypeListIds(addressDataVo.getOtherAsAddressTypeListIds());
        }*/
        address.setSameAsAddress(addressDataVo.getSameAsAddress());
        address.setAddressLine1(addressDataVo.getAddressLine1());
        address.setAddressLine2(addressDataVo.getAddressLine2());
        address.setNumberOfMonthsAtAddress(addressDataVo.getNumberOfMonthsAtAddress());
        address.setAddressLine3(addressDataVo.getAddressLine3());
        address.setMonthsInCurrentCity(addressDataVo.getMonthsInCurrentCity());
        address.setNumberOfYearsAtAddress(addressDataVo.getNumberOfYearsAtAddress());
        address.setAddressLine4(addressDataVo.getAddressLine4());
        address.setCompleteAddress(addressDataVo.getCompleteAddress());

        if(Objects.nonNull(addressDataVo.getAddressType()) && StringUtils.isNotEmpty(addressDataVo.getAddressType())){
            AddressType addressType = genericParameterService.findByCode(addressDataVo.getAddressType(),AddressType.class);
            address.setAddressType(addressType);
        }

        address.setYearsInCurrentCity(addressDataVo.getYearsInCurrentCity());

        if(Objects.nonNull(addressDataVo.getCountry()) && addressDataVo.getCountry()!=0l){
            Country country = entityDao.find(Country.class,addressDataVo.getCountry());
            address.setCountry(country);
        }
        if(Objects.nonNull(addressDataVo.getState()) && addressDataVo.getState()!=0l){
            State state = entityDao.find(State.class,addressDataVo.getState());
            address.setState(state);
        }
        if(Objects.nonNull(addressDataVo.getCity()) && addressDataVo.getCity()!=0l){
            City city = entityDao.find(City.class,addressDataVo.getCity());
            address.setCity(city);
        }

        address.setVillage(addressDataVo.getVillage());

        if(Objects.nonNull(addressDataVo.getDistrict()) && addressDataVo.getDistrict()!=0l){
            District district = entityDao.find(District.class,addressDataVo.getDistrict());
            address.setDistrict(district);
        }
        if(Objects.nonNull(addressDataVo.getZipcode()) && addressDataVo.getZipcode()!=0l){
            ZipCode zipCode = entityDao.find(ZipCode.class,addressDataVo.getZipcode());
            address.setZipcode(zipCode);
        }
        if(Objects.nonNull(addressDataVo.getRegion()) && addressDataVo.getRegion()!=0l){
            IntraCountryRegion region = entityDao.find(IntraCountryRegion.class,addressDataVo.getRegion());
            address.setRegion(region);
        }
        if(Objects.nonNull(addressDataVo.getArea()) && addressDataVo.getArea()!=0l){
            Area area = entityDao.find(Area.class,addressDataVo.getArea());
            address.setArea(area);
        }
        /*address.setPhoneNumberList(addressDataVo.getPhoneNumberList());*/
        address.setLandMark(addressDataVo.getLandMark());
        address.setActiveAddress(addressDataVo.isActiveAddress());
        address.setOccupancyStartDate(addressDataVo.getOccupancyStartDate());
        address.setOccupancyEndDate(addressDataVo.getOccupancyEndDate());
        address.setSendParcel(addressDataVo.isSendParcel());
        address.setAdditionalInfo(addressDataVo.getAdditionalInfo());
        address.setPrimaryAddress(addressDataVo.isPrimaryAddress());
        address.setExpressionId(addressDataVo.getExpressionId());
        address.setCustomPincodeFlag(addressDataVo.getCustomPincodeFlag());
        address.setCustomPincodeValue(addressDataVo.getCustomPincodeValue());
        /*address.setGstIn(addressDataVo.getGstIn());*/
        if(Objects.nonNull(addressDataVo.getAccomodationType()) && StringUtils.isNotEmpty(addressDataVo.getAccomodationType())){
            AccomodationType accomodationType = genericParameterService.findByCode(addressDataVo.getAccomodationType(),AccomodationType.class);
            address.setAccomodationType(accomodationType);
        }
        /*address.setLongitude(addressDataVo.getLongitude());
        address.setLatitude(addressDataVo.getLatitude());*/
        if(Objects.nonNull(addressDataVo.getResidenceType()) && StringUtils.isNotEmpty(addressDataVo.getResidenceType())){
            ResidenceType residenceType = genericParameterService.findByCode(addressDataVo.getResidenceType(),ResidenceType.class);
            address.setResidenceType(residenceType);
        }
        address.setOtherResidenceType(addressDataVo.getOtherResidenceType());
        address.setTaluka(addressDataVo.getTaluka());
        if(Objects.nonNull(addressDataVo.getAddressTypeAgriculture()) && StringUtils.isNotEmpty(addressDataVo.getAddressTypeAgriculture())){
            AddressTypeAgriculture addressTypeAgriculture = genericParameterService.findByCode(addressDataVo.getAddressTypeAgriculture(),AddressTypeAgriculture.class);
            address.setAddressTypeAgriculture(addressTypeAgriculture);
        }
        if(Objects.nonNull(addressDataVo.getVillageMaster()) && addressDataVo.getVillageMaster()!=0l){
            VillageMaster villageMaster = entityDao.find(VillageMaster.class,addressDataVo.getVillageMaster());
            address.setVillageMaster(villageMaster);
        }
        if(Objects.nonNull(addressDataVo.getTehsil()) && addressDataVo.getTehsil()!=0l){
            Tehsil tehsil = entityDao.find(Tehsil.class,addressDataVo.getTehsil());
            address.setTehsil(tehsil);
        }
        if(Objects.nonNull(addressDataVo.getStreetType()) && addressDataVo.getStreetType()!=0l){
            Street street = entityDao.find(Street.class,addressDataVo.getStreetType());
            address.setStreetMaster(street);
        }
        address.setAdditionalField1(addressDataVo.getAdditionalField1());
        address.setAdditionalField2(addressDataVo.getAdditionalField2());
        address.setAdditionalField3(addressDataVo.getAdditionalField3());
        address.setAdditionalField4(addressDataVo.getAdditionalField4());
        address.setAdditionalField5(addressDataVo.getAdditionalField5());
        address.setAdditionalDropdownField1(addressDataVo.getAdditionalDropdownField1());
        address.setAdditionalDropdownField2(addressDataVo.getAdditionalDropdownField2());
        /*address.setGenericYesNo(addressDataVo.getGenericYesNo());
        address.setGcdId(addressDataVo.getGcdId());
        address.setGSTINDetails(addressDataVo.getGSTINDetails());*/
        return address;

    }

    @Override
    public Address prepareAddressToSave( Address address){

        List<PhoneNumber> phoneNumbers = new ArrayList<PhoneNumber>();


        if(null != address.getCustomPincodeFlag()) {
            if (address.getCustomPincodeFlag() == true) {
                address.setZipcode(null);
            }

            if (address.getCustomPincodeFlag() == false) {
                address.setCustomPincodeValue(null);
            }
        }

        if (address.getCountry() == null || address.getCountry().getId() == null) {
            address.setCountry(null);
        } else {
            address.setCountry(entityDao.find(Country.class, address.getCountry().getId()));
        }

        if (address.getCity() == null || address.getCity().getId() == null) {
            address.setCity(null);
        }else{
            address.setCity(entityDao.find(City.class, address.getCity().getId()));
        }

        if (address.getState() == null || address.getState().getId() == null) {
            address.setState(null);
        }else {
            address.setState(entityDao.find(State.class, address.getState().getId()));
        }
        if (address.getArea() == null || address.getArea().getId() == null) {
            address.setArea(null);
        }else {
            address.setArea(entityDao.find(Area.class, address.getArea().getId()));
        }
        if (address.getDistrict() == null || address.getDistrict().getId() == null) {
            address.setDistrict(null);
        }else {
            address.setDistrict(entityDao.find(District.class, address.getDistrict().getId()));
        }

        if (address.getRegion() == null || address.getRegion().getId() == null) {
            address.setRegion(null);
        }else {
            address.setRegion(entityDao.find(IntraCountryRegion.class, address.getRegion().getId()));
        }

        if (address.getZipcode() == null || address.getZipcode().getId() == null) {
            address.setZipcode(null);
        }else {
            address.setZipcode(entityDao.find(ZipCode.class, address.getZipcode().getId()));
        }

        if (address.getAccomodationType() == null || address.getAccomodationType().getId() == null) {
            address.setAccomodationType(null);
        }else {
            address.setAccomodationType(entityDao.find(AccomodationType.class, address.getAccomodationType().getId()));
        }

        if (address.getResidenceType() == null || address.getResidenceType().getId() == null) {
            address.setResidenceType(null);
        }else {
            address.setResidenceType(entityDao.find(ResidenceType.class, address.getResidenceType().getId()));
        }

        if (address.getAddressTypeAED() == null || address.getAddressTypeAED().getId() == null) {
            address.setAddressTypeAED(null);
        }else {
            address.setAddressTypeAED(entityDao.find(AddressTypeAED.class, address.getAddressTypeAED().getId()));
        }
        if (address.getAddressType() == null || address.getAddressType().getId() == null) {
            address.setAddressType(null);
        }else{
            address.setAddressType(entityDao.find(AddressType.class, address.getAddressType().getId()));
        }

        if (address.getAddressTypeAgriculture() == null || address.getAddressTypeAgriculture().getId() == null) {
            address.setAddressTypeAgriculture(null);
        }else {
            address.setAddressTypeAgriculture(entityDao.find(AddressTypeAgriculture.class, address.getAddressTypeAgriculture().getId()));
        }

        if (address.getOwnershipStatus() == null || address.getOwnershipStatus().getId() == null) {
            address.setOwnershipStatus(null);
        }else {
            address.setOwnershipStatus(entityDao.find(OwnershipStatus.class, address.getOwnershipStatus().getId()));
        }

        if (address.getAdditionalAddressPurpose() == null || address.getAdditionalAddressPurpose().getId() == null) {
            address.setAdditionalAddressPurpose(null);
        }else {
            address.setAdditionalAddressPurpose(entityDao.find(AdditionalAddressPurpose.class, address.getAdditionalAddressPurpose().getId()));
        }

        if (address.getGenericYesNo() == null || address.getGenericYesNo().getId() == null) {
            address.setGenericYesNo(null);
        }else {
            address.setGenericYesNo(entityDao.find(AddressGeneric.class, address.getGenericYesNo().getId()));
        }
        if (address.getStreetMaster() == null || address.getStreetMaster().getId() == null) {
            address.setStreetMaster(null);
        }else {
            address.setStreetMaster(entityDao.find(Street.class, address.getStreetMaster().getId()));
        }
        address.setAdditionalDropdownField1(address.getAdditionalDropdownField1());
        address.setAdditionalDropdownField2(address.getAdditionalDropdownField2());

        if (address.getPhoneNumberList() != null && !address.getPhoneNumberList().isEmpty()) {
            for (PhoneNumber phoneNumber : address.getPhoneNumberList()) {
                if (phoneNumber.getPhoneNumber() != null && !phoneNumber.getPhoneNumber().equals("")) {
                    if (phoneNumber.getNumberType() != null && phoneNumber.getNumberType().getCode() != null) {
                        PhoneNumberType phoneNumberType = genericParameterService.findByCode(phoneNumber.getNumberType()
                                .getCode(), PhoneNumberType.class);
                        PhoneNumberType numberType = phoneNumberType;
                        phoneNumber.setNumberType(numberType);
                    }

                    phoneNumbers.add(phoneNumber);
                }
            }
            address.setPhoneNumberList(phoneNumbers);
        }
        String newVillageName = address.getVillage();
        VillageMaster villageMaster = null;
        if (address.getVillageMaster() != null && address.getVillageMaster().getId() != null) {
            villageMaster = entityDao.find(VillageMaster.class, address.getVillageMaster().getId());
        }
        if (villageMaster != null && villageMaster.getName().equalsIgnoreCase(address.getVillage())) {
            address.setVillageMaster(villageMaster);
            address.setVillage(villageMaster.getName());
        }

        else{
            if (StringUtils.isNotEmpty(newVillageName)) {
                Long tempVillageId = findTempVillageIdByName(newVillageName);
                address.setVillageMaster(null);
                if (tempVillageId == null) {
                    TempVillage village = new TempVillage();
                    village.setVillageName(newVillageName);
                    address.setVillage(newVillageName);
                    saveTempVillage(village);
                } else {
                    address.setVillage(newVillageName);
                }
            } else {
                address.setVillageMaster(null);
                address.setVillage(null);
            }
        }
        String newTehsilName = address.getTaluka();
        Tehsil tehsil = null;
        if (address.getTehsil() != null && address.getTehsil().getId() != null) {
            tehsil = entityDao.find(Tehsil.class, address.getTehsil().getId());
        }
        if (tehsil != null && tehsil.getName().equalsIgnoreCase(address.getTaluka())) {
            address.setTehsil(tehsil);
            address.setTaluka(tehsil.getName());
        } else {
            if (StringUtils.isNotEmpty(newTehsilName)) {
                Long tempTehsilId = findTempTehsilByName(newTehsilName);
                if (tempTehsilId == null) {
                    TempTehsil tempTehsil = new TempTehsil();
                    tempTehsil.setName(newTehsilName);
                    address.setTaluka(newTehsilName);
                    address.setTehsil(null);
                    saveTempTehsil(tempTehsil);
                } else {
                    address.setTehsil(null);
                    address.setTaluka(newTehsilName);
                }
            } else {
                address.setTehsil(null);
                address.setTaluka(null);
            }
        }
        return address;
    }

    public void initializeAddressLazyProperties(Address address) {
        if (address != null) {


            Hibernate.initialize(address.getPhoneNumberList());

            if(address.getPhoneNumberList()!=null && !address.getPhoneNumberList().isEmpty()){

                for(PhoneNumber phoneNumber:address.getPhoneNumberList()){
                    if(phoneNumber!=null){
                        Hibernate.initialize(phoneNumber.getPhoneConnectionType());
                        Hibernate.initialize(phoneNumber.getNumberType());
                    }
                }
            }
            Hibernate.initialize(address.getAddressTypeAED());
            Hibernate.initialize(address.getOwnershipStatus());
            Hibernate.initialize(address.getAdditionalAddressPurpose());
            Hibernate.initialize(address.getAddressType());
            if(address.getAddressType()!=null) {
                Hibernate.initialize(address.getAddressType().getName());
            }

            Hibernate.initialize(address.getAddressTypeAgriculture());
            Hibernate.initialize(address.getCity());
            Hibernate.initialize(address.getZipcode());
            Hibernate.initialize(address.getCountry());
            Hibernate.initialize(address.getState());
            Hibernate.initialize(address.getOtherAddressTypeList());
            Hibernate.initialize(address.getRegion());
            Hibernate.initialize(address.getAccomodationType());
            Hibernate.initialize(address.getVillageMaster());
            Hibernate.initialize(address.getTehsil());
            Hibernate.initialize(address.getDistrict());
            Hibernate.initialize(address.getArea());
            Hibernate.initialize(address.getResidenceType());
            Hibernate.initialize(address.getGenericYesNo());
            Hibernate.initialize(address.getGSTINDetails());
            Hibernate.initialize(address.getStreetMaster());
        }
    }

    /**
     *
     * @param address
     * @return address converted to upperCase
     */
    public Address returnAddressInUpperCase(Address address,String profileName) {
        if (profileName.equalsIgnoreCase("icici")) {
            if (address != null) {
                if (address.getAddressLine1() != null) {
                    address.setAddressLine1(address.getAddressLine1().toUpperCase());
                }
                if (address.getAddressLine2() != null) {
                    address.setAddressLine2(address.getAddressLine2().toUpperCase());
                }
                if (address.getAddressLine3() != null) {
                    address.setAddressLine3(address.getAddressLine3().toUpperCase());
                }
                if (address.getAddressLine4() != null) {
                    address.setAddressLine4(address.getAddressLine4().toUpperCase());
                }
                if (address.getVillage() != null) {
                    address.setVillage(address.getVillage().toUpperCase());
                }
                if (address.getTaluka() != null) {
                    address.setTaluka(address.getTaluka().toUpperCase());
                }
                if (address.getAdditionalInfo() != null) {
                    address.setAdditionalInfo(address.getAdditionalInfo().toUpperCase());
                }
                if (address.getLandMark() != null) {
                    address.setLandMark(address.getLandMark().toUpperCase());
                }
                /*if (address.getStreet() != null) {
                    address.setStreet(address.getStreet().toUpperCase());
                }*/

            }
        }

        return address;

    }

    @Override
    public List<Street> findAllStreetInCity(Long cityId) {
        NeutrinoValidator.notNull(cityId);
        List<Integer> approvalStatusList = new ArrayList<>();
        approvalStatusList.add(ApprovalStatus.APPROVED);
        approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);

        NamedQueryExecutor<Street> streetQuery = new NamedQueryExecutor<Street>("address.getAllStreetFromCity").addParameter(
                "cityId", cityId).addParameter(APPROVAL_STATUS, approvalStatusList);
        List<Street> streetList = entityDao.executeQuery(streetQuery);

        Collections.sort(streetList, new Comparator<Street>() {
            @Override
            public int compare(Street o1, Street o2) {
                return o1.getStreetName().compareTo(o2.getStreetName());
            }
        });
        return streetList;

    }




    @Override
    public List<Address> addConsolidatedAddressInViewProperties(List<Address> addressList){

        ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),"config.cas.show.consolidated.address.table");
        if(configurationVO!=null && configurationVO.getPropertyValue().equalsIgnoreCase("true")){
            ConfigurationVO configurationVO1 = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),"config.custom.address.table.data");
            if(configurationVO1!=null) {
                String fields = configurationVO1.getPropertyValue();
                String [] fieldsArray = fields.split(",");
                if (CollectionUtils.isNotEmpty(addressList)) {
                    for (Address address : addressList) {
                        String consolidatedAddress = "";
                        for(int i=0;i<fieldsArray.length;i++){
                            String complexKey = fieldsArray[i];
                            Object tempMap = address;
                            String key = null;
                            try {
                                while (complexKey.contains(".")) {
                                    String[] keyValue = complexKey.split("\\.", 2);
                                    key = keyValue[0];
                                    complexKey = keyValue[1];
                                    tempMap = HibernateUtils.initializeAndUnproxy(tempMap);
                                    Class oClass = tempMap.getClass();

                                    Field field = oClass.getDeclaredField(key.split("\\.", 2)[0]);
                                    field = HibernateUtils.initializeAndUnproxy(field);
                                    field.setAccessible(true);
                                    tempMap = field.get(tempMap);
                                    if(tempMap==null)
                                        break;

                                }
                                if(tempMap!=null) {
                                    tempMap = HibernateUtils.initializeAndUnproxy(tempMap);
                                    Class oClass = tempMap.getClass();
                                    Object value = null;
                                    if (oClass != null) {
                                        Class baseEntityClass = oClass;
                                        while(baseEntityClass!=null){
                                            baseEntityClass = baseEntityClass.getSuperclass();
                                            if(baseEntityClass.getSimpleName().equalsIgnoreCase("BaseEntity"))
                                                break;

                                        }
                                        Field transientMaskingMapField = baseEntityClass.getDeclaredField("transientMaskingMap");
                                        transientMaskingMapField.setAccessible(true);

                                        if(transientMaskingMapField.get(tempMap) != null){
                                            Map map = (Map)transientMaskingMapField.get(tempMap);
                                            value = map.get(complexKey);
                                        }else {
                                            Field field = oClass.getDeclaredField(complexKey);
                                            field = HibernateUtils.initializeAndUnproxy(field);
                                            field.setAccessible(true);

                                            value = field.get(tempMap);
                                        }
                                    }
                                    if (value != null) {
                                        consolidatedAddress += value.toString() + CONSOLIDATED_ADDRESS_SEPARATOR ;
                                    }
                                }
                            }catch (Exception e){
                                BaseLoggers.flowLogger.error("Exception in calcualting consolidated address",e);
                            }

                        }
                        int index = consolidatedAddress.lastIndexOf(CONSOLIDATED_ADDRESS_SEPARATOR);
                        consolidatedAddress = consolidatedAddress.substring(0,index);
                        address.addProperty("consolidatedAddress",consolidatedAddress);
                    }
                }
            }

        }

        return addressList;
    }


}