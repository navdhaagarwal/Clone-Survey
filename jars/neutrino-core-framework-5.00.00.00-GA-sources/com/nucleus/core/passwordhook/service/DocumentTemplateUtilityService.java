package com.nucleus.core.passwordhook.service;

import java.util.Map;

import com.nucleus.letterMaster.LetterType;

public interface DocumentTemplateUtilityService {
	  Map<String, Object> documentType(LetterType document,Map contextmap);
}
