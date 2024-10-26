package com.anran.partner.service.impl;


import com.anran.partner.mapper.TeamMapper;
import com.anran.partner.model.DTO.TeamQuery;
import com.anran.partner.model.Enums.TeamStatusEnum;
import com.anran.partner.model.VueObject.TeamUserVo;
import com.anran.partner.model.VueObject.UserVo;
import com.anran.partner.model.domain.Team;
import com.anran.partner.model.domain.User;
import com.anran.partner.model.domain.UserTeam;
import com.anran.partner.model.request.DeleteTeamRequest;
import com.anran.partner.model.request.JoinTeamRequest;
import com.anran.partner.model.request.QuitTeamRequest;
import com.anran.partner.model.request.UpdateTeamRequest;
import com.anran.partner.service.TeamService;
import com.anran.partner.service.UserService;
import com.anran.partner.service.UserTeamService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.time.LocalTime.now;

/**
* @author DELL
* @description 针对表【team(队伍表)】的数据库操作Service实现
* @createDate 2024-10-15 11:40:31
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User loginUser) {
        // 1. 请求参数是否为空
        if(team == null) return -1;
        // 2. 是否登录，未登录不允许创建
        if(loginUser == null) return -2;

        final long userId = loginUser.getId();
        // 3. 校验信息
            // a. 队伍人数 > 1且 <20
            // int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0); 这是因为该字段是int类型，使用的包装类，所以使用了这个进行了一个包装
        int maxNum = team.getMaxNum();
        if(maxNum < 1 || maxNum > 20) return -3;
            // b. 队伍标题 <= 20
        String teamName = team.getName();
        if(teamName.length() > 20 || teamName.isEmpty()) return -4;
            // c. 描述 <= 512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description) && description.length() > 512) return -5;
            // d. 是否公开（int） 不传默认为0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
        if(statusEnum == null) return -6;
            // e. status是加密状态，一定要有密码，且密码 <= 20
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)){
            if(StringUtils.isBlank(password) || password.length() > 20)
                return -7;
        }
            // f. 超时时间 > 当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)) return -8;
            // g. 校验用户最多创建5个队伍
        // TODO 有 BUG可能同时创建100个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNnum = this.count(queryWrapper);
        if(hasTeamNnum >= 5) return -9;

        // TODO 事务，要么都有，要么都没有
        // 4. 插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        Long teamId = team.getId();
        if(!save || teamId == null) return -10;

        // 5. 插入用户 => 队伍关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        save = userTeamService.save(userTeam);
        if(!save) return -11;

        return teamId;
    }

    @Override
    public List<TeamUserVo> listTeam(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        // 1. Mybatis Plus组合查询条件
        if(teamQuery != null) {
            Long id = teamQuery.getId();
            if(id != null && id >= 0) {
                queryWrapper.eq("id",id);
            }
            String searchText = teamQuery.getSearchText();
            if(StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("searchText",searchText).or().like("description",searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)) {
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)) {
                queryWrapper.like("description",description);
            }
            Long userId = teamQuery.getUserId();
            if(userId != null && userId >= 0) {
                queryWrapper.eq("userId",userId);
            }
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum != null && maxNum >= 0) {
                queryWrapper.eq("max_num",maxNum);
            }
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(status);
            if(statusEnum == null) {
                statusEnum = TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin && !statusEnum.equals(TeamStatusEnum.PUBLIC)) {
                System.out.println("无权限查询");
            }
            queryWrapper.eq("status",statusEnum.getValue());
        }
        // 不展示过期队伍
        // expireTime isNUll or expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime",new Date()).or().isNull("expireTime"));

        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVoList = new ArrayList<>();
        // 关联查询创建人信息
        for(Team team : teamList) {
            Long userId = team.getUserId();
            if(userId == null) {
                continue;
            }
            User user = userService.getById(userId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            // 对用户信息脱敏
            if(user != null) {
                UserVo userVo = new  UserVo();
                BeanUtils.copyProperties(user, userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return null;
    }

    @Override
    public boolean updateTeam(UpdateTeamRequest updateTeamRequest, User loginUser) {
        if(updateTeamRequest == null) {
            return false;
        }
        Long teamId = updateTeamRequest.getId();
        Team oldTeam = getTeamById(teamId);

        // 只有 管理员 或者 队伍创建者 才可以修改
        if(!userService.isAdmin(loginUser) && !oldTeam.getUserId().equals(loginUser.getId())) return false;
        TeamStatusEnum statusEnum = TeamStatusEnum.getTeamStatusByValue(updateTeamRequest.getStatus());
        if(statusEnum.equals(TeamStatusEnum.SECRET)) {
            if(StringUtils.isBlank(updateTeamRequest.getPassword())) {
                return false;
            }
        }

        Team updateTeam = new Team();
        BeanUtils.copyProperties(updateTeamRequest, updateTeam);
        boolean result = this.updateById(updateTeam);
        return result;
    }

    @Override
    public boolean joinTeam(JoinTeamRequest joinTeamRequest,User loginUser) {
        if(joinTeamRequest ==null) {
            return false;
        }
        Long teamId = joinTeamRequest.getTeamId();
        Team team = getTeamById(teamId);

        Date expireTime = team.getExpireTime();
        if (expireTime != null && expireTime.before(new Date())) {
            return false;
        }
        Integer teamStatus = team.getStatus();
        String password = joinTeamRequest.getPassword();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getTeamStatusByValue(teamStatus);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) { // 保证不为空 有值放在前 翻转 equals
            return false;
        } else if(TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if(StringUtils.isBlank(password) || !password.equals(team.getPassword())) {
                return false;
            }
        }
        // 用户已经加入队伍数量
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        long hasJoinNum = getTeamUserById(teamId);
        if(hasJoinNum >= 5) {
            return false;
        }
        // 不能重复加入已经加入的队伍
        userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("userId", userId);
        userTeamQueryWrapper.eq("teamId", teamId);
        long hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
        if(hasJoinTeam > 0) { // 已经加入
            return false;
        }
        // 该队伍的队伍人数
        long teamHasJoinNum = userTeamService.count(userTeamQueryWrapper);
        if(teamHasJoinNum >= team.getMaxNum()) {
            return false;
        }
        // 更新队伍信息
        UserTeam userTeam = new UserTeam();

        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        userTeam.setUpdateTme(new Date());

        boolean save = userTeamService.save(userTeam);
        return save;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(QuitTeamRequest quitTeamRequest, User loginUser) {
        if(quitTeamRequest == null) {
            return false;
        }
        Long teamId = quitTeamRequest.getTeamId();
        Team team = getTeamById(teamId);

        Long userId = loginUser.getId();
        UserTeam queryUserTeam = new UserTeam();
        queryUserTeam.setUserId(userId);
        queryUserTeam.setTeamId(teamId);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>(queryUserTeam);
        long count = userTeamService.count(queryWrapper);
        if(count <= 0) { // 未加入队伍
            return false;
        }
        long teamUserCount = getTeamUserById(teamId);
        // 队伍只有一人，解散
        if(teamUserCount == 1) {
            // 删除队伍 和 所有用户-队伍关系
            this.removeById(teamId);
            // 删除队伍所有的关系
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
            boolean result = userTeamService.remove(userTeamQueryWrapper);
            if(!result) {
                return false;
            }
        } else {
            // 队伍至少 2 人
            // 是否是队长
            if(team.getUserId() == userId) {
                // 把队伍转移给第二早的用户
                // 1. 查询已经加入队伍的 所有用户和 加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if(CollectionUtils.isEmpty(userTeamList) || userTeamList.size() < 2) {
                    return false;
                }
                UserTeam nextUserTeam = userTeamList.get(1);// 第二条数据
                Long nextTeamLeaderId = nextUserTeam.getUserId();

                // 更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(nextTeamLeaderId);
                updateTeam.setUpdateTme(new Date());

                boolean result = this.updateById(updateTeam);
                if(!result) {
                    return false;
                }
                // 移除当前用户的 用户-队伍关联表
                // 前面已经定义了 所以这里不重复定义
                // userTeamQueryWrapper = new QueryWrapper<>();
                // userTeamQueryWrapper.eq("teamId", teamId);
                // userTeamQueryWrapper.eq("userId", userId);
                return userTeamService.remove(queryWrapper);
            } else {
                // 不是队长
                return userTeamService.remove(queryWrapper);
            }
        }

        return false;
    }

    /**
     * 删除（解散）队伍
     * @param deleteTeamRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(DeleteTeamRequest deleteTeamRequest, User loginUser) {
        // 校验请求参数
        if(deleteTeamRequest == null) {
            return false;
        }
        // 校验队伍是否存在
        Team team = getTeamById(deleteTeamRequest.getTeamId());
        long teamId = team.getId();
        // 校验是不是队长
        if(team.getUserId() != loginUser.getId()) {
            return false;
        }
        // 移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        userTeamService.remove(userTeamQueryWrapper);
        // 删除队伍
        boolean res = this.removeById(teamId);
        if(!res) {
            System.out.println("删除队伍失败");
        }
        return res;
    }

    /**
     * 根据 id 查询队伍
     * @param teamId
     * @return
     */
    private Team getTeamById(Long teamId) {
        if(teamId == null || teamId <= 0) {
            return null;
        }
        Team team = this.getById(teamId);
        if(team == null) {
            return null;
        }
        return team;
    }


    /**
     * 计算某队伍，当前队伍人数
     * 在关联表中找关于 teamId 的数据条数
     * @param teamId
     * @return
     */
    private long getTeamUserById(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }
}




