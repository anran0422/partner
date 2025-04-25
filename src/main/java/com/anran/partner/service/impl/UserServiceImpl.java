package com.anran.partner.service.impl;

import com.anran.partner.common.ErrorCode;
import com.anran.partner.exception.BusinessException;
import com.anran.partner.mapper.UserMapper;
import com.anran.partner.model.domain.User;
import com.anran.partner.service.UserService;
import com.anran.partner.utils.AlgorithmUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.anran.partner.constant.UserConstant.ADMIN_ROLE;
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        if(userAccount.length() < 4|| userPassoword.length() < 8 || checkPassword.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号密码不符合规范");
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
     * 根据标签搜索用户（SQL 查询版)
     *
     * @param tagNameList 用户拥有的标签
     * @return 脱敏后的用户
     * 打上过期标签，说明不用
     */
    @Deprecated
    private List<User> searchUserByTagsSQL(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for(String tagName : tagNameList) {
            // like会自动帮我们添加 %实现模糊查询
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSaftyUser).collect(Collectors.toList());
    }

    /**
     * 根据标签查询搜索用户（内存过滤）
     * @param tagNamelist 标签列表
     * @return 返回脱敏的用户
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNamelist) {
        if(CollectionUtils.isEmpty(tagNamelist)) {
            return null;
        }
        // 1. 先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2. 在内存中判断是否包含要求的标签(stream()API)
        return userList.stream().filter(user -> {
            String tagStr = user.getTags();
            if(StringUtils.isBlank(tagStr)) {
                return false;
            }// json 反序列化 转为java对象

            Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>(){}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            //            gson.toJson(tempTagNameList); 这是序列化还是反序列化
            for(String tagName : tagNamelist) {
                if(!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSaftyUser).collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user,User loginUser) {
        long userId = user.getId();
        if(userId <= 0) return -1;
        // todo 补充校验，如果用户没有传任何更新的值，不调用sql语句
        // 校验权限：仅管理员以及用户可以修改
        // 如果是管理员，允许更新任意用户
        // 如果是用户，仅允许修改自己的信息
        if(isAdmin(loginUser) && userId !=loginUser.getId()) {
            return -1;
        }
        User targetUser = userMapper.selectById(userId);
        if(targetUser == null) return -1;
        userMapper.updateById(user);
        return user.getId();
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 鉴权
        if(userObj == null) {
            return null;
        }
        return (User)userObj;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 推荐匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    @Override
    public List<User> matchUsers(long num, User loginUser) {
//        这里我因为电脑内存问题，没有办法像鱼皮电脑那样可以存放100万数据，可以直接运行。所以我选择了运行5万条数据。
//        不然的话会报 OOM（内存）的问题
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.last("limit 50000");
//        List<User> userList = this.list(queryWrapper);

//         或者用page分页查询，自己输入或默认数值，但这样匹配就有限制了
//        List<User> userList = this.page(new Page<>(pageNum,pageSize),queryWrapper);

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("tags");
        queryWrapper.select("id","tags");
        List<User> userList = this.list(queryWrapper);

        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() { // Json字符串 -> 列表
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User,Long>> list = new ArrayList<>();
        // 依次计算当前用户和所有用户的相似度
        for (int i = 0; i <userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签的 或当前用户为自己
            if (StringUtils.isBlank(userTags) || user.getId().longValue() == loginUser.getId()){
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(Pair.of(user,distance));
        }
        //按编辑距离有小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        //有顺序的userID列表
        List<Integer> userListVo = topUserPairList.stream().map(pair -> {
            return pair.getKey().getId();
        }).collect(Collectors.toList());

        //根据id查询user完整信息
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id",userListVo);
        Map<Integer, List<User>> userIdUserListMap = this.list(userQueryWrapper).stream()
                .map(user -> getSaftyUser(user))
                .collect(Collectors.groupingBy(User::getId));

        // 因为上面查询打乱了顺序，这里根据上面有序的userID列表赋值
        List<User> finalUserList = new ArrayList<>();
        for (Integer userId : userListVo){
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

}