package com.nucleus.synonym.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.nucleus.synonym.metadata.pojo.UserObject;

public class UserObjectResultSetExtractor implements ResultSetExtractor<UserObject> {

	@Override
	public UserObject extractData(ResultSet rs) throws SQLException, DataAccessException {
		UserObject userObject = new UserObject();
		userObject.setDbObjectName(rs.getString(1));
		userObject.setDbObjectType(rs.getString(2));
		return userObject;
	}

}
