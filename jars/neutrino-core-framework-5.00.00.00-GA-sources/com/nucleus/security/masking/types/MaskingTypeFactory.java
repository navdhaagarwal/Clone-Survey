package com.nucleus.security.masking.types;

import java.util.Map;

public class MaskingTypeFactory {

	private Map<String, MaskingType> map;
	
	public MaskingType getMaskingType(String type){
		
		return map.get(type);
	}

	public Map<String, MaskingType> getMap() {
		return map;
	}

	public void setMap(Map<String, MaskingType> map) {
		this.map = map;
	}

	
}
