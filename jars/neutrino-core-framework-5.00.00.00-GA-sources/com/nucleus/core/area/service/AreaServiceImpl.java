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
package com.nucleus.core.area.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Named;
import com.nucleus.address.ZipCode;

import net.bull.javamelody.MonitoredWithSpring;

import com.nucleus.address.ZipCode;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.logging.BaseLoggers;
/**
 * 
 * @author Nucleus Software Exports Limited
 */
@Named("areaService")
@MonitoredWithSpring(name = "Area_Service_IMPL_")
public class AreaServiceImpl extends BaseServiceImpl implements AreaService {
    private static final int DEFAULT_PAGE_SIZE =3;

	
    @Override
    public List<ZipCode> getZipCodesByCityId(Long cityId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<ZipCode> zipcodeCriteria = new NamedQueryExecutor<ZipCode>(
                "ZipCode.ZipCodesByCityId").addParameter("cityId", cityId)
                .addParameter("approvalStatus", statusList);
        return entityDao.executeQuery(zipcodeCriteria);
    }

    
    
    @Override
    public List<Map<String, ?>> getZipCodesByCityId(Long cityId,int page) {
        List<ZipCode> zipCodeList = null;
       // List<Map<String,?>> zipMapCodeList =null; 
        List<Map<String, ?>> finalResult = new ArrayList<Map<String, ?>>();
         int counter = 0;
         long totalRecords = 0;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<ZipCode> zipcodeCriteria = new NamedQueryExecutor<ZipCode>(
                "ZipCode.ZipCodesByCityId").addParameter("cityId", cityId)
                .addParameter("approvalStatus", statusList);
    //    return entityDao.executeQuery(zipcodeCriteria);
    
    
    
    
   zipCodeList = entityDao.executeQuery(zipcodeCriteria, page * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);
   
  // zipMapCodeList = new ArrayList<>();

  // List<ZipCode> zips = []...

   
  List<Map<String,?>> list = new ArrayList<Map<String,?>>();
  /* zipCodeList.forEach(zip->{
   Map<String,Long> zipMap=new HashMap();     	   
	   zipMap.put("zip",zip.getId());
	   zipMapCodeList.add(zipMap);
   });
*/
  for(ZipCode zipCode:zipCodeList){
		
		 Map<String,Object> zipMap=new HashMap();     	   
		   zipMap.put("id",zipCode.getId());
		   zipMap.put("zipCode",zipCode.getZipCode());
		 //list.add(zipMap);
	     finalResult.add(counter, zipMap);
	        counter++;
	}
    
   
   
    

   /* for (Map<String, ?> temp : list) {
        finalResult.add(counter, temp);
        counter++;
    }*/
    totalRecords = totalRecords + entityDao.executeTotalRowsQuery(zipcodeCriteria);


Map<String, Long> sizeMap = new HashMap<String, Long>();

sizeMap.put("size",totalRecords);
finalResult.add(counter, sizeMap);
if (finalResult != null) {
    BaseLoggers.flowLogger.debug("size of finalResult :" + finalResult.size());
}       
    
    return finalResult;
}

    @Override
    public List<ZipCode> getZipCodesByVillageId(Long villageId) {
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<ZipCode> zipcodeCriteria = new NamedQueryExecutor<ZipCode>(
                "ZipCode.ZipCodesByVillageId").addParameter("villageId", villageId)
                .addParameter("approvalStatus", statusList);
        return entityDao.executeQuery(zipcodeCriteria);
    }

    @Override
    public List<Map<String, ?>> getZipCodesByVillageId(Long villageId, int page) {
        List<ZipCode> zipCodeList;
        List<Map<String, ?>> finalResult = new ArrayList<Map<String, ?>>();
        int counter = 0;
        long totalRecords = 0;
        List<Integer> statusList = new ArrayList<Integer>();
        statusList.add(ApprovalStatus.APPROVED);
        statusList.add(ApprovalStatus.APPROVED_MODIFIED);
        statusList.add(ApprovalStatus.APPROVED_DELETED);
        statusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
        NamedQueryExecutor<ZipCode> zipcodeCriteria = new NamedQueryExecutor<ZipCode>(
                "ZipCode.ZipCodesByVillageId").addParameter("villageId", villageId)
                .addParameter("approvalStatus", statusList);
        zipCodeList = entityDao.executeQuery(zipcodeCriteria, page * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);


        List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
        for (ZipCode zipCode : zipCodeList) {

            Map<String, Object> zipMap = new HashMap<>();
            zipMap.put("id", zipCode.getId());
            zipMap.put("zipCode", zipCode.getZipCode());
            finalResult.add(counter, zipMap);
            counter++;
        }

        totalRecords = totalRecords + entityDao.executeTotalRowsQuery(zipcodeCriteria);

        Map<String, Long> sizeMap = new HashMap<String, Long>();

        sizeMap.put("size", totalRecords);
        finalResult.add(counter, sizeMap);
        BaseLoggers.flowLogger.debug("Size of finalResult :" + finalResult.size());
        return finalResult;
    }
}
