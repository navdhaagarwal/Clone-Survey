package com.nucleus.rules.service;

import com.nucleus.core.genericparameter.service.GenericParameterService;
import com.nucleus.core.loanproduct.ProductType;
import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.core.workflowconfig.entity.ProcessingStageType;
import com.nucleus.dao.query.MapQueryExecutor;
import com.nucleus.dao.query.NamedQueryExecutor;
import com.nucleus.entity.ApprovalStatus;
import com.nucleus.entity.PersistenceStatus;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.makerchecker.MakerCheckerService;
import com.nucleus.master.BaseMasterEntity;
import com.nucleus.master.BaseMasterService;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.assignmentmatrix.service.AssignmentMatrixService;
import com.nucleus.rules.datatable.DataTableJsonHelper;
import com.nucleus.rules.model.*;
import com.nucleus.rules.model.assignmentMatrix.*;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixGridData;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixMaster;
import com.nucleus.rules.model.ruleMatrixMaster.RuleMatrixTypeOgnlMapping;
import com.nucleus.rules.model.ruleMatrixMaster.pojo.AssignmentSetVO;
import com.nucleus.rules.model.ruleMatrixMaster.pojo.RuleMatrixColumnData;
import com.nucleus.rules.model.ruleMatrixMaster.pojo.RuleMatrixRowData;
import com.nucleus.rules.model.ruleMatrixMaster.pojo.RuleMatrixTableData;
import com.nucleus.user.User;
import com.nucleus.user.UserInfo;
import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.hibernate.Hibernate;
import org.springframework.context.ApplicationContext;
import org.springframework.ui.ModelMap;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.BindingResult;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


import static com.nucleus.logging.BaseLoggers.flowLogger;
import static java.util.Comparator.comparing;

@Named("ruleMatrixMasterService")
public class RuleMatrixMasterServiceImpl implements RuleMatrixMasterService {

    @Inject
    @Named("ruleService")
    private RuleService                ruleService;

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("baseMasterService")
    private BaseMasterService baseMasterService;

    @Inject
    @Named("genericParameterService")
    protected GenericParameterService genericParameterService;

    @Inject
    @Named(value = "makerCheckerService")
    private MakerCheckerService makerCheckerService;

    @Inject
    @Named(value = "assignmentMatrixService")
    private AssignmentMatrixService assignmentMatrixService;




