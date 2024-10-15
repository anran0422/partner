package com.anran.partner.controller;

import com.anran.partner.model.domain.User;
import com.anran.partner.model.domain.request.UserLogin;
import com.anran.partner.model.domain.request.UserRegister;
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
import java.util.stream.Collectors;

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

    @PostMapping("/login")
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

    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if(request == null) return null;

        return userService.logout(request);
    }

    /**
     * 这个应该应用在哪里呢？获取用户头像等之类的时候
     * @param request 用户请求
     * @return 脱敏后的用户
     */
    @GetMapping("/current")
    public User currentUser(HttpServletRequest request) {

        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObject;
        // 用户是否登录
        if(currentUser == null) return null;
        // 查询数据库
        Integer userId = currentUser.getId();
        // 脱敏后将用户返回
        User user = userService.getById(userId);

        return userService.getSaftyUser(user);
    }

    @GetMapping("/search/tags")
    public List<User> searchUserByTags(@RequestParam(required=false) List<String> tagNameList) {
        if(tagNameList == null) return null;

        List<User> userList = userService.searchUserByTags(tagNameList);
        return userList;
    }

    @GetMapping("/recommend")
    public Page<User> recommendUsers(long pageSize, long pageNum,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("partner:user:recommend:%s", loginUser.getId());

        // 有缓存先取缓存
        Page<User> userListPage = (Page<User>) redisTemplate.opsForValue().get(redisKey);
        if(userListPage != null) {
            return userListPage;
        }

        // 没有缓存再查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        userListPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        try {
            redisTemplate.opsForValue().set(redisKey, userListPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("设置用户错误",e);
        }
        return userListPage;
    }

    @PostMapping("/update")
    public String updateUser(@RequestBody User user,HttpServletRequest request) {
        // 判空
        if(user == null) {
            return "接收参数错误";
        }
        // 我们做内外两层鉴权，不仅是service层鉴权，这一层也进行鉴权，为了保险
        // 但是既然是为了鉴权，不如直接在 getLoginUser 内部鉴权，这还省了一些代码量
        User loginUser = userService.getLoginUser(request);
//        if(loginUser == null) {
//            return "用户未登录";
//        }
        int userID = userService.updateUser(user, loginUser);
        return "修改成功,用户ID为：" + userID;
    }
}
