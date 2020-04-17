package com.nucleus.taskCount;

import java.util.Map;

import io.swagger.annotations.ApiModelProperty;

public class TaskCountResponse {

	@ApiModelProperty(notes="This field is response string",required=false,hidden=false)
    private Map<String,Long> uriCountMap;

    public Map<String, Long> getUriCountMap() {
        return uriCountMap;
    }

    public void setUriCountMap(Map<String, Long> uriCountMap) {
        this.uriCountMap = uriCountMap;
    }
}
