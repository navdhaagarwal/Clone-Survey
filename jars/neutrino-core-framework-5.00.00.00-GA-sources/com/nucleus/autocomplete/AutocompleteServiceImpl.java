package com.nucleus.autocomplete;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.util.ReflectionUtils;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.service.BaseServiceImpl;

import net.bull.javamelody.MonitoredWithSpring;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("autocompleteService")
@MonitoredWithSpring(name = "Autocomplete_Service_IMPL_")
public class AutocompleteServiceImpl extends BaseServiceImpl implements AutocompleteService {

	private static final String MASTER_LIFE_CYCLE_DATA_APPROVAL_STATUS = "masterLifeCycleData.approvalStatus IN (:approvalStatus)";
	private static final String PERSISTANCE_STATUS = "(entityLifeCycleData.persistenceStatus !="+PersistenceStatus.EMPTY_PARENT +" OR entityLifeCycleData.persistenceStatus IS NULL)" ;
	private static final String LOWER_PARAM = "lower(:";
	private static final String LOWER = "lower(";
	private static final String ITEM_VALUE_CANNOT_BE_NULL = "Item value cannot be null";
	private static final String CLASS_WITH_NAME = "Class with name ";
	private static final String CLASS_NAME_CANNOT_BE_NULL = "Class name cannot be null";
	private static final String LIKE = ") like ";
	private static final String ENTITY_LIFE_CYCLE_DATA_SNAPSHOT_RECORD = "(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) and activeFlag = true ";
	private static final String ENTITY_LIFE_CYCLE_DATA_SNAPSHOT = "(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) ";
	private static final String NOT_FOUND = " Not Found: ";
	/**
     *This function returns a list of desired values on the basis of the value of desired search criteria(itemVal).
     */

    private final int DEFAULT_PAGE_SIZE = 3;
	private static final String APPROVAL_STATUS="approvalStatus";
	private static final List<Integer> APPROVED_STATUS_LIST = Arrays.asList(ApprovalStatus.APPROVED,
			ApprovalStatus.APPROVED_MODIFIED, ApprovalStatus.APPROVED_DELETED,
			ApprovalStatus.APPROVED_DELETED_IN_PROGRESS);
    
