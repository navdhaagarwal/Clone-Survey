package com.nucleus.web.masking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.nucleus.core.genericparameter.dao.GenericParameterDao;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.role.entity.Role;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterUtils;
import com.nucleus.persistence.EntityDao;
import com.nucleus.security.masking.entities.MaskingDefinition;
import com.nucleus.security.masking.entities.MaskingPolicy;
import com.nucleus.security.masking.entities.MaskingType;
import com.nucleus.security.masking.entities.TagType;
import com.nucleus.security.masking.types.Constants;
import com.nucleus.security.masking.types.MaskingTypeFactory;
import com.nucleus.user.User;
import com.nucleus.web.common.controller.BaseController;

@Transactional
@Controller
@RequestMapping(value = "/MaskingPolicy")
public class MaskingPolicyController extends BaseController {

	private static final String masterId = "MaskingPolicy";
	private static final String childId = "MaskingDefinition";

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;

	@Inject
	@Named("maskingPolicyValidator")
	private Validator maskingPolicyValidator;

	@Inject
	@Named("genericParameterService")
	private GenericParameterService genericParameterService;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("genericParameterDao")
	GenericParameterDao genericParameterDao;

	@Inject
	@Named("maskingTypeFactory")
	private MaskingTypeFactory maskingTypeFactory;

	@Inject
	@Named("maskingPolicyService")
	private MaskingPolicyService maskingPolicyService;
	
