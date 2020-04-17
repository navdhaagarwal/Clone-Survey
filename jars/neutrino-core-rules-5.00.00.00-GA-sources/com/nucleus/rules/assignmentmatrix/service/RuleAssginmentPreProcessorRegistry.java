package com.nucleus.rules.assignmentmatrix.service;

import com.nucleus.logging.BaseLoggers;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;
import javax.inject.Named;
import java.util.*;

@Named("ruleAssginmentPreProcessorRegistry")
public class RuleAssginmentPreProcessorRegistry implements ApplicationContextAware {

    @Resource(name = "ruleAssignmentPreProcessorResourceLoader")
    private Properties ruleAssignmentPreProcessorResourceLoader;

    private ApplicationContext applicationContext;

    public static final String EXCEPTION_OCCURED_WHILE_COLLECTING_BEAN = "Exception occured while collecting bean %s";

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public List<IRuleAssginmentSimulation> getRuleEntity(String className){
        List<IRuleAssginmentSimulation> iRuleAssignmentSimulationList = new LinkedList<>();
        IRuleAssginmentSimulation iRuleAssignmentSimulation = null;
        String beanName = ruleAssignmentPreProcessorResourceLoader.getProperty(className);
        try{
            if(StringUtils.isNotBlank(beanName)){
                List<String> list = new ArrayList<>(Arrays.asList(beanName.split(",")));
                if(CollectionUtils.isNotEmpty(list)){
                    for(String bean : list){
                        iRuleAssignmentSimulation = applicationContext.getBean(bean,IRuleAssginmentSimulation.class);
                        if(iRuleAssignmentSimulation!=null){
                            iRuleAssignmentSimulationList.add(iRuleAssignmentSimulation);
                        }
                    }
                }
            }
        }catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e){
            BaseLoggers.exceptionLogger.error(String.format(EXCEPTION_OCCURED_WHILE_COLLECTING_BEAN,e.getMessage()));
        }
        return iRuleAssignmentSimulationList;
    }
}
