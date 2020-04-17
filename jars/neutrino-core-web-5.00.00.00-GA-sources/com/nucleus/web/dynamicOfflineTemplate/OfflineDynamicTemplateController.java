package com.nucleus.web.dynamicOfflineTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.dynamicform.service.FormConfigurationConstant;
import com.nucleus.core.formsConfiguration.PersistentFormData;
import com.nucleus.core.formsConfiguration.UIMetaData;
import com.nucleus.core.formsConfiguration.UIMetaDataVo;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.formDefinition.BaseDynamicFormController;

@Transactional
@Controller
@RequestMapping(value = "/OfflineDynamicTemplate")
public class OfflineDynamicTemplateController extends BaseDynamicFormController {

    @Inject
    @Named("dynamicFormProcessor")
    protected DynamicFormProcessor dynamicFormProcessor;

    /**
     * 
     * creates Offline Template data for the dynamic form
     * @param map
     * @param taskId
     * @return
     */
    @RequestMapping(value = "/createFormData", method = RequestMethod.POST)
    public String createOfflineFormData(ModelMap map, @RequestParam(value = "formName", required = false) String formName,
            @RequestParam(value = "formVersion", required = false) String formVersion, HttpServletRequest request) {
        String viewMode = "false";
        Map<String, Object> pfdPlusUiMap = formConfigurationMappingService.getUiMetaData(formName, null, null, formVersion);
        UIMetaData uiMetaData = (UIMetaData) pfdPlusUiMap.get(FormConfigurationConstant.UI_META_DATA);

        PersistentFormData persistentFormData = (PersistentFormData) pfdPlusUiMap
                .get(FormConfigurationConstant.PERSISTENT_FORM_DATA);

        Map<String, Object> dataMap = formService.loadPersistentDataMap(null, formName, persistentFormData);
        UIMetaDataVo uiMetaDataVo = mergeFormDetailsAndData(uiMetaData, dataMap);

        if (viewMode != null && !viewMode.isEmpty()) {
            map.put("viewMode", viewMode);
        }
        map.put("uiMetaDataVo", uiMetaDataVo);
        if (null != uiMetaDataVo.getFormName()) {
            map.put("formKey", uiMetaDataVo.getFormName().replaceAll(" ", "_"));
        }
        map.put("offlineTemplate", true);
        return "dynamicOfflineTemplate";
    }

    /**
     * 
     * creates Offline Template 
     *  for the dynamic form
     * @param map
     * @param taskId
     * @return
     */
  @RequestMapping(value = "/createOfflineForm", method = RequestMethod.POST)
    @ResponseBody
    public String createHTML(@RequestParam("formName") String formName,
            @RequestParam(value = "formVersion", required = false) String formVersion, HttpServletRequest request) {

        String realPath = request.getServletContext().getRealPath("/");
        String fileUrl = null;
        // fileUrl - where file need to be saved
        if (null == formVersion || formVersion.isEmpty()) {
            fileUrl = realPath + "/" + formName;
        } else {
            fileUrl = realPath + "/" + formName + "_" + formVersion;
        }
        String hostUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String dynamicHtmlUrl = hostUrl + request.getContextPath() + request.getServletPath()
                + "/OfflineDynamicTemplate/createFormData";

        BaseLoggers.flowLogger.debug("realPath = " + realPath + " fileUrl = " + fileUrl + " hostUrl = " + hostUrl
                + " dynamicHtmlUrl = " + dynamicHtmlUrl);
        String dynamicZipUrl = dynamicFormProcessor.getZipUrl(dynamicHtmlUrl, fileUrl, hostUrl, formName, formVersion,
                request, null);
        return dynamicZipUrl;
    }
  
    @RequestMapping(value = "/downloadOfflineTemplate")
    public void downloadOfflineTemplate(@RequestParam("fileUrl") String fileUrl, @RequestParam("formName") String formName,
            @RequestParam(value = "formVersion", required = false) String formVersion, ModelMap map,
            HttpServletResponse response) {
        formName = formName.replace(" ", "_");
        response.setContentType("application/zip");
        if (null == formVersion || formVersion.isEmpty()) {
            response.setHeader("Content-Disposition", "attachment;filename=" + formName + ".zip");
        } else {
            response.setHeader("Content-Disposition", "attachment;filename=" + formName + "_" + formVersion + ".zip");
        }
        FileInputStream fileInputStream =null;
        try {
            fileInputStream = new FileInputStream(new File(fileUrl + ".zip"));
            ServletOutputStream outServletOutputStream = response.getOutputStream();
            byte[] outputByte = new byte[4096];
            while (fileInputStream.read(outputByte, 0, 4096) != -1) {
                outServletOutputStream.write(outputByte, 0, 4096);
            }
            outServletOutputStream.flush();
            outServletOutputStream.close();
            FileUtils.deleteDirectory(new File(fileUrl));
            File file = new File(fileUrl + ".zip");
            if(!file.delete()){
                throw new IOException("Unable to delete file");
            }
        } catch (IOException e) {
            BaseLoggers.exceptionLogger.debug("Exception: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

}
