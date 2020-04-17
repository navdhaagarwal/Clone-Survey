package com.nucleus.role;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.nucleus.activeInactiveReason.MasterActiveInactiveReasons;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.persistence.HibernateUtils;


import com.nucleus.autocomplete.AutocompleteVO;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csvreader.CsvWriter;
import com.nucleus.authority.Authority;
import com.nucleus.authority.dao.AuthorityDao;
import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.core.role.entity.Role;
import com.nucleus.core.role.roleVO.RoleInfoVO;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import com.nucleus.user.UserService;
import com.nucleus.web.common.controller.BaseController;


@Transactional
@Controller
@RequestMapping(value = "/Role")
public class RoleController extends BaseController {

	private static final String START_DATE_SMALLER = "com.nucleus.role.RoleController.START_DATE_SMALLER";
	private static final String DISABLE_LOGIN_UNCHECKED = "com.nucleus.role.RoleController.DISABLE_LOGIN_UNCHECKED";

	@Inject
	@Named("makerCheckerService")
	private MakerCheckerService makerCheckerService;

	@Inject
	@Named("entityDao")
	private EntityDao entityDao;

	@Inject
	@Named("userService")
	private UserService userService;

	@Inject
	@Named("baseMasterService")
	private BaseMasterService baseMasterService;

	@Inject
	@Named("messageSource")
	protected MessageSource messageSource;

	@Inject
	@Named("AuthorityDao")
	protected AuthorityDao authorityDao;

	private static final String masterId = "Role";

	/*
	 * Method Added to send current Entity Uri for working of
	 * comments,activity,history,notes
	 */
	@ModelAttribute("currentEntityClassName")
	public String getEntityClassName() {
		return Role.class.getName();
	}

	@PreAuthorize("hasAuthority('MAKER_ROLE')")
	@RequestMapping(value = "/create")
	public String createRole(ModelMap map) {
		Role role = new Role();
		role.setAuthorityIdsString("");
		role.setAuthorityNames("");
		ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
		role.setReasonActInactMap(reasActInactMap);
		map.put("reasonsActiveInactiveMapping",role.getReasonActInactMap());
		map.put("role", role);
		map.put("masterID", masterId);
		return "role";
	}

