package com.anran.partner.service;

import com.anran.partner.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author Anran
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-05-08 14:38:24
*/
public interface UserService extends IService<User> {

    /**
     *
     * @param userAccount 用户账号
     * @param userPassoword 用户密码
     * @param checkPassword 检验密码
     * @param planetCode 星球编码
     * @return 新用户id
     */
    Integer register(String userAccount,String userPassoword,String checkPassword,String planetCode);

    /**
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request 请求
     * @return 脱敏后的用户
     */
    User login(String userAccount, String userPassword, HttpServletRequest request);

    User getSaftyUser(User user);

    int logout(HttpServletRequest request);
}
