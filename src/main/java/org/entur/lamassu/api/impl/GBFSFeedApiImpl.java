package org.entur.lamassu.api.impl;

import org.entur.lamassu.api.GBFSFeedApi;
import org.entur.lamassu.model.FeedProvider;
import org.entur.lamassu.model.gbfs.v2_1.FreeBikeStatus;
import org.entur.lamassu.model.gbfs.v2_1.GBFS;
import org.entur.lamassu.model.gbfs.v2_1.GBFSBase;
import org.entur.lamassu.model.gbfs.v2_1.GBFSFeedName;
import org.entur.lamassu.model.gbfs.v2_1.GBFSVersions;
import org.entur.lamassu.model.gbfs.v2_1.GeofencingZones;
import org.entur.lamassu.model.gbfs.v2_1.StationInformation;
import org.entur.lamassu.model.gbfs.v2_1.StationStatus;
import org.entur.lamassu.model.gbfs.v2_1.SystemAlerts;
import org.entur.lamassu.model.gbfs.v2_1.SystemCalendar;
import org.entur.lamassu.model.gbfs.v2_1.SystemHours;
import org.entur.lamassu.model.gbfs.v2_1.SystemInformation;
import org.entur.lamassu.model.gbfs.v2_1.SystemPricingPlans;
import org.entur.lamassu.model.gbfs.v2_1.SystemRegions;
import org.entur.lamassu.model.gbfs.v2_1.VehicleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class GBFSFeedApiImpl implements GBFSFeedApi {
    private final WebClient webClient;

    public GBFSFeedApiImpl(@Autowired WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<GBFS> getDiscoveryFeed(FeedProvider feedProvider) {
        return get(feedProvider.getUrl(), GBFS.class);
    }

    @Override
    public Mono<? extends GBFSBase> getFeed(GBFS discoveryFeed, GBFSFeedName feedName, String language) {
        return discoveryFeed.getData().get(language).getFeeds().stream()
                .filter(feed -> feedName.equals(feed.getName()))
                .findFirst()
                .map(GBFS.GBFSFeed::getUrl)
                .map(url -> get(url, getType(feedName)))
                .orElse(null);
    }

    private <T extends GBFSBase> Mono<T> get(String url, Class<T> type) {
        return webClient.get()
                .uri(url)
                .exchange()
                .flatMap(res -> res.bodyToMono(type));
    }

    private Class<? extends GBFSBase> getType(GBFSFeedName feedName) {
        switch (feedName) {
            case GBFS:
                return GBFS.class;
            case GBFS_VERSIONS:
                return GBFSVersions.class;
            case SYSTEM_INFORMATION:
                return SystemInformation.class;
            case VEHICLE_TYPES:
                return VehicleTypes.class;
            case STATION_INFORMATION:
                return StationInformation.class;
            case STATION_STATUS:
                return StationStatus.class;
            case FREE_BIKE_STATUS:
                return FreeBikeStatus.class;
            case SYSTEM_HOURS:
                return SystemHours.class;
            case SYSTEM_CALENDAR:
                return SystemCalendar.class;
            case SYSTEM_REGIONS:
                return SystemRegions.class;
            case SYSTEM_PRICING_PLANS:
                return SystemPricingPlans.class;
            case SYSTEM_ALERTS:
                return SystemAlerts.class;
            case GEOFENCING_ZONES:
                return GeofencingZones.class;
            default:
                throw new HttpServerErrorException(HttpStatus.NOT_IMPLEMENTED);
        }
    }
}
