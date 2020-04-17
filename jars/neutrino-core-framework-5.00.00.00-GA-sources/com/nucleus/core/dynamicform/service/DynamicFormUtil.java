package com.nucleus.core.dynamicform.service;

import java.lang.reflect.Method;
import java.util.*;

import com.nucleus.core.formsConfiguration.*;
import com.nucleus.core.formsConfiguration.fieldcomponent.EmailInfoVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.LOVFieldVO;
import com.nucleus.core.formsConfiguration.fieldcomponent.PhoneNumberVO;
import com.nucleus.core.formsConfiguration.validationcomponent.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.ui.ModelMap;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.core.formsConfiguration.validationcomponent.FormValidationConstants.Operators;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.persistence.EntityDao;

public class DynamicFormUtil {

	public static final String PERSISTENT_FORM_DATA_GETTER_METHOD = "getPersistentFormData";
	public static final String PERSISTENT_FORM_DATA_SETTER_METHOD = "setPersistentFormData";

	public static String getFormNameWithoutSpace(String formName)
	{
		return formName.replaceAll(" ","_"); 
	}
	
	 public  static void getDataForEditViewMode(Long id, ModelMap map,boolean isFilter) {

		 	EntityDao entityDao=NeutrinoSpringAppContextUtil.getBeanByName("entityDao", EntityDao.class);
		 	FormDefinitionService formDefinitionService=NeutrinoSpringAppContextUtil.getBeanByName("formDefinitionService", FormDefinitionService.class);
		 	GenericParameterService genericParameterService=NeutrinoSpringAppContextUtil.getBeanByName("genericParameterService", GenericParameterService.class);
		 	UIMetaData uiMetaData=entityDao.find(UIMetaData.class, id);
		 	
	        Hibernate.initialize(uiMetaData.getPanelDefinitionList());
	        Map<String,FieldDefinition> ifComponentList = new HashMap();	
	        for (int i = 0 ; i < uiMetaData.getPanelDefinitionList().size() ; i++) {
	            Hibernate.initialize(uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList());
	            for (int j = 0 ; j < uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList().size() ; j++) {
	                Hibernate.initialize(uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList().get(j).getValue());
	                if(FormContainerType.FIELD_TYPE_TABLE != uiMetaData.getPanelDefinitionList().get(i).getPanelType() && FormContainerType.FIELD_TYPE_SPECIAL_TABLE != uiMetaData.getPanelDefinitionList().get(i).getPanelType()){
	                	 FieldDefinition fieldDefinition = uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList().get(j);
	 	                Set<Operators> operators = FormValidationDataTypeOperatorMap.getOperatorsByDataType(fieldDefinition.getFieldType());
	 	                if(operators == null){
	 	                	operators =FormValidationDataTypeOperatorMap.getOperatorsByComponenrTypeAndFieldType(fieldDefinition.getFieldType(),fieldDefinition.getFieldDataType());
	 	                }
	 	                if(operators != null){
	 	                	ifComponentList.put(fieldDefinition.getFieldKey(),fieldDefinition);
	 	                }
	                }
	            }
	            if(FormContainerType.FIELD_TYPE_PANEL == uiMetaData.getPanelDefinitionList().get(i).getPanelType()){
					if(CollectionUtils.isNotEmpty(uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList())){
						Optional<FieldDefinition> optionals = uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList().stream().filter(s->s.isMandatoryField()).findFirst();
						if(!optionals.isPresent()){
							FieldDefinition fieldDefinition = new FieldDefinition();
							fieldDefinition.setFieldKey(uiMetaData.getPanelDefinitionList().get(i).getPanelKey());
							fieldDefinition.setFieldType(FormComponentType.PANEL);
							ifComponentList.put(uiMetaData.getPanelDefinitionList().get(i).getPanelKey(),fieldDefinition);
						}
					}
				}
	        }
	        
	        FormVO formVO = formDefinitionService.createVOFromRealObject(uiMetaData);
	        List<String> panelKeyList = new ArrayList<>();
	        if(formVO!=null && CollectionUtils.isNotEmpty(formVO.getContainerVOList())){
	        	for(FormContainerVO formContainerVO : formVO.getContainerVOList()){
	        		if(formContainerVO.getType() == FormContainerType.FIELD_TYPE_PANEL && formContainerVO.getAllowPanelSave()!=null && formContainerVO.getAllowPanelSave().equals(Boolean.TRUE)){
						panelKeyList.add(formContainerVO.getFieldKey());
					}
				}
			}
			map.put("panelKeyList",panelKeyList);
	        map.put("formName", uiMetaData.getFormName());
	        map.put("formVO", formVO);
	        map.put("ifComponentList", ifComponentList);
	        map.put("componentList", genericParameterService.retrieveTypes(FormComponentType.class));
	        map.put("whenList", FormValidationConstants.WhenActionTypes.values());
	    }
	 
	 public static Set<String> updateDedupe(String pathField, Set<String> dedupeSet) {
	        if (StringUtils.isNotEmpty(pathField)) {
	            String[] field1PathArray = pathField.split("\\.");
	            for (String key : field1PathArray) {
	                dedupeSet.add(key);
	            }
	        }
	        return dedupeSet;
	    }

	 
	 public static void initializeUiMetaData(UIMetaData uiMetaData)
	 {
		 Hibernate.initialize(uiMetaData.getPanelDefinitionList());
         for (int i = 0 ; i < uiMetaData.getPanelDefinitionList().size() ; i++) {
              Hibernate.initialize(uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList());
              for (int j = 0 ; j < uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList().size() ; j++) {
                  Hibernate.initialize(uiMetaData.getPanelDefinitionList().get(i).getFieldDefinitionList().get(j).getValue());
              }
          }
	 }
	 
