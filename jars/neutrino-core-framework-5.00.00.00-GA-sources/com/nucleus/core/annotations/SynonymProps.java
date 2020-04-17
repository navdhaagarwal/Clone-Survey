/**
 * 
 */
package com.nucleus.core.annotations;

/**
 * To collect the properties specified in synonym annotation
 * and to use this bean in scripting business logic  
 * @author harikant.verma
 *
 */
public class SynonymProps {

	public SynonymProps(String grant, String remoteTableName, String originSchema) {
		this.grant = grant;
		this.remoteTableName = remoteTableName;
		this.originSchema = originSchema;
	}
	
	//this property will not be changed
	private final String grant;
	
	//this property will not be changed
	private final String originSchema;

	//this property will not be changed
	private final String remoteTableName;

	public String getGrant() {
		return grant;
	}

	public String getRemoteTableName() {
		return remoteTableName;
	}

	public String getOriginSchema() {
		return originSchema;
	}
}
