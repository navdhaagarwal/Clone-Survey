package com.nucleus.web.fileconsolidator.util;

import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.*;
import com.nucleus.finnone.pro.fileconsolidator.util.DataTypeEnum;
import com.nucleus.finnone.pro.fileconsolidator.util.FileUploadDownloadConstants;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.CompositeFormat;
import org.apache.commons.lang3.ObjectUtils;
import org.dbunit.dataset.datatype.DataType;

public class CompositeNewFormatDetailVO extends CompositeFormatDetailVO {

    private Boolean alreadyMapped = Boolean.FALSE;

    public CompositeNewFormatDetailVO(FileUploadDownloadBaseFormatDetail baseFormatDetail, FileUploadDownloadUserFormat userFormat, FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifierDetail, Boolean isMapped){
        this(baseFormatDetail,userFormat,isMapped);
        if(userRecordIdentifierDetail != null) {
            setUserRecordIdentifierId(defaultStringIfNull(userRecordIdentifierDetail.getId(), StringUtils.EMPTY));
            setRecordIdentifierCode(defaultStringIfNull(userRecordIdentifierDetail.getRecordIdentifierCode(), getRecordIdentifierCode()));
        }
    }

    public CompositeNewFormatDetailVO(FileUploadDownloadBaseFormatDetail baseFormatDetail, FileUploadDownloadUserFormat userFormat, Boolean isMapped){
        super(baseFormatDetail);
        if(userFormat != null){
            setUserFormatId(String.valueOf(userFormat.getId()));
        }
        this.alreadyMapped = ObjectUtils.defaultIfNull(isMapped,alreadyMapped);
    }

    public CompositeNewFormatDetailVO(FileUploadDownloadUserFormat userFormat, FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifierDetail){
        super();
        if(userFormat != null){
            setUserFormatId(String.valueOf(userFormat.getId()));
        }
        if(userRecordIdentifierDetail != null) {
            setUserRecordIdentifierId(defaultStringIfNull(userRecordIdentifierDetail.getId(), getUserRecordIdentifierId()));
            setRecordIdentifierCode(defaultStringIfNull(userRecordIdentifierDetail.getRecordIdentifierCode(), getRecordIdentifierCode()));
        }
        //TODO Localize
        setFieldName(FileConsolidatorGridConstants.UFD_IGNORE_FIELD_MSG);
        setFieldOrder(String.valueOf(0L));
    }

    public Boolean getAlreadyMapped() {
        return alreadyMapped;
    }

    public void setAlreadyMapped(Boolean alreadyMapped) {
        this.alreadyMapped = alreadyMapped;
    }
}
