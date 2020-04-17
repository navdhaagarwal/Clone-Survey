package com.nucleus.web.formDefinition;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.commons.collections4.CollectionUtils;
import org.codehaus.jettison.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nucleus.core.formsConfiguration.UIMetaDataVo;
import com.nucleus.entity.Entity;
import com.nucleus.logging.BaseLoggers;
import com.nucleus.web.dynamicOfflineTemplate.DynamicFormProcessor;
import org.springframework.security.access.prepost.PreAuthorize;

@Transactional
@Controller
@RequestMapping(value = "/OfflineFormDefinition")
public class OfflineFormDefinitionController extends BaseDynamicFormController {

    @Inject
    @Named("dynamicFormProcessor")
    protected DynamicFormProcessor dynamicFormProcessor;

    @Value(value = "${cas.offline.template.path}")
    private String                 realPath;

    /**
     * 
     * creates Offline Template for the dynamic form
     * 
     * @param map
     * @param taskId
     * @return
     */
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping("/createOfflineForm")
    @ResponseBody
    public String createHTML(@RequestParam("fIChecked") String fIChecked, HttpServletRequest request) {

        String fileUrl = realPath + "/" + "FieldInvestigationEntryData";
        String hostUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String dynamicHtmlUrl = hostUrl + request.getContextPath() + request.getServletPath()
                + "/FieldInvestigation/FieldInvestigationVerification/loadFIEntriesFormOffineTemplate";
        String dynamicZipUrl = dynamicFormProcessor.getZipUrl(dynamicHtmlUrl, fileUrl, hostUrl,
                "FieldInvestigationEntryForm", "", request, fIChecked);
        return dynamicZipUrl;
    }

    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/syncFormData", method = RequestMethod.POST)
    public @ResponseBody
    String sinkFormData(@RequestBody String jsonString, HttpServletRequest request, HttpServletResponse response)
            throws IOException, JSONException {
        List<UIMetaDataVo> uiMetaDataVoList = formService.getUIMetaDataVoList(jsonString);

        if (CollectionUtils.isNotEmpty(uiMetaDataVoList)) {
            for (UIMetaDataVo uiMetaDataVo : uiMetaDataVoList) {
                String uri = uiMetaDataVo.getEntityUri();
                String packageName = uiMetaDataVo.getPackageName();
                Class clazz = null;
                Entity object = null;

                if (null != packageName && !packageName.isEmpty()) {

                    try {
                        clazz = Class.forName(packageName);
                        if (null != clazz) {
                            object = (Entity) clazz.newInstance();
                        }
                    } catch (ClassNotFoundException e) {
                        BaseLoggers.exceptionLogger.error(e.getMessage());
                    } catch (InstantiationException e) {
                        BaseLoggers.exceptionLogger.error(e.getMessage());
                    } catch (IllegalAccessException e) {
                        BaseLoggers.exceptionLogger.error(e.getMessage());
                    }
                }

                if (null != object) {
                    formService.saveNewObject(object);
                    uri = object.getUri();
                }

                if (null != uri && !uri.isEmpty()) {
                    formService.persistFormData(uri, formService.getJsonMapToSave(uiMetaDataVo), uiMetaDataVo);
                }
            }
        }
        /**
         * To check the browser type
         */
        String userAgent = request.getHeader("User-Agent");
        boolean isChrome = (userAgent != null && userAgent.indexOf("Chrome/") != -1);
        if (isChrome) {
            response.addHeader("Access-Control-Allow-Origin", "file://");
        }
        response.addHeader("Access-Control-Allow-Credentials", "true");

        return "success";

    }

    /**
     * 
     * saves Dynamic Form Data for
     * 
     * @param uiMetaDataVo
     * @param taskId
     */
    @PreAuthorize("hasAuthority('MAKER_FORM_CONFIG')")
    @RequestMapping(value = "/saveFormData")
    public @ResponseBody
    String saveDynamicFormData(UIMetaDataVo uiMetaDataVo, @RequestParam(value = "uri", required = false) String uri,
            @RequestParam(value = "packageName", required = false) String packageName, HttpServletResponse response) {

        Class clazz = null;
        Entity object = null;

        if (null != packageName && !packageName.isEmpty()) {

            try {
                clazz = Class.forName(packageName);
                if (null != clazz) {
                    object = (Entity) clazz.newInstance();
                }
            } catch (ClassNotFoundException e) {
                BaseLoggers.exceptionLogger.error(e.getMessage());
            } catch (InstantiationException e) {
                BaseLoggers.exceptionLogger.error(e.getMessage());
            } catch (IllegalAccessException e) {
                BaseLoggers.exceptionLogger.error(e.getMessage());
            }
        }

        if (null != object) {
            formService.saveNewObject(object);
            uri = object.getUri();
        }

        if (null != uri && !uri.isEmpty()) {
            formService.persistFormData(uri, formService.getJsonMapToSave(uiMetaDataVo), uiMetaDataVo);
        }
        return "success";
    }
}
