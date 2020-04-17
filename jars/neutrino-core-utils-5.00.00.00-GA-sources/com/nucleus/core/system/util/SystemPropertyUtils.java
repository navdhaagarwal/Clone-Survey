package com.nucleus.core.system.util;


/**
 * @author Nucleus Software Exports Limited
 *
 */

public class SystemPropertyUtils {
    
    
    private static final String newLine=System.getProperty("line.separator");

    public static String getNewline() {
        return newLine;
    }

}
