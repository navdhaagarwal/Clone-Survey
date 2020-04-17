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

import com.nucleus.era.Era;

/**
 * The interface defines the various methods to be used for handling locale based dates
 *
 */

/**
 * @author Nucleus Software Exports Limited
 * TODO -> sanu.gupta Add documentation to class
 */
/**
 * @author Nucleus Software Exports Limited
 * TODO -> sanu.gupta Add documentation to class
 */
public interface DateService {

    /**
     * 
     * @param year
     * @return Era in which the year falls
     */
    public Era getEraBasedOnYear(Integer year);

    /**
     * 
     * @param yearOfKing
     * @return the startYear of the particular king
     */
    public Integer getEraBasedOnYearOfKing(Character yearOfKing);
    
    /**
     * 
     * @param yearOfKing
     * @return the EraLimit of the particular king
     */
    public Integer getEraLimitBasedOnYearOfKing(Character yearOfKing);
   
    /**
     * 
     * @return the MaxStartYear from the era table
     */  
    public Integer getMaxStartYear();
}
