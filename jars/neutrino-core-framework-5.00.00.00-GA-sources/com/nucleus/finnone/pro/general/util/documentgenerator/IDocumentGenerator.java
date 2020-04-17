/**
 * 
 */
package com.nucleus.finnone.pro.general.util.documentgenerator;

import java.util.List;
import java.util.Map;

/**
 * @author shivani.aggarwal
 *
 */
public interface IDocumentGenerator {
	
	String getTemplatePath();
	void setTemplatePath(String templatePath);
	Map<String,String> getImageVariablesWithPathMap();
	
	byte[] getPDFOutput(Map<String,Object> data);
	byte[] getPDFOutput(Map<String,Object> data,Map<String,String> imageVariablesWithPath);
	byte[] getPDFOutput(Map<String,Object> data,Map<String,String> imageVariablesWithPath,List<TableDataVO> tableDataVOList,List<String> variableList);
	
	byte[] getDocxOutput(Map<String,Object> data);
	byte[] getDocxOutput(Map<String,Object> data,Map<String,String> imageVariablesWithPath);
	byte[] getDocxOutput(Map<String,Object> data,Map<String,String> imageVariablesWithPath,List<TableDataVO> tableDataVOList,List<String> variableList);

}
