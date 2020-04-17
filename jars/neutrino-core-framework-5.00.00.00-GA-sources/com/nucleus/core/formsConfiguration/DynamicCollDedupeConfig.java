package com.nucleus.core.formsConfiguration;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import com.nucleus.entity.CloneOptions;

@Entity
@Synonym(grant = "ALL")
public class DynamicCollDedupeConfig extends BaseEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Column(length = 4000)
    private String            pathField1;

    @Column(length = 4000)
    private String            pathField2;
    @Column(length = 4000)
    private String            pathField3;
    @Column(length = 4000)
    private String            pathField4;
    @Column(length = 4000)
    private String            pathField5;
    @Column(length = 4000)
    private String            pathField6;

    private Integer           scoreField1;
    private Integer           scoreField2;
    private Integer           scoreField3;
    private Integer           scoreField4;
    private Integer           scoreField5;
    private Integer           scoreField6;

    public String getPathField1() {
        return pathField1;
    }

    public void setPathField1(String pathField1) {
        this.pathField1 = pathField1;
    }

    public String getPathField2() {
        return pathField2;
    }

    public void setPathField2(String pathField2) {
        this.pathField2 = pathField2;
    }

    public String getPathField3() {
        return pathField3;
    }

    public void setPathField3(String pathField3) {
        this.pathField3 = pathField3;
    }

    public String getPathField4() {
        return pathField4;
    }

    public void setPathField4(String pathField4) {
        this.pathField4 = pathField4;
    }

    public String getPathField5() {
        return pathField5;
    }

    public void setPathField5(String pathField5) {
        this.pathField5 = pathField5;
    }

    public String getPathField6() {
        return pathField6;
    }

    public void setPathField6(String pathField6) {
        this.pathField6 = pathField6;
    }

    public Integer getScoreField1() {
        return scoreField1;
    }

    public void setScoreField1(Integer scoreField1) {
        this.scoreField1 = scoreField1;
    }

    public Integer getScoreField2() {
        return scoreField2;
    }

    public void setScoreField2(Integer scoreField2) {
        this.scoreField2 = scoreField2;
    }

    public Integer getScoreField3() {
        return scoreField3;
    }

    public void setScoreField3(Integer scoreField3) {
        this.scoreField3 = scoreField3;
    }

    public Integer getScoreField4() {
        return scoreField4;
    }

    public void setScoreField4(Integer scoreField4) {
        this.scoreField4 = scoreField4;
    }

    public Integer getScoreField5() {
        return scoreField5;
    }

    public void setScoreField5(Integer scoreField5) {
        this.scoreField5 = scoreField5;
    }

    public Integer getScoreField6() {
        return scoreField6;
    }

    public void setScoreField6(Integer scoreField6) {
        this.scoreField6 = scoreField6;
    }

    @Override
    protected void populate(BaseEntity baseEntity, CloneOptions cloneOptions) {
        DynamicCollDedupeConfig dynamicCollDedupeConfig = (DynamicCollDedupeConfig) baseEntity;
        super.populate(dynamicCollDedupeConfig, cloneOptions);
        dynamicCollDedupeConfig.setPathField1(pathField1);
        dynamicCollDedupeConfig.setPathField2(pathField2);
        dynamicCollDedupeConfig.setPathField3(pathField3);
        dynamicCollDedupeConfig.setPathField4(pathField4);
        dynamicCollDedupeConfig.setPathField5(pathField5);
        dynamicCollDedupeConfig.setPathField6(pathField6);
        dynamicCollDedupeConfig.setScoreField1(scoreField1);
        dynamicCollDedupeConfig.setScoreField2(scoreField2);
        dynamicCollDedupeConfig.setScoreField3(scoreField3);
        dynamicCollDedupeConfig.setScoreField4(scoreField4);
        dynamicCollDedupeConfig.setScoreField5(scoreField5);
        dynamicCollDedupeConfig.setScoreField6(scoreField6);

    }

    @Override
    protected void populateFrom(BaseEntity baseEntity, CloneOptions cloneOptions) {

        DynamicCollDedupeConfig dynamicCollDedupeConfig = (DynamicCollDedupeConfig) baseEntity;
        super.populateFrom(dynamicCollDedupeConfig, cloneOptions);
        this.setPathField1(dynamicCollDedupeConfig.getPathField1());
        this.setPathField2(dynamicCollDedupeConfig.getPathField2());
        this.setPathField3(dynamicCollDedupeConfig.getPathField3());
        this.setPathField4(dynamicCollDedupeConfig.getPathField4());
        this.setPathField5(dynamicCollDedupeConfig.getPathField5());
        this.setPathField6(dynamicCollDedupeConfig.getPathField6());
        this.setScoreField1(dynamicCollDedupeConfig.getScoreField1());
        this.setScoreField2(dynamicCollDedupeConfig.getScoreField2());
        this.setScoreField3(dynamicCollDedupeConfig.getScoreField3());
        this.setScoreField4(dynamicCollDedupeConfig.getScoreField4());
        this.setScoreField5(dynamicCollDedupeConfig.getScoreField5());
        this.setScoreField6(dynamicCollDedupeConfig.getScoreField6());

    }

}
