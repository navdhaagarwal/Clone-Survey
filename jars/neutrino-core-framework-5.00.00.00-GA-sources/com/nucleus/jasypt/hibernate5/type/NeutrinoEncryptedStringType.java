package com.nucleus.jasypt.hibernate5.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.jasypt.hibernate4.type.AbstractEncryptedAsStringType;

public class NeutrinoEncryptedStringType extends AbstractEncryptedAsStringType {

	static final int sqlType = Types.VARCHAR;

	
	public Object nullSafeGet(ResultSet rs, String[] names, SharedSessionContractImplementor session, Object owner)
			throws HibernateException, SQLException {

		checkInitialization();
		final String message = rs.getString(names[0]);
		return rs.wasNull() ? null : convertToObject(this.encryptor.decrypt(message));

	}

	public void nullSafeSet(PreparedStatement st, Object value, int index, SharedSessionContractImplementor session)
			throws HibernateException, SQLException {

		checkInitialization();
		if (value == null) {
			st.setNull(index, sqlType);
		} else {
			st.setString(index, this.encryptor.encrypt(convertToString(value)));
		}

	}

	@Override
	public Object nullSafeGet(final ResultSet rs, final String[] names, final SessionImplementor session,
			final Object owner) throws HibernateException, SQLException {

		checkInitialization();
		final String message = rs.getString(names[0]);
		return rs.wasNull() ? null : convertToObject(this.encryptor.decrypt(message));

	}

	@Override
	public void nullSafeSet(final PreparedStatement st, final Object value, final int index,
			final SessionImplementor session) throws HibernateException, SQLException {

		checkInitialization();
		if (value == null) {
			st.setNull(index, sqlType);
		} else {
			st.setString(index, this.encryptor.encrypt(convertToString(value)));
		}

	}

	@Override
	protected Object convertToObject(String string) {
		return string;
	}

	@Override
	public Class returnedClass() {
		return String.class;
	}

}
