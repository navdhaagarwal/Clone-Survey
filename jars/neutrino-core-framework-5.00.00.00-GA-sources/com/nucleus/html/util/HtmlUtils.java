package com.nucleus.html.util;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

/**
 * 
 * 
 * Creating wrapper around org.springframework.web.util.HtmlUtils avoid impact
 * of change done in SPR-15540 as earlier HtmlUtils accepted null as input but
 * now it is throwing exception.
 * 
 *
 * The Class HtmlUtils.
 */
public class HtmlUtils {
	
		/**
		 * Instantiates a new html utils.
		 */
		private HtmlUtils(){}
		
		/**
		 * Gets the normal text from html text.
		 *
		 * @param htmlText the html text
		 * @return the normal text from html text
		 */
		public static String getNormalTextFromHtmlText(String htmlText){
			String normalText = "";
			
			if(! StringUtils.isEmpty(htmlText)){
				normalText = Jsoup.parse(htmlText).text();				
			}
			
			return normalText;
		}

		public static String htmlEscape(String input) {
			if (input == null) {
				return null;
			}
			return org.springframework.web.util.HtmlUtils.htmlEscape(input);
		}

		public static String htmlEscape(String input, String encoding) {
			if (input == null) {
				return null;
			}
			return org.springframework.web.util.HtmlUtils.htmlEscape(input, encoding);
		}

		public static String htmlEscapeDecimal(String input) {
			if (input == null) {
				return null;
			}
			return org.springframework.web.util.HtmlUtils.htmlEscapeDecimal(input);
		}

		public static String htmlEscapeDecimal(String input, String encoding) {
			if (input == null) {
				return null;
			}
			return org.springframework.web.util.HtmlUtils.htmlEscapeDecimal(input, encoding);
		}

		public static String htmlEscapeHex(String input) {
			if (input == null) {
				return null;
			}
			return org.springframework.web.util.HtmlUtils.htmlEscapeHex(input);
		}

		public static String htmlEscapeHex(String input, String encoding) {
			if (input == null) {
				return null;
			}
			return org.springframework.web.util.HtmlUtils.htmlEscapeHex(input, encoding);
		}

		public static String htmlUnescape(String input) {
			if (input == null) {
				return null;
			}
			return org.springframework.web.util.HtmlUtils.htmlUnescape(input);
		}
}
