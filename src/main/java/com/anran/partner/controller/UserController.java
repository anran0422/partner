package com.anran.partner.controller;

import com.anran.partner.common.BaseResponse;
import com.anran.partner.common.ErrorCode;
import com.anran.partner.common.ResponseResult;
import com.anran.partner.exception.BusinessException;
import com.anran.partner.model.domain.User;
import com.anran.partner.model.request.UserLogin;
import com.anran.partner.model.request.UserRegister;
import com.anran.partner.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.anran.partner.constant.UserConstant.USER_LOGIN_STATE;

@Slf4j
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"}, allowedHeaders = {"*"}, allowCredentials = "true")
public class UserController {

    @Resource
    UserService userService;

    @Resource
    RedisTemplate redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Integer> userRegister(@RequestBody UserRegister userRegister) { // 账号 密码 检验密码 星球编号
        if(userRegister == null) {
//            return ResponseResult.error(ErrorCode.PARAMS_ERROR);
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegister.getUserAccount();
        String userPassoword = userRegister.getUserPassword();
        String checkPassword = userRegister.getCheckPassword();
        String planetCode = userRegister.getPlanetCode();

        if(StringUtils.isAnyBlank(userAccount,userPassoword,checkPassword,planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Integer result = userService.register(userAccount, userPassoword, checkPassword, planetCode);
        return ResponseResult.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLogin userLogin, HttpServletRequest request) {
        if(userLogin == null) {
            return null;
        }
        String userAccount = userLogin.getUserAccount();
        String userPassword = userLogin.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            return null;
        }

        User user = userService.login(userAccount, userPassword, request);
        return ResponseResult.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if(request == null) return null;

        int logoutUserId = userService.logout(request);
        return ResponseResult.success(logoutUserId);
    }

    /**
     * 这个应该应用在哪里呢？获取用户头像等之类的时候
     * @param request 用户请求
     * @return 脱敏后的用户
     */
    @GetMapping("/current")
    public BaseResponse<User> currentUser(HttpServletRequest request) {

        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        // 用户是否登录
        if(currentUser == null) return null;
        // 查询数据库
        long userId = currentUser.getId();
        // 脱敏后将用户返回
        User user = userService.getById(userId);

        User saftyUser = userService.getSaftyUser(user);

        return ResponseResult.success(saftyUser);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUserByTags(@RequestParam(required=false) List<String> tagNameList) {
        if(tagNameList == null) return null;

        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResponseResult.success(userList);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("partner:user:recommend:%s", loginUser.getId());

        // 有缓存先取缓存
        Page<User> userListPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        if(userListPage != null) {
            return ResponseResult.success(userListPage);
        }

        // 没有缓存再查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userListPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            redisTemplate.opsForValue().set(redisKey, userListPage, 3000, TimeUnit.MILLISECONDS); // 3000 or -1
        } catch (Exception e) {
            log.error("设置用户错误",e);
        }
        return ResponseResult.success(userListPage);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request) {
        // 判空
        if(user == null) {
            return null;
        }
        User loginUser = userService.getLoginUser(request);
        int userID = userService.updateUser(user, loginUser);
        return ResponseResult.success(userID);
    }

    /**
     * 获取最匹配的用户
     * 不宜过大，否则把数据库都拿走了
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if(num <= 0 || num > 20) {
            return null;
        }
        User loginUser = userService.getLoginUser(request);
        List<User> userList = userService.matchUsers(num, loginUser);
        return ResponseResult.success(userList);
    }
}
