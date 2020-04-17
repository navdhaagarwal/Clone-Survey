package com.nucleus.master.audit.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.springframework.stereotype.Component;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.misc.util.DateUtils;
import com.nucleus.entity.EntityId;
import com.nucleus.entity.SystemEntity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MasterConfigurationRegistry;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.audit.MasterChangeAuditLog;
import com.nucleus.master.audit.MasterChangeDiffHolder;
import com.nucleus.master.audit.MasterChangeEntityHolder;
import com.nucleus.master.audit.MasterChangeVO;
import com.nucleus.master.audit.annotation.NeutrinoAuditableMaster;
import com.nucleus.master.audit.dao.MasterChangeDao;
import com.nucleus.master.audit.metadata.AuditableClassMetadataFactory;
import com.nucleus.master.audit.metadata.AuditableClassReferenceInitlizer;
import com.nucleus.master.audit.service.diffmessage.MasterChangeMessageGenerationUtility;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserService;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@Component("masterChangeAuditLogGenerator")
public class MasterChangeAuditLogGenerator extends BaseServiceImpl {

	@Inject
	@Named("masterChangePrePostProcessorExecutor")
	private MasterChangePrePostProcessorExecutor prePostProcessor;

	@Inject
	@Named("masterChangeJaversRegister")
	private MasterChangeJaversRegister javersRegister;

	@Inject
	@Named("masterChangeJaversExecutor")
	private MasterChangeJaversExecutor changeExecutor;

	@Inject
	@Named("masterChangeDao")
	private MasterChangeDao auditDao;

	@Inject
	@Named("masterConfigurationRegistry")
	private MasterConfigurationRegistry masterConfigurationRegistry;

	@Inject
	@Named("userService")
	private UserService userService;

	@Inject
	@Named("masterChangeMessageGenerationUtility")
	private MasterChangeMessageGenerationUtility utility;

	@Inject
	@Named("auditableClassReferenceInitlizer")
	private AuditableClassReferenceInitlizer referenceIntializer;

	@Inject
	@Named("configurationService")
	private ConfigurationService config;

	public void generateChangeAndSave(MasterChangeEntityHolder oldEntity, MasterChangeEntityHolder newEntity,
			Class entityClass, EntityId reviewerId, String lastUpdatedByUri) {
		try {

			// invoke-pre populater -> to be use in case to load any disjoint
			// entity
			prePostProcessor.executePreProcess(oldEntity, newEntity, entityClass);
			// inti lazy references
			initClassMetadataAndLazyReferences(oldEntity, newEntity, entityClass);
			// invoke diff generator
			MasterChangeDiffHolder diffs = changeExecutor.getDiff(oldEntity, newEntity, entityClass);
			// post process the diff to beutify message
			prePostProcessor.executePostProcess(diffs, entityClass, oldEntity, newEntity);
			// generate message
			utility.updateMessagesInDIffObject(diffs, oldEntity, newEntity);
			// persist into DB`
			MasterChangeAuditLog log = convertToDBEntity(reviewerId, diffs, newEntity.getRootEntity().getUri(),
					lastUpdatedByUri);
			auditDao.saveMasterAuditData(log);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in Creating Audit for Master : " + entityClass, e);
		}

	}

	private AuditableClassMetadataFactory initClassMetadataAndLazyReferences(MasterChangeEntityHolder oldEntity,
			MasterChangeEntityHolder newEntity, Class entityClass) throws Exception {
		AuditableClassMetadataFactory classBuilder = javersRegister.getClassBuilder(entityClass);
		oldEntity.setMetadataFactory(classBuilder);
		newEntity.setMetadataFactory(classBuilder);
		referenceIntializer.intializedReference(classBuilder.getOutputClassMetadata(),oldEntity, newEntity, entityClass);
		// inti lazy reference for disjoint entiy
		if (MapUtils.isNotEmpty(oldEntity.getDisJointChildEntity())) {
			oldEntity.getDisJointChildEntity().forEach((key, disjointEntityHolder) -> {
				try {
					AuditableClassMetadataFactory disJointEntityBuilder = null;
					Class disjointEntityClass = null;
					if(disjointEntityHolder.getDisJointEntity()!=null){
						disjointEntityClass = disjointEntityHolder.getDisJointEntity().getClass();
						disJointEntityBuilder = javersRegister
								.getClassBuilder(disjointEntityClass,disjointEntityHolder.getIdentifier());
					}else if(newEntity.getDisJointChildByName(key).getDisJointEntity()!=null){
						disjointEntityClass = newEntity.getDisJointChildByName(key).getDisJointEntity().getClass();
						disJointEntityBuilder = javersRegister
								.getClassBuilder(newEntity.getDisJointChildByName(key).getDisJointEntity().getClass(),disjointEntityHolder.getIdentifier());
					}
					if(Objects.nonNull(disJointEntityBuilder)){
						disjointEntityHolder.setMetadataFactory(disJointEntityBuilder);
						newEntity.getDisJointChildByName(key).setMetadataFactory(disJointEntityBuilder);
						classBuilder.getOutputClassMetadata().addDisJointChildMetadata(key, disJointEntityBuilder.getOutputClassMetadata());
						referenceIntializer.intializedReference(disJointEntityBuilder.getOutputClassMetadata(),disjointEntityHolder,
								newEntity.getDisJointChildByName(key), disjointEntityClass);
					}

				} catch (Exception e) {
					BaseLoggers.exceptionLogger.error("Error in Creating Audit for Master : "
							+ disjointEntityHolder.getDisJointEntity().getClass(), e);
				}

			});
		}
		return classBuilder;
	}

