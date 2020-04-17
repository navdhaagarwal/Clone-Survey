package com.nucleus.regional.metadata;

import java.util.Map;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.regional.metadata.service.RegionalMetaDataService;

@Named("regionalMetaDataListener")
public class RegionalMetaDataListener implements ServletContextListener {

    private static ApplicationContext appCtx;

    public void setApplicationContext(ServletContext servletConext){
		appCtx =WebApplicationContextUtils.getRequiredWebApplicationContext(servletConext);
	}


    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	setApplicationContext(sce.getServletContext());
        RegionalMetaDataService regionalMetaDataService = (RegionalMetaDataService) appCtx.getBean("regionalMetaDataService");
        RegionalMetaDataProcessingBean  regionalMetaDataProcessingBean= (RegionalMetaDataProcessingBean)appCtx.getBean("regionalMetaDataProcessingBean");
        try {
        	
        	Map<String, Object> regionalMetaDataMap = regionalMetaDataService.getRegionalMetaData();
        	regionalMetaDataProcessingBean.setRegionalMetaDataMap(regionalMetaDataMap);
            BaseLoggers.flowLogger.debug("Meta Data Map at context initialization -->"+regionalMetaDataMap);

            sce.getServletContext().setAttribute("regionalMetaDataMapContext", regionalMetaDataMap);
           
            BaseLoggers.flowLogger.debug("Regional MetaData loaded for application");
            
          
        } catch (Exception e) {
            BaseLoggers.exceptionLogger.debug("Exception occured at Regional MetaData Listener : " + e);
        }
    }
    

    
    public void contextDestroyed(ServletContextEvent sce) {
        BaseLoggers.flowLogger.debug("Regional MetaData contextDestroyed()");
    }

}
