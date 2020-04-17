/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.dynamicQuery.service;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import net.bull.javamelody.MonitoredWithSpring;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.hql.internal.antlr.HqlBaseLexer;
import org.hibernate.hql.internal.antlr.HqlTokenTypes;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;

import antlr.Token;
import antlr.TokenStreamException;

import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.entity.QueryTokenValue;
import com.nucleus.core.exceptions.SystemException;
import com.nucleus.core.formsConfiguration.FieldDefinition;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.event.Event;
import com.nucleus.event.EventService;
import com.nucleus.event.EventTypes;
import com.nucleus.event.UserSecurityTrailEvent;
import com.nucleus.persistence.EntityDao;
import com.nucleus.service.BaseServiceImpl;
import com.nucleus.user.UserService;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Named("dynamicQueryMetadataService")
@MonitoredWithSpring(name = "dynamicQueryMetadataService_IMPL_")
public class DynamicQueryMetadataServiceImpl extends BaseServiceImpl implements DynamicQueryMetadataService {

    private static final String       TOKEN_BY_NAME_AND_CONTEXT_ID_QUERY             = "select qt from QueryContext qc left join qc.queryTokens qt where qc.id=:contextId and qt.tokenName=:tokenName";
    private static final String       ALL_TOKENS_BY_CONTEXT_CODE_QUERY               = "select qt from QueryContext qc left join qc.queryTokens qt where qc.queryCode=:contextCode";
    private static final String       ALL_WHERE_TYPE_TOKEN_NAMES_BY_CONTEXT_ID_QUERY = "select qt.tokenName from QueryContext qc left join qc.queryTokens qt where qc.id=:contextId and qt.tokenType in :tokenTypes";

    private static final String       ALL_TOKENS_BY_CONTEXT_ID_AND_TYPE_QUERY        = "select qt from QueryContext qc left join qc.queryTokens qt where qc.id=:contextId and qt.tokenType in :tokenTypes";

    private static final List<String> DATE_TIME_VALUES                               = Collections.unmodifiableList(Arrays
                                                                                             .asList("now()",
                                                                                                     "startOfDay()",
                                                                                                     "startOfWeek()",
                                                                                                     "startOfMonth()",
                                                                                                     "startOfYear()",
                                                                                                     "endOfDay()",
                                                                                                     "endOfWeek()",
                                                                                                     "endOfMonth()",
                                                                                                     "endOfYear()",
                                                                                                     "date(dd-MM-yyyy)",
                                                                                                     "currentLogin()",
                                                                                                     "lastLogin()"));

    private static final String       NEUTRINO_BASE_QUERY                            = "Select xyz as x from Neutrino where ";

    private static final String       TOKEN_VALUE_QUERY                              = "select en.%s from  %s en";
    private static final String       TOKEN_VALUE_QUERY_LIKE                         = "select en.%s from  %s en where en.%s like :searchTerm";

    private static final List<String> OPERATORS_FOR_STRING_TYPE                      = Arrays.asList("=", "!=", "is NULL",
                                                                                             "is not NULL", "in", "not in");

    private static final List<String> OPERATORS_FOR_NUMBER_TYPE                      = Arrays.asList("=", "!=", "<", ">",
                                                                                             "<=", ">=", "between",
                                                                                             "not between", "is NULL",
                                                                                             "is not NULL", "in", "not in");

    private static final List<String> OPERATORS_FOR_BOOLEAN_TYPE                     = Arrays.asList("=", "!=", "is NULL",
                                                                                             "is not NULL");

    private static final String       DYNAMIC_FORM_ID_NAME_VERSION_QUERY             = "select new Map(mmd.uiMetaData.id as id,concat(mmd.uiMetaData.formName, '_', mmd.uiMetaData.formVersion) as nameVersion)  from FormConfigurationMapping mmd " +
                                                                                        " where mmd.masterLifeCycleData.approvalStatus IN (0,3,4,6) AND mmd.activeFlag = true";

    private static final String       NON_TABLE_FIELD_DEFINITION_QUERY               = "select fd from FormConfigurationMapping umd join umd.uiMetaData.panelDefinitionList pd join pd.fieldDefinitionList fd where umd.uiMetaData.id=:formId and pd.panelType in :panelType) " +
                                                                                        " AND umd.masterLifeCycleData.approvalStatus IN (0,3,4,6) AND umd.activeFlag = true";

