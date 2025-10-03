package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ColeccionController {

    private final Fachada fachada;
    private static final Logger logger = LoggerFactory.getLogger(ColeccionController.class);

    public ColeccionController(Fachada fachada) {
        this.fachada = fachada;
    }

    @GetMapping("/colecciones/{nombre}/hechos")
    public ResponseEntity<List<HechoDTO>> listarHechosPorColeccion(@PathVariable String nombre) {
        logger.info("Request: listar hechos para coleccion='{}'", nombre);
        List<HechoDTO> hechos = fachada.hechos(nombre);
        logger.info("Response: se retornan {} hechos para coleccion='{}'", hechos == null ? 0 : hechos.size(), nombre);
        return ResponseEntity.ok(hechos);
    }

}