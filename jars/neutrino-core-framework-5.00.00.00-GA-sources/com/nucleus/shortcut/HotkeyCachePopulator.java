package com.nucleus.shortcut;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.core.initialization.ProductInformationLoader;
import com.nucleus.finnone.pro.base.exception.SystemException;
import com.nucleus.finnone.pro.cache.common.FWCachePopulator;
import com.nucleus.finnone.pro.cache.constants.FWCacheConstants;
import com.nucleus.logging.BaseLoggers;

@Named("hotkeyCachePopulator")
public class HotkeyCachePopulator extends FWCachePopulator {

	@Inject
	@Named("hotkeyService")
	private HotkeyService hotkeyService;

	@Inject
	@Named("hotkeyUIService")
	private HotkeyUIServiceImpl hotkeyUIService;

	
	private String moduleName;
	
	
	
	@Override
	public void init() {
		BaseLoggers.flowLogger.debug("Init Called : HotkeyCachePopulator");
		moduleName=ProductInformationLoader.getProductName();
	}

	@Override
	public Object fallback(Object key) {
		String[] resultArray = ((String) key).split(FWCacheConstants.REGEX_DELIMITER, 2);
		String hotKeyType = resultArray[1];
		if (hotKeyType != null && !hotKeyType.isEmpty()) {
			List<Hotkeys> data = hotkeyService.getHotKeysBasedOnType(hotKeyType);
			return hotkeyUIService.prepareData(data, hotKeyType);
		}
		return null;
	}

	@Override
	public void build(Long tenantId) {
		List<Hotkeys> hotkeysData = null;
		for (HotKeyType hotKeyType : HotKeyType.values()) {
			if (!(HotKeyType.NOT_ALLOWED.getHotKeyTypeCode().equals(hotKeyType.getHotKeyTypeCode()))) {
				hotkeysData = hotkeyService.getHotKeysBasedOnType(hotKeyType.getHotKeyTypeCode());
				put(new StringBuilder(moduleName).append(FWCacheConstants.KEY_DELIMITER).append(hotKeyType.getHotKeyTypeCode()),hotkeyUIService.prepareData(hotkeysData, hotKeyType.getHotKeyTypeCode()));
			}
		}
	}

	@Override
	public void update(Action action, Object object) {
		BaseLoggers.flowLogger.debug("Update Called : HotkeyCachePopulator");
		throw new SystemException(UPDATE_ERROR_MSG + getNeutrinoCacheName());
	}

	@Override
	public String getNeutrinoCacheName() {
		return FWCacheConstants.FW_HOTKEY_CACHE;
	}

	@Override
	public String getCacheGroupName() {
		return FWCacheConstants.HOTKEY_CACHE_GROUP;
	}

}
