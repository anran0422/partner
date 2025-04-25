package com.anran.partner.service;

import com.anran.partner.model.DTO.TeamQuery;
import com.anran.partner.model.VueObject.TeamUserVo;
import com.anran.partner.model.domain.Team;
import com.anran.partner.model.domain.User;
import com.anran.partner.model.request.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author DELL
* @description 针对表【team(队伍表)】的数据库操作Service
* @createDate 2024-10-15 11:40:31
*/
public interface TeamService extends IService<Team> {

    long addTeam(Team team, User loginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeam(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param updateTeamRequest
     * @return
     */
    boolean updateTeam(UpdateTeamRequest updateTeamRequest,User loginUser);

    boolean joinTeam(JoinTeamRequest joinTeamRequest, User loginUser);

    boolean quitTeam(QuitTeamRequest quitTeamRequest, User loginUser);

    Boolean deleteTeam(DeleteTeamRequest deleteTeamRequest, User loginUser);
}
