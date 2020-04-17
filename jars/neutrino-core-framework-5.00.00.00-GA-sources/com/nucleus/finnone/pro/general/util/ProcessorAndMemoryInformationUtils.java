package com.nucleus.finnone.pro.general.util;

import java.lang.management.ManagementFactory;

/**
 * Utility methods to check CPU and Memory health of running application.
 * 
 * @author syambrij.maurya
 *
 */
public class ProcessorAndMemoryInformationUtils {
	
	private static final String VENDOR_KEY = "java.vendor";
	private static final String IBM_VENDOR_IDENTIFICATION_KEY = "ibm";
	
	private ProcessorAndMemoryInformationUtils() {
		throw new UnsupportedOperationException("Creation of object is not allowed.");
	}
	
	/**
	 * For Oracle jdk it will return "Oracle Corporation",
	 * for IBM jdk it will return "IBM Corporation".
	 * 
	 * @return vendorName String value for vendor name.
	 */
	public static String getVendorName() {
		return System.getProperties().getProperty(VENDOR_KEY);
	}
	
	/**
	 * This method use com.sun package available only for oracle and open jdk.
	 * 
	 * @return cpuLoad
	 */
	private static double getCPULoadForOracleAndOpenJdk() {
		return ((com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getProcessCpuLoad();
	}
	
	
	/**
	 * Method returns cpu load for IBM jdk.
	 * It considers whole system load because of no availability of process load
	 * checking API in IBM jdk.
	 * 
	 * @return cpuLoad total system load if available or -1 in case of no implementation.
	 */
	private static double getCPULoadForIBMJdk() {
		return ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
	}
	
	/**
	 * CPU load of running system. In case of oracle/open jdk it returns usage of application only.
	 * Otherwise it returns the cpu usage of whole system. 
	 *  
	 * @return cpuLoad total system load if available or -1 in case of no implementation.
	 */
	public static double getCPULoad() {
		String vendorName = getVendorName();
		if (vendorName.toLowerCase().contains(IBM_VENDOR_IDENTIFICATION_KEY)) {
			return getCPULoadForIBMJdk();
		}
		return getCPULoadForOracleAndOpenJdk();
	}
	
	/**
	 * Gets CPU usage of application and compares if less than against given threshold. 
	 * 
	 * @param threshold value between 0 to 1.
	 * @return boolean
	 */
	public static boolean ifCpuUsageIsLessThanThreshold(double threshold) {
		double currentCPULoad = getCPULoad();
		if (Double.compare(currentCPULoad, -1) == 0) {
			//It means implementation of cpu load method in OSMXBean is not available.
			return false;
		}
		return Double.compare(currentCPULoad, threshold) == -1;
	}
	
	/**
	 * Gets CPU usage of application and compares if greater than against given threshold. 
	 * 
	 * @param threshold value between 0 to 1.
	 * @return boolean
	 */
	public static boolean ifCpuUsageIsGreaterThanThreshold(double threshold) {
		return !ifCpuUsageIsLessThanThreshold(threshold);
	}
	
	/**
	 * Memory usage of running system. It returns only usage of this JVM only.(Excludes whole system load.)
	 * On scale of 0 to 1.
	 * 
	 * @return memoryUsageLoad -1 if no inherent limit for memory or the usage in general.
	 */
	public static double getMemoryUsageLoad() {
		Runtime runtime = Runtime.getRuntime();
		if (runtime.maxMemory() == Long.MAX_VALUE) {
			return -1;
		}
		return ((double)runtime.maxMemory() - runtime.freeMemory())/runtime.maxMemory();
	}
	
	/**
	 * Checks if memory usage of application is less than given threshold. 
	 * 
	 * @param threshold value between 0 to 1.
	 * @return boolean true if memoryUsage is less than given threshold.
	 */
	public static boolean ifMemoryUsageIsLessThanThreshold(double threshold) {
		double memoryUsage = getMemoryUsageLoad();
		return Double.compare(memoryUsage, threshold) == -1;
	}
	
	/**
	 * Checks if memory usage of application is more than given threshold. 
	 * 
	 * @param threshold value between 0 to 1.
	 * @return boolean true if memoryUsage is more than given threshold.
	 */
	public static boolean ifMemoryUsageIsGreaterThanThreshold(double threshold) {
		return !ifMemoryUsageIsLessThanThreshold(threshold);
	}
}
