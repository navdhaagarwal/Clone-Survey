package com.nucleus.rules.model;

/**
 * @author Nucleus Software Exports Limited
 * Bean containing information used in UI
 */

public class Token {

    private String displayName;

    private String englishSentence;

    private int    dataType;

    private String tokenType;

    /**
     * 
     * Gets displayName parameter of token
     * @return
     */

    public String getDisplayName() {
        return displayName;
    }

    /**
     *  Sets displayName parameter of token
     * @param displayName
     */

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 
     * Gets englishSentence parameter of token
     * @return
     */

    public String getEnglishSentence() {
        return englishSentence;
    }

    /**
     * 
     *  Sets englishSentence parameter of token
     * @param englishSentence
     */
    public void setEnglishSentence(String englishSentence) {
        this.englishSentence = englishSentence;
    }

    /**
     * 
     * Gets dataType parameter of token
     * @return
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * 
     * Sets dataType parameter of token
     * @param dataType
     */
    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    /**
     * 
     * Gets tokenType parameter of token
     * @return
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * 
     * Sets tokenType parameter of token
     * @param tokenType
     */

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

}