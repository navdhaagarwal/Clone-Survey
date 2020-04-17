package com.nucleus.shortcut;

import java.util.List;

public interface HotkeyService {

	
	public List<Hotkeys> getHotKeysBasedOnType(String type);
	
	public List<Hotkeys> getAllHotKeys();
	
}
