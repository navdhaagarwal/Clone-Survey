package com.nucleus.finnone.pro.communicationgenerator.businessobject;

import java.util.Map;
import java.util.StringTokenizer;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.nucleus.finnone.pro.base.Message;
import com.nucleus.finnone.pro.base.exception.BusinessException;
import com.nucleus.finnone.pro.base.exception.ExceptionBuilder;
import com.nucleus.finnone.pro.base.utility.BeanAccessHelper;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationGeneratorConstants;
import com.nucleus.finnone.pro.communicationgenerator.constants.CommunicationType;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.AttachmentEncryptionPolicy;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationName;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationRequestDetail;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.CommunicationTemplate;
import com.nucleus.finnone.pro.communicationgenerator.domainobject.PasswordDecryptionText;
import com.nucleus.finnone.pro.communicationgenerator.service.ICommunicationPasswordProvider;
import com.nucleus.finnone.pro.communicationgenerator.vo.CommunicationGroupCriteriaVO;
import com.nucleus.finnone.pro.communicationgenerator.vo.GeneratedContentVO;
import com.nucleus.finnone.pro.general.constants.ExceptionSeverityEnum;
import com.nucleus.finnone.pro.general.util.ValidatorUtils;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.pdfutility.NeutrinoPdfUtility;

/*
 * @author gajendra.jatav
 */
@Service("communicationEncryptionBusinessObject")
public class CommunicationEncryptionBusinessObject implements ICommunicationEncryptionBusinessObject{

    
    @Inject
    private BeanAccessHelper beanAccessHelper;
    
    @Override
    public void encryptAttachments(CommunicationName communication, CommunicationTemplate communicationTemplate,
            Map<String, Object> dataMap,CommunicationGroupCriteriaVO communicationGroupCriteriaVO) {

        if(!((CommunicationType.EMAIL.equals(communication.getCommunicationType().getCode()) || CommunicationType.WHATSAPP.equals(communication.getCommunicationType().getCode()))
        		&&ValidatorUtils.hasElements(communication.getAttachments())))
        {
            return;
        }
        AttachmentEncryptionPolicy encryptionPolicy=getEncryptionPolicy(communication,communicationTemplate);
        if(encryptionPolicy==null)
        {
            return;
        }
        PasswordDecryptionText pwdDecryptionText=communicationTemplate.getPasswordDecryptionText();
        if(pwdDecryptionText==null)
        {
        	pwdDecryptionText=communication.getPasswordDecryptionText();
        }
        if(pwdDecryptionText==null)
        {
        	pwdDecryptionText=encryptionPolicy.getPasswordDecryptionText();
        }
        if(pwdDecryptionText==null || pwdDecryptionText.getText()==null)
        {
        	pwdDecryptionText=new PasswordDecryptionText();
        	pwdDecryptionText.setText("");
        	
        }
        dataMap.put(AttachmentEncryptionPolicy.PSWD_DECRPT_PLACEHOLDER, pwdDecryptionText.getText());
        String passwordProviderBean=encryptionPolicy.getPasswordProviderBean();
        String password=null;
        ICommunicationPasswordProvider communicationPasswordProvider=beanAccessHelper.getBean(passwordProviderBean, ICommunicationPasswordProvider.class);
        try{
            password=communicationPasswordProvider.computePassword(encryptionPolicy,communication,communicationTemplate,dataMap);
        }catch (Exception e) {
            logAndThrowException(communication,e);
        }
        if(password==null)
        {
            BaseLoggers.flowLogger.debug("Password for encryption got null for communication with communication Code "+communication.getCommunicationCode());
            logAndThrowException(communication, null);
        }
        encryptAttachmentsWithPassword(communication,communicationGroupCriteriaVO.getRequestDtlAndContentMap(),password);
}

