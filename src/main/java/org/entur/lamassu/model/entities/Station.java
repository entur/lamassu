/*
 *
 *
 *  * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 *  * the European Commission - subsequent versions of the EUPL (the "Licence");
 *  * You may not use this work except in compliance with the Licence.
 *  * You may obtain a copy of the Licence at:
 *  *
 *  *   https://joinup.ec.europa.eu/software/page/eupl
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the Licence is distributed on an "AS IS" basis,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the Licence for the specific language governing permissions and
 *  * limitations under the Licence.
 *
 */

package org.entur.lamassu.model.entities;

import java.util.List;
import java.util.Objects;

public class Station implements LocationEntity {

  private String id;
  private TranslatedString name;
  private TranslatedString shortName;
  private Double lat;
  private Double lon;
  private String address;
  private String crossStreet;
  private Region region;
  private String postCode;
  private List<RentalMethod> rentalMethods;
  private Boolean isVirtualStation;
  private MultiPolygon stationArea;
  private ParkingType parkingType;
  private Boolean parkingHoop;
  private String contactPhone;
  private Integer capacity;
  private List<VehicleTypeCapacity> vehicleCapacity;
  private List<VehicleDocksCapacity> vehicleDocksCapacity;
  private List<VehicleTypeCapacity> vehicleTypeCapacity;
  private List<VehicleTypesCapacity> vehicleTypesCapacity;
  private Boolean isValetStation;
  private Boolean isChargingStation;
  private RentalUris rentalUris;
  private Integer numBikesAvailable;
  private Integer numVehiclesAvailable;
  private List<VehicleTypeAvailability> vehicleTypesAvailable;
  private Integer numBikesDisabled;
  private Integer numVehiclesDisabled;
  private Integer numDocksAvailable;
  private List<VehicleDocksAvailability> vehicleDocksAvailable;
  private Integer numDocksDisabled;
  private Boolean isInstalled;
  private Boolean isRenting;
  private Boolean isReturning;
  private Long lastReported;
  private System system;
  private List<PricingPlan> pricingPlans;
  private List<List<String>> stationAreaPolylineEncodedMultiPolygon;

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public TranslatedString getName() {
    return name;
  }

  public void setName(TranslatedString name) {
    this.name = name;
  }

  public TranslatedString getShortName() {
    return shortName;
  }

  public void setShortName(TranslatedString shortName) {
    this.shortName = shortName;
  }

  @Override
  public Double getLat() {
    return lat;
  }

  public void setLat(Double lat) {
    this.lat = lat;
  }

  @Override
  public Double getLon() {
    return lon;
  }

