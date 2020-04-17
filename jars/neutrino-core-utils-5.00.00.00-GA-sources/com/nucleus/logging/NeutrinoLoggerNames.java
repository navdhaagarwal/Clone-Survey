package com.nucleus.logging;

import com.nucleus.core.exceptions.SystemException;

/**
 * Class that has all module logger names at one place. Using these names, any product built over Neutrino platform can leverage the same names for loggers.<br/>
 * Class level loggers (with class name as logger name) are not encouraged unless there is a very specific need. <br/>
 * Also see {@link BaseLoggers} class which already have all the logger objects created out of these logger names.
 *
 * @author Nucleus Software Exports Limited
 */
public interface NeutrinoLoggerNames {

    /**
     * Logger for Neutrino event framework related logging.
     */
    public static final String EVENT_LOGGER                 = "com.nucleus.event";

    /**
     * Logger for logging flow of application on info and debug mode. This will be one of the most widely used loggers and will be useful to debug low
     * level API functioning.
     */
    public static final String FLOW_LOGGER                  = "com.nucleus.control";

    /**
     * Logger for logging exceptions in the system which should be reported to administrator. This particularly denotes third party API exceptions
     * , {@link SystemException} and its sub classes.
     */
    public static final String EXCEPTION_LOGGER             = "com.nucleus.exception";

    /**
     * Logger for logging messages which are related to persistence layer related APIs.
     */
    public static final String PERSISTENCE_LOGGER           = "com.nucleus.persistence";

    /**
     * Logger for logging messages which are related to workflow related APIs in the system.
     */
    public static final String WORKFLOW_LOGGER              = "com.nucleus.workflow";

    /**
     * Logger for logging messages which are related to security in the system. This includes security events and breaches and also the flow of security related APIs.
     */
    public static final String SECURITY_LOGGER              = "com.nucleus.security";

    /**
     * Logger to be plugged wherever we need to measure the performance of the system.
     */
    public static final String PERFORMANCE_LOGGER           = "com.nucleus.performance";

    /**
     * Logger related to web layer related APIs.
     */
    public static final String WEB_LOGGER                   = "com.nucleus.web";

    /**
     * Logger related to accessing the application.
     */
    public static final String ACCESS_LOGGER                = "com.nucleus.access";

    /**
     * Logger which should be used to dump information related to product configuration and environment in which the product is running. This will be essentially
     * used at the time of product initialization on server start up.
     */
    public static final String PRODUCT_INFO_LOGGER          = "com.nucleus.product.info";

    /**
     * Logger for rule execution related information.
     */
    public static final String RULE_LOGGER                  = "com.nucleus.rule";

    /**
     * Very important logger which should be used for logging exceptions which denotes possible bugs in the system. For example, you are developing a class in which you are reading from an input
     * stream and it is 100% sure that input stream will not be null and will contain data. Therefore in the IOException catch block of input stream reading method, you may catch the exception
     * (which should ideally never happen) and log it using this logger. By logging such exceptions into separate files, we know that in which situation, the code should have some more worst case
     * scenario handling. <br/>
     * <b>Note:</b> Do not use this logger unless you exactly understand what it is meant for.
     */
    public static final String BUG_LOGGER                   = "com.nucleus.bug";

    /**
     * Logger for integration calls.
     */
    public static final String INTEGRATION_LOGGER           = "com.nucleus.integration";

    /**
     * Logger used for jobs that are scheduled through quartz
     */
    public static final String QUARTZ_JOB_LOGGER            = "com.nucleus.core.scheduler";

    /**
     * Logger used for jobs that are scheduled through quartz
     */
    public static final String CONVERSATIONAL_LOGGER        = "com.nucleus.conversation";
    
    
    public static final String API_MANAGEMENT_LOGGER        = "com.nucleus.api.management";
    

    /**
     * Logger used for jobs that are scheduled through quartz
     */
    public static final String GENERIC_CREDIT_BUREAU_LOGGER = "com.nucleus.creditBureau.generic";
	
	/**
     * Logger for logging error message related to master data's fields while showing on grid data table.
     */
    public static final String MASTER_DATA_GRID_ERROR_LOGGER = "com.nucleus.makerchecker.GridDataUtility";
    
    /**
     * Logger for logging data related to dynamic workflow.
     */
    public static final String DYNAMIC_WORKFLOW_LOGGER = "com.nucleus";
    /**
     * Logger for logging data related to STP workflow activity.
     */
    public static final String STP_WORKFLOW_LOGGER = "com.nucleus.workflow.stp";
    
    /**
     * Logger for logging data related to encryption activity.
     */
    public static final String ENCRYPTION_LOGGER = "com.nucleus.encryption";

}