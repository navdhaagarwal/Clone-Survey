package com.nucleus.shortcut;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import com.nucleus.service.BaseServiceImpl;

@Named("hotkeyUIService")
public class HotkeyUIServiceImpl extends BaseServiceImpl{

	public List<HotKeyUI> prepareData(List<Hotkeys> hotkeys, String type)
	{
		List<HotKeyUI> finalData = new ArrayList<HotKeyUI>();
		for (Hotkeys hk : hotkeys) {
			HotKeyUI hotkeyUI = new HotKeyUI();
			hotkeyUI.setShortCutKey(hk.getHotKey());
			if (HotKeyType.NAVIGATION.getHotKeyTypeCode().equals(type)) {
				hotkeyUI.setShortCutKeySuggestion(hk.getElementMapping().get(0).getShortCutKeySuggestion());
			}
			hotkeyUI.setIdentifierList(getRelatedToList(hk.getElementMapping()));
			hotkeyUI.setDescription(hk.getDescription());
			finalData.add(hotkeyUI);
		}

		return finalData;
	}
	
	private String updateIdentifier(String identifier, Boolean identifierType) {
		if (identifierType) {
			return "#" + identifier;
		} else {
			return identifier;
		}
	}

	
	private List<String> updateIdentifierList(List<HotkeyElementIdentifier> hKEIs) {
		List<String> newData = new ArrayList<String>();
		for (HotkeyElementIdentifier hkEI : hKEIs) {
			newData.add(updateIdentifier(hkEI.getIdentifier(), hkEI.isIdentifierType()));
		}
		return newData;
	}

	
	private List<List<String>> getRelatedToList(List<HotkeyElementIdentifier> data) {
		List<List<String>> finalData = new ArrayList<List<String>>();

		for (int j = 0; j < data.size(); j++) {
			
			List<String> newData = new ArrayList<String>();
			
			newData.add(updateIdentifier(data.get(j).getIdentifier(), data.get(j).isIdentifierType()));
			newData.addAll(updateIdentifierList(data.get(j).getRelatedToId()));
			finalData.add(newData);
		}

		return finalData;
	}
	
}
