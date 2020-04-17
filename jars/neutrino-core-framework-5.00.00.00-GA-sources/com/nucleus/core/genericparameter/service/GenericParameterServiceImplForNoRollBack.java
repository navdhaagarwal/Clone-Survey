package com.nucleus.core.genericparameter.service;

import javax.inject.Named;

import org.springframework.transaction.annotation.Transactional;

/**
 * 
 * 
 * This class has been added to avoid any roll-back occurring in the transaction
 * due to exceptions while calling genericParameterServiceImpl functions
 * 
 */
@Named("genericParameterServiceForNoRollBack")
@Transactional(noRollbackFor = Exception.class)
public class GenericParameterServiceImplForNoRollBack extends
		GenericParameterServiceImpl {

}
