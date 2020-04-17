package com.nucleus.taskCount;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

public class TaskCountRequest {

	@ApiModelProperty(notes="This field is for list of user uri's for which active task need to be fetched",required=true,dataType="String",hidden=false)
    private List<String> userUri;

    private List<String> teamUri;

    public List<String> getUserUri() {
        return userUri;
    }

    public void setUserUri(List<String> userUri) {
        this.userUri = userUri;
    }

    public List<String> getTeamUri() {
        return teamUri;
    }

    public void setTeamUri(List<String> teamUri) {
        this.teamUri = teamUri;
    }
}