    @Override
    public List<Map<String, ?>> searchOnFieldValue(String className, String itemVal, String[] searchColumnList,
            String value, boolean loadApprovedEntityFlag, String listOfItems, boolean strictSearchOnListOfItems) {
        NeutrinoValidator.notNull(className, CLASS_NAME_CANNOT_BE_NULL);
        NeutrinoValidator.notNull(searchColumnList, "Columns List cannot be null");
        NeutrinoValidator.notNull(itemVal, ITEM_VALUE_CANNOT_BE_NULL);
        Class entityClass = null;
        List<Map<String, ?>> finalResult = new ArrayList<Map<String, ?>>();
        // The classes must be comma separated, but the searchColList must contain column names present in both the Entities
        String[] classList = className.split(",");

        List<Long> itemsId = new ArrayList<>();

        for (String tempClass : classList) {
            entityClass = AutocompleteLoadedEntitiesMap.getClassFromMap(tempClass);
            StringBuilder sb = new StringBuilder();
            boolean isFirstClause = true;

            MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(searchColumnList).addQueryColumns(
                    itemVal);
            if (BaseMasterEntity.class.isAssignableFrom(entityClass) && loadApprovedEntityFlag) {
                    executor.addAndClause(MASTER_LIFE_CYCLE_DATA_APPROVAL_STATUS);
                    executor.addAndClause(PERSISTANCE_STATUS);
                    
                    executor.addBoundParameter(APPROVAL_STATUS, APPROVED_STATUS_LIST);
            }

            StringBuilder whereClause = new StringBuilder();
            if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
                whereClause
                        .append(ENTITY_LIFE_CYCLE_DATA_SNAPSHOT_RECORD);
            } else {
                whereClause
                        .append(ENTITY_LIFE_CYCLE_DATA_SNAPSHOT);
            }
            if (listOfItems != null) {
                itemsId = getItemListIds(listOfItems);

            }

            if (itemsId != null) {
                if (!itemsId.isEmpty()) {
                    whereClause.append(" and id IN (:itemsIds)");
                    executor.addBoundParameter("itemsIds", itemsId);
                } else if (strictSearchOnListOfItems) {
                    /* In case strict search on listOfItems is enabled and list of items is empty, return empty list. */
                    return new ArrayList<Map<String, ?>>();
                }
            }

            executor.addAndClause(whereClause.toString());

            for (String search_col : searchColumnList) {
                if (isFirstClause) {
                	sb.append("(lower(" + search_col + LIKE );
                    isFirstClause = false;
                } else {
                	sb.append(" or " + LOWER + search_col + LIKE );
                }
                sb.append( "lower(:value) ");
            }
            executor.addBoundParameter("value", "%"+value+"%");
            sb.append(")");
            executor = executor.addAndClause(sb.toString());

            String orderByClause=prepareOrderByClause(searchColumnList,entityClass);
            executor.addOrderByClause(orderByClause);

            List<Map<String, ?>> result = entityDao.executeQuery(executor);
            for (Map<String, ?> temp : result) {
                finalResult.add(temp);
            }
        }
        return finalResult;
    }
    
    @Deprecated
    @Override
    public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String[] searchColumnList,
            String value, boolean loadApprovedEntityFlag, String listOfItems, boolean strictSearchOnListOfItems, int page) {
    	
    return searchOnFieldValueByPage(className, itemVal, searchColumnList, value, loadApprovedEntityFlag, listOfItems, strictSearchOnListOfItems, page, null, null);
    }
        
	@Override
    public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String[] searchColumnList,
                String value, boolean loadApprovedEntityFlag, String listOfItems, boolean strictSearchOnListOfItems, int page, String parentIdValue, String parentCol) {
		return searchOnFieldValueByPage(className, itemVal, searchColumnList, value, loadApprovedEntityFlag, listOfItems, strictSearchOnListOfItems, page, parentIdValue, parentCol, true);
	}

	@Override
	public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String[] searchColumnList, String value,
														 boolean loadApprovedEntityFlag, String listOfItems, boolean strictSearchOnListOfItems, int page,
														 String parentIdValue, String parentCol, boolean containsSearchEnabled) {
		return  searchOnFieldValueByPage(className,itemVal,searchColumnList,value,loadApprovedEntityFlag,listOfItems,strictSearchOnListOfItems,page,parentIdValue
		,parentCol,false,false);
	}
    @Override
	public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String[] searchColumnList, String value,
			boolean loadApprovedEntityFlag, String listOfItems, boolean strictSearchOnListOfItems, int page,
			String parentIdValue, String parentCol, boolean containsSearchEnabled,boolean getRowsWithParentValueNull) {
           	
    	NeutrinoValidator.notNull(className, CLASS_NAME_CANNOT_BE_NULL);
        NeutrinoValidator.notNull(searchColumnList, "Columns List cannot be null");
        NeutrinoValidator.notNull(itemVal, ITEM_VALUE_CANNOT_BE_NULL);
        Class entityClass = null;
        List<Map<String, ?>> finalResult = new ArrayList<>();
        String[] classList = className.split(",");
        List<Long> itemsIdList = new ArrayList<>();
        int counter = 0;
        long totalRecords = 0;
        for (String tempClass : classList) {
        	entityClass = AutocompleteLoadedEntitiesMap.getClassFromMap(tempClass);
            StringBuilder sb = new StringBuilder();
            boolean isFirstClause = true;
            MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(searchColumnList).addQueryColumns(itemVal);
            if (BaseMasterEntity.class.isAssignableFrom(entityClass) && loadApprovedEntityFlag) {
                    executor.addAndClause(MASTER_LIFE_CYCLE_DATA_APPROVAL_STATUS);
                    executor.addBoundParameter(APPROVAL_STATUS, APPROVED_STATUS_LIST);
                    executor.addAndClause(PERSISTANCE_STATUS);
            }
            StringBuilder whereClause = new StringBuilder();
            if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
                whereClause.append(ENTITY_LIFE_CYCLE_DATA_SNAPSHOT_RECORD);
            } else {
                whereClause.append(ENTITY_LIFE_CYCLE_DATA_SNAPSHOT);
            }
            if (listOfItems != null) {
                itemsIdList = getItemListIds(listOfItems);

            }
            if (itemsIdList != null) {
                if (!itemsIdList.isEmpty()) {
                    whereClause.append(" and id IN (:itemsIds)");
                    executor.addBoundParameter("itemsIds", itemsIdList);
                } else if (strictSearchOnListOfItems) {
                    /* In case strict search on listOfItems is enabled and list of items is empty, return empty list. */
                    return new ArrayList<>();
                }
            }
            executor.addAndClause(whereClause.toString());
            for (String search_col : searchColumnList) {
                if (isFirstClause) {
                    sb.append(" (lower(" + search_col + LIKE + LOWER_PARAM +getSearchParam(search_col)+ " )");
                    isFirstClause = false;
                } else {
                    sb.append(" or " + LOWER + search_col + LIKE + LOWER_PARAM +getSearchParam(search_col)+ " )");
                }
            }
            sb.append(")");
            if (checkParentDependantValues(parentIdValue,parentCol)) {
            	if(getRowsWithParentValueNull){
					sb.append(addParentDependantWithNull(parentCol));
				}else {
					sb.append(addParentDependant(parentCol));
				}
            }
            executor = executor.addAndClause(sb.toString());
            String orderByClause = prepareOrderByClause(searchColumnList,entityClass);
            executor.addOrderByClause(orderByClause);
            addSearchParamValue(executor,searchColumnList,value, parentIdValue, parentCol, containsSearchEnabled);
            List<Map<String, ?>> result = entityDao.executeQuery(executor, page * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);
            for (Map<String, ?> temp : result) {
                finalResult.add(counter, temp);
                counter++;
            }
            totalRecords = totalRecords + entityDao.executeTotalRowsQuery(executor);
        }
        Map<String, Long> sizeMap = new HashMap<>();
        sizeMap.put("size", totalRecords);
        finalResult.add(counter, sizeMap);
        if (finalResult != null) {
            BaseLoggers.flowLogger.debug("size of finalResult :", finalResult.size());
        }
        return finalResult;
	}

	private void addSearchParamValue(MapQueryExecutor executor, String[] searchColumnList, String value, String parent, String parentCol, boolean containsSearchEnabled) {
		if (searchColumnList==null || searchColumnList.length==0) {
			return ;
		}
        for (String search_col : searchColumnList) {
        	if (containsSearchEnabled) {
            	executor.addBoundParameter(getSearchParam(search_col), "%" + value + "%");
        	} else {
            	executor.addBoundParameter(getSearchParam(search_col), value + "%");
        	}
        }
        if (checkParentDependantValues(parent,parentCol)) {
        	executor.addBoundParameter(getSearchParam(parentCol), parent);
        }
	}

	private String getSearchParam(String searchParam) {
		return searchParam.replace('.', '_');
	}
	
	private boolean checkParentDependantValues(String parent, String parentCol) {
		return parent!=null && !parent.isEmpty() && parentCol!=null && !parentCol.isEmpty();
	}
	
	private String addParentDependant(String parentCol) {
		return "and lower(" + parentCol + ") = " + LOWER_PARAM + getSearchParam(parentCol) + " )";
	}
	private String addParentDependantWithNull(String parentCol) {
		return "and (lower(" + parentCol + ") = " + LOWER_PARAM + getSearchParam(parentCol) + " ) or " + parentCol + " is null )";
	}

    /**
     * @description This function is used to search all the postal codes for the country ISO code selected from the drop down list of
     * the country either by JSP or by manual selection
     */

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<Map<String, ?>> getZipCodesForCountrySelected(String className, String itemVal, String search_col,
            String value, boolean flag, String code) {
        Class entityClass = null;
        NeutrinoValidator.notNull(className, CLASS_NAME_CANNOT_BE_NULL);
        NeutrinoValidator.notNull(search_col, "Column name cannot be null");
        NeutrinoValidator.notNull(itemVal, ITEM_VALUE_CANNOT_BE_NULL);
        entityClass = AutocompleteLoadedEntitiesMap.getClassFromMap(className);
        StringBuilder sb = new StringBuilder();
        MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(search_col).addQueryColumns(itemVal);
        if (flag) {
            executor.addAndClause("approvalStatus = 0");
        }

        sb.append("(" + search_col + " like :value");
        sb.append(")");
        executor.addBoundParameter("value", value+"%");
        executor = executor.addAndClause(sb.toString());
        executor = executor.addAndClause("countryCode = :code");
        executor.addBoundParameter("code", code);
        // For the handling of bulky data rendered from geographic database we
        // are currently restricting the result from 0 to maximum 70 results
        return entityDao.executeQuery(executor, 0, 70);
    }

    @Override
    public String getAutoCompleteValue(Long id, String className, String columnName) {
        NeutrinoValidator.notNull(id, "Id cannot be null");
        NeutrinoValidator.notNull(className, CLASS_NAME_CANNOT_BE_NULL);
        NeutrinoValidator.notNull(columnName, "Column Name cannot be null");
        Class entityClass = null;
        String returnval="";
        entityClass = AutocompleteLoadedEntitiesMap.getClassFromMap(className);
        MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(columnName);
        executor = executor.addAndClause("id =:id");
        executor.addBoundParameter("id", id);
        Map<String, String> dataValue = (Map<String, String>) entityDao.executeQueryForSingleValue(executor);
        if (dataValue != null && dataValue.get(columnName) !=null && dataValue.size() > 0) {
        	returnval=dataValue.get(columnName);
        }
        return returnval;
    }

    private List<Long> getItemListIds(String listItems) {

        String iList = new String(listItems);
        iList = iList.substring(1, iList.length() - 1);
        List<Long> listOfIds = new ArrayList<>();
        if (StringUtils.isNoneEmpty(iList)) {
            String[] list = iList.split(",");
            for (int i = 0 ; i < list.length ; i++) {
                String[] subList = list[i].split(":");
                listOfIds.add(Long.parseLong(subList[1]));

            }
        }
        return listOfIds;

    }


	private String prepareOrderByClause(String[] searchColumnList,
			Class entityClass) {
		StringBuilder orderByClause = new StringBuilder();
		orderByClause.append("order by ");
		for (int i = 0; i < searchColumnList.length; i++) {
			Class endColumnType = entityClass;
			String[] sortColumns = searchColumnList[i].split("\\.");
			for (String sortColumn : sortColumns) {
				Field sortableField = ReflectionUtils.findField(endColumnType,
						sortColumn);
				if (ValidatorUtils.isNull(sortableField)) {
					break;
				} else {
					endColumnType = sortableField.getType();
				}

			}

			if (checkIfFieldIsStringOrCharType(endColumnType)) {
				prepareOrderByClauseIfFieldIsStringOrCharType(orderByClause,
						searchColumnList[i], i, searchColumnList.length);
			} else {
				if (searchColumnList.length > 1
						&& i != searchColumnList.length - 1) {
					orderByClause.append(searchColumnList[i] + " , ");
				} else {
					orderByClause.append(searchColumnList[i]);
				}
			}
		}
		return orderByClause.toString();
	}

	private void prepareOrderByClauseIfFieldIsStringOrCharType(
			StringBuilder orderByClause, String searchColumn, int arrayIndex, int searchColArrayLength) {
		if(searchColArrayLength>1 && arrayIndex!=searchColArrayLength-1){				
			orderByClause.append(LOWER+searchColumn+") , ");
		}else{	        		
			orderByClause.append(LOWER+searchColumn+")");
		}
		
	}

	private Boolean checkIfFieldIsStringOrCharType(Class type) {
		if(ValidatorUtils.notNull(type) && (type.equals(String.class) || type.equals(Character.class))){
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	@Override
	public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemVal, String itemLabel, String searchColumn, 
			String inputValue, boolean loadApprovedEntityFlag, String itemsToBeExcluded, int page, int pageSize) {
		return searchOnFieldValueByPage(className, itemVal, itemLabel, searchColumn, inputValue, loadApprovedEntityFlag, itemsToBeExcluded, page, pageSize, false);
	}
	
	private Class getFieldClass(String fieldName,Class ownerClass)
	{
		BeanWrapper ownerBeanWrapper = new BeanWrapperImpl(ownerClass);
		ownerBeanWrapper.setAutoGrowNestedPaths(true);
		try{
			return ownerBeanWrapper.getPropertyType(fieldName);
		}catch (BeansException e) {
			return null;
		}
	}

	private void appendClauseForExcludedItems(String itemVal, String itemsToBeExcluded, StringBuilder whereClause,MapQueryExecutor executor, Class entityClass) {
		if (itemsToBeExcluded != null) {
			List<Object> itemListToBeExcluded = new ArrayList<>();
			List<String> rawItemList = getRawItemList(itemsToBeExcluded);

			Class fieldClass = getFieldClass(itemVal, entityClass);
			if (fieldClass == null){
				fieldClass = String.class;
			}

			if (!String.class.isAssignableFrom(fieldClass)) {
				for (String rawItem : rawItemList) {
					if (Long.class.isAssignableFrom(fieldClass)) {
						itemListToBeExcluded.add(Long.valueOf(rawItem));
					}// TODO add more cases for more possible types
					else{
						itemListToBeExcluded.add(rawItem);
					}
					
				}
			} else {
				itemListToBeExcluded.addAll(rawItemList);
			}

			if (ValidatorUtils.hasElements(itemListToBeExcluded)) {
				whereClause.append(" and " + itemVal + " NOT IN (:itemListToBeExcluded)");
				executor.addBoundParameter("itemListToBeExcluded", itemListToBeExcluded);
			}
		}
	}

	private List<Map<String, Object>> updateMapKeys(String itemVal, String itemLabel, String searchColumn,
			List<Map<String, ?>> result) {
		List<Map<String, Object>> list = new ArrayList<>();
		if (itemLabel.contains(".") || searchColumn.contains(".")) {
			for (Map<String, ?> map : result) {
				Map<String, Object> newMap = new HashMap<>();
				newMap.put(itemLabel, map.get(itemLabel.replace(".", "")));
				newMap.put(itemVal, map.get(itemVal));
				newMap.put(searchColumn, map.get(searchColumn.replace(".", "")));
				list.add(newMap);
			}
		}
		return list;
	}

	@SuppressWarnings("rawtypes")
	private void appendClauseForEntityLifeCycleData(Class entityClass, StringBuilder whereClause) {
		if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
			whereClause.append(ENTITY_LIFE_CYCLE_DATA_SNAPSHOT_RECORD);
		} else {
			whereClause.append(ENTITY_LIFE_CYCLE_DATA_SNAPSHOT);
		}
	}

	private List<String> getRawItemList(String listItems) {
		List<String> rawList = new ArrayList<>();
		if (StringUtils.isNotEmpty(listItems)) {
			if (listItems.contains(",")) {
				String[] list = listItems.split(",");
				rawList.addAll(Arrays.asList(list));
			} else {
				rawList.add(listItems);
			}
		}
		return rawList;

	}
	
	@SuppressWarnings("rawtypes")
	private Class getEntityClass(Class entityClass, String tempClass) {
		return AutocompleteLoadedEntitiesMap.getClassFromMap(tempClass);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Map<String, ?>> searchOnFieldValueByPage(String className, String itemValue, String itemLabel,
			String searchColumn, String inputValue, boolean loadApprovedEntityFlag, String itemsToBeExcluded, int page, int pageSize,
			Boolean containsSearchEnabled) {
		NeutrinoValidator.notNull(className, CLASS_NAME_CANNOT_BE_NULL);
		Class entityClass = null;
		List<Map<String, ?>> itemsList = new ArrayList<Map<String, ?>>();
		String[] classList = className.split(",");
		int counter = 0;
		long totalRecords = 0;
		for (String tempClass : classList) {
			entityClass = getEntityClass(entityClass, tempClass);
			StringBuilder sb = new StringBuilder();
			MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(searchColumn, itemValue,
					itemLabel);
			if (BaseMasterEntity.class.isAssignableFrom(entityClass) && loadApprovedEntityFlag) {
                executor.addAndClause(MASTER_LIFE_CYCLE_DATA_APPROVAL_STATUS);
                executor.addBoundParameter(APPROVAL_STATUS, APPROVED_STATUS_LIST);
                executor.addAndClause(PERSISTANCE_STATUS);
			}
			StringBuilder whereClause = new StringBuilder();
			appendClauseForEntityLifeCycleData(entityClass, whereClause);
			// Exclude given Items
			appendClauseForExcludedItems(itemValue, itemsToBeExcluded, whereClause,executor,entityClass);
			executor.addAndClause(whereClause.toString());
			StringBuilder searchColumnSb = new StringBuilder();
			searchColumnSb.append(LOWER).append(searchColumn).append(LIKE + LOWER_PARAM)
					.append(getSearchParam(searchColumn)).append(" )");
			sb.append(searchColumnSb);
			sb.append(")");
			executor = executor.addAndClause(sb.toString());
			if (containsSearchEnabled) {
				executor.addBoundParameter(getSearchParam(searchColumn), "%" + inputValue + "%");
			} else {
				executor.addBoundParameter(getSearchParam(searchColumn), inputValue + "%");
			}
			List<Map<String, ?>> result = entityDao.executeQuery(executor, (page - 1) * pageSize, pageSize);
			// In case if there is a Nested Property
			List<Map<String, Object>> list = updateMapKeys(itemValue, itemLabel, searchColumn, result);
			if (ValidatorUtils.hasNoElements(list)) {
				for (Map<String, ?> map : result) {
					itemsList.add(counter, map);
					counter++;
				}
			} else {
				for (Map<String, Object> map : list) {
					itemsList.add(counter, map);
					counter++;
				}
			}
			totalRecords = totalRecords + entityDao.executeTotalRowsQuery(executor);
		}
		Map<String, Long> sizeMap = new HashMap<String, Long>();
		sizeMap.put("size", totalRecords);
		itemsList.add(counter, sizeMap);
		if (itemsList != null) {
			BaseLoggers.flowLogger.debug("size of finalResult :", itemsList.size());
		}
		return itemsList;
	}
}
