package com.nucleus.core.i18n.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Embeddable;
import javax.persistence.Transient;

import com.nucleus.core.data.util.DelimitedValueHolder;
import com.nucleus.core.exceptions.InvalidDataException;

@Embeddable
public class MultiLingualValue implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private String               delimitedValue;

    @Transient
    private DelimitedValueHolder holder;

    public void addMultiLingualValue(String locale, String value) {
        if (holder == null) {
            createHolder();
        }
        delimitedValue = holder.addKeyValue(locale, value);
    }

    private void createHolder() {
        holder = new DelimitedValueHolder(delimitedValue);
    }

    public void removeValueForLocale(String locale) {
        if (holder != null) {
            delimitedValue = holder.removeValueForKey(locale);
        }
    }

    public String getValueForLocale(Locale locale) {
        if (locale == null) {
            throw new InvalidDataException("Invalid Locale.");
        }
        if (holder == null) {
            return null;
        }
        return holder.getValueForKey(locale.getDisplayName());
    }

    public Map<String, String> getBreakup() {
        if (holder != null) {
            return Collections.unmodifiableMap(holder.getBreakup());
        }
        return null;
    }

}
