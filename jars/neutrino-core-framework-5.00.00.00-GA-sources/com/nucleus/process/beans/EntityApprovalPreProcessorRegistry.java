package com.nucleus.process.beans;

import java.lang.annotation.Annotation;
import java.util.Properties;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.core.annotation.AnnotationUtils;

@Named("entityApprovalPreProcessorRegistry")
public class EntityApprovalPreProcessorRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    /*@Inject
    @Named("messageSource")
    protected MessageSource    messageSource;*/
    @Resource(name = "entityApprovalPreProcessorResourceLoader")
    private Properties entityApprovalPreProcessorResourceLoader;

    
   /*
    @Value("${com.nucleus.core.collateral.asset.master.AssetModel}")
    private String             ENTITY_PRE_PROCESSOR_VALUE;*/

    @SuppressWarnings("rawtypes")
	public EntityApprovalPreProcessor getEntityApprovalPreProcessor(Class xlass) {
        EntityApprovalPreProcessor entityApprovalPreProcessor = null;
        String beanName = entityApprovalPreProcessorResourceLoader.getProperty(xlass.getName());
                if(beanName == null){
                    // trying find for single table bean
                    Class superclass = xlass.getSuperclass();
                    Inheritance inheritance = AnnotationUtils.findAnnotation(superclass, Inheritance.class);
                    if(inheritance !=null && inheritance.strategy()!=null && inheritance.strategy().equals(InheritanceType.SINGLE_TABLE)){
                        beanName = entityApprovalPreProcessorResourceLoader.getProperty(superclass.getName());
                    }
                }

        if (beanName != null) {
            entityApprovalPreProcessor = applicationContext.getBean(beanName,
                    EntityApprovalPreProcessor.class);
        }
        return entityApprovalPreProcessor;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
