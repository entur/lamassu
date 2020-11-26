package org.entur.lamassu.model;

import lombok.Getter;
import lombok.Setter;

public class FeedProvider {
    @Getter @Setter private String codespace;
    @Getter @Setter private String city;
    @Getter @Setter private String vehicleType;
    @Getter @Setter private String url;
}
