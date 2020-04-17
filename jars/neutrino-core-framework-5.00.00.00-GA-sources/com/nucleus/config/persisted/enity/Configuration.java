/*
 * 
 */
package com.nucleus.config.persisted.enity;

import java.util.Arrays;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.config.persisted.vo.ValueType;
import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import javax.persistence.Column;
/**
 * The Configuration entity class. This would hold a configuration property record for a particular entity.
 * 
 * @author Nucleus Software Exports Limited
 */
@Entity
@DynamicUpdate
@DynamicInsert
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Synonym(grant="ALL")
@Table(indexes={@Index(name="propertyKey_index",columnList="propertyKey"),@Index(name="config_group_fk_index",columnList="configuration_group_fk")})
public class Configuration extends BaseEntity {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -8658013138548563067L;
    
    public  static final String JAP_LOCALE = "ja_JP";

    public static final String NOTIFY_EVENT="notifyEvents"; 
    public static final String NOTIFY_EVENT_FOR_LIST="Notifications Enabled for"; 
    public static final String GET_NOTIFICATION_EVENT_QUERY="Configuration.getPropertyValueFromPropertyKey";
    public static final String PACKAGE_NAME="com.nucleus.*";
    
    public static final String CHAT_ENABLED                                   = "CHAT_ENABLED";
    
    public static final String DASHBOARD_COMMENT_WIDGET                       = "config.dashboard.commentWidget";
    
    public static final String DASHBOARD_STREAM_WIDGET                        = "config.dashboard.streamWidget";
    
    public static final String DASHBOARD_APP_COUNT_BY_PRODUCT_TYPE_WIDGET     = "config.dashboard.appCountByProductTypeWidget";
    
    public static final String DASHBOARD_APP_COUNT_BY_STAGE_WIDGET            = "config.dashboard.appCountByStageWidget";
    
    public static final String DASHBOARD_NOTES_WIDGET                         = "config.dashboard.notesWidget";
    
    public static final String DASHBOARD_LEAD_COUNT_BY_CITY_WIDGET            = "config.dashboard.leadCountByCityWidget";
    
    public static final String DASHBOARD_LEAD_COUNT_BY_CONVERSION_WIDGET      = "config.dashboard.leadCountByConversionWidget";
    
    public static final String DASHBOARD_LEAD_COUNT_BY_TAT_WIDGET             = "config.dashboard.leadCountByTatWidget";
    
    public static final String DASHBOARD_LEAD_COUNT_BY_DUE_TODAY_WIDGET       = "config.dashboard.leadCountByDueTodayWidget";
    
    public static final String DASHBOARD_LEAD_COUNT_BY_STATUS_WIDGET          = "config.dashboard.leadCountByStatusWidget";
    
    public static final String DASHBOARD_RECENT_MAILS                         = "config.dashboard.recentMails";
    
    public static final String TAB_PREFERRED_TAB_STATUS                       = "config.tab.preferredTabStatus";
    
    public static final String NOTIFICATIONS_MAX_NUMBER                       = "config.notifications.maxNumber";
    
    public static final String NOTIFICATIONS_DURATION                         = "config.notifications.duration";
    
    public static final String NOTIFICATIONS_TIMER                            = "config.notifications.timer";
     
    public static final String NOTIFICATIONS_MY_FAVORITES                     = "config.notifications.myFavorites";
    
    public static final String USER_LOCALE                                    = "config.user.locale";
     
    public static final String DATE_FORMATS                                   = "config.date.formats";
    
    public static final String USERLOCK_COOL_OFF_HOURS                        = "config.userLock.coolOffHours";
    
    public static final String NOTIFICATIONS_KEEP_NOTIFICATIONS               = "config.notifications.keepNotifications";
    
    public static final String SIDEBAR_GADGET_TODO_MAX_NUMBER                 = "config.sidebar.gadget.toDo.maxNumber";
    
    public static final String USER_TIME_ZONE                                 = "config.user.time.zone";
    
    public static final String USER_DATE_MAXIMUM_YEAR                         = "config.user.date.maximum.year";
    
    public static final String EXCEPTIONS_REMOVE_LOG                          = "config.exceptions.remove.log";
    
    public static final String NOTIFICATIONS_ACCUMULATE                       = "config.notifications.accumulate";
    
    public static final String NOTIFICATIONS_TOOLTIP                          = "config.notifications.tooltip";
    
    public static final String NOTIFICATIONS_SHOW                             = "config.notifications.show";
    
    public static final String NOTIFICATIONS_SEND_SMTP_NOTIFICATIONS          = "config.notifications.sendSMTPNotifications";
     
    public static final String NOTIFICATIONS_SEND_INTERNAL_MAIL_NOTIFICATIONS = "config.notifications.sendInternalEmailNotifications";
    
    public static final String APPLICATION_SCORE_CARD_IS_MOCK                 = "config.applicationScoreCard.isMock";
     
