/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.hibernate.search;
import org.hibernate.search.bridge.StringBridge;

import com.nucleus.core.money.entity.Money;


/**
 * @author Nucleus Software Exports Limited
 *
 */
public class MoneyToStringOneWayBridge implements StringBridge {


    @Override
    public String objectToString(Object object) {

        if (object == null) {
            return null;
        }
        if (Money.class.isAssignableFrom(object.getClass())) {
           
            Money money = (Money) object;
            return money.getNonBaseAmount().getValue().toString();
        } 
        else if(String.class.isAssignableFrom(object.getClass())){
            return (String) object;
        }

        else {
            throw new RuntimeException("MoneyToStringOneWayBridge can not be used for type:" + object.getClass().getName());
        }
    }
}
