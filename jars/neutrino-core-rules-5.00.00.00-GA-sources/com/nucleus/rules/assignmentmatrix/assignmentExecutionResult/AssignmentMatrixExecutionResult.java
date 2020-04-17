package com.nucleus.rules.assignmentmatrix.assignmentExecutionResult;

import com.nucleus.core.annotations.Synonym;
import com.nucleus.entity.BaseEntity;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DynamicUpdate
@DynamicInsert
@Synonym(grant = "ALL")
@Table(name = "ASSIGN_MAT_EXEC_RESULT")
public class AssignmentMatrixExecutionResult extends BaseEntity {

    private static final long serialVersionUID = 1L;

    private String assignmentMatrixName;
    private Long applicationId;
    @Column(name = "CON_OBJ_MATRIX_RES_STR1")
    private String contextObjectAssignmentMatrixResultString1;
    @Column(name = "CON_OBJ_MATRIX_RES_STR2")
    private String contextObjectAssignmentMatrixResultString2;
    @Column(name = "CON_OBJ_MATRIX_RES_STR3")
    private String contextObjectAssignmentMatrixResultString3;
    @Column(name = "CON_OBJ_MATRIX_RES_STR4")
    private String contextObjectAssignmentMatrixResultString4;
    @Column(name = "CON_OBJ_MATRIX_RES_STR5")
    private String contextObjectAssignmentMatrixResultString5;
    @Column(name = "CON_OBJ_MATRIX_RES_STR6")
    private String contextObjectAssignmentMatrixResultString6;
    @Column(name = "CON_OBJ_MATRIX_RES_STR7")
    private String contextObjectAssignmentMatrixResultString7;
    @Column(name = "CON_OBJ_MATRIX_RES_STR8")
    private String contextObjectAssignmentMatrixResultString8;
    @Column(name = "CON_OBJ_MATRIX_RES_STR9")
    private String contextObjectAssignmentMatrixResultString9;
    @Column(name = "CON_OBJ_MATRIX_RES_STR10")
    private String contextObjectAssignmentMatrixResultString10;
    @Column(name = "CON_OBJ_MATRIX_RES_STR11")
    private String contextObjectAssignmentMatrixResultString11;
    @Column(name = "CON_OBJ_MATRIX_RES_STR12")
    private String contextObjectAssignmentMatrixResultString12;
    @Column(name = "CON_OBJ_MATRIX_RES_STR13")
    private String contextObjectAssignmentMatrixResultString13;
    @Column(name = "CON_OBJ_MATRIX_RES_STR14")
    private String contextObjectAssignmentMatrixResultString14;
    @Column(name = "CON_OBJ_MATRIX_RES_STR15")
    private String contextObjectAssignmentMatrixResultString15;
    @Column(name = "CON_OBJ_MATRIX_RES_INT1")
    private String contextObjectAssignmentMatrixResultInteger1;
    @Column(name = "CON_OBJ_MATRIX_RES_INT2")
    private String contextObjectAssignmentMatrixResultInteger2;
    @Column(name = "CON_OBJ_MATRIX_RES_INT3")
    private String contextObjectAssignmentMatrixResultInteger3;
    @Column(name = "CON_OBJ_MATRIX_RES_INT4")
    private String contextObjectAssignmentMatrixResultInteger4;
    @Column(name = "CON_OBJ_MATRIX_RES_INT5")
    private String contextObjectAssignmentMatrixResultInteger5;
    @Column(name = "CON_OBJ_MATRIX_RES_INT6")
    private String contextObjectAssignmentMatrixResultInteger6;
    @Column(name = "CON_OBJ_MATRIX_RES_INT7")
    private String contextObjectAssignmentMatrixResultInteger7;
    @Column(name = "CON_OBJ_MATRIX_RES_INT8")
    private String contextObjectAssignmentMatrixResultInteger8;
    @Column(name = "CON_OBJ_MATRIX_RES_INT9")
    private String contextObjectAssignmentMatrixResultInteger9;
    @Column(name = "CON_OBJ_MATRIX_RES_INT10")
    private String contextObjectAssignmentMatrixResultInteger10;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM1")
    private String contextObjectAssignmentMatrixResultNumber1;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM2")
    private String contextObjectAssignmentMatrixResultNumber2;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM3")
    private String contextObjectAssignmentMatrixResultNumber3;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM4")
    private String contextObjectAssignmentMatrixResultNumber4;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM5")
    private String contextObjectAssignmentMatrixResultNumber5;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM6")
    private String contextObjectAssignmentMatrixResultNumber6;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM7")
    private String contextObjectAssignmentMatrixResultNumber7;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM8")
    private String contextObjectAssignmentMatrixResultNumber8;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM9")
    private String contextObjectAssignmentMatrixResultNumber9;
    @Column(name = "CON_OBJ_MATRIX_RES_NUM10")
    private String contextObjectAssignmentMatrixResultNumber10;
    @Column(name = "CON_OBJ_MATRIX_RES_BOOL1")
    private String contextObjectAssignmentMatrixResultBoolean1;
    @Column(name = "CON_OBJ_MATRIX_RES_BOOL2")
    private String contextObjectAssignmentMatrixResultBoolean2;
    @Column(name = "CON_OBJ_MATRIX_RES_BOOL3")
    private String contextObjectAssignmentMatrixResultBoolean3;