	@PreAuthorize("hasAuthority('MAKER_ROLE')")
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public String saveRole(@ModelAttribute("role") Role role, BindingResult result, ModelMap map,
						   @RequestParam("createAnotherMaster") boolean createAnotherMaster, HttpServletRequest request) {
		BaseLoggers.flowLogger.debug("Saving Role Details-->" + role.getLogInfo());
		map.put("masterID", masterId);

		/*
		 * Map whose Key Is Table Column Name with whom to validate and Value is
		 * The One to be validated.This Map Is Send in the Validator Method
		 */
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put("name", role.getName());

		/*
		 * Code to check as if any existing(or new) record is being modified(or
		 * created) into another existing record
		 */
		String validationResult = validateRoleDisabling(role, request);
		List<String> colNameList = checkValidationForDuplicates(role, Role.class, validateMap);
		if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)
				|| validationResult.indexOf("FAILED") > -1) {
			// getActInactReasMapForEdit(map,role);
			String masterName = role.getClass().getSimpleName();
			String uniqueValue = null;
			String uniqueParameter = null;
			if (null != role.getId()) {
				Role roleForName = baseMasterService.findById(Role.class, role.getId());
				uniqueValue = roleForName.getName();
				uniqueParameter = "name";
				getActInactReasMapForEditApproved(map, role, masterName, uniqueParameter, uniqueValue);
			}
			else {
				ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
				role.setReasonActInactMap(reasActInactMap);
			}
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,role.getReasonActInactMap());
			role.setReasonActInactMap(reasonsActiveInactiveMapping);
			List<Authority> authorities = userService.getAuthorities();
			map.put("authorities", authorities);
			if (colNameList != null && colNameList.size() > 0) {
				result.rejectValue("name", "label.role.roleName.validation.exists");
			}
			if (validationResult.indexOf("FAILED") > -1) {
				result.rejectValue("disabledFrom", validationResult.split("\\|")[1]);
				result.rejectValue("disabledTo", validationResult.split("\\|")[1]);
			}
			if(role.getId() != null){
				Role actualRole = entityDao.find(Role.class, role.getId());
				role.setAuthorities(actualRole.getAuthorities());
				setRoleStringDataForDisplay(role);
				role.setAuthorities(null);
			} else if(!StringUtils.isEmpty(role.getAuthorityIdsString())) {
				Set<Authority> authoritySet = new HashSet<>();
				String[] authorityIds = role.getAuthorityIdsString().split(",");
				for (String authorityId : authorityIds) {
					if (!StringUtils.isEmpty(authorityId)) {
						authoritySet.add(userService.getAuthorityById(Long.parseLong(authorityId)));
					}
				}
				role.setAuthorities(authoritySet);
				setRoleStringDataForDisplay(role);
				role.setAuthorities(null);
			}
			map.put("edit" , true);
			map.put("viewable", false);
			return "role";
		}
		boolean eventResult = executeMasterEvent(role,"contextObjectRole",map);
		if(!eventResult) {
			// getActInactReasMapForEdit(map,role);
			String masterName = role.getClass().getSimpleName();
			String uniqueParameter = "name";
			String uniqueValue = role.getName();
			getActInactReasMapForEditApproved(map,role,masterName,uniqueParameter,uniqueValue);
			map.put("edit" , true);
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,role.getReasonActInactMap());
			role.setReasonActInactMap(reasonsActiveInactiveMapping);
			List<Authority> authorities = userService.getAuthorities();
			map.put("authorities", authorities);
			map.put("viewable", false);
			map.put("masterID", masterId);
			return "role";
		}

		if (role.getAuthorityIdsString() != null && role.getAuthorityIdsString().length() > 0) {
			String[] authorityIDsArr = role.getAuthorityIdsString().split(",");
			Set<Authority> authorities = new HashSet<Authority>();
			for (String authorityId : authorityIDsArr) {
				if (!StringUtils.isEmpty(authorityId)) {
					authorities.add(userService.getAuthorityById(Long.parseLong(authorityId)));
				}
			}
			role.setAuthorities(authorities);
		}
		if (role.getProductDescriminator() == null || role.getProductDescriminator().length() == 0) {
			role.setProductDescriminator(ProductInformationLoader.getProductName());
		}
		User user = getUserDetails().getUserReference();
		if (user != null) {
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = role.getReasonActInactMap();
			if(reasonsActiveInactiveMapping != null){
				saveActInactReasonForMaster(reasonsActiveInactiveMapping,role);
			}
			role.setReasonActInactMap(reasonsActiveInactiveMapping);
			makerCheckerService.masterEntityChangedByUser(role, user);
		}
		if (createAnotherMaster) {
			return createRole(map);
		}
		return "redirect:/app/grid/Role/Role/loadColumnConfig";

	}

	@PreAuthorize("hasAuthority('MAKER_ROLE')")
	@RequestMapping(value = "/saveAndSendForApproval", method = RequestMethod.POST)
	public String saveAndSendForApproval(Role role, BindingResult result, ModelMap map,
										 @RequestParam("createAnotherMaster") boolean createAnotherMaster, HttpServletRequest request) {
		BaseLoggers.flowLogger.debug("Saving Role Details-->" + role.getLogInfo());
		map.put("masterID", masterId);

		/*
		 * Map whose Key Is Table Column Name with whom to validate and Value is
		 * The One to be validated.This Map Is Send in the Validator Method
		 */
		Map<String, Object> validateMap = new HashMap<String, Object>();
		validateMap.put("name", role.getName());

		/*
		 * Code to check as if any existing(or new) record is being modified(or
		 * created) into another existing record
		 */

		List<String> colNameList = checkValidationForDuplicates(role, Role.class, validateMap);

		String validationResult = validateRoleDisabling(role, request);
		if (result.hasErrors() || (colNameList != null && colNameList.size() > 0)
				|| validationResult.indexOf("FAILED") > -1) {
			String masterName = role.getClass().getSimpleName();
			String uniqueValue = null;
			String uniqueParameter = null;
			if (null != role.getId()) {
				Role roleForName = baseMasterService.findById(Role.class, role.getId());
				uniqueValue = roleForName.getName();
				uniqueParameter = "name";
				getActInactReasMapForEditApproved(map, role, masterName, uniqueParameter, uniqueValue);
			}
			else {
				ReasonsActiveInactiveMapping reasActInactMap = getActInactReasMapForCreate(map);
				role.setReasonActInactMap(reasActInactMap);
			}
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,role.getReasonActInactMap());
			role.setReasonActInactMap(reasonsActiveInactiveMapping);
			List<Authority> authorities = userService.getAuthorities();
			map.put("authorities", authorities);
			if (colNameList != null && colNameList.size() > 0) {
				result.rejectValue("name", "label.role.roleName.validation.exists");
			}
			if (validationResult.indexOf("FAILED") > -1) {
				result.rejectValue("disabledFrom", validationResult.split("\\|")[1]);
				result.rejectValue("disabledTo", validationResult.split("\\|")[1]);
			}
			if(role.getId() != null){
				Role actualRole = entityDao.find(Role.class, role.getId());
				role.setAuthorities(actualRole.getAuthorities());
				setRoleStringDataForDisplay(role);
				role.setAuthorities(null);
			} else if(!StringUtils.isEmpty(role.getAuthorityIdsString())) {
				Set<Authority> authoritySet = new HashSet<>();
				String[] authorityIds = role.getAuthorityIdsString().split(",");
				for (String authorityId : authorityIds) {
					if (!StringUtils.isEmpty(authorityId)) {
						authoritySet.add(userService.getAuthorityById(Long.parseLong(authorityId)));
					}
				}
				role.setAuthorities(authoritySet);
				setRoleStringDataForDisplay(role);
				role.setAuthorities(null);
			}
			map.put("edit" , true);
			map.put("viewable", false);
			return "role";
		}
		boolean eventResult = executeMasterEvent(role,"contextObjectRole",map);
		if(!eventResult) {
			// getActInactReasMapForEdit(map,role);
			String masterName = role.getClass().getSimpleName();
			String uniqueParameter = "name";
			String uniqueValue = role.getName();
			getActInactReasMapForEditApproved(map,role,masterName,uniqueParameter,uniqueValue);
			map.put("edit" , true);
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,role.getReasonActInactMap());
			role.setReasonActInactMap(reasonsActiveInactiveMapping);
			List<Authority> authorities = userService.getAuthorities();
			map.put("authorities", authorities);
			map.put("viewable", false);
			map.put("masterID", masterId);
			return "role";
		}

		if (role.getAuthorityIdsString() != null && role.getAuthorityIdsString().length() > 0) {
			String[] authorityIDsArr = role.getAuthorityIdsString().split(",");
			Set<Authority> authorities = new HashSet<Authority>();
			for (String authorityId : authorityIDsArr) {
				if (!StringUtils.isEmpty(authorityId)) {
					authorities.add(userService.getAuthorityById(Long.parseLong(authorityId)));
				}
			}
			role.setAuthorities(authorities);
		}
		if (role.getProductDescriminator() == null || role.getProductDescriminator().length() == 0) {
			role.setProductDescriminator(ProductInformationLoader.getProductName());
		}

		// we need to get below logged in user from session
		User user = getUserDetails().getUserReference();
		if (user != null) {
			ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = role.getReasonActInactMap();
			if(reasonsActiveInactiveMapping != null){
				saveActInactReasonForMaster(reasonsActiveInactiveMapping,role);
			}
			role.setReasonActInactMap(reasonsActiveInactiveMapping);
			makerCheckerService.saveAndSendForApproval(role, user);
		}
		if (createAnotherMaster) {
			return createRole(map);
		}
		return "redirect:/app/grid/Role/Role/loadColumnConfig";

	}

	@PreAuthorize("hasAuthority('MAKER_ROLE')")
	@RequestMapping(value = "/edit/{id}")
	public String editRole(ModelMap map, @PathVariable("id") Long id) {
		Role role = entityDao.find(Role.class, id);
		Hibernate.initialize(role.getAuthorities());
		map.put("masterID", masterId);
		if (role.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
			Role prevRole = (Role) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(role.getEntityId());
			map.put("prevProduct", prevRole);
			map.put("editLink", false);

		} else if (role.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
				|| role.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
			Role prevRole = (Role) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(role.getEntityId());
			map.put("prevProduct", prevRole);
			map.put("viewLink", false);
		}
		setRoleStringDataForDisplay(role);
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,role.getReasonActInactMap());
		role.setReasonActInactMap(reasonsActiveInactiveMapping);
		String masterName = role.getClass().getSimpleName();
		String uniqueParameter = "name";
		String uniqueValue = role.getName();
		getActInactReasMapForEditApproved(map,role,masterName,uniqueParameter,uniqueValue);
		map.put("viewable" ,false);
		map.put("role", role);
		map.put("editLink", false);
		map.put("edit", true);
		return "role";

	}

	@SuppressWarnings("unchecked")
	@PreAuthorize("hasAuthority('MAKER_ROLE') or hasAuthority('CHECKER_ROLE') or hasAuthority('VIEW_ROLE')")
	@RequestMapping(value = "/view/{id}", method = RequestMethod.GET)
	public String viewRole(@PathVariable("id") Long id, ModelMap map) {

		UserInfo currentUser = getUserDetails();
		Role role = baseMasterService.getMasterEntityWithActionsById(Role.class, id,
				currentUser.getUserEntityId().getUri());

		if (role.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED) {
			Role prevRole = (Role) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(role.getEntityId());
			map.put("prevProduct", prevRole);
			map.put("editLink", false);
		} else if (role.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED
				|| role.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS) {
			Role prevRole = (Role) baseMasterService.getLastUnApprovedEntityByApprovedEntityId(role.getEntityId());
			map.put("prevProduct", prevRole);
			map.put("viewLink", false);
		}
		ReasonsActiveInactiveMapping reasonsActiveInactiveMapping = getActInactReasonsForEditGeneric(map,role.getReasonActInactMap());
		role.setReasonActInactMap(reasonsActiveInactiveMapping);
		String masterName = role.getClass().getSimpleName();
		String uniqueParameter = "name";
		String uniqueValue = role.getName();
		getActInactReasMapForEditApproved(map,role,masterName,uniqueParameter,uniqueValue);
		map.put("role", role);
		map.put("masterID", masterId);
		map.put("viewable", true);
		setRoleStringDataForDisplay(role);
		if (role.getViewProperties() != null) {
			ArrayList<String> actions = (ArrayList<String>) role.getViewProperties().get("actions");
			if (actions != null) {
				for (String act : actions) {
					String actionString = "act" + act;
					map.put(actionString.replaceAll(" ", ""), false);
				}

			}

		}

		return "role";
	}

	@PreAuthorize("hasAuthority('MAKER_ROLE') or hasAuthority('CHECKER_ROLE') or hasAuthority('VIEW_ROLE')")
	@RequestMapping(value = "/csvView", method = RequestMethod.GET)
	public void createCSV(HttpServletRequest request, HttpServletResponse response) throws IOException {
		List<Role> roleList = userService.getRoles();
		response.setContentType("text/csv;charset=utf-8");
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String strDate = sdfDate.format(now);

		response.setHeader("Content-Disposition", "attachment; filename=\"" + ProductInformationLoader.getProductName()
				+ "role-data_" + strDate + ".csv\"");
		OutputStream resOs = null;
		OutputStream buffOs = null;
		OutputStreamWriter outputwriter = null;
		CsvWriter writer = null;
		try {
			resOs = response.getOutputStream();
			buffOs = new BufferedOutputStream(resOs);
			outputwriter = new OutputStreamWriter(buffOs);

			writer = new CsvWriter(outputwriter, ',');

			for (Role role : roleList) {
				writer.write(role.getProductDescriminator().toUpperCase() + "_" + role.getName());
			}
			outputwriter.flush();
		} finally {
			writer.close();
			outputwriter.close();
			buffOs.close();
			resOs.close();

		}

	}

	private String validateRoleDisabling(Role role, HttpServletRequest request) {
		StringBuilder message = new StringBuilder("");
		if ((role.getIsDisabled() != null && role.getIsDisabled().equals(Boolean.TRUE))
				|| ((role.getIsDisabled() == null || role.getIsDisabled().equals(Boolean.FALSE))
				&& (role.getDisabledFrom() == null && role.getDisabledTo() == null))) {
			message.append("SUCESS");
		} else if ((role.getIsDisabled() == null || role.getIsDisabled().equals(Boolean.FALSE))
				&& (role.getDisabledFrom() != null || role.getDisabledTo() != null)) {
			message.append("FAILED|" + messageSource.getMessage(DISABLE_LOGIN_UNCHECKED, null, request.getLocale()));
		}

		if ((role.getIsDisabled() != null && role.getIsDisabled().equals(Boolean.TRUE))
				&& (role.getDisabledFrom() != null && role.getDisabledTo() != null)) {
			long millisecs = role.getDisabledTo().getMillis() - role.getDisabledFrom().getMillis();
			message.append((millisecs >= 0) ? "SUCESS"
					: "FAILED|" + messageSource.getMessage(START_DATE_SMALLER, null, request.getLocale()));
		}

		return message.toString();
	}

	@RequestMapping(value = "/saveAllAuthorities/{sourceProductID}")
	@ResponseBody
	public RoleInfoVO addAllAuthoritiesOfSourceSystem (@RequestParam String containsSearchEnabled,
													   @PathVariable("sourceProductID") String sourceProductID){
		List<Authority> authorities = getAllAuthoritiesFromFilter(containsSearchEnabled, sourceProductID, "");
		RoleInfoVO roleInfoVO=new RoleInfoVO();
		String[] authorityNames=new String[authorities.size()];
		String[] authorityIds=new String[authorities.size()];
		String[] authorityDescription=new String[authorities.size()];
		String[] authorityModuleName=new String[authorities.size()];
		if (!CollectionUtils.isEmpty(authorities)) {
			int count=0;
			for (Authority authority : authorities) {
				authorityIds[count]=authority.getId().toString();
				authorityNames[count]=authority.getName();
				authorityDescription[count]=authority.getDescription();
				String moduleName = (authority.getSysName() != null) ? authority.getSysName().getCode() : "";
				authorityModuleName[count]=moduleName;
				count++;
			}
			roleInfoVO.setAuthorityIds(authorityIds);
			roleInfoVO.setAuthorityNames(authorityNames);
			roleInfoVO.setAuthorityDescription(authorityDescription);
			roleInfoVO.setAuthorityModuleName(authorityModuleName);
		}
		return roleInfoVO;
	}

	private List<Authority> getAllAuthoritiesFromFilter(String containsSearchEnabled, String sourceProductID, String value){
		String[] authorityIds = null;
		List<Long> authorityIDSList = new ArrayList<>();
		if (!StringUtils.isEmpty(containsSearchEnabled) && !containsSearchEnabled.equalsIgnoreCase("NULL")) {
			authorityIds = containsSearchEnabled.split(",");
			int maxLimit = (authorityIds.length > 999) ? 999 : authorityIds.length;
			for (int i = 0; i < maxLimit; i++) {
				if (!StringUtils.isEmpty(authorityIds[i])) {
					authorityIDSList.add(Long.parseLong(authorityIds[i]));
				}
			}
		}
		List<Authority> authorities = null;
		String queryString = (value.equals("%%%")) ? "" : value;
		if (sourceProductID.equalsIgnoreCase("ALL") && authorityIDSList.isEmpty()) {
			authorities = authorityDao.getAllAuthoritiesWithQuery(queryString);
		} else if (sourceProductID.equalsIgnoreCase("ALL") && !authorityIDSList.isEmpty()) {
			authorities = authorityDao.getAllAuthoritiesWithQueryExcludingAuthorityID(authorityIDSList,
					queryString);
		} else if (!sourceProductID.equalsIgnoreCase("ALL") && authorityIDSList.isEmpty()) {
			Long productID = Long.parseLong(sourceProductID);
			authorities = authorityDao.getAllAuthorityOfSourceProductWithQuery(productID, queryString);
		} else {
			Long productID = Long.parseLong(sourceProductID);
			authorities = authorityDao.getAllAuthorityOfSourceProductWithQueryExcludingAuthorityID(productID,
					authorityIDSList, queryString);
		}
		List<Authority> copyAuthorities = new ArrayList<Authority>(authorities);
		if(authorityIds != null && authorityIds.length > 999){
			List <String> authorityIdList = Arrays.asList(authorityIds);
			for (int i = 0; i < copyAuthorities.size(); i++) {
				String id = copyAuthorities.get(i).getId()+"";
				if(authorityIdList.contains(id)){
					for (int j = 0; j < authorities.size(); j++) {
						if(copyAuthorities.get(i).getId().equals(authorities.get(j).getId())){
							authorities.remove(j);
							break;
						}
					}
				}
			}
		}
		return authorities;
	}

	@RequestMapping(value = "/populateAuthorityAutoComplete/{sourceProductID}/{authorityIdsToExclude}")
	@ResponseBody
	public AutocompleteVO populateAuthorityAutoComplete(ModelMap map, @RequestParam String value, @RequestParam String itemVal,
														@RequestParam String searchCol, @RequestParam String className,
														@RequestParam Boolean loadApprovedEntityFlag, @RequestParam String i_label, @RequestParam String idCurr,
														@RequestParam String content_id, @RequestParam int page, @RequestParam(required = false) String itemsList,
														@RequestParam(required = false) Boolean strictSearchOnitemsList, HttpServletRequest req,
														@RequestParam String containsSearchEnabled, @PathVariable("sourceProductID") String sourceProductID,
														@PathVariable("authorityIdsToExclude") String authorityIdsToExclude) {
		AutocompleteVO autocompleteVO = new AutocompleteVO();
		String[] searchColumnList = searchCol.split(" ");
		try {
			List<Authority> authorities = getAllAuthoritiesFromFilter(containsSearchEnabled, sourceProductID, value);
			List<Map<String, ?>> list = new ArrayList<>();
			if (org.apache.commons.collections.CollectionUtils.isNotEmpty(authorities)) {
				//map.put("size", Integer.valueOf(authorities.size()));
				//map.put("page", page);
				autocompleteVO.setS(Integer.valueOf(authorities.size()));
				autocompleteVO.setP(page);
				for (Authority authority : authorities) {
					LinkedHashMap<String, Object> authMap = new LinkedHashMap<String, Object>();
					authMap.put("id", (Long) authority.getId());
					authMap.put("name", authority.getName());
					authMap.put("description", authority.getDescription());
					String moduleName = (authority.getSysName() != null) ? authority.getSysName().getCode() : "";
					authMap.put("sourceSystem", moduleName);
					list.add(authMap);
				}
			}
			if (org.apache.commons.collections.CollectionUtils.isNotEmpty(list)) {
				//map.put("size", list.size());
				//map.put("page", page);

				autocompleteVO.setS(list.size());
				autocompleteVO.setP(page);
				if (list.size() / 3 == page && list.size() % 3 == 1)
					list = list.subList(3 * page, 3 * page + 1);

				else if (list.size() / 3 == page && list.size() % 3 == 2)
					list = list.subList(3 * page, 3 * page + 2);

				else
					list = list.subList(3 * page, 3 * page + 3);
			}

			//map.put("data", list);
			//map.put("searchCol", searchCol);
			autocompleteVO.setD(list);
			autocompleteVO.setScl(searchColumnList);
			String[] sclHeading=new String[searchColumnList.length];

			for(int i=0;i<searchColumnList.length;i++)
			{
				searchColumnList[i]=searchColumnList[i].replace(".", "");
				sclHeading[i]=messageSource.getMessage("label.autoComplete."+searchColumnList[i],null, Locale.getDefault());
			}

			if (i_label != null && i_label.contains(".")) {
				i_label = i_label.replace(".", "");
			}
			if (idCurr != null && idCurr.trim().length() > 0) {
				idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
			}
			//map.put("idCurr", idCurr);
			//map.put("i_label", i_label);
			//map.put("content_id", content_id);
			autocompleteVO.setIc(idCurr);
			autocompleteVO.setIl(i_label);
			autocompleteVO.setCi(content_id);
			autocompleteVO.setIv(itemVal);
			autocompleteVO.setScl(searchColumnList);
			autocompleteVO.setColh(sclHeading);
		} catch (Exception e) {
			BaseLoggers.exceptionLogger.error("Could not add authority id to exclude :", e.getMessage());
		}
		return autocompleteVO;
	}

	private void setRoleStringDataForDisplay(Role role) {
		StringBuilder authorityNames = new StringBuilder("");
		StringBuilder authorityIds = new StringBuilder("");
		StringBuilder authorityDescription = new StringBuilder("");
		StringBuilder authorityModuleName = new StringBuilder("");
		if (role.getAuthorities() != null && !role.getAuthorities().isEmpty()) {
			final String sep = ",";
			final String sep1 = "_Seprator_";
			for (Authority authority : role.getAuthorities()) {
				authorityIds.append(authority.getId() + sep);
				authorityNames.append(authority.getName() + sep1);
				authorityDescription.append(authority.getDescription() + sep1);
				String moduleName = (authority.getSysName() != null) ? authority.getSysName().getCode() : "";
				authorityModuleName.append(moduleName + sep);
			}
			authorityIds.deleteCharAt(authorityIds.length() - 1);
			authorityNames.delete(authorityNames.lastIndexOf(sep1), authorityNames.lastIndexOf(sep1) + 10);
			authorityDescription.delete(authorityDescription.lastIndexOf(sep1),
					authorityDescription.lastIndexOf(sep1) + 10);
			authorityModuleName.deleteCharAt(authorityModuleName.length() - 1);;
			role.setAuthorityIdsString(authorityIds.toString());
			role.setAuthorityNames(authorityNames.toString());
			role.setAuthorityDescription(authorityDescription.toString());
			role.setAuthoritySourceProductString(authorityModuleName.toString());
		}
	}


}
