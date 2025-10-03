package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.dtos.ConsensosEnum;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/consensos")
public class ConsensoController {

    private final Fachada fachada;

    public ConsensoController(Fachada fachada) {
        this.fachada = fachada;
    }

    @PatchMapping
    public ResponseEntity<Void> configurarConsenso(@RequestBody Map<String, String> body) {
        ConsensosEnum consenso = ConsensosEnum.valueOf(body.get("tipo").toUpperCase());
        String coleccion = body.get("coleccion");

        fachada.setConsensoStrategy(consenso, coleccion);
        return ResponseEntity.noContent().build();
    }

}