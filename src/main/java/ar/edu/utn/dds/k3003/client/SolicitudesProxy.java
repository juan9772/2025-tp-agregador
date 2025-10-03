package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.EstadoSolicitudBorradoEnum;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.http.HttpStatus;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

public class SolicitudesProxy implements FachadaSolicitudes {
    final private String endpoint;
    private final SolicitudesRetrofitClient service;

    public SolicitudesProxy(ObjectMapper objectMapper) {

        var env = System.getenv();
        String base = env.getOrDefault("SolicitudesProxy", "https://localhost:8081/");
        if (!base.endsWith("/")) {
            base = base + "/";
        }

        this.endpoint = base;

        try {
            var retrofit =
                    new Retrofit.Builder()
                            .baseUrl(this.endpoint)
                            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                            .build();

            this.service = retrofit.create(SolicitudesRetrofitClient.class);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Endpoint inválido para Solicitudes: '" + this.endpoint + "' - " + e.getMessage(), e);
        }
    }
    @Override
    public SolicitudDTO agregar(SolicitudDTO solicitudDTO) {
        return null;
    }

    @Override
    public SolicitudDTO modificar(String solicitudId, EstadoSolicitudBorradoEnum esta, String descripcion) throws NoSuchElementException {
        return null;
    }

    @Override
    public List<SolicitudDTO> buscarSolicitudXHecho(String hechoId) {
        try {
            Response<List<SolicitudDTO>> response = service.obtenerSolicitudPorHecho(hechoId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            if (response.code() == HttpStatus.NOT_FOUND.getCode()) {
                return List.of();
            }
            throw new RuntimeException("Error al buscar Solicitudes por hechoId: " + response.message());
        } catch (IOException e) {
            throw new RuntimeException("Error de I/O al conectarse con el componente de Solicitudes.", e);
        }
    }

    @Override
    public SolicitudDTO buscarSolicitudXId(String solicitudId) {
        return null;
    }

    @Override
    public boolean estaActivo(String unHechoId) {
        return false;
    }

    @Override
    public void setFachadaFuente(FachadaFuente fuente) {

    }
}
