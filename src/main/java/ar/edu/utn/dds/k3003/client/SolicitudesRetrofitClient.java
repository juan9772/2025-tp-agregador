package ar.edu.utn.dds.k3003.client;

import ar.edu.utn.dds.k3003.facades.dtos.SolicitudDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface SolicitudesRetrofitClient {
    @GET("/api/solicitudes/{hechoId}")
    Call<List<SolicitudDTO>> obtenerSolicitudPorHecho(@Path("hechoId") String id);
}