	 public static void populateValidationRelatedData(FormVO formVo, ModelMap map,
				Map<String, FieldDefinition> componentList) {
			if(formVo.getValidationsVO() !=null && !formVo.getValidationsVO().isEmpty()){
	    	// creating drop down list for opratores and right hand operators
		    	List<List<Map<String,String>>> ifsOperatorsList = new ArrayList<>();
		    	List<List<Map<String,String>>> ifsRightHandFieldList = new ArrayList<>();

		    	List<List<Map<String,String>>> thenActionsList = new ArrayList<>();
		    	List<List<Map<String,String>>> thenRightHandFieldsList = new ArrayList<>();
		    	List<List<FieldDefinition>> ifsFieldDefinitionList = new ArrayList<>();
		    	List<List<Object>> ifsRightSideValueList = new ArrayList<>();

		    	for (FormValidationMetadataVO validation : formVo.getValidationsVO()) {
					if(validation == null){
						continue;
					}
					List<Map<String,String>> singleThenActionsList = new ArrayList<>();
					List<Map<String,String>> singleThenRightHandFieldsList = new ArrayList<>();
	 
					List<Map<String,String>> singleifsOperatorsList = new ArrayList<>();
			    	List<Map<String,String>> singleifsRightHandFieldList = new ArrayList<>();

			    	List<FieldDefinition> singleIfFieldDefinitionList = new ArrayList<>();
			    	List<Object> singleIfRightSideValueList = new ArrayList<>();

					if(validation.getIfConditions()!=null && !validation.getIfConditions().isEmpty()){
						for (FormValidationRulesIFMetadataVO ifVo : validation.getIfConditions()) {
							if(ifVo == null || ifVo.getLeftOperandFieldKey()==null){
								continue;
							}
							if(ifVo.getLeftOperandFieldKey().getExpressionType().equals(FormValidationConstants.IfOperandExpressionType.SIMPLE_EXPRESSION.getCode())){
								String fieldId = ifVo.getLeftOperandFieldKey().getExpression();
								FieldDefinition fieldDefinition = componentList.get(fieldId);
								Map<String,String> operatorsString = new HashMap<>();
						    	Map<String,String> fieldsString = new HashMap<>();
						    	populateOperatorsAndFieldsByFieldType(componentList, fieldDefinition, operatorsString, fieldsString);
						    	singleifsOperatorsList.add(operatorsString);
						    	singleifsRightHandFieldList.add(fieldsString);
								singleIfFieldDefinitionList.add(fieldDefinition);
							}
						}
					}
					if(validation.getThenActions()!=null && !validation.getThenActions().isEmpty()){
						for (FormValidationRulesThenMetadataVO then : validation.getThenActions()) {
							if(then == null){
								continue;
							}
							FieldDefinition fieldDefinition = componentList.get(then.getTargetFieldKey());
							Map<String,String> operatorsString = new HashMap<>();
					    	Map<String,String> fieldsString = new HashMap<>();
					    	populateOperatorsAndFieldListForThen(componentList, fieldDefinition, operatorsString, fieldsString);
					    	operatorsString.put(FormValidationConstants.ThenActionTypes.SHOW_MESSAGE.getCode(), FormValidationConstants.ThenActionTypes.SHOW_MESSAGE.getCode());
					    	singleThenActionsList.add(operatorsString);
					    	singleThenRightHandFieldsList.add(fieldsString);
						}
					}
					ifsOperatorsList.add(singleifsOperatorsList);
					ifsRightHandFieldList.add(singleifsRightHandFieldList);
					ifsFieldDefinitionList.add(singleIfFieldDefinitionList);
					
					thenActionsList.add(singleThenActionsList);
					thenRightHandFieldsList.add(singleThenRightHandFieldsList);
					
				}
		    	map.put("ifsOperatorsList", ifsOperatorsList);
		    	map.put("ifsRightHandFieldList", ifsRightHandFieldList);
		    	map.put("ifsFieldDefinitionList",ifsFieldDefinitionList);
		    	map.put("thenActionsList", thenActionsList);
		    	map.put("thenRightHandFieldsList", thenRightHandFieldsList);
	    	}
		}
	 
	 public static void populateOperatorsAndFieldsByFieldType(Map<String, FieldDefinition> componentList,
				FieldDefinition field, Map<String, String> operatorsString, Map<String, String> fieldsString) {
			if(field !=null){
	    		// getting opeators by field type
	    		String fieldType = field.getFieldType();
	    		Set<Operators> operators = FormValidationDataTypeOperatorMap.getOperatorsByDataType(fieldType);
	    		if(operators == null){
	    			operators = FormValidationDataTypeOperatorMap.getOperatorsByComponenrTypeAndFieldType(fieldType, field.getFieldDataType());
	    		}
	    		if(CollectionUtils.isNotEmpty(operators)) {
					for (Operators operators2 : operators) {
						operatorsString.put(operators2.getOperator_exp(), operators2.getOperator_displayName());
					}
				}
	    		// getting target field type also
	    		
	    		for (Map.Entry<String, FieldDefinition> fields : componentList.entrySet()) {
					if (fields.getValue().getFieldType().equals(field.getFieldType())
							&& (fields.getValue().getFieldDataType() == field.getFieldDataType())) {
						fieldsString.put(fields.getValue().getFieldKey(),fields.getValue().getFieldKey());
						} 
				} 
	    	}
		}
	 
	 public static void populateOperatorsAndFieldListForThen(Map<String, FieldDefinition> componentList, FieldDefinition field,
				Map<String, String> operatorsString, Map<String, String> fieldsString) {
			if(field !=null){
	    		// getting opeators by field type
	    		String fieldType = field.getFieldType();
	    		Set<FormValidationConstants.ThenActionTypes> thenActions = FormValidationDataTypeOperatorMap.getThenActionByFieldType(fieldType);
	    		if(thenActions !=null && !thenActions.isEmpty()){
	    			for (FormValidationConstants.ThenActionTypes then : thenActions) {
	        			operatorsString.put(then.getCode(), then.getCode());
	        			
	    			}
	    		}
	    		// getting target field type also
	    		for (Map.Entry<String, FieldDefinition> fields : componentList.entrySet()) {
					if (fields.getValue().getFieldType().equals(field.getFieldType())
							&& (fields.getValue().getFieldDataType() == field.getFieldDataType())) {
						fieldsString.put(fields.getValue().getFieldKey(), fields.getValue().getFieldKey());
						} 
				} 
	    	}
		}
	 
