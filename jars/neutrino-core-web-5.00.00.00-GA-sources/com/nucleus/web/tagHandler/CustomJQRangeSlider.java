package com.nucleus.web.tagHandler;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.joda.time.DateTime;

import com.nucleus.core.exceptions.SystemException;

public class CustomJQRangeSlider extends SimpleTagSupport {
    private String       id;
    private String       slider;
    private double       minBound;
    private double       maxBound;
    private DateTime     minDateBound    = null;
    private DateTime     maxDateBound    = null;
    private boolean      arrows;
    private double       defaultMin;
    private double       defaultMax;
    private DateTime     defaultDateMin  = null;
    private DateTime     defaultDateMax  = null;
    private int          delayOut;
    private int          durationIn;
    private int          durationOut;
    private String       rangeMin;
    private String       rangeMax;
    private double       step;
    private String       dateStep;
    private String       type;
    private String       valueLabels;
    private String       wheelMode;
    private int          wheelSpeed;

    private final String sliderTypeOne   = "Basic";
    private final String sliderTypeTwo   = "Edit";
    private final String sliderTypeThree = "Date";

    public void doTag() throws JspException {

        try {
            // Get the writer object for output.
            JspWriter out = getJspContext().getOut();

            String rangeBound = "";
            if (minBound != 0 && maxBound != 0) {
                rangeBound = "bounds: {min: " + minBound + ", max: " + maxBound + "}";
            }

            String dateRangeBound = "";
            if (minDateBound != null && maxDateBound != null) {
                dateRangeBound = "bounds:{min: " + "new Date(" + minDateBound.getYear() + ","
                        + minDateBound.getMonthOfYear() + "," + minDateBound.getDayOfMonth() + ")" + ",max: " + "new Date("
                        + maxDateBound.getYear() + "," + maxDateBound.monthOfYear() + "," + maxDateBound.getDayOfMonth()
                        + ")" + "}";
            }

            String showArrows = "arrows:" + arrows + ",";

            String defaultValues = "";
            if (defaultMin != 0 && defaultMax != 0) {
                defaultValues = "defaultValues:{min: " + defaultMin + ",max: " + defaultMax + "}" + ",";
            }

            String defaultDateValues = "";
            if (defaultDateMin != null && defaultDateMax != null) {

                defaultDateValues = "defaultValues:{min: " + "new Date(" + defaultDateMin.getYear() + ","
                        + defaultDateMin.getMonthOfYear() + "," + defaultDateMin.getDayOfMonth() + ")" + ", max: "
                        + "new Date(" + defaultDateMax.getYear() + "," + defaultDateMax.getMonthOfYear() + ","
                        + defaultDateMax.getDayOfMonth() + ")" + "}" + ",";

            }

            String setDelayout = "";
            if (delayOut != 0) {
                setDelayout = "delayOut: " + delayOut + ",";
            }

            String setDurationIn = "";
            if (durationIn != 0) {
                setDurationIn = "durationIn: " + durationIn + ",";
            }

            String setDurationOut = "";
            if (durationOut != 0) {
                setDurationOut = "durationOut: " + durationOut + ",";
            }

            String setRangeMin = "";
            if (rangeMin != null && rangeMax != null) {
                setRangeMin = "range: {min: " + rangeMin + ", max: " + rangeMax + "}" + ",";
            }

            String setStep = "";
            if (step != 0) {
                setStep = "step: " + step + ",";
            }

            String setDateStep = "";
            if (dateStep != null) {
                setDateStep = "step:" + dateStep + ",";
            }

            String setType = "";
            if (type != null) {
                setType = "type: \"" + type + "\"" + ",";
            }

            String setValueLable = "";
            if (valueLabels != null) {
                setValueLable = "valueLabels: \"" + valueLabels + "\"" + ",";
            }

            String setWheelMode = "";
            if (wheelMode != null) {
                setWheelMode = "wheelMode: \"" + wheelMode + "\"" + ",";
            }

            String setWheelSpeed = "";
            if (wheelSpeed != 0 && wheelMode.equals("scroll")) {
                setWheelSpeed = "wheelSpeed: " + wheelSpeed + ",";
            }

            if (slider.equalsIgnoreCase(sliderTypeOne)) {

                out.println("<div id=\"" + id + "\"></div>" + "<script>" + "$(\"#" + id + "\").rangeSlider({" + showArrows
                        + defaultValues + setDelayout + setValueLable + setDurationIn + setDurationOut + setRangeMin
                        + setStep + setWheelMode + setWheelSpeed + rangeBound + "});" + "</script>");

            } else if (slider.equalsIgnoreCase(sliderTypeTwo)) {

                out.println("<div id=\"" + id + "\"></div>" + "<script>" + "$(\"#" + id + "\").editRangeSlider({"
                        + showArrows + defaultValues + setDelayout + setValueLable + setType + setDurationIn
                        + setDurationOut + setRangeMin + setStep + setWheelMode + setWheelSpeed + rangeBound + "});"
                        + "</script>");

            } else if (slider.equalsIgnoreCase(sliderTypeThree)) {

                out.println("<div id=\"" + id + "\"></div>" + "<script>" + "$(\"#" + id + "\").dateRangeSlider({"
                        + showArrows + defaultDateValues + setDelayout + setValueLable + setDurationIn + setDurationOut
                        + setRangeMin + setDateStep + setWheelMode + setWheelSpeed + dateRangeBound + "});" + "</script>");

            } else {
                throw new SystemException("Incorrect slider type specified can only have values in " + sliderTypeOne + " ,"
                        + sliderTypeTwo + " ," + sliderTypeThree);
            }

        } catch (Exception e) {
            throw new SystemException("Exception in JQ Range slider", e);
        }

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSlider() {
        return slider;
    }

    public void setSlider(String slider) {
        this.slider = slider;
    }

    public double getMinBound() {
        return minBound;
    }

    public void setMinBound(double minBound) {
        this.minBound = minBound;
    }

    public double getMaxBound() {
        return maxBound;
    }

    public void setMaxBound(double maxBound) {
        this.maxBound = maxBound;
    }

    public boolean isArrows() {
        return arrows;
    }

    public void setArrows(boolean arrows) {
        this.arrows = arrows;
    }

    public double getDefaultMin() {
        return defaultMin;
    }

    public void setDefaultMin(double defaultMin) {
        this.defaultMin = defaultMin;
    }

    public double getDefaultMax() {
        return defaultMax;
    }

    public void setDefaultMax(double defaultMax) {
        this.defaultMax = defaultMax;
    }

    public int getDelayOut() {
        return delayOut;
    }

    public void setDelayOut(int delayOut) {
        this.delayOut = delayOut;
    }

    public int getDurationIn() {
        return durationIn;
    }

    public void setDurationIn(int durationIn) {
        this.durationIn = durationIn;
    }

    public String getRangeMin() {
        return rangeMin;
    }

    public void setRangeMin(String rangeMin) {
        this.rangeMin = rangeMin;
    }

    public String getRangeMax() {
        return rangeMax;
    }

    public void setRangeMax(String rangeMax) {
        this.rangeMax = rangeMax;
    }

    public double getStep() {
        return step;
    }

    public void setStep(double step) {
        this.step = step;
    }

    public String getDateStep() {
        return dateStep;
    }

    public void setDateStep(String dateStep) {
        this.dateStep = dateStep;
    }

    public String getValueLabels() {
        return valueLabels;
    }

    public void setValueLabels(String valueLabels) {
        this.valueLabels = valueLabels;
    }

    public String getWheelMode() {
        return wheelMode;
    }

    public void setWheelMode(String wheelMode) {
        this.wheelMode = wheelMode;
    }

    public int getWheelSpeed() {
        return wheelSpeed;
    }

    public void setWheelSpeed(int wheelSpeed) {
        this.wheelSpeed = wheelSpeed;
    }

    public int getDurationOut() {
        return durationOut;
    }

    public void setDurationOut(int durationOut) {
        this.durationOut = durationOut;
    }

    public DateTime getMinDateBound() {
        return minDateBound;
    }

    public void setMinDateBound(DateTime minDateBound) {
        this.minDateBound = minDateBound;
    }

    public DateTime getMaxDateBound() {
        return maxDateBound;
    }

    public void setMaxDateBound(DateTime maxDateBound) {
        this.maxDateBound = maxDateBound;
    }

    public DateTime getDefaultDateMin() {
        return defaultDateMin;
    }

    public void setDefaultDateMin(DateTime defaultDateMin) {
        this.defaultDateMin = defaultDateMin;
    }

    public DateTime getDefaultDateMax() {
        return defaultDateMax;
    }

    public void setDefaultDateMax(DateTime defaultDateMax) {
        this.defaultDateMax = defaultDateMax;
    }

}
