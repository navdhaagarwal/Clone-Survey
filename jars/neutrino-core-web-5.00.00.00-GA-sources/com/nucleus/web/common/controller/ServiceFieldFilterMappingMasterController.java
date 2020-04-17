package com.nucleus.web.common.controller;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.persistence.HibernateUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriUtils;

import com.nucleus.address.MasterGeographicService;
import com.nucleus.core.dynamicform.entities.FieldFilterMapping;
import com.nucleus.core.dynamicform.entities.ServiceFieldFilterMapping;
import com.nucleus.currency.Currency;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.master.ServiceIdentifierService;
import com.nucleus.user.UserService;
import com.nucleus.ws.core.entities.ServiceFieldName;
import com.nucleus.ws.core.entities.ServiceFieldType;
import com.nucleus.ws.core.entities.ServiceIdentifier;

@Transactional
@Controller
@RequestMapping(value = "/ServiceFieldFilterMapping")
public class ServiceFieldFilterMappingMasterController extends BaseController {

	@Inject
	@Named("stringEncryptor")
	private StandardPBEStringEncryptor encryptor;
	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;
	
	@Inject
	@Named("serviceIdentifierService")
	private ServiceIdentifierService serviceIdentifierService;

	@Inject
	@Named("masterGeographicService")
	private MasterGeographicService masterGeographicService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	@Inject
	@Named("userService")
	private UserService userService;

	private static final String masterId = "ServiceFieldFilterMapping";
	private static final String pathToRedirect = "redirect:/app/grid/ServiceFieldFilterMapping/ServiceFieldFilterMapping/loadColumnConfig";
	private static final String viewString = "serviceFieldFilterMapping";

	private static final String makerAuthorityCheckString = "hasAuthority('MAKER_SERVICEFIELDFILTERMAPPING')";

	/**
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@ModelAttribute("currentEntityClassName")
	public String getEntityClassName() throws UnsupportedEncodingException {
		String encrypt = "enc_" + encryptor.encrypt(ServiceFieldFilterMapping.class.getName());
		String returnUri = UriUtils.encodeQueryParam(encrypt, "UTF-8");
		return returnUri;
	}

	/**
	 * 
	 * @param serviceFieldFilterMapping
	 * @param result
	 * @param map
	 * @param createAnotherMaster
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@PreAuthorize(makerAuthorityCheckString)
	public String saveServiceFieldFilterMapping(ServiceFieldFilterMapping serviceFieldFilterMapping,
			BindingResult result, ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

		removeInvalidFieldFilterMapping(serviceFieldFilterMapping);
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = serviceFieldFilterMapping.getReasonActInactMap();
		if( serviceFieldFilterMapping.getReasonActInactMap() != null && serviceFieldFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
			List<MasterActiveInactiveReasons> reasonInActList = serviceFieldFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
			List<MasterActiveInactiveReasons> reasonActList = serviceFieldFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

			if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
				saveActInactReasonForMaster(reasonsActiveInactiveMapping, serviceFieldFilterMapping);
				serviceFieldFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
			}
			else{
				serviceFieldFilterMapping.setReasonActInactMap(null);
			}
		}
		makerCheckerService.masterEntityChangedByUser(serviceFieldFilterMapping, getUserDetails().getUserReference());

		if (createAnotherMaster) {
			updateModelMapForCreate(map, null);
			return viewString;
		}

		return pathToRedirect;

	}

	/**
	 * 
	 * @param map
	 * @param serviceFieldFilterMapping
	 * @param result
	 * @return
	 */

	@RequestMapping(value = "/create")
	@PreAuthorize(makerAuthorityCheckString)
	public String createServiceFieldFilterMapping(ModelMap map, ServiceFieldFilterMapping serviceFieldFilterMapping,
			BindingResult result) {
		updateModelMapForCreate(map, serviceFieldFilterMapping);
		return viewString;
	}

	/**
	 * 
	 * @param id
	 * @param map
	 * @return
	 */
	@RequestMapping(value = "/edit/{id}")
	@PreAuthorize(makerAuthorityCheckString)
	public String editServiceFieldFilterMapping(@PathVariable("id") Long id, ModelMap map) {

		updateModelMapForViewOrEdit(map, id, true);
		map.put("viewable" ,false);
		map.put("edit", true);
		return viewString;
	}

