package org.entur.lamassu.util;

import org.entur.lamassu.cache.VehicleSpatialIndexId;
import org.entur.lamassu.model.provider.FeedProvider;
import org.entur.lamassu.model.entities.FormFactor;
import org.entur.lamassu.model.entities.PropulsionType;
import org.entur.lamassu.model.entities.Vehicle;
import org.entur.lamassu.model.entities.VehicleType;
import org.entur.lamassu.service.VehicleFilterParameters;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SpatialIndexIdFilterTest {

    @Test
    public void testNoFilter() {
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId(), testFilterParams())
        );
    }

    @Test
    public void testCodespaceFilter() {
        var testId = testId();
        var params = testFilterParams();

        params.setCodespaces(List.of("TST"));
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );

        params.setCodespaces(List.of("FOO"));
        Assert.assertFalse(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );
    }

    @Test
    public void testSystemFilter() {
        var testId = testId();
        var params = testFilterParams();

        params.setSystems(List.of("TST:System:testprovider"));
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );

        params.setSystems(List.of("FOO:System:foo"));
        Assert.assertFalse(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );
    }

    @Test
    public void testOperatorFilter() {
        var testId = testId();
        var params = testFilterParams();

        params.setOperators(List.of("TST:Operator:test"));
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );

        params.setOperators(List.of("FOO:Operator:foo"));
        Assert.assertFalse(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );
    }

    @Test
    public void testFormFactorFilter() {
        var testId = testId();
        var params = testFilterParams();

        params.setFormFactors(List.of(FormFactor.SCOOTER));
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );

        params.setFormFactors(List.of(FormFactor.BICYCLE));
        Assert.assertFalse(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );
    }

    @Test
    public void testPropulsionTypeFilter() {
        var testId = testId();
        var params = testFilterParams();

        params.setPropulsionTypes(List.of(PropulsionType.ELECTRIC));
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );

        params.setPropulsionTypes(List.of(PropulsionType.COMBUSTION));
        Assert.assertFalse(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );
    }

    @Test
    public void testIncludeReservedFilter() {
        var testId = testReservedId();
        var params = testFilterParams();

        params.setIncludeReserved(true);
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );

        params.setIncludeReserved(false);
        Assert.assertFalse(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );
    }

    @Test
    public void testIncludeDisabledFilter() {
        var testId = testDisabledId();
        var params = testFilterParams();

        params.setIncludeDisabled(true);
        Assert.assertTrue(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );

        params.setIncludeDisabled(false);
        Assert.assertFalse(
                SpatialIndexIdFilter.filterVehicle(testId, params)
        );
    }

    private VehicleSpatialIndexId testId() {
        return VehicleSpatialIndexId.fromString(SpatialIndexIdUtil.createVehicleSpatialIndexId(testVehicle(), testProvider()));
    }

    private VehicleSpatialIndexId testReservedId() {
        var vehicle = testVehicle();
        vehicle.setReserved(true);
        return VehicleSpatialIndexId.fromString(SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, testProvider()));
    }

    private VehicleSpatialIndexId testDisabledId() {
        var vehicle = testVehicle();
        vehicle.setDisabled(true);
        return VehicleSpatialIndexId.fromString(SpatialIndexIdUtil.createVehicleSpatialIndexId(vehicle, testProvider()));
    }

    private Vehicle testVehicle() {
        var vehicle = new Vehicle();
        vehicle.setId("TST:Vehicle:abc123");
        vehicle.setReserved(false);
        vehicle.setDisabled(false);
        vehicle.setVehicleType(scooterVehicle());
        return vehicle;
    }

    private VehicleType scooterVehicle() {
        var type = new VehicleType();
        type.setId("TST:VehicleType:Scooter");
        type.setFormFactor(FormFactor.SCOOTER);
        type.setPropulsionType(PropulsionType.ELECTRIC);
        return type;
    }

    private FeedProvider testProvider() {
        var provider = new FeedProvider();
        provider.setCodespace("TST");
        provider.setSystemId("TST:System:testprovider");
        provider.setOperatorId("TST:Operator:test");
        provider.setOperatorName("testprovider");
        return provider;
    }

    private VehicleFilterParameters testFilterParams() {
        var params = new VehicleFilterParameters();
        params.setIncludeReserved(false);
        params.setIncludeDisabled(false);
        return params;
    }
}
