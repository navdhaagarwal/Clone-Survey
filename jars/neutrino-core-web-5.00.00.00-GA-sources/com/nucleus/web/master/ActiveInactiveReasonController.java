package com.nucleus.web.master;


import com.nucleus.activeInactiveReason.ReasonActive;
import com.nucleus.activeInactiveReason.ReasonInActive;
import com.nucleus.activeInactiveReason.ReasonsActiveInactiveMapping;
import com.nucleus.core.genericparameter.service.GenericParameterService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Controller
@RequestMapping(value = "/ActiveInactiveReasonController")
public class ActiveInactiveReasonController {

    @Inject
    @Named("genericParameterService")
    private GenericParameterService genericParameterService;

    @RequestMapping(value = "/populateDescriptionForActiveInActiveReasons")
    @ResponseBody
    public String populateReasonForActiveInActive(@RequestParam("reasonValueId") Long reasonId,@RequestParam("flagForActiveInActive") String flagForActiveInActive){
       String description = null;
        if(flagForActiveInActive.equalsIgnoreCase("active")) {
          ReasonActive reasonActive =  genericParameterService.findById(reasonId, ReasonActive.class);
          description = reasonActive.getDescription();
       }
        if(flagForActiveInActive.equalsIgnoreCase("Inactive")) {
            ReasonInActive reasonInActive =  genericParameterService.findById(reasonId, ReasonInActive.class);
            description = reasonInActive.getDescription();
        }
        return description;
    }

    @RequestMapping(value = "/addActiveReasonRow")
    public String addActiveReasonRow(@RequestParam int endSize,@RequestParam String masterID, ModelMap map, HttpServletRequest request) {
        map.put("reasonsActiveInactiveMapping",new ReasonsActiveInactiveMapping());
        map.put("endSize",endSize);
        map.put("masterID",masterID);
        return "/activeInactiveReason/addActiveReasons";
    }

    @RequestMapping(value = "/addInActiveReasonRow")
    public String addInActiveReasonRow(@RequestParam int endSize,@RequestParam String masterID, ModelMap map, HttpServletRequest request) {
        map.put("reasonsActiveInactiveMapping",new ReasonsActiveInactiveMapping());
        map.put("endSize",endSize);
        map.put("masterID",masterID);
        return "/activeInactiveReason/addInactiveReason";
    }



}
