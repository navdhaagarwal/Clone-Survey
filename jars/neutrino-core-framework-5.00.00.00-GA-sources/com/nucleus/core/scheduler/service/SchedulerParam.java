package com.nucleus.core.scheduler.service;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchedulerParam {

    public static final List<Map<String, String>> JOB_HOURS_OF_DAY         = new ArrayList<Map<String, String>>();
    public static final List<Map<String, String>> JOB_DATEOFMONTH          = new ArrayList<Map<String, String>>();
    public static final List<Map<String, String>> JOB_FREQUENCY            = new ArrayList<Map<String, String>>();
    public static final List<Map<String, String>> JOB_STATUS               = new ArrayList<Map<String, String>>();
    public static final List<Map<String, String>> JOB_DAYSOFWEEK           = new ArrayList<Map<String, String>>();

    private static final NumberFormat              NUMBER_FORMAT            = new DecimalFormat("0000");
    public static final String                     DYNAMIC_REPORT_JOB_GROUP = "DYNAMIC_REPORT_JOB_GROUP";
    public static final String                     PURGE_JOB_GROUP = "PURGE_JOB_GROUP";

    static {

        for (int i = 0 ; i < 25 ; i++) {
            final int j = i;
            JOB_HOURS_OF_DAY.add(new HashMap<String, String>() {
                private static final long serialVersionUID = 3240540352591750040L;
                {
                    put("id", String.valueOf(j));
                    put("name", NUMBER_FORMAT.format(j*100L));
                }
            });
        }

        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> map2 = new HashMap<String, String>();
        map.put("id", "FIRST");
        map.put("name", "First");
        map2.put("id", "LAST");
        map2.put("name", "Last");
        JOB_DATEOFMONTH.add(map);
        JOB_DATEOFMONTH.add(map2);

        Map<String, String> map3 = new HashMap<String, String>();
        Map<String, String> map4 = new HashMap<String, String>();
        Map<String, String> map5 = new HashMap<String, String>();
        Map<String, String> map6 = new HashMap<String, String>();
        map3.put("id", "MONTHLY");
        map3.put("name", "MONTHLY");
        map4.put("id", "ONCE");
        map4.put("name", "ONCE");
        map5.put("id", "WEEKLY");
        map5.put("name", "WEEKLY");
        map6.put("id", "DAILY");
        map6.put("name", "DAILY");
        JOB_FREQUENCY.add(map3);
        JOB_FREQUENCY.add(map4);
        JOB_FREQUENCY.add(map5);
        JOB_FREQUENCY.add(map6);

        Map<String, String> map7 = new HashMap<String, String>();
        Map<String, String> map8 = new HashMap<String, String>();
        map7.put("id", "0");
        map7.put("name", "Active");
        map8.put("id", "1");
        map8.put("name", "Paused");
        JOB_STATUS.add(map7);
        JOB_STATUS.add(map8);

        Map<String, String> map9 = new HashMap<String, String>();
        Map<String, String> map10 = new HashMap<String, String>();
        Map<String, String> map11 = new HashMap<String, String>();
        Map<String, String> map12 = new HashMap<String, String>();
        Map<String, String> map13 = new HashMap<String, String>();
        Map<String, String> map14 = new HashMap<String, String>();
        Map<String, String> map15 = new HashMap<String, String>();
        map9.put("id", "1");
        map9.put("name", "Sunday");
        map10.put("id", "2");
        map10.put("name", "Monday");
        map11.put("id", "3");
        map11.put("name", "Tuesday");
        map12.put("id", "4");
        map12.put("name", "Wednesday");
        map13.put("id", "5");
        map13.put("name", "Thursday");
        map14.put("id", "6");
        map14.put("name", "Friday");
        map15.put("id", "7");
        map15.put("name", "Saturday");
        JOB_DAYSOFWEEK.add(map9);
        JOB_DAYSOFWEEK.add(map10);
        JOB_DAYSOFWEEK.add(map11);
        JOB_DAYSOFWEEK.add(map12);
        JOB_DAYSOFWEEK.add(map13);
        JOB_DAYSOFWEEK.add(map14);
        JOB_DAYSOFWEEK.add(map15);
    }
    
}
