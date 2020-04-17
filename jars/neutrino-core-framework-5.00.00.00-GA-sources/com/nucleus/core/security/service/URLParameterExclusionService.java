package com.nucleus.core.security.service;

import java.util.List;

import com.nucleus.core.security.entities.AdditionalBlackListPattern;
import com.nucleus.core.security.entities.UnfilteredRequestUri;

public interface URLParameterExclusionService {
	List<UnfilteredRequestUri> findAllUnfilteredRequestUris();
	List<AdditionalBlackListPattern>findAllAdditionalBlackListPatterns();
}
