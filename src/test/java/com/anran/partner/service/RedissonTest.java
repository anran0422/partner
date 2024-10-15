package com.anran.partner.service;

import com.anran.partner.model.domain.User;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void testRedisson() {
        // list 数据存储在本地 JVM 内存中
        List<String> list = new ArrayList<>();
        list.add("a");
        System.out.println("list:" + list.get(0));
//        list.remove(0);

        // 数据存储在 redis 内存中
        RList<String> rList = redissonClient.getList("test-list");
        rList.add("a");
        System.out.println("rList:" + rList.get(0));
        rList.remove(0);

        // map

        // set

        // stack

    }

    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("partner:preCacheJob:lock"); // 设置 键
        try {
            // 只有一个线程能获取到锁
            if(lock.tryLock(0,-1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(300000);
                System.out.println("getLock:" + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        } finally {
            // 是否是当前线程获取的锁，可以释放锁
            if(lock.isHeldByCurrentThread()) {
                System.out.println("unlock" + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
