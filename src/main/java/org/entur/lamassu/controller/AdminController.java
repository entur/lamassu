package org.entur.lamassu.controller;

import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.service.GeoSearchService;
import org.redisson.api.RFuture;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/admin")
@Profile("leader")
public class AdminController {
    private final RedissonClient redissonClient;
    private final GeoSearchService geoSearchService;

    @Value("${org.entur.lamassu.serializationVersion}")
    private String serializationVersion;

    @Autowired
    public AdminController(RedissonClient redissonClient, GeoSearchService geoSearchService) {
        this.redissonClient = redissonClient;
        this.geoSearchService = geoSearchService;
    }

    @GetMapping("/cache_keys")
    public Collection<String> getCacheKeys() {
        return StreamSupport.stream(
                redissonClient.getKeys().getKeys().spliterator(), false).collect(Collectors.toList());
    }

    @GetMapping("/vehicle_orphans")
    public Collection<String> getOrphans() {
        return geoSearchService.getVehicleSpatialIndexOrphans();
    }

    @DeleteMapping("/vehicle_orphans")
    public Collection<String> clearOrphans() {
        return geoSearchService.removeVehicleSpatialIndexOrphans();
    }

    @PostMapping("/clear_db")
    public RFuture<Void> clearDb() {
        return redissonClient.getKeys().flushdbParallelAsync();
   }

    @PostMapping("/clear_old_cache")
    public List<String> clearOldCache() {
        var keys = redissonClient.getKeys();
        List<String> deletedKeys = new java.util.ArrayList<>();
        keys.getKeys().forEach(key -> {
            if (!key.endsWith("_" + serializationVersion)) {
                keys.delete(key);
                deletedKeys.add(key);
            }
        });
        return deletedKeys;
    }
}
