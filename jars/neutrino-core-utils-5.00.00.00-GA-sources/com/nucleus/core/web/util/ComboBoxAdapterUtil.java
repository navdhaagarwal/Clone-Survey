package com.nucleus.core.web.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.nucleus.core.exceptions.InvalidDataException;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public class ComboBoxAdapterUtil {

    /**
     * 
     * @param listOfMaps
     * @param keyPositionKey
     * @param valuePositionKey
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Map listOfMapsToSingleMap(List listOfMaps, String keyPositionKey, String valuePositionKey) {
        Map combinedMap = new LinkedHashMap();
        for (Map map : (List<Map>) listOfMaps) {
            if (!map.containsKey(keyPositionKey) || !map.containsKey(valuePositionKey)) {
                throw new InvalidDataException("Either " + keyPositionKey + " or " + valuePositionKey + " key not found!!");
            }
            combinedMap.put(map.get(keyPositionKey), map.get(valuePositionKey));
        }
        return combinedMap;
    }

}
