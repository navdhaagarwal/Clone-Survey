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
package com.nucleus.process;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nucleus Software Exports Limited
 */
public class WorkflowFormConfig implements Serializable {

    private static final long serialVersionUID = -8114332419398126621L;

    private Set<String>       enabledForms     = Collections.emptySet();
    private Set<String>       visibleForms     = Collections.emptySet();

    /*
     * Delibrate default method
     */
    void setEnabledForms(List<Object> enabledForms) {
        Set<String> tempSet = new LinkedHashSet<String>();
        for (Object enabledForm : enabledForms) {
            tempSet.add(enabledForm.toString());
        }
        this.enabledForms = Collections.unmodifiableSet(tempSet);
    }

    /*
     * Delibrate default method
     */
    void setVisibleForms(List<Object> visibleForms) {
        Set<String> tempSet = new LinkedHashSet<String>();
        for (Object visibleForm : visibleForms) {
            tempSet.add(visibleForm.toString());
        }
        this.visibleForms = Collections.unmodifiableSet(tempSet);
    }

    public Set<String> getEnabledForms() {
        return enabledForms;
    }

    public Set<String> getVisibleForms() {
        return visibleForms;
    }

    /**
     * return: 0=Unknown 100=Visible 200=Enabled
     */
    public int getFormMode(String formName) {
        if (enabledForms.contains(formName)) {
            return 200;
        }
        if (visibleForms.contains(formName)) {
            return 100;
        }
        return 0;
    }

    public boolean isVisible(String formName) {
        return visibleForms.contains(formName);
    }

    public boolean isEnabled(String formName) {
        return enabledForms.contains(formName);
    }

    public void merge(WorkflowFormConfig formConfig) {
        Set<String> tempEnabledForms = new LinkedHashSet<String>(enabledForms);
        tempEnabledForms.addAll(formConfig.getEnabledForms());
        this.enabledForms = Collections.unmodifiableSet(tempEnabledForms);
        Set<String> tempVisibleForms = new LinkedHashSet<String>(visibleForms);
        tempVisibleForms.addAll(formConfig.getVisibleForms());
        this.visibleForms = Collections.unmodifiableSet(tempVisibleForms);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((enabledForms == null) ? 0 : enabledForms.hashCode());
        result = prime * result + ((visibleForms == null) ? 0 : visibleForms.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WorkflowFormConfig other = (WorkflowFormConfig) obj;
        if (enabledForms == null) {
            if (other.enabledForms != null)
                return false;
        } else if (!enabledForms.equals(other.enabledForms))
            return false;
        if (visibleForms == null) {
            if (other.visibleForms != null)
                return false;
        } else if (!visibleForms.equals(other.visibleForms))
            return false;
        return true;
    }

}
