package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin/metrics")
public class MetricsController {

    private static final Logger log = LoggerFactory.getLogger(MetricsController.class);
    private final MeterRegistry meterRegistry;
    private final Fachada fachada;
    
    // Gauges con AtomicInteger/AtomicLong para que se actualicen correctamente
    private final AtomicInteger totalFuentesGauge = new AtomicInteger(0);
    private final AtomicLong totalColeccionesGauge = new AtomicLong(0);
    private final AtomicInteger totalHechosGauge = new AtomicInteger(0);
    
    // Map para gauges dinámicos con tags (hechos por colección)
    private final Map<String, AtomicInteger> hechosPorColeccionGauges = new ConcurrentHashMap<>();

    @Autowired
    public MetricsController(MeterRegistry meterRegistry, Fachada fachada) {
        this.meterRegistry = meterRegistry;
        this.fachada = fachada;
        
        // Registrar gauges UNA SOLA VEZ en el constructor
        meterRegistry.gauge("dds.fuentes.total.count", totalFuentesGauge);
        meterRegistry.gauge("dds.colecciones.total.count", totalColeccionesGauge);
        meterRegistry.gauge("dds.hechos.activos.count", totalHechosGauge);
        
        // Sincronizar valores iniciales con la base de datos
        try {
            totalFuentesGauge.set(fachada.fuentes().size());
            totalHechosGauge.set(fachada.hechos("").size());
            long colecciones = fachada.hechos("").stream()
                .map(h -> ((ar.edu.utn.dds.k3003.facades.dtos.HechoDTO) h).nombreColeccion())
                .distinct()
                .count();
            totalColeccionesGauge.set(colecciones);
            log.info("✅ MetricsController inicializado - Fuentes: {}, Colecciones: {}, Hechos: {}",
                totalFuentesGauge.get(), totalColeccionesGauge.get(), totalHechosGauge.get());
        } catch (Exception e) {
            log.warn("⚠️ No se pudieron sincronizar valores iniciales: {}", e.getMessage());
        }
    }

    // Total de fuentes registradas
    @GetMapping("/fuentes/total")
    public ResponseEntity<Map<String, Object>> getTotalFuentes() {
        List<?> fuentes = fachada.fuentes();
        int total = fuentes.size();
        totalFuentesGauge.set(total); // Actualizar el gauge existente
        log.debug("📊 Total fuentes actualizado: {}", total);
        return ResponseEntity.ok(Map.of("totalFuentes", total));
    }

    // Total de colecciones activas (por nombre único de colección en hechos)
    @GetMapping("/colecciones/total")
    public ResponseEntity<Map<String, Object>> getTotalColecciones() {
        List<?> hechos = fachada.hechos(""); // "" para traer todos los hechos
        long total = hechos.stream()
                .map(h -> ((ar.edu.utn.dds.k3003.facades.dtos.HechoDTO) h).nombreColeccion())
                .distinct()
                .count();
        totalColeccionesGauge.set(total); // Actualizar el gauge existente
        log.debug("📊 Total colecciones actualizado: {}", total);
        return ResponseEntity.ok(Map.of("totalColecciones", total));
    }

    // Total de hechos activos
    @GetMapping("/hechos/total")
    public ResponseEntity<Map<String, Object>> getTotalHechos() {
        List<?> hechos = fachada.hechos(""); // "" para traer todos los hechos
        int total = hechos.size();
        totalHechosGauge.set(total); // Actualizar el gauge existente
        log.debug("📊 Total hechos actualizado: {}", total);
        return ResponseEntity.ok(Map.of("totalHechos", total));
    }

    // Hechos por colección
    @GetMapping("/hechos/por-coleccion")
    public ResponseEntity<Map<String, Object>> getHechosPorColeccion() {
        List<?> hechos = fachada.hechos(""); // "" para traer todos los hechos
        Map<String, Integer> hechosPorColeccion = new HashMap<>();
        
        for (Object obj : hechos) {
            var hecho = (ar.edu.utn.dds.k3003.facades.dtos.HechoDTO) obj;
            hechosPorColeccion.merge(hecho.nombreColeccion(), 1, Integer::sum);
        }
        
        // Actualizar gauges dinámicos con tags correctos
        hechosPorColeccion.forEach((coleccion, count) -> {
            AtomicInteger gauge = hechosPorColeccionGauges.computeIfAbsent(coleccion, key -> {
                AtomicInteger newGauge = new AtomicInteger(0);
                meterRegistry.gauge("dds.hechos.por.coleccion", 
                    Tags.of("coleccion", coleccion), 
                    newGauge);
                return newGauge;
            });
            gauge.set(count);
        });
        
        log.debug("📊 Hechos por colección actualizado: {}", hechosPorColeccion);
        return ResponseEntity.ok(Map.of("hechosPorColeccion", hechosPorColeccion));
    }
}