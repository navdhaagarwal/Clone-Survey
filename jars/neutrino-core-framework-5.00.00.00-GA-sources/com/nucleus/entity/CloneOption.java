/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus SOftware
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.entity;

/**
 * Class to represent one clone option
 */
public class CloneOption {

    private final String key;
    private final String value;

    public CloneOption(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public CloneOption(String key, Boolean value) {
        this.key = key;
        this.value = String.valueOf(value);
    }

    public CloneOption(String key, Number value) {
        this.key = key;
        this.value = String.valueOf(value);
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
