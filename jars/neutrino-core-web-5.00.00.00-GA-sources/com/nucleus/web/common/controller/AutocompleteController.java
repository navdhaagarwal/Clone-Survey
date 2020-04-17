package com.nucleus.web.common.controller;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import com.nucleus.autocomplete.AutocompleteVO;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.nucleus.html.util.HtmlUtils;

import com.nucleus.autocomplete.AutocompleteService;

/**
 * @author Nucleus Software India Pvt Ltd This field is being used for
 *         controlling country CRUD and task allocation work-flow related
 *         operations.
 */
@Transactional
@Controller
@RequestMapping(value = "/autocomplete")
public class AutocompleteController extends BaseController {

    @Inject
    @Named("autocompleteService")
    private AutocompleteService autocompleteService;

    @Inject
    @Named("messageSource")
    private MessageSource messageSource;

    @RequestMapping(value = "/populate")
    @ResponseBody
    public AutocompleteVO populateValues(ModelMap map, @RequestParam String value, @RequestParam String itemVal,
            @RequestParam String searchCol, @RequestParam String className, @RequestParam Boolean loadApprovedEntityFlag,
            @RequestParam String i_label, @RequestParam String idCurr, @RequestParam String content_id,
            @RequestParam int page, @RequestParam(required = false) String itemsList,
            @RequestParam(required = false) Boolean strictSearchOnitemsList, HttpServletRequest req,
            @RequestParam(required = false) String parentId, @RequestParam(required = false) String parentCol,
            @RequestParam(required = false) Boolean containsSearchEnabled,@RequestParam(required = false) Boolean searchrowswithparentIdnull) {
        String[] searchColumnList = searchCol.split(" ");
        AutocompleteVO autocompleteVO = new AutocompleteVO();
        if (strictSearchOnitemsList == null) {
        	strictSearchOnitemsList = false;
        }
        if (loadApprovedEntityFlag == null) {
        	loadApprovedEntityFlag = false;
        }
        if (containsSearchEnabled == null) {
        	containsSearchEnabled = false;
        }
        if(searchrowswithparentIdnull == null){
            searchrowswithparentIdnull = false;
        }
        List<Map<String, ?>> list = autocompleteService.searchOnFieldValueByPage(className, itemVal, searchColumnList, value,
        		loadApprovedEntityFlag, itemsList, strictSearchOnitemsList , page, parentId, parentCol, containsSearchEnabled,searchrowswithparentIdnull);
        int sizeList = 0;
        if (!list.isEmpty()) {
            Map<String, ?> listMap = list.get(list.size() - 1);
            sizeList = ((Long) listMap.get("size")).intValue();
            list.remove(list.size() - 1);
            /*map.put("size", sizeList);
            map.put("page", page);*/
            autocompleteVO.setS(sizeList);
            autocompleteVO.setP(page);

        }
        int i;
        String[] sclHeading=new String[searchColumnList.length];
        for(i=0;i<searchColumnList.length;i++)
        {
            searchColumnList[i]=searchColumnList[i].replace(".", "");
            sclHeading[i]=messageSource.getMessage("label.autoComplete."+searchColumnList[i],null,Locale.getDefault());
        }
        if (i_label != null && i_label.contains(".")) {
            i_label = i_label.replace(".", "");
        }

        /*map.put("data", list);*/
        autocompleteVO.setD(list);
        if(idCurr!=null && idCurr.trim().length()>0){
        	idCurr = idCurr.replaceAll("[^\\w\\s\\-_]", "");
        }
        /*map.put("idCurr", HtmlUtils.htmlEscape(idCurr));
        map.put("i_label", i_label);
        map.put("content_id", content_id);
        map.put("itemVal", itemVal);*/
        autocompleteVO.setIc(HtmlUtils.htmlEscape(idCurr));
        autocompleteVO.setIl(i_label);
        autocompleteVO.setCi(content_id);
        autocompleteVO.setIv(itemVal);
        autocompleteVO.setScl(searchColumnList);
        autocompleteVO.setColh(sclHeading);
        //return "autocomplete";
        return autocompleteVO;

    }

    @RequestMapping(value = "/populateZipCode")
    public @ResponseBody
    List<Map<String, ?>> populateValuesInZipCode(ModelMap map, @RequestParam String value, @RequestParam String itemVal,
            @RequestParam String searchCol, @RequestParam String className, @RequestParam boolean loadApprovedEntityFlag,
            @RequestParam String code) {
        List<Map<String, ?>> zipCodeListUnique = new ArrayList<Map<String, ?>>();
        List<Map<String, ?>> zipCodeList = autocompleteService.getZipCodesForCountrySelected(className, itemVal, searchCol,
                value, loadApprovedEntityFlag, code);
        HashSet<String> zipCodeHashSet = new HashSet<String>();
        for (Map<String, ?> zipCodeMap : zipCodeList) {
            for (Map.Entry<String, ?> entry : zipCodeMap.entrySet()) {
                zipCodeHashSet.add((String) entry.getValue());

            }
        }
        for (String zipCode : zipCodeHashSet) {
            Map<String, String> mapZipCode = new HashMap<String, String>();
            mapZipCode.put("postalCode", zipCode);
            zipCodeListUnique.add(mapZipCode);
        }
        return zipCodeListUnique;
    }

    @RequestMapping(value = "/pagination")
    public @ResponseBody
    void paginateList(ModelMap map, @RequestParam int offset) {
        if (offset == 2) {
            map.put("offset", 3);
        } else if (offset == 3) {
            map.put("offset", 6);
        } else if (offset == 4) {
            map.put("offset", 9);
        }

    }

    /**
     * used to populate menu items on typing keys
     * 
     * 
     */
    @RequestMapping(value = "/populateMenu")
    public String populateMenu() {
        return "autocomplete/autoCompleteMenuItem";

    }

    @RequestMapping(value = "/getAutoCompleteValue")
    public @ResponseBody
    String getAutoCompleteValue(@RequestParam Long id, @RequestParam String showLabel, @RequestParam String className) {
        return autocompleteService.getAutoCompleteValue(id, className, showLabel);
    }

}
