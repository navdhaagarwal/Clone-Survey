package com.nucleus.core.formsConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import org.cryptacular.spec.Spec;

import java.io.Serializable;
import java.util.List;

public class FormComponentVO implements Serializable {

    private static final long     serialVersionUID        = -22134007536119034L;

    public static final int       UI_COMPONENT_TYPE_PANEL = 0;
    public static final int       UI_COMPONENT_TYPE_GROUP = 1;
    public static final int       UI_COMPONENT_TYPE_TABLE = 2;
    public static final int       UI_COMPONENT_TYPE_FIELD = 3;

    private int                   type                    = 0;

    @ApiModelProperty(notes="This field is Panel Name",required=false,dataType="String",hidden=false)
    private String                panelName;

    @ApiModelProperty(notes="This field is Panel Header",required=false,dataType="String",hidden=false)
    private String                panelHeader;

    @ApiModelProperty(notes="This field is Accordian",required=false,dataType="boolean",hidden=false)
    private boolean               accordian;

    @ApiModelProperty(notes="This field is Display Border",required=false,dataType="boolean",hidden=false)
    private boolean               displayBorder;

    @ApiModelProperty(notes="This field is Form Field VO List",required=false,dataType="List of FormFieldVO",hidden=false)
    private List<FormFieldVO>     formFieldVOList;

    @ApiModelProperty(notes="This field is Panel Type",required=false,dataType="int",hidden=false)
    private int                   panelType;

    @ApiModelProperty(notes="This field is Form Component List",required=false,dataType="List of FormComponentVO",hidden=false)
    private List<FormComponentVO> formComponentList;

    /**
     * value used to decide panel layout i.e one column or two column layout
     */
    @ApiModelProperty(notes="This field is Panel Column Layout",required=false,dataType="int",hidden=false)
    private int                   panelColumnLayout;

    @ApiModelProperty(notes="This field is Panel Key",required=false,dataType="String",hidden=false)
    private String                panelKey;

    @ApiModelProperty(notes="This field is Special Table",required=false,dataType="String",hidden=false)
    private String specialTable;

    @ApiModelProperty(notes="This field is Allow Panel Save",required=false,dataType="Boolean",hidden=false)
    private Boolean allowPanelSave;

    public Boolean getAllowPanelSave() {
        return allowPanelSave;
    }

    public void setAllowPanelSave(Boolean allowPanelSave) {
        this.allowPanelSave = allowPanelSave;
    }

    public String getSpecialTable() {
        return specialTable;
    }

    public void setSpecialTable(String specialTable) {
        this.specialTable = specialTable;
    }

    /**
     * @return the panelName
     */
    public String getPanelName() {
        return panelName;
    }

    /**
     * @param panelName the panelName to set
     */
    public void setPanelName(String panelName) {
        this.panelName = panelName;
    }

    /**
     * @return the panelHeader
     */
    public String getPanelHeader() {
        return panelHeader;
    }

    /**
     * @param panelHeader the panelHeader to set
     */
    public void setPanelHeader(String panelHeader) {
        this.panelHeader = panelHeader;
    }

    /**
     * @return the accordian
     */
    public boolean isAccordian() {
        return accordian;
    }

    /**
     * @param accordian the accordian to set
     */
    public void setAccordian(boolean accordian) {
        this.accordian = accordian;
    }

    /**
     * @return the displayBorder
     */
    public boolean isDisplayBorder() {
        return displayBorder;
    }

    /**
     * @param displayBorder the displayBorder to set
     */
    public void setDisplayBorder(boolean displayBorder) {
        this.displayBorder = displayBorder;
    }

    /**
     * @return the formFieldVOList
     */
    public List<FormFieldVO> getFormFieldVOList() {
        return formFieldVOList;
    }

    /**
     * @param formFieldVOList the formFieldVOList to set
     */
    public void setFormFieldVOList(List<FormFieldVO> formFieldVOList) {
        this.formFieldVOList = formFieldVOList;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the panelColumnLayout
     */
    public int getPanelColumnLayout() {
        return panelColumnLayout;
    }

    /**
     * @param panelColumnLayout the panelColumnLayout to set
     */
    public void setPanelColumnLayout(int panelColumnLayout) {
        this.panelColumnLayout = panelColumnLayout;
    }

    /**
     * @return the formComponentList
     */
    public List<FormComponentVO> getFormComponentList() {
        return formComponentList;
    }

    /**
     * @param formComponentList the formComponentList to set
     */
    public void setFormComponentList(List<FormComponentVO> formComponentList) {
        this.formComponentList = formComponentList;
    }

    /**
     * @return the panelType
     */
    public int getPanelType() {
        return panelType;
    }

    /**
     * @param panelType the panelType to set
     */
    public void setPanelType(int panelType) {
        this.panelType = panelType;
    }

    /**
     * @return the panelKey
     */
    public String getPanelKey() {
        return panelKey;
    }

    /**
     * @param panelKey the panelKey to set
     */
    public void setPanelKey(String panelKey) {
        this.panelKey = panelKey;
    }

}
