/*
 * 
 */
package com.nucleus.config.persisted.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nucleus.config.persisted.enity.Configuration;
import com.nucleus.entity.EntityId;

/**
 * The ConfigurationVO value object class. An object of this class would hold all the data to communicate
 * between the presentation and the controller/service layers.
 * 
 * @author Nucleus Software Exports Limited
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigurationVO extends Configuration {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6259102571508875088L;

    /** The text. */
    private String            text;

    /** The date. */
    private Date              date;

    /** The from date. */
    private Date              fromDate;

    /** The to date. */
    private Date              toDate;

    /** The day. */
    private String            day;

    /** The from day. */
    private String            fromDay;

    /** The to day. */
    private String            toDay;

    /** The configurable. */
    private Boolean           configurable;

    /** The entity uri. */
    private String            associatedEntityUri;

    /**Override Flag */
    private Boolean           override;

    private String            commentWidget;

    private String            streamWidget;

    private String            appCountByProductTypeWidget;

    private String            appCountByStageWidget;

    private String            notesWidget;

    private String            leadCountByCityWidget;
    
    private String            leadCountByConversionWidget;
    
    private String            leadCountByTatWidget;
    
    private String            leadCountByDueTodayWidget;
    
    private String            leadCountByStatusWidget;
    
    private String            recentMails;

    private String              label;

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * Gets the from date.
     *
     * @return the fromDate
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * Gets the to date.
     *
     * @return the toDate
     */
    public Date getToDate() {
        return toDate;
    }

    /**
     * Gets the day.
     *
     * @return the day
     */
    public String getDay() {
        return day;
    }

    /**
     * Gets the from day.
     *
     * @return the fromDay
     */
    public String getFromDay() {
        return fromDay;
    }

    /**
     * Gets the to day.
     *
     * @return the toDay
     */
    public String getToDay() {
        return toDay;
    }

    /**
     * Sets the text.
     *
     * @param text the text to set
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Sets the date.
     *
     * @param date the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Sets the from date.
     *
     * @param fromDate the fromDate to set
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * Sets the to date.
     *
     * @param toDate the toDate to set
     */
    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    /**
     * Sets the day.
     *
     * @param day the day to set
     */
    public void setDay(String day) {
        this.day = day;
    }

    /**
     * Sets the from day.
     *
     * @param fromDay the fromDay to set
     */
    public void setFromDay(String fromDay) {
        this.fromDay = fromDay;
    }

    /**
     * Sets the to day.
     *
     * @param toDay the toDay to set
     */
    public void setToDay(String toDay) {
        this.toDay = toDay;
    }

    /**
     * Gets the entity uri.
     *
     * @return the entity uri
     */
    public EntityId getAssociatedEntityId() {
        return EntityId.fromUri(associatedEntityUri);
    }

    /**
     * Sets the entity uri.
     *
     * @param entityUri the new entity uri
     */
    public void setAssociatedEntityId(EntityId entityId) {
        this.associatedEntityUri = entityId.getUri();
    }

    /**
     * Gets the configurable.
     *
     * @return the boolean
     */
    public Boolean isConfigurable() {
        return getConfigurable();
    }

    /**
     * Sets the configurable.
     *
     * @param configurable the new configurable
     */
    public void setConfigurable(Boolean configurable) {
        this.configurable = configurable;
    } 

    /**
     * Gets the configurable.
     *
     * @return the boolean
     */
    public Boolean getConfigurable() {
        return configurable;
    }

    /**
     * Gets the override.
     *
     * @param configurable the new configurable
     */
    public Boolean isOverride() {
        return override;
    }

    /**
     * Sets the override.
     *
     * @param configurable the new configurable
     */
    public void setOverride(Boolean override) {
        this.override = override;
    }

    /**
     * Gets the override.
     *
     * @return the boolean
     */
    public Boolean getOverride() {
        return override;
    }

    public String getCommentWidget() {
        return commentWidget;
    }

    public void setCommentWidget(String commentWidget) {
        this.commentWidget = commentWidget;
    }

    public String getStreamWidget() {
        return streamWidget;
    }

    public void setStreamWidget(String streamWidget) {
        this.streamWidget = streamWidget;
    }

    public String getAppCountByStageWidget() {
        return appCountByStageWidget;
    }

    public void setAppCountByStageWidget(String appCountByStageWidget) {
        this.appCountByStageWidget = appCountByStageWidget;
    }

    public String getNotesWidget() {
        return notesWidget;
    }

    public void setNotesWidget(String notesWidget) {
        this.notesWidget = notesWidget;
    }

    public String getAppCountByProductTypeWidget() {
        return appCountByProductTypeWidget;
    }

    public void setAppCountByProductTypeWidget(String appCountByProductTypeWidget) {
        this.appCountByProductTypeWidget = appCountByProductTypeWidget;
    }

    public String getLeadCountByCityWidget() {
        return leadCountByCityWidget;
    }

    public void setLeadCountByCityWidget(String leadCountByCityWidget) {
        this.leadCountByCityWidget = leadCountByCityWidget;
    }

    public String getLeadCountByConversionWidget() {
        return leadCountByConversionWidget;
    }

    public void setLeadCountByConversionWidget(String leadCountByConversionWidget) {
        this.leadCountByConversionWidget = leadCountByConversionWidget;
    }

    public String getLeadCountByTatWidget() {
        return leadCountByTatWidget;
    }

    public void setLeadCountByTatWidget(String leadCountByTatWidget) {
        this.leadCountByTatWidget = leadCountByTatWidget;
    }

    public String getLeadCountByDueTodayWidget() {
        return leadCountByDueTodayWidget;
    }

    public void setLeadCountByDueTodayWidget(String leadCountByDueTodayWidget) {
        this.leadCountByDueTodayWidget = leadCountByDueTodayWidget;
    }

    public String getLeadCountByStatusWidget() {
        return leadCountByStatusWidget;
    }

    public void setLeadCountByStatusWidget(String leadCountByStatusWidget) {
        this.leadCountByStatusWidget = leadCountByStatusWidget;
    }

    public String getRecentMails() {
        return recentMails;
    }

    public void setRecentMails(String recentMails) {
        this.recentMails = recentMails;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