	 public static String validateValidationsRules(List<FormValidationMetadataVO> validations){
	    	if(validations == null || validations.isEmpty()){
	    		return "No Validation Rules Found";
	    	}
	    	StringBuilder result = new StringBuilder();
	    	int validationIndex = 1;
	    	for (Iterator iterator = validations.iterator(); iterator.hasNext();) {
				FormValidationMetadataVO formValidationMetadataVO = (FormValidationMetadataVO) iterator.next();
				// for deleted rules
	    		if(formValidationMetadataVO == null){
	    			iterator.remove();
				}
	    		else{
		    		boolean checkAndOrOperator = false;
		    		int index  = 1;
		    		if(formValidationMetadataVO.getWhenCondition().equals(FormValidationConstants.WhenActionTypes.PANEL_SAVE_CLICK.name()) && StringUtils.isEmpty(formValidationMetadataVO.getWhenConditionPanelId())){
						result.append("Rule Block : "+validationIndex+" : Invalid Panel Id for PANEL_SAVE_CLICK at:"+index).append("\n");
					}
		    		for (FormValidationRulesIFMetadataVO ifs : formValidationMetadataVO.getIfConditions()) {
		    			boolean checkRightExpression = true;
						if(ifs != null){
							if(checkAndOrOperator && isNullOREmpty(ifs.getJoinWithPreviousRule())){
								result.append("Rule Block : "+validationIndex+" : Invalid Join Operator with previous If at :"+index).append("\n");
							}
							if(isNullOREmpty(ifs.getOperator())){
								result.append("Rule Block : "+validationIndex+" : Invalid Operator in If at :"+index).append("\n");
							}else{
								Operators operator = FormValidationConstants.Operators.getOperatorByOperatorExpression(ifs.getOperator());
								if(operator!=null && (FormValidationConstants.Operators.IS_CHECKED.equals(operator) || FormValidationConstants.Operators.IS_UNCHECKED.equals(operator)
										|| FormValidationConstants.Operators.IS_SELECTED.equals(operator))){
									checkRightExpression = false;
								}
							}
							if(ifs.getLeftOperandFieldKey()==null || isNullOREmpty(ifs.getLeftOperandFieldKey().getExpression())){
								result.append("Rule Block : "+validationIndex+" : Invalid left Operator in If at :"+index).append("\n");
							}
							if(checkRightExpression && (ifs.getRightOperandFieldKey() ==null || isNullOREmpty(ifs.getRightOperandFieldKey().getExpression()))){
								result.append("Rule Block : "+validationIndex+" : Invalid right Operator in If at :"+index).append("\n");
							}
							
							checkAndOrOperator = true;
						}
					}
					if(CollectionUtils.isEmpty(formValidationMetadataVO.getThenActions())){
						result.append("Rule Block : "+validationIndex+ " : Atleast one then action is required").append("\n");
					}else {
						for (FormValidationRulesThenMetadataVO then : formValidationMetadataVO.getThenActions()) {
							if (then != null) {
								if (isNullOREmpty(then.getTargetFieldKey())) {
									result.append("Rule Block : " + validationIndex + " : Invalid Target field in then at :" + index).append("\n");
								}
								if (isNullOREmpty(then.getTypeOfAction())) {
									result.append("Rule Block : " + validationIndex + " : Invalid type of action in then at :" + index).append("\n");
								}
							}
						}
					}
		    		validationIndex++;
				}
	    	}
	    	return result.toString();
	    }
	 
	  public static Boolean isNullOREmpty(String input){
	    	return input == null || input.isEmpty();
	    }
	  
	  public static boolean validateFormVO(FormVO formVO) {

	        if (formVO == null || ValidatorUtils.hasNoElements(formVO.getContainerVOList())) {
	            return false;

	        } else {
	            if (formVO.getContainerVOList() != null && !formVO.getContainerVOList().isEmpty()) {

	                for (FormContainerVO formContainerVO : formVO.getContainerVOList()) {

	                    if ((formContainerVO.getFieldType() != null && (formContainerVO.getFieldType().equals(
	                            FormComponentType.PANEL) || formContainerVO.getFieldType().equals(FormComponentType.TABLE)))
	                            || formContainerVO.getType() == FormContainerType.FIELD_TYPE_VIRTUAL
	                            || formContainerVO.getType() == FormContainerType.FIELD_TYPE_FIELD) {
	                        // it is a panel
	                        if (formContainerVO.getFormContainerVOList() != null
	                                && !formContainerVO.getFormContainerVOList().isEmpty()) {
	                            for (FormContainerVO formContainerVO2 : formContainerVO.getFormContainerVOList()) {
	                                if (formContainerVO2.getFieldKey() == null || formContainerVO2.getFieldKey().isEmpty()
	                                        || formContainerVO2.getFieldDataType() == null) {
	                                    return false;
	                                }
	                            }
	                        }// it is a single field
	                        else {
	                            if (formContainerVO.getFieldKey() == null || formContainerVO.getFieldKey().isEmpty()
	                                    || formContainerVO.getFieldDataType() == null) {
	                                return false;
	                            }
	                        }
	                    }// invalid field type
	                    else {
	                        return false;
	                    }
	                }
	            }
	        }
	        return true;
	    }
	  
	  
	  public static void swapFormContainerInList(List<FormContainerVO> parentList, int sourceIndex, int targetIndex) {
	        if (sourceIndex >= 0 && targetIndex >= 0 && parentList != null && !parentList.isEmpty()
	                && parentList.size() > sourceIndex && parentList.size() > targetIndex) {
	            FormContainerVO sourceElement = parentList.get(sourceIndex);
	            updateComponentDisplayKey(sourceElement, targetIndex);
	            FormContainerVO targetElement = parentList.get(targetIndex);
	            updateComponentDisplayKey(targetElement, sourceIndex);
	            parentList.remove(sourceIndex);
	            if (targetIndex > sourceIndex) {
	                parentList.remove(targetIndex - 1);
	                parentList.add(sourceIndex, targetElement);
	                parentList.add(targetIndex, sourceElement);
	            } else {
	                parentList.remove(targetIndex);
	                parentList.add(targetIndex, sourceElement);
	                parentList.add(sourceIndex, targetElement);
	            }

	        }
	    }

	    public static void updateComponentDisplayKey(FormContainerVO sourceFormContainerVO, int index) {
	        List<Integer> indexList = getPanelComponentIndex(sourceFormContainerVO.getComponentDisplayKey());
			if(indexList != null) {
				if (indexList.size() == 1) {
					sourceFormContainerVO.setComponentDisplayKey("component[" + index + "]");
				} else if (indexList.size() == 2) {
					sourceFormContainerVO.setComponentDisplayKey("component[" + indexList.get(0) + "][" + index + "]");
				}
			}
	    }

	   

	    /**
	     * getting list of index for the elements inside panel
	     * @param componentName
	     * @return
	     */
	    public static List<Integer> getPanelComponentIndex(String componentName) {
	        List<Integer> result = null;
	        if (componentName.contains("[")) {
	            result = new ArrayList<Integer>();
	            int depth = 0;
	            if (componentName.matches("^component\\[\\d+\\]\\[\\d+\\].*")) {
	                depth = 2;
	            } else if (componentName.matches("^component\\[\\d+\\].*")) {
	                depth = 1;
	            }
	            String[] bb = componentName.split("\\[");
	            for (int i = 1 ; i <= depth ; i++) {
	                result.add(Integer.parseInt(bb[i].split("\\]")[0]));
	            }
	        }
	        return result;
	    }
	    public static int getParentPanelFromField(String componentName)
	    {
	        List<Integer> result = null;
	        if (componentName.contains("[")) {
	            result = new ArrayList<Integer>();
	            int depth = 0;
	            if (componentName.matches("^component\\[\\d+\\]\\[\\d+\\].*")) {
	                depth = 2;
	            } else if (componentName.matches("^component\\[\\d+\\].*")) {
	                depth = 1;
	            }
	            
	            if(depth==2)
	            {
	            	String[] bb = componentName.split("\\[");
	                return Integer.parseInt(bb[1].split("\\]")[0]);
	             }
	          
	        }
	        return -1;

	    }
	    
