package ar.edu.utn.dds;

import java.time.Duration;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import io.micrometer.datadog.DatadogConfig;
import io.micrometer.datadog.DatadogMeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DDMetricsUtils {
	private static final Logger log = LoggerFactory.getLogger(DDMetricsUtils.class);

	private final StepMeterRegistry registry;

	public DDMetricsUtils(String appTag) {
		log.info("Inicializando métricas Datadog para app: {}", appTag);

		var config = new DatadogConfig() {
			@Override
			public Duration step() {
				return Duration.ofSeconds(10);
			}

			@Override
			public String apiKey() {
				String apiKey = System.getenv("DDAPI");
				if (apiKey == null || apiKey.trim().isEmpty()) {
					log.warn("⚠️  Variable de entorno DDAPI no configurada. Las métricas Datadog no funcionarán.");
					return "dummy-key";
				}
				log.info("✅ API key de Datadog configurada correctamente");
				return apiKey;
			}

			@Override
			public String uri() {
				return "https://api.us5.datadoghq.com";
			}

			@Override
			public String get(String k) {
				return null;
			}
		};

		registry = new DatadogMeterRegistry(config, Clock.SYSTEM);
		registry.config().commonTags("app", appTag, "environment", "production", "service", "agregador", "module", "agregador");
		log.info("Datadog registry creado con tags: app={}, environment=production, service=agregador, module=agregador", appTag);

		initInfraMonitoring();
	}

	public StepMeterRegistry getRegistry() {
		return registry;
	}

	private void initInfraMonitoring() {
		log.info("Inicializando métricas de infraestructura...");
		try (var jvmGcMetrics = new JvmGcMetrics();
			 var jvmHeapPressureMetrics = new JvmHeapPressureMetrics()) {
			jvmGcMetrics.bindTo(registry);
			jvmHeapPressureMetrics.bindTo(registry);
		}
		new JvmMemoryMetrics().bindTo(registry);
		new ProcessorMetrics().bindTo(registry);
		new FileDescriptorMetrics().bindTo(registry);
		log.info("✅ Métricas de infraestructura inicializadas");
	}
}