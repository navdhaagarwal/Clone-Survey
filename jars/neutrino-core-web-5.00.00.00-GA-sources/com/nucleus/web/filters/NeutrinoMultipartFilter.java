package com.nucleus.web.filters;

import static com.nucleus.finnone.pro.general.util.ValidatorUtils.isNull;
import static com.nucleus.finnone.pro.general.util.ValidatorUtils.notNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MultipartFilter;

import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.core.json.util.JsonUtils;
import com.nucleus.core.mutitenancy.service.MultiTenantService;
import com.nucleus.user.UserInfo;

public class NeutrinoMultipartFilter extends MultipartFilter{
	
	private static final String MAX_UPLOAD_SIZE_ERROR_CODE = "fmsg.fileSizeExceeded";
	private static final String MAX_UPLOAD_ERROR_KEY= "fileSizeExceeded";
	private static final String MULTI_TENANT_BEAN = "multiTenantService";
	private static final String USER_PROFILE = "user_profile";
	private static final String USER_LOCALE = "config.user.locale";
	private static final String UPLOAD_ERROR = "Uploaded file size is exceeding maximum allowed size ";
	private static final String KB = " KB.";
	private static final String MB = " MB.";
	private static final String UNDERSCORE = "_";
	private static final BigDecimal BASE_VALUE = new BigDecimal("1024");
	private static final BigDecimal MB_BASE_VALUE = new BigDecimal("1048576");
	private WebApplicationContext webApplicationContext;
	private MultiTenantService multiTenantService;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try{
			super.doFilterInternal(request, response, filterChain);
		}catch(MaxUploadSizeExceededException e){
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			if(isNull(webApplicationContext)){
				webApplicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getSession().getServletContext());
				multiTenantService = webApplicationContext.getBean(MULTI_TENANT_BEAN,MultiTenantService.class);
			}
			Locale locale = getLocale(request);
			String errorMessage = getErrorMessage(e,locale);
	        response.getWriter().write(JsonUtils.serializeWithoutLazyInitialization(new ImmutablePair<>(MAX_UPLOAD_ERROR_KEY, errorMessage)));
		}
	}
	private Locale getLocale(HttpServletRequest request){
		Locale locale = null;
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute(USER_PROFILE);
		if(notNull(userInfo) && notNull(userInfo.getUserPreferences())){
			ConfigurationVO configurationVO = userInfo.getUserPreferences().get(USER_LOCALE);
			if (notNull(configurationVO) && StringUtils.isNotBlank(configurationVO.getPropertyValue())) {
				String[] langRegion = configurationVO.getPropertyValue().split(UNDERSCORE);
				if (langRegion.length > 1){
					locale = new Locale(langRegion[0], langRegion[1]);
				}
				else if (langRegion.length == 1) {
					locale = new Locale(langRegion[0]);
				}
			}
		}
		if(isNull(locale)){
			locale = new Locale(multiTenantService.getDefaultTenant().getLocale());
		}
		return locale;
	}
	private String getErrorMessage(MaxUploadSizeExceededException e,Locale locale){
		String errorMessage;
		BigDecimal maxUploadSize = new BigDecimal(e.getMaxUploadSize());
		BigDecimal sizeInKBFraction = maxUploadSize.divide(MB_BASE_VALUE,15,RoundingMode.HALF_UP);
		if(sizeInKBFraction.intValue()==0){
			BigDecimal kbValue = maxUploadSize.divide(BASE_VALUE,2,RoundingMode.HALF_UP);
			errorMessage = webApplicationContext.getMessage(MAX_UPLOAD_SIZE_ERROR_CODE,new String[]{kbValue.toPlainString(),KB},UPLOAD_ERROR+kbValue+KB,locale);
		}else{
			BigDecimal mbValue = maxUploadSize.divide(MB_BASE_VALUE,2,RoundingMode.HALF_UP);
			errorMessage = webApplicationContext.getMessage(MAX_UPLOAD_SIZE_ERROR_CODE,new String[]{mbValue.toPlainString(),MB},UPLOAD_ERROR+mbValue+MB,locale);
		}
		return errorMessage;
	}
}
