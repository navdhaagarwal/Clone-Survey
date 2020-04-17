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
package com.nucleus.core.persistence.jdbc;

import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public class PersistenceUtils {

    private static final int[] AUTO_FILL_RANGES = { 0, 10, 50, 100, 200, 500, 900 };
    private static final int   LOOP_END         = AUTO_FILL_RANGES.length - 1;

    public static <T> void resizeListWithAutoFill(List<T> originalList) {

        int size = originalList.size();

        if (size == 0) {
            return;
        }

        int elementsToAdd = 0;
        T elementToRepeat = originalList.get(0);
        for (int i = 0 ; i < LOOP_END ; i++) {
            if (AUTO_FILL_RANGES[i] < size && size < AUTO_FILL_RANGES[i + 1]) {
                elementsToAdd = AUTO_FILL_RANGES[i + 1] - size;
                break;
            }
        }

        for (int i = 0 ; i < elementsToAdd ; i++) {
            originalList.add(elementToRepeat);
        }

    }

}
