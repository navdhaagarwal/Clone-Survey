package com.nucleus.cas.sequence;

import javax.inject.Named;

import org.joda.time.DateTime;

import com.nucleus.core.misc.util.DateUtils;

@Named("iciciSequenceService")
public class IciciSequenceServiceImpl extends CasSequenceServiceImpl {

    @Override
    public String generateNextApplicationNumber() {
        DateTime today = DateUtils.getCurrentUTCTime();
        String yearDigit = ((Integer) today.getYear()).toString().substring(2, 4);
        String monthDigit = pad((Integer) today.getMonthOfYear(), 2);
        String dayDigit = pad((Integer) today.getDayOfMonth(), 2);
        return yearDigit + monthDigit + dayDigit + pad(entityDao.getNextValue("application_sequence"), 5);
    }

    @Override
    public String[] generateNextApplicationNumbersRange(int incrementBy) {
        DateTime today = DateUtils.getCurrentUTCTime();
        String yearDigit = ((Integer) today.getYear()).toString().substring(2, 4);
        String monthDigit = pad((Integer) today.getMonthOfYear(), 2);
        String dayDigit = pad((Integer) today.getDayOfMonth(), 2);
        String[] appNumbers = new String[2];
        Long startnumber = entityDao.getNextValue("application_sequence", incrementBy);
        appNumbers[0] = yearDigit + monthDigit + dayDigit + pad(startnumber, 5);
        appNumbers[1] = yearDigit + monthDigit + dayDigit + pad(startnumber + incrementBy, 5);
        return appNumbers;
    }

}
