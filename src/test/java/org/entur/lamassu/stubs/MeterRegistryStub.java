package org.entur.lamassu.stubs;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.distribution.pause.PauseDetector;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;
import java.util.function.ToDoubleFunction;
import java.util.function.ToLongFunction;

public class MeterRegistryStub {

    @Bean
    public MeterRegistry prometheusMeterRegistry() {
        return new MeterRegistry(Clock.SYSTEM) {
            @Override
            protected <T> Gauge newGauge(Meter.Id id, T t, ToDoubleFunction<T> toDoubleFunction) {
                return null;
            }

            @Override
            protected Counter newCounter(Meter.Id id) {
                return null;
            }

            @Override
            protected Timer newTimer(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig, PauseDetector pauseDetector) {
                return null;
            }

            @Override
            protected DistributionSummary newDistributionSummary(Meter.Id id, DistributionStatisticConfig distributionStatisticConfig, double v) {
                return null;
            }

            @Override
            protected Meter newMeter(Meter.Id id, Meter.Type type, Iterable<Measurement> iterable) {
                return null;
            }

            @Override
            protected <T> FunctionTimer newFunctionTimer(Meter.Id id, T t, ToLongFunction<T> toLongFunction, ToDoubleFunction<T> toDoubleFunction, TimeUnit timeUnit) {
                return null;
            }

            @Override
            protected <T> FunctionCounter newFunctionCounter(Meter.Id id, T t, ToDoubleFunction<T> toDoubleFunction) {
                return null;
            }

            @Override
            protected TimeUnit getBaseTimeUnit() {
                return null;
            }

            @Override
            protected DistributionStatisticConfig defaultHistogramConfig() {
                return null;
            }
        };
    }
}
