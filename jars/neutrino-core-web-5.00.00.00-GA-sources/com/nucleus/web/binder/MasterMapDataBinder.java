/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - � 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.web.binder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import com.nucleus.master.BaseMasterService;

/**
 * @author Nucleus Software Exports Limited
 */
@Transactional
public class MasterMapDataBinder extends AbstractWebDataBinder<List<?>> {

    private final Class<?> clazz;
    private String[]       columnNameList;
    private Map<String,Object>       whereList;

    public MasterMapDataBinder(Class<?> clazz) {
        this.clazz = clazz;
    }

    public MasterMapDataBinder(Class<?> clazz, String... columnNameList) {
        super();
        this.clazz = clazz;
        this.columnNameList = columnNameList;
    }
    public MasterMapDataBinder(Class<?> clazz,  Map<String,Object> whereList, String... columnNameList) {
        super();
        this.clazz = clazz;
        this.columnNameList = columnNameList;
        this.whereList = whereList;
    }

    @Override
    public List<?> getData() {
        BaseMasterService baseMasterService = (BaseMasterService) getWebApplicationContext().getBean("baseMasterService");
        List<Map<String, Object>> dataForEntity;
        if(whereList !=null){
        	dataForEntity = baseMasterService.getApprovedAndActiveSelectedListEntitiesForGivenCriteria(clazz,whereList,columnNameList);
        }else{
        dataForEntity = baseMasterService.getAllApprovedAndActiveSelectedListEntities(clazz,
                columnNameList);
        }
        Map<String, Object> mapOthers = null;

        Iterator<Map<String, Object>> it = dataForEntity.iterator();
        while (it.hasNext()) {
            Map<String, Object> mapData = it.next();

            if (mapData.containsValue("OTHERS") || mapData.containsValue("others") || mapData.containsValue("Others")
                    || mapData.containsValue("OTHER") || mapData.containsValue("other")) {
                mapOthers = mapData;
                it.remove();
                break;
            }
        }
        if (mapOthers != null) {
            dataForEntity.add(mapOthers);
        }

        return dataForEntity;

    }

    @Override
    public String toString() {
        return "MasterDataBinder for class:" + clazz.getName();
    }

}
