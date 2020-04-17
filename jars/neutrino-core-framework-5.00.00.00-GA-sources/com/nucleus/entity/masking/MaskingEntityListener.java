package com.nucleus.entity.masking;

import javax.persistence.PostLoad;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.nucleus.core.NeutrinoSpringAppContextUtil;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.standard.context.INeutrinoExecutionContextHolder;
import com.nucleus.user.UserInfo;

/**
 * 
 * @author prakhar.varshney
 *
 */
public class MaskingEntityListener {
	private static final String NEUTRINOCONTEXTHOLDER="neutrinoExecutionContextHolder";
	private static final String FIELDMASKINGUTILITY="fieldMaskingUtility";
		
		private INeutrinoExecutionContextHolder getUserContextBean()
		{
			return NeutrinoSpringAppContextUtil
					.getBeanByName(NEUTRINOCONTEXTHOLDER, INeutrinoExecutionContextHolder.class);
		}
	
		private FieldMaskingUtility getFieldMaskingBean(){
		    return NeutrinoSpringAppContextUtil
                    .getBeanByName(FIELDMASKINGUTILITY, FieldMaskingUtility.class);
		}
		
		
		@PostLoad
		public void maskEntityAfterLoad(Object object) {
			BaseLoggers.flowLogger.debug("MaskingEntityListener called after object Load : {}",object);
			try { 
			INeutrinoExecutionContextHolder neutrinoContextHolder=getUserContextBean();
			if(neutrinoContextHolder!=null){
			UserInfo userInfo=neutrinoContextHolder.getLoggedInUserDetails();
			 if(userInfo!=null && userInfo.hasAuthority("FIELD_MASKING_ENABLED")){
			     getFieldMaskingBean().findMaskingBeanAndMaskData(object,userInfo);
           
			 }
			}
			}catch (NoSuchBeanDefinitionException e) {
	            BaseLoggers.exceptionLogger.error("No implementation is available for interface INeutrinoExecutionContextHolder moving ahead.");
	        }
		}
	
	
}
