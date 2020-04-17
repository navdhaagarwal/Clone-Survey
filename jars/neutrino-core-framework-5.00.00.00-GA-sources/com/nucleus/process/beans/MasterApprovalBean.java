package com.nucleus.process.beans;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.entity.EntityId;
import com.nucleus.mail.SimpleMailMessageBuilder;
import com.nucleus.makerchecker.MakerCheckerService;

/**
 * class for handling the service task operations of master approval workflow
 */
@Named("masterApprovalBean")
public class MasterApprovalBean {

    private String              verifyForCompleteness = "New Entity created for verification";

    @Inject
    @Named("makerCheckerService")
    private MakerCheckerService makerCheckerService;

    /**
     * invoking service for sending mail
     * 
     * @param emailParameters
     */
    public void sendMail(SimpleMailMessageBuilder simpleMessageBuilder) {

        // mailService.sendMail(simpleMessageBuilder);
    }

    /**
     * Method for saving the approved record
     */
    public void saveRecord(String processEntityUri, Long reviewerId) {
        makerCheckerService.terminateFlowByApproval(EntityId.fromUri(processEntityUri).getLocalId(), reviewerId);

    }

    /**
     * Method for removing the rejected record
     */
    public void removeRecord(String processEntityUri, Long reviewerId) {
        makerCheckerService.terminateFlowByDecline(EntityId.fromUri(processEntityUri).getLocalId(), reviewerId);

    }

    public void processComplete(String processEntity) {

    }

    public String getVerifyForCompleteness() {
        return verifyForCompleteness;
    }

    public void setVerifyForCompleteness(String verifyForCompleteness) {
        this.verifyForCompleteness = verifyForCompleteness;
    }

}
