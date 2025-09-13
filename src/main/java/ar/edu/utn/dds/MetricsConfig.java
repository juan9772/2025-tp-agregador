package ar.edu.utn.dds;

import io.micrometer.statsd.StatsdConfig;
import io.micrometer.statsd.StatsdFlavor;
import io.micrometer.statsd.StatsdMeterRegistry;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterRegistry meterRegistry() {
        CompositeMeterRegistry composite = new CompositeMeterRegistry();

        // DataDog direct registry with API key
        DatadogConfig datadogConfig = new DatadogConfig() {
            @Override
            public Duration step() { return Duration.ofSeconds(10); }
            @Override
            public String apiKey() { return "67d5e0918573fd8f8ec96fd384f0c342"; }
            @Override
            public String get(String key) { return null; }
        };
        composite.add(new DatadogMeterRegistry(datadogConfig, Clock.SYSTEM));

        // StatsD registry for local agent (backup)
        StatsdConfig statsdConfig = new StatsdConfig() {
            @Override
            public Duration step() { return Duration.ofSeconds(10); }
            @Override
            public String host() { return "localhost"; }
            @Override
            public int port() { return 8125; }
            @Override
            public StatsdFlavor flavor() { return StatsdFlavor.DATADOG; }
            @Override
            public String get(String key) { return null; }
        };
        composite.add(new StatsdMeterRegistry(statsdConfig, Clock.SYSTEM));

        return composite;
    }
}