  public void setLon(Double lon) {
    this.lon = lon;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCrossStreet() {
    return crossStreet;
  }

  public void setCrossStreet(String crossStreet) {
    this.crossStreet = crossStreet;
  }

  public Region getRegion() {
    return region;
  }

  public void setRegion(Region region) {
    this.region = region;
  }

  public String getPostCode() {
    return postCode;
  }

  public void setPostCode(String postCode) {
    this.postCode = postCode;
  }

  public List<RentalMethod> getRentalMethods() {
    return rentalMethods;
  }

  public void setRentalMethods(List<RentalMethod> rentalMethods) {
    this.rentalMethods = rentalMethods;
  }

  public Boolean getVirtualStation() {
    return isVirtualStation;
  }

  public void setVirtualStation(Boolean virtualStation) {
    isVirtualStation = virtualStation;
  }

  public MultiPolygon getStationArea() {
    return stationArea;
  }

  public void setStationArea(MultiPolygon stationArea) {
    this.stationArea = stationArea;
  }

  public ParkingType getParkingType() {
    return parkingType;
  }

  public void setParkingType(ParkingType parkingType) {
    this.parkingType = parkingType;
  }

  public Boolean getParkingHoop() {
    return parkingHoop;
  }

  public void setParkingHoop(Boolean parkingHoop) {
    this.parkingHoop = parkingHoop;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }

  public Integer getCapacity() {
    return capacity;
  }

  public void setCapacity(Integer capacity) {
    this.capacity = capacity;
  }

  public List<VehicleTypeCapacity> getVehicleCapacity() {
    return vehicleCapacity;
  }

  public void setVehicleCapacity(List<VehicleTypeCapacity> vehicleCapacity) {
    this.vehicleCapacity = vehicleCapacity;
  }

  public List<VehicleDocksCapacity> getVehicleDocksCapacity() {
    return vehicleDocksCapacity;
  }

  public void setVehicleDocksCapacity(List<VehicleDocksCapacity> vehicleDocksCapacity) {
    this.vehicleDocksCapacity = vehicleDocksCapacity;
  }

  public List<VehicleTypeCapacity> getVehicleTypeCapacity() {
    return vehicleTypeCapacity;
  }

  public void setVehicleTypeCapacity(List<VehicleTypeCapacity> vehicleTypeCapacity) {
    this.vehicleTypeCapacity = vehicleTypeCapacity;
  }

  public List<VehicleTypesCapacity> getVehicleTypesCapacity() {
    return vehicleTypesCapacity;
  }

  public void setVehicleTypesCapacity(List<VehicleTypesCapacity> vehicleTypesCapacity) {
    this.vehicleTypesCapacity = vehicleTypesCapacity;
  }

  public Boolean getValetStation() {
    return isValetStation;
  }

  public void setValetStation(Boolean valetStation) {
    isValetStation = valetStation;
  }

  public Boolean getChargingStation() {
    return isChargingStation;
  }

  public void setChargingStation(Boolean chargingStation) {
    isChargingStation = chargingStation;
  }

  public RentalUris getRentalUris() {
    return rentalUris;
  }

  public void setRentalUris(RentalUris rentalUris) {
    this.rentalUris = rentalUris;
  }

  public Integer getNumBikesAvailable() {
    return numBikesAvailable;
  }

  public void setNumBikesAvailable(Integer numBikesAvailable) {
    this.numBikesAvailable = numBikesAvailable;
  }

  public Integer getNumVehiclesAvailable() {
    return numVehiclesAvailable;
  }

  public void setNumVehiclesAvailable(Integer numVehiclesAvailable) {
    this.numVehiclesAvailable = numVehiclesAvailable;
  }

  public List<VehicleTypeAvailability> getVehicleTypesAvailable() {
    return vehicleTypesAvailable;
  }

  public void setVehicleTypesAvailable(
    List<VehicleTypeAvailability> vehicleTypesAvailable
  ) {
    this.vehicleTypesAvailable = vehicleTypesAvailable;
  }

  public Integer getNumBikesDisabled() {
    return numBikesDisabled;
  }

  public void setNumBikesDisabled(Integer numBikesDisabled) {
    this.numBikesDisabled = numBikesDisabled;
  }

  public Integer getNumVehiclesDisabled() {
    return numVehiclesDisabled;
  }

  public void setNumVehiclesDisabled(Integer numVehiclesDisabled) {
    this.numVehiclesDisabled = numVehiclesDisabled;
  }

  public Integer getNumDocksAvailable() {
    return numDocksAvailable;
  }

  public void setNumDocksAvailable(Integer numDocksAvailable) {
    this.numDocksAvailable = numDocksAvailable;
  }

  public List<VehicleDocksAvailability> getVehicleDocksAvailable() {
    return vehicleDocksAvailable;
  }

  public void setVehicleDocksAvailable(
    List<VehicleDocksAvailability> vehicleDocksAvailable
  ) {
    this.vehicleDocksAvailable = vehicleDocksAvailable;
  }

  public Integer getNumDocksDisabled() {
    return numDocksDisabled;
  }

  public void setNumDocksDisabled(Integer numDocksDisabled) {
    this.numDocksDisabled = numDocksDisabled;
  }

  public Boolean getInstalled() {
    return isInstalled;
  }

  public void setInstalled(Boolean installed) {
    isInstalled = installed;
  }

  public Boolean getRenting() {
    return isRenting;
  }

  public void setRenting(Boolean renting) {
    isRenting = renting;
  }

  public Boolean getReturning() {
    return isReturning;
  }

  public void setReturning(Boolean returning) {
    isReturning = returning;
  }

  public Long getLastReported() {
    return lastReported;
  }

  public void setLastReported(Long lastReported) {
    this.lastReported = lastReported;
  }

  public System getSystem() {
    return system;
  }

  public void setSystem(System system) {
    this.system = system;
  }

  public List<PricingPlan> getPricingPlans() {
    return pricingPlans;
  }

  public void setPricingPlans(List<PricingPlan> pricingPlans) {
    this.pricingPlans = pricingPlans;
  }

  public void setStationAreaPolylineEncodedMultiPolygon(
    List<List<String>> stationAreaPolylineEncodedMultiPolygon
  ) {
    this.stationAreaPolylineEncodedMultiPolygon = stationAreaPolylineEncodedMultiPolygon;
  }

  public List<List<String>> getStationAreaPolylineEncodedMultiPolygon() {
    return stationAreaPolylineEncodedMultiPolygon;
  }

  @Override
  public String toString() {
    return (
      "Station{" +
      "id='" +
      id +
      '\'' +
      ", name=" +
      name +
      ", shortName=" +
      shortName +
      ", lat=" +
      lat +
      ", lon=" +
      lon +
      ", address='" +
      address +
      '\'' +
      ", crossStreet='" +
      crossStreet +
      '\'' +
      ", region=" +
      region +
      ", postCode='" +
      postCode +
      '\'' +
      ", rentalMethods=" +
      rentalMethods +
      ", isVirtualStation=" +
      isVirtualStation +
      ", stationArea=" +
      stationArea +
      ", parkingType=" +
      parkingType +
      ", parkingHoop=" +
      parkingHoop +
      ", contactPhone='" +
      contactPhone +
      '\'' +
      ", capacity=" +
      capacity +
      ", vehicleCapacity=" +
      vehicleCapacity +
      ", vehicleTypeCapacity=" +
      vehicleTypeCapacity +
      ", isValetStation=" +
      isValetStation +
      ", isChargingStation=" +
      isChargingStation +
      ", rentalUris=" +
      rentalUris +
      ", numBikesAvailable=" +
      numBikesAvailable +
      ", vehicleTypesAvailable=" +
      vehicleTypesAvailable +
      ", numBikesDisabled=" +
      numBikesDisabled +
      ", numDocksAvailable=" +
      numDocksAvailable +
      ", vehicleDocksAvailable=" +
      vehicleDocksAvailable +
      ", numDocksDisabled=" +
      numDocksDisabled +
      ", isInstalled=" +
      isInstalled +
      ", isRenting=" +
      isRenting +
      ", isReturning=" +
      isReturning +
      ", lastReported=" +
      lastReported +
      ", system=" +
      system +
      ", pricingPlans=" +
      pricingPlans +
      '}'
    );
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    var station = (Station) o;
    return id.equals(station.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
