package org.entur.lamassu.controller;

import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.service.VehiclesNearbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VehiclesController {

    @Autowired
    VehiclesNearbyService vehiclesNearbyService;

    @GetMapping("/vehicles/nearby")
    public List<FreeBikeStatus.Bike> getVehiclesNearby(
            @RequestParam("lon") Double longitude,
            @RequestParam("lat") Double latitude,
            @RequestParam("range") Double range,
            @RequestParam("count") Integer count
    ) {
        return vehiclesNearbyService.getVehiclesNearby(
                longitude,
                latitude,
                range,
                count
        );
    }
}
