package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class ColeccionController {

    private final Fachada fachada;

    public ColeccionController(Fachada fachada) {
        this.fachada = fachada;
    }

    @GetMapping("/colecciones/{nombre}/hechos")
    public ResponseEntity<List<HechoDTO>> listarHechosPorColeccion(@PathVariable String nombre) {
        return ResponseEntity.ok(fachada.hechos(nombre));
    }

}