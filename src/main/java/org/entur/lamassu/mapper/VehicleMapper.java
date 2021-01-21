package org.entur.lamassu.mapper;

import org.entur.lamassu.model.PricingPlan;
import org.entur.lamassu.model.Vehicle;
import org.entur.lamassu.model.VehicleType;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.springframework.stereotype.Component;

@Component
public class VehicleMapper {

    public Vehicle mapVehicle(FreeBikeStatus.Bike bike, VehicleType vehicleType, PricingPlan pricingPlan) {
        var vehicle = new Vehicle();
        vehicle.setId(bike.getBikeId());
        vehicle.setLat(bike.getLat());
        vehicle.setLon(bike.getLon());
        vehicle.setReserved(bike.getReserved());
        vehicle.setDisabled(bike.getDisabled());
        vehicle.setCurrentRangeMeters(bike.getCurrentRangeMeters());
        vehicle.setVehicleType(vehicleType);
        vehicle.setPricingPlan(pricingPlan);
        return vehicle;
    }
}