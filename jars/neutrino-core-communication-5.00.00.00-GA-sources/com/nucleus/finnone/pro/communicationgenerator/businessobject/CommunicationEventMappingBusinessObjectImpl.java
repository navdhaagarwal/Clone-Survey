package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.event.EventCode;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.entity.SystemEntity;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.dao.CommunicationEventMappingDAO;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationEventMappingHeader;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.EventCommCriteriaTypeMapping;
import com.nucleus.finnone.pro.communicationgenerator.util.CommunicationEventMappingHelper;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.SourceProduct;
import com.nucleus.standard.context.NeutrinoExecutionContextHolder;
import com.nucleus.user.User;

@Named("communicationEventMappingBusinessObject")
public class CommunicationEventMappingBusinessObjectImpl implements CommunicationEventMappingBusinessObject {

	@Inject
	@Named("communicationEventMappingDAO")
	private CommunicationEventMappingDAO communicationEventMappingDAO;

	@Inject
	@Named("neutrinoExecutionContextHolder")
	private NeutrinoExecutionContextHolder neutrinoExecutionContextHolder;

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;

	@Inject
	@Named("communicationEventMappingHelper")
	private CommunicationEventMappingHelper communicationEventMappingHelper;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("configurationService")
	private ConfigurationService configurationService;

	@Inject
	@Named("entityDao")
	protected EntityDao entityDao;

	public List<CommunicationName> getCommunicationCodesBySourceProductId(Long sourceProductId) {
		return communicationEventMappingDAO.getCommunicationCodesBySourceProductId(sourceProductId);
	}

	public CommunicationType getCommunicationTypeByCommunicationCodeId(Long communicationId) {
		return communicationEventMappingDAO.getCommunicationTypeByCommunicationCodeId(communicationId);
	}

	public List<CommunicationTemplate> getCommunicationTemplateByCommunicationCodeId(Long communicationId) {
		return communicationEventMappingDAO.getCommunicationTemplateByCommunicationCodeId(communicationId);
	}

	public CommunicationEventMappingHeader saveCommunicationEventmapping(
			CommunicationEventMappingHeader communicationEventMapping, User user) {
		NeutrinoValidator.notNull(communicationEventMapping, "CommunicationEventMapping Entity Cannot be saved null");
		// communicationEventMapping.setCommunicationCode(entityDao.find(CommunicationName.class,
		// communicationEventMapping.getCommunicationCodeId()));
		communicationEventMappingHelper.prepareCommEventMappingDetails(communicationEventMapping);
		communicationEventMapping.setTenantId(neutrinoExecutionContextHolder.getTenantId());
		communicationEventMapping.setMakeBusinessDate(neutrinoExecutionContextHolder.getBusinessDateWithSystemTime());
		communicationEventMapping.markTemp();
		return (CommunicationEventMappingHeader) makerCheckerService
				.masterEntityChangedByUser(communicationEventMapping, user);

	}

	public boolean checkDuplicateCommunicationEventMapping(Long eventMappingHdrId, Long moduleId, Long eventId) {
		List<CommunicationEventMappingHeader> communicationEventMappings = communicationEventMappingDAO
				.checkDuplicateCommunicationEventMapping(moduleId, eventId);
		if (hasElements(communicationEventMappings)) {
			for (CommunicationEventMappingHeader communicationEventMapping : communicationEventMappings) {
				if (isNull(eventMappingHdrId) || !communicationEventMapping.getId().equals(eventMappingHdrId)) {
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}

	public List<CommunicationTemplate> getCommunicationTemplateByCommunicationTypeId(Long communicationTypeId) {
		return communicationEventMappingDAO.getCommunicationTemplateByCommunicationTypeId(communicationTypeId);
	}

	public List<? extends GenericParameter> getSourceProductList() {
		List<SourceProduct> sourceProductList = new ArrayList<>();
		ConfigurationVO configuredSrcProducts = configurationService.getConfigurationPropertyFor(
				SystemEntity.getSystemEntityId(), "config.commEventMapping.sourceProducts");

		if (configuredSrcProducts.getPropertyValue() == null
				&& StringUtils.isEmpty(configuredSrcProducts.getPropertyValue())) {
			genericParameterService.retrieveTypes(SourceProduct.class);
		} else {
			getSourceProductList(sourceProductList, configuredSrcProducts.getPropertyValue());
		}
		return sourceProductList;

	}

	private void getSourceProductList(List<SourceProduct> sourceProductList, String configuredSrcProducts) {
		if (!StringUtils.contains(configuredSrcProducts, ',')) {
			sourceProductList.add(genericParameterService.findByName(configuredSrcProducts, SourceProduct.class));
		} else {
			for (String srcProduct : StringUtils.split(configuredSrcProducts, ',')) {
				sourceProductList
						.add(genericParameterService.findByName(StringUtils.trim(srcProduct), SourceProduct.class));
			}
		}
	}

	public CommunicationEventMappingHeader getCommunicationEventMapping(String eventCode, SourceProduct module) {
		return communicationEventMappingDAO.getCommunicationEventMapping(eventCode, module);
	}

	@Override
	public List<EventCode> getEventCodesBySourceProductId(Long sourceProductId) {
		return communicationEventMappingDAO.getEventCodesBySourceProductId(sourceProductId);
	}

	@Override
	public String getCommunicationCriteriaType(Long sourceProductId, Long eventCodeId) {
		EventCommCriteriaTypeMapping eventCommCritMap = communicationEventMappingDAO
				.getEventCommCriteriaTypeMapping(sourceProductId, eventCodeId);
		if (eventCommCritMap != null && eventCommCritMap.getCommCriteriaType() != null) {
			return eventCommCritMap.getCommCriteriaType().getCode();
		}
		return null;
	}
}
