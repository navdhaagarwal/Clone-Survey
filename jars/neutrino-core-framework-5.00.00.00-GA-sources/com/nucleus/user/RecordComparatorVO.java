package com.nucleus.user;


public class RecordComparatorVO {

    private String fieldName;
    private String oldValue;
    private String newValue;
    private String deletedValue;
    private String addedValue;

    public static final String ROLE_NAMES                  = "roleNames";
    public static final String BRANCH_CODE                 = "branchCode";
    public static final String BRANCH_ADMIN_CODE           = "branchAdminCode";
    public static final String PRODUCT_CODE                = "productCode";
    public static final String TEAM_NAMES                  = "teamNames";
    
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

	public String getDeletedValue() {
		return deletedValue;
	}

	public void setDeletedValue(String deletedValue) {
		this.deletedValue = deletedValue;
	}

	public String getAddedValue() {
		return addedValue;
	}

	public void setAddedValue(String addedValue) {
		this.addedValue = addedValue;
	}

}
