package org.entur.lamassu.mapper;

import org.entur.lamassu.model.entities.PricingPlan;
import org.entur.lamassu.model.entities.RentalUris;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
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
        vehicle.setRentalUris(mapRentalUris(bike.getRentalUris()));
        return vehicle;
    }

    private RentalUris mapRentalUris(org.entur.lamassu.model.gbfs.v2_1.RentalUris rentalUris) {
        if (rentalUris == null) {
            return null;
        }

        var mapped = new RentalUris();
        mapped.setAndroid(rentalUris.getAndroid());
        mapped.setIos(rentalUris.getIos());
        mapped.setWeb(rentalUris.getWeb());
        return mapped;
    }
}
