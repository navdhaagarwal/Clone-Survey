package com.nucleus.api.documentation.service;

import java.util.List;

import com.nucleus.api.documentation.entity.ApiMessageCode;

public interface ApiDocumentationService {

	public List<ApiMessageCode> getApiMessageCodesByModuleCode(String moduleCode);
}
