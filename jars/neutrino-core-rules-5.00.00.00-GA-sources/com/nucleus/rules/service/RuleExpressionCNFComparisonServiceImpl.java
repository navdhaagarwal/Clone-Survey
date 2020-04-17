package com.nucleus.rules.service;

import com.nucleus.finnone.pro.cache.common.*;
import com.nucleus.logging.*;
import com.nucleus.rules.model.*;
import com.nucleus.rules.populator.*;
import org.apache.commons.collections4.*;
import org.apache.commons.lang3.*;
import org.logicng.formulas.*;
import org.logicng.io.parsers.*;

import javax.annotation.*;
import javax.inject.*;
import java.util.*;

@Named("ruleExpressionCNFComparisonService")
public class RuleExpressionCNFComparisonServiceImpl implements RuleExpressionCNFComparisonService {

    @Inject
    @Named("ruleService")
    private RuleService                ruleService;

    @Inject
    @Named("ruleExpressionCNFMetaDataCache")
    private RuleExpressionCNFMetaDataCachePopulator ruleExpressionCNFMetaDataCache;

    @Inject
    @Named("ruleDistinctConditionCache")
    private RuleDistinctConditionCachePopulator ruleDistinctConditionCache;


    public final FormulaFactory f = new FormulaFactory();
    public final PropositionalParser p = new PropositionalParser(f);


    public List<RuleExpressionCNFMetaData> compareRuleExpression(String ruleExp,String ruleCode){

        String cnfForm = "";
        List<RuleExpressionCNFMetaData> similarRuleExpressionCnfMetaDataList = new ArrayList<>();
        if(StringUtils.isNotEmpty(ruleExp)) {
            try {
                Formula formula = p.parse(ruleExp.replaceAll("&&", "&").replaceAll("\\|\\|", "\\|"));
                cnfForm = formula.cnf().toString();
                Map<String, Object> map = preProcessCnf(cnfForm);
                //compare here
                Set<Long> similarRuleIds = (Set<Long>) ruleDistinctConditionCache.get((String) map.get("distinctVariablesKey"));
                Long numberOfGroups = (Long) map.get("numberOfGroups");
                Set sortedGroupVariablesSet = (Set) map.get("sortedGroupVariablesSet");
                if (CollectionUtils.isNotEmpty(similarRuleIds)) {
                    for (Long ruleId : similarRuleIds) {
                        RuleExpressionCNFMetaData ruleExpressionCNFMetaData = (RuleExpressionCNFMetaData) ruleExpressionCNFMetaDataCache.get(ruleId);
                        if (ruleExpressionCNFMetaData != null && !ruleCode.equals(ruleExpressionCNFMetaData.getRuleCode()) && ruleExpressionCNFMetaData.getNumberOfGroups() == numberOfGroups && sortedGroupVariablesSet.containsAll(ruleExpressionCNFMetaData.getGroupVariableSet())) {
                            similarRuleExpressionCnfMetaDataList.add(ruleExpressionCNFMetaData);
                        }
                    }
                }
            } catch (Exception e) {
                BaseLoggers.flowLogger.error("unable to compare rule expressions", e);
            }
        }


        return similarRuleExpressionCnfMetaDataList;
    }



    public void createCnfForAllRules(){
        try {

            List<Object> ruleList = ruleService.getAllRuleExpressions();

            for(Object obj : ruleList){
            Object [] array = (Object[])obj;
            String ruleExpression = (String)array[2];
            if(StringUtils.isNotEmpty(ruleExpression)) {

                    Formula formula = p.parse(ruleExpression.replaceAll("&&", "&").replaceAll("\\|\\|", "\\|"));
                    String cnf = formula.cnf().toString();

                    Map<String, Object> map = preProcessCnf(cnf);

                    updateCache(map,array,ruleExpression,cnf);

                }

            }
        }
        catch (Exception e) {
            BaseLoggers.flowLogger.error("unable to create cache for rule expressions", e);
        }


    }



