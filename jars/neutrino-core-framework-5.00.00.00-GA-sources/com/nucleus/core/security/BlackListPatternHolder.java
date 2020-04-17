package com.nucleus.core.security;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasAnyEntry;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.hasNoEntry;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class BlackListPatternHolder {
	public static final String CSV_FORMULA_PATTERN_WITH_COMMA_CODE="CSV_FORMULA_PATTERN_WITH_COMMA";
	public static final String CSV_FORMULA_PATTERN_WITHOUT_COMMA_CODE="CSV_FORMULA_PATTERN_WITHOUT_COMMA";
	private static Map<String,Pattern> csvBlackListPatterns = new  HashMap<>();
		
	public static final List<PatternConfig> paramPatterns = new ArrayList<>(
			Arrays.asList(new PatternConfig((input -> input.indexOf("iframe") != -1)),
					new PatternConfig((input -> input.indexOf('=') != -1),
							Pattern.compile("(.*)=[\\s]*[\"\']?[a-zA-Z_$0-9]*[\\(](.*)[\\)]",
									Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL))));

	public static final List<PatternConfig> headerAndParamPatterns = new ArrayList<>(Arrays.asList(
			
			new PatternConfig((input -> input.indexOf('<') != -1), Pattern.compile("(.*)[<][^0-9\\s]+(.*)[>]",
					Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)),

			// src='...'
			new PatternConfig((input -> input.indexOf("src") != -1),
					Pattern.compile("(.*)src[\\s]*=[\\s]*['\"](.*?)['\"]",
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)),

			// eval(...)
			new PatternConfig((input -> input.indexOf("eval") != -1),
					//added exclusion for strings like keval containing eval
					Pattern.compile("^eval[\\s]*\\((.*?)\\)|[^\\w]eval[\\s]*\\((.*?)\\)",
					//Pattern.compile("(.*)eval[\\s]*\\((.*?)\\)",
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)),
			// expression(...)
			new PatternConfig((input -> input.indexOf("expression") != -1),
					Pattern.compile("(.*)expression[\\s]*\\((.*?)\\)",
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)),
			// javascript:...
			new PatternConfig((input -> input.indexOf("javascript") != -1),
					Pattern.compile("(.*)javascript[\\s]*:", Pattern.CASE_INSENSITIVE)),
			// vbscript:...
			new PatternConfig((input -> input.indexOf("vbscript") != -1)),

			// onload(...)=...  
			new PatternConfig((input -> input.indexOf("onload") != -1),
					Pattern.compile("(.*)onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)),

			new PatternConfig((input -> input.indexOf("/>") != -1),
					Pattern.compile("(.*)/>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL))));

		private static  Map<String,Pattern> codeWiseBlackListPatternMap=null;
		
		private BlackListPatternHolder()
		{
			
		}
		public static Map<String, Pattern> getBlackListPatternMap() {
			return codeWiseBlackListPatternMap;
		}

		public static void setBlackListPatternMap(
				Map<String, Pattern> blackListPatternMap) {
			if(hasNoEntry(blackListPatternMap)||hasAnyEntry(BlackListPatternHolder.codeWiseBlackListPatternMap)){
				return;
			}
			BlackListPatternHolder.codeWiseBlackListPatternMap = java.util.Collections.unmodifiableMap(blackListPatternMap) ;
		}


		public static Map<String,Pattern> getCsvBlackListPattern()
		{
			 
			if(csvBlackListPatterns.isEmpty())
			{
			    Pattern blackListPatternWithoutComma= codeWiseBlackListPatternMap.get(CSV_FORMULA_PATTERN_WITHOUT_COMMA_CODE);
			    if(notNull(blackListPatternWithoutComma))
			    {
			    
			    
			    csvBlackListPatterns.put(CSV_FORMULA_PATTERN_WITHOUT_COMMA_CODE,blackListPatternWithoutComma);
			    }
			    Pattern blackListPatternWithComma= codeWiseBlackListPatternMap.get(CSV_FORMULA_PATTERN_WITH_COMMA_CODE);
			    if(notNull(blackListPatternWithComma))
			    {
			    
	 
			    csvBlackListPatterns.put(CSV_FORMULA_PATTERN_WITH_COMMA_CODE,blackListPatternWithComma);
			    } 
			    
			    
			}
			return csvBlackListPatterns;
			
		}
		
	
}
