package org.entur.lamassu.mapper;

import org.entur.gbfs.v2_2.free_bike_status.GBFSBike;
import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.System;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {
    private final RentalUrisMapper rentalUrisMapper;

    @Autowired
    public VehicleMapper(RentalUrisMapper rentalUrisMapper) {
        this.rentalUrisMapper = rentalUrisMapper;
    }

    public Vehicle mapVehicle(GBFSBike bike, VehicleType vehicleType, PricingPlan pricingPlan, System system) {
        var vehicle = new Vehicle();
        vehicle.setId(bike.getBikeId());
        vehicle.setLat(bike.getLat());
        vehicle.setLon(bike.getLon());
        vehicle.setReserved(bike.getIsReserved());
        vehicle.setDisabled(bike.getIsDisabled());
        vehicle.setCurrentRangeMeters(bike.getCurrentRangeMeters());
        vehicle.setVehicleType(vehicleType);
        vehicle.setPricingPlan(pricingPlan);
        vehicle.setRentalUris(rentalUrisMapper.mapRentalUris(bike.getRentalUris()));
        vehicle.setSystem(system);
        return vehicle;
    }
}
