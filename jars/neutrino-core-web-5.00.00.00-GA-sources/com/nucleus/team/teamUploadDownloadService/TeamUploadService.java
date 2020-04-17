package com.nucleus.core.team.service;

import com.nucleus.core.team.vo.TeamVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

@Service
@Named("teamUploadService")
public class TeamUploadService implements ITeamUploadService{

    @Inject
    private ITeamUploadBusinessObj  teamUploadBusinessObj;

    @Override
    @Transactional
    public TeamVO uploadTeam(TeamVO teamVO) {

        return teamUploadBusinessObj.uploadTeam(teamVO);
    }
}