    @Override
    public AssignmentSet convertToJsonMap(AssignmentSet assignmentSet) {
        for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentSet.getAssignmentMatrixRowData()) {

            AssignmentMatrixAction assignmentMatrixAction = assignmentMatrixRowData.getAssignmentMatrixAction();
            if (assignmentSet instanceof AssignmentGrid) {

                String mapValues = assignmentMatrixRowData.getRowMapValues();
                if (null != mapValues) {
                    Map<Object, Object> linkedMap = (Map<Object, Object>) new JSONDeserializer().deserialize(mapValues);

                    Iterator<Map.Entry<Object, Object>> entries = linkedMap.entrySet().iterator();
                    while (entries.hasNext()) {
                        Map.Entry<Object, Object> entry = entries.next();
                        if (null != entry.getValue()) {
                            String[] valueTokenSring = ((String) entry.getValue())
                                    .split(AssignmentConstants.MULTI_VALUE_SEPARATOR);
                            if (valueTokenSring.length > 1) {
                                boolean isReferenceType = false;
                                for (AssignmentFieldMetaData assignmentFieldMetaData : ((AssignmentGrid) assignmentSet)
                                        .getAssignmentFieldMetaDataList()) {

                                    if (assignmentFieldMetaData.getIndexId().equals(entry.getKey())) {

                                        if (assignmentFieldMetaData.getDataType() == ParameterDataType.PARAMETER_DATA_TYPE_REFERENCE
                                                && (assignmentFieldMetaData.getParameterBased() == null || !assignmentFieldMetaData
                                                .getParameterBased()))
                                            isReferenceType = true;
                                    } else {

                                    }
                                }
                                if (isReferenceType) {

                                    entry.setValue(valueTokenSring);
                                } else {
                                    entry.setValue(((String) entry.getValue()).replace(
                                            AssignmentConstants.MULTI_VALUE_SEPARATOR, ","));
                                }

                            }
                        }
                    }
                    assignmentMatrixRowData.setLinkedMap(new LinkedHashMap<Object, Object>(linkedMap));
                }
            }

            String assignValues = assignmentMatrixAction.getAssignActionValues();
            Map<Object, Object> assignMap = (Map<Object, Object>) new JSONDeserializer().deserialize(assignValues);
            assignmentMatrixAction.setJsonAssignActionMap(new LinkedHashMap<Object, Object>(assignMap));

        }
        return assignmentSet;
    }

    @Override
    public void processAssignmentSet(AssignmentSet assignmentSet) {
        if (assignmentSet.getAssignmentMatrixRowData() != null && !assignmentSet.getAssignmentMatrixRowData().isEmpty()) {

            for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentSet.getAssignmentMatrixRowData()) {
                JSONSerializer serializer = new JSONSerializer();
                if (assignmentSet instanceof AssignmentGrid) {
                    Map<Object, Object> linkedMap = assignmentMatrixRowData.getLinkedMap();
                    String newStringVal = "";
                    if (linkedMap != null) {
                        Iterator<Map.Entry<Object, Object>> entries = linkedMap.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry<Object, Object> entry = entries.next();
                            Object val = entry.getValue();
                            if (null != val && val instanceof String[]) {
                                String[] vals = (String[]) val;
                                newStringVal = "";
                                StringBuilder builder = new StringBuilder();
                                for (String s : vals) {
                                    if (s != null && !s.isEmpty()) {
                                        builder.append(s).append(AssignmentConstants.MULTI_VALUE_SEPARATOR);
                                    }
                                }
                                newStringVal = builder.toString();
                                if (newStringVal.length() == 0) {
                                    entry.setValue(null);
                                } else {
                                    newStringVal = newStringVal.substring(0, newStringVal.length()
                                            - AssignmentConstants.MULTI_VALUE_SEPARATOR.length());
                                    entry.setValue(newStringVal);
                                }

                            } else {
                                if (null != val && !"".equals(val)) {
                                    val = ((String) val).replace(",", AssignmentConstants.MULTI_VALUE_SEPARATOR);
                                } else {
                                    val = null;
                                }
                                entry.setValue(val);
                            }
                        }
                    }
                    String mapValues = serializer.deepSerialize(linkedMap);
                    assignmentMatrixRowData.setRowMapValues(mapValues);

                }

                Map<Object, Object> assignMap = assignmentMatrixRowData.getAssignmentMatrixAction().getJsonAssignActionMap();
                /*  checkTeamUserValidation(assignMap);*/
                String assignValues = serializer.deepSerialize(assignMap);
                assignmentMatrixRowData.getAssignmentMatrixAction().setAssignActionValues(assignValues);
                assignmentMatrixRowData.getAssignmentMatrixAction().setPersistenceStatus(50);

            }
        }
    }

    @Override
    public List<Map<String, Object>> getParametersBasedOnOperators(String operandName, String operator, Long moduleId, String sourceProduct) {
        List<Map<String, Object>> parameterList = null;
        List<Integer> dataTypes = new ArrayList<>();

        if ((null != operandName && !operandName.equals(""))
                && (null != operator && !operator.equals("") && !operator.equals("IN") && !operator.equals("NOT_IN") && !operator
                .equals("BETWEEN"))) {
            ObjectGraphTypes objectGraphTypes = ruleService.getObjectGraphTypesWithDisplayName(operandName);
            ParameterDataType parameterDataType = objectGraphTypes.getDataType();
            Map<Integer, List<Integer>> supportedDataTypeMap = ExpressionValidationConstants.operatorsDataTypeMap
                    .get(operator);

            if (null != supportedDataTypeMap) {
                dataTypes = supportedDataTypeMap.get(Integer.valueOf(parameterDataType.getCode()));
            }

        } else if (((null != operandName && !operandName.equals(""))
                && (null == operator || operator.equals("IN") || operator.equals("NOT_IN") || operator.equals("BETWEEN")))) {
            ObjectGraphTypes objectGraphTypes = ruleService.getObjectGraphTypesWithDisplayName(operandName);
            ParameterDataType parameterDataType = objectGraphTypes.getDataType();
            dataTypes.add(Integer.parseInt(parameterDataType.getCode()));
        }
        else {
            dataTypes = ExpressionValidationConstants.CONDITION_SUPPORTED_DATATYPES;
        }
        if (null != dataTypes && dataTypes.size() > 0) {

            if (null != moduleId) {
                parameterList = ruleService.getApprovedParametersbyDataTypes(sourceProduct, dataTypes, moduleId);

            } else {
                parameterList = ruleService.getApprovedParametersbyDataTypes(sourceProduct, dataTypes);

            }

        }
        return parameterList;
    }

    @Override
    public List<Map<String, ?>> searchAutoCompleteValues(String className, String itemVal, String[] searchColumnList,
                                                         String value, Boolean loadApprovedEntityFlag, String itemsList,
                                                         Boolean strictSearchOnitemsList, int page, String whereCondition,
                                                         Map<String, Object>paramMap) {
        NeutrinoValidator.notNull(className, "Class name cannot be null");
        NeutrinoValidator.notNull(searchColumnList, "Columns List cannot be null");
        NeutrinoValidator.notNull(itemVal, "Item value cannot be null");
        Class entityClass = null;
        int counter = 0;
        long totalRecords = 0;
        try {
            entityClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            flowLogger.error(e.toString());
        }
        List<Map<String, ?>> finalResult = new ArrayList<>();
        List<Long> itemsId = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean isFirstClause = true;
        MapQueryExecutor executor = new MapQueryExecutor(entityClass).addQueryColumns(searchColumnList).addQueryColumns(itemVal);
        if (BaseMasterEntity.class.isAssignableFrom(entityClass) && loadApprovedEntityFlag) {
            executor.addAndClause("masterLifeCycleData.approvalStatus IN (:approvalStatus)");
            executor.addAndClause("entityLifeCycleData.persistenceStatus !="+ PersistenceStatus.EMPTY_PARENT);
            executor.addBoundParameter("approvalStatus", ApprovalStatus.APPROVED_RECORD_STATUS_LIST);
        }

        StringBuilder whereClause = new StringBuilder();
        if (BaseMasterEntity.class.isAssignableFrom(entityClass)) {
            whereClause
                    .append("(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) and activeFlag = true ");
        } else {
            whereClause
                    .append("(entityLifeCycleData.snapshotRecord IS NULL OR entityLifeCycleData.snapshotRecord = false) ");
        }
        whereClause.append(whereCondition);
        for(Map.Entry<String, Object> entry : paramMap.entrySet()){
            executor.addBoundParameter(entry.getKey(), entry.getValue());
        }
        if (itemsList != null) {
            itemsId = getItemListIds(itemsList);

        }
        if (itemsId != null) {
            if (!itemsId.isEmpty()) {
                whereClause.append(" and id IN (:itemsIds)");
                executor.addBoundParameter("itemsIds", itemsId);
            } else if (strictSearchOnitemsList) {
                /* In case strict search on listOfItems is enabled and list of items is empty, return empty list. */
                return new ArrayList<>();
            }
        }

        executor.addAndClause(whereClause.toString());

        for (String search_col : searchColumnList) {
            if (isFirstClause) {
                sb.append("(lower(" + search_col + ") like " );
                isFirstClause = false;
            } else {
                sb.append(" or " + "lower(" + search_col + ") like " );
            }
            sb.append( "lower(:value) ");
        }
        executor.addBoundParameter("value", "%"+value+"%");
        sb.append(")");
        executor = executor.addAndClause(sb.toString());
        String orderByClause=prepareOrderByClause(searchColumnList,entityClass);
        executor.addOrderByClause(orderByClause);
        List<Map<String, ?>> result = entityDao.executeQuery(executor,page * 3, 3);
        for (Map<String, ?> temp : result) {
            finalResult.add(counter, temp);
            counter++;
        }
        totalRecords = totalRecords + entityDao.executeTotalRowsQuery(executor);
        Map<String, Long> sizeMap = new HashMap<>();
        sizeMap.put("size", totalRecords);
        finalResult.add(counter, sizeMap);
        if (finalResult != null) {
            flowLogger.debug("size of finalResult :{}", finalResult.size());
        }
        return finalResult;
    }



    @Override
    public String prepareAutoComplete(ModelMap map, String i_label, String idCurr,
                                      String content_id, int page, List<Map<String, ?>> list) {
        if(CollectionUtils.isNotEmpty(list)) {
            Map listMap = list.get(list.size() - 1);
            int sizeList1 = Integer.parseInt(listMap.get("size").toString());
            list.remove(list.size() - 1);
            map.put("size", sizeList1);
            map.put("page", page);
        }
        if(i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }
        map.put("data", list);
        if(idCurr != null && idCurr.trim().length() > 0) {
            idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }
        map.put("idCurr", idCurr);
        map.put("i_label", i_label);
        map.put("content_id", content_id);
        return "autocomplete";
    }

    private List<Long> getItemListIds(String listItems) {
        listItems = listItems.substring(1, listItems.length() - 1);
        List<Long> listOfIds = new ArrayList<>();
        if (org.apache.commons.lang3.StringUtils.isNoneEmpty(listItems)) {
            String[] list = listItems.split(",");
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
            orderByClause.append("lower(" +searchColumn+") , ");
        }else{
            orderByClause.append("lower(" +searchColumn+")");
        }

    }

    private Boolean checkIfFieldIsStringOrCharType(Class type) {
        if(ValidatorUtils.notNull(type) && (type.equals(String.class) || type.equals(Character.class))){
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public String getDataTypeForObjectGraphType(Long id) {
        NamedQueryExecutor<String> queryExecutor = new NamedQueryExecutor<String>("RuleMatrix.getDataTypeForObjectGraphType").addParameter("id", id);
        return entityDao.executeQueryForSingleValue(queryExecutor);
    }

    @Override
    public void populateMatrix(RuleMatrixTableData ruleMatrixTableData, RuleMatrixMaster ruleMatrixMaster, ModelMap map,Boolean editViewModeOfAssignment) {
        if ( (dataInRuleMatrixTableNull(ruleMatrixTableData) ||  ifThenDataModified(ruleMatrixTableData,ruleMatrixMaster)) && !editViewModeOfAssignment){
            ruleMatrixTableData.setModuleNameTableData(ruleMatrixMaster.getModuleName());
            ruleMatrixTableData.setSourceProductTableData(ruleMatrixMaster.getSourceProduct());
            ruleMatrixTableData.setIfTableGridData(ruleMatrixMaster.getIfGridData());
            ruleMatrixTableData.setThenTableGridData(ruleMatrixMaster.getThenGridData());
            ruleMatrixTableData.setRuleMatrixRowDataList(new ArrayList<>());
            if(ruleMatrixTableData.getAssignmentSet() == null) {
                ruleMatrixTableData.setAssignmentSet(new AssignmentSetVO());
            }

        }
        if(ruleMatrixTableData!=null && ruleMatrixTableData.getAssignmentSet()!=null && ruleMatrixTableData.getAssignmentSet().getAssignmentSetRule()!=null && ruleMatrixTableData.getAssignmentSet().getAssignmentSetRule().getId()==null)
            ruleMatrixTableData.getAssignmentSet().setAssignmentSetRule(null);

    }

    private boolean ifThenDataModified(RuleMatrixTableData ruleMatrixTableData, RuleMatrixMaster ruleMatrixMaster){

        boolean isDataModified = false;
        if(!compareStringsWithNullCheck(ruleMatrixTableData.getSourceProductTableData(),ruleMatrixMaster.getSourceProduct())){
            isDataModified = true;
        }
        if(Objects.nonNull(ruleMatrixTableData.getModuleNameTableData()) && Objects.nonNull(ruleMatrixMaster.getModuleName()) && !isDataModified){
            if(ruleMatrixTableData.getModuleNameTableData().getId().compareTo(ruleMatrixMaster.getModuleName().getId()) !=0){
                isDataModified = true;
            }
        }
        if(!isDataModified && CollectionUtils.isNotEmpty(ruleMatrixTableData.getIfTableGridData()) && CollectionUtils.isNotEmpty(ruleMatrixMaster.getIfGridData())){
            if(ruleMatrixTableData.getIfTableGridData().size() != ruleMatrixMaster.getIfGridData().size()){
                isDataModified = true;
            }else{
                for(int i =0; i<ruleMatrixTableData.getIfTableGridData().size();i++){
                    RuleMatrixGridData tableData = ruleMatrixTableData.getIfTableGridData().get(i);
                    RuleMatrixGridData assignmentData = ruleMatrixMaster.getIfGridData().get(i);
                    if(!assignmentData.isGridDataSame(tableData)){
                        return true;
                    }
                }
            }
        }
        if(!isDataModified && CollectionUtils.isNotEmpty(ruleMatrixTableData.getThenTableGridData()) && CollectionUtils.isNotEmpty(ruleMatrixMaster.getThenGridData())){
            if(ruleMatrixTableData.getThenTableGridData().size() != ruleMatrixMaster.getThenGridData().size()){
                isDataModified = true;
            }else{
                for(int i =0; i<ruleMatrixTableData.getThenTableGridData().size();i++){
                    RuleMatrixGridData tableData = ruleMatrixTableData.getThenTableGridData().get(i);
                    RuleMatrixGridData assignmentData = ruleMatrixMaster.getThenGridData().get(i);
                    if(!assignmentData.isGridDataSame(tableData)){
                        return true;
                    }
                }
            }
        }
        return isDataModified;
    }

    private boolean dataInRuleMatrixTableNull(RuleMatrixTableData ruleMatrixTableData){
        if(Objects.isNull(ruleMatrixTableData.getModuleNameTableData())
                || Objects.isNull(ruleMatrixTableData.getSourceProductTableData())
                || Objects.isNull(ruleMatrixTableData.getIfTableGridData())
                || Objects.isNull(ruleMatrixTableData.getThenTableGridData())
                || Objects.isNull(ruleMatrixTableData.getAssignmentSet())){
            return true;
        }
        return false;
    }

    private boolean compareStringsWithNullCheck(String str1, String str2) {
        return (str1 == null ? str2 == null : str1.equals(str2));
    }

    @Override
    public String loadAssignmentGridHeaderJson(RuleMatrixTableData ruleMatrixTableData) {
        List<List<Object>> headData = new ArrayList<>();
        DataTableJsonHelper jsonHelper = new DataTableJsonHelper();
        List<Object> rowData = new ArrayList<>();
        prepareHeadJson(rowData, ruleMatrixTableData);
        headData.add(rowData);
        jsonHelper.setAaData(headData);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        flowLogger.info("String is"+jsonString);
        return jsonString;
    }

    private void prepareHeadJson(List<Object> rowData, RuleMatrixTableData ruleMatrixTableData) {
        rowData.add("Priority");
        List<RuleMatrixGridData> ifGridData= ruleMatrixTableData.getIfTableGridData();
        if(CollectionUtils.isEmpty(ifGridData)){
            ifGridData = new ArrayList<>();
        }
        List<RuleMatrixGridData> thenGridData= ruleMatrixTableData.getThenTableGridData();
        if(CollectionUtils.isEmpty(thenGridData)){
            thenGridData = new ArrayList<>();
        }
        for (RuleMatrixGridData ifGridItem : ifGridData) {
            if(Objects.isNull(ifGridItem.getObjectGraphType())){
                rowData.add("");
            } else if("Rule".equalsIgnoreCase(ifGridItem.getRuleOgnlType())){
                Long id = ifGridItem.getObjectGraphType().getId();
                Rule rule= entityDao.find(Rule.class, id);
                if(rule!= null) {
                    rowData.add(rule.getDisplayName());
                } else{
                    rowData.add("");
                }
            }else{
                Long id = ifGridItem.getObjectGraphType().getId();
                ObjectGraphTypes objectGraphTypes = entityDao.find(ObjectGraphTypes.class, id);
                if(objectGraphTypes != null) {
                    rowData.add(objectGraphTypes.getDisplayName() + "  " + ifGridItem.getOperator());
                } else{
                    rowData.add("");
                }
            }
        }
        for (RuleMatrixGridData thenGridItem : thenGridData) {
            if(Objects.isNull(thenGridItem.getObjectGraphType())){
                rowData.add("");
            } else {
                Long id = thenGridItem.getObjectGraphType().getId();
                ObjectGraphTypes objectGraphTypes=null;
                RuleMatrixTypeOgnlMapping ruleMatrixTypeOgnlMapping= entityDao.find(RuleMatrixTypeOgnlMapping.class,id);
                if(ruleMatrixTypeOgnlMapping!=null){
                   objectGraphTypes = ruleMatrixTypeOgnlMapping.getObjectGraphType();
                }else{
                    objectGraphTypes = entityDao.find(ObjectGraphTypes.class, id);
                }

                //ObjectGraphTypes objectGraphTypes = entityDao.find(ObjectGraphTypes.class, id);
                if(objectGraphTypes != null) {
                    rowData.add(objectGraphTypes.getDisplayName()+"####");
                } else{
                    rowData.add("");
                }
            }
        }
        rowData.add("Actions");
    }

    @Override
    public String populateRowData(RuleMatrixTableData ruleMatrixTableData, String mode, Integer index, ModelMap map) {
        RuleMatrixRowData ruleMatrixRowData = null;
        Map<String, FormConfigEntityDataVO> onglBinderMap = new HashMap<>();
        Map<String, ObjectGraphTypes> onglMap = new HashMap<>();
        if(mode.equalsIgnoreCase("edit")){
            if(ruleMatrixTableData != null &&
                    CollectionUtils.isNotEmpty(ruleMatrixTableData.getRuleMatrixRowDataList())){
                for(RuleMatrixGridData ruleMatrixGridData : ruleMatrixTableData.getIfTableGridData()) {
                    if (Objects.isNull(ruleMatrixGridData.getObjectGraphType())) {
                        continue;
                    }
                    if("Rule".equalsIgnoreCase(ruleMatrixGridData.getRuleOgnlType())){
                        //do Nothing
                    }else {
                        ObjectGraphTypes objectGraphType = baseMasterService.findById(ObjectGraphTypes.class, ruleMatrixGridData.getObjectGraphType().getId());
                        if(null != objectGraphType.getDataType() && "6".equalsIgnoreCase(objectGraphType.getDataType().getCode())) {
                            onglMap.put(objectGraphType.getDisplayName(), objectGraphType);
                            onglBinderMap.put(objectGraphType.getDisplayName(), assignmentMatrixService.getBinderNameForReferenceOgnl(objectGraphType.getObjectGraph()));
                        }
                    }
                }
                for(RuleMatrixRowData ruleMatrixRowData1 : ruleMatrixTableData.getRuleMatrixRowDataList()){
                    if(ruleMatrixRowData1.getIndex().equals(index)){
                        ruleMatrixRowData = ruleMatrixRowData1;
                        for(RuleMatrixColumnData ruleMatrixColumnData : ruleMatrixRowData.getIfColumnData()){
                            if("6".equalsIgnoreCase(ruleMatrixColumnData.getDataType())) {
                                FormConfigEntityDataVO formConfigEntityDataVO = onglBinderMap.get(ruleMatrixColumnData.getFieldName());
                                if(null != formConfigEntityDataVO) {
                                    ruleMatrixColumnData.setWebDataBinderName(formConfigEntityDataVO.getWebDataBinderName());
                                    ruleMatrixColumnData.setItemValue(formConfigEntityDataVO.getItemValue());
                                    ruleMatrixColumnData.setItemLabel(formConfigEntityDataVO.getItemLabel());
                                    ObjectGraphTypes objectGraphTypes = onglMap.get(ruleMatrixColumnData.getFieldName());
                                    ruleMatrixColumnData.setObjectGraph(objectGraphTypes.getObjectGraph());
                                    if (ArrayUtils.isEmpty(ruleMatrixColumnData.getParameterArr()) && StringUtils.isNotEmpty(ruleMatrixColumnData.getStringValue())) {
                                        String[] stringNum = ruleMatrixColumnData.getStringValue().split(",");
                                        Long[] longNum = new Long[stringNum.length];
                                        for (int i = 0; i < stringNum.length; i++)
                                            longNum[i] = Long.parseLong(stringNum[i]);
                                        ruleMatrixColumnData.setParameterArr(longNum);
                                    }
                                }
                            }
                            if("objectGraphType".equalsIgnoreCase(ruleMatrixColumnData.getFieldType()) &&
                                    ruleMatrixColumnData.getDataType() != null && CollectionUtils.isEmpty(ruleMatrixColumnData.getParameters()) &&
                                    (ruleMatrixColumnData.getOperator().equals("IN") || ruleMatrixColumnData.getOperator().equals("NOT_IN"))) {
                                ruleMatrixColumnData.setParameters(getParametersBasedOnDataTypeModule(
                                        ruleMatrixTableData.getModuleNameTableData().getId(), ruleMatrixTableData.getSourceProductTableData(),
                                        Integer.parseInt(ruleMatrixColumnData.getDataType())));
                            }
                        }
                       /* for(RuleMatrixColumnData ruleMatrixColumnData : ruleMatrixRowData.getThenColumnData()){
                            ruleMatrixColumnData.setFieldType("field");
                        }*/
                        break;
                    }
                }
            }
        } else{
            ruleMatrixRowData = new RuleMatrixRowData();
            List<RuleMatrixColumnData> ifColumnData = new ArrayList<>();
            List<RuleMatrixColumnData> thenColumnData = new ArrayList<>();
            for(RuleMatrixGridData ruleMatrixGridData : ruleMatrixTableData.getIfTableGridData()){
                if(Objects.isNull(ruleMatrixGridData.getObjectGraphType())){
                    continue;
                }
                if("Rule".equalsIgnoreCase(ruleMatrixGridData.getRuleOgnlType())){
                    RuleMatrixColumnData ruleMatrixColumnData = new RuleMatrixColumnData();
                    ruleMatrixColumnData.setRuleBased(Boolean.TRUE);
                    Rule rule = baseMasterService.findById(Rule.class, ruleMatrixGridData.getObjectGraphType().getId());
                    ruleMatrixColumnData.setFieldName(rule.getDisplayName());
                    ifColumnData.add(ruleMatrixColumnData);
                }else {
                    RuleMatrixColumnData ruleMatrixColumnData = new RuleMatrixColumnData();
                    ruleMatrixColumnData.setRuleBased(Boolean.FALSE);
                    ruleMatrixColumnData.setOperator(ruleMatrixGridData.getOperator());
                    ObjectGraphTypes objectGraphType = baseMasterService.findById(ObjectGraphTypes.class, ruleMatrixGridData.getObjectGraphType().getId());
                    if(null != objectGraphType.getDataType() && "6".equalsIgnoreCase(objectGraphType.getDataType().getCode())) {
                        FormConfigEntityDataVO formConfigEntityDataVO = assignmentMatrixService.getBinderNameForReferenceOgnl(objectGraphType.getObjectGraph());
                        if (null != formConfigEntityDataVO) {
                            ruleMatrixColumnData.setWebDataBinderName(formConfigEntityDataVO.getWebDataBinderName());
                            ruleMatrixColumnData.setItemLabel(formConfigEntityDataVO.getItemLabel());
                            ruleMatrixColumnData.setItemValue(formConfigEntityDataVO.getItemValue());
                            ruleMatrixColumnData.setObjectGraph(objectGraphType.getObjectGraph());
                        }
                    }
                    String dataType = null;
                    String displayName = null;
                    Integer dataTypeInt = null;
                    if (objectGraphType != null) {
                        if (objectGraphType.getDataType() != null) {
                            dataType = objectGraphType.getDataType().getCode();
                            try {
                                dataTypeInt = Integer.parseInt(dataType);
                            } catch (NumberFormatException e) {
                                dataTypeInt = null;
                            }
                        }
                        displayName = objectGraphType.getDisplayName();
                    }
                    ruleMatrixColumnData.setDataType(dataType);
                    ruleMatrixColumnData.setFieldType(ruleMatrixGridData.getParamType());
                    ruleMatrixColumnData.setFieldName(displayName);
                    if (ruleMatrixGridData.getParamType().equalsIgnoreCase("objectGraphType") && dataTypeInt != null &&
                            (ruleMatrixGridData.getOperator().equals("IN") || ruleMatrixGridData.getOperator().equals("NOT_IN"))) {
                        ruleMatrixColumnData.setParameters(getParametersBasedOnDataTypeModule(
                                ruleMatrixTableData.getModuleNameTableData().getId(), ruleMatrixTableData.getSourceProductTableData(), dataTypeInt));
                    }
                   ifColumnData.add(ruleMatrixColumnData);
                }
            }
            for(RuleMatrixGridData ruleMatrixGridData : ruleMatrixTableData.getThenTableGridData()){
                if(Objects.isNull(ruleMatrixGridData.getObjectGraphType())){
                    continue;
                }
                RuleMatrixColumnData ruleMatrixColumnData = new RuleMatrixColumnData();
                ruleMatrixColumnData.setOperator(ruleMatrixGridData.getOperator());
                ObjectGraphTypes objectGraphType=null;
                RuleMatrixTypeOgnlMapping ruleMatrixTypeOgnlMapping=entityDao.find(RuleMatrixTypeOgnlMapping.class,ruleMatrixGridData.getObjectGraphType().getId());
               // ObjectGraphTypes objectGraphType =baseMasterService.findById(ObjectGraphTypes.class, ruleMatrixGridData.getObjectGraphType().getId());
                if(ruleMatrixTypeOgnlMapping!=null){
                    objectGraphType =ruleMatrixTypeOgnlMapping.getObjectGraphType();
                }else{
                    objectGraphType=baseMasterService.findById(ObjectGraphTypes.class, ruleMatrixGridData.getObjectGraphType().getId());
                }


                String dataType=null;
                String displayName=null;
                if(objectGraphType != null){
                    if(objectGraphType.getDataType() != null) {
                        dataType = objectGraphType.getDataType().getCode();
                    }
                    displayName = objectGraphType.getDisplayName();
                }
                ruleMatrixColumnData.setDataType(dataType);
                ruleMatrixColumnData.setFieldType(ruleMatrixGridData.getParamType());
                ruleMatrixColumnData.setFieldName(displayName);
                thenColumnData.add(ruleMatrixColumnData);
            }
            ruleMatrixRowData.setIfColumnData(ifColumnData);
            ruleMatrixRowData.setThenColumnData(thenColumnData);
        }
        map.put("moduleId", ruleMatrixTableData.getModuleNameTableData().getId());
        map.put("sourceProduct", ruleMatrixTableData.getSourceProductTableData());
        map.put("assignmentSetVO",ruleMatrixTableData.getAssignmentSet());
        map.put("ruleMatrixRowData", ruleMatrixRowData);
        //map.put("onglBinderMap",onglBinderMap);
        map.put("thenView",true);
        List<String> ruleResultList=new ArrayList<String>();
        ruleResultList.add("P");
        ruleResultList.add("F");
        ruleResultList.add("*");
        map.put("ruleResultList",ruleResultList);
        return "gridRowDataRuleMatrix";
    }

    @Override
    public List<Parameter> getParametersBasedOnDataTypeModule(Long moduleId, String sourceProduct, Integer dataType) {
        NamedQueryExecutor<Parameter> queryExecutor = new NamedQueryExecutor<Parameter>("RuleMatrix.getParametersBasedOnDataTypeModule")
                .addParameter("sourceProduct", sourceProduct).addParameter("moduleId", moduleId).addParameter("dataType", dataType);
        return entityDao.executeQuery(queryExecutor);
    }

    @Override
    public String loadAssignmentGridBodyJson(RuleMatrixTableData ruleMatrixTableData) {
        List<List<Object>> bodyData = new ArrayList<>();
        DataTableJsonHelper jsonHelper = new DataTableJsonHelper();
        if(Objects.nonNull(ruleMatrixTableData) && CollectionUtils.isNotEmpty(ruleMatrixTableData.getRuleMatrixRowDataList())){
            List<RuleMatrixRowData> ruleMatrixRowDataList = ruleMatrixTableData.getRuleMatrixRowDataList();
            ruleMatrixRowDataList.forEach(ruleMatrixRowData -> {
                List<Object> rowData = new ArrayList<>();
                updateDisplayNameInRowData(ruleMatrixRowData);
                prepareRowJson(rowData, ruleMatrixRowData);
                bodyData.add(rowData);
            });
        }
        jsonHelper.setAaData(bodyData);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        flowLogger.info("String is"+jsonString);
        return jsonString;
    }

    private void updateDisplayNameInRowData(RuleMatrixRowData ruleMatrixRowData) {
        if(CollectionUtils.isNotEmpty(ruleMatrixRowData.getIfColumnData())){
            ruleMatrixRowData.getIfColumnData().forEach(columnData -> {
                        if (StringUtils.isEmpty(columnData.getDisplayName())) {
                            updateDisplayNameInColumnData(columnData);
                        }
                    }
            );
        }
        if(CollectionUtils.isNotEmpty(ruleMatrixRowData.getThenColumnData())){
            ruleMatrixRowData.getThenColumnData().forEach(columnData -> {
                        if (StringUtils.isEmpty(columnData.getDisplayName())) {
                            updateDisplayNameInColumnData(columnData);
                        }
                    }
            );
        }
    }

    private void prepareRowJson(List<Object> rowData, RuleMatrixRowData ruleMatrixRowData) {
        List<Object> priorityList = new ArrayList<>();
        List<Object> actionsList = new ArrayList<>();
        priorityList.add(ruleMatrixRowData.getPriority());
        priorityList.add(ruleMatrixRowData.getIndex());
        rowData.add(priorityList);
        for(RuleMatrixColumnData ruleMatrixColumnData : ruleMatrixRowData.getIfColumnData()){
            if(ruleMatrixColumnData != null) {
                rowData.add(ruleMatrixColumnData.getDisplayName());
            } else{
                rowData.add("");
            }
        }
        for(RuleMatrixColumnData ruleMatrixColumnData : ruleMatrixRowData.getThenColumnData()){
            if(ruleMatrixColumnData != null) {
                rowData.add(ruleMatrixColumnData.getDisplayName());
            } else{
                rowData.add("");
            }
        }
        actionsList.add("edit");
        actionsList.add("delete");
        rowData.add(actionsList);
    }

    private void updateDisplayNameInColumnData(RuleMatrixColumnData columnData){
        StringBuilder stringBuilder = new StringBuilder();
        if(StringUtils.isNotEmpty(columnData.getStringValue())){
            if (columnData.getDataType().equalsIgnoreCase("6")) {
                String className = ContextObjectClass.getClassName(columnData.getObjectGraph());
                try {
                    Class aClass =ClassUtils.forName(className, null);
                    Object o = entityDao.find(aClass,Long.valueOf(columnData.getStringValue()));
                    Object name= FieldUtils.readDeclaredField(aClass.cast(o),columnData.getItemLabel(),true);
                    if(null != name){
                        stringBuilder.append(name);
                    }else{
                        stringBuilder.append(columnData.getStringValue());
                    }
                }catch (Exception e){
                    stringBuilder.append(columnData.getStringValue());
                }
            }else {
                stringBuilder.append(columnData.getStringValue());
            }
        }else if(StringUtils.isNotEmpty(columnData.getStringValueFrom()) && StringUtils.isNotEmpty(columnData.getStringValueTo())){
            stringBuilder.append("From: ");
            stringBuilder.append(columnData.getStringValueFrom());
            stringBuilder.append(" To: ");
            stringBuilder.append(columnData.getStringValueTo());
        }else if(Objects.nonNull(columnData.getParameter())){
            Parameter parameter = entityDao.find(Parameter.class, columnData.getParameter());
            stringBuilder.append(parameter.getDisplayName());
        }else if(Objects.nonNull(columnData.getParameterFrom()) && Objects.nonNull(columnData.getParameterTo())){
            stringBuilder.append("From: ");
            Parameter parameterFrom = entityDao.find(Parameter.class, columnData.getParameterFrom());
            stringBuilder.append(parameterFrom.getDisplayName());
            stringBuilder.append(" To: ");
            Parameter parameterTo = entityDao.find(Parameter.class, columnData.getParameterTo());
            stringBuilder.append(parameterTo.getDisplayName());
        }else if(columnData.getParameterArr()!= null && columnData.getParameterArr().length != 0){
            for(int i =0;i<columnData.getParameterArr().length;i++) {
                if (columnData.getDataType().equalsIgnoreCase("6")) {
                    String className = ContextObjectClass.getClassName(columnData.getObjectGraph());
                    try {
                       Class aClass =ClassUtils.forName(className, null);
                       Object o = entityDao.find(aClass,columnData.getParameterArr()[i]);
                       Object name= FieldUtils.readDeclaredField(aClass.cast(o),columnData.getItemLabel(),true);
                       if(null != name){
                           stringBuilder.append(name);
                       }else{
                           stringBuilder.append(columnData.getParameterArr()[i]);
                       }
                    }catch (Exception e){
                        Parameter parameter = entityDao.find(Parameter.class, columnData.getParameterArr()[i]);
                        stringBuilder.append(parameter.getDisplayName());
                    }
                } else{
                    Parameter parameter = entityDao.find(Parameter.class, columnData.getParameterArr()[i]);
                    stringBuilder.append(parameter.getDisplayName());
                }
                if (i < columnData.getParameterArr().length - 1) {
                    stringBuilder.append(',');
                }
            }
        }
        columnData.setDisplayName(stringBuilder.toString());
    }

    @Override
    public void addRowData(RuleMatrixTableData ruleMatrixTableData, RuleMatrixRowData ruleMatrixRowData, ModelMap map) {
        updateDisplayNameInRowData(ruleMatrixRowData);
        loadObjectDataInRowData(ruleMatrixTableData.getIfTableGridData());
        ruleMatrixRowData.setIfColumnData(orderColumnDataBasedOnHeader(ruleMatrixRowData.getIfColumnData(),ruleMatrixTableData.getIfTableGridData() ));
        if(ruleMatrixRowData.getIndex() == null) {
            addNewRow(ruleMatrixTableData, ruleMatrixRowData);
        } else{
            editExistingRow(ruleMatrixTableData, ruleMatrixRowData);
        }
    }

    private void editExistingRow(RuleMatrixTableData ruleMatrixTableData, RuleMatrixRowData ruleMatrixRowData) {
        if (CollectionUtils.isNotEmpty(ruleMatrixTableData.getRuleMatrixRowDataList())) {
            for(RuleMatrixRowData ruleMatrixRowDataSaved : ruleMatrixTableData.getRuleMatrixRowDataList()){
                if(ruleMatrixRowDataSaved != null && ruleMatrixRowDataSaved.getIndex().equals(ruleMatrixRowData.getIndex())){
                    try {
                        BeanUtils.copyProperties(ruleMatrixRowDataSaved, ruleMatrixRowData);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void addNewRow(RuleMatrixTableData ruleMatrixTableData, RuleMatrixRowData ruleMatrixRowData) {
        if (CollectionUtils.isNotEmpty(ruleMatrixTableData.getRuleMatrixRowDataList())) {
            List<Integer> indeces = new ArrayList<>();
            for(RuleMatrixRowData ruleMatrixRowDataSaved : ruleMatrixTableData.getRuleMatrixRowDataList()){
                indeces.add(ruleMatrixRowDataSaved.getIndex());
            }
            ruleMatrixRowData.setIndex(Collections.max(indeces)+1);
            ruleMatrixTableData.getRuleMatrixRowDataList().add(ruleMatrixRowData);
        } else {
            List<RuleMatrixRowData> ruleMatrixRowDataList = new ArrayList<>();
            ruleMatrixRowData.setIndex(0);
            ruleMatrixRowDataList.add(ruleMatrixRowData);
            ruleMatrixTableData.setRuleMatrixRowDataList(ruleMatrixRowDataList);
        }
    }


    private void loadObjectDataInRowData(List<RuleMatrixGridData> ruleMatrixGridDataList) {
        ruleMatrixGridDataList.forEach(workflowAssignmentGridData -> {
            if(StringUtils.isEmpty(workflowAssignmentGridData.getObjectGraphType().getDisplayName())){
                if("Rule".equalsIgnoreCase(workflowAssignmentGridData.getRuleOgnlType())){
                    //do nothing
                }else{
                    workflowAssignmentGridData.setObjectGraphType(entityDao.find(ObjectGraphTypes.class, workflowAssignmentGridData.getObjectGraphType().getId()));
                }
            }
        });
    }

    private List<RuleMatrixColumnData> orderColumnDataBasedOnHeader(List<RuleMatrixColumnData> columnDataList, List<RuleMatrixGridData> gridDataList) {
        HashMap fieldColumnDataMap = new HashMap();
        ArrayList orderedColumnData = new ArrayList();
        columnDataList.forEach(columnData->{
            fieldColumnDataMap.put(columnData.getFieldName(),columnData );
        });
        gridDataList.forEach(gridData ->{
            if("Rule".equalsIgnoreCase(gridData.getRuleOgnlType())){
                Rule rule = baseMasterService.findById(Rule.class,gridData.getObjectGraphType().getId());
                orderedColumnData.add(fieldColumnDataMap.get(rule.getDisplayName()));
            }else {
                orderedColumnData.add(fieldColumnDataMap.get(gridData.getObjectGraphType().getDisplayName()));
            }
        });
        return orderedColumnData;
    }

    @Override
    public String loadAssignmentGridRowJson(RuleMatrixRowData ruleMatrixRowData) {

        List<List<Object>> bodyData = new ArrayList<>();
        List<Object> rowData = new ArrayList<>();
        DataTableJsonHelper jsonHelper = new DataTableJsonHelper();
        updateDisplayNameInRowData(ruleMatrixRowData);
        prepareRowJson(rowData, ruleMatrixRowData);
        bodyData.add(rowData);
        jsonHelper.setAaData(bodyData);
        JSONSerializer iSerializer = new JSONSerializer();
        String jsonString = iSerializer.exclude("*.class").deepSerialize(jsonHelper);
        BaseLoggers.flowLogger.info("String is"+jsonString);
        return jsonString;
    }



    @Override
    public String saveRuleMatrixMaster(RuleMatrixMaster ruleMatrixMaster, BindingResult result,
                                         ModelMap map, boolean createAnotherMaster, List<RuleMatrixTableData> ruleMatrixTableData, boolean isSaveOnly, User user) {

        if (result.hasErrors()) {
            return newRuleMatrixMaster(ruleMatrixMaster, ruleMatrixTableData, map);
        }


        if(CollectionUtils.isEmpty(ruleMatrixTableData)){
            map.put("noAssignmentSet", true);
            if (ruleMatrixMaster.getId() != null) {
                map.put("edit", true);
                RuleMatrixMaster a = baseMasterService.getMasterEntityById(RuleMatrixMaster.class, ruleMatrixMaster.getId());
                if (!(ApprovalStatus.UNAPPROVED_ADDED == a.getApprovalStatus() || ApprovalStatus.CLONED == a.getApprovalStatus())) {
                    map.put("codeViewMode", true);
                }
            }
            return newRuleMatrixMaster(ruleMatrixMaster,ruleMatrixTableData, map);
        }


        if (user != null) {
            int count = 0;
            for(RuleMatrixTableData ruleMatrixTableData1:ruleMatrixTableData) {
                if (ruleMatrixTableData1.getAssignmentSet() != null) {
                    count++;
                    break;
                }
            }
            if (count == 0) {
                map.put("noAssignmentSet", true);
                return newRuleMatrixMaster(ruleMatrixMaster, ruleMatrixTableData, map);
            }

            for(RuleMatrixTableData ruleMatrixTableData1:ruleMatrixTableData){
                updateAssignmentSet(ruleMatrixMaster,ruleMatrixTableData1);

            }

            if(isSaveOnly){
                makerCheckerService.masterEntityChangedByUser(ruleMatrixMaster, user);
            } else {
                makerCheckerService.saveAndSendForApproval(ruleMatrixMaster, user);
            }
        }
        if (createAnotherMaster) {
            RuleMatrixTableData ruleMatrixTableData1 =new RuleMatrixTableData();
            ruleMatrixTableData1.setAssignmentIndex(0);
            List<RuleMatrixTableData> ruleMatrixTableDataList = new ArrayList<>();
            ruleMatrixTableDataList.add(ruleMatrixTableData1);
            return newRuleMatrixMaster(new RuleMatrixMaster(), ruleMatrixTableDataList , map);
        }
        map.put("masterID", "RuleMatrixMaster");
        return "redirect:/app/grid/RuleMatrixMaster/RuleMatrixMaster/loadColumnConfig";
    }


    @Override
    public String newRuleMatrixMaster(RuleMatrixMaster ruleMatrixMaster, List<RuleMatrixTableData> ruleMatrixTableData, ModelMap map) {
        map.put("ruleMatrixMaster", ruleMatrixMaster);
        map.put("sessionTableData",ruleMatrixTableData);
        map.put("moduleNames", genericParameterService.retrieveTypes(ModuleName.class));
        map.put("masterID", "RuleMatrixMaster");
        map.put("viewable",false);
        return "createRuleMatrixMaster";
    }


    private void updateAssignmentSet(RuleMatrixMaster ruleMatrixMaster,RuleMatrixTableData ruleMatrixTableData1) {


        AssignmentGrid assignmentGrid = new AssignmentGrid();


        List<AssignmentFieldMetaData> assignmentFieldMetaDataList = new ArrayList<>();
        int count = 0;
        String gridLevelExpression = "";
        String gridLevelExpressionId = "";

            if (CollectionUtils.isNotEmpty(ruleMatrixTableData1.getIfTableGridData())) {

                Map<Long, ObjectGraphTypes> objectGraphTypesList = new HashMap<>();
                for (RuleMatrixGridData ruleMatrixGridData : ruleMatrixTableData1.getIfTableGridData()) {
                    if (Objects.isNull(ruleMatrixGridData.getObjectGraphType())) {
                        continue;
                    }
                    Long id = ruleMatrixGridData.getObjectGraphType().getId();
                    if("Rule".equalsIgnoreCase(ruleMatrixGridData.getRuleOgnlType())){
                    //do nothing
                    }else {
                        objectGraphTypesList.put(id, entityDao.find(ObjectGraphTypes.class, id));
                    }
                }

                for (RuleMatrixGridData ruleMatrixGridData : ruleMatrixTableData1.getIfTableGridData()) {
                    if (Objects.isNull(ruleMatrixGridData.getObjectGraphType())) {
                        continue;
                    }
                    if("Rule".equalsIgnoreCase(ruleMatrixGridData.getRuleOgnlType())){
                        Rule rule = entityDao.find(Rule.class, ruleMatrixGridData.getObjectGraphType().getId());
                        AssignmentFieldMetaData assignmentFieldMetaData = new AssignmentFieldMetaData();
                        assignmentFieldMetaData.setParameterBased(false);
                        assignmentFieldMetaData.setRuleBased(Boolean.TRUE);
                        assignmentFieldMetaData.setRule(rule);
                        assignmentFieldMetaData.setFieldName(rule.getDisplayName());
                        assignmentFieldMetaData.setIndexId(rule.getId() + "_" + count);
                        gridLevelExpression += " " + rule.getDisplayName() + " &&";
                        gridLevelExpressionId += " " + rule.getId() + "_" + count + " &&";
                        count++;
                        assignmentFieldMetaDataList.add(assignmentFieldMetaData);
                    }else {
                        ObjectGraphTypes objectGraphType = objectGraphTypesList.get(ruleMatrixGridData.getObjectGraphType().getId());
                        AssignmentFieldMetaData assignmentFieldMetaData = new AssignmentFieldMetaData();
                        if ("objectGraphType".equals(ruleMatrixGridData.getParamType())) {
                            assignmentFieldMetaData.setParameterBased(true);
                        } else {
                            assignmentFieldMetaData.setParameterBased(false);
                        }
                        assignmentFieldMetaData.setOperator(ruleMatrixGridData.getOperator());
                        assignmentFieldMetaData.setOgnl(objectGraphType.getObjectGraph());
                        assignmentFieldMetaData.setDataType(Integer.parseInt(objectGraphType.getDataType().getCode()));
                        assignmentFieldMetaData.setFieldName(objectGraphType.getDisplayName());
                        assignmentFieldMetaData.setIndexId(objectGraphType.getId() + "_" + count);
                        gridLevelExpression += " " + objectGraphType.getDisplayName() + " &&";
                        gridLevelExpressionId += " " + objectGraphType.getId() + "_" + count + " &&";
                        count++;
                        assignmentFieldMetaDataList.add(assignmentFieldMetaData);
                    }


                }

            }


            gridLevelExpression = gridLevelExpression.substring(0, gridLevelExpression.length() - 3);
            gridLevelExpressionId = gridLevelExpressionId.substring(0, gridLevelExpressionId.length() - 3);
            assignmentGrid.setGridLevelExpression(gridLevelExpression);
            assignmentGrid.setGridLevelExpressionId(gridLevelExpressionId);
            assignmentGrid.setAssignmentFieldMetaDataList(assignmentFieldMetaDataList);

            assignmentGrid.setAssignmentSetType(AssignmentConstants.ASSIGNMENT_SET_TYPE_GRID);
            setAssignmentSetPropertiesForGrid(ruleMatrixMaster, ruleMatrixTableData1.getAssignmentIndex(), assignmentGrid, ruleMatrixTableData1);

            createAssignmentMatrixRowData(assignmentGrid, ruleMatrixTableData1);

            processAssignmentSet(assignmentGrid);
            setAssignmentGridExpProperties(assignmentGrid);




    }


    private void setAssignmentSetPropertiesForGrid(RuleMatrixMaster ruleMatrixMaster, int assignmentGridIndex,AssignmentSet assignmentSet,RuleMatrixTableData ruleMatrixTableData) {

        assignmentSet.setRuleMatrix(true);
        if(ruleMatrixTableData.getAssignmentSet()!=null){
            AssignmentSetVO assignmentSetVO=ruleMatrixTableData.getAssignmentSet();
            assignmentSet.setAssignmentSetName(assignmentSetVO.getAssignmentSetName());
            assignmentSet.setPriority(assignmentSetVO.getAssignmentPriority());
            assignmentSet.setAssignmentSetRule(assignmentSetVO.getAssignmentSetRule());
            assignmentSet.setExecuteAll(assignmentSetVO.getExecuteAll());
            assignmentSet.setDefaultSet(assignmentSetVO.getDefaultSet());
            assignmentSet.setEffectiveFrom(assignmentSetVO.getEffectiveFrom());
            assignmentSet.setEffectiveTill(assignmentSetVO.getEfffectiveTill());
            assignmentSet.setBufferDays(assignmentSetVO.getBufferDays());
        }
        if (assignmentGridIndex == 0) {
            List<AssignmentSet> assignmentSetList = new ArrayList<AssignmentSet>();
            assignmentSetList.add(assignmentSet);
            ruleMatrixMaster.setAssignmentSet(assignmentSetList);
        } else {
            if(CollectionUtils.isNotEmpty(ruleMatrixMaster.getAssignmentSet()))
              ruleMatrixMaster.getAssignmentSet().add(assignmentGridIndex, assignmentSet);
            else{
                List<AssignmentSet> assignmentSetList = new ArrayList<AssignmentSet>();
                assignmentSetList.add(assignmentSet);
                ruleMatrixMaster.setAssignmentSet(assignmentSetList);
            }
        }

        List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(ruleMatrixTableData.getThenTableGridData()))
        {
            Map<Long,ObjectGraphTypes> objectGraphTypesActionFieldList = new HashMap<>();
            for (RuleMatrixGridData ruleMatrixGridData : ruleMatrixTableData.getThenTableGridData()) {
                Long id = ruleMatrixGridData.getObjectGraphType().getId();
                ObjectGraphTypes objectGraphTypes;
                RuleMatrixTypeOgnlMapping ruleMatrixTypeOgnlMapping=entityDao.find(RuleMatrixTypeOgnlMapping.class,id);
                if(ruleMatrixTypeOgnlMapping!=null){
                    objectGraphTypes=ruleMatrixTypeOgnlMapping.getObjectGraphType();
                }else{
                    objectGraphTypes=entityDao.find(ObjectGraphTypes.class,id);
                }

                objectGraphTypesActionFieldList.put(objectGraphTypes.getId(),objectGraphTypes);
            }
            for (RuleMatrixGridData ruleMatrixGridData : ruleMatrixTableData.getThenTableGridData()) {
                Long id = ruleMatrixGridData.getObjectGraphType().getId();
                ObjectGraphTypes objectGraphTypes1;
                RuleMatrixTypeOgnlMapping ruleMatrixTypeOgnlMapping=entityDao.find(RuleMatrixTypeOgnlMapping.class,id);
                if(ruleMatrixTypeOgnlMapping!=null){
                    objectGraphTypes1=entityDao.find(RuleMatrixTypeOgnlMapping.class,id).getObjectGraphType();
                }else{
                    objectGraphTypes1=entityDao.find(ObjectGraphTypes.class,id);
                }
                ObjectGraphTypes objectGraphTypes = objectGraphTypesActionFieldList.get(objectGraphTypes1.getId());
                AssignmentFieldMetaData assignmentFieldMetaData = new AssignmentFieldMetaData();
                if("objectGraphType".equals(ruleMatrixGridData.getParamType())){
                    assignmentFieldMetaData.setParameterBased(true);
                }else{
                    assignmentFieldMetaData.setParameterBased(false);
                }
                assignmentFieldMetaData.setOgnl(objectGraphTypes.getObjectGraph());
                assignmentFieldMetaData.setDataType(Integer.parseInt(objectGraphTypes.getDataType().getCode()));
                assignmentFieldMetaData.setFieldName(objectGraphTypes.getDisplayName());
                assignmentFieldMetaData.setIndexId(objectGraphTypes.getId() + "");
                assignmentActionFieldMetaDataList.add(assignmentFieldMetaData);
            }
        }
        assignmentSet.setAssignmentActionFieldMetaDataList(assignmentActionFieldMetaDataList);

    }

    private void createAssignmentMatrixRowData(AssignmentGrid assignmentGrid,RuleMatrixTableData ruleMatrixTableData){
        if(Objects.isNull(assignmentGrid) || ruleMatrixTableData==null){
            return;
        }
        List<AssignmentMatrixRowData> assignmentMatrixRowDataList= new ArrayList<>();
        List<RuleMatrixRowData> ruleMatrixRowDataList = ruleMatrixTableData.getRuleMatrixRowDataList();
        HashMap<String,String> fieldNameIndexIdMap = new HashMap<>();
        HashMap<String,String> actionFieldNameIndexIdMap = new HashMap<>();
        for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentGrid.getAssignmentFieldMetaDataList()) {
            fieldNameIndexIdMap.put(assignmentFieldMetaData.getFieldName(),assignmentFieldMetaData.getIndexId() );
        }

        for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentGrid.getAssignmentActionFieldMetaDataList()) {
            actionFieldNameIndexIdMap.put(assignmentFieldMetaData.getFieldName(),assignmentFieldMetaData.getIndexId() );
        }

        for (RuleMatrixRowData ruleMatrixRowData : ruleMatrixRowDataList) {
            LinkedHashMap<Object,Object> jsonAssignActionMap = new LinkedHashMap();
            LinkedHashMap<Object,Object> linkedMap = new LinkedHashMap();
            List<RuleMatrixColumnData> ifColumnData = ruleMatrixRowData.getIfColumnData();
            for (RuleMatrixColumnData ruleMatrixColumnData : ifColumnData) {
                ruleMatrixColumnData.setIndexId(fieldNameIndexIdMap.get(ruleMatrixColumnData.getFieldName()));
                updateAssignmentMap(linkedMap,ruleMatrixColumnData);
            }

            List<RuleMatrixColumnData> thenColumnData = ruleMatrixRowData.getThenColumnData();
            for (RuleMatrixColumnData ruleMatrixColumnData : thenColumnData) {
                ruleMatrixColumnData.setIndexId(actionFieldNameIndexIdMap.get(ruleMatrixColumnData.getFieldName()));
                updateAssignmentMap(jsonAssignActionMap,ruleMatrixColumnData);
            }

            AssignmentMatrixRowData assignmentMatrixRowData = new AssignmentMatrixRowData();
            if(NumberUtils.toInt(ruleMatrixRowData.getPriority(),-1)!=-1) {
                assignmentMatrixRowData.setPriority(NumberUtils.toInt(ruleMatrixRowData.getPriority()));
            }
            AssignmentMatrixAction assignmentMatrixAction = new AssignmentMatrixAction();

            assignmentMatrixAction.setJsonAssignActionMap(jsonAssignActionMap);
            assignmentMatrixRowData.setAssignmentMatrixAction(assignmentMatrixAction);
            assignmentMatrixRowData.setEditedOrNewFlag(true);
            assignmentMatrixRowData.setLinkedMap(linkedMap);
            assignmentMatrixRowDataList.add(assignmentMatrixRowData );
        }
        assignmentGrid.setAssignmentMatrixRowData(assignmentMatrixRowDataList);
    }

    void updateAssignmentMap(LinkedHashMap<Object,Object> jsonAssignActionMap,RuleMatrixColumnData columnData){
        StringBuilder stringBuilder = new StringBuilder();
        if(StringUtils.isNotEmpty(columnData.getStringValue())){
            jsonAssignActionMap.put(columnData.getIndexId(),columnData.getStringValue());
        }else if(StringUtils.isNotEmpty(columnData.getStringValueFrom()) && StringUtils.isNotEmpty(columnData.getStringValueTo())){
            stringBuilder.append(columnData.getStringValueFrom());
            stringBuilder.append(",");
            stringBuilder.append(columnData.getStringValueTo());
            jsonAssignActionMap.put(columnData.getIndexId(),stringBuilder.toString());
        }else if(Objects.nonNull(columnData.getParameter())){
            jsonAssignActionMap.put(columnData.getIndexId(),String.valueOf(columnData.getParameter()));
        }else if(Objects.nonNull(columnData.getParameterFrom()) && Objects.nonNull(columnData.getParameterTo())){
            stringBuilder.append(columnData.getParameterFrom());;
            stringBuilder.append(",");
            stringBuilder.append(columnData.getParameterTo());
            jsonAssignActionMap.put(columnData.getIndexId(),stringBuilder.toString());
        }else if(columnData.getParameterArr()!= null && columnData.getParameterArr().length != 0){
            for(int i =0;i<columnData.getParameterArr().length;i++){
                stringBuilder.append(columnData.getParameterArr()[i]);
                if (i < columnData.getParameterArr().length - 1){
                    stringBuilder.append(',');
                }
            }
            jsonAssignActionMap.put(columnData.getIndexId(),stringBuilder.toString());
        }
    }

    @Override
    public void addAssignmentSetToSession(RuleMatrixTableData ruleMatrixTableData, ModelMap map,String assignmentSetName,Integer assignmentPriority,Long assignmentSetruleId,Boolean executeAll,Boolean defaultSet, Date effectiveFrom, Date effectiveTill,Integer bufferDays) {
        Rule assignmentSetRule=null;
        if(assignmentSetruleId!=null){
           assignmentSetRule=entityDao.find(Rule.class,assignmentSetruleId);
        }
            addNewAssignmentRow(ruleMatrixTableData,assignmentSetName,assignmentPriority,assignmentSetRule,executeAll,defaultSet,effectiveFrom,effectiveTill,bufferDays);

    }


    private void addNewAssignmentRow(RuleMatrixTableData ruleMatrixTableData,String assignmentSetName,
                                     Integer assignmentPriority,Rule assignmentSetRule,Boolean executeAll,Boolean  defaultSet, Date effectiveFrom, Date effectiveTill,Integer bufferDays) {

            AssignmentSetVO assignmentSetVO = new AssignmentSetVO();
            assignmentSetVO.setAssignmentSetName(assignmentSetName);
            assignmentSetVO.setAssignmentPriority(assignmentPriority);
            assignmentSetVO.setAssignmentSetRule(assignmentSetRule);
            assignmentSetVO.setExecuteAll(executeAll);
            assignmentSetVO.setDefaultSet(defaultSet);
            assignmentSetVO.setEffectiveFrom(effectiveFrom);
            assignmentSetVO.setEfffectiveTill(effectiveTill);
            assignmentSetVO.setBufferDays(bufferDays);
            ruleMatrixTableData.setAssignmentSet(assignmentSetVO);

    }

    @Override
    public String deleteRowData(RuleMatrixTableData ruleMatrixTableData, int index) {
        if(CollectionUtils.isNotEmpty(ruleMatrixTableData.getRuleMatrixRowDataList())){
            Iterator<RuleMatrixRowData> iterator = ruleMatrixTableData.getRuleMatrixRowDataList().iterator();
            while (iterator.hasNext()){
                RuleMatrixRowData ruleMatrixRowData = iterator.next();
                if(ruleMatrixRowData.getIndex().equals(index)){
                    iterator.remove();
                    break;
                }
            }
        }
        return "success";
    }

    @Override
    public String openRuleMatrixMaster(Long id, ModelMap map, UserInfo currentUser, boolean isViewOnly) {
        RuleMatrixMaster ruleMatrixMaster = baseMasterService.getMasterEntityWithActionsById(RuleMatrixMaster.class, id,
                currentUser.getUserEntityId().getUri());
        List<RuleMatrixTableData> ruleMatrixTableData =new ArrayList<>();
        RuleMatrixTableData ruleMatrixTableData1=new RuleMatrixTableData();
        if (ruleMatrixMaster.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED ||
                (ruleMatrixMaster.getApprovalStatus() == ApprovalStatus.WORFLOW_IN_PROGRESS && isViewOnly)){
            getPrevRuleMatrixMaster(map, ruleMatrixMaster);
            map.put("editLink", true);
        } else if(ruleMatrixMaster.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED && isViewOnly){
            getPrevRuleMatrixMaster(map, ruleMatrixMaster);
            map.put("viewLink", true);
        }
        if (ruleMatrixMaster.getApprovalStatus() == ApprovalStatus.APPROVED
                || ruleMatrixMaster.getApprovalStatus() == ApprovalStatus.UNAPPROVED_MODIFIED
                || ruleMatrixMaster.getApprovalStatus() == ApprovalStatus.APPROVED_MODIFIED) {
            map.put("disableIsoCode", true);
        }
        initialize(ruleMatrixMaster);
        for (AssignmentSet assignmentSet : ruleMatrixMaster.getAssignmentSet()) {
            generateAssignmentSetData(assignmentSet);
        }
        List<AssignmentSet> assignmentSetsList = ruleMatrixMaster.getAssignmentSet();

        sortAssignmentSetByPriority(assignmentSetsList);

        if (null != assignmentSetsList && assignmentSetsList.size() > 0) {
            int assignmentIndex=0;
            for (AssignmentSet assignmentSet : assignmentSetsList) {
                List<AssignmentMatrixRowData> assignmentMatrixRowDataList = assignmentSet.getAssignmentMatrixRowData();

                if (null != assignmentMatrixRowDataList && assignmentMatrixRowDataList.size() > 0) {
                    sortAssignmentMatrixRowDataByPriority(assignmentMatrixRowDataList);
                }
                ruleMatrixTableData1 = generateRuleMatrixTableData(assignmentSet,ruleMatrixMaster,assignmentIndex);
                ruleMatrixTableData.add(ruleMatrixTableData1);
                assignmentIndex++;
            }
        }

        map.put("ruleMatrixMaster", ruleMatrixMaster);
        map.put("sessionTableData",ruleMatrixTableData);
        map.put("masterID", "RuleMatrixMaster");
        if(isViewOnly){
            map.put("viewable" , true);
            map.put("codeViewMode", true);
        } else {
            map.put("viewable", false);
            map.put("edit", true);
            if(!(ApprovalStatus.UNAPPROVED_ADDED == ruleMatrixMaster.getApprovalStatus() || ApprovalStatus.CLONED == ruleMatrixMaster.getApprovalStatus())) {
                map.put("codeViewMode", true);
            }

        }
        ArrayList<String> actions = (ArrayList<String>) ruleMatrixMaster.getViewProperties().get("actions");
        if (actions != null) {
            for (String act : actions) {
                map.put("act" + act, false);
            }
        }
        return "createRuleMatrixMaster";
    }


    private void getPrevRuleMatrixMaster(ModelMap map, RuleMatrixMaster ruleMatrixMaster) {
        RuleMatrixMaster prevRuleMatrixMaster = (RuleMatrixMaster) baseMasterService.getLastApprovedEntityByUnapprovedEntityId(ruleMatrixMaster.getEntityId());
        if(prevRuleMatrixMaster!=null){
            initialize(prevRuleMatrixMaster);
        }
        map.put("prevRuleMatrixMaster", prevRuleMatrixMaster);
    }

    private void initialize(RuleMatrixMaster ruleMatrixMaster){

        Hibernate.initialize(ruleMatrixMaster.getModuleName());
        Hibernate.initialize(ruleMatrixMaster.getSourceProduct());

        Hibernate.initialize(ruleMatrixMaster.getAssignmentSet());
        if(ruleMatrixMaster.getAssignmentSet()!=null){
            ruleMatrixMaster.getAssignmentSet().forEach(assignmentSet -> {
                Hibernate.initialize(assignmentSet);
                if(assignmentSet!=null){
                    Hibernate.initialize(assignmentSet.getEntityTypeMetaDataList());
                    Hibernate.initialize(assignmentSet.getAssignmentActionFieldMetaDataList());
                    Hibernate.initialize(assignmentSet.getAssignmentMatrixRowData());
                    for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentSet.getAssignmentActionFieldMetaDataList()) {
                        Hibernate.initialize(assignmentFieldMetaData);
                    }
                    for (AssignmentFieldMetaData assignmentFieldMetaData : ((AssignmentGrid) assignmentSet)
                            .getAssignmentFieldMetaDataList()) {
                        Hibernate.initialize(assignmentFieldMetaData);
                    }
                    if(assignmentSet.getAssignmentMatrixRowData()!=null){
                        assignmentSet.getAssignmentMatrixRowData().forEach(assignmentMatrixRowData ->
                        {
                            Hibernate.initialize(assignmentMatrixRowData);
                            if(assignmentMatrixRowData!=null){
                                Hibernate.initialize(assignmentMatrixRowData);
                                Hibernate.initialize(assignmentMatrixRowData.getAssignmentMatrixAction());
                                Hibernate.initialize(assignmentMatrixRowData.getAssignmentMatrixAction().getParameters());
                                Hibernate.initialize(assignmentMatrixRowData.getRule());
                                Hibernate.initialize(assignmentMatrixRowData.getRule().getRuleTagNames());
                                if(null!=assignmentMatrixRowData.getRule().getRuntimeRuleMapping()){
                                    Hibernate.initialize(assignmentMatrixRowData.getRule().getRuntimeRuleMapping().getParameters());
                                    Hibernate.initialize(assignmentMatrixRowData.getRule().getRuntimeRuleMapping().getObjectGraphs());
                                }
                            }
                        });
                    }
                }
            });

        }

    }

    private void generateAssignmentSetData(AssignmentSet assignmentSet) {
        if (CollectionUtils.isNotEmpty(assignmentSet.getAssignmentMatrixRowData())) {
            for (AssignmentMatrixRowData assignmentMatrixRowData : assignmentSet.getAssignmentMatrixRowData()) {

                AssignmentMatrixAction assignmentMatrixAction = assignmentMatrixRowData.getAssignmentMatrixAction();
                if (assignmentSet instanceof AssignmentGrid) {

                    String mapValues = assignmentMatrixRowData.getRowMapValues();
                    if (null != mapValues) {
                        Map<Object, Object> linkedMap = (Map<Object, Object>) new JSONDeserializer().deserialize(mapValues);

                        Iterator<Map.Entry<Object, Object>> entries = linkedMap.entrySet().iterator();
                        while (entries.hasNext()) {
                            Map.Entry<Object, Object> entry = entries.next();

                            if (null != entry.getValue()) {
                                entry.setValue(((String) entry.getValue()).replace(
                                        AssignmentConstants.MULTI_VALUE_SEPARATOR, ","));
                            }
                        }

                        assignmentMatrixRowData.setLinkedMap((new LinkedHashMap<Object, Object>(linkedMap)));

                    }
                }
                String assignValues = assignmentMatrixAction.getAssignActionValues();

                if (null != assignValues) {
                    Map<Object, Object> assignMap = (Map<Object, Object>) new JSONDeserializer().deserialize(assignValues);
                    assignmentMatrixAction.setJsonAssignActionMap(((new LinkedHashMap<Object, Object>(assignMap))));
                }

            }
        }
    }


    protected void sortAssignmentMatrixRowDataByPriority(List<AssignmentMatrixRowData> assignmentMatrixRowDatas) {
        assignmentMatrixRowDatas.sort(comparing(AssignmentMatrixRowData::getPriority,Comparator.nullsLast(Comparator.naturalOrder())));
    }


    protected void sortAssignmentSetByPriority(List<AssignmentSet> assignmentSetsList) {
        Collections.sort(assignmentSetsList, new Comparator<AssignmentSet>() {
            @Override
            public int compare(AssignmentSet o1, AssignmentSet o2) {

                if (o1.getPriority() == null && o2.getPriority() == null) {
                    return 0;
                }

                else if (o1.getPriority() == null) {
                    return 1;
                }

                else if (o2.getPriority() == null) {
                    return -1;
                }

                else if (o1.getPriority() == o2.getPriority()) {
                    return 0;
                }

                return o1.getPriority() < o2.getPriority() ? -1 : 1;
            }
        });
    }


    private RuleMatrixTableData generateRuleMatrixTableData(AssignmentSet assignmentSet,RuleMatrixMaster ruleMatrixMaster,int assignmentIndex){
        if(assignmentSet==null){
            return new RuleMatrixTableData();
        }
        RuleMatrixTableData ruleMatrixTableData = new RuleMatrixTableData();
        List<RuleMatrixRowData> ruleMatrixRowDataList = new ArrayList<>();
        List<RuleMatrixGridData> ifGridData= new ArrayList<>();
        List<RuleMatrixGridData> thenGridData=new ArrayList<>();
        AssignmentSetVO assignmentSetVO=null;
        AtomicInteger count = new AtomicInteger();


            AssignmentGrid assignmentGrid = null;
            if(assignmentSet instanceof AssignmentGrid){
                assignmentGrid = (AssignmentGrid)assignmentSet;
            }

            for(AssignmentMatrixRowData assignmentMatrixRowData: assignmentSet.getAssignmentMatrixRowData())
            {
                RuleMatrixRowData ruleMatrixRowData = new RuleMatrixRowData();
                ruleMatrixRowData.setIndex(count.getAndIncrement());
                ruleMatrixRowData.setPriority(String.valueOf(assignmentMatrixRowData.getPriority()));
                List<RuleMatrixColumnData> ifColumnData = new ArrayList<>();
                List<RuleMatrixColumnData> thenColumnData = new ArrayList<>();
                if(assignmentGrid!=null){
                    List<AssignmentFieldMetaData> assignmentFieldMetaDataList = assignmentGrid.getAssignmentFieldMetaDataList();
                    ifGridData=prepareIfGridData(assignmentFieldMetaDataList);
                    HashMap assignmentMatrixLinkedMap = assignmentMatrixRowData.getLinkedMap();
                    for (AssignmentFieldMetaData assignmentFieldMetaData : assignmentFieldMetaDataList) {
                        if(assignmentFieldMetaData.getIndexId()!=null){
                            RuleMatrixColumnData columnData = getRuleMatrixColumnData(assignmentMatrixLinkedMap, assignmentFieldMetaData);
                            ifColumnData.add(columnData);
                        }
                    }
                    List<RuleMatrixColumnData> orderedIfColumnData = orderColumnDataBasedOnHeader(ifColumnData,ifGridData);
                    ruleMatrixRowData.setIfColumnData(orderedIfColumnData);
                    List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList = assignmentGrid.getAssignmentActionFieldMetaDataList();
                    thenGridData=prepareThenGridData(assignmentActionFieldMetaDataList);
                    assignmentSetVO=prepareAssignmentVO(assignmentSet);
                    Map<Object, Object> jsonAssignActionMap = assignmentMatrixRowData.getAssignmentMatrixAction().getJsonAssignActionMap();
                    assignmentActionFieldMetaDataList.forEach(assignmentActionFieldMetaData ->
                    {
                        if(assignmentActionFieldMetaData.getIndexId()!=null){
                            RuleMatrixColumnData columnData = getRuleMatrixColumnData(jsonAssignActionMap, assignmentActionFieldMetaData);
                            thenColumnData.add(columnData);
                        }
                    });
                    List<RuleMatrixColumnData> orderedThenColumnData = new ArrayList<>();
                    ruleMatrixRowData.setThenColumnData(thenColumnData);
                }
                ruleMatrixRowDataList.add(ruleMatrixRowData);
            }


        ruleMatrixTableData.setRuleMatrixRowDataList(ruleMatrixRowDataList);
        ruleMatrixTableData.setIfTableGridData(ifGridData);
        ruleMatrixTableData.setThenTableGridData(thenGridData);
        ruleMatrixTableData.setModuleNameTableData(ruleMatrixMaster.getModuleName());
        ruleMatrixTableData.setSourceProductTableData(ruleMatrixMaster.getSourceProduct());
        ruleMatrixTableData.setAssignmentSet(assignmentSetVO);
        ruleMatrixTableData.setAssignmentIndex(assignmentIndex);

        return ruleMatrixTableData;
    }


    private RuleMatrixColumnData getRuleMatrixColumnData(Map assignmentMatrixLinkedMap, AssignmentFieldMetaData assignmentFieldMetaData) {
        String value = "";
        if(assignmentMatrixLinkedMap.containsKey(assignmentFieldMetaData.getIndexId())) {
            if(assignmentMatrixLinkedMap.get(assignmentFieldMetaData.getIndexId())!=null)
                value = assignmentMatrixLinkedMap.get(assignmentFieldMetaData.getIndexId()).toString();
        }
        RuleMatrixColumnData columnData = new RuleMatrixColumnData();
        columnData.setOperator(assignmentFieldMetaData.getOperator());
        columnData.setDataType(String.valueOf(assignmentFieldMetaData.getDataType()));
        columnData.setFieldName(assignmentFieldMetaData.getFieldName());
        if(Objects.nonNull(assignmentFieldMetaData.getRuleBased()) && assignmentFieldMetaData.getRuleBased()){
            columnData.setFieldType("Rule");
            columnData.setRuleBased(true);
            columnData.setStringValue(value);
        }else if(Objects.nonNull(assignmentFieldMetaData.getParameterBased()) && assignmentFieldMetaData.getParameterBased()){
            columnData.setFieldType("objectGraphType");
            if("BETWEEN".equalsIgnoreCase(assignmentFieldMetaData.getOperator())){
                if(StringUtils.isNotBlank(value)) {
                    String[] splitValue = value.split(",");
                    columnData.setParameterFrom(Long.valueOf(splitValue[0]));
                    columnData.setParameterTo(Long.valueOf(splitValue[1]));
                }
            }else if("IN".equalsIgnoreCase(assignmentFieldMetaData.getOperator()) || "NOT_IN".equalsIgnoreCase(assignmentFieldMetaData.getOperator())){
                if(StringUtils.isNotBlank(value)) {
                    String[] splitValue = value.split(",");
                    Long[] splitLongValue = new Long[splitValue.length];
                    for (int i = 0; i < splitValue.length; i++) {
                        splitLongValue[i] = NumberUtils.isNumber(splitValue[i]) ? Long.valueOf(splitValue[i]) : null;
                    }
                    columnData.setParameterArr(splitLongValue);
                }
            }else{
                if(NumberUtils.isNumber(value)) {
                    columnData.setParameter(Long.valueOf(value));
                }
            }
        }else{
            columnData.setFieldType("field");
            if("BETWEEN".equalsIgnoreCase(assignmentFieldMetaData.getOperator())){
                if(StringUtils.isNotBlank(value)) {
                    String[] splitValue = value.split(",");
                    if (splitValue.length == 2) {
                        columnData.setStringValueFrom(splitValue[0]);
                        columnData.setStringValueTo(splitValue[1]);
                    }
                }
            }else{
                columnData.setStringValue(value);
                if("6".equalsIgnoreCase(columnData.getDataType())){
                    columnData.setObjectGraph(assignmentFieldMetaData.getOgnl());
                    ObjectGraphTypes objectGraphTypes = baseMasterService.findById(ObjectGraphTypes.class, Long.valueOf(assignmentFieldMetaData.getIndexId().split("_")[0]));
                    if(null != objectGraphTypes){
                        FormConfigEntityDataVO formConfigEntityDataVO = assignmentMatrixService.getBinderNameForReferenceOgnl(objectGraphTypes.getObjectGraph());
                        if(null != formConfigEntityDataVO){
                            columnData.setWebDataBinderName(formConfigEntityDataVO.getWebDataBinderName());
                            columnData.setItemLabel(formConfigEntityDataVO.getItemLabel());
                            columnData.setItemValue(formConfigEntityDataVO.getItemValue());
                        }
                    }
                    if(columnData.getStringValue().contains(",")){
                        String[] stringValueList = columnData.getStringValue().split(",");
                        Long[] parameterArray=new Long[stringValueList.length];
                        for(int i=0;i<stringValueList.length;i++){
                            parameterArray[i]=Long.valueOf(stringValueList[i]);
                        }
                        columnData.setParameterArr(parameterArray);
                        columnData.setStringValue(null);
                    }
                }
            }
        }
        updateDisplayNameInColumnData(columnData);
        return columnData;
    }

    private List<RuleMatrixGridData> prepareIfGridData(List<AssignmentFieldMetaData> assignmentFieldMetaDataList){
         List<RuleMatrixGridData> ifGridData= new ArrayList<>();
         for(AssignmentFieldMetaData assignmentFieldMetaData:assignmentFieldMetaDataList){
             RuleMatrixGridData ruleMatrixGridData=new RuleMatrixGridData();
             if(null != assignmentFieldMetaData.getRuleBased() && assignmentFieldMetaData.getRuleBased()) {
                 ruleMatrixGridData.setRuleOgnlType("Rule");
                 String id = assignmentFieldMetaData.getIndexId().split("_")[0];
                 Rule rule = entityDao.find(Rule.class, Long.parseLong(id));
                 ObjectGraphTypes objectGraphTypes=new ObjectGraphTypes();
                 objectGraphTypes.setId(rule.getId());
                 ruleMatrixGridData.setObjectGraphType(objectGraphTypes);
             }else{
                 ruleMatrixGridData.setRuleOgnlType("objectGraphType");
                 if (assignmentFieldMetaData.getParameterBased() != null && assignmentFieldMetaData.getParameterBased()) {
                     ruleMatrixGridData.setParamType("objectGraphType");
                 } else {
                     ruleMatrixGridData.setParamType("field");
                 }
                 ruleMatrixGridData.setOperator(assignmentFieldMetaData.getOperator());
                 String id = assignmentFieldMetaData.getIndexId().split("_")[0];
                 ObjectGraphTypes objectGraphTypes = entityDao.find(ObjectGraphTypes.class, Long.parseLong(id));
                 ruleMatrixGridData.setObjectGraphType(objectGraphTypes);
             }
             ifGridData.add(ruleMatrixGridData);
         }

         return ifGridData;
    }

    private List<RuleMatrixGridData> prepareThenGridData(List<AssignmentFieldMetaData> assignmentActionFieldMetaDataList){
        List<RuleMatrixGridData> thenGridData= new ArrayList<>();
        for(AssignmentFieldMetaData assignmentFieldMetaData:assignmentActionFieldMetaDataList){
            RuleMatrixGridData ruleMatrixGridData=new RuleMatrixGridData();
            if(assignmentFieldMetaData.getParameterBased()!=null && assignmentFieldMetaData.getParameterBased()){
                ruleMatrixGridData.setParamType("objectGraphType");
            }else{
                ruleMatrixGridData.setParamType("field");
            }
            ruleMatrixGridData.setOperator(assignmentFieldMetaData.getOperator());
            ObjectGraphTypes objectGraphTypes=entityDao.find(ObjectGraphTypes.class,Long.parseLong(assignmentFieldMetaData.getIndexId().trim()));
            ruleMatrixGridData.setObjectGraphType(objectGraphTypes);
            thenGridData.add(ruleMatrixGridData);
        }

        return thenGridData;
    }


    private AssignmentSetVO prepareAssignmentVO(AssignmentSet assignmentSet){
        AssignmentSetVO assignmentSetVO=new AssignmentSetVO();
        if(assignmentSet!=null){
            assignmentSetVO.setAssignmentSetName(assignmentSet.getAssignmentSetName());
            assignmentSetVO.setAssignmentPriority(assignmentSet.getPriority());
            assignmentSetVO.setAssignmentSetRule(assignmentSet.getAssignmentSetRule());
            assignmentSetVO.setDefaultSet(assignmentSet.getDefaultSet());
            assignmentSetVO.setExecuteAll(assignmentSet.getExecuteAll());
            assignmentSetVO.setEffectiveFrom(assignmentSet.getEffectiveFrom());
            assignmentSetVO.setEfffectiveTill(assignmentSet.getEffectiveTill());
            assignmentSetVO.setBufferDays(assignmentSet.getBufferDays());
        }

        return assignmentSetVO;
    }

    private void setAssignmentGridExpProperties(AssignmentSet assignmentSet) {
        if (assignmentSet instanceof AssignmentGrid) {
            ((AssignmentGrid)assignmentSet).setGridLevelExpressionId(((AssignmentGrid)assignmentSet).getGridLevelExpressionId().replaceAll("\\s+", " ").trim());
            AssignmentGrid assignmentGrid = (AssignmentGrid) assignmentSet;
            assignmentMatrixService.populateAssignmentGridProperties(assignmentGrid);

        }
    }


}
