package com.nucleus.exceptionLogging;

import java.util.ArrayList;
import java.util.List;

import com.nucleus.core.genericparameter.entity.GenericParameter;

@SuppressWarnings("serial")
public class ExceptionLoggingConstants {

    public static final String                 USER_BASED_FILTER      = "User";
    public static final String                 EXCEPTION_BASED_FILTER = "Exception";
    public static final String                 APPLICATION_BASED_FILTER = "Application";

    public static final List<Integer>          DAYS_LIST              = new ArrayList<Integer>() {
                                                                          {
                                                                              add(10);
                                                                              add(20);
                                                                              add(30);
                                                                          }
                                                                      };

    public static final List<GenericParameter> BASED_ON_FILTER        = new ArrayList<GenericParameter>();
    static {
        GenericParameter userBasedGenericParam = new GenericParameter() {
            {
                setCode(USER_BASED_FILTER);
                setName(USER_BASED_FILTER);
            }
        };
        GenericParameter exceptionBasedGenericParam = new GenericParameter() {
            {
                setCode(EXCEPTION_BASED_FILTER);
                setName(EXCEPTION_BASED_FILTER);
            }
        };
        GenericParameter applicationBasedGenericParam = new GenericParameter() {
            {
                setCode(APPLICATION_BASED_FILTER);
                setName(APPLICATION_BASED_FILTER);
            }
        };
        BASED_ON_FILTER.add(userBasedGenericParam);
        BASED_ON_FILTER.add(exceptionBasedGenericParam);
        BASED_ON_FILTER.add(applicationBasedGenericParam);
    }

}
