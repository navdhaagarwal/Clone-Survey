package com.nucleus.web.tagHandler;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.genericparameter.entity.GenericParameter;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.tag.TagProtectionUtil;

@Transactional
public class SelectTagHandlerApplicantEntry extends SimpleTagSupport implements DynamicAttributes {

    @Autowired
    GenericParameterService     genericParameterService;

    @Autowired
    MessageSource               bundleMessageSource;

    private Map<String, Object> dynamicAttrs = new HashMap<String, Object>();
    private String              genericParameterType;
    private String              genericParameterPath;
    private Long                selectedGenericParameterId;
    private String              validationClass;
    private String              onChange;
    private String              colSpan;
    private String              label;
    private String              mandatory;
    private String              selectBoxColSpan;
    private String              viewMode;
    private String              id;
    private String              toolTip;
    private Integer             tabindex;
    private String              parentCode;
    private String 				pathPrepender;
    private String 				placeHolderKey;
    private String 				sortBy;
    private String 				comparatorType;
    private String 				modificationAllowed;
    private Boolean 			parentCodeNullFlag = Boolean.FALSE;
    
    
    private static final String FALSE = "false";
    
    @SuppressWarnings({ "unchecked" })
    @Override
    public void doTag() throws JspException {

        JspWriter out = getJspContext().getOut();
        PageContext pc = (PageContext) getJspContext();
        HttpServletRequest request = (HttpServletRequest) pc.getRequest();  
        String direction=(String)request.getSession().getAttribute("alignment");
        String directionCssClass="";
        if(direction!=null&&("rtl".equalsIgnoreCase(direction)))
        {
        	directionCssClass="chosen-rtl";
        }
        if(checkAndValidateIfRegionalVisibilityIsTrue(request)){
        	StringBuilder stringBuilder = new StringBuilder();
            String selectBoxSpanClass = null;
            genericParameterService = (GenericParameterService) RequestContextUtils.findWebApplicationContext(request).getBean(
                    "genericParameterService");

            bundleMessageSource = (MessageSource) RequestContextUtils.findWebApplicationContext(request).getBean("messageSource");
            String dynamicAttributeKey = "";
            String dynamicAttributeValue = "";

            if (!dynamicAttrs.isEmpty()) {
                for (Map.Entry<String, Object> entry : dynamicAttrs.entrySet()) {
                    dynamicAttributeKey = entry.getKey();
                    if(entry != null && entry.getValue() != null ) {
                    	dynamicAttributeValue =entry.getValue().toString();
                    }

                }
            }
            try {
                Locale loc = RequestContextUtils.getLocale(request);
                String result = bundleMessageSource.getMessage(label, null, loc);

                label = result;
                Class<GenericParameter> genParamClassName = (Class<GenericParameter>) Class.forName(genericParameterType);
                List<GenericParameter> genericParamList = null;
                if (parentCode != null && !(parentCode.isEmpty()) && !parentCodeNullFlag) {
                    genericParamList = genericParameterService.findChildrenByParentCode(parentCode, genParamClassName);
                }
                else if(parentCode != null && !(parentCode.isEmpty()) && parentCodeNullFlag){
                    genericParamList  = genericParameterService.findChildrenByParentCodeAndNullCode(parentCode, genParamClassName,parentCodeNullFlag,true);
                }
                else {
                    genericParamList = genericParameterService.retrieveTypes(genParamClassName);
                }

                // Move others to last if present
                GenericParameter genericParameterForOthers = null;
                ListIterator<GenericParameter> it = genericParamList.listIterator();
                while (it.hasNext()) {
                    GenericParameter genericParameter = it.next();
                    if ("others".equalsIgnoreCase(genericParameter.getCode())) {
                        genericParameterForOthers = genericParameter;
                        it.remove();
                    }
                }

                if (genericParameterForOthers != null) {
                    genericParamList.add(genericParameterForOthers);
                }
                
                genericParameterService.sortGenericParameterList(genericParamList, sortBy, comparatorType);
                
                if (mandatory != null && StringUtils.isNotEmpty(mandatory)) {
                    stringBuilder.append("<div id=\"" + genParamClassName.getSimpleName().toLowerCase()
                            + "-control-group\" class=\"form-group input-group input-group col-sm-" + colSpan + " "+directionCssClass+" \">");
                } else {
                    stringBuilder.append("<div id=\"" + genParamClassName.getSimpleName().toLowerCase()
                            + "-control-group\" class=\"form-group nonMandatory input-group input-group col-sm-" + colSpan + " "+directionCssClass+" \">");
                }
                stringBuilder.append("<label>");
                stringBuilder.append("<strong>" + label + "</strong>");
                stringBuilder.append("");
                if (StringUtils.isNotEmpty(label) && StringUtils.isNotEmpty(mandatory)) {
                    stringBuilder.append("<span style=\"color:red\">&nbsp;*</span>");
                }

                stringBuilder.append("</label>");
                String tooltip = bundleMessageSource.getMessage(toolTip, null, loc);
                
                String select = null;
                if(StringUtils.isBlank(placeHolderKey))
                	  select = bundleMessageSource.getMessage("label.select.one", null, loc);
				else
                	 select = bundleMessageSource.getMessage(placeHolderKey, null, loc);

                toolTip = tooltip;
                	
                appendPathPrependerInGenericParameterPath();
                
                
                stringBuilder.append("<select id=\"" + id + "\" tabindex=\"" + tabindex + "\" data-original-title=\"" + toolTip+ "\" " +
                		" data-placeholder=\"" + select + "\" name=\"" + genericParameterPath + "\" default-title=\""+toolTip+"\" class=\"form-control neoSelectTooltip chosen_a "
                        + validationClass);
                if (StringUtils.isNotEmpty(selectBoxColSpan)) {
                    selectBoxSpanClass = "col-sm-" + selectBoxColSpan;
                } else {
                    selectBoxSpanClass = "col-sm-10";
                }
                stringBuilder.append(" " + selectBoxSpanClass + "\"");
                if (StringUtils.isNotEmpty(onChange)) {
                    stringBuilder.append("onChange=\"" + onChange + "\"");
                }
                if (StringUtils.isNotEmpty(viewMode) && viewMode.equalsIgnoreCase("true")) {
                    stringBuilder.append(" disabled=\"" + viewMode + "\"");
                }
                if (parentCode != null && !(parentCode.isEmpty())) {
                    stringBuilder.append(" data-parent-code=\"" + parentCode + "\"");
                }
                stringBuilder.append(">");

                if (validationClass != null && validationClass.contains("chosen_a")) {
                    stringBuilder.append("<option value=''> </option>");
                } else {
                    stringBuilder.append("<option value=''>" + select + "</option>");
                }
                boolean selectedGenParameter=false;
                for (GenericParameter genericParameter : genericParamList) {
                    stringBuilder.append("<option value=\"").append(genericParameter.getId()).append("\"");
                    if ((selectedGenericParameterId != null && selectedGenericParameterId != 0) && genericParameter.getId() != null) {
                        if (genericParameter.getId().equals(selectedGenericParameterId)) {
                            selectedGenParameter=true;
                            stringBuilder.append(" selected=\"selected\" ");
                            if(StringUtils.isNotEmpty(modificationAllowed) && modificationAllowed.equalsIgnoreCase(FALSE)){
                            	TagProtectionUtil.addProtectedFieldToRequest(request, genericParameterPath, selectedGenericParameterId+"");
                            }
                        }
                    }

                    else if (selectedGenericParameterId == null || selectedGenericParameterId == 0 ) {
                        if(genericParameter.getDefaultFlag()!=null){
                            if (genericParameter.getDefaultFlag()) {
                                stringBuilder.append(" selected=\"selected\" ");
                                if(StringUtils.isNotEmpty(modificationAllowed) && modificationAllowed.equalsIgnoreCase(FALSE)){
                                    TagProtectionUtil.addProtectedFieldToRequest(request, genericParameterPath, selectedGenericParameterId+"");
                                }
                            }
                        }
                    }

                    if ((!dynamicAttributeKey.equals("") && !genericParameter.getCode().equals(""))) {
                        stringBuilder.append(dynamicAttributeKey);
                        stringBuilder.append("=\"" + genericParameter.getCode() + "\"");
                    }
                    stringBuilder.append(">");
                    if(StringUtils.isNotEmpty(viewMode) && viewMode.equalsIgnoreCase("true") && selectedGenParameter && genericParameter.getTransientMaskingMap().get("name")!=null){
                        stringBuilder.append((String)genericParameter.getTransientMaskingMap().get("name")); 
                    }else{
                    stringBuilder.append(genericParameter.getName());
                    }
                    stringBuilder.append("</option>");
                }
                stringBuilder.append("</select>");
                stringBuilder.append("</div>");
                stringBuilder.append("<script type=\"text/javascript\">");
                stringBuilder.append("$(document).ready(function() {");
                //stringBuilder.append("var ids = [" + "\"" + "#<c:out value='${id}'/>" + "\"" + "];");
                stringBuilder.append("var ids = [\'#'" +"+escapeSpecialCharactersInId('"+id+"')"+"];");
                stringBuilder.append("$(ids[0]).data('neutrino-chosen-options',{disable_search_threshold:'20',chosen_auto_update:true});");
                stringBuilder.append(" executeOnLoad(ids); ");
                stringBuilder.append(" }); ");
                stringBuilder.append("</script>");
                out.print(stringBuilder);
            } catch (Exception e) {
                BaseLoggers.exceptionLogger.error("No Such Class Exist:" + e);
                throw new SystemException("No Such Class Exist:" + e);
            }
        }
        
    }

