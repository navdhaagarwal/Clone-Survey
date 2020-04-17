package com.nucleus.core.web.dynamicQuery.staticBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import antlr.TokenStreamException;

import com.nucleus.core.dynamicQuery.entity.QueryToken;
import com.nucleus.core.dynamicQuery.entity.QueryTokenValue;
import com.nucleus.core.dynamicQuery.service.DynamicQueryMetadataService;
import com.nucleus.dao.query.JPAQueryExecutor;
import com.nucleus.persistence.EntityDao;
import com.nucleus.web.common.controller.BaseController;

/**
 * @author Nucleus Software Exports Limited
 * 
 */
@Controller
@RequestMapping(value = "/staticQueryBuilder")
public class StaticQueryBuilderController extends BaseController {

    private static final List<String> OPERATORS_FOR_STRING_TYPE  = Arrays.asList("equal", "not_equal", "is_null",
                                                                         "is_not_null", "in", "not_in");

    // TODO:not between pending
    private static final List<String> OPERATORS_FOR_NUMBER_TYPE  = Arrays.asList("equal", "not_equal", "less", "greater",
                                                                         "less_or_equal", "greater_or_equal", "between",
                                                                         "is_null", "is_not_null", "in", "not_in");

    private static final List<String> OPERATORS_FOR_BOOLEAN_TYPE = Arrays.asList("equal", "not_equal", "is_null",
                                                                         "is_not_null");

    private static final String       TOKEN_VALUE_QUERY          = "select en.%s from  %s en";
    private static final String       TOKEN_VALUE_QUERY_LIKE     = "select en.%s from  %s en where en.%s like :searchTerm";

    @Inject
    @Named(value = "entityDao")
    EntityDao                         entityDao;

    @Inject
    @Named(value = "dynamicQueryMetadataService")
    DynamicQueryMetadataService       dynamicQueryMetadataService;

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping("/initializeQueryBuilder/{queryContextId}")
    public @ResponseBody
    StaticQueryBuilderConfiguration initializeQueryBuilder(@PathVariable("queryContextId") Long queryContextId) {

        List<QueryToken> queryTokens = dynamicQueryMetadataService.getAllTokensWithContextIdAndType(queryContextId,
                Arrays.asList(QueryToken.WHERE_TYPE, QueryToken.BOTH));

        StaticQueryBuilderConfiguration builderConfiguration = new StaticQueryBuilderConfiguration();
        builderConfiguration.setDefault_condition("OR");
        builderConfiguration.setSortable(true);
        builderConfiguration.setConditions(Arrays.asList("AND", "OR"));

        List<StaticQueryBuilderFilter> filters = new ArrayList<StaticQueryBuilderFilter>();
        for (QueryToken singleToken : queryTokens) {
            filters.add(convertToFilter(singleToken));
        }
        builderConfiguration.setFilters(filters);

        return builderConfiguration;
    }

    @PreAuthorize("hasAuthority('AUTHORITY_DYNAMIC_QUERY')")
    @RequestMapping(value = "/contentAssistValues/{queryContextId}")
    @ResponseBody
    public Set<String> contentAssist(ModelMap map, @RequestParam("queryText") String queryTokenName,
            @PathVariable("queryContextId") Long queryContextId, @RequestParam("cursorPosition") int cursorPosition,
            @RequestParam("queryTerm") String queryTerm) throws TokenStreamException {

        QueryToken queryToken = dynamicQueryMetadataService.findTokenByNameAndContextId(queryTokenName, queryContextId);
        if (queryToken == null) {
            return new HashSet<String>();
        }

        List<String> proposals = getValueForEntityAndColumnForAutocomplete(queryToken, queryTerm);
        return proposals != null ? new HashSet<String>(proposals) : new HashSet<String>();
    }

    private StaticQueryBuilderFilter convertToFilter(QueryToken singleToken) {

        StaticQueryBuilderFilter builderFilter = new StaticQueryBuilderFilter();

        builderFilter.setId(singleToken.getTokenName());
        builderFilter.setOperators(returnOperatorsForQueryToken(singleToken));
        builderFilter.setType(getType(singleToken));
        Map<String, String> values = getValuesForToken(singleToken);
        if (values != null) {
            builderFilter.setValues(values);
            builderFilter.setInput(singleToken.getValueDisplayType().equalsIgnoreCase(QueryToken.BOOLEAN) ? "radio"
                    : "select");
        } else {
            builderFilter.setInput("text");
        }

        return builderFilter;
    }

    private String getType(QueryToken queryToken) {

        String targetType = "string";
        if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.NUMBER)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.FLOAT)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.MONEY)) {
            targetType = "integer";
        }
        return targetType;
    }

    private Map<String, String> getValuesForToken(QueryToken queryToken) {

        Map<String, String> values = null;

        // fetch values from db(not true for date,boolean)
        if (queryToken.isFetchValues()) {
            List<String> valuesList = getValuesForTokenIfLimited(queryToken);
            if (valuesList == null)
                return null;
            else {
                values = new HashMap<String, String>();
                for (String tokenValue : valuesList) {
                    values.put(tokenValue, tokenValue);
                }
            }

            // ignore date-time and numbers here as we don't want drop-downs for these types
        } else if (!(queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.DATE_TIME)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.LOCAL_DATE)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.CALENDAR)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.NUMBER)
                || queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.FLOAT) || queryToken.getValueDisplayType()
                .equalsIgnoreCase(QueryToken.MONEY))) {
            values = new HashMap<String, String>();
            if (queryToken.getValueDisplayType().equalsIgnoreCase(QueryToken.BOOLEAN)) {
                values.put("TRUE", "YES");
                values.put("FALSE", "NO");
            } else {
                for (QueryTokenValue tokenValue : queryToken.getQueryTokenValues()) {
                    values.put(tokenValue.getDisplayName(), tokenValue.getDisplayName());
                }
            }

        }

        if (values != null && values.isEmpty()) {
            values.put("0", "No Value Found");
        }
        return values;
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

    private List<String> getValuesForTokenIfLimited(QueryToken queryToken) {

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
        List<String> values = entityDao.executeQuery(queryExecutor, 0, 25);
        return (values != null && values.size() <= 22) ? values : null;
    }

    private List<String> getValueForEntityAndColumnForAutocomplete(QueryToken queryToken, String searchTerm) {

        if (searchTerm!=null && !searchTerm.isEmpty()) {
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
            return entityDao.executeQuery(queryExecutor, 0, 10);
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
            return entityDao.executeQuery(queryExecutor, 0, 10);
        }
    }
}
