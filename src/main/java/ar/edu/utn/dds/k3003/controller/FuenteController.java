package ar.edu.utn.dds.k3003.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.FuenteDTO;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/fuentes")
public class FuenteController {

    private final Fachada fachada;

    public FuenteController(Fachada fachada) {
        this.fachada = fachada;
    }

    @GetMapping
    public ResponseEntity<List<FuenteDTO>> fuentes() {
        return ResponseEntity.ok(fachada.fuentes());
    }

    @PostMapping
    public ResponseEntity<FuenteDTO> agregarFuente(@RequestBody FuenteDTO fuenteDTO) {
        return ResponseEntity.ok(fachada.agregar(fuenteDTO));
    }

}
