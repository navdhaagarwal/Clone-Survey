package com.nucleus.autocomplete;

import java.util.List;
import java.util.Map;

public interface AutocompleteService {

    /**
     * This function returns a list of desired columns on the basis of the value of desired column(itemVal).
     *
     * @param className the class name
     * @param itemVal the item val
     * @param searchColumnList the search column list
     * @param value the value
     * @param flag the flag
     * @param listOfItems: list of items on which search is to run
     * @param strictSearchOnListOfItems : It defines whether the search on list of items is strict or not i.e. if list of items is empty and this variable is true, it returns an empty list.
     * @return the list
     */
    public List<Map<String, ?>> searchOnFieldValue(String className, String itemVal, String[] searchColumnList,
            String value, boolean flag, String listOfItems, boolean strictSearchOnListOfItems);

    /**
     * This function is used to search all the postal codes for the country ISO code selected from the drop down list of
     * the country either by JSP or by manual selection.
     *
     * @param className the class name
     * @param itemVal the item val
     * @param searchColumnList the search column list
     * @param value the value
     * @param flag the flag
     * @param code the code
     * @return the zip codes for country selected
     */
    public List<Map<String, ?>> getZipCodesForCountrySelected(String className, String itemVal, String searchColumnList,
            String value, boolean flag, String code);

    public String getAutoCompleteValue(Long id, String className, String columnName);

	List<Map<String, ?>> searchOnFieldValueByPage(String className,
			String itemVal, String[] searchColumnList, String value,
			boolean loadApprovedEntityFlag, String listOfItems,
			boolean strictSearchOnListOfItems, int page);
	
	List<Map<String, ?>> searchOnFieldValueByPage(String className,
			String itemVal, String[] searchColumnList, String value,
			boolean loadApprovedEntityFlag, String listOfItems,
			boolean strictSearchOnListOfItems, int page, String parentIdValue, String parentCol);

	 List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String itemLabel, String searchCol,
			 String inputValue, boolean loadApprovedEntityFlag, String itemsToBeExcluded, int page, int pageSize);

	List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String[] searchColumnList,
			String value, boolean loadApprovedEntityFlag, String listOfItems, boolean strictSearchOnListOfItems,
			int page, String parentIdValue, String parentCol, boolean containsSearchEnabled);

	public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemValue, String itemLabel,
			String searchColumn, String inputValue, boolean b, String itemToBeExcluded, int page, int pageSize,
			Boolean containsSearchEnabled);
	public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String[] searchColumnList, String value,
														 boolean loadApprovedEntityFlag, String listOfItems, boolean strictSearchOnListOfItems, int page,
														 String parentIdValue, String parentCol, boolean containsSearchEnabled,boolean getRowsWithParentValueNull);

}
