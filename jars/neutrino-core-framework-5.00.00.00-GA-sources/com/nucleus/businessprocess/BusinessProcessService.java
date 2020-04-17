package com.nucleus.businessprocess;

import java.io.IOException;
import java.util.List;

import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

public interface BusinessProcessService {

    public String deployNewBusinessProcess(CommonsMultipartFile commonsMultipartFile) throws Exception;

    public abstract ProcessDefinition getLatestVersionsProcessDefinitionByKey(String key);

    public abstract List<ProcessDefinition> getAllVersionsProcessDefinitionByKey(String key);

    public abstract List<ProcessDefinition> getActiveLatestProcessDefinition();

    public abstract byte[] getWorkflowImage(String processDefinitionKey) throws IOException;

    public String getWorkflowStringXML(ProcessDefinition processDefinition) throws IOException;

    public abstract List<String> getUnmappedLatestProcessDefinition();

    public String deployEixistingBusinessProcess(String toBeUpdatedSourcekey, CommonsMultipartFile commonsMultipartFile)
            throws Exception;

}