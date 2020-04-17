package com.nucleus.contact;

import javax.persistence.Embeddable;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Class to hold properties for
 *          preferred email type
 *          Subscription mode
 */

@Embeddable
public class StmntSubsrptnPreference {

    public static final String STMNT_SUBSCRPTN_MODE_EMAIL            = "EMAIL";
    public static final String STMNT_SUBSCRPTN_MODE_MOBILE           = "MOBILE";
    public static final String STMNT_SUBSCRPTN_MODE_EMAIL_AND_MOBILE = "EMAIL_AND_MOBILE";

    public static final String PREF_EMAIL_TYPE_PERSONAL              = "PERSONAL";
    public static final String PREF_EMAIL_TYPE_OFFICIAL              = "OFFICIAL";
    public static final String PREF_EMAIL_TYPE_PERSONAL_AND_OFFICIAL = "PERSONAL_AND_OFFICIAL";

    /**
     * Below field indicates the communication mode used by the customer to get the statements. (for e.g credit card.)
     * possible values : EMAIL, MOBILE, EMAIL_AND_MOBILE. 
     */
    private String             statementSubscriptionMode;

    /**
     * Below field shows the type of email saved for receiving statement.
     * possible values : PERSONAL, OFFICIAL,  PERSONAL_AND_OFFICIAL
     */
    private String             preferredEmailType;

    public String getStatementSubscriptionMode() {
        return statementSubscriptionMode;
    }

    public void setStatementSubscriptionMode(String statementSubscriptionMode) {
        this.statementSubscriptionMode = statementSubscriptionMode;
    }

    public String getPreferredEmailType() {
        return preferredEmailType;
    }

    public void setPreferredEmailType(String preferredEmailType) {
        this.preferredEmailType = preferredEmailType;
    }

}
