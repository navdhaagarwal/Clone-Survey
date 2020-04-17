package com.nucleus.finnone.pro.lmsbase.utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import javax.inject.Named;

import com.nucleus.finnone.pro.base.utility.CoreUtility;

@Named("genericUtility")
public class GenericUtility {

	public static String getUniqueId() {
		return CoreUtility.getUniqueId();
	} 
}
