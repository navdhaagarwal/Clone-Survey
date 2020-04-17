/**
 * Copyright Nucleus Software India Pvt Ltd. All rights reserved.
 */

package com.nucleus.web.formatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import flexjson.TypeContext;
import flexjson.transformer.AbstractTransformer;

/**
 * @author Nucleus Software India Pvt Ltd
 * Custom transformer to handle Calendar and Date fields serialization.
 */
public class CalendarFieldTransformer extends AbstractTransformer {
    private final String transformedFieldName;
    private final String dateformat;

    public CalendarFieldTransformer(String transformedFieldName, String dateformat) {
        super();
        this.transformedFieldName = transformedFieldName;
        this.dateformat = dateformat;
    }

    @Override
    public void transform(Object object) {
        boolean setContext = false;
        String formattedDate = null;

        TypeContext typeContext = getContext().peekTypeContext();
        DateFormat formatter = new SimpleDateFormat(dateformat);
        if (object instanceof Calendar) {
            formattedDate = formatter.format(((Calendar) object).getTime());
        } else// For now we have assumed it is passed Object is an instance of java.util.Date otherwise
        {
            formattedDate = formatter.format(((Date) object).getTime());
        }
        if (!typeContext.isFirst()) {
            getContext().writeComma();
        }
        getContext().writeName(getTransformedFieldName());
        getContext().writeQuoted(formattedDate);
        if (setContext) {
            getContext().writeCloseObject();
        }
    }

    @Override
    public Boolean isInline() {
        return Boolean.TRUE;
    }

    public String getTransformedFieldName() {
        return this.transformedFieldName;
    }
}
