package com.nucleus.core.mutitenancy;

import java.io.IOException;
import java.util.Properties;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

import com.nucleus.logging.BaseLoggers;

public class CustomSchemaResolver implements CurrentTenantIdentifierResolver {

    public static final String CAS_SCHEMA = "CAS_SCHEMA";
    public static final String LMS_SCHEMA = "LMS_SCHEMA";
    public static Properties prop  = new Properties(); 
    static{
        try {
            prop.load(ConnectionProviderUtil.class.getClassLoader().getResourceAsStream("\\neutrino-product-suite-config\\common-config\\core-framework-config\\database-config.properties"));
        } catch (IOException e) {
            BaseLoggers.persistenceLogger.error("error occured while loading properties" + e.getMessage());
        }
    }
    @Override
    public String resolveCurrentTenantIdentifier() {
        String schemaTobeUsed = prop.getProperty("database.multi.tenancy.connection.schema.schematobeused");
        BaseLoggers.persistenceLogger.info("resolved schema identifier :" + schemaTobeUsed);
        return schemaTobeUsed;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return false;
    }
}