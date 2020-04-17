package com.nucleus.finnone.pro.communicationgenerator.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.general.util.documentgenerator.TableDataVO;


public class CommunicationGroupCriteriaVO {

	private String communicationCode;
	private Map<String,Object> dataMap = new HashMap<String,Object>();
	private Long communicationTemplateId;
	private List<TableDataVO> tableDataVOList = new ArrayList<TableDataVO>();
	private List<String> variableList = new ArrayList<String>();
	private Map<CommunicationRequestDetail,GeneratedContentVO> requestDtlAndContentMap;
	private Boolean generateContentOnly;
	private Map<String,Object> processedDataMap = new HashMap<String,Object>();
	private CommunicationTemplate communicationTemplate;
	
	private CommunicationName communicationName;
	
    private String schedularInstanceId;
    
    private String barcodeReferenceNumber;
	
	public CommunicationTemplate getCommunicationTemplate() {
		return communicationTemplate;
	}
	public void setCommunicationTemplate(CommunicationTemplate communicationTemplate) {
		this.communicationTemplate = communicationTemplate;
	}
	public CommunicationName getCommunicationName() {
		return communicationName;
	}
	public void setCommunicationName(CommunicationName communicationName) {
		this.communicationName = communicationName;
	}
	public Map<String, Object> getProcessedDataMap() {
		return processedDataMap;
	}
	public void setProcessedDataMap(Map<String, Object> processedDataMap) {
		this.processedDataMap = processedDataMap;
	}
	public Boolean getGenerateContentOnly() {
		if(generateContentOnly==null)
			return false;
		return generateContentOnly;
	}
	public void setGenerateContentOnly(Boolean generateContentOnly) {
		this.generateContentOnly = generateContentOnly;
	}
	public Map<CommunicationRequestDetail, GeneratedContentVO> getRequestDtlAndContentMap() {
		return requestDtlAndContentMap;
	}
	public void setRequestDtlAndContentMap(
			Map<CommunicationRequestDetail, GeneratedContentVO> requestDtlAndContentMap) {
		this.requestDtlAndContentMap = requestDtlAndContentMap;
	}
	public List<TableDataVO> getTableDataVOList() {
		return tableDataVOList;
	}
	public void setTableDataVOList(List<TableDataVO> tableDataVOList) {
		this.tableDataVOList = tableDataVOList;
	}
	public List<String> getVariableList() {
		return variableList;
	}
	public void setVariableList(List<String> variableList) {
		this.variableList = variableList;
	}	
	public Long getCommunicationTemplateId() {
		return communicationTemplateId;
	}
	public void setCommunicationTemplateId(Long communicationTemplateId) {
		this.communicationTemplateId = communicationTemplateId;
	}	
	public Map<String, Object> getDataMap() {
		return dataMap;
	}
	public void setDataMap(Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}
	public String getCommunicationCode() {
		return communicationCode;
	}
	public void setCommunicationCode(String communicationCode) {
		this.communicationCode = communicationCode;
	}
	public String getSchedularInstanceId() {
		return schedularInstanceId;
	}
	public void setSchedularInstanceId(String schedularInstanceId) {
		this.schedularInstanceId = schedularInstanceId;
	}
	public String getBarcodeReferenceNumber() {
		return barcodeReferenceNumber;
	}
	public void setBarcodeReferenceNumber(String barcodeReferenceNumber) {
		this.barcodeReferenceNumber = barcodeReferenceNumber;
	}
	
	
}
