package com.nucleus.contact;

import java.io.Serializable;

/**
 * 
 * @author Nucleus Software Exports Limited
 * POJO to hold the call timings
 */

public class CallRecord implements Serializable {

    private static final long serialVersionUID = 917120108803838185L;

    private String fromTime;

    private String toTime;

    private String dndFromTime;

    private String dndToTime;

    private String day;

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getFromTime() {
        return fromTime;
    }

    public void setFromTime(String fromTime) {
        this.fromTime = fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public void setToTime(String toTime) {
        this.toTime = toTime;
    }

    public String getDndFromTime() {
        return dndFromTime;
    }

    public void setDndFromTime(String dndFromTime) {
        this.dndFromTime = dndFromTime;
    }

    public String getDndToTime() {
        return dndToTime;
    }

    public void setDndToTime(String dndToTime) {
        this.dndToTime = dndToTime;
    }

}
