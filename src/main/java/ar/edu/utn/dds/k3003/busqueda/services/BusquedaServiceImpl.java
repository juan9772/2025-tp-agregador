package ar.edu.utn.dds.k3003.busqueda.services;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import ar.edu.utn.dds.k3003.busqueda.repository.HechoBusquedaRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusquedaServiceImpl implements BusquedaService {

    private final HechoBusquedaRepository hechoBusquedaRepository;
    private final MeterRegistry meterRegistry;

    public BusquedaServiceImpl(HechoBusquedaRepository hechoBusquedaRepository, MeterRegistry meterRegistry) {
        this.hechoBusquedaRepository = hechoBusquedaRepository;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Page<HechoBusqueda> buscar(String query, int page, int size) {
        // Iniciar temporizador para medir latencia
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // 1. Parsear la consulta para separar texto y tags
            List<String> parts = Arrays.asList(query.split("\\s+"));
            List<String> tags = parts.stream()
                    .filter(p -> p.startsWith("tag:"))
                    .map(p -> p.substring(4).toLowerCase()) // Normalizar a minúsculas
                    .collect(Collectors.toList());

            String searchText = parts.stream()
                    .filter(p -> !p.startsWith("tag:"))
                    .collect(Collectors.joining(" ")).trim();

            // 2. Crear el objeto de paginación
            Pageable pageable = PageRequest.of(page, size);

            // 3. Ejecutar la consulta adecuada
            boolean hasText = !searchText.isEmpty();
            boolean hasTags = !tags.isEmpty();

            Page<HechoBusqueda> resultados;
            String queryType; // Para etiquetar métricas

            if (hasText && hasTags) {
                resultados = hechoBusquedaRepository.searchByTextAndTags(searchText, tags, pageable);
                queryType = "text_and_tags";
            } else if (hasText) {
                resultados = hechoBusquedaRepository.searchByText(searchText, pageable);
                queryType = "text_only";
            } else if (hasTags) {
                resultados = hechoBusquedaRepository.searchByTags(tags, pageable);
                queryType = "tags_only";
            } else {
                // Si no hay ni texto ni tags, devolver una página vacía o todos los hechos, según se prefiera.
                // Aquí devolvemos una página vacía para evitar una carga masiva no intencionada.
                resultados = Page.empty(pageable);
                queryType = "empty";
            }

            // Registrar métricas de éxito
            meterRegistry.counter("dds.busqueda.requests", 
                "query_type", queryType, 
                "status", "success",
                "has_results", String.valueOf(resultados.hasContent())
            ).increment();

            // Registrar latencia
            sample.stop(meterRegistry.timer("dds.busqueda.request.latency", 
                "query_type", queryType
            ));

            return resultados;
            
        } catch (Exception e) {
            // Registrar error
            meterRegistry.counter("dds.busqueda.requests", 
                "query_type", "unknown", 
                "status", "error"
            ).increment();
            
            // Re-lanzar excepción
            throw e;
        }
    }
}
