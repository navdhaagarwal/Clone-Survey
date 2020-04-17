package com.nucleus.finnone.pro.communicationgenerator.service;

import java.util.Map;

import javax.inject.Named;

import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;

@Named("neutrinoCommPasswordProvider")
public class CommunicationPasswordProvider implements ICommunicationPasswordProvider{

    @Override
    public String computePassword(AttachmentEncryptionPolicy encryptionPolicy, CommunicationName communication,
            CommunicationTemplate communicationTemplate, Map<String, Object> dataMap) {
    	return computePassword(encryptionPolicy,dataMap);
    }
    
    @Override
    public String computePassword(AttachmentEncryptionPolicy encryptionPolicy,  Map<String, Object> dataMap) {
        StringBuilder password=new StringBuilder();
        String passwordExpression=encryptionPolicy.getPasswordExpression();
        int startVarIndex=-1;
        StringBuilder expression=new StringBuilder();
        for (int i = 0; i < passwordExpression.length(); i++){
            char c = passwordExpression.charAt(i);        
            if(c=='{')
            {
                startVarIndex=i;
            }else if(c=='}')
            {
                password.append(processExpression(expression.toString(),dataMap));
                startVarIndex=-1;
                expression.delete(0, expression.length());
            }
            else if(startVarIndex!=-1)
            {
                expression.append(c);
            }
            else
            {
                password.append(c);
            }
        }
        
        return password.toString();
    }


    private String processExpression(String expression,Map<String, Object> dataMap) {
        
        int separator=expression.indexOf(':');
        if(separator!=-1)
        {
            String value=(String)dataMap.get(expression.substring(0,separator));
            char startSide=expression.charAt(separator+1);
            int numOfCharTokeep=Integer.parseInt(expression.substring(separator+2));
            if(startSide=='f'||startSide=='F')
            {
                return value.substring(0,numOfCharTokeep);
            }
            else if(startSide=='l'||startSide=='L')
            {
                return value.substring(value.length()-numOfCharTokeep);
            }
        }
        return (String)dataMap.get(expression);
    }

}
