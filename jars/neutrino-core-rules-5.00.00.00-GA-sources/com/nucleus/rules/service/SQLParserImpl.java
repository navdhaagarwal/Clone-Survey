package com.nucleus.rules.service;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.core.formsConfiguration.validationcomponent.Tuple_2;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.persistence.EntityDao;
import com.nucleus.rules.model.Rule;
import com.nucleus.rules.model.SQLParseResponse;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.*;

import static com.nucleus.rules.service.RuleConstants.LEFT_PAREN;

@Component("sQLParserImpl")
public class SQLParserImpl implements ISQLParser {

    @Inject
    @Named("messageSource")
    private MessageSource messageSource;

    @Inject
    @Named("entityDao")
    private EntityDao entityDao;

    @Inject
    @Named("configurationService")
    private ConfigurationService configurationService;

    private static List<Map> excludedTableMetaData = new ArrayList<>();
    private static List<String> excludedColumnForSuperSet = new ArrayList<>();
    public static final String TABLE_NAME = "Table Name : ";
    public static final String COLUMN_EXCLUDED = " Column Excluded : ";
    public static final String FALSE = "false";
    public static final String COLUMN_NAMES = "columnNames";
    public static final String QUERY_STRING = "SELECT new Map(ss.tableName AS TableName , ss.columnNames AS columnNames) FROM SqlTableMetaData ss ";

    @Override
    public String parseSqlAndCheckExclusion(String query) {
        String result = null;
        Map<String,String> errorMap = new HashMap<>();
        try {
            String[] whereClauses =StringUtils.substringsBetween(query ,RuleConstants.LEFT_CURLY_BRACES,RuleConstants.RIGHT_CURLY_BRACES);
            for (int i = 0; i < whereClauses.length; i++) {
                query = query.replace(RuleConstants.LEFT_CURLY_BRACES+whereClauses[i]+RuleConstants.RIGHT_CURLY_BRACES, " ? ");
            }
            String pattern = "(\\\\(\\\\s*\\\\+\\\\s*\\\\))";
            query = query.replaceAll(pattern," ");
            CCJSqlParser manager = new CCJSqlParser(new ByteArrayInputStream(query.getBytes()));
            Statement st = manager.Statement();
            Select selectStatement = (Select) st;
            SQLParseResponse response = new SQLParseResponse();
            PlainSelect ps = (PlainSelect) selectStatement.getSelectBody();
            List<String> errorList = new ArrayList<>();
            checkForAliasPresent(ps,errorList);
            if(CollectionUtils.isEmpty(errorList)) {
                parseSelectStatement(ps, response);
                errorMap.put(RuleConstants.WARNING,checkForTableExclusion(response));

            }else{
                prepareErrorWarningMap(errorList,errorMap);

            }
            BaseLoggers.exceptionLogger.error("Query Parsed : "+query +" ERROR :"+errorMap.get(RuleConstants.ERROR)+" WARNING :"+errorMap.get(RuleConstants.WARNING));
        } catch (ParseException e) {
            BaseLoggers.exceptionLogger.error("Error occured while parsing sql : "+query+"  "+ e.getLocalizedMessage());
        }
        return errorMap.get(RuleConstants.ERROR);
    }

    private void prepareErrorWarningMap(List<String> errorList,Map<String,String> errorMap){
        StringBuilder stringBuilder = new StringBuilder();
        errorList.forEach(el->{
            if(stringBuilder.length()!=0){
                stringBuilder.append(",");
            }
            stringBuilder.append(el);
        });
        errorMap.put(RuleConstants.ERROR,stringBuilder.toString());
    }

