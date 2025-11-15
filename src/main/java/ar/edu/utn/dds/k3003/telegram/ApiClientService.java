package ar.edu.utn.dds.k3003.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class ApiClientService {

    private final WebClient appClient;
    private final WebClient agregadorClient;
    private final WebClient pdiClient;
    private final WebClient solicitudesClient;
    private WebClient fuenteClient; // Modificado para ser dinámico
    private final MetricasService metricasService;

    public ApiClientService(
            @Value("${app.baseUrl:${APP_BASE_URL:https://two025-tp-agregador.onrender.com/api}}") String appBase,
            @Value("${agregador.baseUrl:${AGREGADOR_BASE_URL:https://two025-tp-agregador.onrender.com/api}}") String agregadorBase,
            @Value("${pdi.baseUrl:${PDI_BASE_URL:https://two025-dds-tp-procesadorpdi.onrender.com/api}}") String pdiBase,
            @Value("${solicitudes.baseUrl:${SOLICITUDES_BASE_URL:https://grupo12-solicitudes.onrender.com/api}}") String solicitudesBase,
            @Value("${fuente.baseUrl:${FUENTE_BASE_URL:https://two025-tp-fuente.onrender.com/api}}") String fuenteBase,
            MetricasService metricasService
    ) {
        this.appClient = WebClient.builder().baseUrl(appBase).build();
        this.agregadorClient = WebClient.builder().baseUrl(agregadorBase).build();
        this.pdiClient = WebClient.builder().baseUrl(pdiBase).build();
        this.solicitudesClient = WebClient.builder().baseUrl(solicitudesBase).build();
        this.fuenteClient = WebClient.builder().baseUrl(fuenteBase).build(); // Fuente por defecto
        this.metricasService = metricasService;
    }

    public void setFuenteActiva(String baseUrl) {
        this.fuenteClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    // --- Colecciones ---
    public List<Map<String, Object>> listarColecciones() {
        return fuenteClient.get().uri("/colecciones")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    // --- Hechos ---
    public List<Map<String, Object>> listarHechos() {
        return fuenteClient.get().uri("/hechos")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    public List<Map<String, Object>> listarHechosPorColeccion(String nombreColeccion) {
        return fuenteClient.get().uri("/colecciones/{nombre}/hechos", nombreColeccion)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    public Map<String, Object> obtenerHecho(String id) {
        return fuenteClient.get().uri("/hechos/{id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> crearHecho(Map<String, Object> payload) {
        metricasService.incrementarHechosCreados();
        return fuenteClient.post().uri("/hechos")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> modificarEstado(String id, String estado) {
        return fuenteClient.patch().uri("/hechos/{id}", id)
                .bodyValue(Map.of("estado", estado))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    // --- Solicitudes ---
    public List<Map<String, Object>> listarSolicitudesPorHecho(String hechoId) {
        return solicitudesClient.get().uri(uriBuilder -> uriBuilder.path("/solicitudes").queryParam("hechoId", hechoId).build())
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    public Map<String, Object> crearSolicitud(Map<String, Object> payload) {
        return solicitudesClient.post().uri("/solicitudes")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> obtenerSolicitud(String id) {
        return solicitudesClient.get().uri("/solicitudes/{id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> actualizarSolicitud(String id, Map<String, Object> payload) {
        return solicitudesClient.patch().uri("/solicitudes/{id}", id)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Boolean estaActivoSolicitudPorHecho(String hechoId) {
        return solicitudesClient.get().uri("/solicitudes/hechos/{id}/estaActivo", hechoId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    // --- Procesador PdI ---
    public List<Map<String, Object>> listarPdis() {
        return pdiClient.get().uri("/pdis")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    public Map<String, Object> crearPdi(Map<String, Object> payload) {
        long startTime = System.nanoTime();
        try {
            metricasService.incrementarPdisCreados();
            return pdiClient.post().uri("/pdis")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } finally {
            long duration = System.nanoTime() - startTime;
            metricasService.registrarDuracionCreacionPdi(duration, TimeUnit.NANOSECONDS);
        }
    }

    public Map<String, Object> buscarPdiPorId(String id) {
        return pdiClient.get().uri("/pdis/{id}", id)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public List<Map<String, Object>> buscarPdisPorHecho(String hechoId) {
        return pdiClient.get().uri("/hechos/{hechoId}/pdis", hechoId)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    // --- Fuentes / Consensos (agregador) ---
    public List<Map<String, Object>> listarFuentes() {
        return agregadorClient.get().uri("/fuentes")
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .collectList()
                .block();
    }

    public Map<String, Object> crearFuente(Map<String, Object> payload) {
        return agregadorClient.post().uri("/fuentes")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    // --- Búsqueda (agregador) ---
    public Map<String, Object> buscar(String query, int page, int size) {
        return agregadorClient.get()
                .uri(uriBuilder -> uriBuilder.path("/busqueda")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}