	public void generateChangeAndSave(BaseMasterEntity oldEntity, BaseMasterEntity newEntity, Class entityClass,
			EntityId reviewedId, String lastUpdatedByUri) {
		try {
			if (isMasterChildAUditEnabld() && isAuditable(entityClass)) {
				generateChangeAndSave(new MasterChangeEntityHolder(oldEntity), new MasterChangeEntityHolder(newEntity),
						entityClass, reviewedId, lastUpdatedByUri);
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in Creating Audit for Master : " + entityClass, e);
		}

	}

	public boolean isAuditable(Class entityClass) {
		try {
			return entityClass.getAnnotation(NeutrinoAuditableMaster.class) != null;
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Error in Buidling javerse instanc for Class :" + entityClass, e);
		}
		return false;
	}

	private MasterChangeAuditLog convertToDBEntity(EntityId reviewedBy, MasterChangeDiffHolder diff, String entiytUri,
			String changedByUri) {
		MasterChangeAuditLog log = new MasterChangeAuditLog();
		log.setEntityURI(entiytUri);
		log.getEntityLifeCycleData().setCreationTimeStamp(DateUtils.getCurrentUTCTime());
		log.getEntityLifeCycleData().setCreatedByUri(reviewedBy.getUri());
		log.getEntityLifeCycleData().setLastUpdatedByUri(changedByUri);
		log.setJaversMessage(diff.getDelta() != null ? diff.getDelta().prettyPrint() : "No Change");
		log.setNeutrinoMessage(new JSONSerializer().deepSerialize(diff.getDeltaInString()));
		return log;
	}

	public List<MasterChangeVO> getAuditDetailByEntity(String masterId, Long id) {

		if (masterId != null && !masterId.isEmpty()) {
			Class entityClass = null;
			String entityPath = masterConfigurationRegistry.getEntityClass(masterId);
			if (entityPath != null && !entityPath.isEmpty()) {
				try {
					entityClass = Class.forName(entityPath);
				} catch (ClassNotFoundException e) {
					BaseLoggers.exceptionLogger.error(e.getMessage());
				}
			}
			List<MasterChangeAuditLog> logs = auditDao.getAuditLogs(new EntityId(entityClass, id).getUri());
			List<MasterChangeVO> result = new ArrayList<>();
			logs.forEach((l) -> {
				result.add(convertDBObjectToVo(l));
			});
			return result;
		}
		return null;
	}

	public MasterChangeVO convertDBObjectToVo(MasterChangeAuditLog log) {
		MasterChangeVO vo = new MasterChangeVO();
		vo.setUserName(userService.getUserNameByUserUri(log.getEntityLifeCycleData().getCreatedByUri()));
		vo.setActionMessages((List<String>) new JSONDeserializer<>().deserialize(log.getNeutrinoMessage()));
		vo.setSubmittedBy(StringUtils.isNotEmpty(log.getEntityLifeCycleData().getLastUpdatedByUri())
				? userService.getUserNameByUserUri(log.getEntityLifeCycleData().getLastUpdatedByUri()) : "");
		vo.setTypeOfAction("Edit-Reviewed");
		vo.setDateOfAction(DateFormatUtils.format(log.getEntityLifeCycleData().getCreationTimeStamp().getMillis(),
				getUserPreferredDateTimeFormat()));
		return vo;
	}

	public Boolean isMasterChildAUditEnabld() {
		Boolean isMasterChildAuditEnabled = false;
		try {
			String productName = ProductInformationLoader.getProductName();
			if (productName == null || productName.isEmpty()) {
				BaseLoggers.exceptionLogger.error("No product name found");
				return isMasterChildAuditEnabled;
			}
			String propertyKey = "config." + productName + ".masterChildAudit.enable";
			ConfigurationVO configurationVO = config.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(),
					propertyKey);
			if (configurationVO == null || configurationVO.getPropertyValue() == null
					|| BooleanUtils.toBooleanObject(configurationVO.getPropertyValue()) == null) {
				BaseLoggers.exceptionLogger.error(
						"No Master Child Audit entry found : Property value missing or invalid for : " + propertyKey);
				return isMasterChildAuditEnabled;
			}
			String childDeletionCheckPropertyValue = configurationVO.getPropertyValue();
			if (BooleanUtils.toBooleanObject(childDeletionCheckPropertyValue) != null) {
				isMasterChildAuditEnabled = BooleanUtils.toBooleanObject(childDeletionCheckPropertyValue);
			}
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Exception Occured : ", e);
		}
		return isMasterChildAuditEnabled;
	}
}
