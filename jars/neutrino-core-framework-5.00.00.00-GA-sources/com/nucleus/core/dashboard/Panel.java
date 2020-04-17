package com.nucleus.core.dashboard;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Panel implements Serializable {

    private static final String TASK_ID = "Task_ID";

    public Panel(String name, List<String> filterableColumns, List<Map<String, String>> taskData) {
        this.name = name;
        this.filterableColumns = filterableColumns;
        Set<String> uniqueTaskId = new HashSet<>();
        if (taskData != null) {
            for (Iterator iterator = taskData.iterator() ; iterator.hasNext() ;) {
                Map<String, String> map = (Map<String, String>) iterator.next();
                if (map.get(TASK_ID) != null && !uniqueTaskId.add(map.get(TASK_ID))) {
                    // duplicate task id
                    iterator.remove();
                }
            }
        }
        this.taskData = taskData;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getFilterableColumns() {
        return filterableColumns;
    }

    public void setFilterableColumns(List<String> filterableColumns) {
        this.filterableColumns = filterableColumns;
    }

    public List<Map<String, String>> getTaskData() {
        return taskData;
    }

    public void setTaskData(List<Map<String, String>> taskData) {
        this.taskData = taskData;
    }

    private List<String>              filterableColumns;
    private List<Map<String, String>> taskData;
}
