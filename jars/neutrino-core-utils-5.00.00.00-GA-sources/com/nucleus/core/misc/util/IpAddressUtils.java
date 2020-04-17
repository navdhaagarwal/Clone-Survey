/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.misc.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Nucleus Software Exports Limited
 */
public class IpAddressUtils {

    private static final String  IP_ADDRESS     = "(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})";
    private static final Pattern addressPattern = Pattern.compile(IP_ADDRESS);

    public static boolean isInRange(String from, String to, String remoteAddr) {
        int address = toInteger(remoteAddr);
        int low = toInteger(from);
        int high = toInteger(to);
        return low <= address && address <= high;
    }

    /*
     * Convert a dotted decimal format address to a packed integer format
     */
    public static int toInteger(String address) {
        Matcher matcher = addressPattern.matcher(address);
        if (matcher.matches()) {
            return matchAddress(matcher);
        } else {
            throw new IllegalArgumentException("Could not parse [" + address + "]");
        }
    }

    /*
     * Convenience method to extract the components of a dotted decimal address and
     * pack into an integer using a regex match
     */
    private static int matchAddress(Matcher matcher) {
        int addr = 0;
        for (int i = 1 ; i <= 4 ; ++i) {
            int n = (rangeCheck(Integer.parseInt(matcher.group(i)), -1, 255));
            addr |= ((n & 0xff) << 8 * (4 - i));
        }
        return addr;
    }

    /*
     * Convenience function to check integer boundaries.
     * Checks if a value x is in the range (begin,end].
     * Returns x if it is in range, throws an exception otherwise.
     */
    private static int rangeCheck(int value, int begin, int end) {
        if (value > begin && value <= end) { // (begin,end]
            return value;
        }

        throw new IllegalArgumentException("Value [" + value + "] not in range (" + begin + "," + end + "]");
    }

}
