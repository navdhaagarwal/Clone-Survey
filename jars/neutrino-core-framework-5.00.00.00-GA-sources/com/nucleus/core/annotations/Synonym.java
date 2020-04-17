package com.nucleus.core.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies that the class is a synonym. 
 * Possible values of grant attribute are ALL, SELECT.
 * schemaOrigin is logical name of remote schema, default is MASTERS.
 * remote table name is optional and if entity name is different than remote table name then need to provide this field. 
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Synonym {
	
	/** Possible values are ALL, SELECT */
	String grant();
	
	
	/** Default value is Master */
	String originSchema() default "MASTERS";
	
	
	/** Table Name in remote database */
	String remoteTableName() default "";
}