    private void appendPathPrependerInGenericParameterPath() {
		if(pathPrepender!=null && genericParameterPath!=null){
			StringBuilder appendedGenericParameterPath=new StringBuilder();
			appendedGenericParameterPath.append(pathPrepender).append(".").append(genericParameterPath);
			genericParameterPath=appendedGenericParameterPath.toString();
		}
		
	}

	private Boolean checkAndValidateIfRegionalVisibilityIsTrue(
			HttpServletRequest request) {
    
		String fieldName=null;
		
		if(id != null){
		 	fieldName=id;
		}else{
			throw new SystemException(
					"Attribute 'id' must be specified");
		} 
		
		String regionalVisibilityValue=(String)request.getAttribute(fieldName+"_regionalVisibility");
		String mandatoryValue=(String)request.getAttribute(fieldName+"_mandatoryMode");
		String viewModeValue=(String)request.getAttribute(fieldName+"_viewMode");
		String labelKeyValue=(String)request.getAttribute(fieldName+"_label");
		String placeHolderKeyValue=(String)request.getAttribute(fieldName+"_placeHolderKey");
		String tooltipKeyValue=(String)request.getAttribute(fieldName+"_toolTipKey");	
		
		if(mandatoryValue !=null && mandatoryValue != "" && mandatoryValue.equals("true")){
			mandatory=mandatoryValue;		
		}else if(mandatoryValue !=null &&mandatoryValue != "" && mandatoryValue.equals(FALSE)){
			mandatory="";	
		}		 
		if(viewModeValue !=null && viewModeValue != ""){
			viewMode=viewModeValue;					
		}
		if(labelKeyValue !=null && labelKeyValue != ""){
			label=labelKeyValue;				
		}
		/*if(placeHolderKeyValue!=null && placeHolderKeyValue!=""){
			jspContext.setAttribute("placeHolderKey",placeHolderKey);	
		}*/
		if(tooltipKeyValue!=null && tooltipKeyValue!=""){
			toolTip=tooltipKeyValue;
		}	
		if(regionalVisibilityValue !=null && regionalVisibilityValue != "" && regionalVisibilityValue.equals(FALSE)){
			return false;
			
		}else{
			return true;
		}
	}

