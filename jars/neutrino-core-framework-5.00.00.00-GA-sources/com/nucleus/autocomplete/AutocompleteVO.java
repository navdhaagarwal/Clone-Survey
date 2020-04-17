package com.nucleus.autocomplete;

import com.nucleus.html.util.HtmlUtils;

import java.util.List;
import java.util.Map;

public class AutocompleteVO
{
    private long s;                      //size
    private int p;                      //page
    private List<Map<String, ?>> d;     //data
    private String ic;                  //idCurr
    private String il;                  //i_label
    private String ci;                  //content_id
    private String iv;                  //itemVal
    private String[] scl;               //searchColumnList
    private String[] colh;              //column heading

    public String[] getScl() {
        return scl;
    }

    public void setScl(String[] scl) {
        this.scl = scl;
    }


    public long getS() {
        return s;
    }

    public void setS(long s) {
        this.s = s;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public List<Map<String, ?>> getD() {
        return d;
    }

    public void setD(List<Map<String, ?>> d) {
        this.d = d;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = HtmlUtils.htmlEscape(ic);
    }

    public String getIl() {
        return il;
    }

    public void setIl(String il) {
        this.il = il;
    }

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String[] getColh() {
        return colh;
    }

    public void setColh(String[] colh) {
        this.colh = colh;
    }


}
