package com.nucleus.businessprocess;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class BusinessProcessFileVO {

    private CommonsMultipartFile commonsMultipartFile;

    public CommonsMultipartFile getCommonsMultipartFile() {
        return commonsMultipartFile;
    }

    public void setCommonsMultipartFile(CommonsMultipartFile commonsMultipartFile) {
        this.commonsMultipartFile = commonsMultipartFile;
    }
}
