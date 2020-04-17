package com.nucleus.dao.query;

import java.io.Serializable;

public class KeyOptrValueHolder implements Serializable {

    private static final long serialVersionUID = 650840104728517912L;

    protected String          key;

    protected Integer          operator;

    protected Object          value;

    public KeyOptrValueHolder(String key, Integer operator, Object value) {
        super();
        this.key = key;
        this.operator = operator;
        this.value = value;
    }



}
