package com.anran.partner.once;

import com.anran.partner.mapper.UserMapper;
import com.anran.partner.model.domain.User;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUMBER = 1000;
        for(int i = 0;i < INSERT_NUMBER;i++) {
            User user = new User();
            user.setUsername("jiayonghu");
            user.setUserAccount("yonghu");
            user.setAvatarUrl("https://img1.baidu.com/it/u=349272460,2526437403&fm=253&fmt=auto&app=138&f=JPEG?w=456&h=609");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("12378945610");
            user.setEmail("jia-yong-hu@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode(String.valueOf(10 + INSERT_NUMBER));
            user.setTags("[\"java\",\"python\",\"c++]");
//            user.setProfile("这个人很懒，什么都没有留下");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }
}

