package com.nucleus.core.state.service;

import javax.inject.Inject;
import javax.inject.Named;

import com.nucleus.address.State;

@Named("stateUploadService")
public class StateUploadService implements IStateUploadService {

    @Inject
    @Named("stateUploadBusinessObj")
    private IStateUploadBusinessObj stateUploadBusinessObj;
    
    @Override
    public State uploadState(State state) {
        return stateUploadBusinessObj.uploadState(state);
    }

}