    private String checkForTableExclusion(SQLParseResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        if(org.apache.commons.collections4.CollectionUtils.isEmpty(excludedTableMetaData)){
            JPAQueryExecutor jpaQueryExecutor = new JPAQueryExecutor(QUERY_STRING);
            excludedTableMetaData = entityDao.executeQuery(jpaQueryExecutor);
            if(!CollectionUtils.isEmpty(excludedTableMetaData)){
                excludedTableMetaData.forEach(res->{
                    if(res.get("TableName").toString().equalsIgnoreCase("All_TABLE")){
                        excludedColumnForSuperSet = new ArrayList(Arrays.asList(res.get("columnNames").toString().split(",")));
                    }
                });
            }
        }

        if(response!=null){
            Map<String, Set<String>> extractedData = new HashMap<>();
            Map<String, String> tableDetail = response.getTableDetail();
            Map<String, Set<Tuple_2>> tableSelectColumn = response.getTableSelectColumn();
            Map<String, Set<String>> tableWhereColumn = response.getTableWhereColumn();
            if(MapUtils.isNotEmpty(tableDetail)){
                for(Map.Entry m : tableDetail.entrySet()){
                    Set<String> set = new HashSet<>();
                    extractedData.put(m.getValue().toString(),set);
                }
            }
            if(MapUtils.isNotEmpty(tableSelectColumn)){
                extractedData.keySet().forEach(ks->{
                    if(tableSelectColumn.containsKey(ks)){
                        Set<String> columns = new HashSet<>();
                        tableSelectColumn.get(ks).forEach(singleTuple_2->{
                            columns.add(singleTuple_2.get_1().toUpperCase());
                        });
                        extractedData.put(ks,columns);
                    }
                });
            }
            if(MapUtils.isNotEmpty(tableWhereColumn)){
                extractedData.keySet().forEach(ks->{
                    if(tableWhereColumn.containsKey(ks)){
                        Set<String> set = extractedData.get(ks);
                        Set<String> set1 = new HashSet<>();
                        tableWhereColumn.get(ks).forEach(cc->{
                            set1.add(cc.toUpperCase());
                        });
                        set.addAll(set1);
                    }
                });
            }
            checkForExclusion(response, stringBuilder, extractedData);
        }
        return stringBuilder.toString();
    }

