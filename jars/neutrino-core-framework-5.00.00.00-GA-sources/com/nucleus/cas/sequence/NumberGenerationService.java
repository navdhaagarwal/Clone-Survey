/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.cas.sequence;

import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("casSequenceService")
@DependsOn({"defaultCASSequenceService","iciciSequenceService","regionalSequenceService"})
public class NumberGenerationService implements FactoryBean<CasSequenceService>, ApplicationContextAware {

    private ApplicationContext appContext;

    @Value("${application.numbergeneration.impl}")
    private String             numberGenerationImpl;

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appContext = applicationContext;
    }

    @Override
    public CasSequenceService getObject() throws Exception {
        return appContext.getBean(numberGenerationImpl, CasSequenceService.class);
    }

    @Override
    public Class<?> getObjectType() {
        return CasSequenceService.class;
    }

}
