package com.nucleus.core.dynamicform.exception;

import com.nucleus.finnone.pro.base.exception.BaseException;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;

/**
 * Created by gajendra.jatav on 8/30/2019.
 */
public class InvalidDynamicFormDataException extends BaseException {


    public static enum ERROR_CODE {

        INVALID_AMOUNT_FORMAT("fmsg.00011340"),
        INVALID_CHECKBOX_VALUE("fmsg.00011341"), INVALID_MOBILE_NUM("fmsg.00011342"), INVALID_MOBILE_NUM_FORMAT("fmsg.00011343"),
        INVALID_PHONE_NUM_STD_CODE("fmsg.00011344"), INVALID_PHONE_NUM_FORMAT("fmsg.00011345"), INVALID_PHONE_NUM_EXT("fmsg.00011346"),
        VALUE_NOT_FOUND_IN_SYSTEM("fmsg.00011347"), INVALID_DATE_FORMAT("fmsg.00011348"), DATE_OUT_OF_ALLOWED_RANGE("fmsg.00011349"),
        MANDATORY_FIELD_MISSING("fmsg.00011350"), FIELD_VALUE_LESS_THAN_MIN("fmsg.00011351"), FIELD_VALUE_MORE_THAN_MAX("fmsg.00011352"),
        INVALID_NUMBER_FORMAT("fmsg.00011353"), MULTIPLE_VALUES_EXPECTED("fmsg.00011354"), INVALID_MASTER_MAINTENANCE("fmsg.00011355");

        private String messageCode;

        ERROR_CODE(String messageCode) {
            this.messageCode = messageCode;
        }

        public String getMessageCode() {
            return messageCode;
        }
    }

    public InvalidDynamicFormDataException(String message) {
        super(message);
    }

    public InvalidDynamicFormDataException(String message, Throwable cause) {
        super(message, cause);
    }

}
