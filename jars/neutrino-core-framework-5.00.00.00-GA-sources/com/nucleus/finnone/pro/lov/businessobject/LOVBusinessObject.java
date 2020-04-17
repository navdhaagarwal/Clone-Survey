package com.nucleus.finnone.pro.lov.businessobject;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasElements;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.BaseEntity;
import com.nucleus.finnone.pro.lov.LOVDynamicQueryExecutor;
import com.nucleus.finnone.pro.lov.LOVFilterVO;
import com.nucleus.finnone.pro.lov.LOVSearchVO;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.persistence.BaseMasterDao;

@Named("lovBusinessObject")
public class LOVBusinessObject implements ILOVBusinessObject {
  
  @Inject
  @Named("baseMasterDao")
  private BaseMasterDao baseMasterDao;
  
  @Override
  public int getTotalRecordSize(Class<Serializable> entityClass) {
    List<Integer> statusList = getApprovalStatusList();
    
    return this.baseMasterDao.getTotalRecords(entityClass, statusList);
    
  }
  
  private List<Integer> getApprovalStatusList() {
	    List<Integer> approvalStatusList = new ArrayList<>();
	    approvalStatusList.add(ApprovalStatus.APPROVED);
	    approvalStatusList.add(ApprovalStatus.APPROVED_MODIFIED);
	    approvalStatusList.add(ApprovalStatus.APPROVED_DELETED);
	    approvalStatusList.add(ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
	    return approvalStatusList;
  }



/**
   * 
   * @param entityClass
   * @param lovSearchVO
   * @return
   */
  @Override
  public <T extends BaseMasterEntity> List<T> loadPaginatedMasterEntityData(Class<T> entityClass, LOVSearchVO lovSearchVO) {
    if ((entityClass == null) || (lovSearchVO.getSearchMap() == null)) {
      return Collections.emptyList();
    }
    LOVDynamicQueryExecutor<T> lovDynamicQueryExecutor = new LOVDynamicQueryExecutor<T>(entityClass);
    prepareDynamicQueryExecutor(lovDynamicQueryExecutor, entityClass, lovSearchVO);
    
    return this.baseMasterDao.executeQuery(lovDynamicQueryExecutor, lovSearchVO.getiDisplayStart(), lovSearchVO.getiDisplayLength() + 1);
  }
  
  /**
   * 
   * @param entityClass
   * @param lovSearchVO
   * @return
   */
  @Override
  public <T extends BaseEntity> List<T> loadPaginatedEntityData(Class<T> entityClass, LOVSearchVO lovSearchVO) {
    if ((entityClass == null) || (lovSearchVO.getSearchMap() == null)) {
      return Collections.emptyList();
    }
    LOVDynamicQueryExecutor<T> lovDynamicQueryExecutor = new LOVDynamicQueryExecutor<T>(entityClass);
    prepareDynamicQueryExecutor(lovDynamicQueryExecutor, entityClass, lovSearchVO);
    
    return this.baseMasterDao.executeQuery(lovDynamicQueryExecutor, lovSearchVO.getiDisplayStart(), lovSearchVO.getiDisplayLength() + 1);
  }
  
  /**
   * 
   * @param lovDynamicQueryExecutor
   * @param lovSearchVO
   */
  protected void prepareDynamicQueryExecutor(LOVDynamicQueryExecutor<? extends BaseEntity> lovDynamicQueryExecutor, Class<? extends BaseEntity> entityClass, LOVSearchVO lovSearchVO) {
    
    Iterator itrQueryMap = lovSearchVO.getSearchMap().entrySet().iterator();
    boolean applyDefaultApprovalStatusCheck = true;
    while (itrQueryMap.hasNext()) {
      Map.Entry entry = (Map.Entry) itrQueryMap.next();
      lovDynamicQueryExecutor.addOrClause((String) entry.getKey(), LOVDynamicQueryExecutor.LIKE_OPERATOR, entry.getValue());
    }
    
    if (hasElements(lovSearchVO.getFilterVoList())) {
      for (LOVFilterVO lovFilterVO : lovSearchVO.getFilterVoList()) {
        
        if (lovFilterVO.getQueryFilterClause() == LOVDynamicQueryExecutor.NOT_IN_OPERATOR) {
          lovDynamicQueryExecutor.addAndClause(lovFilterVO.getFilterColumnName(), LOVDynamicQueryExecutor.NOT_IN_OPERATOR, lovFilterVO.getFilterColumnValues());
        } else {
          lovDynamicQueryExecutor.addAndClause(lovFilterVO.getFilterColumnName(), LOVDynamicQueryExecutor.IN_OPERATOR, lovFilterVO.getFilterColumnValues());
        }
        
        if (lovFilterVO.getFilterColumnName() != null && "masterLifeCycleData.approvalStatus".equals(lovFilterVO.getFilterColumnName()) && applyDefaultApprovalStatusCheck) {
          applyDefaultApprovalStatusCheck = false;
        }
      }
    }
    
    addOrderByClause(lovSearchVO,lovDynamicQueryExecutor);
    
    
    applyCommonFilterClauses(entityClass, lovDynamicQueryExecutor, applyDefaultApprovalStatusCheck);
  }
  
  private void addOrderByClause(LOVSearchVO lovSearchVO, LOVDynamicQueryExecutor<? extends BaseEntity> lovDynamicQueryExecutor) {
    if (hasElements(lovSearchVO.getSortedElements())) {
      for (String element : lovSearchVO.getSortedElements()) {
        lovDynamicQueryExecutor.addOrderByClause(element);
      }
    }
    
  }

  /**
   * Applies common filter clauses for LOV on subclass of BaseEntity
   * 
   * @param entityClass
   * @param lovDynamicQueryExecutor
   */
  private void applyCommonFilterClauses(Class<? extends BaseEntity> entityClass, LOVDynamicQueryExecutor<? extends BaseEntity> lovDynamicQueryExecutor, boolean applyDefaultApprovalStatusCheck) {
    
    if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
      applyCommonFilterClausesForBaseMasterEntity(lovDynamicQueryExecutor, applyDefaultApprovalStatusCheck);
    }
    
    lovDynamicQueryExecutor
            .addAndOrClause("entityLifeCycleData.snapshotRecord",
                    LOVDynamicQueryExecutor.EQUALS_OPERATOR, null);
    
    lovDynamicQueryExecutor
             .addAndOrClause("entityLifeCycleData.snapshotRecord",
                    LOVDynamicQueryExecutor.EQUALS_OPERATOR, 
                      Boolean.valueOf(false));
    
    lovDynamicQueryExecutor
    .addQueryHint("org.hibernate.readOnly", 
                    Boolean.TRUE);
  }
  
  /**
   * Applies common filter clauses for LOV on subclass of BaseMasterEntity
   * 
   * @param entityClass
   * @param lovDynamicQueryExecutor
   */
  private void applyCommonFilterClausesForBaseMasterEntity(LOVDynamicQueryExecutor<? extends BaseEntity> lovDynamicQueryExecutor, boolean applyDefaultApprovalStatusCheck) {
    if (applyDefaultApprovalStatusCheck) {
      List<Integer> statusList = getApprovalStatusList();
      lovDynamicQueryExecutor
        .addAndClause("masterLifeCycleData.approvalStatus", 
             LOVDynamicQueryExecutor.IN_OPERATOR, statusList);
    }
  }
  
}