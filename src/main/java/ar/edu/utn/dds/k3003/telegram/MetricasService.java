package ar.edu.utn.dds.k3003.telegram;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class MetricasService {

    private final Counter busquedasCounter;
    private final Counter pdisCreadosCounter;
    private final Counter hechosCreadosCounter;
    private final Timer busquedaTimer;
    private final Timer pdiCreacionTimer;

    public MetricasService(MeterRegistry registry) {
        this.busquedasCounter = Counter.builder("busquedas.realizadas")
                .description("Número total de búsquedas realizadas")
                .register(registry);

        this.pdisCreadosCounter = Counter.builder("pdis.creados")
                .description("Número total de PDIs creados")
                .register(registry);

        this.hechosCreadosCounter = Counter.builder("hechos.creados")
                .description("Número total de hechos creados")
                .register(registry);

        this.busquedaTimer = Timer.builder("busqueda.duracion")
                .description("Duración de las búsquedas")
                .register(registry);

        this.pdiCreacionTimer = Timer.builder("pdi.creacion.duracion")
                .description("Duración de la creación de PDIs")
                .register(registry);
    }

    public void incrementarBusquedas() {
        busquedasCounter.increment();
    }

    public void incrementarPdisCreados() {
        pdisCreadosCounter.increment();
    }

    public void incrementarHechosCreados() {
        hechosCreadosCounter.increment();
    }

    public void registrarDuracionBusqueda(long duracion, TimeUnit unit) {
        busquedaTimer.record(duracion, unit);
    }

    public void registrarDuracionCreacionPdi(long duracion, TimeUnit unit) {
        pdiCreacionTimer.record(duracion, unit);
    }
}
