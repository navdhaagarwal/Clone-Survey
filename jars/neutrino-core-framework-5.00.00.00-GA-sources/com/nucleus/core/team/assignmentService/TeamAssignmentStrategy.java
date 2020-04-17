/**
 * This file and a proportion of its content is copyright of Nucleus Software Exports Limited - © 2012. All rights reserved.
 * Any redistribution or reproduction of part or all of the contents in any form is prohibited other than the following:
 * - you cannot print or download to a local hard disk extract contents either part or full for personal/ commercial/
 * academic or any other use
 * - you may not copy the content to individual/ third parties for any type of use, either as compiled or source format
 * without the knowledge and consent of Nucleus Software
 * - You may not, except with our express written permission, distribute or commercially exploit the content. Nor may you
 * transmit it or store it in any other web site or other form of electronic retrieval system.
 */
package com.nucleus.core.team.assignmentService;

import java.util.List;

import com.nucleus.address.City;
import com.nucleus.core.team.entity.Team;
import com.nucleus.service.BaseService;

/**
 * @author Nucleus Software Exports Limited
 */
public interface TeamAssignmentStrategy extends BaseService {

    public Team getAssignedTeam();

    public Team getTeamToAssignForQuickLead(String productTypeShortName);

    public Team getTeamToAssignForIC(City city);

    public Team getLeastLoadedTeam(List<Team> teamList);
//For Performance T
    public Long getAssignedTeamId();
}
