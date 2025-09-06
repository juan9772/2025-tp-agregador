package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.dtos.FuenteDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.utn.dds.k3003.app.Fachada;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FuenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private Fachada fachadaMock;

    @Test
    public void fuentes_DeberiaRetornarListaVacia() throws Exception {
        when(fachadaMock.fuentes()).thenReturn(List.of());

        mockMvc.perform(get("/fuentes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void agregarFuente_DeberiaRetornarFuenteCreada() throws Exception {
        FuenteDTO fuenteDTO = new FuenteDTO("1", "Fuente Test", "http://localhost:9999");

        when(fachadaMock.agregar(any(FuenteDTO.class))).thenReturn(fuenteDTO);

        mockMvc.perform(post("/fuentes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuenteDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Fuente Test"))
                .andExpect(jsonPath("$.endpoint").value("http://localhost:9999"));
    }
}