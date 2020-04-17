package com.nucleus.core.mutitenancy;

import static com.nucleus.core.mutitenancy.CustomSchemaResolver.CAS_SCHEMA;
import static com.nucleus.core.mutitenancy.CustomSchemaResolver.LMS_SCHEMA;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.nucleus.logging.BaseLoggers;

public class ConnectionProviderUtil {

    private static Properties prop = new Properties();

    public static Map<String, ConnectionProvider> PrepareConnectionProviders() {

        try {
            prop.load(ConnectionProviderUtil.class.getClassLoader().getResourceAsStream(
                    "\\neutrino-product-suite-config\\common-config\\core-framework-config\\database-config.properties"));
        } catch (IOException e) {
            BaseLoggers.persistenceLogger.error("error occured while loading properties" + e.getMessage());
        }

        Map<String, ConnectionProvider> connectionProviders = new HashMap<String, ConnectionProvider>();
        ComboPooledDataSource casds = createNewComboPooledDataSourcewithGivenName("casds",
                prop.getProperty("database.multi.tenancy.connection.schema.cas"));
        DatasourceConnectionProviderImpl casProvider = new DatasourceConnectionProviderImpl();
        casProvider.setDataSource(casds);
        casProvider.configure(getConnectionProperties(prop.getProperty("database.multi.tenancy.connection.schema.cas")));

        ComboPooledDataSource lmsds = createNewComboPooledDataSourcewithGivenName("lmsds",
                prop.getProperty("database.multi.tenancy.connection.schema.lms"));
        DatasourceConnectionProviderImpl lmsProvider = new DatasourceConnectionProviderImpl();
        lmsProvider.setDataSource(lmsds);
        lmsProvider.configure(getConnectionProperties(prop.getProperty("database.multi.tenancy.connection.schema.lms")));

        connectionProviders.put(CAS_SCHEMA, casProvider);
        connectionProviders.put(LMS_SCHEMA, lmsProvider);
        return connectionProviders;

    }

    private static Properties getConnectionProperties(String schemaName) {
        Properties properties = new Properties();
        properties.put(AvailableSettings.DIALECT, prop.getProperty("hibernate.dialect"));
        properties.put(AvailableSettings.DRIVER, prop.getProperty("database.driver.class"));
        properties.put(AvailableSettings.URL, prop.getProperty("database.multi.tenancy.connection.dburl") + schemaName);
        properties.put(AvailableSettings.USER, prop.getProperty("database.username"));
        properties.put(AvailableSettings.PASS, prop.getProperty("database.password"));
        return properties;

    }

    private static ComboPooledDataSource createNewComboPooledDataSourcewithGivenName(String dataSourcename, String schemaName) {
        ComboPooledDataSource cds = new ComboPooledDataSource(dataSourcename);
        try {
            cds.setDriverClass(prop.getProperty("database.driver.class"));
        } catch (PropertyVetoException e) {
            BaseLoggers.persistenceLogger.error("error occured while loading properties" + e.getMessage());
        }
        String dbonlyURL = prop.getProperty("database.multi.tenancy.connection.dburl");
        cds.setJdbcUrl(dbonlyURL + schemaName);
        cds.setUser(prop.getProperty("database.username"));
        cds.setPassword(prop.getProperty("database.password"));
        return cds;
    }
}