    private static final String       FIELD_DEFINITION_BY_IDs_QUERY                  = "select fdl.id,fdl.fieldKey,fdl.fieldDataType from FieldDefinition fdl where  fdl.id in :fieldDefIds";

    @Inject
    @Named(value = "entityDao")
    EntityDao                         entityDao;

    @Inject
    @Named("eventService")
    private EventService              eventService;

    @Inject
    @Named("userService")
    private UserService               userService;

    @Override
    public QueryToken findTokenByNameAndContextId(String tokenName, Long contextId) {

        JPAQueryExecutor<QueryToken> jpaQueryExecutor = new JPAQueryExecutor<QueryToken>(TOKEN_BY_NAME_AND_CONTEXT_ID_QUERY);
        jpaQueryExecutor.addParameter("contextId", contextId);
        jpaQueryExecutor.addParameter("tokenName", tokenName);
        List<QueryToken> queryTokens = entityDao.executeQuery(jpaQueryExecutor);
        if (queryTokens != null && queryTokens.size() == 1) {
            return queryTokens.get(0);
        }
        return null;

    }

    @Override
    public List<QueryToken> findAllTokensByContextCode(String contextCode) {

        JPAQueryExecutor<QueryToken> jpaQueryExecutor = new JPAQueryExecutor<QueryToken>(ALL_TOKENS_BY_CONTEXT_CODE_QUERY);
        jpaQueryExecutor.addParameter("contextCode", contextCode);
        List<QueryToken> queryTokens = entityDao.executeQuery(jpaQueryExecutor);
        return queryTokens;
    }

    @Override
    public List<String> findAllWhereClauseTokenNamesByContextId(Long contextId) {
        JPAQueryExecutor<String> jpaQueryExecutor = new JPAQueryExecutor<String>(
                ALL_WHERE_TYPE_TOKEN_NAMES_BY_CONTEXT_ID_QUERY);
        jpaQueryExecutor.addParameter("contextId", contextId)
        				.addParameter("tokenTypes", Arrays.asList(1,2));        
        List<String> queryTokens = entityDao.executeQuery(jpaQueryExecutor);
        queryTokens.removeAll(Collections.singleton(null));
        return queryTokens;
    }

    @Override
    public Object getDateTimeValueForTokenValue(String dateTimeTokenValue) {
        String[] values = dateTimeTokenValue.split(":");
        if (values != null && values.length == 2) {
            if (QueryToken.LOCAL_DATE.equalsIgnoreCase(values[0])) {
                return getDateTimeValue(values[1]).toLocalDate();
            }
            if (QueryToken.CALENDAR.equalsIgnoreCase(values[0])) {
                return getDateTimeValue(values[1]).toGregorianCalendar();
            }
        }
        return getDateTimeValue(values[1]);
    }

    @Override
    public List<String> getDateTimeDisplayValues() {
        return DATE_TIME_VALUES;
    }

    @Override
    public String getBaseQuery() {
        return NEUTRINO_BASE_QUERY;
    }

    @Override
    public List<QueryToken> getAllTokensWithContextIdAndType(Long contextId, List<Integer> tokenTypes) {

        if (contextId != null && tokenTypes != null && !tokenTypes.isEmpty()) {
            JPAQueryExecutor<QueryToken> jpaQueryExecutor = new JPAQueryExecutor<QueryToken>(
                    ALL_TOKENS_BY_CONTEXT_ID_AND_TYPE_QUERY);
            jpaQueryExecutor.addParameter("contextId", contextId);
            jpaQueryExecutor.addParameter("tokenTypes", tokenTypes);
            List<QueryToken> queryTokens = entityDao.executeQuery(jpaQueryExecutor);
            return queryTokens;
        }
        return new ArrayList<QueryToken>();
    }

    @Override
    public Map<Long, String> getAllTokensIdNameMapWithContextIdAndType(Long contextId, List<Integer> tokenTypes) {

        Map<Long, String> map = new HashMap<Long, String>();
        List<QueryToken> queryTokens = getAllTokensWithContextIdAndType(contextId, tokenTypes);
        if (queryTokens != null && !queryTokens.isEmpty()) {

            for (QueryToken queryToken : queryTokens) {
                if (queryToken.isToken()) {
                    map.put(queryToken.getId(), queryToken.getTokenName());
                } else {
                    map.put(queryToken.getId(), queryToken.getPropertyEntityName());
                }

            }
        }

        return map;
    }

