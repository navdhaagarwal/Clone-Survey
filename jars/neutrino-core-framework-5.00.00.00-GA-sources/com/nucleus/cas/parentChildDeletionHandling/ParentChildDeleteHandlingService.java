package com.nucleus.cas.parentChildDeletionHandling;

import java.util.List;

public interface ParentChildDeleteHandlingService {
    List<DependencyUsageVO> prepareDependencyData(String masterId,Long id);
    List<Object> executeHQL(String query,Long id);
}
