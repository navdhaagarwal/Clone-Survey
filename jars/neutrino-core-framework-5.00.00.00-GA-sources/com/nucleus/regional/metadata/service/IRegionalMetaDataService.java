package com.nucleus.regional.metadata.service;

import java.util.List;
import java.util.Map;

import com.nucleus.regional.RegionalData;
import com.nucleus.regional.RegionalEnabled;
import com.nucleus.regional.metadata.RegionalMetaData;

public interface IRegionalMetaDataService {
    Map<String, Object> getRegionalMetaData();

    Map<String, Object> getRegionalDataAttributesValue(
            RegionalEnabled regionalEnabled);

    Map<String, Object> getRegionalDataAttributeValue(RegionalData data,
            String sourceEntity);

    void setRegionalDataAttributesValue(
            Map<String, Object> logicalNameValueMap,
            RegionalEnabled regionalEnabled);

    Map<String, String> getLogicalNameAndRegionalFieldMapping(
            String sourceEntityName);

    String getRegionalPathBasedOnSourceEntityAndLogicalField(
            String logicalName, String sourceEntityName);

    String getQueryAppenderForRegionalFields(String logicalName,
            RegionalEnabled regionalEnabled);

    List<RegionalMetaData> fetchRegionalMetaDataforSourceEntity(
            String sourceEntityName);

    Map<String, RegionalMetaData> getLogicalNameAndRegionalMetaDataMap(
            String sourceEntityName);
}
