
package com.nucleus.core.persistence.jdbc.query;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="named-sql-query" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="default-query" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="query" maxOccurs="unbounded" minOccurs="0">
 *                     &lt;complexType>
 *                       &lt;simpleContent>
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
 *                           &lt;attribute name="targetDatabase" use="required" type="{}targetDatabase" />
 *                         &lt;/extension>
 *                       &lt;/simpleContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "namedSqlQuery"
})
@XmlRootElement(name = "neutrino-jdbc-queries")
public class NeutrinoJdbcQueries {

    @XmlElement(name = "named-sql-query")
    protected List<NeutrinoJdbcQueries.NamedSqlQuery> namedSqlQuery;

    /**
     * Gets the value of the namedSqlQuery property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the namedSqlQuery property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNamedSqlQuery().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link NeutrinoJdbcQueries.NamedSqlQuery }
     * 
     * 
     */
    public List<NeutrinoJdbcQueries.NamedSqlQuery> getNamedSqlQuery() {
        if (namedSqlQuery == null) {
            namedSqlQuery = new ArrayList<NeutrinoJdbcQueries.NamedSqlQuery>();
        }
        return this.namedSqlQuery;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="default-query" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="query" maxOccurs="unbounded" minOccurs="0">
     *           &lt;complexType>
     *             &lt;simpleContent>
     *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
     *                 &lt;attribute name="targetDatabase" use="required" type="{}targetDatabase" />
     *               &lt;/extension>
     *             &lt;/simpleContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "defaultQuery",
        "query"
    })
    public static class NamedSqlQuery {

        @XmlElement(name = "default-query", required = true)
        protected String defaultQuery;
        protected List<NeutrinoJdbcQueries.NamedSqlQuery.Query> query;
        @XmlAttribute(required = true)
        protected String name;

        /**
         * Gets the value of the defaultQuery property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getDefaultQuery() {
            return defaultQuery;
        }

        /**
         * Sets the value of the defaultQuery property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setDefaultQuery(String value) {
            this.defaultQuery = value;
        }

        /**
         * Gets the value of the query property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the query property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getQuery().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link NeutrinoJdbcQueries.NamedSqlQuery.Query }
         * 
         * 
         */
        public List<NeutrinoJdbcQueries.NamedSqlQuery.Query> getQuery() {
            if (query == null) {
                query = new ArrayList<NeutrinoJdbcQueries.NamedSqlQuery.Query>();
            }
            return this.query;
        }

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }


        /**
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;simpleContent>
         *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema>string">
         *       &lt;attribute name="targetDatabase" use="required" type="{}targetDatabase" />
         *     &lt;/extension>
         *   &lt;/simpleContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "", propOrder = {
            "value"
        })
        public static class Query {

            @XmlValue
            protected String value;
            @XmlAttribute(required = true)
            protected TargetDatabase targetDatabase;

            /**
             * Gets the value of the value property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getValue() {
                return value;
            }

            /**
             * Sets the value of the value property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setValue(String value) {
                this.value = value;
            }

            /**
             * Gets the value of the targetDatabase property.
             * 
             * @return
             *     possible object is
             *     {@link TargetDatabase }
             *     
             */
            public TargetDatabase getTargetDatabase() {
                return targetDatabase;
            }

            /**
             * Sets the value of the targetDatabase property.
             * 
             * @param value
             *     allowed object is
             *     {@link TargetDatabase }
             *     
             */
            public void setTargetDatabase(TargetDatabase value) {
                this.targetDatabase = value;
            }

        }

    }

}
