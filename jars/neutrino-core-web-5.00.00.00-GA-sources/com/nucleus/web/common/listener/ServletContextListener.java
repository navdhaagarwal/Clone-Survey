package com.nucleus.web.common.listener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;

import org.apache.commons.lang3.RandomStringUtils;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.base.utility.CoreUtility;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.logging.BaseLoggers;

/**
 * A servlet that plugs into the servlet lifecycle to call startup and shutdown hooks.
 */
public class ServletContextListener implements javax.servlet.ServletContextListener {

    private static final String MESSAGE_STRING = "\n" + RandomStringUtils.random(100, "-") + "\n" + "%s" + "\n"
                                                       + RandomStringUtils.random(100, "-");
    private static final String MESSAGE_EXCEPTION = "Error in method contextInitialized in ServletContextListener ";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
			if (ProductInformationLoader.productInfoExists()) {
			    BaseLoggers.flowLogger
			            .info(String.format(
			                    MESSAGE_STRING,
			                    "Neutrino "
			                            + ProductInformationLoader.getProductName()
			                            + " Web Application has been Successfully started. Copyright Nucleus Software Exports Limited (India)."));
			} else {
			    BaseLoggers.flowLogger.warn("THIS NEUTRINO WEB APPLICATION IS RUNNING WITHOUT '"
			            + ProductInformationLoader.PRODUCT_INFO_FILE_NAME
			            + "' FILE IN THE CLASSPATH. PLEASE ADD THE FILE AT THE CLASSPATH ROOT INSIDE WAR.");
			}
		} catch (Exception exception) {
			BaseLoggers.exceptionLogger.error(MESSAGE_EXCEPTION,exception);
			  throw ExceptionBuilder.getInstance(SystemException.class, MESSAGE_EXCEPTION, MESSAGE_EXCEPTION)
			  .setOriginalException(exception)
	          .setMessage(CoreUtility.prepareMessage(MESSAGE_EXCEPTION)).setSeverity(ExceptionSeverityEnum.SEVERITY_HIGH.getEnumValue()).build();
		}
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (ProductInformationLoader.productInfoExists()) {
            BaseLoggers.flowLogger.info(String.format(MESSAGE_STRING,
                    "Shutting down Neutrino " + ProductInformationLoader.getProductName() + " Web Application"));
        } else {
            BaseLoggers.flowLogger.warn("THIS NEUTRINO WEB APPLICATION WAS RUNNING WITHOUT '"
                    + ProductInformationLoader.PRODUCT_INFO_FILE_NAME
                    + "' FILE IN THE CLASSPATH. PLEASE ADD THE FILE AT THE CLASSPATH ROOT INSIDE WAR.");
        }

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                BaseLoggers.flowLogger.info("Deregistering jdbc driver : {}", driver);
            } catch (SQLException sqle) {
                BaseLoggers.flowLogger.error("Error deregistering jdbc driver : {}", driver, sqle);
            }
        }

    }

}