    private AttachmentEncryptionPolicy getEncryptionPolicy(
            CommunicationName communication,
            CommunicationTemplate communicationTemplate) {

        AttachmentEncryptionPolicy communicationEncryptionPolicy=communication.getAttachmentEncryptionPolicy();
        AttachmentEncryptionPolicy templateEncryptionPolicy=communicationTemplate.getAttachmentEncryptionPolicy();
        if(templateEncryptionPolicy!=null)
        {
            return templateEncryptionPolicy;
        }
        else if(communicationEncryptionPolicy!=null)
        {
            return communicationEncryptionPolicy;
        }
            return null;
    }

    private void encryptAttachmentsWithPassword(CommunicationName communication,
            Map<CommunicationRequestDetail, GeneratedContentVO> attachments,
            String password) {
           byte[] encryptedContent;
            for (Map.Entry<CommunicationRequestDetail, GeneratedContentVO> entry : attachments.entrySet()) {
                GeneratedContentVO contentVO=entry.getValue();
                encryptedContent=NeutrinoPdfUtility.encryptPdfContent(contentVO.getGeneratedContent(), "", password);
                if(encryptedContent==null)
                {
                    logAndThrowException(communication,null);                
                }
                contentVO.setGeneratedContent(encryptedContent);
            } 
    }
    private void logAndThrowException(CommunicationName communication,Exception e)
    {
        
        BaseLoggers.flowLogger.error("Error while password encryption of attachments for communication with code "+communication.getCommunicationCode());
        if(e!=null)
        {
            BaseLoggers.flowLogger.error("Exception "+e);
        }
        Message message = new Message(CommunicationGeneratorConstants.ERROR_IN_ATTACHMENT_ENCRPT,
                Message.MessageType.ERROR);
        throw ExceptionBuilder
        .getInstance(BusinessException.class)
        .setMessage(message)
        .setSeverity(
                ExceptionSeverityEnum.SEVERITY_MEDIUM
                .getEnumValue()).build();

    }

    /**
     *
     * Masking format F:-10,L:-16,M:X means -Mask all character except for first 10 and last 16 using masking character X
     * Masking format F:10,L:2,M:X means -Mask first 10 and last 2 using masking character X
     * @param value
     * @param formatMask masking format to be applied on value
     * @return
     */
    @Override
    public String maskCommunicationParameter(String value, String formatMask) {

        StringTokenizer tokenizer=new StringTokenizer(formatMask, ",");
        char maskingChar='X';
        Integer firstChar=null;
        Integer lastChar=null;
        while(tokenizer.hasMoreTokens())
        {
            String arg=tokenizer.nextToken();
            char key=arg.charAt(0);
            String val=arg.substring(2);
            if(key=='M' || key=='m')
            {
                maskingChar=val.charAt(0);
            }
            else if(key=='F' || key=='f')
            {
                firstChar=Integer.parseInt(val);
            }
            else if(key=='L' || key=='l')
            {
                lastChar=Integer.parseInt(val);
            }
        }



        return doMaskingWithArgs(value,maskingChar,firstChar,lastChar);
    }
    private String doMaskingWithArgs(String value, char maskingChar, Integer charCountFromStart, Integer charCountFromEnd) {

        int length=value.length();
        int fStart=-1;
        int fEnd=-1;
        int lStart=-1;
        int lEnd=-1;
        char[] result=new char[length];
        if(charCountFromStart==null){
            fStart=-1;
            fEnd=-1;
        }
        else if(charCountFromStart>=0){
            fStart=0;
            fEnd=charCountFromStart;
        }else {
            fStart=(0-charCountFromStart);
            fEnd=length;
        }
        if(fEnd>length)
        {
            fEnd=length;
        }

        if(charCountFromEnd==null){
            lStart=-1;
            lEnd=-1;
        }
        else if(charCountFromEnd>=0){
            lStart=length-charCountFromEnd;
            lEnd=length;
        }else {
            lStart=0;
            lEnd=length+charCountFromEnd;
        }
        if(lStart<0)
        {
            lStart=0;
        }
        for(int i=0;i<length;i++)
        {
            if((i>=fStart && i<fEnd) || (i>=lStart && i<lEnd))
            {
                result[i]=maskingChar;
            }
            else
            {
                result[i]=value.charAt(i);
            }

        }
        return String.valueOf(result);
    }
}
