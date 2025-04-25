package com.anran.partner.controller;

import com.anran.partner.common.BaseResponse;
import com.anran.partner.common.ResponseResult;
import com.anran.partner.model.DTO.TeamQuery;
import com.anran.partner.model.VueObject.TeamUserVo;
import com.anran.partner.model.VueObject.UserVo;
import com.anran.partner.model.domain.Team;
import com.anran.partner.model.domain.User;
import com.anran.partner.model.domain.UserTeam;
import com.anran.partner.model.request.*;
import com.anran.partner.service.TeamService;
import com.anran.partner.service.UserService;
import com.anran.partner.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"}, allowedHeaders = {"*"}, allowCredentials = "true")
public class TeamController {

    @Resource
    private UserService userService;
    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody AddTeamRequest addTeamRequest, HttpServletRequest request) {
        if(addTeamRequest == null) return null;

        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(addTeamRequest, team);
        long teamId = teamService.addTeam(team, loginUser);

        return ResponseResult.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteTeamRequest deleteTeamRequest, HttpServletRequest request) {
        if(deleteTeamRequest ==null) return null;
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(deleteTeamRequest, loginUser);
        return ResponseResult.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody UpdateTeamRequest updateTeamRequest, HttpServletRequest request) {
        if(updateTeamRequest == null) return null;

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(updateTeamRequest,loginUser);
        if(!result) return null;

        return ResponseResult.success(result);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id) {
        if(id <= 0) return null;
        Team team = teamService.getById(id);

        if(team == null) return null;

        return ResponseResult.success(team);
    }

    /**
     * 全量查询所有队伍
     * @param teamQuery 队伍查询封装类
     * @return 队伍
     */
    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) return null;

        boolean isAdmin = userService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamUserVo> teamList = teamService.listTeam(teamQuery, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVo::getId).collect(Collectors.toList());
        // 2、关联查询当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {}
        // 3. 关联查询加入队伍的用户信息（人数）
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍id -> 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });

        return ResponseResult.success(teamList);
    }

    // todo 完善接口逻辑
    /**
     * 分页查询队伍
     * @param teamQuery 队伍查询封装类
     * @return 队伍
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamByPage(TeamQuery teamQuery) {
        if(teamQuery == null) return null;

        Team team = new Team();
        BeanUtils.copyProperties(team, teamQuery);

        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> pageDataList = teamService.page(page, queryWrapper);

        return ResponseResult.success(pageDataList);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody JoinTeamRequest joinTeamRequest, HttpServletRequest request){
        if(joinTeamRequest == null) return null;

        User loginUser = userService.getLoginUser(request);
        if(loginUser == null) return null;

        boolean result = teamService.joinTeam(joinTeamRequest, loginUser);
        return ResponseResult.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody QuitTeamRequest quitTeamRequest, HttpServletRequest request){
        if(quitTeamRequest == null) return null;

        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(quitTeamRequest, loginUser);
        return ResponseResult.success(result);
    }

    /**
     * 查询创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/create")
    public BaseResponse<List<TeamUserVo>>  listMyCreateTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) return null;

        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId().longValue()); // todo Integer 转 Long
        List<TeamUserVo> teamList = teamService.listTeam(teamQuery, true); // todo 这里只是为了复用
        return ResponseResult.success(teamList);
    }

    /**
     * 查询加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/join")
    public BaseResponse<List<TeamUserVo>>  listMyJoinTeam(TeamQuery teamQuery, HttpServletRequest request) {
        if(teamQuery == null) return null;
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();

        User loginUser = userService.getLoginUser(request);
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);

        // 取出不重复的队伍 Id
        // teamId userId
        // 1 2
        // 1 3
        // 2 3
        // result
        // 1 -> [2,3]
        // 2 -> [3]
        Map<Long, List<UserTeam>> listMap = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVo> teamList = teamService.listTeam(teamQuery, true);
        return ResponseResult.success(teamList);
    }
}