	    public static boolean checkIfDedupeConfiguredDeleted(FormVO formVO, List<Integer> nestedPanelPosition) {
        if (CollectionUtils.isNotEmpty(formVO.getContainerVOList()) && formVO.getDedupeKeySet() != null
                && formVO.getDedupeKeySet().size() != 0) {
                boolean deleted=false;
	            Set<String> elementsToBeDeleted=new HashSet<String>();
	            
	            if (nestedPanelPosition != null && !nestedPanelPosition.isEmpty()) {
	                if (nestedPanelPosition.size() == 1) {
	                    if ((formVO.getContainerVOList().size() - 1) >= nestedPanelPosition.get(0).intValue()) {
	                        FormContainerVO formContainerVO = formVO.getContainerVOList()
	                                .get(nestedPanelPosition.get(0).intValue());
	                        if (formVO.getDedupeKeySet().contains(formContainerVO.getFieldKey())) {
	                            for (FormContainerVO formContainerVOChilds : formContainerVO.getFormContainerVOList()) {
	                                if (formVO.getDedupeKeySet().contains(formContainerVOChilds.getFieldKey())) {
	                                    String elementDeleted = formContainerVO.getFieldKey() + "."
	                                            + formContainerVOChilds.getFieldKey();	                                 
	                                    elementsToBeDeleted.add(elementDeleted);	                                   
	                                }
	                            }	                         
	                            deleted=true;
	                        }

	                    }

	                } else {
	                    if ((formVO.getContainerVOList().size() - 1) >= nestedPanelPosition.get(0).intValue()) {
	                        FormContainerVO parentContainer = formVO.getContainerVOList().get(nestedPanelPosition.get(0));
	                        if (parentContainer != null) {
	                            List<FormContainerVO> nestedList = parentContainer.getFormContainerVOList();
	                            if ((nestedList.size() - 1) >= nestedPanelPosition.get(1).intValue()) {
	                                FormContainerVO formContainerVO = nestedList.get(nestedPanelPosition.get(1).intValue());
                                    if (formVO.getDedupeKeySet().contains(formContainerVO.getFieldKey())) {
                                        String elementDeleted = "";
                                        if (StringUtils.isNotEmpty(parentContainer.getFieldKey())) {
                                            elementDeleted = parentContainer.getFieldKey() + "." + formContainerVO.getFieldKey();
                                        } else {
                                            elementDeleted = formContainerVO.getFieldKey();
                                        }
                                        elementsToBeDeleted.add(elementDeleted);
                                        deleted = true;
                                    }
	                            }
	                        }
	                    }

	                }
	            }
	            formVO.setDedupeSetToBeDeleted(elementsToBeDeleted);
	            if(deleted){
	                return false;
	            }
	        }
          
	        return true;
	    }
	    	    
        public static void deleteDedupeMapping(FormVO formVO) {
            if (formVO.getDedupeSetToBeDeleted() != null && formVO.getDedupeSetToBeDeleted().size() != 0
                    && formVO.getDedupeMapperVO() != null) {
                if (formVO.getDedupeSetToBeDeleted().contains(formVO.getDedupeMapperVO().getPathField1())) {
                    formVO.getDedupeMapperVO().setPathField1(null);
                    formVO.getDedupeMapperVO().setScoreField1(null);
                }
                if (formVO.getDedupeSetToBeDeleted().contains(formVO.getDedupeMapperVO().getPathField2())) {
                    formVO.getDedupeMapperVO().setPathField2(null);
                    formVO.getDedupeMapperVO().setScoreField2(null);
                }
                if (formVO.getDedupeSetToBeDeleted().contains(formVO.getDedupeMapperVO().getPathField3())) {
                    formVO.getDedupeMapperVO().setPathField1(null);
                    formVO.getDedupeMapperVO().setScoreField1(null);
                }
                if (formVO.getDedupeSetToBeDeleted().contains(formVO.getDedupeMapperVO().getPathField3())) {
                    formVO.getDedupeMapperVO().setPathField1(null);
                    formVO.getDedupeMapperVO().setScoreField1(null);
                }
                if (formVO.getDedupeSetToBeDeleted().contains(formVO.getDedupeMapperVO().getPathField4())) {
                    formVO.getDedupeMapperVO().setPathField4(null);
                    formVO.getDedupeMapperVO().setScoreField4(null);
                }
                if (formVO.getDedupeSetToBeDeleted().contains(formVO.getDedupeMapperVO().getPathField5())) {
                    formVO.getDedupeMapperVO().setPathField5(null);
                    formVO.getDedupeMapperVO().setScoreField5(null);
                }
    
                Set<String> dedupeKeySet = new HashSet<String>();
                updateDedupe(formVO.getDedupeMapperVO().getPathField1(), dedupeKeySet);
                updateDedupe(formVO.getDedupeMapperVO().getPathField2(), dedupeKeySet);
                updateDedupe(formVO.getDedupeMapperVO().getPathField3(), dedupeKeySet);
                updateDedupe(formVO.getDedupeMapperVO().getPathField4(), dedupeKeySet);
                updateDedupe(formVO.getDedupeMapperVO().getPathField5(), dedupeKeySet);
                updateDedupe(formVO.getDedupeMapperVO().getPathField6(), dedupeKeySet);
                formVO.setDedupeKeySet(dedupeKeySet);
            }
        }

