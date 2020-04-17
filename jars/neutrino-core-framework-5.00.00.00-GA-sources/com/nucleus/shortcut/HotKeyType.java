package com.nucleus.shortcut;

public enum HotKeyType {
	ACTION ("0"),
	NAVIGATION ("1"),
	NOT_ALLOWED ("2"),
	HIDDEN_ACTION ("3");
	
	private String hotKeyTypeCode;
	
	private HotKeyType(String hotKeyTypeCode){
		 this.hotKeyTypeCode = hotKeyTypeCode;
	}
	
	public String getHotKeyTypeCode() {
        return this.hotKeyTypeCode;
    }
}