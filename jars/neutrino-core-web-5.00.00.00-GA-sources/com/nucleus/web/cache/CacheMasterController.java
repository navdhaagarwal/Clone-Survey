package com.nucleus.web.cache;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.finnone.pro.cache.common.CacheManager;
import com.nucleus.finnone.pro.cache.common.MasterCacheService;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.finnone.pro.cache.service.CacheCommonService;
import com.nucleus.finnone.pro.cache.vo.CacheMasterVO;
import com.nucleus.finnone.pro.cache.vo.ImpactedCacheVO;
import com.nucleus.web.common.controller.NonTransactionalBaseController;
import com.nucleus.web.datatable.SimpleDataTableJsonHepler;

import flexjson.JSONSerializer;
import flexjson.transformer.AbstractTransformer;
import flexjson.transformer.Transformer;

@Controller
@RequestMapping(value = "/cacheMaster")
public class CacheMasterController extends NonTransactionalBaseController {

	private static final String DATE_FORMAT_PATTERN = "dd/MM/yyyy HH:mm:ss";
	private static final String ERROR = "error";
	private static final String MESSAGE_KEY = "message";
	private static final String ERROR_MSG_KEY_CACHE_REFRESH = "label.cache.group.message.build.error";
	private static final String SUCCESS_MSG_KEY_CACHE_REFRESH = "label.cache.group.message.build.success";
	private static final String ERROR_MSG_KEY_IMPACTED_CACHE_BUILD = "label.impacted.cache.message.build.error";
	private static final String SUCCESS_MSG_KEY_IMPACTED_CACHE_BUILD = "label.impacted.cache.message.build.success";

	private static final String CACHE_REAPER_JSP = "cacheReaper";
	private static final String IMPACTED_CACHE_JSP = "impactedCache";

	@Inject
	@Named(FWCacheConstants.CACHE_MANAGER)
	private CacheManager cacheManager;

	@Inject
	@Named("masterCacheService")
	private MasterCacheService masterCacheService;

	@Inject
	@Named("cacheCommonService")
	private CacheCommonService cacheCommonService;

	@RequestMapping(value = "/getAllCacheRegions", method = RequestMethod.GET)
	public String getAllCacheRegions(ModelMap masterMap) {
		Set<String> regionSet = cacheManager.getCacheRegionNames();
		masterMap.put("regionList", regionSet);
		return CACHE_REAPER_JSP;
	}

	@RequestMapping(value = "/getAllCacheGroupsForRegion/{region}", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody String getAllCacheGroupsForRegion(@PathVariable("region") String regionName) {
		NeutrinoValidator.notEmpty(regionName, "RegionName cannot be NULL or EMPTY");
		List<CacheMasterVO> list = masterCacheService.getCacheGroupStatusFromMasterCache(regionName);

		SimpleDataTableJsonHepler jsonHelper = new SimpleDataTableJsonHepler();
		jsonHelper.setAaData(list);

		JSONSerializer iSerializer = new JSONSerializer();
		return iSerializer.exclude("*.class").transform(date_transformer, DateTime.class).deepSerialize(jsonHelper);
	}

	@RequestMapping(value = "/getCacheNamesForGroup/{group}", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody String getCacheNamesForGroup(@PathVariable("group") String groupName) {
		NeutrinoValidator.notEmpty(groupName, "GroupName cannot be NULL or EMPTY");
		List<Object> list = masterCacheService.getIndividualCacheStatusFromCacheGroup(groupName);

		SimpleDataTableJsonHepler jsonHelper = new SimpleDataTableJsonHepler();
		jsonHelper.setAaData(list);

		JSONSerializer iSerializer = new JSONSerializer();
		return iSerializer.exclude("*.class").transform(date_transformer, DateTime.class).deepSerialize(jsonHelper);
	}

	@PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
	@RequestMapping(value = "/markForCacheRefresh", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> markForCacheRefresh(
			@RequestParam(required = true, value = "cacheGroupNames[]") String[] cacheGroupNames) {
		Map<String, Object> responseMap = new HashMap<>();
		Boolean successStatus = masterCacheService.markCacheGroupListForRefresh(Arrays.asList(cacheGroupNames));
		if (successStatus) {
			responseMap.put(MESSAGE_KEY, messasgeFromMessageResource(SUCCESS_MSG_KEY_CACHE_REFRESH));
			responseMap.put(ERROR, false);
		} else {
			responseMap.put(MESSAGE_KEY, messasgeFromMessageResource(ERROR_MSG_KEY_CACHE_REFRESH));
			responseMap.put(ERROR, true);
		}
		return responseMap;
	}

	@RequestMapping(value = "/getImpactedCacheMaster", method = RequestMethod.GET)
	public String getImpactedCacheMaster(ModelMap masterMap) {
		return IMPACTED_CACHE_JSP;
	}

	@RequestMapping(value = "/getAllImpactedCaches", method = { RequestMethod.GET, RequestMethod.POST })
	public @ResponseBody String getAllImpactedCaches() {
		List<ImpactedCacheVO> impactedCacheVOs = cacheCommonService.getFailedImpactedCacheVOs();

		SimpleDataTableJsonHepler jsonHelper = new SimpleDataTableJsonHepler();
		jsonHelper.setAaData(impactedCacheVOs);

		JSONSerializer iSerializer = new JSONSerializer();
		return iSerializer.exclude("*.class").transform(date_transformer, DateTime.class).deepSerialize(jsonHelper);
	}

	@PreAuthorize("hasAuthority('ADMIN_AUTHORITY')")
	@RequestMapping(value = "/buildImpactedCaches", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> buildImpactedCaches(
			@RequestParam(required = true, value = "cacheIdentifierSet[]") String[] cacheIdentifierSet) {

		Map<String, Object> responseMap = new HashMap<>();

		if (cacheCommonService.updateImpactedCacheLastUpdatedTime(cacheIdentifierSet)) {
			cacheCommonService.buildImpactedCaches(cacheIdentifierSet);
			responseMap.put(MESSAGE_KEY, messasgeFromMessageResource(SUCCESS_MSG_KEY_IMPACTED_CACHE_BUILD));
			responseMap.put(ERROR, false);
		} else {
			responseMap.put(MESSAGE_KEY, messasgeFromMessageResource(ERROR_MSG_KEY_IMPACTED_CACHE_BUILD));
			responseMap.put(ERROR, true);
		}

		return responseMap;
	}

	private final Transformer date_transformer = new AbstractTransformer() {
		@Override
		public void transform(Object object) {

			DateTimeFormatter format = DateTimeFormat.forPattern(DATE_FORMAT_PATTERN);
			getContext().write("\"" + format.print((DateTime) object) + "\"");
		}
	};

}