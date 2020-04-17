package com.nucleus.web.common.controller;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.nucleus.core.dynamicform.entities.PlaceholderFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderFilterMapping;
import com.nucleus.core.dynamicform.entities.ServicePlaceholderMapping;
import com.nucleus.currency.Currency;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.master.ServiceIdentifierService;
import com.nucleus.master.ServicePlaceHolderService;
import com.nucleus.user.UserService;
import com.nucleus.ws.core.entities.ServiceFieldType;
import com.nucleus.ws.core.entities.ServiceIdentifier;


@Transactional
@Controller
@RequestMapping(value = "/ServicePlaceholderFilterMapping")
public class ServicePlaceholderFilterMappingController extends BaseController {

	@Inject
	@Named("stringEncryptor")
	private StandardPBEStringEncryptor encryptor;
	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService        makerCheckerService;

	@Inject
	@Named("serviceIdentifierService")
	private ServiceIdentifierService serviceIdentifierService;

	@Inject
	@Named("masterGeographicService")
	private MasterGeographicService   masterGeographicService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService          baseMasterService;

	@Inject
	@Named("servicePlaceHolderService")
	private ServicePlaceHolderService          servicePlaceHolderService;

	@Inject
	@Named("userService")
	private UserService                userService;


	private static final String  masterId   = "ServicePlaceholderFilterMapping";
	private static final String	pathToRedirect="redirect:/app/grid/ServicePlaceholderFilterMapping/ServicePlaceholderFilterMapping/loadColumnConfig";
	private static final String  viewString = "servicePlaceholderFilterMapping";


	private static final String makerAuthorityCheckString="hasAuthority('MAKER_SERVICEPLACEHOLDERFILTERMAPPING')";

