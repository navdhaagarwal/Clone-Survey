package com.nucleus.reason;

import com.nucleus.core.genericparameter.entity.GenericParameter;

import javax.persistence.Entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;


@Entity
@DynamicUpdate
@DynamicInsert
public class BlockReason extends GenericParameter {

    private static final long serialVersionUID = 1L;
    public static final String REASON_BLOCK = "Block User";
    public static final String Dormancy  = "Dormancy";
    public static final String Initial_Dormancy  = "Initial_Dormancy";
    public static final String Failed_password_attempts  = "Failed_password_attempts";
    public static final String Failed_reset_password_attempts  = "Failed_reset_password_attempts";
   
    public static final String Department_Change  = "Department_Change";
    public static final String HR_Exit_Temporary  = "HR_Exit-Temporary";
    
    public static final String FAILED_OTP_ATTEMTS  ="max_failed_OTP_attempts";
    public static final String MAX_OTP_SEND_ATTEMPTS="max_OTP_send_attempts";

    private int                levelInHierarchy;

    public int getLevelInHierarchy() {
        return levelInHierarchy;
    }

    public void setLevelInHierarchy(int level) {
        this.levelInHierarchy = level;
    }


}