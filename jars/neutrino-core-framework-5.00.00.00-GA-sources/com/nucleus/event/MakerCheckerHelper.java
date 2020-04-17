package com.nucleus.event;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.MessageSource;

import com.nucleus.user.UserService;

@Named("makerCheckerHelper")
public class MakerCheckerHelper {
	
	@Inject
    @Named("messageSource")
    protected MessageSource           messageSource;
	
	@Inject
    @Named("userService")
    private UserService                 userService;
	
	public String getEntityDescription(String entityName){
    	
    	
    	String entityDescriptionKey = "MASTER_ENTITYNAME_"+entityName.toUpperCase();
        String entityDescription = messageSource.getMessage(entityDescriptionKey, null, userService.getUserLocale());
        
        if(!entityDescription.equals(entityDescriptionKey)){
        	return entityDescription;
        }else{
        	return entityName;
        }
       
        
    }

}
