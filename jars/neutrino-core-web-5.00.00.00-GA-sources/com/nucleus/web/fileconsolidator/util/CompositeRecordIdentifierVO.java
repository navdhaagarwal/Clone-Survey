package com.nucleus.web.fileconsolidator.util;

import com.nucleus.NeutrinoUUIDGenerator;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadBaseFormat;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadBaseRecordIdentifierDetail;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadUserFormat;
import com.nucleus.finnone.pro.fileconsolidator.domainobject.entities.FileUploadDownloadUserRecordIdentifierDetail;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class CompositeRecordIdentifierVO implements Serializable {

    public static final String HTML_ID_PREFIX = "formatDetail_";

    String id = StringUtils.EMPTY;
    String tableConfigKey = StringUtils.EMPTY;
    String baseFormatId = StringUtils.EMPTY;
    String userFormatId = StringUtils.EMPTY;
    String userRecordIdentifierId = StringUtils.EMPTY;
    String baseRecordIdentifierId = StringUtils.EMPTY;
    String commonIdentifierCode = StringUtils.EMPTY;
    String commonIdentifierDescription = StringUtils.EMPTY;
    String multiplicity = StringUtils.EMPTY;

    public CompositeRecordIdentifierVO(){}

    //For User Format Grid
    public CompositeRecordIdentifierVO(FileUploadDownloadUserFormat userFormat, FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier, FileUploadDownloadUserRecordIdentifierDetail userRecordIdentifier){
        this(userFormat.getBaseFormat(),baseRecordIdentifier);

        this.userFormatId = userFormat!=null ? userFormat.getId().toString() : StringUtils.EMPTY;
        this.userRecordIdentifierId = userRecordIdentifier!=null ? userRecordIdentifier.getId().toString() : StringUtils.EMPTY;

        if(userRecordIdentifier != null){
            this.commonIdentifierCode = StringUtils.defaultIfBlank(userRecordIdentifier.getRecordIdentifierCode(),this.commonIdentifierCode);
            this.commonIdentifierDescription = StringUtils.defaultIfBlank(userRecordIdentifier.getRecordIdentifierString(),this.commonIdentifierDescription);
        }

    }

    //For Base Format grid
    public CompositeRecordIdentifierVO(FileUploadDownloadBaseFormat baseFormat, FileUploadDownloadBaseRecordIdentifierDetail baseRecordIdentifier){
        generateUniqueId();
        this.baseFormatId = baseFormat!=null ? baseFormat.getId().toString() : StringUtils.EMPTY;
        this.baseRecordIdentifierId = baseRecordIdentifier!=null ? baseRecordIdentifier.getId().toString() : StringUtils.EMPTY;

        if(baseRecordIdentifier != null){
            this.commonIdentifierCode = StringUtils.defaultIfBlank(baseRecordIdentifier.getRecordIdentifierCode(),StringUtils.EMPTY);
            this.commonIdentifierDescription = StringUtils.defaultIfBlank(baseRecordIdentifier.getRecordIdentifierString(),StringUtils.EMPTY);
            this.multiplicity = StringUtils.defaultIfBlank(baseRecordIdentifier.getMultiplicity(),StringUtils.EMPTY);
        }

    }

    public static CompositeRecordIdentifierVO changeVOForViewColumns(CompositeRecordIdentifierVO voToChange){
        voToChange.setCommonIdentifierCode(FileConsolidatorGridConstants.TD_VIEW_COLUMNS);
        voToChange.setCommonIdentifierDescription(StringUtils.EMPTY);
        voToChange.setMultiplicity(StringUtils.EMPTY);
        return voToChange;
    }

    private void generateUniqueId(){
        this.id = HTML_ID_PREFIX + (new NeutrinoUUIDGenerator()).generateUuid().replace("-","");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTableConfigKey() {
        return tableConfigKey;
    }

    public void setTableConfigKey(String tableConfigKey) {
        this.tableConfigKey = tableConfigKey;
    }

    public String getCommonIdentifierCode() {
        return commonIdentifierCode;
    }

    public void setCommonIdentifierCode(String commonIdentifierCode) {
        this.commonIdentifierCode = commonIdentifierCode;
    }

    public String getCommonIdentifierDescription() {
        return commonIdentifierDescription;
    }

    public void setCommonIdentifierDescription(String commonIdentifierDescription) {
        this.commonIdentifierDescription = commonIdentifierDescription;
    }

    public String getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(String multiplicity) {
        this.multiplicity = multiplicity;
    }

    public String getBaseFormatId() {
        return baseFormatId;
    }

    public void setBaseFormatId(String baseFormatId) {
        this.baseFormatId = baseFormatId;
    }

    public String getUserFormatId() {
        return userFormatId;
    }

    public void setUserFormatId(String userFormatId) {
        this.userFormatId = userFormatId;
    }

    public String getUserRecordIdentifierId() {
        return userRecordIdentifierId;
    }

    public void setUserRecordIdentifierId(String userRecordIdentifierId) {
        this.userRecordIdentifierId = userRecordIdentifierId;
    }

    public String getBaseRecordIdentifierId() {
        return baseRecordIdentifierId;
    }

    public void setBaseRecordIdentifierId(String baseRecordIdentifierId) {
        this.baseRecordIdentifierId = baseRecordIdentifierId;
    }
}
