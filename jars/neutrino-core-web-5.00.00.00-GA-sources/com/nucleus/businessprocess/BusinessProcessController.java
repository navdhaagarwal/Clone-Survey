package com.nucleus.businessprocess;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.validation.util.NeutrinoValidator;
import com.nucleus.license.security.web.LicenseSecurityConstants;
import com.nucleus.logging.BaseLoggers;

@Controller
@RequestMapping("BusinessProcess")
public class BusinessProcessController {

    @Inject
    @Named("BusinessProcessService")
    BusinessProcessService businessProcessService;

    @PreAuthorize("hasAnyAuthority('VIEW_BUSINESS_PROCESS','ADD_BUSINESS_PROCESS','UPDATE_BUSINESS_PROCESS','GET_WORKFLOW_IMAGE','GET_XML') and " + LicenseSecurityConstants.LICENSE_WORK_FLOW_EDITOR)
    @RequestMapping("businessProcess")
    public String getBusinessProcessPage(ModelMap map) {
        List<ProcessDefinition> processDefinitions = businessProcessService.getActiveLatestProcessDefinition();
        map.put("processDefinition", processDefinitions);

        return "businessProcess";
    }

    @PreAuthorize("hasAuthority('ADD_BUSINESS_PROCESS')")
    @RequestMapping("newBusinessProcess")
    @ResponseBody
    public String newBusinessProcess(BusinessProcessFileVO uploadItem, ModelMap map) throws Exception {
        String result = businessProcessService.deployNewBusinessProcess(uploadItem.getCommonsMultipartFile());
        return result;
    }

    @PreAuthorize("hasAuthority('UPDATE_BUSINESS_PROCESS')")
    @RequestMapping("updateBusinessProcess/{processDefinitionKey}")
    @ResponseBody
    public String updateBusinessProcess(BusinessProcessFileVO uploadItem, ModelMap map,
            @PathVariable("processDefinitionKey") String processDefinitionKey) throws Exception {
        String result = businessProcessService.deployEixistingBusinessProcess(processDefinitionKey,
                uploadItem.getCommonsMultipartFile());
        return result;
    }

    @PreAuthorize("hasAuthority('GET_WORKFLOW_IMAGE')")
    @RequestMapping(value = "/getWorkflowImage/{processDefinitionKey}")
    //@ResponseBody
    public ResponseEntity<byte[]> getWorkflowImage(@PathVariable("processDefinitionKey") String processDefinitionKey, HttpServletResponse response) throws IOException {
    	
        NeutrinoValidator.notNull(processDefinitionKey, "Process Definition Key cannot be null");
        byte[] bytes = businessProcessService.getWorkflowImage(processDefinitionKey);
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<byte[]> (bytes, headers, HttpStatus.OK);

    }

    @PreAuthorize("hasAuthority('GET_XML')")
    @RequestMapping(value = "/getXML/{processDefinitionKey}")
  
    public @ResponseBody
    void getXML(@PathVariable("processDefinitionKey") String processDefinitionKey, HttpServletResponse response)
            throws IOException {

        NeutrinoValidator.notNull(processDefinitionKey, "Process Definition Key cannot be null");
        ProcessDefinition processDefinition = businessProcessService
                .getLatestVersionsProcessDefinitionByKey(processDefinitionKey);
        String xml = businessProcessService.getWorkflowStringXML(processDefinition);
        response.setContentType("text/plain");
        response.setHeader("Content-Disposition", "attachment;filename=" + processDefinition.getResourceName());
        ServletOutputStream out = response.getOutputStream();
        out.println(xml);
        out.flush();
        out.close();

    }

}
