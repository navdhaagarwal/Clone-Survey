package com.nucleus.core.database.initializer;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.filter.ITableFilterSimple;

import com.nucleus.logging.BaseLoggers;

/**
 * 
 * @author gajendra.jatav
 * 
 * TableFilterSimple can be used for filtering table names by patterns which should be ignored while DB seeding
 */


public class TableFilterSimple implements ITableFilterSimple{

	
	private String defaultPattern;
	
	/**
	 * Additional patterns can be added in this property
	 * configure database.seed.ignoretable.pattern 
	 */
	private String additionalPatterns;
	
	private Pattern defaultRegexPattern;
	
	private Pattern additionalRegexPattern;
	
	private static String additionalRegexPatternPropertyKey="database.seed.ignoretable.pattern"; 
		
	public String getDefaultPattern() {
		return defaultPattern;
	}

	public void setDefaultPattern(String defaultPattern) {
		if(StringUtils.isNoneEmpty(defaultPattern))
		{
			try{
				this.defaultRegexPattern=Pattern.compile(defaultPattern,Pattern.CASE_INSENSITIVE);
				BaseLoggers.flowLogger.debug("Configured "+defaultPattern+" for excluding table from seeding operation");
			}catch (Exception e) {
				BaseLoggers.flowLogger.error("Not able to compile defaultPattern regex "+defaultPattern,e);
				throw e;
			}
		}
		this.defaultPattern = defaultPattern;
	}



	public String getAdditionalPatterns() {
		return additionalPatterns;
	}


	public void setAdditionalPatterns(String additionalPatterns) {
		if(StringUtils.isNoneEmpty(additionalPatterns))
		{
			if(additionalPatterns.contains((additionalRegexPatternPropertyKey)))
			{
						additionalPatterns=null;
						BaseLoggers.flowLogger.info("database.seed.ignoretable.pattern not found");
						return;
			}
			try{
					this.additionalRegexPattern=Pattern.compile(additionalPatterns,Pattern.CASE_INSENSITIVE);
					BaseLoggers.flowLogger.debug("Configured "+defaultPattern+" for excluding table from seeding operation");
			}catch (Exception e) {
				BaseLoggers.flowLogger.error("Not able to compile additionalPatterns regex "+additionalPatterns,e);
				throw e;
			}
		}
		this.additionalPatterns = additionalPatterns;

	}



	@Override
	public boolean accept(String tableName) throws DataSetException {
		
		if(defaultRegexPattern==null)
		{
			BaseLoggers.flowLogger.error("defaultRegexPattern not defined");
			
		}
		if(defaultRegexPattern!=null && defaultRegexPattern.matcher(tableName).find())
		{
			BaseLoggers.flowLogger.error("Excluding table "+tableName+" from seeding operation as it matches with defaultRegexPattern"+defaultPattern);
			return false;
		}
		else if(additionalRegexPattern!=null && additionalRegexPattern.matcher(tableName).find())
		{
			BaseLoggers.flowLogger.error("Excluding table "+tableName+" from seeding operation as it matches with additionalRegexPattern"+additionalPatterns);
			return false;
		}
		else
		{
			return true;
		}
	}

}
