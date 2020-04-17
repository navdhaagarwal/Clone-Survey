package com.nucleus.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central class to hold all module level logger instances at one place.<br/>
 * Class level loggers (with class name as logger name) are not encouraged unless there is a very specific need. <br/>
 * Also see {@link NeutrinoLoggerNames} class for explanation of each logger.
 * @author Nucleus Software Exports Limited
 */
public interface BaseLoggers {

    /**
     * See {@link NeutrinoLoggerNames#EVENT_LOGGER}
     */
    public static final Logger eventLogger               = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.EVENT_LOGGER)) ;

    /**
     * See {@link NeutrinoLoggerNames#FLOW_LOGGER}
     */
    public static final Logger flowLogger                = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.FLOW_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#EXCEPTION_LOGGER}
     */
    public static final Logger exceptionLogger           = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.EXCEPTION_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#PERSISTENCE_LOGGER}
     */
    public static final Logger persistenceLogger         = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.PERSISTENCE_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#WORKFLOW_LOGGER}
     */
    public static final Logger workflowLogger            = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.WORKFLOW_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#SECURITY_LOGGER}
     */
    public static final Logger securityLogger            = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.SECURITY_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#PERFORMANCE_LOGGER}
     */
    public static final Logger performanceLogger         = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.PERFORMANCE_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#WEB_LOGGER}
     */
    public static final Logger webLogger                 = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.WEB_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#ACCESS_LOGGER}
     */
    public static final Logger accessLogger              = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.ACCESS_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#PRODUCT_INFO_LOGGER}
     */
    public static final Logger productInfoLogger         = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.PRODUCT_INFO_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#BUG_LOGGER}
     */
    public static final Logger bugLogger                 = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.BUG_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#INTEGRATION_LOGGER}
     */
    public static final Logger integrationLogger         = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.INTEGRATION_LOGGER));

    /**
     * See {@link NeutrinoLoggerNames#API_MANAGEMENT_LOGGER}
     */
    public static final Logger apiManagementLogger      = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.FLOW_LOGGER));

    
    
    public static final Logger quartzJobLogger           = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.QUARTZ_JOB_LOGGER));

    public static final Logger creditBureauGenericLogger = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory
            .getLogger(NeutrinoLoggerNames.GENERIC_CREDIT_BUREAU_LOGGER));

    public static final Logger conversationalLogger      = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory
            .getLogger(NeutrinoLoggerNames.CONVERSATIONAL_LOGGER));
	
    public static final Logger masterDataLogger      = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.MASTER_DATA_GRID_ERROR_LOGGER));
    
    public static final Logger dynamicWorkflowLogger  = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.DYNAMIC_WORKFLOW_LOGGER));
    
    public static final Logger stpWorkflowLogger      = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.STP_WORKFLOW_LOGGER));

    public static final Logger encryptionLogger = (Logger) SLF4JLoggerProxy.newInstance(LoggerFactory.getLogger(NeutrinoLoggerNames.EXCEPTION_LOGGER));
    
}
