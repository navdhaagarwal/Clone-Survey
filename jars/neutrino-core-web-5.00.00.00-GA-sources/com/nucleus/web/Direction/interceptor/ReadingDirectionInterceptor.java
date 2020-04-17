package com.nucleus.web.Direction.interceptor;

import static org.apache.commons.collections4.MapUtils.isNotEmpty;

import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.nucleus.core.locale.LanguageInfoReader;
import com.nucleus.core.locale.LanguageInfoVO;
import com.nucleus.logging.BaseLoggers;
public class ReadingDirectionInterceptor implements HandlerInterceptor {

    @Inject
    private LanguageInfoReader infoReader;
    public static final String DEFAULT_DIRECTION="ltr";
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {

        Locale locale = RequestContextUtils.getLocale(request);
        String localeCode = locale.getLanguage() + "_" + locale.getCountry();
        Map<String, LanguageInfoVO> languageLocales = infoReader
                .getAvailableLocaleLanguageInfoMap();
        String direction = DEFAULT_DIRECTION;
        if (isNotEmpty(languageLocales)&& languageLocales.containsKey(localeCode)) {
            direction = languageLocales.get(localeCode).getReadingDirection()
                            .toLowerCase();
        }
        checkAndUpdateAlignmentInSession(request, direction);

    }

    private void checkAndUpdateAlignmentInSession(HttpServletRequest request, String direction) {
        String currentDirection = (String) request.getSession().getAttribute("alignment");
        if (currentDirection == null || !currentDirection.equals(direction)) {
            request.getSession().setAttribute("alignment", direction);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
            HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
        BaseLoggers.flowLogger
                .debug("Alignment changed according to Language change");

    }

}