	/**
	 * 
	 * @param serviceFieldFilterMapping
	 * @param result
	 * @param map
	 * @param createAnotherMaster
	 * @return
	 */
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	@PreAuthorize(makerAuthorityCheckString)
	public String saveAndSendForApproval(@Validated ServiceFieldFilterMapping serviceFieldFilterMapping,
			BindingResult result, ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

		removeInvalidFieldFilterMapping(serviceFieldFilterMapping);
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = serviceFieldFilterMapping.getReasonActInactMap();
		if( serviceFieldFilterMapping.getReasonActInactMap() != null && serviceFieldFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
			List<MasterActiveInactiveReasons> reasonInActList = serviceFieldFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
			List<MasterActiveInactiveReasons> reasonActList = serviceFieldFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

			if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
				saveActInactReasonForMaster(reasonsActiveInactiveMapping, serviceFieldFilterMapping);
				serviceFieldFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
			}
			else{
				serviceFieldFilterMapping.setReasonActInactMap(null);
			}
		}
		makerCheckerService.saveAndSendForApproval(serviceFieldFilterMapping, getUserDetails().getUserReference());

		if (createAnotherMaster) {
			updateModelMapForCreate(map, null);
			return viewString;
		}
		return pathToRedirect;

	}

	/**
	 * @param record
	 *            id for view
	 * @return void
	 * @throws @description
	 *             to view
	 */

	@PreAuthorize("hasAuthority('VIEW_SERVICEFIELDFILTERMAPPING') or hasAuthority('MAKER_SERVICEFIELDFILTERMAPPING') or hasAuthority('CHECKER_SERVICEFIELDFILTERMAPPING')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewTestMaster(@PathVariable("id") Long id, ModelMap map) {
		updateModelMapForViewOrEdit(map, id, false);
		map.put("viewable", true);
		return viewString;

	}

	private List<Map<String, Object>> convertListToTagMap(List<ServiceIdentifier> columnList) {
		List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
		for (ServiceIdentifier column : columnList) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("itemLabel", column.getName());
			item.put("itemValue", column.getId());
			items.add(item);
		}

		return items;
	}

	private void updateModelMapForCreate(ModelMap map, ServiceFieldFilterMapping serviceFieldFilterMapping) {
		List<ServiceIdentifier> listOfServicesForSend = serviceIdentifierService.getServiceIdentifiersForSend();
		ServiceFieldFilterMapping serviceFieldFilterMappingSF = new ServiceFieldFilterMapping();
		if (notNull(serviceFieldFilterMapping) && notNull(serviceFieldFilterMapping.getServiceIdentifier())
				&& notNull(serviceFieldFilterMapping.getServiceIdentifier().getId())) {
			ServiceIdentifier serviceIdentifierMaster = baseMasterService.findById(ServiceIdentifier.class,
					serviceFieldFilterMapping.getServiceIdentifier().getId());
			serviceFieldFilterMappingSF.setServiceIdentifier(serviceIdentifierMaster);
			List<ServiceFieldName> serviceFields = serviceIdentifierMaster.getServiceFields();
			if (hasElements(serviceFields)) {
				List<FieldFilterMapping> fieldFilterMappings = new ArrayList<FieldFilterMapping>();
				for (ServiceFieldName fieldName : serviceFields) {
					FieldFilterMapping fieldFilterMapping = new FieldFilterMapping();
					fieldFilterMapping.setServiceFieldName(fieldName);
					fieldFilterMapping.setServiceMappingId(serviceFieldFilterMapping.getId());
					fieldFilterMappings.add(fieldFilterMapping);
				}
				serviceFieldFilterMappingSF.setFieldFilterMappings(fieldFilterMappings);
			}
		}
		ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
		serviceFieldFilterMappingSF.setReasonActInactMap(reasActInactMap);
		map.put("reasonsActiveInactiveMapping",serviceFieldFilterMappingSF.getReasonActInactMap());
		map.put("serviceFieldFilterMapping", serviceFieldFilterMappingSF);
		map.put("listOfServices", convertListToTagMap(listOfServicesForSend));
		map.put("masterID", masterId);
	}

	private void updateModelMapForViewOrEdit(ModelMap map, Long id, boolean isEditMode) {
		ServiceFieldFilterMapping serviceFieldFilterMapping = baseMasterService.getMasterEntityWithActionsById(
				ServiceFieldFilterMapping.class, id, getUserDetails().getUserReference().getUri());
		List<FieldFilterMapping> fieldFilterMappings = serviceFieldFilterMapping.getFieldFilterMappings();

		if (notNull(serviceFieldFilterMapping.getServiceIdentifier())) {
			serviceFieldFilterMapping.getServiceIdentifier().getName();
			serviceFieldFilterMapping.getServiceIdentifier().getId();
		}

		if (hasElements(fieldFilterMappings)) {

			for (FieldFilterMapping fieldFilterMapping : fieldFilterMappings) {
				if(notNull(fieldFilterMapping.getDynamicFormFilter())){
					fieldFilterMapping.getDynamicFormFilter().getDisplayName();
					fieldFilterMapping.getDynamicFormFilter().getId();	
				}
				
				fieldFilterMapping.getServiceFieldName().getFieldName();
			}
		}

		map.put("masterID", masterId);
		if (isEditMode) {
			serviceFieldFilterMapping = updateServiceFieldFilterMapping(serviceFieldFilterMapping);
		}
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,serviceFieldFilterMapping.getReasonActInactMap());
		serviceFieldFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
		String masterName = serviceFieldFilterMapping.getClass().getSimpleName();
		String uniqueValue = serviceFieldFilterMapping.getEntityLifeCycleData().getUuid();
		String uniqueParameter = "entityLifeCycleData.uuid";
		getActInactReasMapForEditApproved(map,serviceFieldFilterMapping,masterName,uniqueParameter,uniqueValue);
		map.put("serviceFieldFilterMapping", serviceFieldFilterMapping);
		List<ServiceIdentifier> listOfServices = new ArrayList<ServiceIdentifier>();
		listOfServices.add(serviceFieldFilterMapping.getServiceIdentifier());
		map.put("listOfServices", convertListToTagMap(listOfServices));
		updateMapWithPermissibleAction(map, serviceFieldFilterMapping);
	}

	private ServiceFieldFilterMapping updateServiceFieldFilterMapping(
			ServiceFieldFilterMapping serviceFieldFilterMapping) {

		List<ServiceFieldName> serviceFieldNames = serviceFieldFilterMapping.getServiceIdentifier().getServiceFields();
		List<ServiceFieldName> serviceFieldNamesMapped = new ArrayList<ServiceFieldName>();

		for (FieldFilterMapping fieldFilterMapping : serviceFieldFilterMapping.getFieldFilterMappings()) {
			serviceFieldNamesMapped.add(fieldFilterMapping.getServiceFieldName());
		}
		List<ServiceFieldName> additionalServiceFieldNamesToBeMapped = new ArrayList<ServiceFieldName>();
		List<FieldFilterMapping> additionalFieldFilterMappings = new ArrayList<FieldFilterMapping>();

		for (ServiceFieldName serviceFieldName : serviceFieldNames) {
			if (!serviceFieldNamesMapped.contains(serviceFieldName)) {
				additionalServiceFieldNamesToBeMapped.add(serviceFieldName);
				FieldFilterMapping additionalfieldFilterMapping = new FieldFilterMapping();
				additionalfieldFilterMapping.setServiceFieldName(serviceFieldName);
				additionalFieldFilterMappings.add(additionalfieldFilterMapping);
			}
		}

		for (FieldFilterMapping additionalFieldFilterMapping : additionalFieldFilterMappings)
			serviceFieldFilterMapping.getFieldFilterMappings().add(additionalFieldFilterMapping);

		return serviceFieldFilterMapping;
	}

	private void updateMapWithPermissibleAction(ModelMap map, ServiceFieldFilterMapping serviceFieldFilterMapping) {
		if (serviceFieldFilterMapping.getViewProperties() != null) {
			ArrayList<String> actions = (ArrayList<String>) serviceFieldFilterMapping.getViewProperties()
					.get("actions");
			if (actions != null) {
				for (String act : actions) {
					String actionString = "act" + act;
					map.put(actionString.replaceAll(" ", ""), false);
				}
			}
		}

	}

	private void removeInvalidFieldFilterMapping(ServiceFieldFilterMapping serviceFieldFilterMapping) {
		List<FieldFilterMapping> fieldFilterMappings = new ArrayList<FieldFilterMapping>();
		fieldFilterMappings = serviceFieldFilterMapping.getFieldFilterMappings();


		for (Iterator<FieldFilterMapping> iterator = fieldFilterMappings.iterator(); iterator.hasNext();) {
			FieldFilterMapping fieldFilterMapping = iterator.next();
		    if (fieldFilterMapping.getDynamicFormFilter().getId() == null) {
		        iterator.remove();
		    }
		}

	}


}
