package org.entur.lamassu.config.cache;

import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;

public class CustomExpiryPolicy implements ExpiryPolicy {
    private final Duration expiryForCreation;
    private final Duration expiryForAccess;
    private final Duration expiryForUpdate;

    public CustomExpiryPolicy(Duration expiryForCreation, Duration expiryForAccess, Duration expiryForUpdate) {
        this.expiryForCreation = expiryForCreation;
        this.expiryForAccess = expiryForAccess;
        this.expiryForUpdate = expiryForUpdate;
    }

    @Override
    public Duration getExpiryForCreation() {
        return expiryForCreation;
    }

    @Override
    public Duration getExpiryForAccess() {
        return expiryForAccess;
    }

    @Override
    public Duration getExpiryForUpdate() {
        return expiryForUpdate;
    }
}
