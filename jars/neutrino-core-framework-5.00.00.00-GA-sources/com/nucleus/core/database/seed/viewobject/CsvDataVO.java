package com.nucleus.core.database.seed.viewobject;

import com.nucleus.logging.BaseLoggers;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvDataVO implements Serializable {

    private String seedLocation;
    private int seedSequence;
    private boolean seedActive;

    public static final Map<String, Map<String, List<Long>>> filterMap = new HashMap<>();
    public static final Map<String, String> schemaNameMap = new HashMap<>();
    public static String SCHEMA_NAME;

    public String getSeedLocation() {
        return seedLocation;
    }

    public void setSeedLocation(String seedLocation) {
        this.seedLocation = seedLocation;
    }

    public int getSeedSequence() {
        return seedSequence;
    }

    public void setSeedSequence(int seedSequence) {
        this.seedSequence = seedSequence;
    }

    public boolean isSeedActive() {
        return seedActive;
    }
    public void setSeedActive(boolean seedActive) {
        this.seedActive = seedActive;
    }

    public CsvDataVO(String line, String seedLocation) {
        String[] splitValues = line.split(",",-1);
        this.seedLocation = String.format("%s%s",seedLocation,splitValues[0]);
        this.seedSequence = Integer.parseInt(splitValues[1]);
        this.seedActive = Boolean.parseBoolean(splitValues[2]);
        if (this.seedActive) {
            String sheetName = splitValues[0].replace(".xls","");
            if(splitValues.length == 3) {
                schemaNameMap.put(sheetName.toUpperCase(), SCHEMA_NAME);
            } else if(splitValues.length == 4) {
                schemaNameMap.put(sheetName.toUpperCase(), (StringUtils.isNotEmpty(splitValues[3]) ? splitValues[3]: SCHEMA_NAME));
            } else if(splitValues.length == 5) {
                schemaNameMap.put((StringUtils.isNotEmpty(splitValues[4]) ? splitValues[4].toUpperCase() : sheetName),(StringUtils.isNotEmpty(splitValues[3]) ? splitValues[3]: SCHEMA_NAME));
            }
            if(splitValues.length == 7) {
                schemaNameMap.put((StringUtils.isNotEmpty(splitValues[4]) ? splitValues[4].toUpperCase() : sheetName),(StringUtils.isNotEmpty(splitValues[3]) ? splitValues[3]: SCHEMA_NAME));
                if(StringUtils.isNoneEmpty(splitValues[5], splitValues[6])) {
                    Map<String, List<Long>> rangeMap = populateMap(splitValues[5], splitValues[6]);
                    if (MapUtils.isNotEmpty(rangeMap)) {
                        this.filterMap.put(sheetName.toLowerCase(), rangeMap);
                    }
                }
            }
        }
    }
    private Map<String, List<Long>> populateMap(String keyString, String rangeString){
        String[] keyArr = keyString.split(";",-1);
        String[] rangeArrStr = rangeString.split(";", -1);
        Map<String, List<Long>> rangeMap = new HashMap<>();
        for (int i=0;i<keyArr.length;i++){
            List<Long> rangeList = new ArrayList<>();
            String range = rangeArrStr[i].trim();
            if(StringUtils.isNotEmpty(range)) {
                String[] multiRangeArr = range.split(":", -1);
                for (int j=0;j<multiRangeArr.length;j++) {
                    String[] rangeArr = multiRangeArr[j].split("-",-1);
                    rangeList.add(Long.parseLong(rangeArr[0].trim()));
                    rangeList.add(Long.parseLong(rangeArr[1].trim()));
                }
                rangeMap.put(keyArr[i], rangeList);
                BaseLoggers.flowLogger.info("Using range mode seeding for {} ", this.seedLocation );
            }
        }
        return rangeMap;
    }
}

