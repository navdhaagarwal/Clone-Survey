/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following-
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.contact;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Embeddable class to hold properties
 *          detail call info
 *          preferred language
 */

@Embeddable
public class CallPreference implements Serializable {

    private static final long   serialVersionUID        = 6958720629879571601L;

    @Column(name = "callPrefData", length = 4000)
    private String              detailedCallPrefInfoForDay;

    private Long                numberOfCallsPerDay;

    @ManyToOne
    private PrefferedLanguage   prefferedLanguage;

    @Column(name = "prefCommMode")
    private String              prefferedModeOfCommunication;

    private Boolean             receivePromotionalCalls = Boolean.FALSE;

    private Boolean             receivePromrotionalCall;

    @Transient
    private CallRecord          callRecordMon           = new CallRecord();

    @Transient
    private CallRecord          callRecordTue           = new CallRecord();

    @Transient
    private CallRecord          callRecordWed           = new CallRecord();

    @Transient
    private CallRecord          callRecordThu           = new CallRecord();

    @Transient
    private CallRecord          callRecordFri           = new CallRecord();

    @Transient
    private CallRecord          callRecordSat           = new CallRecord();

    @Transient
    private CallRecord          callRecordSun           = new CallRecord();

    private static final String EMAIL                   = "Email";

    private static final String PHONE                   = "PhoneCall";

    public Boolean getReceivePromotionalCalls() {
        return receivePromotionalCalls;
    }

    public void setReceivePromotionalCalls(Boolean receivePromotionalCalls) {
        this.receivePromotionalCalls = receivePromotionalCalls;
    }

    public PrefferedLanguage getPrefferedLanguage() {
        return prefferedLanguage;
    }

    public void setPrefferedLanguage(PrefferedLanguage prefferedLanguage) {
        this.prefferedLanguage = prefferedLanguage;
    }

    public String getPrefferedModeOfCommunication() {
        return prefferedModeOfCommunication;
    }

    public void setPrefferedModeOfCommunication(String prefferedModeOfCommunication) {
        this.prefferedModeOfCommunication = prefferedModeOfCommunication;
    }

    public Long getNumberOfCallsPerDay() {
        return numberOfCallsPerDay;
    }

    public void setNumberOfCallsPerDay(Long numberOfCallsPerDay) {
        this.numberOfCallsPerDay = numberOfCallsPerDay;
    }

    public Boolean getReceivePromrotionalCall() {
        return receivePromrotionalCall;
    }

    public void setReceivePromrotionalCall(Boolean receivePromrotionalCall) {
        this.receivePromrotionalCall = receivePromrotionalCall;
    }

    public String getDetailedCallPrefInfoForDay() {
        return detailedCallPrefInfoForDay;
    }

    public void setDetailedCallPrefInfoForDay(String detailedCallPrefInfoForDay) {
        this.detailedCallPrefInfoForDay = detailedCallPrefInfoForDay;
    }

    public CallRecord getCallRecordMon() {
        return callRecordMon;
    }

    public void setCallRecordMon(CallRecord callRecordMon) {
        this.callRecordMon = callRecordMon;
    }

    public CallRecord getCallRecordTue() {
        return callRecordTue;
    }

    public void setCallRecordTue(CallRecord callRecordTue) {
        this.callRecordTue = callRecordTue;
    }

    public CallRecord getCallRecordWed() {
        return callRecordWed;
    }

    public void setCallRecordWed(CallRecord callRecordWed) {
        this.callRecordWed = callRecordWed;
    }

    public CallRecord getCallRecordThu() {
        return callRecordThu;
    }

    public void setCallRecordThu(CallRecord callRecordThu) {
        this.callRecordThu = callRecordThu;
    }

    public CallRecord getCallRecordFri() {
        return callRecordFri;
    }

    public void setCallRecordFri(CallRecord callRecordFri) {
        this.callRecordFri = callRecordFri;
    }

    public CallRecord getCallRecordSat() {
        return callRecordSat;
    }

    public void setCallRecordSat(CallRecord callRecordSat) {
        this.callRecordSat = callRecordSat;
    }

    public CallRecord getCallRecordSun() {
        return callRecordSun;
    }

    public void setCallRecordSun(CallRecord callRecordSun) {
        this.callRecordSun = callRecordSun;
    }

    public String getDetailedCallPrefInfo() {
        return detailedCallPrefInfoForDay;
    }

