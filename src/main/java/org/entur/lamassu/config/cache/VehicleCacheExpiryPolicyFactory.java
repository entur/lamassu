package org.entur.lamassu.config.cache;

import javax.cache.configuration.Factory;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

public class VehicleCacheExpiryPolicyFactory implements Factory<ExpiryPolicy> {
    @Override
    public ExpiryPolicy create() {
        return new CustomExpiryPolicy(Duration.ONE_MINUTE, null, Duration.ONE_MINUTE);
    }
}
