package ar.edu.utn.dds.k3003.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.utn.dds.k3003.app.Fachada;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.NoSuchElementException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ColeccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Fachada fachadaMock;

    @Test
    public void listarHechosPorColeccion_DeberiaRetornarNotFoundSiNoHayHechos() throws Exception {
        when(fachadaMock.hechos(anyString())).thenThrow(new NoSuchElementException("Colección no encontrada"));

        mockMvc.perform(get("/coleccion/test/hechos"))
                .andExpect(status().isNotFound());
    }
}