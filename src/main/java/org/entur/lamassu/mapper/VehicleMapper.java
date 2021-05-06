package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    private final RentalUrisMapper rentalUrisMapper;

    @Autowired
    public VehicleMapper(RentalUrisMapper rentalUrisMapper) {
        this.rentalUrisMapper = rentalUrisMapper;
    }

    public Vehicle mapVehicle(FreeBikeStatus.Bike bike, VehicleType vehicleType, PricingPlan pricingPlan, System system) {
        var vehicle = new Vehicle();
        vehicle.setId(bike.getBikeId());
        vehicle.setLat(bike.getLat());
        vehicle.setLon(bike.getLon());
        vehicle.setReserved(bike.getReserved());
        vehicle.setDisabled(bike.getDisabled());
        vehicle.setCurrentRangeMeters(bike.getCurrentRangeMeters());
        vehicle.setVehicleType(vehicleType);
        vehicle.setPricingPlan(pricingPlan);
        vehicle.setRentalUris(rentalUrisMapper.mapRentalUris(bike.getRentalUris()));
        vehicle.setSystem(system);
        return vehicle;
    }
}