    public static final String USER_CORPORATE_MAILS_ENABLED                   = "config.user.corporateMails.enabled";
    
    public static final String UI_DIRECTION                                   = "config.ui.direction";
    
    public static final String THEME_PREFERRED_THEME                          = "config.theme.preferredTheme";
    
    public static final String LOGGED_IN_USERS                                = "loggedinUsers";
    
    public static final String PREFERENCES                                    = "preferences";
    
    public static final String USER_LIST                                      = "userList";
    
    public static final String IS_CDN_ENABLED                                      = "system.config.cdn.enabled";
    
    public static final String CDN_URL                                      = "system.config.cdn.url";
    
    public static final String IS_PAGE_REFRESH_ENABLED                      = "system.config.page.refresh.enabled";
    
    public static final String IMEI_LENGTH                   				= "config.imei.length";
    public static final String FCMID_LENGTH                                  = "config.fcmId.length";
   
    public static final String MEID_LENGTH                   				= "config.meid.length";

    public static final String APPLICATION_GRID_DEFAULT_TAB                 = "config.applicationGrid.defaultTab";

    public static final String LEAD_GRID_DEFAULT_TAB                        = "config.leadGrid.defaultTab";

    public static final String CREDIT_APPROVAL_GRID_DEFAULT_TAB             = "config.creditApprovalGrid.defaultTab";
    
    public static List<String> NORMAL_TEXT_CONFIG_LIST                      = Arrays.asList(TAB_PREFERRED_TAB_STATUS, NOTIFICATIONS_MAX_NUMBER,
                                                                                               NOTIFICATIONS_DURATION, NOTIFICATIONS_TIMER,
                                                                                               NOTIFICATIONS_MY_FAVORITES, USER_LOCALE, DATE_FORMATS,
                                                                                               USERLOCK_COOL_OFF_HOURS, NOTIFICATIONS_KEEP_NOTIFICATIONS,
                                                                                               SIDEBAR_GADGET_TODO_MAX_NUMBER, USER_TIME_ZONE,
                                                                                               USER_DATE_MAXIMUM_YEAR, EXCEPTIONS_REMOVE_LOG, UI_DIRECTION,
                                                                                               IMEI_LENGTH, MEID_LENGTH,APPLICATION_GRID_DEFAULT_TAB,
                                                                                               LEAD_GRID_DEFAULT_TAB, CREDIT_APPROVAL_GRID_DEFAULT_TAB );
    
    public static List<String> BOOLEAN_VALUE_CONFIG_LIST                      = Arrays.asList(NOTIFICATIONS_ACCUMULATE, NOTIFICATIONS_TOOLTIP,
                                                                                               NOTIFICATIONS_SHOW, NOTIFICATIONS_SEND_SMTP_NOTIFICATIONS,
                                                                                               NOTIFICATIONS_SEND_INTERNAL_MAIL_NOTIFICATIONS,
                                                                                               USER_CORPORATE_MAILS_ENABLED, APPLICATION_SCORE_CARD_IS_MOCK);
    public static final String DEFAULT_TENANT_CONFIG_KEY="default.tenant.id";
    public static final String CUSTOM_MOBILE_VALIDATION="config.custom.mobile.validation";
    public static final String ALLOW_INVALID_PHONE_NUMBER="config.allow.invalid.number";

    public static final String SQL_MAX_RUN_TIME = "sqlMaxRunTime";

    public static final String REQ_ALL_FIELDS_PREF = "req.UserPreferences.Json";    
    /** The property key. */
    private String            propertyKey;

    /** The property value. */
    @Column(length = 4000)
    private String            propertyValue;

    /** The value type. */
    private String            valueType;

    /** Whether a normal user can override this property  */
    private boolean           userModifiable;

    public boolean isUserModifiable() {
        return userModifiable;
    }

    public void setUserModifiable(boolean userModifiable) {
        this.userModifiable = userModifiable;
    }

    /**
     * Instantiates a new base configuration.
     *
     */
    public Configuration() {
    }

    /**
     * Gets the property key.
     *
     * @return the property key
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * Gets the property value.
     *
     * @return the property value
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * Gets the value type.
     *
     * @return the value type
     */
    public ValueType getValueType() {
        if (StringUtils.isBlank(valueType)) {
            return null;
        } else {
            return ValueType.valueOf(valueType);
        }
    }

    /**
     * Sets the property key.
     *
     * @param propertyKey the new property key
     */
    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * Sets the property value.
     *
     * @param propertyValue the new property value
     */
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    /**
     * Sets the value type.
     *
     * @param valueType the new value type
     */
    public void setValueType(ValueType valueType) {
        this.valueType = valueType.toString();
    }


   @Transient
	private String[] eventsValue;

public String[] getEventsValue() {
	return eventsValue;
}

public void setEventsValue(String[] eventsValue) {
	this.eventsValue = eventsValue;
}

	

	
	
}
