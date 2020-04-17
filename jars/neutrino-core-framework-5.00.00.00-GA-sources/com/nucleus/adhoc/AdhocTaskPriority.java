package com.nucleus.adhoc;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

/**
 * 
 * @author ruchir.sachdeva
 * 
 */
public interface AdhocTaskPriority {
    public static final String  CRITICAL   = "CRITICAL";
    public static final String  HIGH       = "HIGH";
    public static final String  NORMAL     = "NORMAL";
    public static final String  LOW        = "LOW";
    public static final String  LEAST      = "LEAST";

    public static final BidiMap PRIORITIES = new DualHashBidiMap() {
                                               {
                                                   put(80, CRITICAL);
                                                   put(60, HIGH);
                                                   put(40, NORMAL);
                                                   put(20, LOW);
                                                   put(0, LEAST);
                                               }
                                           };
}
