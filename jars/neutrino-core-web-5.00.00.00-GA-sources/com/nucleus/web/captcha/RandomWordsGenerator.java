package com.nucleus.web.captcha;

import java.awt.event.KeyEvent;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import com.nucleus.core.exceptions.SystemException;
import com.octo.captcha.component.word.wordgenerator.WordGenerator;

public class RandomWordsGenerator implements WordGenerator {

    Charset charset = Charset.defaultCharset();

    RandomWordsGenerator() {
    }

    RandomWordsGenerator(String charsetStr) {
        if (charsetStr != null && !charsetStr.isEmpty()) {
            try {
                this.charset = Charset.forName(charsetStr);
            } catch (Exception e) {
                throw new SystemException("Exception while loading charset " + charsetStr, e);
            }
        }
    }

    @Override
    public String getWord(Integer length) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0 ; i < 255 ; i++) {
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(i);
            String str = new String(bb.array(), charset).trim();
            if (str.length() > 0 && !str.equals("?") && isPrintableChar(str.charAt(0))
                    && StringUtils.isAlphanumeric(String.valueOf(str.charAt(0)))) {
                stringBuffer.append(str.charAt(0));
            }
        }
        return RandomStringUtils.random(length, stringBuffer.toString());
    }

    @Override
    public String getWord(Integer length, Locale locale) {
        return getWord(length);
    }

    public boolean isPrintableChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c)) && c != KeyEvent.CHAR_UNDEFINED && block != null
                && block != Character.UnicodeBlock.SPECIALS;
    }

}
