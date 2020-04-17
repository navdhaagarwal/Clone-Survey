package com.nucleus.persistence;

import org.springframework.stereotype.Repository;

import com.nucleus.entity.BaseEntity;

@Repository
public abstract class BaseDaoImpl<T extends BaseEntity> extends EntityDaoImpl implements BaseDao<T>{

}