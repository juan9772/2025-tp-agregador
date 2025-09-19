package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.FachadaAgregador;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin/metrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);
    private final MeterRegistry meterRegistry;
    private final FachadaAgregador fachadaAgregador;

    @Autowired
    public MetricsController(MeterRegistry meterRegistry, FachadaAgregador fachadaAgregador) {
        this.meterRegistry = meterRegistry;
        this.fachadaAgregador = fachadaAgregador;
        log.info("✅ MetricsController inicializado para métricas funcionales");
    }

    // Total de fuentes registradas
    @GetMapping("/fuentes/total")
    public ResponseEntity<Map<String, Object>> getTotalFuentes() {
        List<?> fuentes = fachadaAgregador.fuentes();
        int total = fuentes.size();
        meterRegistry.gauge("dds.fuentes.total.count", total);
        return ResponseEntity.ok(Map.of("totalFuentes", total));
    }

    // Total de colecciones activas (por nombre único de colección en hechos)
    @GetMapping("/colecciones/total")
    public ResponseEntity<Map<String, Object>> getTotalColecciones() {
        List<?> hechos = fachadaAgregador.hechos(""); // "" para traer todos los hechos
        long total = hechos.stream()
                .map(h -> ((ar.edu.utn.dds.k3003.facades.dtos.HechoDTO) h).nombreColeccion())
                .distinct()
                .count();
        meterRegistry.gauge("dds.colecciones.total.count", total);
        return ResponseEntity.ok(Map.of("totalColecciones", total));
    }

    // Total de hechos activos
    @GetMapping("/hechos/total")
    public ResponseEntity<Map<String, Object>> getTotalHechos() {
        List<?> hechos = fachadaAgregador.hechos(""); // "" para traer todos los hechos
        int total = hechos.size();
        meterRegistry.gauge("dds.hechos.activos.count", total);
        return ResponseEntity.ok(Map.of("totalHechos", total));
    }

    // Hechos por colección
    @GetMapping("/hechos/por-coleccion")
    public ResponseEntity<Map<String, Object>> getHechosPorColeccion() {
        List<?> hechos = fachadaAgregador.hechos(""); // "" para traer todos los hechos
        Map<String, Integer> hechosPorColeccion = new HashMap<>();
        for (Object obj : hechos) {
            var hecho = (ar.edu.utn.dds.k3003.facades.dtos.HechoDTO) obj;
            hechosPorColeccion.merge(hecho.nombreColeccion(), 1, Integer::sum);
        }
        hechosPorColeccion.forEach((coleccion, count) ->
                meterRegistry.gauge("dds.hechos.por.coleccion", List.of(), count));
        return ResponseEntity.ok(Map.of("hechosPorColeccion", hechosPorColeccion));
    }
}