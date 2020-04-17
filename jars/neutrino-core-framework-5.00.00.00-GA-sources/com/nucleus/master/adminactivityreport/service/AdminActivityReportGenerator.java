package com.nucleus.master.adminactivityreport.service;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.EntityId;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.adminactivityreport.util.AdminActivityReportHelper;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.User;
import com.nucleus.user.UserService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

@Component("adminActivityReportGenerator")
public class AdminActivityReportGenerator extends BaseServiceImpl implements ApplicationContextAware {

    private ApplicationContext apctx;

	public void generateAdminActivityReportAndSave(BaseEntity oldEntity, BaseEntity newEntity,
                                                   Class entityClass, String lastUpdatedByUri) {
		try {

            AdminActivityReportHelper adminHelper = getActivityReportHelperInstance(entityClass);
			if(adminHelper!=null && entityClass == User.class){
                adminHelper.processReport(oldEntity,newEntity);
            }

		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in Generating Admin Activity Report for Master : " + entityClass, e);
		}

	}

    public AdminActivityReportHelper getActivityReportHelperInstance(Class entityClass) throws Exception {
        if (entityClass != null) {
            try {
                Object bean = this.apctx.getBean(entityClass.getSimpleName().toLowerCase() + "AdminReportHelper");
                if(bean!=null && bean instanceof AdminActivityReportHelper){
                    return (AdminActivityReportHelper) bean;
                } else {
                    BaseLoggers.exceptionLogger
                            .error("Bean Found But is of Not Desired type (extends class AdminActivityReportHelper)"
                                    + bean.getClass().getSimpleName());
                }
            } catch (NoSuchBeanDefinitionException e) {
                //BaseLoggers.exceptionLogger.error("No bean found for Admin activity report for : " + entityClass, e);
            }
        }
        return null;
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.apctx = applicationContext;
    }


}
