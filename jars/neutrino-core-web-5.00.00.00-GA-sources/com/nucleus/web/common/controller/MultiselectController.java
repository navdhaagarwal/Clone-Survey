package com.nucleus.web.common.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.autocomplete.AutocompleteService;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;

/**
 * @author Nucleus Software India Pvt. Ltd.
 * 
 */
@Transactional
@Controller
@RequestMapping(value = "/multiselect")
public class MultiselectController extends BaseController {

	@Inject
	@Named("autocompleteService")
	private AutocompleteService autocompleteService;

	@RequestMapping(value = "/populate")
	@ResponseBody
	public Map<String, ?> populateMultiselectListItems(ModelMap map, @RequestParam String inputValue,
			@RequestParam String searchColumn, @RequestParam String itemLabel, @RequestParam String itemValue,
			@RequestParam String className, @RequestParam int page, @RequestParam int pageSize,
			@RequestParam(required = false) String itemToBeExcluded, @RequestParam(required = false) Boolean containsSearchEnabled,
			HttpServletRequest req) {
		int listItemsSize = 0;
		if (ValidatorUtils.isNull(containsSearchEnabled)) {
			containsSearchEnabled = false;
		}
		Map<String, Object> responseMap = new HashMap<>();
		List<Map<String, ?>> multiselectListItems = autocompleteService.searchOnFieldValueByPage(className, itemValue,
				itemLabel, searchColumn, inputValue, true, itemToBeExcluded, page, pageSize, containsSearchEnabled);
		if (ValidatorUtils.hasElements(multiselectListItems)) {
			Map<String, ?> listMap = multiselectListItems.get(multiselectListItems.size() - 1);
			listItemsSize = ((Long) listMap.get("size")).intValue();
			multiselectListItems.remove(multiselectListItems.size() - 1);
			responseMap.put("items", multiselectListItems);
			responseMap.put("count", listItemsSize);
		}
		return responseMap;
	}

}