    public String getAssignmentMatrixName() {
        return assignmentMatrixName;
    }

    public void setAssignmentMatrixName(String assignmentMatrixName) {
        this.assignmentMatrixName = assignmentMatrixName;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getContextObjectAssignmentMatrixResultString1() {
        return contextObjectAssignmentMatrixResultString1;
    }

    public void setContextObjectAssignmentMatrixResultString1(String contextObjectAssignmentMatrixResultString1) {
        this.contextObjectAssignmentMatrixResultString1 = contextObjectAssignmentMatrixResultString1;
    }

    public String getContextObjectAssignmentMatrixResultString2() {
        return contextObjectAssignmentMatrixResultString2;
    }

    public void setContextObjectAssignmentMatrixResultString2(String contextObjectAssignmentMatrixResultString2) {
        this.contextObjectAssignmentMatrixResultString2 = contextObjectAssignmentMatrixResultString2;
    }

    public String getContextObjectAssignmentMatrixResultString3() {
        return contextObjectAssignmentMatrixResultString3;
    }

    public void setContextObjectAssignmentMatrixResultString3(String contextObjectAssignmentMatrixResultString3) {
        this.contextObjectAssignmentMatrixResultString3 = contextObjectAssignmentMatrixResultString3;
    }

    public String getContextObjectAssignmentMatrixResultString4() {
        return contextObjectAssignmentMatrixResultString4;
    }

    public void setContextObjectAssignmentMatrixResultString4(String contextObjectAssignmentMatrixResultString4) {
        this.contextObjectAssignmentMatrixResultString4 = contextObjectAssignmentMatrixResultString4;
    }

    public String getContextObjectAssignmentMatrixResultString5() {
        return contextObjectAssignmentMatrixResultString5;
    }

    public void setContextObjectAssignmentMatrixResultString5(String contextObjectAssignmentMatrixResultString5) {
        this.contextObjectAssignmentMatrixResultString5 = contextObjectAssignmentMatrixResultString5;
    }

    public String getContextObjectAssignmentMatrixResultString6() {
        return contextObjectAssignmentMatrixResultString6;
    }

    public void setContextObjectAssignmentMatrixResultString6(String contextObjectAssignmentMatrixResultString6) {
        this.contextObjectAssignmentMatrixResultString6 = contextObjectAssignmentMatrixResultString6;
    }

    public String getContextObjectAssignmentMatrixResultString7() {
        return contextObjectAssignmentMatrixResultString7;
    }

    public void setContextObjectAssignmentMatrixResultString7(String contextObjectAssignmentMatrixResultString7) {
        this.contextObjectAssignmentMatrixResultString7 = contextObjectAssignmentMatrixResultString7;
    }

    public String getContextObjectAssignmentMatrixResultString8() {
        return contextObjectAssignmentMatrixResultString8;
    }

    public void setContextObjectAssignmentMatrixResultString8(String contextObjectAssignmentMatrixResultString8) {
        this.contextObjectAssignmentMatrixResultString8 = contextObjectAssignmentMatrixResultString8;
    }

    public String getContextObjectAssignmentMatrixResultString9() {
        return contextObjectAssignmentMatrixResultString9;
    }

    public void setContextObjectAssignmentMatrixResultString9(String contextObjectAssignmentMatrixResultString9) {
        this.contextObjectAssignmentMatrixResultString9 = contextObjectAssignmentMatrixResultString9;
    }

    public String getContextObjectAssignmentMatrixResultString10() {
        return contextObjectAssignmentMatrixResultString10;
    }

    public void setContextObjectAssignmentMatrixResultString10(String contextObjectAssignmentMatrixResultString10) {
        this.contextObjectAssignmentMatrixResultString10 = contextObjectAssignmentMatrixResultString10;
    }

    public String getContextObjectAssignmentMatrixResultString11() {
        return contextObjectAssignmentMatrixResultString11;
    }

    public void setContextObjectAssignmentMatrixResultString11(String contextObjectAssignmentMatrixResultString11) {
        this.contextObjectAssignmentMatrixResultString11 = contextObjectAssignmentMatrixResultString11;
    }

    public String getContextObjectAssignmentMatrixResultString12() {
        return contextObjectAssignmentMatrixResultString12;
    }

    public void setContextObjectAssignmentMatrixResultString12(String contextObjectAssignmentMatrixResultString12) {
        this.contextObjectAssignmentMatrixResultString12 = contextObjectAssignmentMatrixResultString12;
    }

    public String getContextObjectAssignmentMatrixResultString13() {
        return contextObjectAssignmentMatrixResultString13;
    }

    public void setContextObjectAssignmentMatrixResultString13(String contextObjectAssignmentMatrixResultString13) {
        this.contextObjectAssignmentMatrixResultString13 = contextObjectAssignmentMatrixResultString13;
    }

    public String getContextObjectAssignmentMatrixResultString14() {
        return contextObjectAssignmentMatrixResultString14;
    }

    public void setContextObjectAssignmentMatrixResultString14(String contextObjectAssignmentMatrixResultString14) {
        this.contextObjectAssignmentMatrixResultString14 = contextObjectAssignmentMatrixResultString14;
    }

    public String getContextObjectAssignmentMatrixResultString15() {
        return contextObjectAssignmentMatrixResultString15;
    }

    public void setContextObjectAssignmentMatrixResultString15(String contextObjectAssignmentMatrixResultString15) {
        this.contextObjectAssignmentMatrixResultString15 = contextObjectAssignmentMatrixResultString15;
    }

    public String getContextObjectAssignmentMatrixResultInteger1() {
        return contextObjectAssignmentMatrixResultInteger1;
    }

    public void setContextObjectAssignmentMatrixResultInteger1(String contextObjectAssignmentMatrixResultInteger1) {
        this.contextObjectAssignmentMatrixResultInteger1 = contextObjectAssignmentMatrixResultInteger1;
    }

    public String getContextObjectAssignmentMatrixResultInteger2() {
        return contextObjectAssignmentMatrixResultInteger2;
    }

    public void setContextObjectAssignmentMatrixResultInteger2(String contextObjectAssignmentMatrixResultInteger2) {
        this.contextObjectAssignmentMatrixResultInteger2 = contextObjectAssignmentMatrixResultInteger2;
    }

    public String getContextObjectAssignmentMatrixResultInteger3() {
        return contextObjectAssignmentMatrixResultInteger3;
    }

    public void setContextObjectAssignmentMatrixResultInteger3(String contextObjectAssignmentMatrixResultInteger3) {
        this.contextObjectAssignmentMatrixResultInteger3 = contextObjectAssignmentMatrixResultInteger3;
    }

    public String getContextObjectAssignmentMatrixResultInteger4() {
        return contextObjectAssignmentMatrixResultInteger4;
    }

    public void setContextObjectAssignmentMatrixResultInteger4(String contextObjectAssignmentMatrixResultInteger4) {
        this.contextObjectAssignmentMatrixResultInteger4 = contextObjectAssignmentMatrixResultInteger4;
    }

    public String getContextObjectAssignmentMatrixResultInteger5() {
        return contextObjectAssignmentMatrixResultInteger5;
    }

    public void setContextObjectAssignmentMatrixResultInteger5(String contextObjectAssignmentMatrixResultInteger5) {
        this.contextObjectAssignmentMatrixResultInteger5 = contextObjectAssignmentMatrixResultInteger5;
    }

    public String getContextObjectAssignmentMatrixResultInteger6() {
        return contextObjectAssignmentMatrixResultInteger6;
    }

    public void setContextObjectAssignmentMatrixResultInteger6(String contextObjectAssignmentMatrixResultInteger6) {
        this.contextObjectAssignmentMatrixResultInteger6 = contextObjectAssignmentMatrixResultInteger6;
    }

    public String getContextObjectAssignmentMatrixResultInteger7() {
        return contextObjectAssignmentMatrixResultInteger7;
    }

    public void setContextObjectAssignmentMatrixResultInteger7(String contextObjectAssignmentMatrixResultInteger7) {
        this.contextObjectAssignmentMatrixResultInteger7 = contextObjectAssignmentMatrixResultInteger7;
    }

    public String getContextObjectAssignmentMatrixResultInteger8() {
        return contextObjectAssignmentMatrixResultInteger8;
    }

    public void setContextObjectAssignmentMatrixResultInteger8(String contextObjectAssignmentMatrixResultInteger8) {
        this.contextObjectAssignmentMatrixResultInteger8 = contextObjectAssignmentMatrixResultInteger8;
    }

    public String getContextObjectAssignmentMatrixResultInteger9() {
        return contextObjectAssignmentMatrixResultInteger9;
    }

    public void setContextObjectAssignmentMatrixResultInteger9(String contextObjectAssignmentMatrixResultInteger9) {
        this.contextObjectAssignmentMatrixResultInteger9 = contextObjectAssignmentMatrixResultInteger9;
    }

    public String getContextObjectAssignmentMatrixResultInteger10() {
        return contextObjectAssignmentMatrixResultInteger10;
    }

    public void setContextObjectAssignmentMatrixResultInteger10(String contextObjectAssignmentMatrixResultInteger10) {
        this.contextObjectAssignmentMatrixResultInteger10 = contextObjectAssignmentMatrixResultInteger10;
    }

    public String getContextObjectAssignmentMatrixResultNumber1() {
        return contextObjectAssignmentMatrixResultNumber1;
    }

    public void setContextObjectAssignmentMatrixResultNumber1(String contextObjectAssignmentMatrixResultNumber1) {
        this.contextObjectAssignmentMatrixResultNumber1 = contextObjectAssignmentMatrixResultNumber1;
    }

    public String getContextObjectAssignmentMatrixResultNumber2() {
        return contextObjectAssignmentMatrixResultNumber2;
    }

    public void setContextObjectAssignmentMatrixResultNumber2(String contextObjectAssignmentMatrixResultNumber2) {
        this.contextObjectAssignmentMatrixResultNumber2 = contextObjectAssignmentMatrixResultNumber2;
    }

    public String getContextObjectAssignmentMatrixResultNumber3() {
        return contextObjectAssignmentMatrixResultNumber3;
    }

    public void setContextObjectAssignmentMatrixResultNumber3(String contextObjectAssignmentMatrixResultNumber3) {
        this.contextObjectAssignmentMatrixResultNumber3 = contextObjectAssignmentMatrixResultNumber3;
    }

    public String getContextObjectAssignmentMatrixResultNumber4() {
        return contextObjectAssignmentMatrixResultNumber4;
    }

    public void setContextObjectAssignmentMatrixResultNumber4(String contextObjectAssignmentMatrixResultNumber4) {
        this.contextObjectAssignmentMatrixResultNumber4 = contextObjectAssignmentMatrixResultNumber4;
    }

    public String getContextObjectAssignmentMatrixResultNumber5() {
        return contextObjectAssignmentMatrixResultNumber5;
    }

    public void setContextObjectAssignmentMatrixResultNumber5(String contextObjectAssignmentMatrixResultNumber5) {
        this.contextObjectAssignmentMatrixResultNumber5 = contextObjectAssignmentMatrixResultNumber5;
    }

    public String getContextObjectAssignmentMatrixResultNumber6() {
        return contextObjectAssignmentMatrixResultNumber6;
    }

    public void setContextObjectAssignmentMatrixResultNumber6(String contextObjectAssignmentMatrixResultNumber6) {
        this.contextObjectAssignmentMatrixResultNumber6 = contextObjectAssignmentMatrixResultNumber6;
    }

    public String getContextObjectAssignmentMatrixResultNumber7() {
        return contextObjectAssignmentMatrixResultNumber7;
    }

    public void setContextObjectAssignmentMatrixResultNumber7(String contextObjectAssignmentMatrixResultNumber7) {
        this.contextObjectAssignmentMatrixResultNumber7 = contextObjectAssignmentMatrixResultNumber7;
    }

    public String getContextObjectAssignmentMatrixResultNumber8() {
        return contextObjectAssignmentMatrixResultNumber8;
    }

    public void setContextObjectAssignmentMatrixResultNumber8(String contextObjectAssignmentMatrixResultNumber8) {
        this.contextObjectAssignmentMatrixResultNumber8 = contextObjectAssignmentMatrixResultNumber8;
    }

    public String getContextObjectAssignmentMatrixResultNumber9() {
        return contextObjectAssignmentMatrixResultNumber9;
    }

    public void setContextObjectAssignmentMatrixResultNumber9(String contextObjectAssignmentMatrixResultNumber9) {
        this.contextObjectAssignmentMatrixResultNumber9 = contextObjectAssignmentMatrixResultNumber9;
    }

    public String getContextObjectAssignmentMatrixResultNumber10() {
        return contextObjectAssignmentMatrixResultNumber10;
    }

    public void setContextObjectAssignmentMatrixResultNumber10(String contextObjectAssignmentMatrixResultNumber10) {
        this.contextObjectAssignmentMatrixResultNumber10 = contextObjectAssignmentMatrixResultNumber10;
    }

    public String getContextObjectAssignmentMatrixResultBoolean1() {
        return contextObjectAssignmentMatrixResultBoolean1;
    }

    public void setContextObjectAssignmentMatrixResultBoolean1(String contextObjectAssignmentMatrixResultBoolean1) {
        this.contextObjectAssignmentMatrixResultBoolean1 = contextObjectAssignmentMatrixResultBoolean1;
    }

    public String getContextObjectAssignmentMatrixResultBoolean2() {
        return contextObjectAssignmentMatrixResultBoolean2;
    }

    public void setContextObjectAssignmentMatrixResultBoolean2(String contextObjectAssignmentMatrixResultBoolean2) {
        this.contextObjectAssignmentMatrixResultBoolean2 = contextObjectAssignmentMatrixResultBoolean2;
    }

    public String getContextObjectAssignmentMatrixResultBoolean3() {
        return contextObjectAssignmentMatrixResultBoolean3;
    }

    public void setContextObjectAssignmentMatrixResultBoolean3(String contextObjectAssignmentMatrixResultBoolean3) {
        this.contextObjectAssignmentMatrixResultBoolean3 = contextObjectAssignmentMatrixResultBoolean3;
    }
}
