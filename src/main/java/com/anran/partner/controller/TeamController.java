package com.anran.partner.controller;

import com.anran.partner.model.DTO.TeamQuery;
import com.anran.partner.model.VueObject.TeamUserVo;
import com.anran.partner.model.VueObject.UserVo;
import com.anran.partner.model.domain.Team;
import com.anran.partner.model.domain.User;
import com.anran.partner.model.request.AddTeamRequest;
import com.anran.partner.model.request.JoinTeamRequest;
import com.anran.partner.model.request.UpdateTeamRequest;
import com.anran.partner.service.TeamService;
import com.anran.partner.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
public class TeamController {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public Long addTeam(@RequestBody AddTeamRequest addTeamRequest, HttpServletRequest request) {
        if(addTeamRequest == null) return (long) -1;

        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(addTeamRequest, team);
        long teamId = teamService.addTeam(team, loginUser);

        return teamId;
    }

    @PostMapping("/delete")
    public Boolean deleteTeam(@RequestBody long id) {
        if(id <= 0) return false;

        boolean res = teamService.removeById(id);
        if(!res) return false;

        return true;
    }

    @PostMapping("/update")
    public Boolean updateTeam(@RequestBody UpdateTeamRequest updateTeamRequest, HttpServletRequest request) {
        if(updateTeamRequest == null) return false;

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(updateTeamRequest,loginUser);
        if(!result) return false;

        return true;
    }

    @GetMapping("/get")
    public Team getTeam(long id) {
        if(id <= 0) return null;
        Team team = teamService.getById(id);

        if(team == null) return null;

        return team;
    }

    /**
     * 全量查询所有队伍
     * @param teamQuery 队伍查询封装类
     * @return 队伍
     */
    @GetMapping("/list")
    public List<TeamUserVo> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) return null;

        boolean isAdmin = userService.isAdmin(request);
        List<TeamUserVo> teamList = teamService.listTeam(teamQuery, isAdmin);
        return teamList;
    }

    /**
     * 分页查询队伍
     * @param teamQuery 队伍查询封装类
     * @return 队伍
     */
    @GetMapping("/list/page")
    public Page<Team> listTeamByPage(TeamQuery teamQuery) {
        if(teamQuery == null) return null;

        Team team = new Team();
        BeanUtils.copyProperties(team, teamQuery);

        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> pageDataList = teamService.page(page, queryWrapper);

        return pageDataList;
    }

    @PostMapping("/join")
    public Boolean joinTeam(@RequestBody JoinTeamRequest joinTeamRequest, HttpServletRequest request){
        if(joinTeamRequest == null) return false;

        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) return false;

        boolean result = teamService.joinTeam(joinTeamRequest, loginUser);
        return result;
    }
}
