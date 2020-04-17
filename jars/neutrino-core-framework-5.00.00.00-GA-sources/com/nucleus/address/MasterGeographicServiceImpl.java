package com.nucleus.address;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.query.constants.QueryHint;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named(value = "masterGeographicService")
public class MasterGeographicServiceImpl implements MasterGeographicService {

    @Inject
    @Named("entityDao")
    protected EntityDao entityDao;

    /* @see com.nucleus.address.MasterGeographicService#getAllStatesInCountry(java.lang.Long) 
     * Method to Get All states Need On Country Id
    */
    @Override
    public List<State> getAllStatesInCountry(Long countryId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        if (countryId != null) {
            NamedQueryExecutor<State> nquery = new NamedQueryExecutor<State>("address.findAllStatesInCoutry").addParameter(
                    "countryId", countryId).addParameter("approvalStatus", statusList);
            List<State> cityList = entityDao.executeQuery(nquery);

            Collections.sort(cityList, new Comparator<State>() {
                @Override
                public int compare(State o1, State o2) {
                    return o1.getStateName().compareTo(o2.getStateName());
                }
            });
            return cityList;
        } else {
            BaseLoggers.exceptionLogger.error("country Id is null");
            return null;
        }
    }

    /* @see com.nucleus.address.MasterGeographicService#getAllStatesInCountry(java.lang.Long) 
     * Method to Get All cities based On stateId
    */

    @Override
    public List<City> getAllCitiesInState(Long stateId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        if (stateId != null) {
            NamedQueryExecutor<City> nquery = new NamedQueryExecutor<City>("address.getAllCityInState").addParameter(
                    "stateId", stateId).addParameter("approvalStatus", statusList);
            List<City> cityList = entityDao.executeQuery(nquery);
            Collections.sort(cityList, new Comparator<City>() {
                @Override
                public int compare(City o1, City o2) {
                    return o1.getCityName().compareTo(o2.getCityName());
                }
            });
            return cityList;
        } else {
            BaseLoggers.exceptionLogger.error("Unable to Retrieve Record : : State Id cannot be null");
            return null;
        }
    }

    /* @see com.nucleus.address.MasterGeographicService#getAllStatesInCountry(java.lang.Long) 
     * Method to Get All ZipCodes in a state
    */

    @Override
    public List<ZipCode> getAllZipCodesInAState(Long stateId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        if (stateId != null) {
            NamedQueryExecutor<ZipCode> nquery = new NamedQueryExecutor<ZipCode>("address.AllZipCodesInAState")
                    .addParameter("stateId", stateId).addParameter("approvalStatus", statusList);
            List<ZipCode> zipCodeList = entityDao.executeQuery(nquery);

            Collections.sort(zipCodeList, new Comparator<ZipCode>() {
                @Override
                public int compare(ZipCode o1, ZipCode o2) {
                    return o1.getZipCode().compareTo(o2.getZipCode());
                }
            });
            return zipCodeList;
        } else {
            BaseLoggers.exceptionLogger.error("Unable to Retrieve Record : : State Id cannot be null");
            return null;
        }

    }

    /* @see com.nucleus.address.MasterGeographicService#getAllStatesInCountry(java.lang.Long) 
     * Method to Get All Area from city Id or Zipcode
    */

    @SuppressWarnings("unused")
    @Override
    public List<Area> getAllAreaFromCityorZipCode(Long cityId, Long zipCodeId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<Area> nquery = null;
        if (zipCodeId == null) {
            nquery = new NamedQueryExecutor<Area>("address.getAllAreaFromCity").addParameter("cityId", cityId).addParameter(
                    "approvalStatus", statusList);
            List<Area> areaList = entityDao.executeQuery(nquery);

            Collections.sort(areaList, new Comparator<Area>() {
                @Override
                public int compare(Area o1, Area o2) {
                    return o1.getAreaName().compareTo(o2.getAreaName());
                }
            });
            return areaList;
        }
        if (cityId == null) {
            nquery = new NamedQueryExecutor<Area>("address.getAllAreaFromZipCode").addParameter("zipCodeId", zipCodeId)
                    .addParameter("approvalStatus", statusList).addQueryHint(QueryHint.QUERY_HINT_CACHEABLE, Boolean.TRUE);
            List<Area> areaList = entityDao.executeQuery(nquery);

            Collections.sort(areaList, new Comparator<Area>() {
                @Override
                public int compare(Area o1, Area o2) {
                    return o1.getAreaName().compareTo(o2.getAreaName());
                }
            });
            return areaList;
        }
        if (zipCodeId == null && cityId == null) {
            BaseLoggers.exceptionLogger.error("Zip Id and Country Id cannot be null");
            return null;
        }
        BaseLoggers.exceptionLogger.error("Unable to Retrieve Record");
        return null;
    }

    /* @see com.nucleus.address.MasterGeographicService#getAllStatesInCountry(java.lang.Long) 
     * Method to Get All countries from Region 
    */
    @Override
    public List<Country> getAllCountriesInReagion(Long regionId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        NamedQueryExecutor<Country> nquery = null;
        if (regionId != null) {
            nquery = new NamedQueryExecutor<Country>("address.getAllCountriesFromRegion").addParameter("regionId", regionId)
                    .addParameter("approvalStatus", statusList);
            List<Country> countryList = entityDao.executeQuery(nquery);
            return countryList;
        } else {
            BaseLoggers.exceptionLogger.error("Region Id id Null");
            return null;
        }

    }
    
    @Override
    public List<IntraCountryRegion> getAllIntraCountryRegionsOfCountryById(Long countryId){
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);

        NamedQueryExecutor<IntraCountryRegion> nquery = null;
        NeutrinoValidator.notNull(countryId,"Country Id cannot be null");
        
            nquery = new NamedQueryExecutor<IntraCountryRegion>("Country.getAllIntraCountryRegions").addParameter("countryId", countryId)
                    .addParameter("statusList", statusList);
            List<IntraCountryRegion> regionList = entityDao.executeQuery(nquery);
            return regionList;
      
    }
}
