package com.nucleus.core.datastore.service;
/**
 * 
 * Status of Anti-Virus scan.
 * 
 * @author gajendra.jatav
 * 
 */
public enum AntiVirusStatus {
	
	FILE_CLEAN(1),VIRUS_FOUND(2),PROBLEM_OCCURED(3);
	
    private int value; 

	private AntiVirusStatus()
    {
    	throw new IllegalStateException("Utility class");
    }
    
	private AntiVirusStatus(int value) {
    	this.value = value; 
    }
    
    public int getValue() {
		return value;
	}

    
    public static AntiVirusStatus fromValue(int value) 
    {
        try {
             return AntiVirusStatus.values()[value-1];
        } catch(ArrayIndexOutOfBoundsException e) {
             throw new IllegalArgumentException("Unknown enum value :"+ value,e);
        }
    }
}