    @Override
    public Map<Long, QueryToken> getIdTokenMapWithContextIdAndType(Long contextId, List<Integer> tokenTypes) {

        Map<Long, QueryToken> map = new HashMap<Long, QueryToken>();
        List<QueryToken> queryTokens = getAllTokensWithContextIdAndType(contextId, tokenTypes);
        if (queryTokens != null && !queryTokens.isEmpty()) {

            for (QueryToken queryToken : queryTokens) {
                map.put(queryToken.getId(), queryToken);

            }
        }
        return map;
    }

    public List<String> getAutocompleteProposals(String hql, Long contextId, String searchTerm) {

        List<Token> tokens;
        try {
            tokens = getAllTokens(hql);
        } catch (TokenStreamException e) {
            throw new SystemException("Error in processing autocomplete for dynamic query", e);
        }
        int size = tokens.size();

        Token lastToken = tokens.get(size - 1);
        int tokenId = lastToken.getType();

        // complete---> in/not in (val1,val2.....)
        if (tokenId == HqlTokenTypes.IN) {
            return Arrays.asList("(");
        }

        if (tokenId == HqlTokenTypes.OPEN && tokens.get(size - 2).getType() == HqlTokenTypes.IN) {

            Token queryTokenToken = null;
            if (tokens.get(size - 3).getType() == HqlTokenTypes.NOT) {
                queryTokenToken = tokens.get(size - 4);
            } else {
                queryTokenToken = tokens.get(size - 3);
            }
            if (queryTokenToken != null && queryTokenToken.getType() == HqlTokenTypes.IDENT) {
                QueryToken queryToken1 = findTokenByNameAndContextId(queryTokenToken.getText(), contextId);
                return returnValuesForQueryToken(queryToken1, searchTerm, false);
            }
            return null;
        }

        if (tokenId == HqlTokenTypes.QUOTED_STRING || tokenId == HqlTokenTypes.NUM_DOUBLE
                || tokenId == HqlTokenTypes.NUM_INT || tokenId == HqlTokenTypes.NULL || tokenId == HqlTokenTypes.COMMA) {

            Token targetToken = null;
            for (int j = 2 ; j < size ; j++) {
                int tokenId2 = tokens.get(size - j).getType();
                if (tokenId2 == HqlTokenTypes.QUOTED_STRING || tokenId2 == HqlTokenTypes.NUM_DOUBLE
                        || tokenId2 == HqlTokenTypes.NUM_INT || tokenId2 == HqlTokenTypes.NULL
                        || tokenId2 == HqlTokenTypes.COMMA) {
                    continue;
                } else if (tokenId2 == HqlTokenTypes.OPEN && tokens.get(size - (j + 1)).getType() == HqlTokenTypes.IN) {

                    if (tokens.get(size - (j + 2)).getType() == HqlTokenTypes.NOT) {
                        targetToken = tokens.get(size - (j + 3));
                    } else {
                        targetToken = tokens.get(size - (j + 2));
                    }
                    if (targetToken != null && targetToken.getType() == HqlTokenTypes.IDENT) {
                        QueryToken queryToken1 = findTokenByNameAndContextId(targetToken.getText(), contextId);
                        List<String> vals = returnValuesForQueryToken(queryToken1, searchTerm, true);
                        vals.add(")");
                        return vals;
                    }
                    break;
                } else {
                    break;
                }
            }

            // don't return here as we need to give chance to others like //
            // logical operators // complete---> BETWEEN start
            // AND end2;
        }

        // complete------------------------------> BETWEEN start AND end
        if (tokenId == HqlTokenTypes.AND) {
            Token prevToken = tokens.get(size - 3);
            if (prevToken.getType() == HqlTokenTypes.BETWEEN || prevToken.getType() == HqlTokenTypes.NOT_BETWEEN) {
                Token prevToken2 = tokens.get(size - 4);
                if (prevToken2.getType() == HqlTokenTypes.NOT) {
                    prevToken2 = tokens.get(size - 5);
                }

                QueryToken queryToken = findTokenByNameAndContextId(prevToken2.getText(), contextId);
                return returnValuesForQueryToken(queryToken, searchTerm, false);
            }
        }
        // complete---> BETWEEN start AND end2
        if (tokenId == HqlTokenTypes.QUOTED_STRING || tokenId == HqlTokenTypes.NUM_DOUBLE
                || tokenId == HqlTokenTypes.NUM_INT || tokenId == HqlTokenTypes.NULL) {
            Token prevToken = tokens.get(size - 2);
            if (prevToken.getType() == HqlTokenTypes.BETWEEN || prevToken.getType() == HqlTokenTypes.NOT_BETWEEN) {
                return Arrays.asList("AND");
            }
        }

        // show columns
        if (tokenId == HqlTokenTypes.WHERE || tokenId == HqlTokenTypes.OR || tokenId == HqlTokenTypes.AND) {

            return returnQueryTokensForContext(contextId);
            /*
             * return Arrays.asList("CREATE_DATE", "LEAD_STATUS",
             * "LEAD_CON_POTENTIAL", "PRODUCT_TYPE", "CHANNEL", "ASSIGNEE",
             * "APPLICATION_TYPE", "APPLICATION_STATE", "BRANCH");
             */
        }

        // show operators
        if (tokenId == HqlTokenTypes.IDENT) {

            QueryToken queryToken = findTokenByNameAndContextId(lastToken.getText(), contextId);
            return returnOperatorsForQueryToken(queryToken);
            /*
             * return Arrays.asList("=", "!=", "<", ">", "<=", ">=", "is",
             * "between", "is NULL");
             */
        }

        // show values
        if (tokenId == HqlTokenTypes.EQ || tokenId == HqlTokenTypes.NE || tokenId == HqlTokenTypes.LT
                || tokenId == HqlTokenTypes.GT || tokenId == HqlTokenTypes.LE || tokenId == HqlTokenTypes.GE
                || tokenId == HqlTokenTypes.IN || tokenId == HqlTokenTypes.BETWEEN || tokenId == HqlTokenTypes.LIKE
                || tokenId == HqlTokenTypes.NOT_IN || tokenId == HqlTokenTypes.NOT_BETWEEN) {

            Token prevToken = tokens.get(size - 2);
            if (prevToken.getType() == HqlTokenTypes.NOT) {
                prevToken = tokens.get(size - 3);
            }
            QueryToken queryToken = findTokenByNameAndContextId(prevToken.getText(), contextId);
            return returnValuesForQueryToken(queryToken, searchTerm, false);
        }

        // logical operators
        if (tokenId == HqlTokenTypes.QUOTED_STRING || tokenId == HqlTokenTypes.CLOSE || tokenId == HqlTokenTypes.NUM_DOUBLE
                || tokenId == HqlTokenTypes.NUM_INT || tokenId == HqlTokenTypes.NULL) {
            return Arrays.asList("OR", "AND");
        }
        return null;
    }