    public void setDetailedCallPrefInfo(String detailedCallPrefInfo) {
        this.detailedCallPrefInfoForDay = detailedCallPrefInfo;
    }

    @PrePersist
    public void createStringFromData() {
        StringBuilder sb = new StringBuilder();
        if (null != callRecordMon.getDay()) {
            sb.append(callRecordMon.getDay()).append("-");

            if (null != callRecordMon.getFromTime() && !callRecordMon.getFromTime().equals("")) {
                sb.append(callRecordMon.getFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordMon.getToTime() && !callRecordMon.getToTime().equals("")) {
                sb.append(callRecordMon.getToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordMon.getDndFromTime() && !callRecordMon.getDndFromTime().equals("")) {
                sb.append(callRecordMon.getDndFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordMon.getDndToTime() && !callRecordMon.getDndToTime().equals("")) {
                sb.append(callRecordMon.getDndToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            sb.append("~");
        }

        if (null != callRecordTue.getDay()) {
            sb.append(callRecordTue.getDay()).append("-");
            if (null != callRecordTue.getFromTime() && !callRecordTue.getFromTime().equals("")) {
                sb.append(callRecordTue.getFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordTue.getToTime() && !callRecordTue.getToTime().equals("")) {
                sb.append(callRecordTue.getToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordTue.getDndFromTime() && !callRecordTue.getDndFromTime().equals("")) {
                sb.append(callRecordTue.getDndFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordTue.getDndToTime() && !callRecordTue.getDndToTime().equals("")) {
                sb.append(callRecordTue.getDndToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            sb.append("~");
        }

        if (null != callRecordWed.getDay()) {
            sb.append(callRecordWed.getDay()).append("-");
            if (null != callRecordWed.getFromTime() && !callRecordWed.getFromTime().equals("")) {
                sb.append(callRecordWed.getFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordWed.getToTime() && !callRecordWed.getToTime().equals("")) {
                sb.append(callRecordWed.getToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordWed.getDndFromTime() && !callRecordWed.getDndFromTime().equals("")) {
                sb.append(callRecordWed.getDndFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordWed.getDndToTime() && !callRecordWed.getDndToTime().equals("")) {
                sb.append(callRecordWed.getDndToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            sb.append("~");
        }

        if (null != callRecordThu.getDay()) {
            sb.append(callRecordThu.getDay()).append("-");
            if (null != callRecordThu.getFromTime() && !callRecordThu.getFromTime().equals("")) {
                sb.append(callRecordThu.getFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordThu.getToTime() && !callRecordThu.getToTime().equals("")) {
                sb.append(callRecordThu.getToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordThu.getDndFromTime() && !callRecordThu.getDndFromTime().equals("")) {
                sb.append(callRecordThu.getDndFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordThu.getDndToTime() && !callRecordThu.getDndToTime().equals("")) {
                sb.append(callRecordThu.getDndToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            sb.append("~");
        }

        if (null != callRecordFri.getDay()) {
            sb.append(callRecordFri.getDay()).append("-");
            if (null != callRecordFri.getFromTime() && !callRecordFri.getFromTime().equals("")) {
                sb.append(callRecordFri.getFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordFri.getToTime() && !callRecordFri.getToTime().equals("")) {
                sb.append(callRecordFri.getToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordFri.getDndFromTime() && !callRecordFri.getDndFromTime().equals("")) {
                sb.append(callRecordFri.getDndFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordFri.getDndToTime() && !callRecordFri.getDndToTime().equals("")) {
                sb.append(callRecordFri.getDndToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            sb.append("~");
        }

        if (null != callRecordSat.getDay()) {
            sb.append(callRecordSat.getDay()).append("-");
            if (null != callRecordSat.getFromTime() && !callRecordSat.getFromTime().equals("")) {
                sb.append(callRecordSat.getFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordSat.getToTime() && !callRecordSat.getToTime().equals("")) {
                sb.append(callRecordSat.getToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordSat.getDndFromTime() && !callRecordSat.getDndFromTime().equals("")) {
                sb.append(callRecordSat.getDndFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordSat.getDndToTime() && !callRecordSat.getDndToTime().equals("")) {
                sb.append(callRecordSat.getDndToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            sb.append("~");
        }

        if (null != callRecordSun.getDay()) {
            sb.append(callRecordSun.getDay()).append("-");
            if (null != callRecordSun.getFromTime() && !callRecordSun.getFromTime().equals("")) {
                sb.append(callRecordSun.getFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordSun.getToTime() && !callRecordSun.getToTime().equals("")) {
                sb.append(callRecordSun.getToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordSun.getDndFromTime() && !callRecordSun.getDndFromTime().equals("")) {
                sb.append(callRecordSun.getDndFromTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            if (null != callRecordSun.getDndToTime() && !callRecordSun.getDndToTime().equals("")) {
                sb.append(callRecordSun.getDndToTime()).append("-");
            } else {
                sb.append("NA").append("-");
            }
            sb.append("~");
        }
        setDetailedCallPrefInfo(sb.toString());
    }

    public void createDataFromString() {

        /*in case of mysql,if we store empty string ("") in database ,it stores as empty string  but for oracle,empty string ("") 
         * is stored as null.*/
        if (detailedCallPrefInfoForDay == null) {
            detailedCallPrefInfoForDay = "";
        }
        String[] daysRecords = detailedCallPrefInfoForDay.split("~");
        String[] daysRecordsDetails = null;

        for (int days = 0 ; days < daysRecords.length ; days++) {
            if (daysRecords[days] != null) {
                daysRecordsDetails = daysRecords[days].split("-");

                if (daysRecordsDetails[0].equals("M")) {
                    callRecordMon.setDay("M");
                    callRecordMon.setFromTime(daysRecordsDetails[1]);
                    callRecordMon.setToTime(daysRecordsDetails[2]);
                    callRecordMon.setDndFromTime(daysRecordsDetails[3]);
                    callRecordMon.setDndToTime(daysRecordsDetails[4]);
                    new CallPreference().setCallRecordMon(callRecordMon);
                }

                if (daysRecordsDetails[0].equals("T")) {
                    callRecordTue.setDay("T");
                    callRecordTue.setFromTime(daysRecordsDetails[1]);
                    callRecordTue.setToTime(daysRecordsDetails[2]);
                    callRecordTue.setDndFromTime(daysRecordsDetails[3]);
                    callRecordTue.setDndToTime(daysRecordsDetails[4]);
                    new CallPreference().setCallRecordTue(callRecordTue);
                }

                if (daysRecordsDetails[0].equals("W")) {
                    callRecordWed.setDay("W");
                    callRecordWed.setFromTime(daysRecordsDetails[1]);
                    callRecordWed.setToTime(daysRecordsDetails[2]);
                    callRecordWed.setDndFromTime(daysRecordsDetails[3]);
                    callRecordWed.setDndToTime(daysRecordsDetails[4]);
                }

                if (daysRecordsDetails[0].equals("TH")) {
                    callRecordThu.setDay("TH");
                    callRecordThu.setFromTime(daysRecordsDetails[1]);
                    callRecordThu.setToTime(daysRecordsDetails[2]);
                    callRecordThu.setDndFromTime(daysRecordsDetails[3]);
                    callRecordThu.setDndToTime(daysRecordsDetails[4]);
                }

                if (daysRecordsDetails[0].equals("F")) {
                    callRecordFri.setDay("F");
                    callRecordFri.setFromTime(daysRecordsDetails[1]);
                    callRecordFri.setToTime(daysRecordsDetails[2]);
                    callRecordFri.setDndFromTime(daysRecordsDetails[3]);
                    callRecordFri.setDndToTime(daysRecordsDetails[4]);
                }

                if (daysRecordsDetails[0].equals("ST")) {
                    callRecordSat.setDay("ST");
                    callRecordSat.setFromTime(daysRecordsDetails[1]);
                    callRecordSat.setToTime(daysRecordsDetails[2]);
                    callRecordSat.setDndFromTime(daysRecordsDetails[3]);
                    callRecordSat.setDndToTime(daysRecordsDetails[4]);
                }

                if (daysRecordsDetails[0].equals("SN")) {
                    callRecordSun.setDay("SN");
                    callRecordSun.setFromTime(daysRecordsDetails[1]);
                    callRecordSun.setToTime(daysRecordsDetails[2]);
                    callRecordSun.setDndFromTime(daysRecordsDetails[3]);
                    callRecordSun.setDndToTime(daysRecordsDetails[4]);
                }

            }

        }
    }
}
