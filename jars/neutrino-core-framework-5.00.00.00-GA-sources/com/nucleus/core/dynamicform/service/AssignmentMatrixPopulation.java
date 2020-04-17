package com.nucleus.core.dynamicform.service;

import java.util.List;
import java.util.Map;

public interface AssignmentMatrixPopulation {
    public List<Map<String, ?>>  searchOnAssignmetMaster(String itemVal, String[] searchColumnList, String value);
}
