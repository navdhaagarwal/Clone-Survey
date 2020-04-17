package com.nucleus.web.formDefinition;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.dynamicform.service.FormConfigurationConstant;
import com.nucleus.core.dynamicform.service.FormDefinitionService;
import com.nucleus.core.formsConfiguration.FieldCustomOptions;
import com.nucleus.core.formsConfiguration.FieldDataType;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.core.formsConfiguration.FormComponentType;
import com.nucleus.core.formsConfiguration.FormConfigEntityData;
import com.nucleus.core.formsConfiguration.FormContainerType;
import com.nucleus.core.formsConfiguration.PanelDefinition;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.entity.BaseEntity;
import com.nucleus.web.WebDataBinderElClass;

public class FormConfigurationExcelBuilder extends AbstractXlsView {

    private static final String DEFAULT_DATE_FORMAT = "MM/dd/yyyy";
    private static final int VALIDATION_RANGE = 5000;

    

    @Override
    protected void buildExcelDocument(			Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
    	Map<String,Object> dataMap = (Map<String,Object>) model.get("dataMap");
        UIMetaData uiMetaData = (UIMetaData) dataMap.get("uiMetaData");
        String formName = (String) dataMap.get("formName");
        // create a word-sheet
        Sheet sheet = (workbook).createSheet(formName);
        if (uiMetaData == null) {
            return;
        }
        // title row
        Row header = sheet.createRow(0);
        short columnCount = 0;
        if (uiMetaData.getPanelDefinitionList() != null) {
            for (PanelDefinition panelDefinition : uiMetaData.getPanelDefinitionList()) {
                if (panelDefinition.getPanelType() != FormContainerType.FIELD_TYPE_TABLE
                        && panelDefinition.getFieldDefinitionList() != null) {
                    for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {
                        Cell cell = header.createCell(columnCount);
                        cell.setCellValue(fieldDefinition.getFieldKey());
                        int dataType = fieldDefinition.getFieldDataType();
                        String fieldType = fieldDefinition.getFieldType();
                        CellStyle style = workbook.createCellStyle();
                        style.setLocked(false);
                        CellStyle headerCellstyle = workbook.createCellStyle();
                        Font headerFont = workbook.createFont();
                        headerFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                        headerCellstyle.setFillBackgroundColor(new HSSFColor.GREY_40_PERCENT().getIndex());
                        headerCellstyle.setFillPattern(HSSFCellStyle.BIG_SPOTS);
                        headerCellstyle.setLocked(true);
                        headerCellstyle.setFont(headerFont);
                        cell.setCellStyle(headerCellstyle);
                        // adding field level validator
                        CellRangeAddressList addressList = new CellRangeAddressList(1, VALIDATION_RANGE, columnCount, columnCount);
                        // List type of Fields -- supported datatype are Integer/Number/reference/Text
                        if (fieldType.equals(FormComponentType.DROP_DOWN) || fieldType.equals(FormComponentType.RADIO)
                                || fieldType.equals(FormComponentType.MULTISELECTBOX)){
                            bindListValidatorToSheet(sheet, fieldDefinition, addressList);
                        }// Check box - only boolean type supported
                        else if(fieldType.equals(FormComponentType.CHECKBOX) && dataType == FieldDataType.DATA_TYPE_TEXT_BOOLEAN){
                            bindBooleanValidatorToSheet(sheet, fieldDefinition, addressList);
                        }//Date - only Date Type is supported
                        else if(fieldType.equals(FormComponentType.DATE) && dataType == FieldDataType.DATA_TYPE_DATE){
                            style.setDataFormat(workbook.createDataFormat().getFormat(DEFAULT_DATE_FORMAT));
                            bindDateValidatorToSheet(workbook, sheet, fieldDefinition, addressList);
                        }//Money - only Number type is supported
                        else if(fieldType.equals(FormComponentType.MONEY) && dataType == FieldDataType.DATA_TYPE_MONEY){
                            bindNumberValidatorToSheet(sheet, fieldDefinition, addressList);
                        }// textBox - Integer/Number/Text Type Supported
                        else if(fieldType.equals(FormComponentType.TEXT_BOX)){
                            if(dataType == FieldDataType.DATA_TYPE_INTEGER){
                                bindIntegerValidatorToSheet(sheet, fieldDefinition, addressList);
                            }else if(dataType == FieldDataType.DATA_TYPE_NUMBER){
                                bindNumberValidatorToSheet(sheet, fieldDefinition, addressList);
                            }else if(dataType == FieldDataType.DATA_TYPE_TEXT){
                                bindStringValidatorToSheet(sheet, fieldDefinition, addressList);
                            }
                        }// TextArea- only Sting type is supported
                        else if(fieldType.equals(FormComponentType.TEXT_AREA) && dataType == FieldDataType.DATA_TYPE_TEXT){
                            bindStringValidatorToSheet(sheet, fieldDefinition, addressList);
                        }
                        sheet.setDefaultColumnStyle(columnCount, style);
                        //Below line was commented as the result of version upgrade setProtect method no more exist as it was previously depricated.
                        //sheet.setProtect(true);
                        columnCount++;
                    }
                }
            }
            // setting file extension
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + formName + "-template.xls");
            response.setContentType("application/xls");

        }

    }


    private void bindStringValidatorToSheet(Sheet sheet, FieldDefinition fieldDefinition,
            CellRangeAddressList addressList) {
        DVConstraint dvConstraint = DVConstraint.createNumericConstraint(
                DVConstraint.ValidationType.ANY, DVConstraint.OperatorType.IGNORED, null, null);
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        dataValidation.createErrorBox("Warning", "Value can String Only");
        dataValidation.setShowErrorBox(true);
        dataValidation.setEmptyCellAllowed(fieldDefinition.isMandatoryField());
        sheet.addValidationData(dataValidation);
    }


    private void bindListValidatorToSheet(Sheet sheet, FieldDefinition fieldDefinition, CellRangeAddressList addressList) {
        List<Object> valueList = new ArrayList<Object>();
        if (fieldDefinition.getBinderName().equals(FormConfigurationConstant.CUSTOM_BINDER)
                && fieldDefinition.getFieldCustomOptionsList() != null) {
            for (FieldCustomOptions fieldCustomOptions : fieldDefinition.getFieldCustomOptionsList()) {
                valueList.add( fieldCustomOptions.getCustomeItemValue());
            }
        }else {
            @SuppressWarnings("unchecked")
            
            FormConfigEntityData formConfigEntityDataObj=getFormConfigEntityDataBasedOnBinderName(fieldDefinition.getBinderName());
            List<Map<String,Object>> ObjectList =  (List<Map<String,Object>>) WebDataBinderElClass.getWebDataBinderData(fieldDefinition.getBinderName());
            if(!ObjectList.isEmpty()){
            	for(Map<String,Object> myMap:ObjectList){
            		if(myMap.get(formConfigEntityDataObj.getItemLabel())!=null){
            			valueList.add(myMap.get(formConfigEntityDataObj.getItemLabel()));
            		}
            	}
            }
        }
        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(valueList.toArray(new String[valueList.size()]));
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        dataValidation.createErrorBox("Warning", "Value can be from"+valueList.toString()+" Only");
        dataValidation.setShowErrorBox(true);
        dataValidation.setEmptyCellAllowed(fieldDefinition.isMandatoryField());
        sheet.addValidationData(dataValidation);
    }


    private FormConfigEntityData getFormConfigEntityDataBasedOnBinderName(String binderName) {
    	FormDefinitionService formDefinitionService=NeutrinoSpringAppContextUtil.getBeanByName("formDefinitionService", FormDefinitionService.class);
		return formDefinitionService.getFormConfigEntityDataBasedOnBinderName(binderName);
	}


	private void bindIntegerValidatorToSheet(Sheet sheet, FieldDefinition fieldDefinition,
            CellRangeAddressList addressList) {
        Integer minValue = Integer.MIN_VALUE;
        Integer maxValue = Integer.MAX_VALUE;
        DVConstraint dvConstraint = DVConstraint.createNumericConstraint(
                DVConstraint.ValidationType.INTEGER, DVConstraint.OperatorType.IGNORED, minValue.toString(), maxValue.toString());
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        dataValidation.createErrorBox("Warning", "Value can Integer Only");
        dataValidation.setShowErrorBox(true);
        dataValidation.setEmptyCellAllowed(fieldDefinition.isMandatoryField());
        sheet.addValidationData(dataValidation);
    }


    private void bindNumberValidatorToSheet(Sheet sheet, FieldDefinition fieldDefinition,
            CellRangeAddressList addressList) {
        Float maxValue= Float.MAX_VALUE;
        Float minValue= Float.MIN_VALUE;
        DVConstraint dvConstraint = DVConstraint.createNumericConstraint(
                DVConstraint.ValidationType.DECIMAL, DVConstraint.OperatorType.IGNORED, minValue.toString(), maxValue.toString());
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        dataValidation.createErrorBox("Warning", "Value can Number Only");
        dataValidation.setShowErrorBox(true);
        dataValidation.setEmptyCellAllowed(fieldDefinition.isMandatoryField());
        sheet.addValidationData(dataValidation);
    }


    private void bindBooleanValidatorToSheet(Sheet sheet, FieldDefinition fieldDefinition,
            CellRangeAddressList addressList) {
        DVConstraint dvConstraint = DVConstraint.createExplicitListConstraint(new String[] { "true",
                "false" });
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(false);
        dataValidation.createErrorBox("Warning", "Value can be true of false Only");
        dataValidation.setShowErrorBox(true);
        dataValidation.setEmptyCellAllowed(fieldDefinition.isMandatoryField());
        sheet.addValidationData(dataValidation);
    }


    private void bindDateValidatorToSheet(Workbook workbook, Sheet sheet, FieldDefinition fieldDefinition,
            CellRangeAddressList addressList) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
     /*   Date minDate = new Date(Long.MIN_VALUE);
        Date maxDate = new Date(Long.MAX_VALUE);*/

        DVConstraint dvConstraint = DVConstraint.createDateConstraint(DVConstraint.OperatorType.BETWEEN,
                "01/01/0001", "31/12/9999", DEFAULT_DATE_FORMAT);
        HSSFDataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint);
        dataValidation.setSuppressDropDownArrow(true);
        dataValidation.createErrorBox("Warning", "Value can be Date Format(MM/DD/YYYY)");
        dataValidation.setShowErrorBox(true);
        dataValidation.setEmptyCellAllowed(fieldDefinition.isMandatoryField());
        sheet.addValidationData(dataValidation);
    }

    private String getValueForField(BaseEntity baseEntity,Field[] allfields, String fieldName){
        if(allfields.length > 0){
            for (Field field : allfields) {
                field.setAccessible(true);
                if(field.getName().equals(fieldName)){
                    try {
                        return field.get(baseEntity).toString();
                    } catch (IllegalArgumentException e) {

                    } catch (IllegalAccessException e) {

                    }
                }
            }
        }
        Class parent = baseEntity.getClass().getSuperclass();
        if(parent != null){
            return getValueForField(baseEntity, parent.getDeclaredFields(), fieldName);
        }
        return null;
    }
}
