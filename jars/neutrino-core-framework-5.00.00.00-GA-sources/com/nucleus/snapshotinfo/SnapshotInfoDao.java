
package com.nucleus.snapshotinfo;

import java.util.List;

import com.nucleus.persistence.BaseDao;

/**
 * @author amit.parashar
 * 
 */
public interface SnapshotInfoDao extends BaseDao<SnapshotInfo> {

	public List<SnapshotInfo> findByClazzNameAndSourceEntityId(String clazzName, Long sourceEntityId);

}
