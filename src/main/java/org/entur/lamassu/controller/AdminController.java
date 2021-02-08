package org.entur.lamassu.controller;

import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final RedissonClient redissonClient;

    @Autowired
    public AdminController(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @PostMapping("/clear_cache")
    public RFuture<Void> clearCache() {
        return redissonClient.getKeys().flushdbParallelAsync();
    }
}
