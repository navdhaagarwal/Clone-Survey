package com.nucleus.core.state.service;

import java.util.List;
import java.util.Map;

public interface VehicleStateService {

    public Map<String,List<String>> checkForDuplicateStateRTOCode(Long stateId,List<String> stateRtoCodes);
}