    private void checkForExclusion(SQLParseResponse response, StringBuilder stringBuilder, Map<String, Set<String>> extractedData) {
        if(MapUtils.isNotEmpty(extractedData) && org.apache.commons.collections4.CollectionUtils.isNotEmpty(excludedTableMetaData)){
            for(Map.Entry m : extractedData.entrySet()){
                String tableName = response.getTableNameByAlias(m.getKey().toString());
                excludedTableMetaData.forEach(res->{
                    if(res.get("TableName").toString().equalsIgnoreCase(tableName)){
                        List list = new ArrayList(Arrays.asList(res.get(COLUMN_NAMES).toString().split(",")));
                        if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(list)){
                            if(list.size()==1 && list.contains("All_COLUMNS")){
                                if(stringBuilder.length()==0){
                                    stringBuilder.append(FALSE);
                                }
                                if(stringBuilder.length()>0){
                                    stringBuilder.append(",");
                                }
                                stringBuilder.append(getMessageAgainstKey("label.invalid.expression.table.excluded",Locale.getDefault())+tableName);
                            }else{
                                Set<String> usedColumn = (Set<String>) m.getValue();
                                usedColumn.forEach(singleColumn->{
                                    if(list.contains(singleColumn) || excludedColumnForSuperSet.contains(singleColumn)){
                                        if(stringBuilder.length()==0){
                                            stringBuilder.append(FALSE);
                                        }
                                        if(stringBuilder.length()>0){
                                            stringBuilder.append(",");
                                        }
                                        stringBuilder.append(TABLE_NAME +tableName+ COLUMN_EXCLUDED +singleColumn);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        }
        if(response!=null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(response.getWhereInjections())){
            response.getWhereInjections().forEach(w->{
                if(stringBuilder.length()==0){
                    stringBuilder.append(FALSE);
                }
                if(stringBuilder.length()>0){
                    stringBuilder.append(",");
                }
                stringBuilder.append(getMessageAgainstKey("label.invalid.expression.where.bothside",Locale.getDefault())+w);
            });
        }
        if(response!=null && org.apache.commons.collections4.CollectionUtils.isNotEmpty(response.getWhereUsedConstants())){
            response.getWhereUsedConstants().forEach(w->{
                if(stringBuilder.length()==0){
                    stringBuilder.append(FALSE);
                }
                if(stringBuilder.length()>0){
                    stringBuilder.append(",");
                }
                stringBuilder.append(getMessageAgainstKey("label.invalid.expression.constant",Locale.getDefault())+w);
            });
        }
    }

    private static void parseSelectStatement(PlainSelect select, SQLParseResponse response) {
        FromItem fromItem = select.getFromItem();
        parseFromItem(select, response, fromItem);
    }

    private void checkForAliasPresent(PlainSelect select, List<String> errorList){
        FromItem fromItem = select.getFromItem();
        if(fromItem instanceof Table){
            Table fromTable = (Table) fromItem;
            String alias = fromTable.getAlias();
            if(alias == null || alias.isEmpty()){
                errorList.add(RuleConstants.ALIAS+fromTable.getName());
            }
            List<Join> jointable = select.getJoins();
            if(org.apache.commons.collections4.CollectionUtils.isNotEmpty(jointable)){
                for(Join join : jointable){
                    FromItem innerFromItem = join.getRightItem();
                    if(innerFromItem instanceof Table) {
                        if (innerFromItem.getAlias() == null || innerFromItem.getAlias().isEmpty())
                            errorList.add(RuleConstants.ALIAS + ((Table) innerFromItem).getName());
                    }else if (innerFromItem instanceof PlainSelect) {
                        checkForAliasPresent((PlainSelect) innerFromItem, errorList);
                    }
                }
            }
        }else if(fromItem instanceof SubSelect){
            SubSelect subSelectFrom = (SubSelect) fromItem;
            SelectBody selBocy = subSelectFrom.getSelectBody();
            if (selBocy instanceof PlainSelect) {
                checkForAliasPresent((PlainSelect) selBocy, errorList);
            }
        }
    }

    private static void parseFromItem(PlainSelect select, SQLParseResponse response, FromItem fromItem) {
        // parsing select items
        List<SelectItem> selItems = select.getSelectItems();
        if (!CollectionUtils.isEmpty(selItems)) {
            selItems.forEach((sel) -> {
                if (sel instanceof SelectExpressionItem) {
                    parseSelectExpression(((SelectExpressionItem) sel).getExpression(),((SelectExpressionItem) sel).getAlias(), response);
                }
            });
        }

        if (fromItem instanceof Table) {
            // parsing from clause
            Table fromTable = (Table) fromItem;
            String alias = fromTable.getAlias();
            String tableName = fromTable.getName();
            response.addTableDetail(tableName, alias);
            List<Join> jointable = select.getJoins();
            // extracting data of table in join and using which column this join
            if (!CollectionUtils.isEmpty(jointable)) {
                for (Join join : jointable) {
                    Expression joinExp = join.getOnExpression();
                    parseWhereExpression(joinExp, response);
                    // Join tables
                    FromItem innerFromItem = join.getRightItem();
                    if (innerFromItem instanceof Table) {
                        parseTable((Table) innerFromItem, response);
                    } else if (innerFromItem instanceof PlainSelect) {
                        parseSelectStatement((PlainSelect) innerFromItem, response);
                    }
                    populateColumnData(response, tableName, join.getUsingColumns(), true, false);
                }
            }
            // extracting where clause
            Expression where = select.getWhere();
            parseWhereExpression(where, response);
            //having clause
            Expression having = select.getHaving();
            if(having!=null){
                parseWhereExpression(having, response);
            }
            // group by claus
            List<Expression> groupBys = select.getGroupByColumnReferences();
            if(!CollectionUtils.isEmpty(groupBys)){
                groupBys.forEach((gr)->{
                    parseWhereExpression(gr, response);
                });
            }

        } else if (fromItem instanceof SubSelect) {
            // parsing select
            SubSelect subSelectFrom = (SubSelect) fromItem;
            SelectBody selBocy = subSelectFrom.getSelectBody();
            if (selBocy instanceof PlainSelect) {
                parseSelectStatement((PlainSelect) selBocy, response);
            } else {
                BaseLoggers.webLogger.error("Unknown select Body" + selBocy.toString());
            }
        }
    }

    private static void parseWhereExpression(Expression exp, SQLParseResponse response) {
        parseExpression(exp, response, true, false,null);
    }

    private static void parseSelectExpression(Expression exp,String alias, SQLParseResponse response) {
        parseExpression(exp, response, false, true,alias);
    }

    private static void parseExpression(Expression exp, SQLParseResponse response, boolean isWhere, boolean isSelect,
                                        String alias) {
        // logical expression
        if (exp instanceof BinaryExpression) {
            BinaryExpression andExp = (BinaryExpression) exp;
            if(isWhere){
                Expression leftExp = andExp.getLeftExpression();
                Expression rightExp = andExp.getRightExpression();
                if((leftExp instanceof LongValue || leftExp instanceof StringValue || leftExp instanceof DoubleValue) && (rightExp instanceof LongValue || rightExp instanceof StringValue|| rightExp instanceof DoubleValue) ){
                    response.addWhereInjections(leftExp.toString()+" AND "+rightExp.toString());
                }
            }
            Expression leftExp = andExp.getLeftExpression();
            parseExpression(leftExp, response, isWhere, isSelect,null);
            Expression rightExp = andExp.getRightExpression();
            parseExpression(rightExp, response, isWhere, isSelect,null);
        } // mathametic expression
        else if (exp instanceof IsNullExpression) {
            IsNullExpression expIsNull = (IsNullExpression)exp;
            parseExpression(expIsNull.getLeftExpression(), response, isWhere, isSelect, null);
        }else if (exp instanceof InExpression) {
            InExpression inexp =	(InExpression)exp;
            ItemsList items = inexp.getItemsList();
            if(items instanceof ExpressionList){
                ExpressionList exps = (ExpressionList)inexp.getItemsList();
                exps.getExpressions().forEach((e)->{
                    parseExpression((Expression) e, response, isWhere, isSelect, null);
                });
            }
        } else if (exp instanceof Between) {
            Between expBetween = (Between)exp;
            Expression start = expBetween.getBetweenExpressionStart();
            parseExpression(start, response, isWhere, isSelect, null);
            Expression end = expBetween.getBetweenExpressionEnd();
            parseExpression(end, response, isWhere, isSelect, null);
        } else if (exp instanceof Column) {
            Column column = (Column) exp;
            populateColumn(response, column, isWhere, isSelect);
        } else if (exp instanceof SubSelect) {
            SubSelect subSelct = (SubSelect) exp;
            SelectBody selectBody = subSelct.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                parseSelectStatement((PlainSelect) selectBody, response);
            } else {
                BaseLoggers.webLogger.error("Invalid Sub Select :" + selectBody.toString());
            }
        } else if (exp instanceof Function) {
            if(alias !=null){
                response.addComplexAlias(alias, exp.toString());
            }
            Function fu = (Function) exp;
            response.addDbFunctions(fu.getName());
            ExpressionList exps = fu.getParameters();
            exps.getExpressions().forEach((ex) -> {
                parseExpression((Expression)ex, response, isWhere, isSelect,null);
            });
        }
        else if(exp instanceof CaseExpression){
            parseCaseExpression((CaseExpression)exp,response,isWhere, isSelect);
            if(alias !=null){
                response.addComplexAlias(alias, exp.toString());
            }
        }
        else if(exp instanceof WhenClause){
            parseWhenClause((WhenClause)exp,response,isWhere,isSelect);
        }
        else if(exp instanceof LongValue){
            if(isWhere){
                response.addWhereUsedConstants(exp.toString());
            }else{
                response.addUsedConstants(exp.toString());
            }
        }
        else if(exp instanceof StringValue){
            if(isWhere){
                response.addWhereUsedConstants(exp.toString());
            }else{
                response.addUsedConstants(exp.toString());
            }
        }
        else if(exp instanceof DoubleValue){
            if(isWhere){
                response.addWhereUsedConstants(exp.toString());
            }else{
                response.addUsedConstants(exp.toString());
            }
        }

    }

    private static void parseWhenClause(WhenClause exp, SQLParseResponse response, boolean isWhere, boolean isSelect) {
        Expression whenExpression = exp.getWhenExpression();
        parseExpression(whenExpression,response,isWhere,isSelect,null);
    }

    private static void parseCaseExpression(CaseExpression exp, SQLParseResponse response, boolean isWhere, boolean isSelect){
        Expression switchExpression = exp.getSwitchExpression();
        List whenClauses = exp.getWhenClauses();
        Expression elseExpression = exp.getElseExpression();
        if(!CollectionUtils.isEmpty(whenClauses)){
            whenClauses.forEach(when->{
                parseExpression((Expression)when, response, isWhere, isSelect, null);
            });
        }
        if(elseExpression!=null){
            parseExpression(elseExpression, response, isWhere, isSelect, null);
        }
    }

    private static void populateColumnData(SQLParseResponse response, String tableName, List<Column> cols,
                                           boolean isWhere, Boolean isSelect) {
        if (!CollectionUtils.isEmpty(cols)) {
            for (Column column : cols) {
                populateColumn(response, column, isWhere, isSelect);
            }
        }
    }

    private static void populateColumn(SQLParseResponse response, Column column, boolean isWhere, Boolean isSelect) {
        String colName = column.getColumnName();
        Table table = column.getTable();
        if (table == null || table.getName() == null) {
            if (isWhere) {
                response.addWhereColumnNameWithoutTable(colName);
            } else if (isSelect) {
                response.addSelectColumnNameWithoutTable(colName);
            }
        } else {
            if (isWhere) {
                response.addTableWhereColumn(table.getName(), colName);
            } else if (isSelect) {
                response.addTableSelectColumn(table.getName(), colName);
            }
        }
    }

    private static void parseTable(Table t, SQLParseResponse respose) {
        respose.addTableDetail(t.getName(), t.getAlias() != null ? t.getAlias() : null);
    }

    private String getMessageAgainstKey(String key, Locale locale) {
        String message = "";

        if (null != key && !key.equals("")) {
            message = messageSource.getMessage(key, null, key, locale);
        }
        return message;
    }
}
