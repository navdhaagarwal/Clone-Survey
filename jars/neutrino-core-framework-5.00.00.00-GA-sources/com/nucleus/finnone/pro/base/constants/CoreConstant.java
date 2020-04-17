/**
 * 
 */
package com.nucleus.finnone.pro.base.constants;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author Vivekanand.Jha 
 *
 */ 
public interface CoreConstant {  
	  
	public static final String DATE_FORMAT="dd/MM/yyyy";
	
	// For Days Difference Calculation :
	public static final int DAYS_COMPUTATION_METHOD_ACTUAL = 1;
	public static final int DAYS_COMPUTATION_METHOD_US360 = 2;
	public static final int DAYS_COMPUTATION_METHOD_EU360 = 3;
	public static final int DAYS_COMPUTATION_METHOD_365 = 4;
	public static final int DAYS_COMPUTATION_METHOD_360 = 5;
	 
	// Total Days in Year for interest calculation : Begin
	public static final int DAYS_IN_YEAR_COMPUTATION_METHOD_ACTUAL = 1;
	public static final int DAYS_IN_YEAR_COMPUTATION_METHOD_360 = 2;
	public static final int DAYS_IN_YEAR_COMPUTATION_METHOD_365 = 3;
	 
	public static final int GREGORIAN_CALENDAR = 1; 
	
	public static final int DIFF_OF_DAYS_METHOD_1 = 1;
	Integer RANDOM_INCREMENTOR = 10000;
	Integer SEVERITY_LOW= Integer.valueOf(0);
	Integer SEVERITY_MEDIUM= Integer.valueOf(5);
	Integer SEVERITY_HIGH= Integer.valueOf(10);
	Integer SEVERITY_NO_ERROR=Integer.valueOf(-1);
	
	/* for temporary exception codes*/
	String DATASYSEXC_CONSTANT_CODE="DAO001";
	String	CONSTRAINTVIOLATIONSYSEXC__CONSTANT_CODE="DAO002";
	String	JDBCSYSEXC__CONSTANT_CODE="DAO003";
	String	SYSEXC__CONSTANT_CODE="DAO004";
	String  ORACLE_ERROR_TAG = "ORA-";
	String  CAUSED_BY_TAG = "Caused by:";
	Character CARRIAGE_RETURN_CHAR = '\n';
	Character COMMA_CHAR = ',';
	Character SPACE_CHAR = ' ';
	public static final String BY_DAYS_CAL ="D";
	public static final String BY_RATE_CAL ="R";
	
	public static final String GRANT_ALL = "ALL";
	public static final String GRANT_SELECT = "SELECT";
	String GRANT_SELECT_REFERENCES = "SELECT,REFERENCES";
	public static final String ORACLE = "oracle";
	public static final String POSTGRES = "postgres";
	public static final String GRANT = "GRANT";
	public static final String SYNONYM = "SYNONYM";
	public static final String MASTERS = "MASTERS";
	public static final String SOURCE = "SOURCE";
	
	/* properties suffix name for master*/
	public static final String SUFFIX_MASTER_DATABASE_USERNAME = ".database.username";
	public static final String SUFFIX_MASTER_DATABASE_PASSWORD = ".database.password";
	public static final String SUFFIX_MASTER_DATABASE_URL = ".database.connection.url";
	public static final String SUFFIX_MASTER_DATABASE_DBLINK = ".db.link.name";
	public static final String SUFFIX_MASTER_DATABASE_SCHEMA_NAME = ".schemaname";
	public static final String SUFFIX_MASTER_DATABASE_JNDI_NAME = ".jndiname";
	public static final String SUFFIX_MASTER_DATABASE_DISABLE_ORIGIN = ".disable.origin";

	public static final String DEFAULT_MASTER_DATABASE_DBLINK = "db.link.name";
	public static final String DEFAULT_MASTER_DATABASE_SCHEMA_NAME = "database.master.schemaname";
	
	public static final String MASTER_DATABASE_USERNAME_KEY = "MASTER_DATABASE_USERNAME_KEY";
	public static final String MASTER_DATABASE_PASSWORD_KEY = "MASTER_DATABASE_PASSWORD_KEY";
	public static final String MASTER_DATABASE_URL_KEY = "MASTER_DATABASE_URL_KEY";
	public static final String MASTER_DATABASE_DBLINK_KEY = "MASTER_DATABASE_DBLINK_KEY";
	public static final String MASTER_DATABASE_SCHEMA_NAME_KEY = "MASTER_DATABASE_SCHEMA_NAME_KEY";
	public static final String MASTER_DATABASE_JNDI_NAME_KEY = "MASTER_DATABASE_JNDI_NAME_KEY";
	public static final String MASTER_DATABASE_DISABLE_ORIGIN_KEY = "MASTER_DATABASE_DISABLE_ORIGIN_KEY";
	public static final String LOCKED_REASON_DESCRIPTION="This account has been locked with Reason : ";
}
