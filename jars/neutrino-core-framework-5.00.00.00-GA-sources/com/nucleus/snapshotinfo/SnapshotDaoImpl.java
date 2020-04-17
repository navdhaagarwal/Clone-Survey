package com.nucleus.snapshotinfo;

import java.util.List;

import com.nucleus.persistence.BaseDaoImpl;

public class SnapshotDaoImpl extends BaseDaoImpl<SnapshotInfo> implements SnapshotInfoDao {

    @Override
    public List<SnapshotInfo> findByClazzNameAndSourceEntityId(String clazzName, Long sourceEntityId) {
        // Query querySnapshotInfos = em.createNativeQuery(
        // "Select * from EMP_TABLE where Salary < #salary", Employee.class);
        // queryEmployeeByFirstName.setParameter("salary", 50000);
        return null;
    }

}
