/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 */
package com.nucleus.core.messageSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.nucleus.logging.BaseLoggers;

/**
 *  This class will load all the message Resource into cached map on server start up
 * @author Nucleus Software Exports Limited
 */
public class DBDrivenMessageResourceLoader implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    DatabaseDrivenMessageSource databaseDrivenMessageSource;

    ApplicationContext          applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        this.applicationContext = arg0;
        databaseDrivenMessageSource = (DatabaseDrivenMessageSource) applicationContext.getBean("messageSource");
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent arg0) {
        BaseLoggers.webLogger.warn("Message Resource Loading start");
        databaseDrivenMessageSource.reload();
    }

}
