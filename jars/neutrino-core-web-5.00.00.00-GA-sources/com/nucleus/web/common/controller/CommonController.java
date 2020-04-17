package com.nucleus.web.common.controller;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.genericparameter.service.GenericParameterServiceImpl;
import com.nucleus.rules.model.SourceProduct;

@Controller
@RequestMapping("/common")
public class CommonController {

	@Inject
	@Named("genericParameterService")
	private GenericParameterServiceImpl genericParameterService;

	@RequestMapping("/getModuleNameByModuleCode")
	@ResponseBody
	public String getModuleNameByModuleCode(@RequestParam("moduleCode") String moduleCode) {
		SourceProduct sourceProduct = genericParameterService.findByCode(moduleCode, SourceProduct.class);
		if(sourceProduct !=null) {
			return sourceProduct.getName();
		}
		return null;
	}
}
