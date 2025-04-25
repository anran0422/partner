package com.anran.partner.service;

import com.anran.partner.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

//@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        // 增加
        valueOperations.set("anranString", "haha");
        valueOperations.set("anranInt", 123);
        valueOperations.set("anranFloat", 12.12f);

        User user = new User();
        user.setId(1);
        user.setUsername("anran");
        valueOperations.set("anranUser", user);

        // 查询
        Object anran = valueOperations.get("anranString");
        Assertions.assertTrue("haha".equals((String)anran));

        anran = valueOperations.get("anranInt");
        Assertions.assertTrue(123 == (Integer)anran);

        anran = valueOperations.get("anranFloat");
        Assertions.assertTrue(12.12f == (Float)anran);

        System.out.println(valueOperations.get("anranUser"));

        // 修改即重新 set
        valueOperations.set("anranString", "anran");

        // 删除
        redisTemplate.delete("anranUser");
    }
}
