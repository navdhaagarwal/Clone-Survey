/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.master;

import java.util.List;

import javax.transaction.Transactional;

import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.web.binder.AbstractWebDataBinder;

/**
 * @author Nucleus Software Exports Limited
 */
@Transactional
public class GenericParameterBinder extends AbstractWebDataBinder<List<? extends GenericParameter>> {

    private final Class<? extends GenericParameter> clazz;

    public GenericParameterBinder(Class<? extends GenericParameter> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<? extends GenericParameter> getData() {
        GenericParameterService genericParameterService = (GenericParameterService) getWebApplicationContext().getBean(
                "genericParameterService");
        return genericParameterService.retrieveTypes(clazz);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " for class: " + clazz.getName();
    }

    @Override
    public List<? extends GenericParameter> getData(Object code) {
        /* if (code != null) {
             parentCode=(String) code;
             GenericParameterService genericParameterService = (GenericParameterService) getWebApplicationContext().getBean(
                     "genericParameterService");
             if (clazz.equals(LoanPurpose.class)) {
                 //here the parentCode for LoanPurpose is the shortname of ProductType 
                return genericParameterService.findChildrenByParentCode(parentCode , LoanPurpose.class);
             }
         }*/
        return null;
    }
}
