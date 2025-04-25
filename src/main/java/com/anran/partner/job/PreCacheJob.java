package com.anran.partner.job;

import com.anran.partner.mapper.UserMapper;
import com.anran.partner.model.domain.User;
import com.anran.partner.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  缓存定时任务
 *
 */

@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户
    private List<Long> mainUserIdList = Arrays.asList(4L);

    /**
     * 每天13:28:00执行，预热推荐用户
     */
    @Scheduled(cron = "0 03 20 * * *")
    public void doPreCacheJob() {
        RLock lock = redissonClient.getLock("partner:preCacheJob:lock"); // 设置 键
        try {
            // 只有一个线程能获取到锁
//            lock.tryLock(0,3000, TimeUnit.MILLISECONDS
            if(lock.tryLock(0,3000, TimeUnit.MILLISECONDS)) {
                for(Long userId : mainUserIdList) {
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userListPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String redisKey = String.format("partner:user:recommend:%s", userId);

                    try {
                        redisTemplate.opsForValue().set(redisKey, userListPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("预热数据失败", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doPreCacheJob 分布式锁错误",e);
        } finally {
            // 是否是当前线程获取的锁，可以释放锁
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