	@Value(value = "#{'${security.masking.characters}'}")
	private String maskingPolicyCharacters;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(maskingPolicyValidator);
	}
	
	private List<String> getMaskingCharactersList(){
		return Arrays.asList(getMaskingPolicyCharacters().split(Constants.COMMA));
	}

	private static final Map<String, String[]> tagNameToMaskingDefinitionNames = new HashMap<>();
	static {
		tagNameToMaskingDefinitionNames.put(TagType.TAG_TYPE_INPUT, new String[] { "input1", "input2" });
		tagNameToMaskingDefinitionNames.put(TagType.TAG_TYPE_NO_TAG, new String[] { "input" });
		tagNameToMaskingDefinitionNames.put("address", new String[] { "country", "city", "village", "" });
	}// TOdO: 1) GET these values FROM db or somewhere else 2) Put more tags and their corresponding values to this map

	@PreAuthorize("hasAuthority('MAKER_MASKINGPOLICY')")
	@RequestMapping(value = "/create")
	public String createMaskingPolicy(ModelMap map) {
		return prepareMaskingPolicyObjectAndReturnView(map);
	}

	private String prepareMaskingPolicyObjectAndReturnView(ModelMap map) {
		List<Role> roleList = baseMasterService.getAllApprovedAndActiveEntities(Role.class);

		MaskingPolicy maskingPolicy = new MaskingPolicy();

		List<MaskingDefinition> maskingDefinitions = getMaskingDefinitionsBasedOnTag(TagType.TAG_TYPE_NO_TAG);
		maskingPolicy.setMaskingDefinitions(maskingDefinitions);
		map.put(Constants.MASKING_POLICY, maskingPolicy);
		map.put("maskingDefinitions", maskingDefinitions);
		map.put("tagTypeList", getTagTypeList());
		map.put("maskingTypeList", getMaskingTypeList());
		map.put("maskingCharactersList", getMaskingCharactersList());
		map.put("masterID", masterId);
		map.put("parentId", masterId);
		map.put("childId", childId);
		map.put("roleList", roleList);
		return Constants.MASKING_POLICY;
	}

	private List<MaskingDefinition> getMaskingDefinitionsBasedOnTag(String tagType) {

		List<MaskingDefinition> maskingDefinitionList = new ArrayList<>();

		if (tagType == null) {
			tagType = TagType.TAG_TYPE_NO_TAG;
		}
		String[] maskingDefinitionNames = tagNameToMaskingDefinitionNames.get(tagType);
		int numberOfMaskingDefinitions = tagNameToMaskingDefinitionNames.get(tagType).length;

		for (int i = 0; i < numberOfMaskingDefinitions; i++) {
			MaskingDefinition maskingDefinition = new MaskingDefinition();
			maskingDefinition.setName(maskingDefinitionNames[i]);
			maskingDefinition.setEnabled(true);
			maskingDefinitionList.add(maskingDefinition);
		}
		return maskingDefinitionList;
	}

	private List<TagType> getTagTypeList() {
		return genericParameterService.retrieveTypes(TagType.class);
	}

	private List<MaskingType> getMaskingTypeList() {
		return genericParameterService.retrieveTypes(MaskingType.class);
	}

	@RequestMapping(value = { "/getMaskingPolicyDefinitions" }, method = { RequestMethod.POST })
	public String getMaskingPolicyDefinitions(MaskingPolicy maskingPolicy,
			@RequestParam(required = false) List<Long> roleIds, ModelMap map) {

		List<MaskingDefinition> maskingDefinitions = getMaskingDefinitionsBasedOnTag(
				getTagNameFromMaskingPolicy(maskingPolicy));
		maskingPolicy.setMaskingDefinitions(maskingDefinitions);
		map.put(Constants.MASKING_POLICY, maskingPolicy);
		map.put("maskingDefinitions", maskingDefinitions);
		map.put("maskingTypeList", getMaskingTypeList());
		map.put("maskingCharactersList", getMaskingCharactersList());
		return "maskingDefinition";
	}

	private String getTagNameFromMaskingPolicy(MaskingPolicy maskingPolicy) {
		TagType tagType = entityDao.find(TagType.class, maskingPolicy.getTagNameToBeMasked().getId());
		return tagType.getCode();
	}


	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('MAKER_MASKINGPOLICY')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApproval(@Validated MaskingPolicy maskingPolicy, @RequestParam List<Long> roleIds,
			BindingResult result, ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

		BaseLoggers.flowLogger.debug(maskingPolicy.getLogInfo());
		map.put("masterID", masterId);
		map.put("childId", childId);
		map.put("parentId", masterId);
		/*
		 * Map whose Key Is Table Column Name with whom to validate and Value is
		 * The One to be validated.This Map Is Send in the Validator Method
		 */
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put(Constants.MASKING_POLICY_NAME, maskingPolicy.getMaskingPolicyName());
		validateMap.put(Constants.MASKING_POLICY_CODE, maskingPolicy.getMaskingPolicyCode());

		/*
		 * Code to check as if any existing(or new) record is being modified(or
		 * created) into another existing record
		 */

		List<MaskingDefinition> maskingDefinitions = new ArrayList<MaskingDefinition>();// Always add a new list
		for (int i = 0; i < maskingPolicy.getMaskingDefinitions().size(); i++) {
			MaskingDefinition maskingDefinition = maskingPolicy.getMaskingDefinitions().get(i);
			maskingDefinition.setOfflineFlag(maskingPolicy.getOfflineFlag());// set offline flag because of certain issues
			maskingDefinitions.add(maskingDefinition);

		}

		maskingPolicy.setMaskingDefinitions(maskingDefinitions);
		Hibernate.initialize(maskingPolicy.getMaskingDefinitions());
		List<MaskingDefinition> modifiedMaskingDefintions = (List<MaskingDefinition>) BaseMasterUtils
				.getUpdatedChildRecordsFromChangedChildList(maskingPolicy, MaskingPolicy.MASKING_DEFINITIONS,
						maskingPolicy.getMaskingDefinitions());

		maskingPolicy.setMaskingDefinitions(modifiedMaskingDefintions);

		List<String> colNameList = checkValidationForDuplicates(maskingPolicy, MaskingPolicy.class, validateMap);
		if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {
			addEntriesToMap(maskingPolicy, map);
			if (colNameList != null && !colNameList.isEmpty() && colNameList.contains(Constants.MASKING_POLICY_NAME)) {
				result.rejectValue(Constants.MASKING_POLICY_NAME, "label.maskingPolicyName.validation.exists");
			}
			if (colNameList != null && !colNameList.isEmpty() && colNameList.contains(Constants.MASKING_POLICY_CODE)) {
				result.rejectValue(Constants.MASKING_POLICY_CODE, "label.maskingPolicyCode.validation.exists");
			}
			return Constants.MASKING_POLICY;
		}

		boolean maskingDefintionsValid = validateMaskingDefinitions(maskingPolicy, result);
		if (!maskingDefintionsValid) {
			addEntriesToMap(maskingPolicy, map);
			return Constants.MASKING_POLICY;
		}

		setDefaultTag(maskingPolicy);

		setUserRoles(maskingPolicy, roleIds);

		// we need to get below logged in user from session
		User user = getUserDetails().getUserReference();
		if (user != null) {
			if (maskingPolicy.getId() == null) {
				maskingPolicy = maskingPolicyService.createMaskingPolicy(maskingPolicy, user);
			} else {
				maskingPolicy = maskingPolicyService.updateMaskingPolicy(maskingPolicy, user);
			}
			makerCheckerService.startMakerCheckerFlow(maskingPolicy.getEntityId(), user.getEntityId());

		}

		if (createAnotherMaster) {
			return prepareMaskingPolicyObjectAndReturnView(map);
		}

		return "redirect:/app/grid/MaskingPolicy/MaskingPolicy/loadColumnConfig";

	}

	private boolean validateMaskingDefinitions(MaskingPolicy maskingPolicy, BindingResult result) {
		int currentIndex = 0;
		boolean maskingDefintionsValid = true;
		for (MaskingDefinition maskingDefinition : maskingPolicy.getMaskingDefinitions()) {
			com.nucleus.security.masking.types.MaskingType maskingType = maskingTypeFactory
					.getMaskingType(entityDao.find(MaskingType.class, maskingDefinition.getType().getId()).getCode());
			if (!maskingType.isValidExpression(maskingDefinition.getExpression())) {
				result.rejectValue("maskingDefinitions[" + currentIndex + "].expression",
						"label.maskingDefinition.incorrectExpression");
				maskingDefintionsValid = false;
			}

			currentIndex++;
		}
		return maskingDefintionsValid;
	}

	private void addEntriesToMap(MaskingPolicy maskingPolicy, ModelMap map) {
		List<Role> roleList = baseMasterService.getAllApprovedAndActiveEntities(Role.class);
		map.put(Constants.MASKING_POLICY, maskingPolicy);
		map.put("maskingDefinitions", maskingPolicy.getMaskingDefinitions());
		map.put("masterID", masterId);
		map.put("childId", childId);
		map.put("parentId", masterId);
		map.put("roleList", roleList);
		map.put("tagTypeList", getTagTypeList());
		map.put("maskingTypeList", getMaskingTypeList());
		map.put("maskingCharactersList", getMaskingCharactersList());
	}

	@PreAuthorize("hasAuthority('MAKER_MASKINGPOLICY')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String save(@Validated MaskingPolicy maskingPolicy, @RequestParam List<Long> roleIds, BindingResult result,
			ModelMap map, @RequestParam("createAnotherMaster") boolean createAnotherMaster) {

		/*
		 * Map whose Key Is Table Column Name with whom to validate and Value is
		 * The One to be validated.This Map Is Send in the Validator Method
		 */
		map.put("masterID", masterId);
		map.put("childId", childId);
		map.put("parentId", masterId);
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put(Constants.MASKING_POLICY_NAME, maskingPolicy.getMaskingPolicyName());
		validateMap.put(Constants.MASKING_POLICY_CODE, maskingPolicy.getMaskingPolicyCode());

		/*
		 * Code to check as if any existing(or new) record is being modified(or
		 * created) into another existing record
		 */
		List<String> colNameList = checkValidationForDuplicates(maskingPolicy, MaskingPolicy.class, validateMap);
		if (result.hasErrors() || (colNameList != null && !colNameList.isEmpty())) {
			addEntriesToMap(maskingPolicy, map);
			if (colNameList != null && !colNameList.isEmpty() && colNameList.contains(Constants.MASKING_POLICY_NAME)) {
				result.rejectValue(Constants.MASKING_POLICY_NAME, "label.maskingPolicyName.validation.exists");
			}
			if (colNameList != null && !colNameList.isEmpty() && colNameList.contains(Constants.MASKING_POLICY_CODE)) {
				result.rejectValue(Constants.MASKING_POLICY_CODE, "label.maskingPolicyCode.validation.exists");
			}
			return Constants.MASKING_POLICY;
		}

		boolean maskingDefintionsValid = validateMaskingDefinitions(maskingPolicy, result);
		if (!maskingDefintionsValid) {
			addEntriesToMap(maskingPolicy, map);
			return Constants.MASKING_POLICY;
		}

		setDefaultTag(maskingPolicy);

		setUserRoles(maskingPolicy, roleIds);
		// we need to get below logged in user from session
		User user = getUserDetails().getUserReference();

		if (user != null) {
			makerCheckerService.masterEntityChangedByUser(maskingPolicy, user);
		}

		if (createAnotherMaster) {
			return prepareMaskingPolicyObjectAndReturnView(map);
		}
		return "redirect:/app/grid/MaskingPolicy/MaskingPolicy/loadColumnConfig";

	}

	@PreAuthorize("hasAuthority('MAKER_MASKINGPOLICY')")
	@RequestMapping("/edit/{id}")
	public String edit(@PathVariable("id") long id, ModelMap map) {
		prepareDataForViewAndEdit(id, map);
		map.put("edit", true);
		return "maskingPolicy";
	}

	@PreAuthorize("hasAuthority('VIEW_MASKINGPOLICY') or hasAuthority('MAKER_MASKINGPOLICY') or hasAuthority('CHECKER_MASKINGPOLICY')")
	@RequestMapping("/view/{id}")
	public String view(@PathVariable("id") long id, ModelMap map) {
		prepareDataForViewAndEdit(id, map);
		map.put("viewable", true);
		return "maskingPolicy";
	}

	@SuppressWarnings("unchecked")
	private void prepareDataForViewAndEdit(long id, ModelMap map) {
		MaskingPolicy maskingPolicy = (MaskingPolicy) BaseMasterUtils.getMergeEditedRecords(MaskingPolicy.class, id);

		Hibernate.initialize(maskingPolicy.getTagNameToBeMasked());
		Hibernate.initialize(maskingPolicy.getMaskingDefinitions());
		for (MaskingDefinition maskingDefinition : maskingPolicy.getMaskingDefinitions()) {
			Hibernate.initialize(maskingDefinition.getType());
		}
		Hibernate.initialize(maskingPolicy.getUserRoles());

		if (maskingPolicy.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
				|| maskingPolicy.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
			MaskingPolicy prevMaskingPolicy = (MaskingPolicy) baseMasterService
					.getLastApprovedEntityByUnapprovedEntityId(maskingPolicy.getEntityId());
			map.put("prevMaskingPolicy", prevMaskingPolicy);
			map.put("editLink", false);
		} else if (maskingPolicy.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
			MaskingPolicy prevMaskingPolicy = (MaskingPolicy) baseMasterService
					.getLastUnApprovedEntityByApprovedEntityId(maskingPolicy.getEntityId());
			map.put("prevMaskingPolicy", prevMaskingPolicy);
			map.put("viewLink", false);
		}
		removeChildDeletedRecord(maskingPolicy); // removing deleted child
													// record which were kept
													// for history
		map.put(Constants.MASKING_POLICY, maskingPolicy);
		map.put("masterID", masterId);
		List<Role> roleList = baseMasterService.getAllApprovedAndActiveEntities(Role.class);
		map.put("roleList", roleList);
		map.put("userRoles", maskingPolicy.getUserRoles());
		map.put("tagTypeList", getTagTypeList());

		ArrayList<String> actions = (ArrayList<String>) maskingPolicy.getViewProperties().get("actions");
		if (actions != null) {
			for (String act : actions) {
				map.put("act" + act, false);
			}
		}
		map.put("maskingDefinitions", maskingPolicy.getMaskingDefinitions());
		map.put("maskingTypeList", getMaskingTypeList());
		map.put("maskingCharactersList", getMaskingCharactersList());
		map.put("viewModeOrEditMode", true);
	}

	private void setDefaultTag(MaskingPolicy maskingPolicy) {
		if (maskingPolicy.getTagNameToBeMasked() == null || maskingPolicy.getTagNameToBeMasked().getId() == null) {
			// if user didn't enter a tag name, default tag name value to
			// TagType.TAG_TYPE_NO_TAG
			TagType tagType = genericParameterDao.findByCode(TagType.TAG_TYPE_NO_TAG, TagType.class);
			maskingPolicy.setTagNameToBeMasked(tagType);
		}
	}

	private void setUserRoles(MaskingPolicy maskingPolicy, List<Long> roleIds) {
		Set<Role> userRoles = new HashSet<>();
		for (Long roleId : roleIds) {
			Role role = entityDao.find(Role.class, roleId);
			userRoles.add(role);
		}
		maskingPolicy.setUserRoles(userRoles);
	}

	private void removeChildDeletedRecord(MaskingPolicy maskingPolicy) {
		List<MaskingDefinition> maskingDefinitionList = maskingPolicy.getMaskingDefinitions();
		if (CollectionUtils.isNotEmpty(maskingDefinitionList)) {
			Iterator<MaskingDefinition> iterator = maskingDefinitionList.iterator();
			while (iterator.hasNext()) {
				MaskingDefinition maskingDefinition = iterator.next();
				if (maskingDefinition.getApprovalStatus() == ApprovalStatus.DELETED_APPROVED_IN_HISTORY) {
					iterator.remove();
				}
			}
		}

	}

	public String getMaskingPolicyCharacters() {
		if ("${security.masking.characters}".equalsIgnoreCase(maskingPolicyCharacters)) {
			return "*,X";
		}
		return maskingPolicyCharacters;
	}

}
