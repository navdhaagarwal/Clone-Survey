package com.nucleus.user;


import com.nucleus.address.Area;

import java.util.List;

public interface UserCityVillageMappingService {
    public List<Area> getAreaFromCity(Long cityId);

    public List<Area> getAreaFromVillage(Long villageId);

    public UserCityVillageMapping getCityVillageMappingByUserId(Long userId);

    public UserCityVillageMapping deletePrevMapping(UserCityVillageMapping prevUserCityVillageMapping,UserCityVillageMapping userCityVillageMapping);

}