	    /**
	     *  updating and deleting (in case newContainerVO is null) from containerRootList, with index info in nestedPanelPosition
	     * @param containerRootList
	     * @param newContainerVO
	     * @param nestedPanelPosition
	     */
	    public static void updateComponentAtPanel(List<FormContainerVO> containerRootList, FormContainerVO newContainerVO,
	            List<Integer> nestedPanelPosition) {
	        if (nestedPanelPosition != null && !nestedPanelPosition.isEmpty()) {
	            // if only one member in list means it is the last member where the new container has to be added
	            if (nestedPanelPosition.size() == 1) {
	                // in case of deleting component
	                if (newContainerVO == null) {
	                    // reducing display key in case deletion is in between of list
	                    if ((containerRootList.size() - 1) >= nestedPanelPosition.get(0).intValue()) {
	                        for (int i = nestedPanelPosition.get(0) + 1 ; i < containerRootList.size() ; i++) {

	                            if (containerRootList.get(i).getFormContainerVOList() != null
	                                    && !containerRootList.get(i).getFormContainerVOList().isEmpty()) {

	                                List<FormContainerVO> nestedList = containerRootList.get(i).getFormContainerVOList();
	                                for (int j = 0 ; j < nestedList.size() ; j++) {
	                                    nestedList.get(j).setComponentDisplayKey(
	                                            "component["
	                                                    + (getComponentIndexFromCode(containerRootList.get(i)
	                                                            .getComponentDisplayKey()) - 1) + "][" + Integer.toString(j)
	                                                    + "]");

	                                }

	                            }
	                            containerRootList.get(i)
	                                    .setComponentDisplayKey(
	                                            "component["
	                                                    + (getComponentIndexFromCode(containerRootList.get(i)
	                                                            .getComponentDisplayKey()) - 1) + "]");

	                        }
	                        containerRootList.remove(nestedPanelPosition.get(0).intValue());
	                    }

	                } else {
	                    containerRootList.set(nestedPanelPosition.get(0), newContainerVO);
	                }
	            }// it means it is in some panel
	            else {
	            	// setComponentAtPanel(sourceFormContainerVOList, newContainerVO, nestedPanelPosition);
	            	List<FormContainerVO> nestedList = containerRootList.get(nestedPanelPosition.get(0))
	            			.getFormContainerVOList();
	            	FormContainerVO formContainerVo = nestedList.get(nestedPanelPosition.get(1).intValue());

                    if(newContainerVO == null && (formContainerVo.getFieldType().equals(FormComponentType.CASCADED_SELECT)|| formContainerVo.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT))){
                        // adding parent and its child element in this list
                    	FormContainerVO temp = formContainerVo;
            			Boolean breakCondition = true;
            			Integer count = 0;
            			while(breakCondition || count <= nestedList.size()){
            				count++;
            				if(nestedList.size()>0){
	            				for (Iterator iterator2 = nestedList.iterator(); iterator2.hasNext();) {
	    							FormContainerVO formContainerVO = (FormContainerVO) iterator2.next();
	    							if(formContainerVO.getComponentDisplayKey().equals(formContainerVo.getComponentDisplayKey())){
	    								iterator2.remove();
	    								continue;
	    							}
	    							if (formContainerVO != null && formContainerVO.getFieldType() != null
	    									&& (formContainerVo.getFieldType().equals(FormComponentType.CASCADED_SELECT) || formContainerVo.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT))
	    									&& formContainerVO.getParentFieldKey() != null && temp.getFieldKey()!=null
	    									&& formContainerVO.getParentFieldKey().equals(temp.getFieldKey()))
	    								{
		    								temp = formContainerVO;
		    								iterator2.remove();
		    								breakCondition = true;
		    								break;
		    							}else{
		    									breakCondition = false;
		    								}
	    							}
            				}else{
            					breakCondition = false;
            				}
            			}
                        if(CollectionUtils.isNotEmpty(nestedList)) {
                            for (int j = 0; j < nestedList.size(); j++) {
                                List<Integer> indexInsidePanelList = getPanelComponentIndex(nestedList.get(j).getComponentDisplayKey());
								if(indexInsidePanelList != null) {
									nestedList.get(j).setComponentDisplayKey("component[" + indexInsidePanelList.get(0) + "][" + j + "]");
								}
                            }
                        }
                        /*if(formContainerVo.getParentFieldKey() == null || formContainerVo.getParentFieldKey().isEmpty()){
                            for(int i = 0 ; i < nestedList.size() ; i++){
                                FormContainerVO childContainerVo = nestedList.get(i);
                                if(childContainerVo.getFieldType().equals(FormComponentType.CASCADED_SELECT) && childContainerVo.getParentFieldKey().equals(formContainerVo.getFieldKey())){
                                    deleteComponentList.add(childContainerVo);
                                    break;
                                }
                            }
                            nestedList.removeAll(deleteComponentList);
                        }
                        //updating keys of remaining elements
                        for (int j = 0 ; j < nestedList.size() ; j++) {
                            List<Integer> indexInsidePanelList = getPanelComponentIndex(nestedList.get(j)
                                    .getComponentDisplayKey());
                            nestedList.get(j).setComponentDisplayKey(
                                    "component[" + indexInsidePanelList.get(0) + "][" + j
                                            + "]");
                        }*/
                    }
	            	else if (newContainerVO == null) {
	            		if ((nestedList.size() - 1) > nestedPanelPosition.get(1).intValue()) {
	            			for (int i = nestedPanelPosition.get(1) + 1 ; i < nestedList.size() ; i++) {
	            				List<Integer> indexInsidePanelList = getPanelComponentIndex(nestedList.get(i)
	            						.getComponentDisplayKey());
	            				nestedList.get(i).setComponentDisplayKey(
	            						"component[" + indexInsidePanelList.get(0) + "][" + (indexInsidePanelList.get(1) - 1)
	            						+ "]");
	            			}
	            		}
	            		// updating map of field keys- in case of element inside panel
	            		// updateFieldKeyMap(nestedList.get(nestedPanelPosition.get(1).intValue()));
	            		nestedList.remove(nestedPanelPosition.get(1).intValue());
	            	}
	            	else {
                    	FormContainerVO formContainerVO = nestedList.get(nestedPanelPosition.get(1));
	            		if(StringUtils.isNotBlank(formContainerVO.getSpecialTable())){
							newContainerVO.setSpecialTable(formContainerVO.getSpecialTable());
							newContainerVO.setFieldDataType(formContainerVO.getFieldDataType());
						}
                    	nestedList.set(nestedPanelPosition.get(1), newContainerVO);
	            	}
	            }
	        }
	    }

	    /**
	     *  placing newContainerVo into containerRootList having panel code panelCode
	     * @param newContainerVO
	     * @param panelCode
	     * @param containerRootList
	     */
	    public static void addComponentAtPanel(FormContainerVO newContainerVO, String panelCode,
	            List<FormContainerVO> containerRootList) {
	    	addComponentAtPanel(newContainerVO, panelCode,
		           containerRootList,null);
	    }
	    /**
	     *  placing newContainerVo into containerRootList having panel code panelCode
	     * @param newContainerVO
	     * @param panelCode
	     * @param containerRootList
	     */
	    public static void addComponentAtPanel(FormContainerVO newContainerVO, String panelCode,
	            List<FormContainerVO> containerRootList,Integer index) {
	        if (panelCode != null && !panelCode.isEmpty()) {
	            FormContainerVO containerVO = containerRootList.get(getComponentIndexFromCode(panelCode));
	            List<FormContainerVO> containerVOList = containerVO.getFormContainerVOList();
	            if (containerVOList == null) {
	                containerVOList = new ArrayList<FormContainerVO>();
	            }
	            if(index !=null){
	            	index += 1;			//added by shikhar
	            	newContainerVO.setComponentDisplayKey(newContainerVO.getComponentDisplayKey().concat(
		                    "[" + index + "]"));
		            containerVOList.add(index,newContainerVO);
		            
		            for(int i = index+1; i < containerVOList.size(); i++){
		            	FormContainerVO existingContainer = containerVOList.get(i);
		            	String compDispKey = existingContainer.getComponentDisplayKey();
		            	StringTokenizer st = new StringTokenizer(compDispKey,"[]", false);
		            	
		            	String token1 = st.nextToken();
		            	String token2 = st.nextToken();
		            	String token3 = st.nextToken();
		            	
		            	Integer token3_int = Integer.parseInt(token3);
		            	token3_int += 1;
		            	token3 = token3_int.toString();
		            	
		            	String compDispKey_updated = token1+"["+token2+"]["+token3+"]";
		            	existingContainer.setComponentDisplayKey(compDispKey_updated);
		            }
		            
	            }else{
	            	newContainerVO.setComponentDisplayKey(newContainerVO.getComponentDisplayKey().concat(
		                    "[" + containerVOList.size() + "]"));
		            containerVOList.add(newContainerVO);
	            }
	            containerRootList.get(getComponentIndexFromCode(panelCode)).setFormContainerVOList(containerVOList);
	        }
	    }

	    /**
	     * getting index of component at Form level
	     * @param componentName
	     * @return
	     */
	    public static int getComponentIndexFromCode(String componentName) {
	        if (componentName.contains("[")) {
	            String[] bb = componentName.split("\\[");
	            return Integer.parseInt(bb[1].split("\\]")[0]);
	        }
	        return -1;
	    }

	    /**
	     * 
	     * Method to set the field data type
	     * @param formContainerVO
	     * @param componentCode
	     * @return
	     */
	    public static FormContainerVO updateContainerVO(FormContainerVO formContainerVO, String componentCode) {

	        if (componentCode.equals(FormComponentType.DROP_DOWN) || componentCode.equals(FormComponentType.RADIO)
	                || componentCode.equals(FormComponentType.MULTISELECTBOX)
	                || componentCode.equals(FormComponentType.AUTOCOMPLETE) 
	                 || componentCode.equals((FormComponentType.CASCADED_SELECT) ) || componentCode.equals(FormComponentType.CUSTOM_CASCADED_SELECT)
	                ) {

	            if (null != formContainerVO.getBinderName()
	                    && formContainerVO.getBinderName().equals(FormConfigurationConstant.CUSTOM_BINDER)) {
	                formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_TEXT);

	            } else {
	                formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_TEXT_REFERENCE);
	            }

	        } else if (componentCode.equals(FormComponentType.PHONE)) {
	            formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_PHONE);

	        } else if (componentCode.equals(FormComponentType.EMAIL)) {
				formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_EMAIL);

			}else if (componentCode.equals(FormComponentType.LOV)) {
				formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_LOV);

			} else if (componentCode.equals(FormComponentType.CHECKBOX)) {
	            formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_TEXT_BOOLEAN);

	        } else if (componentCode.equals(FormComponentType.TEXT_AREA)) {
	            formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_TEXT);

	        } else if (componentCode.equals(FormComponentType.DATE)) {
	            formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_DATE);

	        } else if (componentCode.equals(FormComponentType.MONEY)) {
	            formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_MONEY);
	        
	        } else if (componentCode.equals(FormComponentType.HYPERLINK)) {
	            formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_TEXT);
	       
	        } else if (componentCode.equals(FormComponentType.BUTTON)) {
	            formContainerVO.setFieldDataType(FieldDataType.DATA_TYPE_TEXT);
	        
	        }

	        return formContainerVO;
	    }

	    /**
	     * 
	     * get supported data types list
	     * @param componentCode
	     * @return
	     */
	    public static List<Map<String,String>> createBindToColumnList(List<String> columnList){
	    	List<Map<String,String>> items = new ArrayList<Map<String,String>>();
	        for(String column : columnList){
	        	Map<String,String> item= new HashMap<String, String>();
	        	item.put("itemLabel", column);
	        	item.put("itemValue", column);
	        	items.add(item);
	        }
	        
	        return items;
	    }

	public static UIMetaDataVo mergeFormDetailsAndData(UIMetaData uiMetaData, Map<String, Object> dataMap) {

		UIMetaDataVo uiMetaDataVo = new UIMetaDataVo();
		List<FormComponentVO> formComponentVOList = null;
		List<FormFieldVO> formFieldVOList = null;

		List<Map<String, Object>> actualTableMapData = null;

		Map<String, Object> fieldValueMap = null;

		uiMetaDataVo.setFormName(uiMetaData.getFormName());
		uiMetaDataVo.setFormTitle(uiMetaData.getFormTitle());
		uiMetaDataVo.setModelName(uiMetaData.getModelName());
		uiMetaDataVo.setFormHeader(uiMetaData.getFormHeader());
		uiMetaDataVo.setAllowSaveOption(uiMetaData.getAllowSaveOption());
		uiMetaDataVo.setAllowBorder(uiMetaData.getAllowBorder());
		uiMetaDataVo.setFormuuid(uiMetaData.getFormuuid());
		uiMetaDataVo.setFormVersion(uiMetaData.getFormVersion());
		uiMetaDataVo.setFormUri(uiMetaData.getUri());
		uiMetaDataVo.setModelUri(uiMetaData.getModelUri());
		uiMetaDataVo.setValidationJS(uiMetaData.getFormValidationRulesInJS());

		Hibernate.initialize(uiMetaData.getPanelDefinitionList());
		if (null != uiMetaData.getPanelDefinitionList()) {

			formComponentVOList = new ArrayList<FormComponentVO>();

			// Loop through the panels
			for (PanelDefinition panelDefinition : uiMetaData.getPanelDefinitionList()) {

				fieldValueMap = dataMap;
				// intiall list is intialed to new list
				/*formComponentTableList = new ArrayList<FormComponentVO>();*/

				if (null != panelDefinition) {

					if (panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_PANEL) {
						fieldValueMap = (Map<String, Object>) dataMap.get(panelDefinition.getPanelKey());
					}

					if ((panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_TABLE) || ((panelDefinition.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE))) {
						actualTableMapData = (List<Map<String, Object>>) dataMap.get(panelDefinition.getPanelKey());
					}

					FormComponentVO formComponentVO = new FormComponentVO();
					/*FormComponentVO formComponentTableVO = new FormComponentVO();*/

					formComponentVO.setPanelName(panelDefinition.getPanelName());
					formComponentVO.setPanelHeader(panelDefinition.getPanelHeader());
					formComponentVO.setAccordian(panelDefinition.isAccordian());
					formComponentVO.setDisplayBorder(panelDefinition.isDisplayBorder());
					formComponentVO.setPanelColumnLayout(panelDefinition.getPanelColumnLayout());
					formComponentVO.setPanelType(panelDefinition.getPanelType());
					formComponentVO.setPanelKey(panelDefinition.getPanelKey());
					formComponentVO.setSpecialTable(panelDefinition.getSpecialTable()!=null?panelDefinition.getSpecialTable().getKeyy():null);
					// Loop through the fields inside panel

					Hibernate.initialize(panelDefinition.getFieldDefinitionList());
					if (null != panelDefinition.getFieldDefinitionList()) {

						formFieldVOList = new ArrayList<FormFieldVO>();
						if (formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_PANEL
								|| formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_VIRTUAL) {
							formComponentVO.setAllowPanelSave(panelDefinition.getAllowPanelSave());
							for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {

								Hibernate.initialize(fieldDefinition.getValue());

								FormFieldVO formFieldVO = new FormFieldVO();
								formFieldVO.setId(fieldDefinition.getFieldKey());
								formFieldVO.setParent(fieldDefinition.getParent());
								formFieldVO.setDisable(fieldDefinition.getDisable());
								formFieldVO.setFieldDataType(fieldDefinition.getFieldDataType());
								// to generate default Date

								setDateFieldProperties(fieldDefinition, formFieldVO);

								// If Actual panel and virtual panel
								setFieldValuesForNonTable(formFieldVOList, fieldDefinition, formFieldVO);

								if (null != fieldValueMap && fieldValueMap.size() > 0) {
									setFieldValue(fieldValueMap, fieldDefinition, formFieldVO);
								} else {
									formFieldVO.setValue(fieldDefinition.getValue());
								}

								formComponentVO.setFormFieldVOList(formFieldVOList);

							}
						} else if ((formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_TABLE) || (formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)) {
							// value in configuration map for table
							if (actualTableMapData != null) {
								List<FormComponentVO> formComponentVOs = new ArrayList<FormComponentVO>();
								for (Map<String, Object> singleTableRowMap : actualTableMapData) {
									FormComponentVO tableComponentVO = new FormComponentVO();
									List<FormFieldVO> fieldVOs = new ArrayList<FormFieldVO>();
									for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {

										Hibernate.initialize(fieldDefinition.getValue());
										Hibernate.initialize(fieldDefinition.getSpecialTable());
										FormFieldVO formFieldVO = new FormFieldVO();
										formFieldVO.setId(fieldDefinition.getFieldKey());
										formFieldVO.setParent(fieldDefinition.getParent());
										formFieldVO.setDisable(fieldDefinition.getDisable());
										formFieldVO.setFieldDataType(fieldDefinition.getFieldDataType());
										formFieldVO.setSpecialTable(fieldDefinition.getSpecialTable()!=null?fieldDefinition.getSpecialTable().getKeyy():null);
										// setting filed value
										// If not Table - add properties - Run in all case
										setFieldValueForTable(fieldDefinition, formFieldVO);
										// setting value from map

										setFieldValue(singleTableRowMap, fieldDefinition, formFieldVO);
										fieldVOs.add(formFieldVO);
									}
									tableComponentVO.setFormFieldVOList(fieldVOs);
									formComponentVOs.add(tableComponentVO);
								}
								formComponentVO.setFormComponentList(formComponentVOs);
							}
							// no value in configuration map for table
							else {
								List<FormComponentVO> formComponentVOs = new ArrayList<FormComponentVO>();
								FormComponentVO tableComponentVO = new FormComponentVO();
								List<FormFieldVO> fieldVOs = new ArrayList<FormFieldVO>();
								for (FieldDefinition fieldDefinition : panelDefinition.getFieldDefinitionList()) {

									Hibernate.initialize(fieldDefinition.getValue());
									Hibernate.initialize(fieldDefinition.getSpecialTable());
									FormFieldVO formFieldVO = new FormFieldVO();
									formFieldVO.setId(fieldDefinition.getFieldKey());
									formFieldVO.setParent(fieldDefinition.getParent());
									formFieldVO.setDisable(fieldDefinition.getDisable());
									formFieldVO.setFieldDataType(fieldDefinition.getFieldDataType());
									formFieldVO.setSpecialTable(fieldDefinition.getSpecialTable()!=null?fieldDefinition.getSpecialTable().getKeyy():null);
									// setting filed value
									// If not Table - add properties - Run in all case
									setFieldValueForTable(fieldDefinition, formFieldVO);
									// setting default value
									if((formComponentVO.getPanelType() == FormContainerType.FIELD_TYPE_SPECIAL_TABLE)){
										List list = new ArrayList();
										list.add(" ");
										formFieldVO.setValue(list);
									}else{
									formFieldVO.setValue(fieldDefinition.getValue());
									}
									fieldVOs.add(formFieldVO);
								}
								tableComponentVO.setFormFieldVOList(fieldVOs);
								formComponentVOs.add(tableComponentVO);
								formComponentVO.setFormComponentList(formComponentVOs);
							}

						}

					}
					formComponentVOList.add(formComponentVO);
				}
			}
			uiMetaDataVo.setUiComponents(formComponentVOList);
		}
		return uiMetaDataVo;
	}

	private static void setDateFieldProperties(FieldDefinition fieldDefinition, FormFieldVO formFieldVO) {
		if (fieldDefinition.getFieldType().equals(FormComponentType.DATE)) {
			if (fieldDefinition.getValue() == null || fieldDefinition.getValue().size() == 0
					|| "".equals(fieldDefinition.getValue().get(0))
					|| ValidatorUtils.isNull(fieldDefinition.getValue().get(0))) {
				if (fieldDefinition.getDefaultYear() != null && fieldDefinition.getDefaultMonth() != null) {

					formFieldVO.setDefDate(fieldDefinition.getDefaultMonth() + "/1/" + fieldDefinition.getDefaultYear());

				} else if (fieldDefinition.getDefaultYear() != null && fieldDefinition.getDefaultMonth() == null) {
					formFieldVO.setDefDate("1/1/" + fieldDefinition.getDefaultYear());
				}

				else if (fieldDefinition.getDefaultYear() == null && fieldDefinition.getDefaultMonth() != null) {
					int currentYear = Calendar.getInstance().get(Calendar.YEAR);
					formFieldVO.setDefDate(fieldDefinition.getDefaultMonth() + "/1/" + currentYear);
				}
			}

			else {

				formFieldVO.setDefDate(fieldDefinition.getValue().get(0));
			}

		}
	}

	private static void setFieldValueForTable(FieldDefinition fieldDefinition, FormFieldVO formFieldVO) {
		formFieldVO.setItemLabel(fieldDefinition.getItemLabel());
		formFieldVO.setBinderName(fieldDefinition.getBinderName());
		formFieldVO.setFieldType(fieldDefinition.getFieldType());
		formFieldVO.setMandatoryField(fieldDefinition.isMandatoryField());
		formFieldVO.setExpandableField(fieldDefinition.getExpandableField());
		formFieldVO.setIncludeSelect(fieldDefinition.isIncludeSelect());
		formFieldVO.setItemValue(fieldDefinition.getItemValue());
		formFieldVO.setFieldSequence(fieldDefinition.getFieldSequence());
		formFieldVO.setFieldLabel(fieldDefinition.getFieldLabel());
		formFieldVO.setToolTipMessage(fieldDefinition.getToolTipMessage());
		formFieldVO.setEntityName(fieldDefinition.getEntityName());
		formFieldVO.setMinFieldLength(fieldDefinition.getMinFieldLength());
		formFieldVO.setMaxFieldLength(fieldDefinition.getMaxFieldLength());
		formFieldVO.setMinFieldValue(fieldDefinition.getMinFieldValue());
		formFieldVO.setMaxFieldValue(fieldDefinition.getMaxFieldValue());
		formFieldVO.setHref(fieldDefinition.getHref());
        formFieldVO.setFunctionLogic(fieldDefinition.getFunctionLogic());
        formFieldVO.setAuthority(fieldDefinition.getAuthority());
		
		// for date field tag

		setDateFieldProperties(fieldDefinition, formFieldVO);

		// For Phone Tag Field
		formFieldVO.setMobile(fieldDefinition.getMobile());

		//For LOV Tag Field
		formFieldVO.setLovKey(fieldDefinition.getLovKey());

		// For AutoComplete Tag Field
		String autoCompleteColumnsHolder = fieldDefinition.getAutoCompleteColumnsHolder();
		if (StringUtils.isNotBlank(autoCompleteColumnsHolder)) {
			String[] columns = autoCompleteColumnsHolder.split(",");
			StringBuilder stringBuilder = new StringBuilder();
			for (String column : columns) {
				stringBuilder.append(column);
				stringBuilder.append(" ");
			}
			formFieldVO.setSearchableColumns(stringBuilder.toString());
		}

		formFieldVO.setCustomeLongMessage(fieldDefinition.getCustomeLongMessage());

		if (fieldDefinition.getFieldCustomOptionsList() != null && fieldDefinition.getFieldCustomOptionsList().size() > 0) {
			List<FieldCustomOptionsVO> customOptionsList = new ArrayList<FieldCustomOptionsVO>();
			for (FieldCustomOptions fieldCustomOptions : fieldDefinition.getFieldCustomOptionsList()) {
				FieldCustomOptionsVO customOptions = new FieldCustomOptionsVO();
				customOptions.setCustomeItemLabel(fieldCustomOptions.getCustomeItemLabel());
				customOptions.setCustomeItemValue(fieldCustomOptions.getCustomeItemValue());
				customOptionsList.add(customOptions);
			}
			formFieldVO.setFieldCustomOptionsVOList(customOptionsList);
		}
		if(fieldDefinition.getFieldType().equals(FormComponentType.CASCADED_SELECT) || fieldDefinition.getFieldType().equals(FormComponentType.CUSTOM_CASCADED_SELECT)){
			if(fieldDefinition.getParentFieldKey() != null)
				formFieldVO.setParentFieldKey(fieldDefinition.getParentFieldKey());
			if(fieldDefinition.getActiveChildEntityName() != null)
				formFieldVO.setCurrentChildEntityName(fieldDefinition.getActiveChildEntityName());
			if(fieldDefinition.getUrlCascadeSelect() != null)
				formFieldVO.setUrlCascadeSelect(fieldDefinition.getUrlCascadeSelect());
		}
	}

	/**
	 *
	 * Common method that will be executed
	 *  in case - Actual Panel, Virtual Panel
	 *  Only Map differs
	 * @param fieldValueMap
	 * @param fieldDefinition
	 * @param formFieldVO
	 */
	private static void setFieldValue(Map<String, Object> fieldValueMap, FieldDefinition fieldDefinition, FormFieldVO formFieldVO) {
		if (null != fieldValueMap && fieldValueMap.containsKey(fieldDefinition.getFieldKey())) {

			List<String> newValues = new ArrayList<String>();
			List<Object> oldValuesList = null;

			Object object = fieldValueMap.get(fieldDefinition.getFieldKey());

			if (object instanceof java.util.List) {

				oldValuesList = (List<Object>) fieldValueMap.get(fieldDefinition.getFieldKey());

			} else {

				oldValuesList = new ArrayList<Object>();
				oldValuesList.add(object);
			}

			if (formFieldVO.getFieldDataType() == FieldDataType.DATA_TYPE_MONEY) {

				if (null != oldValuesList) {
					for (Object obj : oldValuesList) {
						newValues.add(obj.toString());
					}

					formFieldVO.setValue(newValues);
				}

			} else if (fieldDefinition.getFieldType().equals(FormComponentType.PHONE)) {
				if (null != oldValuesList && oldValuesList.size() > 0) {
					formFieldVO.setPhoneNumberVO((PhoneNumberVO) oldValuesList.get(0));
				} else {
					formFieldVO.setPhoneNumberVO(new PhoneNumberVO());
				}
				formFieldVO.setMobile(fieldDefinition.getMobile());

			} else if (fieldDefinition.getFieldType().equals(FormComponentType.EMAIL)) {
				if (org.apache.commons.collections4.CollectionUtils.isNotEmpty(oldValuesList)) {
					formFieldVO.setEmailInfoVO((EmailInfoVO) oldValuesList.get(0));
				} else {
					formFieldVO.setEmailInfoVO(new EmailInfoVO());
				}
			} else if (fieldDefinition.getFieldType().equals(FormComponentType.LOV)){
				if (CollectionUtils.isNotEmpty(oldValuesList)) {
					formFieldVO.setLovFieldVO((LOVFieldVO) oldValuesList.get(0));
				} else {
					formFieldVO.setLovFieldVO(new LOVFieldVO());
				}
			} else {
				if (null != oldValuesList) {
					for (Object obj : oldValuesList) {
						newValues.add(obj.toString());
					}
					formFieldVO.setValue(newValues);
				}
			}

		}else{
			formFieldVO.setValue(fieldDefinition.getValue());
		}
	}

	private static void setFieldValuesForNonTable(List<FormFieldVO> formFieldVOList, FieldDefinition fieldDefinition,
										   FormFieldVO formFieldVO) {
		setFieldValueForTable(fieldDefinition, formFieldVO);

		formFieldVOList.add(formFieldVO);
	}
	public static List<Map<String,String>> createBindToColumnList(String[] columnList,String itemLabel,String itemValue){
    	List<Map<String,String>> items = new ArrayList<>();
        for(String column : columnList){
        	Map<String,String> item= new HashMap<>();
        	item.put(itemLabel, column);
        	item.put(itemValue, column);
        	items.add(item);
        }
        return items;
    }
	
	public static Method getPersistentFormDataGetterOrSetterMethod(Object object,String methodName) {
    	Method persistentFormDaTaMethod = null;
    	try {
    		if(PERSISTENT_FORM_DATA_SETTER_METHOD.equals(methodName)) {
        		persistentFormDaTaMethod = object.getClass().getMethod(methodName, PersistentFormData.class);
    		}else if(PERSISTENT_FORM_DATA_GETTER_METHOD.equals(methodName)){
    			persistentFormDaTaMethod = object.getClass().getMethod(methodName, null);
			}
		} catch (Exception exception) {			
		}	
    	return persistentFormDaTaMethod;
	}


}