    private List<String> returnQueryTokensForContext(Long contextId) {
        return findAllWhereClauseTokenNamesByContextId(contextId);
    }

    private List<String> returnOperatorsForQueryToken(QueryToken queryToken) {

        if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.STRING)) {
            return OPERATORS_FOR_STRING_TYPE;
        }
        if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.NUMBER)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.FLOAT)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.MONEY)) {
            return OPERATORS_FOR_NUMBER_TYPE;
        }
        if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.DATE_TIME)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.LOCAL_DATE)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.CALENDAR)) {
            return OPERATORS_FOR_NUMBER_TYPE;
        }
        if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.BOOLEAN)) {
            return OPERATORS_FOR_BOOLEAN_TYPE;
        }
        return null;
    }

    private List<String> returnValuesForQueryToken(QueryToken queryToken, String searchTerm, boolean prependComma) {
        String prependChar = prependComma ? "," : "";

        List<String> values = new ArrayList<String>();

        // do we need to fetch values for this token from DB with query
        if (queryToken.isFetchValues()) {
            List<String> displayValues = getValueForEntityAndColumn(queryToken, searchTerm);
            if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.STRING)) {
                for (String tokenValue : displayValues) {
                    values.add(prependChar.concat("'" + tokenValue + "'"));
                }
            } else {
                if (!prependComma) {
                    values.addAll(displayValues);
                } else {
                    for (String tokenValue : displayValues) {
                        values.add(prependChar.concat(tokenValue));
                    }
                }
            }
        } else {

            if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.STRING)) {
                for (QueryTokenValue tokenValue : queryToken.getQueryTokenValues()) {
                    values.add(prependChar.concat("'" + tokenValue.getDisplayName() + "'"));
                }
            } else if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.NUMBER)
                    || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.FLOAT)
                    || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.MONEY)) {
                for (QueryTokenValue tokenValue : queryToken.getQueryTokenValues()) {
                    values.add(prependChar.concat(tokenValue.getDisplayName()));
                }
            } else if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.DATE_TIME)
                    || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.LOCAL_DATE)
                    || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.CALENDAR)) {
                for (String tokenValue : getDateTimeDisplayValues()) {
                    values.add(prependChar.concat("'" + tokenValue + "'"));
                }
            } else if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.BOOLEAN)) {
                values.add(prependChar.concat("'TRUE'"));
                values.add(prependChar.concat("'FALSE'"));
            }
        }
        return values;
}

    private List<String> getValueForEntityAndColumn(QueryToken queryToken, String searchTerm) {

        if (searchTerm != null && !StringUtils.containsWhitespace(searchTerm) && !searchTerm.endsWith("'")) {
            searchTerm = searchTerm.trim();
            if (searchTerm.startsWith("'")) {
                searchTerm = searchTerm.substring(1);
            }
        } else {
            searchTerm = "";
        }

        if (!searchTerm.isEmpty()) {
            String finalQuery = null;
            // check if it is explicitly provided from which entity and column
            // to fetch values
            if (queryToken.getFetchEntity() == null || queryToken.getFetchEntity().isEmpty()) {
                finalQuery = String.format(TOKEN_VALUE_QUERY_LIKE, queryToken.getTokenProertyName(), queryToken.getOwner()
                        .getPropertyEntityName(), queryToken.getTokenProertyName());
            } else {
                finalQuery = String.format(TOKEN_VALUE_QUERY_LIKE, queryToken.getFetchColumn(), queryToken.getFetchEntity(),
                        queryToken.getFetchColumn());
            }
            JPAQueryExecutor<String> queryExecutor = new JPAQueryExecutor<String>(finalQuery);
            queryExecutor.addParameter("searchTerm", searchTerm.concat("%"));
            return entityDao.executeQuery(queryExecutor, 0, 20);
        } else {
            String finalQuery = null;
            // check if it is explicitly provided from which entity and column
            // to fetch values
            if (queryToken.getFetchEntity() == null || queryToken.getFetchEntity().isEmpty()) {
                finalQuery = String.format(TOKEN_VALUE_QUERY, queryToken.getTokenProertyName(), queryToken.getOwner()
                        .getPropertyEntityName());
            } else {
                finalQuery = String.format(TOKEN_VALUE_QUERY, queryToken.getFetchColumn(), queryToken.getFetchEntity());
            }

            JPAQueryExecutor<String> queryExecutor = new JPAQueryExecutor<String>(finalQuery);
            return entityDao.executeQuery(queryExecutor, 0, 20);
        }
    }

    // ==================

    private HqlBaseLexer baseLexer(String hql) {
        HqlBaseLexer baseLexer = new HqlBaseLexer(new CharArrayReader(hql.toCharArray(), 0, hql.length())) {
            public void newline() {
                // super.newline();
            }

            public int getColumn() {
                return super.getColumn() - 1;
            }
        };
        baseLexer.setTabSize(1);
        return baseLexer;
    }

    List<Token> getAllTokens(String hql) throws TokenStreamException {

        HqlBaseLexer baseLexer = baseLexer(hql);
        List<Token> tokens = new ArrayList<Token>();
        Token token = baseLexer.nextToken();
        while ((token.getType()) != HqlTokenTypes.EOF) {
            tokens.add(token);
            token = baseLexer.nextToken();
        }
        return tokens;
    }

    // FOR dynamic form based reports
    @Override
    public List<Map<String, Object>> getAllDynamicFormIdNameVersionMap() {

        JPAQueryExecutor<Map<String, Object>> jpaQueryExecutor = new JPAQueryExecutor<Map<String, Object>>(
                DYNAMIC_FORM_ID_NAME_VERSION_QUERY);

        List<Map<String, Object>> dynamicFormIdNameMaps = entityDao.executeQuery(jpaQueryExecutor);

        return dynamicFormIdNameMaps;

    }

    @Override
    public List<FieldDefinition> getDynamicFormFieldsByFormId(Long dynamicFormId) {

        JPAQueryExecutor<FieldDefinition> jpaQueryExecutor = new JPAQueryExecutor<FieldDefinition>(
                NON_TABLE_FIELD_DEFINITION_QUERY);

        jpaQueryExecutor.addParameter("formId", dynamicFormId)
        				.addParameter("panelType",Arrays.asList(0,2,3));
        List<FieldDefinition> fieldDefinitions = entityDao.executeQuery(jpaQueryExecutor);

        return fieldDefinitions;
    }

    // fieldId--->fieldId,Key,dataType
    @Override
    public Map<Long, Object[]> getDynamicFormFieldsByIds(Set<Long> ids) {

        JPAQueryExecutor<Object[]> jpaQueryExecutor = new JPAQueryExecutor<Object[]>(FIELD_DEFINITION_BY_IDs_QUERY);

        jpaQueryExecutor.addParameter("fieldDefIds", ids);
        List<Object[]> fieldDefinitions = entityDao.executeQuery(jpaQueryExecutor);

        Map<Long, Object[]> map = new HashMap<Long, Object[]>();
        for (Object[] objects : fieldDefinitions) {
            map.put((Long) objects[0], objects);

        }

        return map;
    }
    
    private DateTime getDateTimeValue(String dateTimeTokenValue) {

        int index = DATE_TIME_VALUES.indexOf(dateTimeTokenValue);
        if (index < 0 && dateTimeTokenValue.startsWith("date(")) {
            index = 9;
        }
        switch (index) {
            case 0:
                return DateTime.now().toDateTime(DateTimeZone.UTC);
            case 1:
                return new DateTime().millisOfDay().withMinimumValue().toDateTime(DateTimeZone.UTC);
            case 2:
                return new DateTime().dayOfWeek().withMinimumValue().withTimeAtStartOfDay().toDateTime(DateTimeZone.UTC);
            case 3:
                return new DateTime().dayOfMonth().withMinimumValue().withTimeAtStartOfDay().toDateTime(DateTimeZone.UTC);
            case 4:
                return new DateTime().dayOfYear().withMinimumValue().withTimeAtStartOfDay().toDateTime(DateTimeZone.UTC);
            case 5:
                return new DateTime().millisOfDay().withMaximumValue().toDateTime(DateTimeZone.UTC);
            case 6:
                return new DateTime().dayOfWeek().withMaximumValue().millisOfDay().withMaximumValue()
                        .toDateTime(DateTimeZone.UTC);
            case 7:
                return new DateTime().dayOfMonth().withMaximumValue().millisOfDay().withMaximumValue()
                        .toDateTime(DateTimeZone.UTC);
            case 8:
                return new DateTime().dayOfYear().withMaximumValue().millisOfDay().withMaximumValue()
                        .toDateTime(DateTimeZone.UTC);

                // user specified date
            case 9:
                String givenVal = dateTimeTokenValue.trim();
                String dateString = givenVal.substring(givenVal.indexOf('(') + 1, givenVal.length() - 1);
                return DateTime.parse(dateString.trim(), DateTimeFormat.forPattern("dd-MM-yyyy")).toDateTime(
                        DateTimeZone.UTC);

                // current login
            case 10:
                Event event = eventService.getLastSuccessLoginEventByAssociatedUseruri(userService.getCurrentUser()
                        .getUserEntityId().getUri());
                if (event != null) {
                    UserSecurityTrailEvent lastLoginInfo = (UserSecurityTrailEvent) event;
                    return lastLoginInfo.getEventTimestamp().toDateTime(DateTimeZone.UTC);
                }

                // last login
            case 11:
                List<Event> eventList = eventService.getPaginatedEventsByTypeAndAssociatedUseruri(
                        Arrays.asList(EventTypes.USER_SECURITY_TRAIL_LOGIN_SUCCESS), userService.getCurrentUser()
                                .getUserEntityId().getUri(), 0, 2);
                if (eventList != null && !eventList.isEmpty()) {
                    UserSecurityTrailEvent lastLoginInfo = (UserSecurityTrailEvent) (eventList.size() > 1 ? eventList.get(1)
                            : eventList.get(0));
                    return lastLoginInfo.getEventTimestamp().toDateTime(DateTimeZone.UTC);
                }
            default:
                break;
        }

        throw new SystemException("Could not resolve value for dateTime constant:" + dateTimeTokenValue);
    }
}
