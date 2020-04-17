package com.nucleus.shortcut;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.cache.common.NeutrinoCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.web.common.controller.BaseController;


@Transactional
@Controller
public class HotKeyController extends BaseController {

	@Inject
	@Named("hotkeyCachePopulator")
	private NeutrinoCachePopulator hotkeyCachePopulator;

	@Inject
	@Named("hotkeyService")
	private HotkeyService hotkeyService;

	@Inject
	@Named("hotkeyUIService")
	private HotkeyUIServiceImpl hotkeyUIService;
	
	private static String moduleName;
	
	@PostConstruct
	public void init() {
		moduleName=ProductInformationLoader.getProductName();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/gethotkeys")
	public @ResponseBody List<HotKeyUI> getHotKeys(@RequestParam(required = false) String type) {

		if (type != null && !type.isEmpty()) {
			return (List<HotKeyUI>) hotkeyCachePopulator.get(new StringBuilder(moduleName).append(FWCacheConstants.KEY_DELIMITER).append(type).toString());
		} else {
			List<Hotkeys> data = hotkeyService.getAllHotKeys();
			return hotkeyUIService.prepareData(data, type);
		}
	}
	
	

}