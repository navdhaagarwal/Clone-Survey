package com.nucleus.master.adminactivityreport.util;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Named("adminActivityUtil")
public class AdminActivityReportUtil {

    public static int getLongestStringArrayLength(List<List<String>> stringArrayList) {
        int maxStringLength = 0;
        List<String> longestString = null;
        for (List<String> s : stringArrayList) {
            if (s.size() > maxStringLength) {
                maxStringLength = s.size();
                longestString = s;
            }
        }
        if(longestString!=null){
            return longestString.size();
        }else{
            return 0;
        }
    }

    public static List<String> breakStringByLastDelimiterForPartitionSize(String originalString, String delimiter, int partitionSize) {
        List<String> partitionedStringList = new ArrayList<>();
        if (Objects.isNull(originalString)) {
            return partitionedStringList;
        }
        if (originalString.length() <= partitionSize) {
            partitionedStringList.add(originalString);
            return partitionedStringList;
        }
        int startPosition = 0;
        int remainingLenth = originalString.length();

        while (remainingLenth >= partitionSize) {
            int splitPosition = originalString.substring(startPosition, partitionSize).lastIndexOf(delimiter);
            splitPosition++;
            if(splitPosition==0){
                splitPosition = originalString.substring(startPosition, partitionSize).lastIndexOf(" ");
                ++splitPosition;
            }
            if(splitPosition==0){
                splitPosition = partitionSize;
                ++splitPosition;
            }
            String scoopedString = originalString.substring(0, splitPosition);
            originalString = originalString.substring(splitPosition);
            remainingLenth = originalString.length();
            partitionedStringList.add(scoopedString);
        }
        if (remainingLenth > 0) {
            partitionedStringList.add(originalString);
        }
        return partitionedStringList;
    }

}
