package ar.edu.utn.dds.k3003.busqueda.controllers;

import ar.edu.utn.dds.k3003.busqueda.document.HechoBusqueda;
import ar.edu.utn.dds.k3003.busqueda.services.BusquedaService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/busqueda")
public class BusquedaController {

    private final BusquedaService busquedaService;

    public BusquedaController(BusquedaService busquedaService) {
        this.busquedaService = busquedaService;
    }

    @GetMapping
    public ResponseEntity<Page<HechoBusqueda>> buscar(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<HechoBusqueda> resultados = busquedaService.buscar(query, page, size);
        return ResponseEntity.ok(resultados);
    }
}
