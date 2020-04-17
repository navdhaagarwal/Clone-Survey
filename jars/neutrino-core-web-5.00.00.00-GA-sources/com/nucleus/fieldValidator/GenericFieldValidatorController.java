package com.nucleus.fieldValidator;

import com.nucleus.config.persisted.service.ConfigurationService;
import com.nucleus.config.persisted.vo.ConfigurationVO;
import com.nucleus.entity.SystemEntity;
import com.nucleus.web.common.controller.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;

@RequestMapping("/GenericFieldValidator")
@Controller
public class GenericFieldValidatorController extends BaseController {

    @Inject
    @Named(value = "configurationService")
    private ConfigurationService configurationService;

    @RequestMapping("/validateDate")
    @ResponseBody
    public Boolean validateDate(@RequestParam("lhsDate") String lhsDate, @RequestParam("rhsDate") String rhsDate,
                                     @RequestParam("validation") String validation){
        ConfigurationVO configurationVO = configurationService.getConfigurationPropertyFor(SystemEntity.getSystemEntityId(), "config.date.formats");
        String dateFormat;
        if(configurationVO == null || StringUtils.isEmpty(configurationVO.getPropertyValue())) {
            dateFormat = "dd/MM/yyyy";
        } else {
            dateFormat = configurationVO.getPropertyValue();
        }
        LocalDate lhs = LocalDate.parse(lhsDate, java.time.format.DateTimeFormatter.ofPattern(dateFormat));
        LocalDate rhs = LocalDate.parse(rhsDate, java.time.format.DateTimeFormatter.ofPattern(dateFormat));
        switch (validation) {
            case "GREATER_THAN":
                return lhs.compareTo(rhs) > 0;
            case "LESS_THAN":
                return lhs.compareTo(rhs) < 0;
            case "EQUALS":
                return lhs.compareTo(rhs) == 0;
            case "NOT_EQUALS":
                return lhs.compareTo(rhs) != 0;
        }
        return false;
    }

}
