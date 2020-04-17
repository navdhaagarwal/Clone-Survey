package com.nucleus.master.audit.service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;
import com.nucleus.master.audit.service.util.MasterChangeExecutionHelper;
import com.nucleus.master.audit.service.util.MasterChangeJaversHolder;

@Component("masterChangeJaversRegister")
public class MasterChangeJaversRegister implements ApplicationContextAware {

	public static final Map<Class<? extends BaseMasterEntity>, MasterChangeJaversHolder> javerseRegister = new HashMap<>();

	public static final Map<Class<? extends BaseMasterEntity>, MasterChangeExecutionHelper> changeHelperRegister = new HashMap<>();

	public static final Map<Class<? extends BaseMasterEntity>, AuditableClassMetadataFactory> classMetaBuilderRegister = new HashMap<>();

	private ApplicationContext apctx;
	
	@Inject
	@Named("masterChangeExecutionHelper")
	private MasterChangeExecutionHelper masterChange;

	public MasterChangeJaversHolder getJaversInstance(Class entityClass) throws Exception {
		if (entityClass != null) {
			if (javerseRegister.get(entityClass) == null) {
				synchronized (this) {
					updateInfoOfJaversClass(entityClass);
				}
			}
			return javerseRegister.get(entityClass);
		}
		return null;
	}

	private void updateInfoOfHelpersClass(Class entityClass) throws Exception {
		try {
			AuditableClassMetadataFactory classMetadataBuilder = getClassBuilder(entityClass);
			try {
				Object bean = this.apctx.getBean(entityClass.getSimpleName().toLowerCase() + "ChangeExecutionHelper");
				if (bean instanceof MasterChangeExecutionHelper) {
					changeHelperRegister.put(entityClass, (MasterChangeExecutionHelper) bean);
				} else {
					BaseLoggers.exceptionLogger
							.error("Bean Found But is of Not Desided type (extends class MasterChangeExecutionHelper)"
									+ bean.getClass().getSimpleName());
				}
			} catch (NoSuchBeanDefinitionException e) {
				changeHelperRegister.put(entityClass, (MasterChangeExecutionHelper) masterChange);

			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in building Javers Instance for : " + entityClass);
			throw e;
		}
	}



	private void updateInfoOfJaversClass(Class entityClass) throws Exception {
		try {
			AuditableClassMetadataFactory classMetadataBuilder = getClassBuilder(entityClass);
			MasterChangeExecutionHelper bean = getDiffHelperInstance(entityClass);
			javerseRegister.put(entityClass, bean.getJaversInstance(classMetadataBuilder.getOutputClassMetadata(), bean));
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in building Javers Instance for : " + entityClass);
			throw e;
		}
	}

	public MasterChangeExecutionHelper getDiffHelperInstance(Class entityClass) throws Exception {
		if (entityClass != null) {
			if (changeHelperRegister.get(entityClass) == null) {
				updateInfoOfHelpersClass(entityClass);
			}
			return changeHelperRegister.get(entityClass);
		}
		return null;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.apctx = applicationContext;

	}

	public AuditableClassMetadataFactory getClassBuilder(Class entityTye,String... varargs) throws Exception {
		AuditableClassMetadataFactory classBuilder = classMetaBuilderRegister.get(entityTye);
		if (classBuilder == null) {
			classMetaBuilderRegister.put(entityTye, new AuditableClassMetadataFactory(entityTye).startFactory(varargs));
			classBuilder = classMetaBuilderRegister.get(entityTye);
		}
		return classBuilder;
	}

}
