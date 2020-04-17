package com.nucleus.core.database.seed.audit;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.nucleus.entity.BaseEntity;

/**
 * @author Nucleus software This class is for auditing the seed data. For
 *         keeping record how many seed entries have actually went into
 *         database.
 */

@Entity
@DynamicUpdate
@DynamicInsert
public class SeedDataAudit extends BaseEntity {

    private static final long serialVersionUID   = 1L;

  

    private String            seedFileName;

    private String            tableName;

    private int               rowCount;

    private int               actualSeededCount;

    boolean                   isSeedingOn        = false;

    String                    seedOperation;

    @Lob
    String                    exceptionOccured;

    boolean                   isSeedingCompleted = false;

    String                    productInfoCode;
    
    int                       version;

    public String getSeedFileName() {
        return this.seedFileName;
    }

    public void setSeedFileName(String seedFileName) {
        this.seedFileName = seedFileName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public int getRowCount() {
        return this.rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public int getActualSeededCount() {
        return actualSeededCount;
    }

    public void setActualSeededCount(int actualSeededCount) {
        this.actualSeededCount = actualSeededCount;
    }

    public String getExceptionOccured() {
        return exceptionOccured;
    }

    public void setExceptionOccured(String exceptionOccured) {
        this.exceptionOccured = exceptionOccured;
    }

    public boolean isSeedingOn() {
        return isSeedingOn;
    }

    public void setSeedingOn(boolean isSeedingOn) {
        this.isSeedingOn = isSeedingOn;
    }

    public boolean isSeedingCompleted() {
        return isSeedingCompleted;
    }

    public void setSeedingCompleted(boolean isSeedingCompleted) {
        this.isSeedingCompleted = isSeedingCompleted;
    }

    public String getSeedOperation() {
        return seedOperation;
    }

    public void setSeedOperation(String seedOperation) {
        this.seedOperation = seedOperation;
    }

    public String getProductInfoCode() {
        return productInfoCode;
    }

    public void setProductInfoCode(String productInfoCode) {
        this.productInfoCode = productInfoCode;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

   

}
