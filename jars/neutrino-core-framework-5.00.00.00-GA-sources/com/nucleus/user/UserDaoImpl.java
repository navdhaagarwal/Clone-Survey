package com.nucleus.user;

import javax.inject.Named;

import com.nucleus.persistence.BaseDaoImpl;

@Named("userDao")
public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {

}