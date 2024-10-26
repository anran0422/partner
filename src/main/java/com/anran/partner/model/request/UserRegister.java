package com.anran.partner.model.request;

import lombok.Data;

/**
 * @author anran
 * 实现用户注册接口
 * 账号 密码 check密码 星球编号
 */
@Data
public class UserRegister {
    String userAccount;
    String userPassword;
    String checkPassword;
    String planetCode;
}
