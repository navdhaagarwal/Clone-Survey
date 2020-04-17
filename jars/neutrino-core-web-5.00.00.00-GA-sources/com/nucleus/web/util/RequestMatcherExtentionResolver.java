package com.nucleus.web.util;

import java.util.Map;

import javax.inject.Named;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.nucleus.finnone.pro.general.util.ValidatorUtils;


/**
 * 
 * @author gajendra.jatav
 *
 */
@Named("requestMatcherExtentionResolver")
public class RequestMatcherExtentionResolver implements ApplicationListener<ContextRefreshedEvent>{

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		ApplicationContext applicationContext=event.getApplicationContext();
		Map<String, RequestMatcherConfig> requestMatcherConfig=applicationContext.getBeansOfType(RequestMatcherConfig.class);
		if(ValidatorUtils.hasAnyEntry(requestMatcherConfig)){
			requestMatcherConfig.forEach((key,matcherConfig)->{
				GenericRequestMatcher genericRequestMatcher = applicationContext
						.getBean(matcherConfig.getTargetRequestMatcher(), GenericRequestMatcher.class);
				genericRequestMatcher.setIncludedPatterns(matcherConfig.getIncludedPatterns());
				genericRequestMatcher.setExcludedPatters(matcherConfig.getExcludedPatterns());
			});
		}
	}

}
