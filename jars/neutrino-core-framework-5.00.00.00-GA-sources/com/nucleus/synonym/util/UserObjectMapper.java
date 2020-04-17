package com.nucleus.synonym.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.nucleus.synonym.metadata.pojo.UserObject;

public class UserObjectMapper implements RowMapper<UserObject> {

	@Override
	public UserObject mapRow(ResultSet rs, int rowNum) throws SQLException {
		UserObjectResultSetExtractor extractor = new UserObjectResultSetExtractor();
		return extractor.extractData(rs);
	}

}
