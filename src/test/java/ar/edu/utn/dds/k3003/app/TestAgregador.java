package ar.edu.utn.dds.k3003.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.edu.utn.dds.k3003.facades.FachadaFuente;
import ar.edu.utn.dds.k3003.facades.dtos.ConsensosEnum;
import ar.edu.utn.dds.k3003.facades.dtos.FuenteDTO;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;

@ExtendWith({ MockitoExtension.class })
public class TestAgregador {

    Fachada fachada;
    @Mock
    FachadaFuente fuente1;
    @Mock
    FachadaFuente fuente2;
    @Mock
    FachadaFuente fuente3;

    public TestAgregador() {
    }

    @BeforeEach
    void setUp() {
        try {
            fachada = new Fachada();
        } catch (Throwable e) {
            throw new RuntimeException("Error al instanciar FachadaAgregador", e);
        }
    }

    // * TEST DEL METODO AGREGAR
    @Test
    @DisplayName("Agregar Fuente")
    public void testAgregarFuente() {
        FuenteDTO fuenteDTO = this.fachada.agregar(new FuenteDTO("", "fuenteTest", "endpoint"));
        Assertions.assertNotNull(fuenteDTO.id(), "FuenteDTO con un id inicializado.");
        assertEquals("fuenteTest", fuenteDTO.nombre());
        assertEquals("endpoint", fuenteDTO.endpoint());
    }

    // * TEST DEL METODO FUENTES
    @Test
    @DisplayName("Buscar las fuentes disponibles")
    public void testListarFuentes() {
        FuenteDTO fuenteDTO1 = this.fachada.agregar(new FuenteDTO("", "FuenteTest1", "123"));
        FuenteDTO fuenteDTO2 = this.fachada.agregar(new FuenteDTO("", "FuenteTest2", "123"));
        FuenteDTO fuenteDTO3 = this.fachada.agregar(new FuenteDTO("", "FuenteTest3", "123"));
        List<FuenteDTO> listaFuenteDTOs = this.fachada.fuentes();
        Assertions.assertEquals(3, listaFuenteDTOs.size(),
                "No se esta recuperando la cantidad de fuentes correctas.");
        Assertions.assertEquals("FuenteTest1", fuenteDTO1.nombre());
        Assertions.assertEquals("FuenteTest2", fuenteDTO2.nombre());
        Assertions.assertEquals("FuenteTest3", fuenteDTO3.nombre());
    }

    // * TEST DEL METODO BUSCAR FUENTE POR ID
    @Test
    @DisplayName("Buscar fuente por id")
    public void testBuscarPorID() {
        FuenteDTO fuenteDTO = this.fachada.agregar(new FuenteDTO("", "FuenteTest", "123"));
        FuenteDTO fuente = this.fachada.buscarFuenteXId(fuenteDTO.id());
        Assertions.assertEquals(fuente.id(), fuenteDTO.id());
        Assertions.assertEquals("FuenteTest", fuente.nombre(),
                "No se esta recuperando el nombre de la fuente correctamente.");
    }

    @Test
    @DisplayName("Buscar fuente por inexistente")
    public void testBuscarPorIDInexistente() {
        assertThrows(NoSuchElementException.class, () -> {
            fachada.hechos("fuenteInexistente");
        });
    }

    // * TEST DEL METODO ADD FACHADA FUENTES
    @Test
    @DisplayName("Asociar una fachada de fuente a una fuente agregada")
    void testAgregarFachadaFuente() {
        FuenteDTO fuenteDTO = this.fachada.agregar(new FuenteDTO("", "nombreFuente", "endpointFuente"));
        Assertions.assertDoesNotThrow(() -> {
            this.fachada.addFachadaFuentes(fuenteDTO.id(), this.fuente1);
        }, "No debería lanzar excepción al agregar una fachada de fuente");

    }

    // * TEST DEL MÉTODO HECHOS

    @Test
    @DisplayName("Busqueda de hechos de una coleccion inexistente")
    void testBuscarHechosColeccionInexistente() {
        assertThrows(NoSuchElementException.class, () -> {
            fachada.hechos("coleccionInexistente");
        });
    }

    @Test
    @DisplayName("Busqueda de hechos usando 3 fuentes y consenso 'TODO'")
    void testBuscarHechosConsensoTodo3Fuentes_TODO() {
        this.initializeFuentes(ConsensosEnum.TODOS);
        List<String> titulos = this.fachada.hechos("coleccionTest").stream().map(HechoDTO::titulo).toList();
        Assertions.assertEquals(6, titulos.size(),
                "El agregador con concenso todos, esta retornando la cantidad de titulos que no deberia.");
        Assertions.assertTrue(
                titulos.containsAll(List.of("titulo1", "titulo2", "titulo3", "titulo4", "titulo5", "titulo6")),
                "El agregador no retorna todos los hechos que deberia para el consenso TODO.");
    }

    @Test
    @DisplayName("Busqueda de hechos usando 3 fuentes y consenso 'AL_MENOS_2'")
    void testBuscarHechosConsensoTodo3Fuentes_AL_MENOS_2() {
        this.initializeFuentes(ConsensosEnum.AL_MENOS_2);
        List<String> titulos = this.fachada.hechos("coleccionTest").stream().map(HechoDTO::titulo).toList();
        Assertions.assertEquals(3, titulos.size(),
                "El agregador con concenso al menos 2, esta retornando la cantidad de titulos que no deberia.");
        Assertions.assertTrue(
                titulos.containsAll(List.of("titulo1", "titulo2", "titulo3")),
                "El agregador no retorna todos los hechos que deberia para el consenso AL_MENOS_2.");
    }

    private void initializeFuentes(ConsensosEnum consenso) {
        FuenteDTO fuenteDTO1 = this.fachada.agregar(new FuenteDTO("", "FuenteTest1", "123"));
        this.fachada.addFachadaFuentes(fuenteDTO1.id(), this.fuente1);
        FuenteDTO fuenteDTO2 = this.fachada.agregar(new FuenteDTO("", "FuenteTest2", "123"));
        this.fachada.addFachadaFuentes(fuenteDTO2.id(), this.fuente2);
        FuenteDTO fuenteDTO3 = this.fachada.agregar(new FuenteDTO("", "FuenteTest3", "123"));
        this.fachada.addFachadaFuentes(fuenteDTO3.id(), this.fuente3);
        this.fachada.setConsensoStrategy(consenso, "coleccionTest");
        Mockito.when(this.fuente1.buscarHechosXColeccion("coleccionTest"))
                .thenReturn(List.of(new HechoDTO("1", "coleccionTest", "titulo1"),
                        new HechoDTO("2", "coleccionTest", "titulo2"),
                        new HechoDTO("3", "coleccionTest", "titulo3")));
        Mockito.when(this.fuente2.buscarHechosXColeccion("coleccionTest"))
                .thenReturn(List.of(new HechoDTO("4", "coleccionTest", "titulo1"),
                        new HechoDTO("5", "coleccionTest", "titulo3"),
                        new HechoDTO("6", "coleccionTest", "titulo5")));
        Mockito.when(this.fuente3.buscarHechosXColeccion("coleccionTest"))
                .thenReturn(List.of(new HechoDTO("7", "coleccionTest", "titulo1"),
                        new HechoDTO("8", "coleccionTest", "titulo4"),
                        new HechoDTO("9", "coleccionTest", "titulo2"), new HechoDTO("10", "coleccionTest", "titulo6")));
    }

}