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
package com.nucleus.core.dynamicQuery.support;

import java.util.List;

import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class MapSelectExpressionItem extends SelectExpressionItem {

    private List<SelectExpressionItem> selectItems;

    public MapSelectExpressionItem(List<SelectExpressionItem> selectItems) {
        super();
        this.selectItems = selectItems;
    }

    public List<SelectExpressionItem> getSelectItems() {
        return selectItems;
    }

    public void setSelectItems(List<SelectExpressionItem> selectItems) {
        this.selectItems = selectItems;
    }

    @Override
    public String toString() {
        return "new Map" + PlainSelect.getStringList(selectItems, true, true);
    }

}
