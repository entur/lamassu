package org.entur.lamassu.controller;

import org.entur.lamassu.service.GeoSearchService;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final RedissonClient redissonClient;
    private final GeoSearchService geoSearchService;

    @Autowired
    public AdminController(RedissonClient redissonClient, GeoSearchService geoSearchService) {
        this.redissonClient = redissonClient;
        this.geoSearchService = geoSearchService;
    }

    @GetMapping("/vehicle_orphans")
    public Collection<String> getOrphans() {
        return geoSearchService.getVehicleSpatialIndexOrphans();
    }

    @DeleteMapping("/vehicle_orphans")
    public Collection<String> clearOrphans() {
        return geoSearchService.removeVehicleSpatialIndexOrphans();
    }

    @PostMapping("/clear_cache")
    public RFuture<Void> clearCache() {
        return redissonClient.getKeys().flushdbParallelAsync();
    }
}
