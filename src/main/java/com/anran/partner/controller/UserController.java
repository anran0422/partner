package com.anran.partner.controller;

import com.anran.partner.model.domain.User;
import com.anran.partner.model.domain.request.UserLogin;
import com.anran.partner.model.domain.request.UserRegister;
import com.anran.partner.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import static com.anran.partner.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;
    @RequestMapping("/register")
    public Integer userRegister(@RequestBody UserRegister userRegister) { // 账号 密码 检验密码 星球编号
        if(userRegister == null) {
            return -1;
        }

        String userAccount = userRegister.getUserAccount();
        String userPassoword = userRegister.getUserPassword();
        String checkPassword = userRegister.getCheckPassword();
        String planetCode = userRegister.getPlanetCode();

        if(StringUtils.isAnyBlank(userAccount,userPassoword,checkPassword,planetCode)) {
            return -1;
        }

        return userService.register(userAccount,userPassoword,checkPassword,planetCode);
    }


    @RequestMapping("/login")
    public User userLogin(@RequestBody UserLogin userLogin, HttpServletRequest request) {
        if(userLogin == null) {
            return null;
        }
        String userAccount = userLogin.getUserAccount();
        String userPassword = userLogin.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            return null;
        }

        return userService.login(userAccount,userPassword, request);
    }

    @RequestMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if(request == null) return null;

        return userService.logout(request);
    }

    /**
     * 这个应该应用在哪里呢？获取用户头像等之类的时候
     * @param request 用户请求
     * @return 脱敏后的用户
     */
    @RequestMapping("/current")
    public User currentUser(HttpServletRequest request) {

        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        // 用户是否登录
        if(currentUser == null) return null;
        // 查询数据库
        Integer userId = currentUser.getId();
        // 脱敏后将用户返回
        User user = userService.getById(userId);
        User safetyUser = userService.getSaftyUser(user);

        return safetyUser;
    }
}
