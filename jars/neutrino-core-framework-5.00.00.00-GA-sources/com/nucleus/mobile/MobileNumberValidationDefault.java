package com.nucleus.mobile;

public class MobileNumberValidationDefault implements MobileNumberValidationBean {

	@Override
	public Boolean validateMobileNumber(String ISDCode, String MobileNUmber) {
		if (ISDCode == null || ISDCode.isEmpty() || MobileNUmber == null || MobileNUmber.isEmpty()) {
			return false;
		} else {
			boolean flag = false;

			if (ISDCode.equals("+91") && MobileNUmber.length() == 10 && (MobileNUmber.startsWith("9")
					|| MobileNUmber.startsWith("8") || MobileNUmber.startsWith("7") || MobileNUmber.startsWith("6"))) {
				flag = true;
			} else
				flag = false;

			return flag;

		}
	}
}
