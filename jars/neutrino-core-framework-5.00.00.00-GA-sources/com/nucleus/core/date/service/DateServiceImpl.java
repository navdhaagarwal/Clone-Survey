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
package com.nucleus.core.date.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.era.Era;
import com.nucleus.persistence.EntityDao;

/**
 * The service implements the DateService
 * @author sandeep.grover
 *
 */

@Named("dateService")
public class DateServiceImpl implements DateService {

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    /**
     * 
     * @param year
     * @return Era in which the year falls
     */
    public Era getEraBasedOnYear(Integer year) {
        NamedQueryExecutor<Era> getEraBasedOnYear = new NamedQueryExecutor<Era>("Date.getEraBasedOnYear").addParameter(
                "year", year);

        Era era = entityDao.executeQueryForSingleValue(getEraBasedOnYear);
        if (era != null) {
            return era;
        }
        return null;
    }

    /**
     * 
     * @param yearOfKing
     * @return the startYear of the particular king
     */
    public Integer getEraBasedOnYearOfKing(Character yearOfKing) {
        NamedQueryExecutor<Integer> getEraBasedOnYearOfKing = new NamedQueryExecutor<Integer>("Date.getEraBasedOnYearOfKing")
                .addParameter("symbol", yearOfKing);

        Integer startYear = entityDao.executeQueryForSingleValue(getEraBasedOnYearOfKing);
        if (startYear != null) {
            return startYear;
        }
        return null;
    }

    /**
     * 
     * @param yearOfKing
     * @return the EraLimitYear of the particular king
     */
    public Integer getEraLimitBasedOnYearOfKing(Character yearOfKing) {
        NamedQueryExecutor<Integer> getEraLimitBasedOnYearOfKing = new NamedQueryExecutor<Integer>(
                "Date.getEraLimitBasedOnYearOfKing").addParameter("symbol", yearOfKing);
        Integer limitYear = entityDao.executeQueryForSingleValue(getEraLimitBasedOnYearOfKing);
        if (limitYear != null) {
            return limitYear;
        }
        return null;
    }
    /**
     * 
     * @return the MaxStartYear from the era table
     */
    public Integer getMaxStartYear()
    {
        NamedQueryExecutor<Integer> getMaxStartYear=new NamedQueryExecutor<Integer>("Date.getMaxStartYear");
        Integer maxStartYear=entityDao.executeQueryForSingleValue(getMaxStartYear);
        if(maxStartYear!=null){
            return maxStartYear;
        }
    return null;
    }
}
