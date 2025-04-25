package com.anran.partner.service;


import com.anran.partner.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class InsertUsersTest {
    @Resource
    private UserService userService;

    private ExecutorService executorService = new ThreadPoolExecutor(16, 1000, 10000,
            TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));

    /**
     * 顺序插入 10w 批量插入
     * 分10批次 21.978s
     */
//    @Test
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        final int INSERT_NUMBER = 100000;
        List<User> userList = new ArrayList<>();
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
            user.setTags("[]");
//            user.setProfile("这个人很懒，什么都没有留下");
            userList.add(user);
        }
        userService.saveBatch(userList, 10000);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }

    /**
     * 异步执行 10w
     * 默认线程池 分10组 1w一组 19.252s
     * 使用定义的线程池 分40 现成 2500一组 39.923s
     */
//    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUMBER = 100000;
        // 10万数据分成 10组
        int cnt = 0;
        int bach_size = 2500;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for(int i = 0;i < 40;i++) {
            List<User> userList = new ArrayList<>();

            while (true) {
                cnt++;
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
                user.setTags("[]");
//            user.setProfile("这个人很懒，什么都没有留下");
                userList.add(user);
                if (cnt % 10000 == 0) break;
            } // while
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName" + Thread.currentThread().getName() + "开始执行");
                userService.saveBatch(userList, bach_size);
            },executorService);
            futureList.add(future);
        }// for
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join(); // 等待所有任务执行完成 开始执行下一步
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
