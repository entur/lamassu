package org.entur.lamassu.cache;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;

import java.util.List;
import java.util.Set;

public interface VehicleCache {
    List<FreeBikeStatus.Bike> getAll(Set<String> keys);
    void updateAll(List<FreeBikeStatus.Bike> vehicles);
}
