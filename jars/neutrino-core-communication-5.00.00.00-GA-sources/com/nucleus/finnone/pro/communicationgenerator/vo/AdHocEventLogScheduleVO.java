package com.nucleus.finnone.pro.communicationgenerator.vo;

public class AdHocEventLogScheduleVO extends CommunicationSchedulerBaseVO{
    
	private boolean adHocFlag;
	
	private Long[] eventCodeIds;
	
	private Boolean generateMergedFile;
   

	public boolean isAdHocFlag() {
		return adHocFlag;
	}

	public void setAdHocFlag(boolean adHocFlag) {
		this.adHocFlag = adHocFlag;
	}

	public Long[] getEventCodeIds() {
        return eventCodeIds;
    }

    public void setEventCodeIds(Long[] eventCodeIds) {
        this.eventCodeIds = eventCodeIds;
    }

	public Boolean getGenerateMergedFile() {
		return generateMergedFile;
	}

	public void setGenerateMergedFile(Boolean generateMergedFile) {
		this.generateMergedFile = generateMergedFile;
	}   
    
}
