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
package com.nucleus.core.web.dynamicQuery.staticBuilder;

import java.util.List;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
public class StaticQueryBuilderConfiguration {

    private boolean                        sortable;

    private List<StaticQueryBuilderFilter> filters;

    private List<String>                   conditions;

    private String                         default_condition = "AND";

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public List<StaticQueryBuilderFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<StaticQueryBuilderFilter> filters) {
        this.filters = filters;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public String getDefault_condition() {
        return default_condition;
    }

    public void setDefault_condition(String default_condition) {
        this.default_condition = default_condition;
    }

}