    public Map<String,Object> preProcessCnf(String cnf){
        Map<String,Object> map = new HashMap<>();
        String [] groups = cnf.split("&");
        long numberOfGroups = groups.length;
        Set<String> sortedGroupVariablesSet = new HashSet<>(); /// comma separeated variables eg. (a & b) -> "a,b"
        Set<String> distinctVariables = new HashSet<>();
        for(int i=0;i<groups.length;i++){
            String group= groups[i].trim();
            String [] groupVariables = group.replaceAll("\\(","").replaceAll("\\)","").replaceAll("//s+","").split("\\|");
            List<String> sortedGroupVariables = Arrays.asList(groupVariables);
            Collections.sort(sortedGroupVariables);
            sortedGroupVariablesSet.add(StringUtils.join(sortedGroupVariables,",").replaceAll("\\s+",""));
            for(int j=0;j<groupVariables.length;j++)
                distinctVariables.add(groupVariables[j].replaceAll("\\s+",""));
        }
        List<String> distinctVariablesList = new ArrayList<>();
        distinctVariablesList.addAll(distinctVariables);
        Collections.sort(distinctVariablesList);
        String distinctVariablesKey = StringUtils.join(distinctVariablesList,",").replaceAll("\\s+","");
        map.put("numberOfGroups",numberOfGroups);
        map.put("distinctVariablesKey",distinctVariablesKey);
        map.put("sortedGroupVariablesSet",sortedGroupVariablesSet);
        return map;
    }


    public void deleteFromCnfMetaDataCache(String ruleExp, String ruleCode, Long ruleId){
        String cnfForm = "";
        if(StringUtils.isNotEmpty(ruleExp)) {
            try {
                Formula formula = p.parse(ruleExp.replaceAll("&&", "&").replaceAll("\\|\\|", "\\|"));
                cnfForm = formula.cnf().toString();
                Map<String, Object> map = preProcessCnf(cnfForm);
                if (ruleExpressionCNFMetaDataCache.containsKey(ruleId))
                    ruleExpressionCNFMetaDataCache.update(NeutrinoCachePopulator.Action.DELETE, ruleId);

                String distinctVariablesKey = (String) map.get("distinctVariablesKey");
                if (StringUtils.isNotEmpty(distinctVariablesKey)) {
                    List<Long> ruleIds = (List<Long>) ruleDistinctConditionCache.get(distinctVariablesKey);
                    if (ruleIds.contains(ruleId)) {
                        ruleDistinctConditionCache.update(NeutrinoCachePopulator.Action.DELETE, Arrays.asList(distinctVariablesKey, ruleId));
                    }
                }
            } catch (Exception e) {
                BaseLoggers.flowLogger.error("unable to delete from rule expression cnf cache", e);
            }
        }
    }

    public void updateFromCnfMetaDataCache(String ruleExp, String ruleCode, Long ruleId, Integer approvalStatus){
        String cnfForm = "";
        if(StringUtils.isNotEmpty(ruleExp)) {
            try {
                Formula formula = p.parse(ruleExp.replaceAll("&&", "&").replaceAll("\\|\\|", "\\|"));
                cnfForm = formula.cnf().toString();
                Map<String, Object> map = preProcessCnf(cnfForm);
                Object[] array = {ruleId, ruleCode, ruleExp, approvalStatus};
                updateCache(map, array, ruleExp, cnfForm);

            } catch (Exception e) {
                BaseLoggers.flowLogger.error("Unable to update rule expression cnf cache", e);
            }
        }
    }


    private void updateCache(Map map , Object[] array, String ruleExpression, String cnf){
        try {
            Set<Long> numberOfDistinctVariableValue = (Set<Long>) ruleDistinctConditionCache.get(map.get("distinctVariablesKey"));
            if (numberOfDistinctVariableValue == null)
                numberOfDistinctVariableValue = new HashSet<>();
            numberOfDistinctVariableValue.add((Long) array[0]);

            ruleDistinctConditionCache.update(NeutrinoCachePopulator.Action.INSERT, Arrays.asList(map.get("distinctVariablesKey"), numberOfDistinctVariableValue));


            RuleExpressionCNFMetaData metaData = new RuleExpressionCNFMetaData();
            metaData.setRuleId((Long) array[0]);
            metaData.setRuleCode((String) array[1]);
            metaData.setNumberOfGroups((Long) map.get("numberOfGroups"));
            metaData.setGroupVariableSet((Set) map.get("sortedGroupVariablesSet"));
            metaData.setRuleExpression(ruleExpression);
            metaData.setCnfForm(cnf);

            ruleExpressionCNFMetaDataCache.update(NeutrinoCachePopulator.Action.INSERT, Arrays.asList(array[0], metaData));
        }catch (Exception e){
            BaseLoggers.flowLogger.error("Unable to update rule expression cnf cache", e);
        }

    }
}
