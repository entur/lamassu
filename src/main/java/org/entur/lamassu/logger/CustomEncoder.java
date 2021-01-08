package org.entur.lamassu.logger;

import ch.qos.logback.classic.spi.ILoggingEvent;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

public class CustomEncoder extends LoggingEventCompositeJsonEncoder {
    @Override
    public void start() {
        JsonProviders<ILoggingEvent> providers = getProviders();
        providers.addProvider(new CustomLogLevelJsonProvider());
        super.start();
    }
}
