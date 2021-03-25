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
    private String name;
    private Double lat;
    private Double lon;
    private String address;
    private Integer capacity;
    private Integer numBikesAvailable;
    private Integer numDocksAvailable;
    private Boolean isInstalled;
    private Boolean isRenting;
    private Boolean isReturning;
    private Long lastReported;
    private System system;
    private List<PricingPlan> pricingPlans;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

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

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getNumBikesAvailable() {
        return numBikesAvailable;
    }

    public void setNumBikesAvailable(Integer numBikesAvailable) {
        this.numBikesAvailable = numBikesAvailable;
    }

    public Integer getNumDocksAvailable() {
        return numDocksAvailable;
    }

    public void setNumDocksAvailable(Integer numDocksAvailable) {
        this.numDocksAvailable = numDocksAvailable;
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

    @Override
    public String toString() {
        return "Station{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", address='" + address + '\'' +
                ", capacity=" + capacity +
                ", numBikesAvailable=" + numBikesAvailable +
                ", numDocksAvailable=" + numDocksAvailable +
                ", isInstalled=" + isInstalled +
                ", isRenting=" + isRenting +
                ", isReturning=" + isReturning +
                ", lastReported=" + lastReported +
                ", system=" + system +
                ", pricingPlans=" + pricingPlans +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Station station = (Station) o;
        return id.equals(station.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
