package org.entur.lamassu.listener;

public interface CacheListener<T> {
    void startListening();
    void stopListening();
}