	/**
	 *
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@ModelAttribute("currentEntityClassName")
	public String getEntityClassName() throws UnsupportedEncodingException {
		String encrypt = "enc_" + encryptor.encrypt(ServicePlaceholderFilterMapping.class.getName());
		String returnUri = UriUtils.encodeQueryParam(encrypt, "UTF-8");
		return returnUri;
	}

	/**
	 *
	 * @param servicePlaceholderFilterMapping
	 * @param result
	 * @param map
	 * @param createAnotherMaster
	 * @return
	 */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@PreAuthorize(makerAuthorityCheckString)
	public String saveServicePlaceholderFilterMapping(ServicePlaceholderFilterMapping servicePlaceholderFilterMapping,
													  BindingResult result, ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		List<PlaceholderFilterMapping> placeholderFilterMappings = servicePlaceholderFilterMapping.getPlaceholderFilterMappings();
		for(PlaceholderFilterMapping placeholderFilterMapping : placeholderFilterMappings){
			if(placeholderFilterMapping.getDynamicFormFilter().getId()==null){
				placeholderFilterMapping.setDynamicFormFilter(null);
			}

		}

		boolean eventResult = executeMasterEvent(servicePlaceholderFilterMapping,"contextObjectServicePlaceholderFilterMapping",map);
		if(!eventResult){
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,servicePlaceholderFilterMapping.getReasonActInactMap());
			servicePlaceholderFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
			map.put("edit" , true);
			map.put("viewable" , false);
			updateModelMapForCreate(map, servicePlaceholderFilterMapping);
			return viewString;
		}
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = servicePlaceholderFilterMapping.getReasonActInactMap();
		if( servicePlaceholderFilterMapping.getReasonActInactMap() != null && servicePlaceholderFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
			List<MasterActiveInactiveReasons> reasonInActList = servicePlaceholderFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
			List<MasterActiveInactiveReasons> reasonActList = servicePlaceholderFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

			if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
				saveActInactReasonForMaster(reasonsActiveInactiveMapping, servicePlaceholderFilterMapping);
				servicePlaceholderFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
			}
			else{
				servicePlaceholderFilterMapping.setReasonActInactMap(null);
			}
		}
		makerCheckerService.masterEntityChangedByUser(servicePlaceholderFilterMapping, getUserDetails().getUserReference());

		if (createAnotherMaster) {
			updateModelMapForCreate(map, null);
			return viewString;
		}

		return pathToRedirect;

	}

	/**
	 *
	 * @param map
	 * @param servicePlaceholderFilterMapping
	 * @param result
	 * @return
	 */

	@RequestMapping(value = "/create")
	@PreAuthorize(makerAuthorityCheckString)
	public String createServicePlaceholderFilterMapping(ModelMap map, ServicePlaceholderFilterMapping servicePlaceholderFilterMapping,
														BindingResult result) {
		updateModelMapForCreate(map, servicePlaceholderFilterMapping);
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
	public String editServicePlaceholderFilterMapping(@PathVariable("id") Long id, ModelMap map) {

		updateModelMapForViewOrEdit(map, id);
		map.put("viewable" ,false);
		map.put("edit", true);
		return viewString;
	}

	/**
	 *
	 * @param servicePlaceholderFilterMapping
	 * @param result
	 * @param map
	 * @param createAnotherMaster
	 * @return
	 */
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	@PreAuthorize(makerAuthorityCheckString)
	public String saveAndSendForApproval(@Validated ServicePlaceholderFilterMapping servicePlaceholderFilterMapping, BindingResult result, ModelMap map,
										 @RequestParam("createAnotherMaster") boolean createAnotherMaster) {
		List<PlaceholderFilterMapping> placeholderFilterMappings = servicePlaceholderFilterMapping.getPlaceholderFilterMappings();
		for(PlaceholderFilterMapping placeholderFilterMapping : placeholderFilterMappings){
			if(placeholderFilterMapping.getDynamicFormFilter().getId()==null){
				placeholderFilterMapping.setDynamicFormFilter(null);
			}
		}

		boolean eventResult = executeMasterEvent(servicePlaceholderFilterMapping,"contextObjectServicePlaceholderFilterMapping",map);
		if(!eventResult){
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,servicePlaceholderFilterMapping.getReasonActInactMap());
			servicePlaceholderFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
			map.put("edit" , true);
			map.put("viewable" , false);
			updateModelMapForCreate(map, servicePlaceholderFilterMapping);
			return viewString;
		}
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = servicePlaceholderFilterMapping.getReasonActInactMap();
		if( servicePlaceholderFilterMapping.getReasonActInactMap() != null && servicePlaceholderFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons() != null) {
			List<MasterActiveInactiveReasons> reasonInActList = servicePlaceholderFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonInactive() != null))).collect(Collectors.toList());
			List<MasterActiveInactiveReasons> reasonActList = servicePlaceholderFilterMapping.getReasonActInactMap().getMasterActiveInactiveReasons().stream()
					.filter(m -> ((m.getReasonActive() != null))).collect(Collectors.toList());

			if (reasonInActList.size() != 0 || reasonActList.size() != 0) {
				saveActInactReasonForMaster(reasonsActiveInactiveMapping, servicePlaceholderFilterMapping);
				servicePlaceholderFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
			}
			else{
				servicePlaceholderFilterMapping.setReasonActInactMap(null);
			}
		}
		makerCheckerService.saveAndSendForApproval(servicePlaceholderFilterMapping, getUserDetails().getUserReference());

		if (createAnotherMaster) {
			updateModelMapForCreate(map, null);
			return viewString;
		}
		return pathToRedirect;

	}

	/**
	 * @param record
	 *  id for view
	 * @return void
	 * @throws
	 * @description to view
	 */

	@PreAuthorize("hasAuthority('VIEW_SERVICEPLACEHOLDERFILTERMAPPING') or hasAuthority('MAKER_SERVICEPLACEHOLDERFILTERMAPPING') or hasAuthority('CHECKER_SERVICEPLACEHOLDERFILTERMAPPING')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewTestMaster(@PathVariable("id") Long id, ModelMap map) {
		updateModelMapForViewOrEdit(map, id);
		map.put("viewable", true);
		return viewString;

	}


	private List<Map<String,Object>> convertListToTagMap(List<ServiceIdentifier> columnList){
		List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
		for(ServiceIdentifier column : columnList){
			Map<String,Object> item= new HashMap<String, Object>();
			item.put("itemLabel", column.getName());
			item.put("itemValue", column.getId());
			items.add(item);
		}
		return items;
	}


	private void updateModelMapForCreate(ModelMap map, ServicePlaceholderFilterMapping servicePlaceholderFilterMapping){
		List<ServiceIdentifier> listOfServicesForRecieve = serviceIdentifierService.getServiceIdentifiersForReceive();
		ServicePlaceholderFilterMapping servicePlaceholderFilterMappingSF= new ServicePlaceholderFilterMapping();
		if (notNull(servicePlaceholderFilterMapping)
				&&notNull(servicePlaceholderFilterMapping.getServiceIdentifier())
				&&notNull(servicePlaceholderFilterMapping.getServiceIdentifier().getId())) {
			ServiceIdentifier serviceIdentifierMaster= baseMasterService.findById(ServiceIdentifier.class, servicePlaceholderFilterMapping.getServiceIdentifier().getId());
			servicePlaceholderFilterMappingSF.setServiceIdentifier(serviceIdentifierMaster);
			List<ServicePlaceholderMapping> servicePlaceholders = servicePlaceHolderService.getServicePlaceHolderByServiceIdentifierID(servicePlaceholderFilterMapping.getServiceIdentifier().getId());
			if(hasElements(servicePlaceholders)){
				List<PlaceholderFilterMapping> placeholderFilterMappings = new ArrayList<PlaceholderFilterMapping>();
				for(ServicePlaceholderMapping servicePlaceholderMapping:servicePlaceholders){
					PlaceholderFilterMapping placeholderFilterMapping=new PlaceholderFilterMapping();
					placeholderFilterMapping.setScreenId(servicePlaceholderMapping.getScreenId());
					placeholderFilterMappings.add(placeholderFilterMapping);
					servicePlaceholderMapping.getScreenId().getScreenName();
				}
				servicePlaceholderFilterMappingSF.setPlaceholderFilterMappings(placeholderFilterMappings);
			}
		}
		ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
		servicePlaceholderFilterMappingSF.setReasonActInactMap(reasActInactMap);
		map.put("reasonsActiveInactiveMapping",servicePlaceholderFilterMappingSF.getReasonActInactMap());
		map.put("servicePlaceholderFilterMapping", servicePlaceholderFilterMappingSF);
		map.put("listOfServices", convertListToTagMap(listOfServicesForRecieve));
		map.put("masterID", masterId);
	}

	private void updateModelMapForViewOrEdit(ModelMap map, Long id){
		ServicePlaceholderFilterMapping servicePlaceholderFilterMapping = baseMasterService.getMasterEntityWithActionsById(ServicePlaceholderFilterMapping.class, id, getUserDetails().getUserReference().getUri());
		List<PlaceholderFilterMapping> placeholderFilterMappings =  servicePlaceholderFilterMapping.getPlaceholderFilterMappings();

		if(notNull(servicePlaceholderFilterMapping.getServiceIdentifier())){
			servicePlaceholderFilterMapping.getServiceIdentifier().getName();
		}

		if(hasElements(placeholderFilterMappings)){

			for(PlaceholderFilterMapping placeholderFilterMapping:placeholderFilterMappings){
				if(notNull(placeholderFilterMapping.getDynamicFormFilter()))
					placeholderFilterMapping.getDynamicFormFilter().getDisplayName();
				placeholderFilterMapping.getScreenId().getDisplayName();
			}
		}

		map.put("masterID", masterId);
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,servicePlaceholderFilterMapping.getReasonActInactMap());
		servicePlaceholderFilterMapping.setReasonActInactMap(reasonsActiveInactiveMapping);
		String masterName = servicePlaceholderFilterMapping.getClass().getSimpleName();
		String uniqueValue = servicePlaceholderFilterMapping.getEntityLifeCycleData().getUuid();
		String uniqueParameter = "entityLifeCycleData.uuid";
		getActInactReasMapForEditApproved(map,servicePlaceholderFilterMapping,masterName,uniqueParameter,uniqueValue);
		map.put("servicePlaceholderFilterMapping", servicePlaceholderFilterMapping);
		List<ServiceIdentifier> listOfServices = new ArrayList<ServiceIdentifier>();
		listOfServices.add(servicePlaceholderFilterMapping.getServiceIdentifier());
		map.put("listOfServices", convertListToTagMap(listOfServices));
		updateMapWithPermissibleAction(map, servicePlaceholderFilterMapping);
	}


	private void updateMapWithPermissibleAction(ModelMap map, ServicePlaceholderFilterMapping servicePlaceholderFilterMapping){
		if (servicePlaceholderFilterMapping.getViewProperties() != null) {
			ArrayList<String> actions = (ArrayList<String>) servicePlaceholderFilterMapping.getViewProperties().get("actions");
			if (actions != null) {
				for (String act : actions) {
					if(!"Clone".equalsIgnoreCase(act)){
						String actionString = "act" + act;
						map.put(actionString.replaceAll(" ", ""), false);
					}
				}
			}
		}

	}



}
