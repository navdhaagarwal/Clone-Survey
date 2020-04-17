/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.dynamicQuery.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public interface DynamicFormDataQueryService {

    public List<Map<String, Object>> getFormDataByDate(Long dynamicFormId, DateTime from, DateTime to,
            Set<Long> projectionFields, Long groupById);

    public List<Map<String, Object>> getFormDataByCriteriaQuery(Long dynamicFormId, String queryString,
            Set<Long> projectionFields, Long groupById);

}
