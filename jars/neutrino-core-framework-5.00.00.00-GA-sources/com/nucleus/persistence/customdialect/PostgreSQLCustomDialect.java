package com.nucleus.persistence.customdialect;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class PostgreSQLCustomDialect extends PostgreSQL95Dialect{

	public PostgreSQLCustomDialect()
	{
		super();
		registerColumnType(Types.BLOB, "bytea");
		registerColumnType( Types.BIGINT, "numeric(19, 0)" );
	}

	 @Override
	 public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
	    if (Types.CLOB == sqlTypeDescriptor.getSqlType()) {
	      return LongVarcharTypeDescriptor.INSTANCE;
	    }
	    
	    if (sqlTypeDescriptor.getSqlType() == java.sql.Types.BLOB) {
	        return BinaryTypeDescriptor.INSTANCE;
	    }

	    return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
	  }


	/**
	 * Overridden this method since getQueryString in PostgreSQLDialect (extends PostgreSQL81Dialect)
	 * returns "select relname from pg_class where relkind='S'"
	 * The above query returns all the sequences from database, not schema
	 * And so, sequences do not get created if any of the schema in the database has sequences
	 * and ddl-auto property is set to update
	 * The below query returns sequence names if schema contains the sequences.
	 * @return
	 */
	@Override
	public String getQuerySequencesString() {
		String schemaName = Environment.getProperties().getProperty("application.schema.name");
		if( StringUtils.isBlank(schemaName)){
			return super.getQuerySequencesString();
		}
		return "select sequencename from pg_catalog.pg_sequences where schemaname='"+schemaName+"'";
	}
}
