package org.entur.lamassu.service;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;

import java.util.List;

public interface VehiclesNearbyService {
    List<FreeBikeStatus.Bike> getVehiclesNearby(Double longitude, Double latitude, Double range, Integer count);
}
