package com.nucleus.core.web.dynamicQuery.staticBuilder;

import java.util.List;
import java.util.Map;

/**
 * @author Nucleus Software Exports Limited
 *
 */
public class StaticQueryBuilderFilter {

    private String              id;

    private String              field;

    private String              label;

    private String              type;

    private String              optgroup;

    private String              input;

    private boolean             multiple;

    private String              placeholder;

    private boolean             vertical=true;

    private Map<String, String> values;

    private List<String>        operators;

    public static String        INPUT_TYPE_TEXT = "text";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOptgroup() {
        return optgroup;
    }

    public void setOptgroup(String optgroup) {
        this.optgroup = optgroup;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean isVertical() {
        return vertical;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public List<String> getOperators() {
        return operators;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

}
