
package com.nucleus.core.persistence.jdbc.query;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for targetDatabase.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="targetDatabase">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="MYSQL"/>
 *     &lt;enumeration value="ORACLE"/>
 *     &lt;enumeration value="SQL_SERVER"/>
 *     &lt;enumeration value="SYBASE"/>
 *     &lt;enumeration value="DB2"/>
 *     &lt;enumeration value="HSQL"/>
 *     &lt;enumeration value="H2"/>
 *     &lt;enumeration value="POSTGRES"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "targetDatabase")
@XmlEnum
public enum TargetDatabase {

    MYSQL("MYSQL"),
    ORACLE("ORACLE"),
    SQL_SERVER("SQL_SERVER"),
    SYBASE("SYBASE"),
    @XmlEnumValue("DB2")
    DB_2("DB2"),
    HSQL("HSQL"),
    @XmlEnumValue("H2")
    H_2("H2"),
    POSTGRES("POSTGRES");
    private final String value;

    TargetDatabase(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TargetDatabase fromValue(String v) {
        for (TargetDatabase c: TargetDatabase.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
