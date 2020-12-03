package org.entur.lamassu.config.cache;

import javax.cache.configuration.Factory;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

public class FeedCacheExpiryPolicyFactory implements Factory<ExpiryPolicy> {
    @Override
    public ExpiryPolicy create() {
        return new CustomExpiryPolicy(Duration.ONE_DAY, null, Duration.ONE_DAY);
    }
}
