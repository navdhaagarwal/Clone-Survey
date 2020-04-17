package com.nucleus.web.editors;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * 
 * @author Nucleus Software Exports Limited
 * Custom String Editor class
 *        Converts empty String to null  
 */
@ControllerAdvice
@Controller
public class CustomStringTrimmerEditor {

   /* @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }*/
}
