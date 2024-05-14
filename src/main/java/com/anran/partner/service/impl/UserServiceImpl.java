package com.anran.partner.service.impl;

import com.anran.partner.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.anran.partner.model.domain.User;
import com.anran.partner.mapper.UserMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.anran.partner.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author DELL
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-05-08 14:38:24
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {
    /**
     * 盐值对密码进行加密
     */
    public static final String SALT = "anran";
    @Resource
    private UserMapper userMapper;
    @Override
    public Integer register(String userAccount, String userPassoword, String checkPassword, String planetCode) {

        // 1.校验
        if(StringUtils.isAnyBlank(userAccount, userPassoword, checkPassword, planetCode)) {
            return -1;
        }
        if(userAccount.length() < 4|| userPassoword.length() < 8 || checkPassword.length() < 5) {
            return -1;
        }
        //账号不能包含特殊字符
        String ValidePattern = "[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？%+_]";
        Matcher matcher = Pattern.compile(ValidePattern).matcher(userAccount);
        if(matcher.find()) {
            return -1;
        }
        // 两次密码不同
        if(!userPassoword.equals(checkPassword)) {
            return -1;
        }
        // 账号不能重复
        QueryWrapper <User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count();
        if(count > 1) {
            return -1;
        }
        // 星球编号重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = this.count();
        if(count > 1) {
            return -1;
        }

        // 2. 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassoword).getBytes());

        // 3. 插入数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user); // 这是service层的逻辑方法
        if(!saveResult) {
            return -1;
        }

        return user.getId(); // 在User类中更改id->Integer
    }

    @Override
    public User login(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if(StringUtils.isAnyBlank(userAccount, userPassword)) return null;
        if(userAccount.length() < 4 || userPassword.length() < 8) return null;

        String validePattern =  "[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）——|{}【】‘；：”“'。，、？%+_]";
        Matcher matcher = Pattern.compile(validePattern).matcher(userAccount);
        if(matcher.find()) return null;

        // 2. 校验密码是否正确
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if(user ==null) return null;

        // 3. 用户信息脱敏
        User saftyUser = getSaftyUser(user);

        // 4. 记录登录态
        // session就是个map,将Attribute当做map使用即可
        request.getSession().setAttribute(USER_LOGIN_STATE, saftyUser);

        return saftyUser;
    }

    /**
     * 用户脱敏，将不敏感信息返回，敏感的不返回
     * @param originalUser 脱敏前的用户
     * @return 返回脱敏后的用户,可以直接查询到的
     */
    @Override
    public User getSaftyUser(User originalUser) {
        if(originalUser == null) return null;

        User saftyUser = new User();
        saftyUser.setId(originalUser.getId());
        saftyUser.setUsername(originalUser.getUsername());
        saftyUser.setUserAccount(originalUser.getUserAccount());
        saftyUser.setAvatarUrl(originalUser.getAvatarUrl());
        saftyUser.setGender(originalUser.getGender());
        saftyUser.setPhone(originalUser.getPhone());
        saftyUser.setEmail(originalUser.getEmail());
        saftyUser.setPlanetCode(originalUser.getPlanetCode());
        saftyUser.setUserRole(originalUser.getUserRole());
        saftyUser.setUserStatus(originalUser.getUserStatus());
        saftyUser.setCreateTime(originalUser.getCreateTime());
        saftyUser.setTags(originalUser.getTags());

        return saftyUser;
    }

    @Override
    public int logout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        return 1;
    }

    /**
     * 通过标签查询用户
     *
     * @param tagNamelist 用户拥有的标签
     * @return 脱敏后的用户
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNamelist) {
        if(CollectionUtils.isEmpty(tagNamelist)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for(String tagName : tagNamelist) {
            // like会自动帮我们添加 %实现模糊查询
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSaftyUser).collect(Collectors.toList());

//        // 1. 先查询所有用户
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        List<User> userList = userMapper.selectList(queryWrapper);
//        Gson gson = new Gson();
//        // 2. 在内存中判断是否包含要求的标签(stream()API)
//        return userList.stream().filter(user -> {
//            String tagStr = user.getTags();
//            if(StringUtils.isBlank(tagStr)) {
//                return false;
//            }
//            // json 转 字符串
//            Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>(){}.getType());
//            //            gson.toJson(tempTagNameList); 这是序列化还是反序列化
//            for(String tagName : tagNamelist) {
//                if(!tempTagNameSet.contains(tagName)) {
//                    return false;
//                }
//            }
//            return true;
//        }).map(this::getSaftyUser).collect(Collectors.toList());
    }
}