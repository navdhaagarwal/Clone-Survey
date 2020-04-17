package com.nucleus.web.security.servlet.api;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.web.security.FilterExcludedUrlHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by gajendra.jatav on 2/19/2020.
 */
public class UrlExcusionUtil {

    public static List<AntPathRequestMatcher> parseAndCreateRegex(String excludedUris, String currentFilterKey) {

        List<AntPathRequestMatcher> excludedUriList = new ArrayList<>();

        FilterExcludedUrlHolder filterExcludedUrlHolder = NeutrinoSpringAppContextUtil
                .getBeanByName("filterExcludedUrlHolder", FilterExcludedUrlHolder.class);

        addAllUrlToList(filterExcludedUrlHolder.getExcludeForAllFiltersList(), excludedUriList);
        Map<String, Set<String>> excludedUriMap=filterExcludedUrlHolder.getExcludedUrlMap();
        if( ValidatorUtils.hasAnyEntry(excludedUriMap))
        {
            Set<String> excludedUrisSet=excludedUriMap.get(currentFilterKey);
            addAllUrlToList(excludedUrisSet, excludedUriList);
        }

        if( StringUtils.isNoneEmpty(excludedUris))
        {
            String[] excludedUrisList=excludedUris.split(",");
            for(String uri:excludedUrisList)
            {
                excludedUriList.add(new AntPathRequestMatcher(uri));
            }
        }
        return excludedUriList;
    }

    private static void addAllUrlToList(Set<String> excludeForAllFiltersList, List<AntPathRequestMatcher> excludedUriList) {
        if(ValidatorUtils.hasNoElements(excludeForAllFiltersList))
        {
            return;
        }
        for(String uri:excludeForAllFiltersList)
        {
            excludedUriList.add(new AntPathRequestMatcher(uri));
        }
    }

    public static boolean isExcludedUri(HttpServletRequest request, List<AntPathRequestMatcher> excludedUriList) {
        for(AntPathRequestMatcher antPathRequestMatcher:excludedUriList)
        {
            if(antPathRequestMatcher.matches((HttpServletRequest) request))
            {
                return true;
            }
        }
        return false;
    }



}
