package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.dtos.ColeccionDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import io.javalin.http.HttpStatus;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

public class FuenteProxy implements FachadaFuente {
    final private String endpoint;
    private final FuenteRetrofitClient service;

    // Primer constructor: Usa el endpoint del entorno (para inyección por Spring)
    public FuenteProxy(ObjectMapper objectMapper) {
        var env = System.getenv();
        this.endpoint = env.getOrDefault("Fuente", "https://two025-tp-entrega-2-juan9772-1.onrender.com/");
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(FuenteRetrofitClient.class);
    }

    // --- NUEVO CONSTRUCTOR ---
    // Segundo constructor: Permite pasar el endpoint dinámicamente.
    public FuenteProxy(ObjectMapper objectMapper, String endpoint) {
        this.endpoint = endpoint; // Usa el endpoint que viene como parámetro
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        var retrofit =
                new Retrofit.Builder()
                        .baseUrl(this.endpoint)
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .build();

        this.service = retrofit.create(FuenteRetrofitClient.class);
    }

    @Override
    public List<ColeccionDTO> colecciones() {
        try {
            Response<List<ColeccionDTO>> response = service.getCollecciones().execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            throw new RuntimeException("Error al obtener colecciones: " + response.message());
        } catch (IOException e) {
            throw new RuntimeException("Error de I/O al conectarse con el componente de fuentes.", e);
        }
    }

    @Override
    public List<HechoDTO> buscarHechosXColeccion(String coleccionId) throws NoSuchElementException {
        try {
            Response<List<HechoDTO>> response = service.getHechosPorColleccion(coleccionId).execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            if (response.code() == HttpStatus.NOT_FOUND.getCode()) {
                throw new NoSuchElementException("No se encontraron hechos para la colección: " + coleccionId);
            }
            throw new RuntimeException("Error al buscar hechos para la colección: " + response.message());
        } catch (IOException e) {
            throw new RuntimeException("Error de I/O al conectarse con el componente de fuentes.", e);
        }
    }
    @Override
    public ColeccionDTO agregar(ColeccionDTO coleccionDTO) {
        return null;
    }

    @Override
    public ColeccionDTO buscarColeccionXId(String coleccionId) throws NoSuchElementException {
        return null;
    }

    @Override
    public HechoDTO agregar(HechoDTO hechoDTO) {
        return null;
    }

    @Override
    public HechoDTO buscarHechoXId(String hechoId) throws NoSuchElementException {
        return null;
    }

    @Override
    public void setProcesadorPdI(FachadaProcesadorPdI procesador) {

    }

    @Override
    public PdIDTO agregar(PdIDTO pdIDTO) throws IllegalStateException {
        return null;
    }
}
