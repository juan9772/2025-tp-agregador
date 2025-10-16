package ar.edu.utn.dds.k3003.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Collections;

@Service
public class ApiClientService {

    private final WebClient appClient;
    private final WebClient agregadorClient;
    private final WebClient pdiClient;
    private final WebClient solicitudesClient;
    private final WebClient fuenteClient;

    public ApiClientService(
            @Value("${app.baseUrl:${APP_BASE_URL:http://localhost:8080/api}}") String appBase,
            @Value("${agregador.baseUrl:${AGREGADOR_BASE_URL:https://two025-tp-agregador.onrender.com/api}}") String agregadorBase,
            @Value("${pdi.baseUrl:${PDI_BASE_URL:https://two025-dds-tp-procesadorpdi.onrender.com/api}}") String pdiBase,
            @Value("${solicitudes.baseUrl:${SOLICITUDES_BASE_URL:https://grupo12-solicitudes.onrender.com/api}}") String solicitudesBase,
            @Value("${fuente.baseUrl:${FUENTE_BASE_URL:https://two025-tp-fuente.onrender.com/api}}") String fuenteBase
    ) {
        this.appClient = WebClient.builder().baseUrl(appBase).build();
        this.agregadorClient = WebClient.builder().baseUrl(agregadorBase).build();
        this.pdiClient = WebClient.builder().baseUrl(pdiBase).build();
        this.solicitudesClient = WebClient.builder().baseUrl(solicitudesBase).build();
        this.fuenteClient = WebClient.builder().baseUrl(fuenteBase).build();
    }

    // --- Hechos (app local o agregador) ---
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarHechos() {
        try {
            return appClient.get().uri("/hechos")
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarHechosPorColeccion(String nombreColeccion) {
        try {
            return agregadorClient.get().uri("/colecciones/{nombre}/hechos", nombreColeccion)
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerHecho(String id) {
        try {
            return appClient.get().uri("/hechos/{id}", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearHecho(Map<String, Object> payload) {
        try {
            return appClient.post().uri("/hechos")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> modificarEstado(String id, String estado) {
        try {
            return appClient.patch().uri("/hechos/{id}", id)
                    .bodyValue(Map.of("estado", estado))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    // --- Solicitudes ---
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarSolicitudesPorHecho(String hechoId) {
        try {
            return solicitudesClient.get().uri(uriBuilder -> uriBuilder.path("/solicitudes").queryParam("hechoId", hechoId).build())
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearSolicitud(Map<String, Object> payload) {
        try {
            return solicitudesClient.post().uri("/solicitudes")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerSolicitud(String id) {
        try {
            return solicitudesClient.get().uri("/solicitudes/{id}", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> actualizarSolicitud(String id, Map<String, Object> payload) {
        try {
            return solicitudesClient.patch().uri("/solicitudes/{id}", id)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    public Boolean estaActivoSolicitudPorHecho(String hechoId) {
        try {
            return solicitudesClient.get().uri("/solicitudes/hechos/{id}/estaActivo", hechoId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (WebClientResponseException e) {
            return Boolean.FALSE;
        }
    }

    // --- Procesador PdI ---
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarPdis() {
        try {
            return pdiClient.get().uri("/pdis")
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> crearPdi(Map<String, Object> payload) {
        try {
            return pdiClient.post().uri("/pdis")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> buscarPdiPorId(String id) {
        try {
            return pdiClient.get().uri("/pdis/{id}", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> buscarPdisPorHecho(String hechoId) {
        try {
            return pdiClient.get().uri("/hechos/{hechoId}/pdis", hechoId)
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            return Collections.emptyList();
        }
    }

    // --- Fuentes / Consensos (agregador) ---
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> listarFuentes() {
        try {
            return agregadorClient.get().uri("/consensos")
                    .retrieve()
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .collectList()
                    .block();
        } catch (WebClientResponseException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> agregarFuente(Map<String, Object> payload) {
        try {
            return agregadorClient.post().uri("/consensos")
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            return null;
        }
    }
}