	public GenericParameterService getGenericParameterService() {
        return genericParameterService;
    }

    public void setGenericParameterService(GenericParameterService genericParameterService) {
        this.genericParameterService = genericParameterService;
    }

    public String getGenericParameterPath() {
        return genericParameterPath;
    }

    public void setGenericParameterPath(String genericParameterPath) {
        this.genericParameterPath = genericParameterPath;
    }

    public String getValidationClass() {
        return validationClass;
    }

    public void setValidationClass(String validationClass) {
        this.validationClass = validationClass;
    }

    public Long getSelectedGenericParameterId() {
        return selectedGenericParameterId;
    }

    public void setSelectedGenericParameterId(Long selectedGenericParameterId) {
        this.selectedGenericParameterId = selectedGenericParameterId;
    }

    public String getOnChange() {
        return onChange;
    }

    public void setOnChange(String onChange) {
        this.onChange = onChange;
    }

    public String getColSpan() {
        return colSpan;
    }

    public void setColSpan(String colSpan) {
        this.colSpan = colSpan;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getPlaceHolderKey() {
        return placeHolderKey;
    }

    public void setPlaceHolderKey(String placeHolderKey) {
        this.placeHolderKey = placeHolderKey;
    }

    public String getMandatory() {
        return mandatory;
    }

    public void setMandatory(String mandatory) {
        this.mandatory = mandatory;
    }

    public String getSelectBoxColSpan() {
        return selectBoxColSpan;
    }

    public void setSelectBoxColSpan(String selectBoxColSpan) {
        this.selectBoxColSpan = selectBoxColSpan;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = viewMode;
    }

    public String getGenericParameterType() {
        return genericParameterType;
    }

    public void setGenericParameterType(String genericParameterType) {
        this.genericParameterType = genericParameterType;
    }

    @Override
    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        dynamicAttrs.put(localName.toLowerCase(), value);
    }

    public Map<String, Object> getDynamicAttrs() {
        return dynamicAttrs;
    }

    public void setDynamicAttrs(Map<String, Object> dynamicAttrs) {
        this.dynamicAttrs = dynamicAttrs;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String toolTip) {
        this.toolTip = toolTip;
    }

    public Integer getTabindex() {
        return tabindex;
    }

    public void setTabindex(Integer tabindex) {
        this.tabindex = tabindex;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

	public String getPathPrepender() {
		return pathPrepender;
	}

	public void setPathPrepender(String pathPrepender) {
		this.pathPrepender = pathPrepender;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getComparatorType() {
		return comparatorType;
	}

	public void setComparatorType(String comparatorType) {
		this.comparatorType = comparatorType;
	}

	public String getModificationAllowed() {
		return modificationAllowed;
	}

	public void setModificationAllowed(String modificationAllowed) {
		this.modificationAllowed = modificationAllowed;
	}

    public Boolean getParentCodeNullFlag() {
        return parentCodeNullFlag;
    }

    public void setParentCodeNullFlag(Boolean parentCodeNullFlag) {
        this.parentCodeNullFlag = parentCodeNullFlag;
    }
}