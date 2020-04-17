package com.nucleus.core.rules.comparison.service;

import java.util.List;
import java.util.Map;

import com.nucleus.service.BaseService;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Interface exposes the service to compare two objects
 *          Return the modified target object
 *          Returns the list of different field with full path
 */

public interface CompareUtlityService extends BaseService {

    /**
     * 
     * @param base 
     *       Base Object to be used as base for comparison
     *       
     * @param compareTo 
     *       Object to be compared with base object
     *       
     * @param target
     *       Target Object which will be modified based on 
     *          difference between Base Object and Compared Object
     *          
     * @param onlyUseFields
     *       This is used for matching given set of fields only for a particular class
     *        A HashMap representing 
     *              class as key
     *              values as comma separated string of fields to be matched
     *              eg: onlyUseFields.put(Address.class, "addressType,addressLine1,addressLine2,country,state");
     *               
     * @param ignorableFieldMap
     *       This is used for ignoring given set of fields only for a particular class
     *        A HashMap representing 
     *              class as key
     *              values as comma separated string of fields to be ognored
     *              eg: ignoreFields.put(IdentificationDetail.class, "id,associatedDocument");
     *                   ignoreFields.put(BaseEntity.class, "entityLifeCycleData, uuid");
     *                   
     * @param mandateFieldMap
     *       This is used for match given set of fields only for a particular class
     *       Use Case : Dtypes in collections, If same dtype is not present while comparing, 
     *                  then that particular item in collection is set to null.
     *        A HashMap representing 
     *              class as key
     *              values as comma separated string of fields to be ognored
     *              mandateFieldMap.put(Address.class, "addressType");
     *              mandateFieldMap.put(PhoneNumber.class, "numberType");
     *                   
     * @param ignoreCommonFields
     *       This is the list containing fields to be removed across entities.
     *       eg : ignoreCommonFields.add("serialVersionUID");
     *       
     * @return
     *      HashMap class instance, with key/value
     *          1.) Difference Field Path
     *              Key - ComparisonConstants.DIFFERENT_FIELDS_PATH
     *              Value - List<String>
     *              
     *          2.) Modified Target Object
     *              Key - ComparisonConstants.TARGET_AFTER_MODIFICATION
     *              value - Object 
     */

    public Map<String, Object> compareObjects(Object base, Object compareTo, Object target,
            Map<Class, String> onlyUseFields, Map<Class, String> ignorableFieldMap, Map<Class, String> mandateFieldMap,
            List<String> ignoreCommonFields);
}
