package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.dtos.ConsensosEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import ar.edu.utn.dds.k3003.app.Fachada;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ConsensoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Fachada fachadaMock;

    @Test
    public void configurarConsenso_DeberiaRetornarNoContent() throws Exception {
        String json = "{ \"tipo\": \"TODOS\", \"coleccion\": \"noticias\" }";

        mockMvc.perform(patch("/consenso")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isNoContent());
    }

}