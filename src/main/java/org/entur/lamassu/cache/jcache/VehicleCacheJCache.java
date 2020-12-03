package org.entur.lamassu.cache.jcache;

import org.entur.lamassu.cache.VehicleCache;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Component
public class VehicleCacheJCache implements VehicleCache {

    @Autowired
    Cache<String, FreeBikeStatus.Bike> cache;

    @Override
    public List<FreeBikeStatus.Bike> getAll(Set<String> keys) {
        return new ArrayList<>(cache.getAll(keys).values());
    }

    @Override
    public void updateAll(List<FreeBikeStatus.Bike> vehicles) {
        cache.putAll(
                vehicles.stream().reduce(
                        new HashMap<>(),
                        ((acc, bike) -> {
                            acc.put(bike.getBikeId(), bike);
                            return acc;
                        }),
                        ((acc1, acc2) -> {
                            acc1.putAll(acc2);
                            return acc1;
                        })
                )
        );
    }
}
